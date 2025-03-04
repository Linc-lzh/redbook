package com.shop.cart.dto;

import com.shop.cart.model.Coupon;
import com.shop.cart.model.Product;
import com.shop.cart.model.Shop;
import com.shop.cart.model.ShoppingCart;

import java.util.List;

public class ShoppingCartDTO {
    private ShoppingCart shoppingCart;
    private Product product;
    private Shop shop;
    private List<Coupon> coupons;

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public List<Coupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<Coupon> coupons) {
        this.coupons = coupons;
    }
}

