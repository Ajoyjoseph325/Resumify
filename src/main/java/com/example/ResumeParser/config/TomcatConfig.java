package com.example.ResumeParser.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);
                connector.setMaxPostSize(1024 * 1024 * 1024); // 1GB
                connector.setProperty("maxSavePostSize", String.valueOf(1024 * 1024 * 1024));
            }
        };
    }
}
