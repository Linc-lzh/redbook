package com.shop.cart.service.impl;

import com.shop.cart.init.ProductDataInitializer;
import com.shop.cart.model.Product;
import com.shop.cart.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Override
    public Product findById(Long productId) {
        return ProductDataInitializer.getProductById(productId);
    }
}
