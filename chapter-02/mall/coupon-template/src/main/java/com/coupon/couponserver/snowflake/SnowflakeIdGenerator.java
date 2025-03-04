package com.coupon.couponserver.snowflake;

/**
 * 雪花算法
 */
public class SnowflakeIdGenerator {
    private final long twepoch = 1621987200000L; // 起始时间戳，可以根据实际需求进行调整
    private final long workerIdBits = 5L; // Worker ID 的位数
    private final long dataCenterIdBits = 5L; // 数据中心 ID 的位数
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits); // 最大 Worker ID
    private final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits); // 最大数据中心 ID
    private final long sequenceBits = 12L; // 序列号的位数

    private final long workerIdShift = sequenceBits; // Worker ID 的位移
    private final long dataCenterIdShift = sequenceBits + workerIdBits; // 数据中心 ID 的位移
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits; // 时间戳的位移
    private final long sequenceMask = -1L ^ (-1L << sequenceBits); // 序列号的掩码

    private long lastTimestamp = -1L;
    private long sequence = 0L;
    private final long workerId;
    private final long dataCenterId;

    public SnowflakeIdGenerator(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("Invalid worker ID");
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException("Invalid data center ID");
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized long generateId() {
        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - twepoch) << timestampLeftShift) |
                (dataCenterId << dataCenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public static void main(String[] args) {
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

        // 生成 ID
        long id = idGenerator.generateId();

        System.out.println("Generated ID: " + id);
    }
}

