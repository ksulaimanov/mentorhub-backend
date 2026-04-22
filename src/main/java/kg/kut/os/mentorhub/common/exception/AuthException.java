package kg.kut.os.mentorhub.common.exception;

/**
 * Auth-specific exception that carries a semantic error code.
 * GlobalExceptionHandler maps this to ApiErrorResponse with the exact code,
 * so frontend can show precise error messages.
 */
public class AuthException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public AuthException(String errorCode, int httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    // --- Factory methods for common auth errors ---

    public static AuthException invalidCredentials() {
        return new AuthException("INVALID_CREDENTIALS", 401, "Неверный email или пароль");
    }

    public static AuthException emailNotVerified() {
        return new AuthException("EMAIL_NOT_VERIFIED", 403, "Email ещё не подтверждён");
    }

    public static AuthException accountDisabled() {
        return new AuthException("ACCOUNT_DISABLED", 401, "Аккаунт недоступен");
    }

    public static AuthException emailAlreadyRegistered() {
        return new AuthException("EMAIL_ALREADY_REGISTERED", 409, "Email уже зарегистрирован");
    }

    public static AuthException userNotFound() {
        return new AuthException("USER_NOT_FOUND", 400, "Пользователь с таким email не найден");
    }

    public static AuthException emailAlreadyVerified() {
        return new AuthException("EMAIL_ALREADY_VERIFIED", 400, "Email уже подтверждён");
    }

    public static AuthException codeExpired() {
        return new AuthException("CODE_EXPIRED", 400, "Срок действия кода истёк");
    }

    public static AuthException codeAlreadyUsed() {
        return new AuthException("CODE_ALREADY_USED", 400, "Код уже использован");
    }

    public static AuthException invalidCode() {
        return new AuthException("INVALID_CODE", 400, "Неверный код подтверждения");
    }

    public static AuthException invalidResetCode() {
        return new AuthException("INVALID_CODE", 400, "Неверный код сброса");
    }

    public static AuthException tooManyAttempts() {
        return new AuthException("TOO_MANY_ATTEMPTS", 400, "Превышено количество попыток ввода кода");
    }

    public static AuthException tooManyRequests() {
        return new AuthException("TOO_MANY_REQUESTS", 429, "Код был отправлен недавно. Попробуйте чуть позже");
    }

    public static AuthException invalidRefreshToken() {
        return new AuthException("INVALID_REFRESH_TOKEN", 401, "Недействительный refresh token");
    }

    public static AuthException refreshTokenRevoked() {
        return new AuthException("REFRESH_TOKEN_REVOKED", 401, "Refresh token отозван");
    }

    public static AuthException refreshTokenExpired() {
        return new AuthException("REFRESH_TOKEN_EXPIRED", 401, "Срок действия refresh token истёк");
    }
}

