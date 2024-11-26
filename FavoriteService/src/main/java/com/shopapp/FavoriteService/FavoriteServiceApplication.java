package com.shopapp.FavoriteService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FavoriteServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FavoriteServiceApplication.class, args);
	}

}
