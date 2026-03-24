package kg.kut.os.mentorhub.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GoogleCloudConfig {

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "gcs")
    public Storage googleCloudStorage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}

