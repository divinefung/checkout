package com.shop.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Basket {

    private Integer userId;

    // Product ID to quantity map
    private Map<Integer, Integer> items;

    public void clear(){
        items.clear();
    }
}
