package com.shop.cart.model;

public class Product {
    private Long id; // 商品ID
    private String name; // 商品名称
    private String description; // 商品描述
    private Double price; // 商品价格
    private Integer stock; // 库存数量

    private Long shopId; // 商家ID

    // 构造函数、getter和setter方法
    public Product() {
    }

    public Product(Long id, String name, String description, Double price, Integer stock, Long shopId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.shopId = shopId;
    }

    // getter和setter方法

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }
}

