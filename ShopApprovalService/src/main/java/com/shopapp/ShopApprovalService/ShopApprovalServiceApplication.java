package com.shopapp.ShopApprovalService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ShopApprovalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopApprovalServiceApplication.class, args);
	}

}
