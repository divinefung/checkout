package com.shop.checkout;

import com.shop.models.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestStoreConfiguration.class)
public class StoreResourceTest {

    @Autowired
    StoreResource unit;

    @Test
    void should_get_products(){
        List<Product> products = unit.getProducts();
        assertEquals(2, products.size());
        assertEquals(1, products.get(0).getId());
        assertEquals("Apple", products.get(0).getName());
        assertEquals("Tasty apple", products.get(0).getDescription());
        assertEquals(5.67, products.get(0).getPrice());
        assertEquals(100, products.get(0).getQuantity());

        assertEquals(2, products.get(1).getId());
        assertEquals("Car", products.get(1).getName());
        assertEquals("Fancy car", products.get(1).getDescription());
        assertEquals(380000.00, products.get(1).getPrice());
        assertEquals(5, products.get(1).getQuantity());
    }

    @Test
    void should_get_product(){
        Product product = unit.getProduct(2);
        assertEquals(2, product.getId());
        assertEquals("Car", product.getName());
        assertEquals("Fancy car", product.getDescription());
        assertEquals(380000.00, product.getPrice());
        assertEquals(5, product.getQuantity());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_create_product(){
        Product product = Product.builder()
                .name("T-shirt")
                .price(345.67)
                .description("100% cotton T-shirt")
                .quantity(100)
                .build();
        int id = unit.createProduct(product);
        assertEquals(3, id);
        Product newProduct = unit.getProduct(3);
        assertEquals(product.getName(), newProduct.getName());
        assertEquals(product.getDescription(), newProduct.getDescription());
        assertEquals(product.getPrice(), newProduct.getPrice());
        assertEquals(product.getQuantity(), newProduct.getQuantity());
    }

    @Test
    void should_not_create_product_if_duplicate_exists(){
        Product product = Product.builder()
                .name("Car")
                .price(200000.00)
                .description("Another fancy car")
                .quantity(10)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.createProduct(product);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Product name already exists", exception.getReason());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_update_product(){
        Product product = Product.builder()
                .id(1)
                .name("Apple")
                .price(6.8)
                .description("Tasty apple v2")
                .quantity(50)
                .build();
        unit.updateProduct(product);
        Product updatedProduct = unit.getProduct(1);
        assertEquals(product.getId(), updatedProduct.getId());
        assertEquals(product.getName(), updatedProduct.getName());
        assertEquals(product.getDescription(), updatedProduct.getDescription());
        assertEquals(product.getPrice(), updatedProduct.getPrice());
        assertEquals(product.getQuantity(), updatedProduct.getQuantity());
    }

    @Test
    void should_fail_to_update_if_product_not_found(){
        Product product = Product.builder()
                .id(4)
                .name("Basketball")
                .price(320.0)
                .description("Basketball made in USA")
                .quantity(10)
                .build();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.updateProduct(product);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product Not Found", exception.getReason());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_remove_product(){
        unit.removeProduct(1);
        List<Product> products = unit.getProducts();
        assertEquals(1, products.size());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.getProduct(1);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product Not Found", exception.getReason());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void should_fail_to_remove_if_product_not_found(){
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()-> {
            unit.removeProduct(3);
        });
        List<Product> products = unit.getProducts();
        assertEquals(2, products.size());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product Not Found", exception.getReason());
    }

}
