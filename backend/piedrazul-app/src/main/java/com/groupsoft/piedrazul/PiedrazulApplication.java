package com.groupsoft.piedrazul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.groupsoft.piedrazul")
public class PiedrazulApplication {

    public static void main(String[] args) {
        SpringApplication.run(PiedrazulApplication.class, args);
    }
}
