package kg.kut.os.mentorhub.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final String storageType;

    public WebConfig(@Value("${app.storage.local.upload-dir:uploads}") String uploadDir,
                     @Value("${app.storage.type:local}") String storageType) {
        this.uploadDir = uploadDir;
        this.storageType = storageType;
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Only serve local files when storage type is "local" — in prod (GCS), skip this
        if (!"local".equalsIgnoreCase(storageType)) {
            return;
        }
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}