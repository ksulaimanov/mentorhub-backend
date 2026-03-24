package kg.kut.os.mentorhub.media;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "gcs")
public class GcsStorageService implements StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    private final Storage storage;
    private final String bucketName;
    private final String publicBaseUrl;
    private final long avatarMaxSizeBytes;

    public GcsStorageService(
            Storage storage,
            @Value("${app.storage.gcs.bucket-name}") String bucketName,
            @Value("${app.storage.gcs.public-base-url}") String publicBaseUrl,
            @Value("${app.storage.gcs.avatar-max-size-bytes}") long avatarMaxSizeBytes
    ) {
        this.storage = storage;
        this.bucketName = bucketName;
        this.publicBaseUrl = publicBaseUrl;
        this.avatarMaxSizeBytes = avatarMaxSizeBytes;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        validateAvatar(file);

        String extension = resolveExtension(file);
        String filename = UUID.randomUUID() + "." + extension;
        String key = "avatars/users/" + userId + "/" + filename;

        try (InputStream inputStream = file.getInputStream()) {
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, key))
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, inputStream.readAllBytes());

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

        try {
            BlobId blobId = BlobId.of(bucketName, key);
            storage.delete(blobId);
        } catch (Exception e) {
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

        return publicBaseUrl + "/" + key;
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

