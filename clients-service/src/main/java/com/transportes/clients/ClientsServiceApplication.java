package com.transportes.clients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.transportes.clients.config.FeignClientConfig;

@SpringBootApplication

@EnableFeignClients(defaultConfiguration = FeignClientConfig.class)
public class ClientsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientsServiceApplication.class, args);
    }
}
