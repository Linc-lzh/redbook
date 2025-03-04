package com.shop.cart.init;

import com.shop.cart.model.Product;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductDataInitializer implements CommandLineRunner {

    private static final Map<Long, Product> productsMap = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        List<Product> products = initializeProductData();
        products.forEach(product -> productsMap.put(product.getId(), product));

        // 打印信息，确认数据已初始化
        products.forEach(product -> System.out.println(product.getName()));
    }

    private List<Product> initializeProductData() {
        List<Product> products = new ArrayList<>();

        products.add(new Product(1L, "苹果笔记本", "高性能笔记本电脑，适合专业人士", 9999.99, 50, 1L));
        products.add(new Product(2L, "安卓智能手机", "最新款的安卓系统智能手机，具有高清摄像头和长久电池", 4999.99, 100, 2L));
        products.add(new Product(3L, "智能手表", "可追踪健康和健身活动的智能手表，防水防尘", 1999.99, 30, 1L));
        products.add(new Product(4L, "蓝牙耳机", "高音质无线蓝牙耳机，适合长时间佩戴", 699.99, 75, 2L));
        products.add(new Product(5L, "电子书阅读器", "轻便的电子墨水屏阅读器，适合长时间阅读", 1299.99, 40, 1L));
        products.add(new Product(6L, "智能健身环", "互动式健身环，可以连接到智能设备", 599.99, 60, 2L));
        products.add(new Product(7L, "无线充电器", "适用于多种设备的快速无线充电器", 299.99, 120, 1L));
        products.add(new Product(8L, "智能灯泡", "可通过智能手机控制的 LED 灯泡", 149.99, 200, 2L));
        products.add(new Product(9L, "机械键盘", "高品质机械键盘，适合游戏和打字", 899.99, 80, 1L));
        products.add(new Product(10L, "无人机", "配备高清摄像头的小型无人机", 3499.99, 30, 2L));

        return products;
    }

    public static Product getProductById(Long id) {
        return productsMap.get(id);
    }
}

