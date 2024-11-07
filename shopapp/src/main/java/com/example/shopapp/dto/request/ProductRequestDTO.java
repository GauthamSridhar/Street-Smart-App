package com.example.shopapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    private boolean available;
}
