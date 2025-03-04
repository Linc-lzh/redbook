package com.shop.cart.service.impl;

import com.shop.cart.dto.ShoppingCartDTO;
import com.shop.cart.model.Coupon;
import com.shop.cart.model.Product;
import com.shop.cart.model.Shop;
import com.shop.cart.model.ShoppingCart;
import com.shop.cart.service.CouponService;
import com.shop.cart.service.ProductService;
import com.shop.cart.service.ShopService;
import com.shop.cart.service.ShoppingCartService;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ProductService productService;
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ShopService shopService;

    @Resource
    private CouponService couponService;

    @Override
    public void addItemToCart(String userId, ShoppingCart cartItem) {
        // 使用 Redisson 锁根据用户ID进行加锁
        RLock lock = redissonClient.getLock("cartLock:" + userId);
        lock.lock();

        try {
            ShoppingCart existingItem = (ShoppingCart) redisTemplate.opsForHash().get("cart:" + userId, cartItem.getProductId().toString());

            if (existingItem != null) {
                // 更新现有项的数量和修改时间
                existingItem.setBuyCount(existingItem.getBuyCount() + cartItem.getBuyCount());
                existingItem.setModifyTime(LocalDateTime.now()); // 设置修改时间
                redisTemplate.opsForHash().put("cart:" + userId, cartItem.getProductId().toString(), existingItem);
            } else {
                // 检查购物车中的商品种类数量
                long itemCount = redisTemplate.opsForHash().size("cart:" + userId);
                if (itemCount >= 200) {
                    throw new RuntimeException("Cannot add more items to the cart. The limit is 200 different items.");
                }

                Product product = productService.findById(cartItem.getProductId());
                if (product == null) {
                    throw new RuntimeException("Product not found");
                }

                cartItem.setAddPrice(product.getPrice()); // 添加商品价格
                cartItem.setShopId(product.getShopId()); // 添加商家id
                cartItem.setBuyTime(LocalDateTime.now()); // 设置加购时间
                cartItem.setModifyTime(LocalDateTime.now()); // 设置修改时间
                cartItem.setSelected(false);//没有选中

                redisTemplate.opsForHash().put("cart:" + userId, cartItem.getProductId().toString(), cartItem);
            }
        } finally {
            // 确保在操作完成后释放锁
            lock.unlock();
        }
    }

    @Override
    public void removeItemFromCart(String userId, Long productId) {
        // 使用 Redisson 锁根据用户ID进行加锁
        RLock lock = redissonClient.getLock("cartLock:" + userId);
        lock.lock();

        try {
            // 直接从购物车中删除该商品
            redisTemplate.opsForHash().delete("cart:" + userId, productId.toString());
        } finally {
            // 确保在操作完成后释放锁
            lock.unlock();
        }
        //需要重新计算购物车的架构
    }

    @Override
    public void updateCartItemQuantity(String userId, Long productId, int newQuantity) {
        RLock lock = redissonClient.getLock("cartLock:" + userId);
        lock.lock();

        try {
            ShoppingCart cartItem = (ShoppingCart) redisTemplate.opsForHash().get("cart:" + userId, productId.toString());
            if (cartItem != null) {
                if (newQuantity <= 0) {
                    // 新数量为零或负数，从购物车中删除该商品
                    redisTemplate.opsForHash().delete("cart:" + userId, productId.toString());
                } else {
                    // 更新数量和修改时间
                    cartItem.setBuyCount(newQuantity);
                    cartItem.setModifyTime(LocalDateTime.now());
                    redisTemplate.opsForHash().put("cart:" + userId, productId.toString(), cartItem);
                }
            } else {
                throw new RuntimeException("Product not found in the cart");
            }
        } finally {
            lock.unlock();
        }
    }

    //200 获取购物车的所有参数，然后去给他展示出来，往下滑动的时候，我们异步的去获取我们具体的这么数据

    @Override
    public Page<ShoppingCartDTO> getCartItemsWithPagination(String userId, List<Long> productIds, Pageable pageable) {
        // 获取购物车中所有商品的键，并筛选出与传入的商品 ID 列表匹配的键
        Set<Object> allKeys = redisTemplate.opsForHash().keys("cart:" + userId);
        List<Object> filteredKeys = allKeys.stream()
                .filter(key -> productIds.contains(Long.valueOf((String) key)))
                .collect(Collectors.toList());

        // 计算分页参数
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredKeys.size());

        // 选取当前页面的键
        List<Object> pageKeys = filteredKeys.subList(start, end);

        // 使用 hmget 一次性获取所有相关的购物车项
        List<Object> cartItems = redisTemplate.opsForHash().multiGet("cart:" + userId, pageKeys);

        List<ShoppingCartDTO> cartDTOs = new ArrayList<>();
        for (Object item : cartItems) {
            ShoppingCart cartItem = (ShoppingCart) item;

            CompletableFuture<Product> productFuture = CompletableFuture.supplyAsync(() -> productService.findById(cartItem.getProductId()));
            CompletableFuture<Shop> shopFuture = CompletableFuture.supplyAsync(() -> shopService.findById(cartItem.getShopId()));
            CompletableFuture<List<Coupon>> couponsFuture = CompletableFuture.supplyAsync(() -> couponService.findCouponsByProductIdAndShopId(cartItem.getProductId(), cartItem.getShopId()));

            ShoppingCartDTO dto = new ShoppingCartDTO();
            try {
                dto.setShoppingCart(cartItem);
                dto.setProduct(productFuture.get()); // 获取异步执行结果
                dto.setShop(shopFuture.get()); // 获取异步执行结果
                dto.setCoupons(couponsFuture.get()); // 获取异步执行结果
            } catch (InterruptedException | ExecutionException e) {
                // 处理异常
                e.printStackTrace();
            }

            cartDTOs.add(dto);
        }

        return new PageImpl<>(cartDTOs, pageable, filteredKeys.size());
    }

}
