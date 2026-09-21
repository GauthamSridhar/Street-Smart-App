package com.shopapp.ShopApprovalService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.shopapp.ShopApprovalService", "com.shopapp.common"})
@EnableFeignClients
@EnableScheduling
public class ShopApprovalServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ShopApprovalServiceApplication.class, args);
  }
}
