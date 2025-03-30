package com.coupon.couponserver.generator;

import com.coupon.couponserver.snowflake.SnowflakeIdGenerator;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

public class CouponCodeGenerator {
    private final int tokensPerSecond; // 每秒生成的令牌数
    private final Queue<String> tokenBucket; // 令牌桶
    private volatile boolean isRunning; // 标志位，表示生成器是否正在运行

    SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    public CouponCodeGenerator(int tokensPerSecond) {
        this.tokensPerSecond = tokensPerSecond;
        this.tokenBucket = new LinkedList<>();
        this.isRunning = true;
        startTokenGenerator();
    }

    // 启动令牌生成器
    private void startTokenGenerator() {
        Thread thread = new Thread(() -> {
            while (isRunning) {
                synchronized (tokenBucket) {
                    if (tokenBucket.size() < tokensPerSecond) {
                        tokenBucket.offer(generateCouponCode());
                        tokenBucket.notify(); // 通知等待的线程
                    }
                }
                try {
                    Thread.sleep(1000 / tokensPerSecond); // 每秒生成指定数量的令牌
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    // 生成优惠券码
    private String generateCouponCode() {
        // 实现优惠券码的生成逻辑
        return String.valueOf(idGenerator.generateId());
    }

    // 获取优惠券码
    public String getCouponCode() {
        synchronized (tokenBucket) {
            while (tokenBucket.isEmpty()) {
                try {
                    tokenBucket.wait(); // 等待令牌生成
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return tokenBucket.poll();
        }
    }

    // 停止生成优惠券码
    public void stop() {
        isRunning = false;
    }

    public static void main(String[] args) {
        CouponCodeGenerator generator = new CouponCodeGenerator(10); // 每秒生成10个令牌
        int i = 0;
        while (true) {
            String couponCode = generator.getCouponCode();
            System.out.println("Coupon Code " + i + ": " + couponCode);
            i++;

            // 在适当的时候停止生成优惠券码
            if (i == 100) {
                generator.stop();
                break;
            }
        }
    }
}
