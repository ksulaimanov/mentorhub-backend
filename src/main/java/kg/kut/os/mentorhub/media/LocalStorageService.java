package kg.kut.os.mentorhub.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local")
public class LocalStorageService implements StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    private final Path uploadRoot;
    private final String publicBaseUrl;
    private final long avatarMaxSizeBytes;

    public LocalStorageService(
            @Value("${app.storage.local.upload-dir}") String uploadDir,
            @Value("${app.storage.local.public-base-url}") String publicBaseUrl,
            @Value("${app.storage.local.avatar-max-size-bytes}") long avatarMaxSizeBytes
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl;
        this.avatarMaxSizeBytes = avatarMaxSizeBytes;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadRoot.resolve("avatars"));
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось создать директорию для загрузок", e);
        }
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        validateAvatar(file);

        String extension = resolveExtension(file);
        String filename = UUID.randomUUID() + "." + extension;
        String key = "avatars/users/" + userId + "/" + filename;

        Path targetPath = uploadRoot.resolve(key).normalize();

        try {
            Files.createDirectories(targetPath.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return key;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить файл"
            );
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        Path targetPath = uploadRoot.resolve(key).normalize();

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось удалить файл"
            );
        }
    }

    @Override
    public String buildPublicUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        return publicBaseUrl + "/uploads/" + key;
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не выбран");
        }

        if (file.getSize() > avatarMaxSizeBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Размер файла превышает 10 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Допустимы только JPG, PNG или WEBP"
            );
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось определить расширение файла");
        }

        String normalized = extension.toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(normalized)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Недопустимое расширение файла"
            );
        }

        return normalized;
    }
}