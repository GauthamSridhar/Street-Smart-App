package com.shopapp.ShopService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.shopapp.ShopService", "com.shopapp.common"})
@EnableFeignClients
@EnableScheduling
public class ShopServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ShopServiceApplication.class, args);
  }
}
