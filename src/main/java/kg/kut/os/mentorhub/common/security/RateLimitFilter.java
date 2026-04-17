package kg.kut.os.mentorhub.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.kut.os.mentorhub.common.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Servlet-level rate limiting filter that runs BEFORE Spring Security.
 * Limits requests by client IP address using Bucket4j token-bucket algorithm.
 * Returns HTTP 429 with ApiErrorResponse JSON body when limit is exceeded.
 */
public class RateLimitFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final RateLimitProperties properties;
    private final Set<String> rateLimitedPaths;
    private final ScheduledExecutorService cleanupExecutor;

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
        this.rateLimitedPaths = Set.copyOf(properties.getPaths());

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        // Schedule cleanup of idle buckets every 10 minutes to prevent memory leaks
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        });
        this.cleanupExecutor.scheduleAtFixedRate(this::cleanupIdleBuckets, 10, 10, TimeUnit.MINUTES);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();

        // Only rate-limit configured paths
        if (!rateLimitedPaths.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Only rate-limit POST requests (login, register, etc.)
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        // Key = IP + path, so limits are per-endpoint
        String bucketKey = clientIp + ":" + path;
        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> createBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;

            log.warn("Rate limit exceeded for IP={} on path={}. Retry after {} seconds", clientIp, path, waitSeconds);

            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Retry-After", String.valueOf(waitSeconds));
            response.setHeader("X-Rate-Limit-Remaining", "0");

            ErrorResponse errorResponse = new ErrorResponse(
                    "RATE_LIMIT_EXCEEDED",
                    "Слишком много запросов. Попробуйте через " + waitSeconds + " с.",
                    LocalDateTime.now()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    @Override
    public void destroy() {
        cleanupExecutor.shutdownNow();
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.simple(properties.getRequestsPerMinute(), Duration.ofMinutes(1))
                )
                .build();
    }

    /**
     * Resolve the real client IP, considering reverse proxies (GCP Load Balancer, nginx).
     * X-Forwarded-For format: "client, proxy1, proxy2" — we take the first one.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Take the first (leftmost) IP — the original client
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Remove buckets that are fully refilled (i.e. the IP hasn't made requests recently).
     * This prevents unbounded growth of the ConcurrentHashMap.
     */
    private void cleanupIdleBuckets() {
        int sizeBefore = buckets.size();
        buckets.entrySet().removeIf(entry -> {
            Bucket bucket = entry.getValue();
            // If bucket has all tokens available, the IP is idle — safe to remove
            return bucket.getAvailableTokens() >= properties.getRequestsPerMinute();
        });
        int removed = sizeBefore - buckets.size();
        if (removed > 0) {
            log.debug("Rate limit cleanup: removed {} idle buckets, {} remaining", removed, buckets.size());
        }
    }
}
