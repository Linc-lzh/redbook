package com.comment.server.service.impl;

import com.comment.server.dto.CommentDTO;
import com.comment.server.mapper.CommentIndexMapper;
import com.comment.server.mapper.CommentLikeDetailsMapper;
import com.comment.server.model.CommentIndex;
import com.comment.server.service.CommentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scripting.support.StaticScriptSource;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate; // 假设这是您的RocketMQ模板

    @Resource
    private ObjectMapper objectMapper;

    private static final String COMMENT_IDS_ZSET_PREFIX = "comment_ids:obj:";

    @Resource
    private CommentIndexMapper commentIndexMapper;

    @Resource
    private CommentLikeDetailsMapper commentLikeDetailsMapper;

    // 定义RateLimiter，这里以每秒100个请求的速率限制为例，这里限流应该是可以动态调整的
    private final RateLimiter rateLimiter = RateLimiter.create(20);

    @Override
    public Set<Long> findAllCommentIdsByObjId(Long objId, Integer parent) {
        String key = COMMENT_IDS_ZSET_PREFIX + objId + ":" + parent;
        try {
            boolean hasKey = redisTemplate.hasKey(key);
            if (hasKey) {
                log.info("Redis中存在key：{}，直接返回集合", key);
                Set<Long> idsSet = redisTemplate.opsForZSet().range(key, 0, -1);
                return idsSet != null ? idsSet : new HashSet<>();
            } else {
                // 限流操作
                rateLimiter.acquire();
                log.info("从数据库查询并缓存评论ID，对象ID：{}，父评论ID：{}", objId, parent);

                //读取数据库有两种方式，第一种就是读取完之后，直接存储到redis当中
                //第二种读取完之后，我们发送mq消息异步的缓存到redis当中
                Set<Long> commentIds = commentIndexMapper.selectCommentIdsByObjIdAndParent(objId, parent);
                if (!commentIds.isEmpty()) {
                    log.info("数据库查询结果非空，共查询到{}条评论ID", commentIds.size());
                }

                // 异步处理消息
                sendAsyncMessageToProcess(objId, parent);
                return commentIds;
            }
        } catch (Exception e) {
            log.error("处理评论ID查询和缓存过程中出错，对象ID：{}，父评论ID：{}，错误信息：{}", objId, parent, e.getMessage());
            throw e; // 根据需要决定是否重新抛出异常
        }
    }


    private void sendAsyncMessageToProcess(Long objId, Integer parent) {
        try {
            String messageContent = objId + "," + parent; // 简化消息内容
            rocketMQTemplate.syncSend("CommentIndexUpdateTopic", messageContent);
            log.info("已发送异步消息至MQ，内容：{}", messageContent);
        } catch (Exception e) {
            log.error("发送MQ消息失败，内容：{}，错误信息：{}", objId + "," + parent, e.getMessage());
            // 根据需要处理异常，例如重试或记录错误信息等
        }
    }

    @Override
    public List<Long> findAllChildCommentIdsByParentId(Long parentId) {
        return null;
    }

    @Override
    public List<CommentDTO> findCommentIndicesByCommentIds(List<Long> commentIds , Long objId, Integer parent) {
        log.info("查询评论索引，评论ID列表: {}", commentIds);

        List<String> keys = commentIds.stream()
                .map(id -> "comment:" + id)
                .collect(Collectors.toList());
        List<String> commentsFromCache = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, CommentDTO> commentsFoundInCache = new HashMap<>();
        List<Long> missingCommentIds = new ArrayList<>();

        //commentsFromCache需要去循环判断是否存在，如果有一条不存在，我们都需要查询数据库然后返回
        IntStream.range(0, commentsFromCache.size()).forEach(i -> {
            String commentJson = commentsFromCache.get(i);
            if (commentJson == null) {
                missingCommentIds.add(commentIds.get(i));
            } else {
                try {
                    CommentDTO comment = objectMapper.readValue(commentJson, CommentDTO.class);
                    commentsFoundInCache.put(commentIds.get(i), comment);
                } catch (IOException e) {
                    log.error("解析JSON到CommentDTO失败, ID: " + commentIds.get(i), e);
                }
            }
        });

        if (!missingCommentIds.isEmpty()) {
            log.info("未能在缓存中找到所有评论数据，将从数据库查询。缺失的评论ID列表: {}", missingCommentIds);

            // 仅查询缺失的评论数据
            List<CommentDTO> missingComments = commentIndexMapper.findCommentsByIds(missingCommentIds);

            // 以后可以在这里发送MQ消息异步更新缓存
            log.info("发送MQ消息以异步更新缓存。缺失的评论ID列表: {}", missingCommentIds);
            rocketMQTemplate.convertAndSend("CommentCacheUpdateTopic", missingCommentIds);

            missingComments.forEach(comment -> commentsFoundInCache.put(comment.getId(), comment));
        } else {
            log.info("所有评论数据均从缓存中成功获取。");
        }

        //查询评论对应点赞数方法
        List<Integer> likes = findCommentLikesByIds(commentIds, objId, parent);

        // 更新CommentDTO对象的like属性
        IntStream.range(0, commentIds.size()).forEach(i -> {
            CommentDTO comment = commentsFoundInCache.get(commentIds.get(i));
            if (comment != null) {
                comment.setLike(likes.get(i)); // 更新like属性
            }
        });

        // 返回CommentDTO列表
        return commentIds.stream()
                .map(commentsFoundInCache::get)
                .collect(Collectors.toList());

    }


    private void sendCacheUpdateMessage(List<Long> commentIds) {
        // 发送消息以触发缓存更新操作
        rocketMQTemplate.convertAndSend("CommentCacheUpdateTopic", commentIds);
    }

    @Override
    public void cacheCommentIdsIfNeeded(Long objId, Integer parent) {
        String key = COMMENT_IDS_ZSET_PREFIX + objId + ":" + parent;
        boolean hasKey = redisTemplate.hasKey(key);

        if (!hasKey) {
            // 从数据库查询评论ID集合
            Set<Long> commentIds = commentIndexMapper.selectCommentIdsByObjIdAndParent(objId, parent);

            // 缓存到Redis中
            if (!commentIds.isEmpty()) {
                commentIds.forEach(id -> redisTemplate.opsForZSet().add(key, id, id));
            }
        }
    }

    @Override
    public List<CommentDTO> findCommentsByIds(List<Long> ids) {
        log.info("根据ID列表查询评论: {}", ids);
        List<CommentDTO> commentsByIds = commentIndexMapper.findCommentsByIds(ids);

        // 准备批量写入Redis的数据
        Map<String, String> keyValueMap = new HashMap<>();
        commentsByIds.forEach(comment -> {
            String key = "comment:" + comment.getId();
            try {
                String value = objectMapper.writeValueAsString(comment);
                keyValueMap.put(key, value);
            } catch (JsonProcessingException e) {
                log.error("序列化评论数据失败: {}", comment.getId(), e);
            }
        });

        // 批量写入Redis，这里使用了multiSet方法，并设定过期时间
        if (!keyValueMap.isEmpty()) {
            redisTemplate.opsForValue().multiSet(keyValueMap);
            keyValueMap.keySet().forEach(key ->
                    redisTemplate.expire(key, 24, TimeUnit.HOURS)); // 示例：设置1小时过期
            log.info("评论数据已更新到Redis");
        }

        return commentsByIds;
    }

    @Override
    public List<Integer> findCommentLikesByIds(List<Long> commentIds, Long objId, Integer parent) {
        // 构造Redis哈希键，格式为 "commentLikes:对象ID:父评论ID"
        String hashKey = String.format("commentLikes:%d:%d", objId, parent);

        // 准备查询Redis哈希的字段名，这里将评论ID列表转换为字符串列表
        List<String> fields = commentIds.stream().map(String::valueOf).collect(Collectors.toList());

        // 从Redis哈希中查询点赞数
        List<String> likesStrings = redisTemplate.opsForHash().multiGet(hashKey, fields);

        // 检查是否有任何点赞数缺失
        boolean anyMissing = likesStrings.contains(null);

        // 如果发现缺失的点赞数，发送MQ消息，消息内容为"对象ID:父评论ID"
        if (anyMissing) {
            String messageContent = objId + ":" + parent;
            rocketMQTemplate.convertAndSend("CommentLikesMissingTopic", messageContent);
        }

        //对于这种数据的话，其实前端也会存储一份数据，如果说后端redis查询失败的话，直接赋值前端的数据就行了。

        // 将从Redis获取的点赞数字符串列表转换为Integer列表，对于任何缺失的点赞数，默认为0
        return likesStrings.stream()
                .map(likeStr -> likeStr != null ? Integer.parseInt(likeStr) : 0) // 如果为null，点赞数默认为0
                .collect(Collectors.toList());
    }

    @Override
    public void findByObjIdAndParent(Long objId, Integer parent) {
        String hashKey = String.format("commentLikes:%d:%d", objId, parent);

        // 从数据库查询
        List<CommentIndex> commentIndices = commentIndexMapper.findByObjIdAndParent(objId, parent);

        // 准备数据保存到Redis
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        Map<String, String> dataToSave = new HashMap<>();

        for (CommentIndex commentIndex : commentIndices) {
            // 假设每个CommentIndex实例都有getId()和getLike()方法
            dataToSave.put(commentIndex.getId().toString(), commentIndex.getLike().toString());
        }

        // 将查询结果保存到Redis的哈希中
        if (!dataToSave.isEmpty()) {
            hashOps.putAll(hashKey, dataToSave);
            // 设置哈希键的过期时间，例如24小时
            redisTemplate.expire(hashKey, 24, TimeUnit.HOURS);
        }
    }

    @Override
    public List<Boolean> checkLikes(Long userId, List<Long> commentIds) {
        // Lua脚本字符串
        /**
         * local result = {} -- 初始化一个空的结果表（数组）
         *
         * -- 遍历所有传入的键（KEYS数组中的元素）
         * for i, key in ipairs(KEYS) do
         *     -- 对每个键执行BF.EXISTS命令，检查ARGV[1]（即第一个参数，这里假设为用户ID）是否存在于布隆过滤器中
         *     if redis.call('BF.EXISTS', key, ARGV[1]) == 1 then
         *         -- 如果存在，向结果表中添加1，表示用户对该评论已点赞
         *         table.insert(result, 1)
         *     else
         *         -- 如果不存在，向结果表中添加0，表示用户对该评论未点赞
         *         table.insert(result, 0)
         *     end
         * end
         *
         * -- 返回包含每个评论点赞状态的结果表
         * return result
         */
        String luaScript = "local result = {} " +
                "for i,key in ipairs(KEYS) do " +
                "    if redis.call('BF.EXISTS', key, ARGV[1]) == 1 then " +
                "        table.insert(result, 1) " +
                "    else " +
                "        table.insert(result, 0) " +
                "    end " +
                "end " +
                "return result";

        // 设置脚本和返回类型
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new StaticScriptSource(luaScript));
        redisScript.setResultType(List.class); // 这里可能需要根据实际返回类型调整

        // 准备KEYS参数
        List<String> keys = commentIds.stream().map(id -> "commentLikes:" + id).collect(Collectors.toList());

        // 执行Lua脚本
        List<Boolean> results = (List<Boolean>) redisTemplate.execute(redisScript, keys, userId.toString());

        // 检查results是否为空
        if (results == null || results.isEmpty() || results.contains(null)) {
            // 如果为空或包含null值，调用Mapper方法从数据库获取所有未取消的点赞评论ID
            List<Long> likedCommentIds = commentLikeDetailsMapper.findCommentIdsByMemberId(userId);

            // 创建一个空的结果列表
            List<Boolean> likeStatuses = new ArrayList<>(Collections.nCopies(commentIds.size(), Boolean.FALSE));

            // 遍历原始的评论ID列表，检查每个ID是否在数据库查询结果中
            for (int i = 0; i < commentIds.size(); i++) {
                if (likedCommentIds.contains(commentIds.get(i))) {
                    // 如果数据库查询结果中包含当前评论ID，设置对应位置为TRUE
                    likeStatuses.set(i, Boolean.TRUE);
                }
            }
            //发送MQ消息
            rocketMQTemplate.convertAndSend("likesUpdateTopic", userId);

            return likeStatuses;
        }

        // 将返回的数字转换为布尔值
        return results.stream().map(num -> num.equals(1)).collect(Collectors.toList());
    }

    @Override
    public void addLikedCommentIdsToBloomFilter(Long memberId) {
        // 查询数据库获取该用户所有点赞的评论ID
        List<Long> likedCommentIds = commentLikeDetailsMapper.findCommentIdsByMemberId(memberId);

        // 假定布隆过滤器的名称为"memberLikedComments:{memberId}"
        String bloomFilterName = "commentLikes:" + memberId;

        // 将查询到的点赞评论ID添加到布隆过滤器中
        for (Long commentId : likedCommentIds) {
            // 这里使用BF.ADD命令将评论ID添加到布隆过滤器中，具体命令依赖于你的布隆过滤器实现
            // 注意: 这个例子使用了假定的方法调用，你需要根据你的Redis客户端库来实现
            redisTemplate.opsForValue().setBit(bloomFilterName, commentId, true);
        }
    }
}
