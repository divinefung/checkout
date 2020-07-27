package com.shop.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BundleDeal {

    private Product product;

    private Product gift;

}
