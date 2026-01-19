package org.example.springmultithread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringMultiThreadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMultiThreadApplication.class, args);
    }

}
