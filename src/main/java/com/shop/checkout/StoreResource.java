package com.shop.checkout;

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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@ManagedResource
@RequestMapping("store")
@ResponseBody
public class StoreResource {

    final Logger logger = LoggerFactory.getLogger(StoreResource.class);

    private List<Product> products;

    private List<DiscountDeal> discountDeals;

    private List<BundleDeal> bundleDeals;

    private AtomicLong counter;

    public StoreResource(List<Product> products,
                         List<DiscountDeal> discountDeals,
                         List<BundleDeal> bundleDeals,
                         AtomicLong counter) {
        this.products = products;
        this.discountDeals = discountDeals;
        this.bundleDeals = bundleDeals;
        this.counter = counter;
    }

    @GetMapping("products")
    public List<Product> getProducts(){
        logger.info("Getting list of products {}", products);
        return products;
    }

    @GetMapping("product")
    public Product getProduct(@RequestParam int id){
        logger.info("Getting product {}", id);
        return getProductIfExists(id);
    }

    @PostMapping(path = "product/add", consumes = "application/json")
    public Integer createProduct(@RequestBody Product product){
        logger.info("Adding product {}", product);
        Optional<Product> productMaybe = products.stream().filter(x -> x.getName().equalsIgnoreCase(product.getName())).findFirst();
        if (!productMaybe.isPresent()){
            Product newProduct = Product.builder()
                    .id(new Long(counter.incrementAndGet()).intValue())
                    .name(product.getName())
                    .price(product.getPrice())
                    .description(product.getDescription())
                    .quantity(product.getQuantity())
                    .build();
            products.add(newProduct);
            return newProduct.getId();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Product name already exists");
        }
    }

    @PutMapping(path = "product/update", consumes = "application/json")
    public void updateProduct(@RequestBody Product product){
        logger.info("Updating product {}", product);
        Product productToUpdate = getProductIfExists(product.getId());
        products.remove(productToUpdate);
        products.add(product);
    }

    @DeleteMapping(path = "product/remove")
    public void removeProduct(@RequestParam int id){
        logger.info("Removing product with id {}", id);
        Product product = getProductIfExists(id);
        products.remove(product);
    }

    @PostMapping(path = "discounts/add")
    public void applyDiscountDeal(@RequestParam int productId, @RequestParam Double discount){
        logger.info("Adding [Buy-1-Get-50%-Off-Second] discount deal for product {}", productId);
        DiscountDeal discountDeal = DiscountDeal.builder()
                .product(getProductIfExists(productId))
                .discount(discount)
                .build();
        discountDeals.add(discountDeal);
    }

    @PostMapping(path = "bundles/add")
    public void applyBundleDeal(@RequestParam int productId, @RequestParam int giftId){
        logger.info("Adding bundle deal for product {} with gift {}", productId, giftId);
        BundleDeal bundleDeal = BundleDeal.builder()
                .product(getProductIfExists(productId))
                .gift(getProductIfExists(giftId))
                .build();
        bundleDeals.add(bundleDeal);
    }

    private Product getProductIfExists(int productId){
        Optional<Product> productMaybe = products.stream().filter(x -> x.getId().equals(productId)).findFirst();
        if (!productMaybe.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Not Found");
        }
        return productMaybe.get();
    }

}
