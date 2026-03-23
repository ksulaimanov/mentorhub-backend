package kg.kut.os.mentorhub.media;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadAvatar(Long userId, MultipartFile file);
    void delete(String key);
    String buildPublicUrl(String key);
}