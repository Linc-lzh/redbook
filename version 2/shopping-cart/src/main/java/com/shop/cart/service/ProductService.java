package com.shop.cart.service;

import com.shop.cart.model.Product;

public interface ProductService {

    Product findById(Long productId);
}
