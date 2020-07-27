# Checkout System

### Documentation

#### Installation
1. Download and unzip the package
1. Navigate to project folder
1. Run project by
   * `gradlew bootRun`
   * Note: Make sure a valid `JAVA_HOME` is configured in system environment variables.
1. The application will start. Refer to next session on available APIs.



#### API Reference
#####Store Administration API
* `GET /store/products`
   * Get a list of inventory available in the store.
   * Sample Response: `[
     {
     "id": 1,
     "name": "Laptop",
     "price": 9800.0,
     "description": "Laptop computer",
     "quantity": 10
     },
     {
     "id": 2,
     "name": "Mouse",
     "price": 128.5,
     "description": "Optical mouse",
     "quantity": 50
     },
     {
     "id": 3,
     "name": "Keyboard",
     "price": 680.0,
     "description": "Wireless keyboard",
     "quantity": 15
     }
     ]`

* `POST /store/product/add`
   * Create a new product and returns the new product ID.
   * Accepts `application/json` with following parameters:
      * `name` **string** unique product name 
      * `price` **number** product price
      * `description`**string** product description
      * `quantity` **number** product quantity
      
* `PUT /store/product/update`
   * Amend an existing product
   * Accepts `application/json` with following parameters:
      * `id` **number** required. ID of the product to amend
      * `name` **string** product name 
      * `price` **number** product price
      * `description`**string** product description
      * `quantity` **number** product quantity

* `DELETE /store/product/remove?id=[?]`
   * Remove an existing product with the given ID
   
* `POST /store/discounts/add`
   * Apply discount deals for products (e.g. Buy 1 get 50% off the second)
   * Accepts `application/x-www-form-urlencoded` with following parameters
      * productId **number** ID of the product to apply discount deal on
      * discount **number** discount in decimals when second item is bought.
   
   
* `POST /store/bundles/add`
   * Apply bundle deals for products (e.g. Buy 1 laptop, get a mouse for free)
   * Accepts `application/x-www-form-urlencoded` with following parameters
      * productId **number** ID of the product to apply bundle deal on
      * giftId **number** ID of the product to be received when bundled product is bought.

#####User API

* `GET /basket/all`
   * Get a list of baskets

* `POST /basket/add`
   * Add product to basket. Each basket is identified by user ID.
   * Accepts `application/json` with following parameters:
      * `userId` **number** required. ID of the user
      * `productId` **number** required. ID of the product to add
      * `quantity` **number** required. product quantity to add
      
* `PUT /basket/amend`
   * Amend quantity of product in a basket
   * Accepts `application/json` with following parameters:
      * `userId` **number** required. ID of the user
      * `productId` **number** required. ID of the product to amend basket on
      * `quantity` **number** required. product quantity to amend to
   
* `DELETE /basket/remove?userId=[?]&productId=[?]`
   * Remove given product from basket of the given user.
   
* `GET /basket/price?userId=[?]`
   * Calculate total price and products taking into account all discounts and bundles

* `POST /basket/checkout`
   * Check out the basket and update product inventory accordingly
   * Accepts `application/x-www-form-urlencoded` with following parameters
      * userId **number** ID of the user whose basket is to be checked out.


#### Examples
* To create an apple as a new product, and then apply a discount deal on it (assuming the product ID returned is 78) 
   * `curl -i -X POST \
        -H "Content-Type:application/json" \
        -d \
     '{
       "name": "Apple",
       "price": 8.50,
       "description": "Fresh apple",
       "quantity": 20
     }' \
      'http://localhost:8080/store/product/add'`
   * `curl -i -X POST \
        -H "Content-Type:application/x-www-form-urlencoded" \
        -d \
     'productId=78&discount=0.5' \
      'http://localhost:8080/store/discounts/add'`


#### Support
Please reach out to [Divine Fung](mailto:fdivine321-linkedin@yahoo.com) for any feedback, questions or suggestions.
