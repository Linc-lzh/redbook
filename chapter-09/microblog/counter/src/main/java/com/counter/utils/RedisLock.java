package com.counter.utils;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisLock {

    private RedisTemplate redisTemplate;

    // 获取锁的超时时间，单位毫秒
    private static final long LOCK_TIMEOUT = 30000;

    // 锁的过期时间，单位秒
    private static final int EXPIRE_TIME = 60;

    /**
     * 加锁
     * @param key 锁的键
     * @param value 锁的值
     * @return 是否成功加锁
     */
    public boolean lock(String key, String value) {
        long start = System.currentTimeMillis();
        while (true) {
            // 尝试获取锁
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, EXPIRE_TIME, TimeUnit.SECONDS);
            if (result != null && result) {
                // 获取锁成功
                return true;
            }
            // 获取锁失败，判断是否超时
            long end = System.currentTimeMillis();
            if ((end - start) > LOCK_TIMEOUT) {
                // 获取锁超时
                return false;
            }
            // 等待一段时间后重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解锁
     * @param key 锁的键
     * @param value 锁的值
     */
    public void unlock(String key, String value) {
        // 判断锁是否存在，并且锁的值是否和传入的值相同
        if (redisTemplate.opsForValue().get(key).equals(value)) {
            // 删除锁
            redisTemplate.delete(key);
        }
    }
}

