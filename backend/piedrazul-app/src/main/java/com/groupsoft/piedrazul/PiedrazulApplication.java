package com.groupsoft.piedrazul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.groupsoft.piedrazul")
@EntityScan(basePackages = "com.groupsoft.piedrazul")
@EnableJpaRepositories(basePackages = "com.groupsoft.piedrazul")
public class PiedrazulApplication {

    public static void main(String[] args) {
        SpringApplication.run(PiedrazulApplication.class, args);
    }
}
