package com.backendie;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;

public class EnvLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger log = LoggerFactory.getLogger(EnvLogger.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();

        String url = env.getProperty("spring.datasource.url");
        String user = env.getProperty("spring.datasource.username");
        String pass = env.getProperty("spring.datasource.password");

        log.info("spring.datasource.url={}", url);
        log.info("spring.datasource.username={}", user);
        log.info("spring.datasource.password={}", pass == null ? "null" : "**** (oculto)");
        // También muestra variables de entorno del sistema (útil para verificar .env)
        log.debug("OS ENV SPRING_DATASOURCE_URL={}", System.getenv("SPRING_DATASOURCE_URL"));
        log.debug("OS ENV SPRING_DATASOURCE_USERNAME={}", System.getenv("SPRING_DATASOURCE_USERNAME"));
        // NO imprimir System.getenv("SPRING_DATASOURCE_PASSWORD") en producción
    }
}