package com.shop.dto;

import com.shop.models.Product;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckoutResponse {

    List<Product> purchases;

    List<Product> gifts;

    Double amount;

}
