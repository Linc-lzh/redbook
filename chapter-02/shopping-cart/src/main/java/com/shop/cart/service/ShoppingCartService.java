package com.shop.cart.service;

import com.shop.cart.dto.ShoppingCartDTO;
import com.shop.cart.model.ShoppingCart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 购物车service类
 */
public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param userId
     * @param cartItem
     */
    void addItemToCart(String userId, ShoppingCart cartItem);

    /**
     * 删除购物车
     * @param userId
     * @param productId
     */
    void removeItemFromCart(String userId, Long productId);

    /**
     * 修改购物车中的数量
     * @param userId
     * @param productId
     * @param newQuantity
     */
    void updateCartItemQuantity(String userId, Long productId, int newQuantity);

    /**
     * 查询购物车
     * @param userId
     * @param productIds
     * @param pageable
     * @return
     */
    Page<ShoppingCartDTO> getCartItemsWithPagination(String userId, List<Long> productIds, Pageable pageable);
}
