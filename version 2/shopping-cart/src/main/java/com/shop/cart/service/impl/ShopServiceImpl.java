package com.shop.cart.service.impl;

import com.shop.cart.init.ShopDataInitializer;
import com.shop.cart.model.Shop;
import com.shop.cart.service.ShopService;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl implements ShopService {

    @Override
    public Shop findById(Long id) {
        return ShopDataInitializer.getShopById(id);
    }
}
