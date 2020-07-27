package com.shop.checkout;

import com.shop.models.Basket;
import com.shop.models.BundleDeal;
import com.shop.models.DiscountDeal;
import com.shop.models.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class TestStoreConfiguration {

    @Bean
    public AtomicLong counter(){
        return new AtomicLong();
    }

    @Bean
    public List<Product> products(AtomicLong counter){
        List<Product> products = new ArrayList<>();
        products.add(
                Product.builder()
                        .id(new Long(counter.incrementAndGet()).intValue())
                        .name("Apple")
                        .price(5.67)
                        .description("Tasty apple")
                        .quantity(100)
                        .build()
        );
        products.add(
                Product.builder()
                        .id(new Long(counter.incrementAndGet()).intValue())
                        .name("Car")
                        .price(380000.00)
                        .description("Fancy car")
                        .quantity(5)
                        .build()
        );
        return products;
    }

    @Bean
    public List<DiscountDeal> discountDeals(){
        return new ArrayList<>();
    }

    @Bean
    public List<BundleDeal> bundleDeals(){
        return new ArrayList<>();
    }

    @Bean
    public BasketResource basketResource(List<Product> products,
                                         List<DiscountDeal> discountDeals,
                                         List<BundleDeal> bundleDeals){
        List<Basket> baskets = new ArrayList<>();
        Map<Integer, Integer> basketItems1 = new HashMap<>();
        Map<Integer, Integer> basketItems2 = new HashMap<>();
        basketItems1.put(1, 10);
        basketItems2.put(1, 5);
        basketItems2.put(2, 1);
        baskets.add(
            Basket.builder()
                    .userId(1001)
                    .items(basketItems1)
                    .build()
        );
        baskets.add(
            Basket.builder()
                    .userId(1002)
                    .items(basketItems2)
                    .build()
        );
        return new BasketResource(baskets, products, discountDeals, bundleDeals);
    }

    @Bean
    public StoreResource storeResource(List<Product> products,
                                       List<DiscountDeal> discountDeals,
                                       List<BundleDeal> bundleDeals,
                                       AtomicLong counter){
        return new StoreResource(products, discountDeals, bundleDeals, counter);
    }

}
