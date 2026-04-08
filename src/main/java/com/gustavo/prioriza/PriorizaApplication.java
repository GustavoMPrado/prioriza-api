package com.gustavo.prioriza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = "com.gustavo")
public class PriorizaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriorizaApplication.class, args);
    }
}
