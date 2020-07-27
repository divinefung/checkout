package com.shop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateBasketRequest {

    Integer userId;

    Integer productId;

    Integer quantity;

}
