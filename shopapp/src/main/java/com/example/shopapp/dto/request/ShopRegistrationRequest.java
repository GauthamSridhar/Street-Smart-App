// dto/request/ShopRegistrationRequest.java
package com.example.shopapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopRegistrationRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String address;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @Data
    public static class ProductRequestDTO {
        @NotBlank
        private String name;

        private String description;

        @NotNull
        private BigDecimal price;

        private boolean available;
    }
}
