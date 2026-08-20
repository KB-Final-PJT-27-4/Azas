package com.azas.domain.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("firebase")
public class FirebaseAdminConfiguration {

    @Bean(destroyMethod = "delete")
    public FirebaseApp firebaseApp(
            @Value("${FIREBASE_PROJECT_ID:}")
            String firebaseProjectId
    ) throws IOException {
        FirebaseOptions.Builder optionsBuilder =
                FirebaseOptions.builder()
                        .setCredentials(
                                GoogleCredentials
                                        .getApplicationDefault()
                        );

        if (
                firebaseProjectId != null
                        && !firebaseProjectId.isBlank()
        ) {
            optionsBuilder.setProjectId(
                    firebaseProjectId.trim()
            );
        }

        return FirebaseApp.initializeApp(
                optionsBuilder.build()
        );
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(
            FirebaseApp firebaseApp
    ) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
