package com.transportes.logistics;

import com.transportes.logistics.config.FeignClientConfig; // <--- IMPORTAR
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
// AGREGAR: defaultConfiguration = FeignClientConfig.class
@EnableFeignClients(defaultConfiguration = FeignClientConfig.class)
public class LogisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticsServiceApplication.class, args);
    }
}