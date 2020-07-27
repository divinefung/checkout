package com.shop.checkout;

import com.google.gson.Gson;
import com.shop.models.Basket;
import com.shop.models.BundleDeal;
import com.shop.models.DiscountDeal;
import com.shop.models.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class StoreConfiguration {

    private URL res = getClass().getClassLoader().getResource("inventory.json");

    @Bean
    public AtomicLong counter(){
        return new AtomicLong();
    }

    @Bean
    public List<Product> products(AtomicLong counter) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(res.getPath()));
        Product[] productArray = new Gson().fromJson(reader, Product[].class);
        List<Product> products = new ArrayList<>();
        for (Product product : Arrays.asList(productArray)){
            Product.builder()
                    .id(new Long(counter.incrementAndGet()).intValue())
                    .name(product.getName())
                    .quantity(product.getQuantity())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .build();
            products.add(product);
        }
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
