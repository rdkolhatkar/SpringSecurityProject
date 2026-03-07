package com.ratnakar.code.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long          productId;
    private String        productName;
    private String        productDetails;
    private BigDecimal    price;
    private Integer       quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
