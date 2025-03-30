package com.shop.cart.init;

import com.shop.cart.model.Shop;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ShopDataInitializer implements CommandLineRunner {

    private static final Map<Long, Shop> shopsMap = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        List<Shop> shops = initializeShopData();
        shops.forEach(shop -> shopsMap.put(shop.getId(), shop));

        // 打印信息，确认数据已初始化
        shops.forEach(shop -> System.out.println(shop.getName()));
    }

    private List<Shop> initializeShopData() {
        List<Shop> shops = new ArrayList<>();

        shops.add(new Shop(1L, "优雅咖啡馆", "提供高品质的咖啡和甜点", "市中心街道123号"));
        shops.add(new Shop(2L, "快乐健身房", "现代化的健身设施和专业的教练团队", "阳光大道456号"));
        shops.add(new Shop(3L, "家乐超市", "各种生活必需品一应俱全", "绿地小区789号"));
        shops.add(new Shop(4L, "时尚服饰店", "最新潮流服装和配饰", "时尚大道101号"));
        shops.add(new Shop(5L, "智能电器城", "提供各种智能家居产品", "科技广场202号"));

        // 可以根据需要添加更多商家

        return shops;
    }

    public static Shop getShopById(Long id) {
        return shopsMap.get(id);
    }
}
