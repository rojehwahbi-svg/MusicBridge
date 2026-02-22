package com.rowa.musicbridge.tidalIntegration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("prod")
public class TidalWebClientConfig {

    /**
     * Configures a WebClient bean for interacting with the Tidal API.
     *
     * @param tidalConfig The TidalConfig instance containing the base URL for the Tidal API.
     * @return A configured WebClient instance for making requests to the Tidal API.
     */
    @Bean
    public WebClient tidalWebClient(TidalConfig tidalConfig) {
        return WebClient.builder()
                .baseUrl(tidalConfig.getBaseUrl())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    /**
     * Configures a TaskScheduler bean for scheduling tasks, such as the initial sync on startup.
     *
     * @return A ThreadPoolTaskScheduler instance for scheduling tasks.
     */
    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

}