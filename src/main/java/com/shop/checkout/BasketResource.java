package com.shop.checkout;

import com.shop.dto.CheckoutResponse;
import com.shop.dto.UpdateBasketRequest;
import com.shop.models.Basket;
import com.shop.models.BundleDeal;
import com.shop.models.DiscountDeal;
import com.shop.models.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@ManagedResource
@RequestMapping("basket")
@ResponseBody
public class BasketResource {

    final Logger logger = LoggerFactory.getLogger(BasketResource.class);

    private List<Basket> baskets;

    private List<Product> products;

    private List<DiscountDeal> discountDeals;

    private List<BundleDeal> bundleDeals;

    public BasketResource(List<Basket> baskets,
                          List<Product> products,
                          List<DiscountDeal> discountDeals,
                          List<BundleDeal> bundleDeals) {
        this.baskets = baskets;
        this.products = products;
        this.discountDeals = discountDeals;
        this.bundleDeals = bundleDeals;
    }

    @GetMapping("all")
    public List<Basket> getBaskets(){
        logger.info("Getting list of baskets {}", baskets);
        return baskets;
    }

    @GetMapping("get")
    public Basket getBasket(@RequestParam int userId){
        logger.info("Getting basket {}", userId);
        Optional<Basket> basketMaybe = baskets.stream().filter(basket -> basket.getUserId().equals(userId)).findFirst();
        if (basketMaybe.isPresent()){
            return basketMaybe.get();
        } else {
            return Basket.builder()
                    .userId(userId)
                    .items(new HashMap<>())
                    .build();
        }
    }

    @PostMapping("add")
    public void addProductToBasket(@RequestBody UpdateBasketRequest request){
        logger.info("Adding {} units of product {} to basket {}", request.getQuantity(), request.getProductId(), request.getUserId());
        Basket basket = getBasket(request.getUserId());
        Product product = getProductIfExists(request.getProductId());
        if (product.getQuantity() >= request.getQuantity()){
            if (basket.getItems().get(request.getProductId()) != null){
                int total = basket.getItems().get(request.getProductId()) + request.getQuantity();
                basket.getItems().put(request.getProductId(), total);
            } else {
                basket.getItems().put(request.getProductId(), request.getQuantity());
            }
            if (!baskets.contains(basket)){
                baskets.add(basket);
            }
            product.setQuantity(product.getQuantity() - request.getQuantity());
        } else {
            logger.error("Failed to add {} units because there are only {} units", request.getQuantity(), product.getQuantity());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient product quantity");
        }
    }

    @PutMapping("amend")
    public void updateProductInBasket(@RequestBody UpdateBasketRequest request){
        logger.info("Changing units of product {} in basket {} to {}", request.getProductId(), request.getUserId(), request.getQuantity());
        Basket basket = getBasketIfExists(request.getUserId());
        Product product = getProductIfExists(request.getProductId());
        int quantityInBasket = (basket.getItems().get(request.getProductId()) != null)? basket.getItems().get(request.getProductId()): 0;
        int quantityToAdd = request.getQuantity() - quantityInBasket;
        if (product.getQuantity() >= quantityToAdd){
            basket.getItems().put(request.getProductId(), request.getQuantity());
            product.setQuantity(product.getQuantity() - quantityToAdd);
        } else {
            logger.error("Failed to add {} units because there are only {} units", quantityToAdd, product.getQuantity());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient product quantity");
        }
    }


    @DeleteMapping("remove")
    public void removeProductInBasket(@RequestParam int productId, @RequestParam int userId){
        logger.info("Removing product {} from basket {} ", productId, userId);
        Basket basket = getBasketIfExists(userId);
        Product product = getProductIfExists(productId);
        Integer quantityRemoved = basket.getItems().remove(productId);
        if (quantityRemoved != null){
            product.setQuantity(product.getQuantity() + quantityRemoved);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Not Found In Basket");
        }
    }

    @GetMapping("price")
    public BigDecimal calculatePriceInBasket(@RequestParam int userId){
        Basket basket = getBasketIfExists(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (int productId : basket.getItems().keySet()){
            Product product = getProductIfExists(productId);
            double price = product.getPrice();
            total = total.add(BigDecimal.valueOf(price * basket.getItems().get(productId)));
        }
        for (DiscountDeal deal : discountDeals){
            if (deal.isEligible(basket)){
                total = total.subtract(BigDecimal.valueOf(deal.adjustment()));
            }
        }
        return total;
    }

    @PostMapping("checkout")
    public CheckoutResponse checkoutBasket(@RequestParam int userId){
        Basket basket = getBasketIfExists(userId);
        BigDecimal total = calculatePriceInBasket(userId);
        List<Product> purchases = new ArrayList<>();
        List<Product> gifts = new ArrayList<>();
        basket.getItems().keySet().stream()
                .forEach(productId -> {
                    Product product = getProductIfExists(productId);
                    Product purchase = Product.builder()
                            .id(productId)
                            .name(product.getName())
                            .price(product.getPrice())
                            .description(product.getDescription())
                            .quantity(basket.getItems().get(productId))
                            .build();
                    purchases.add(purchase);
                });
        bundleDeals.forEach(deal -> {
            if (basket.getItems().keySet().contains(deal.getProduct().getId())){
                Product gift = deal.getGift();
                if (gift.getQuantity() >= 1){
                    gifts.add(Product.builder()
                            .id(gift.getId())
                            .name(gift.getName())
                            .price(gift.getPrice())
                            .description(gift.getDescription())
                            .quantity(1)
                            .build());
                    gift.setQuantity(gift.getQuantity() - 1);
                } else {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bundle Gift Out of Stock");
                }

            }
        });
        basket.clear();
        return CheckoutResponse.builder()
                .amount(total.doubleValue())
                .purchases(purchases)
                .gifts(gifts)
                .build();
    }

    private Basket getBasketIfExists(int userId){
        Optional<Basket> basketMaybe = baskets.stream().filter(x -> x.getUserId().equals(userId)).findFirst();
        if (!basketMaybe.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Basket Not Found");
        }
        return basketMaybe.get();
    }

    private Product getProductIfExists(int productId){
        Optional<Product> productMaybe = products.stream().filter(x -> x.getId().equals(productId)).findFirst();
        if (!productMaybe.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Not Found");
        }
        return productMaybe.get();
    }
}
