package com.shop.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscountDeal {

    private Product product;

    private Double discount;

    private final int limit = 2;

    public boolean isEligible(Basket basket){
        if (basket.getItems().get(product.getId()) >= limit){
            return true;
        } else {
            return false;
        }
    }

    public Double adjustment(){
        return product.getPrice() * discount;
    }

}
