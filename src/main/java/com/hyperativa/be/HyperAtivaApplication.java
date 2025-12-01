package com.hyperativa.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HyperAtivaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HyperAtivaApplication.class, args);
    }
}
