package com.shop.checkout;

import com.shop.dto.CheckoutResponse;
import com.shop.dto.UpdateBasketRequest;
import com.shop.models.Basket;
import com.shop.models.BundleDeal;
import com.shop.models.DiscountDeal;
import com.shop.models.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestStoreConfiguration.class)
public class BasketResourceTest {

    @Autowired
    BasketResource unit;

    @Autowired
    StoreResource storeResource;

    @Test
    void should_get_baskets(){
        List<Basket> baskets = unit.getBaskets();
        assertEquals(2, baskets.size());
        assertEquals(1001, baskets.get(0).getUserId());
        assertEquals(10, baskets.get(0).getItems().get(1));
        assertEquals(null, baskets.get(0).getItems().get(2));
        assertEquals(1002, baskets.get(1).getUserId());
        assertEquals(5, baskets.get(1).getItems().get(1));
        assertEquals(1, baskets.get(1).getItems().get(2));
    }

    @Test
    void should_get_basket(){
        Basket basket = unit.getBasket(1002);
        assertEquals(1002, basket.getUserId());
        assertEquals(5, basket.getItems().get(1));
        assertEquals(1, basket.getItems().get(2));
        assertEquals(null, basket.getItems().get(3));
    }

    @Test
    void should_get_blank_basket_if_not_found(){
        Basket basket = unit.getBasket(1003);
        assertEquals(1003, basket.getUserId());
        assertEquals(0, basket.getItems().size());
        assertEquals(null, basket.getItems().get(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_add_new_product_to_new_basket(){
        UpdateBasketRequest request = UpdateBasketRequest.builder()
                .userId(1003)
                .productId(1)
                .quantity(10)
                .build();
        unit.addProductToBasket(request);
        Map<Integer, Integer> updatedBasketItems = unit.getBasket(1003).getItems();
        assertEquals(1, updatedBasketItems.size());
        assertEquals(10, updatedBasketItems.get(1));
        assertEquals(null, updatedBasketItems.get(2));
        assertEquals(90, storeResource.getProduct(1).getQuantity());
        assertEquals(5, storeResource.getProduct(2).getQuantity());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_add_new_product_to_existing_basket(){
        UpdateBasketRequest request = UpdateBasketRequest.builder()
                .userId(1001)
                .productId(2)
                .quantity(2)
                .build();
        unit.addProductToBasket(request);
        Map<Integer, Integer> updatedBasketItems = unit.getBasket(1001).getItems();
        assertEquals(2, updatedBasketItems.size());
        assertEquals(10, updatedBasketItems.get(1));
        assertEquals(2, updatedBasketItems.get(2));
        assertEquals(100, storeResource.getProduct(1).getQuantity());
        assertEquals(3, storeResource.getProduct(2).getQuantity());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_add_existing_product_to_existing_basket(){
        UpdateBasketRequest request = UpdateBasketRequest.builder()
                .userId(1001)
                .productId(1)
                .quantity(100)
                .build();
        unit.addProductToBasket(request);
        Map<Integer, Integer> updatedBasketItems = unit.getBasket(1001).getItems();
        assertEquals(1, updatedBasketItems.size());
        assertEquals(110, updatedBasketItems.get(1));
        assertEquals(null, updatedBasketItems.get(2));
        assertEquals(0, storeResource.getProduct(1).getQuantity());
        assertEquals(5, storeResource.getProduct(2).getQuantity());
    }

    @Test
    void should_not_add_product_to_basket_if_not_found(){
        UpdateBasketRequest request = UpdateBasketRequest.builder()
                .userId(1002)
                .productId(3)
                .quantity(50)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.addProductToBasket(request);

        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product Not Found", exception.getReason());
    }

    @Test
    void should_not_add_product_to_basket_if_no_stock(){
        UpdateBasketRequest request = UpdateBasketRequest.builder()
                .userId(1004)
                .productId(2)
                .quantity(6)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.addProductToBasket(request);

        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Insufficient product quantity", exception.getReason());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_update_quantity_in_basket(){
        UpdateBasketRequest request = UpdateBasketRequest.builder()
                .userId(1001)
                .productId(1)
                .quantity(50)
                .build();
        unit.updateProductInBasket(request);
        Map<Integer, Integer> updatedBasketItems = unit.getBasket(1001).getItems();
        assertEquals(1, updatedBasketItems.size());
        assertEquals(50, updatedBasketItems.get(1));
        assertEquals(null, updatedBasketItems.get(2));
        assertEquals(60, storeResource.getProduct(1).getQuantity());
        assertEquals(5, storeResource.getProduct(2).getQuantity());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_not_update_basket_if_basket_not_found(){
        UpdateBasketRequest request = UpdateBasketRequest.builder()
                .userId(1005)
                .productId(1)
                .quantity(20)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.updateProductInBasket(request);

        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Basket Not Found", exception.getReason());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_not_update_basket_if_product_not_found(){
        UpdateBasketRequest request = UpdateBasketRequest.builder()
                .userId(1001)
                .productId(4)
                .quantity(5)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.updateProductInBasket(request);

        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product Not Found", exception.getReason());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_not_update_basket_if_product_insufficient(){
        UpdateBasketRequest request = UpdateBasketRequest.builder()
                .userId(1002)
                .productId(2)
                .quantity(7)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.updateProductInBasket(request);

        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Insufficient product quantity", exception.getReason());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_remove_product_from_basket(){
        unit.removeProductInBasket(1, 1002);
        Map<Integer, Integer> updatedBasketItems = unit.getBasket(1002).getItems();
        assertEquals(1, updatedBasketItems.size());
        assertEquals(null, updatedBasketItems.get(1));
        assertEquals(1, updatedBasketItems.get(2));
        assertEquals(105, storeResource.getProduct(1).getQuantity());
        assertEquals(5, storeResource.getProduct(2).getQuantity());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_not_remove_product_if_product_not_found(){
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.removeProductInBasket(5, 1001);

        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product Not Found", exception.getReason());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_not_remove_product_if_basket_not_found(){
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.removeProductInBasket(2, 1003);

        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Basket Not Found", exception.getReason());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_not_remove_product_if_product_not_in_basket(){
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.removeProductInBasket(2, 1001);

        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product Not Found In Basket", exception.getReason());
    }

    @Test
    void should_calculate_price(){
        assertEquals(BigDecimal.valueOf(56.7), unit.calculatePriceInBasket(1001));;
        assertEquals(BigDecimal.valueOf(380028.35), unit.calculatePriceInBasket(1002));;
    }

    @Test
    void should_calculate_price_with_discounts(){
        storeResource.applyDiscountDeal(1, 0.5);
        assertEquals(BigDecimal.valueOf(53.865), unit.calculatePriceInBasket(1001));;
        assertEquals(BigDecimal.valueOf(380025.515), unit.calculatePriceInBasket(1002));;
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_checkout_basket(){
        CheckoutResponse response1 = unit.checkoutBasket(1001);
        assertEquals(1, response1.getPurchases().size());
        assertEquals(1, response1.getPurchases().get(0).getId());
        assertEquals(10, response1.getPurchases().get(0).getQuantity());
        assertEquals(0, response1.getGifts().size());
        assertEquals(BigDecimal.valueOf(56.7), BigDecimal.valueOf(response1.getAmount()));
        assertEquals(0, unit.getBasket(1001).getItems().size());

        CheckoutResponse response2 = unit.checkoutBasket(1002);
        assertEquals(2, response2.getPurchases().size());
        assertEquals(1, response2.getPurchases().get(0).getId());
        assertEquals(5, response2.getPurchases().get(0).getQuantity());
        assertEquals(2, response2.getPurchases().get(1).getId());
        assertEquals(1, response2.getPurchases().get(1).getQuantity());
        assertEquals(0, response2.getGifts().size());
        assertEquals(BigDecimal.valueOf(380028.35), BigDecimal.valueOf(response2.getAmount()));
        assertEquals(0, unit.getBasket(1002).getItems().size());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_checkout_basket_with_bundle(){
        storeResource.applyBundleDeal(2, 1);
        CheckoutResponse response = unit.checkoutBasket(1002);
        assertEquals(2, response.getPurchases().size());
        assertEquals(1, response.getPurchases().get(0).getId());
        assertEquals(5, response.getPurchases().get(0).getQuantity());
        assertEquals(2, response.getPurchases().get(1).getId());
        assertEquals(1, response.getPurchases().get(1).getQuantity());
        assertEquals(1, response.getGifts().size());
        assertEquals(1, response.getGifts().get(0).getId());
        assertEquals(1, response.getGifts().get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(380028.35), BigDecimal.valueOf(response.getAmount()));
        assertEquals(0, unit.getBasket(1002).getItems().size());
        assertEquals(99, storeResource.getProduct(1).getQuantity());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_not_checkout_basket_with_bundle_if_out_of_stock(){
        storeResource.createProduct(
                Product.builder()
                        .id(3)
                        .name("Headset")
                        .price(500.00)
                        .description("Bluetooth Headset")
                        .quantity(0)
                        .build()
        );
        storeResource.applyBundleDeal(2, 3);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.checkoutBasket(1002);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Bundle Gift Out of Stock", exception.getReason());

    }


}
