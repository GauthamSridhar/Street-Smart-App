package com.shopapp.RatingService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.shopapp.RatingService", "com.shopapp.common"})
@EnableFeignClients
@EnableScheduling
public class RatingServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(RatingServiceApplication.class, args);
  }
}
