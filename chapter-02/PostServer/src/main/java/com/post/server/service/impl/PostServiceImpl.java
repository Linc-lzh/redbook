package com.post.server.service.impl;

import com.github.guang19.leaf.core.IdGenerator;
import com.github.guang19.leaf.core.common.Result;
import com.post.server.client.FollowersClient;
import com.post.server.dto.PostDTO;
import com.post.server.mapper.*;
import com.post.server.model.*;
import com.post.server.service.PostService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Resource
    private PostIndexMapper postIndexMapper;

    @Resource
    private PostContentMapper postContentMapper;

    @Resource
    private PostStatsMapper postStatsMapper;

    @Resource
    private PostMediaMapper postMediaMapper;

    @Resource
    private IdGenerator snowflakeIdGenerator;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private FollowersClient followersClient;

    @Resource
    private UserPostsMapper userPostsMapper;


    @Override
    public int insertPostIndex(PostDTO postDTO) {
        /**
         *  这里分几步：
         *  1.使用敏感词过滤判断发送的内容是否符合
         *  2.使用ai进行判断内容是否符合
         *  3.（可能需要）使用人工进行判断是否符合
         *  4.添加index，content，state这几个表数据
         *  5.（可能需要）按照选的标签，地址进行分类处理
         *  6.进行消息推送
         */

        Result result = snowflakeIdGenerator.nextId();
        if (result == null || result.getStatus() != result.getStatus().SUCCESS) {
            throw new IllegalStateException("ID generation failed");
        }
        long id = result.getId();
        // 4. 添加 index，content，state 这几个表数据

        // 假设这里有方法来处理这些插入操作
        insertPostData(id , postDTO);
        //数据库添加成功之后需要添加redis了
        addPostToRedis(id , postDTO);

        // 5. （可能需要）按照选的标签，地址进行分类处理
//        categorizePost(postDTO);

        // 6. 进行消息推送
        triggerNotifications(postDTO);

        return 0;
    }

    /**
     * Feed流推送帖子
     * @param postDTO
     */
    private void triggerNotifications(PostDTO postDTO) {
        //判断是否是大v
        boolean isCelebrity = checkIfCelebrity(postDTO.getUid());

        //粉丝端是从redis当中进行获取，只展示200条，es
        //如果是从数据库当中去查询的话，只需要做好主从就行了
        //获取所有粉丝列表
        List<Integer> allFollowers = followersClient.getAllFollowers(postDTO.getUid());

        //发件箱：实际上就是自己个人页的timeline，上面已经写入这里就不需要写入了
//        Set<String> followers = getFollowers(feedMessage.getUserId(), isCelebrity);

        // 将Feed ID同步给粉丝 这里是更新缓存
        // 性能是比较低的
        // 如果优化：按照分片规则批量的去更新
        for (Integer followerId : allFollowers) {
            String key = "user:" + followerId + ":inbox";
            redisTemplate.opsForZSet().add(key, postDTO.getId(), System.currentTimeMillis());
        }

        //批处理写数据库
        // 分组粉丝列表
        Map<Integer, List<Integer>> followersGroupedByShard = groupFollowersByShard(allFollowers);

        // 批量处理每个分片的粉丝
        for (Map.Entry<Integer, List<Integer>> entry : followersGroupedByShard.entrySet()) {
            List<Integer> shardFollowers = entry.getValue();
            // 执行批量数据库操作
            batchUpdateDatabase(shardFollowers, postDTO);
        }

    }

    /**
     * 根据分片规则把所有数据进行分类
     * @param followers
     * @return
     */
    private Map<Integer, List<Integer>> groupFollowersByShard(List<Integer> followers) {
        Map<Integer, List<Integer>> followersGroupedByShard = new HashMap<>();
        for (Integer followerId : followers) {
            int shardId = getShardId(followerId); // 这个方法应该根据你的分片逻辑来实现
            followersGroupedByShard.computeIfAbsent(shardId, k -> new ArrayList<>()).add(followerId);
        }
        return followersGroupedByShard;
    }

    /**
     * 批处理更新数据库
     * @param shardFollowers
     * @param postDTO
     */
    private void batchUpdateDatabase(List<Integer> shardFollowers, PostDTO postDTO) {
        List<UserPost> userPosts = new ArrayList<>();
        for (Integer followerId : shardFollowers) {
            UserPost userPost = new UserPost();
            userPost.setPostId(postDTO.getId());
            userPost.setUserId(followerId.longValue());
            userPost.setCreationTime(new Date()); // 或使用postDTO中的时间
            userPosts.add(userPost);
        }

        // 使用MyBatis Mapper执行批量操作
        userPostsMapper.batchInsertOrUpdate(userPosts);
    }

    /**
     * 分片规则方法
     * @param followerId
     * @return
     */
    private int getShardId(Integer followerId) {
        // 这里使用粉丝ID的奇偶性作为分片逻辑
        // 假设我们有两张表：user_posts_0 和 user_posts_1
        // 0表示奇数ID，1表示偶数ID
        return followerId % 2;
    }

    /**
     * 判断是否是大V，可以比较粉丝数量，比较是否是大V认证等
     * @param userId
     * @return
     */
    private boolean checkIfCelebrity(Integer userId) {
        // 逻辑来判断用户是否是大V，比如基于粉丝数量
        //这里我直接返回false了
        return false;
    }

    private Set<String> getFollowers(String userId, boolean isCelebrity) {
        // 如果是大V，只拉取活跃粉丝；否则，拉取所有粉丝
        // 这里有个问题，粉丝列表，我们需要根据用户判断存储活跃粉丝还是非活跃粉丝
        // 这里获取数据呢，一般我们是从es当中进行获取，但是这里我们没用到es，所以我们直接从数据库当中进行获取
        return null;
    }

    @Override
    public PostDTO getPostById(Long postId) {
        PostDTO postDTO = new PostDTO();

        // 尝试从 Redis 获取帖子信息
        PostIndex postIndex = (PostIndex) redisTemplate.opsForHash().get("posts_index", "post_" + postId);
        if (postIndex != null) {
            // 如果在 Redis 中找到了帖子信息，填充到 postDTO
            fillPostDTOFromIndex(postDTO, postIndex);
        } else {
            // 如果 Redis 中没有，从数据库中获取
            postIndex = null;
            if (postIndex != null) {
                fillPostDTOFromIndex(postDTO, postIndex);
                // 可以考虑将信息回填到 Redis
            } else {
                // 如果数据库中也没有，返回 null 或抛出异常
                return null;
            }
        }

        // 获取其他相关信息，如内容、媒体链接等
        PostContent postContent = postContentMapper.getPostContent(postId);
        if (postContent != null) {
            postDTO.setContent(postContent.getContent());
        }

        // 获取媒体链接列表等其他操作
        // ...

        return postDTO;
    }

    /**
     * 个人也timeline进行查看
     * @param postIds 帖子ID列表
     * @return
     */
    @Override
    public List<PostDTO> getPostsByIds(List<Long> postIds) {
        // 初始化返回的 PostDTO 列表
        List<PostDTO> postDTOList = new ArrayList<>();
        if (postIds == null || postIds.isEmpty()) {
            // 如果传入的 ID 列表为空，则直接返回空列表
            return postDTOList;
        }

        // 存储在 Redis 中找不到的帖子 ID
        List<Long> missingPostIds = new ArrayList<>();

        // 尝试从 Redis 获取帖子
        fetchPostsFromRedis(postIds, postDTOList, missingPostIds);

        // 从数据库获取在 Redis 中找不到的帖子
        fetchMissingPostsFromDB(missingPostIds, postDTOList);

        missingPostIds = new ArrayList<>();
        //从缓存或者数据库中获取帖子的各种点赞count
        fetchPostStatsFromRedis(postIds, postDTOList, missingPostIds);

        missingPostIds = new ArrayList<>();
        //从缓存或者数据库中获取urls
        getPostMediaListFromCache(postIds, postDTOList, missingPostIds);
        return postDTOList;
    }

    /**
     * 从缓存中获取数据，如果不存在就查询数据库
     * @param postIds
     * @param postDTOList
     * @param missingPostIds
     */
    private void getPostMediaListFromCache(List<Long> postIds, List<PostDTO> postDTOList, List<Long> missingPostIds) {
        // 构建 Redis 键集合
        List<String> redisKeys = postIds.stream()
                .map(id -> "post_urls:" + id)
                .collect(Collectors.toList());

        // 使用 mget 进行批量查询
        List<List<String>> results = redisTemplate.opsForValue().multiGet(redisKeys);

        for (int i = 0; i < results.size(); i++) {
            List<String> postMedias = results.get(i);
            if (postMedias != null && !postMedias.isEmpty()) {
                // 更新 PostDTO 列表
                updatePostDTOWithPostMedias(postDTOList, postIds.get(i), postMedias);
            } else {
                // 记录缺失的 postId
                missingPostIds.add(postIds.get(i));
            }
        }
    }

    /**
     * 批量添加到post_urls下
     * @param postDTOList
     * @param postId
     * @param postMedias
     */
    private void updatePostDTOWithPostMedias(List<PostDTO> postDTOList, Long postId, List<String> postMedias) {
        for (PostDTO postDTO : postDTOList) {
            if (postDTO.getId().equals(postId)) {
                postDTO.setMediaUrls(postMedias);
                break;
            }
        }
    }

    public void fetchPostStatsFromRedis(List<Long> postIds, List<PostDTO> postDTOList, List<Long> missingPostIds) {
        for (Long postId : postIds) {
            PostStats postStats = (PostStats) redisTemplate.opsForValue().get("post_count:" + postId);
            if (postStats != null) {
                // 在 Redis 中找到统计信息，将其添加到相应的 PostDTO
                for (PostDTO postDTO : postDTOList) {
                    if (postDTO.getId().equals(postId)) {
                        postDTO.setLikesCount(postStats.getLikesCount());
                        postDTO.setCommentsCount(postStats.getCommentsCount());
                        postDTO.setSharesCount(postStats.getSharesCount());
                        break;
                    }
                }
            } else {
                // 在 Redis 中找不到统计信息，记录缺失的帖子 ID
                missingPostIds.add(postId);
            }
        }
    }

    public void fetchMissingPostStatsFromDB(List<Long> missingPostIds, List<PostDTO> postDTOList) {
        if (!missingPostIds.isEmpty()) {
            for (Long missingPostId : missingPostIds) {
                PostStats postStats = postStatsMapper.getPostStats(missingPostId);
                if (postStats != null) {
                    // 从数据库中找到统计信息，将其添加到相应的 PostDTO
                    for (PostDTO postDTO : postDTOList) {
                        if (postDTO.getId().equals(missingPostId)) {
                            postDTO.setLikesCount(postStats.getLikesCount());
                            postDTO.setCommentsCount(postStats.getCommentsCount());
                            postDTO.setSharesCount(postStats.getSharesCount());
                            break;
                        }
                    }
                    // 将结果回填到 Redis
                    redisTemplate.opsForValue().set("post_count:" + missingPostId, postStats);
                }
            }
        }
    }


    private void fetchPostsFromRedis(List<Long> postIds, List<PostDTO> postDTOList, List<Long> missingPostIds) {
        // 构建 Redis 中的键集合
        List<String> redisKeys = postIds.stream()
                .map(id -> "post_" + id)
                .collect(Collectors.toList());

        // 一次性从 Redis 获取所有帖子的信息
        List<Object> redisResults = redisTemplate.opsForHash().multiGet("posts_index", redisKeys);
        for (int i = 0; i < redisResults.size(); i++) {
            Object result = redisResults.get(i);
            if (result instanceof PostIndex) {
                // 如果 Redis 中找到了帖子信息，转换为 PostDTO 并添加到列表
                PostDTO postDTO = new PostDTO();
                fillPostDTOFromIndex(postDTO, (PostIndex) result);
                postDTOList.add(postDTO);
            } else {
                // 记录 Redis 中找不到的帖子 ID
                missingPostIds.add(postIds.get(i));
            }
        }
    }

    private void fetchMissingPostsFromDB(List<Long> missingPostIds, List<PostDTO> postDTOList) {
        // 如果有缺失的帖子 ID，从数据库中查询这些帖子
        if (!missingPostIds.isEmpty()) {
            List<PostIndex> postIndexes = postIndexMapper.selectPostsByIds(missingPostIds);
            for (PostIndex postIndex : postIndexes) {
                // 转换数据库查询结果为 PostDTO 并添加到列表
                PostDTO postDTO = new PostDTO();
                fillPostDTOFromIndex(postDTO, postIndex);
                postDTOList.add(postDTO);

                // 将数据库查询结果回填到 Redis
                redisTemplate.opsForHash().put("posts_index", "post_" + postIndex.getId(), postIndex);
            }
        }
    }


    private void fillPostDTOFromIndex(PostDTO postDTO, PostIndex postIndex) {
        if (postIndex != null && postDTO != null) {
            postDTO.setId(postIndex.getId()); // 假设 PostIndex 有一个 ID 字段
            postDTO.setTitle(postIndex.getTitle());
            postDTO.setUid(postIndex.getUid());
            postDTO.setChannelId(postIndex.getChannelId());
            postDTO.setFlag(postIndex.getFlag());
            postDTO.setType(postIndex.getType());
            postDTO.setProvinceId(postIndex.getProvinceId());
            postDTO.setCityId(postIndex.getCityId());
            postDTO.setCountyId(postIndex.getCountyId());
            postDTO.setLl(postIndex.getLl());
            // 可能还有其他需要复制的字段
        }
    }

    /**
     * 添加到redis中，这里最优解是监听binlog
     * @param id
     * @param postDTO
     */
    private void addPostToRedis(Long id, PostDTO postDTO) {

        // 1. 添加到 ZSet
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add("posts:"+postDTO.getUid(), String.valueOf(postDTO.getId()), score);


        // 2. 添加到 Hash
        PostIndex postIndex = new PostIndex();
        transferDataFromDTOtoIndex(id , postDTO , postIndex);
        redisTemplate.opsForHash().put("posts_index:"+postDTO.getUid(), "post_" + postDTO.getId(), postIndex);

        // 3. 添加 List (这里假设您在某处维护了帖子的计数)
        PostStats postState = new PostStats();
        postState.setPostId(id);
        postState.setCommentsCount(0);
        postState.setLikesCount(0);
        postState.setSharesCount(0);
        redisTemplate.opsForValue().set("post_count:" + postState.getPostId(), postState);

        // 4. 添加到 String
        redisTemplate.opsForValue().set("post_content:" + id, postDTO.getContent());

        // 5. 添加到 List (存储URL)
        List<String> mediaUrls = postDTO.getMediaUrls();
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            redisTemplate.opsForValue().set("post_urls:"+id, mediaUrls);
        }
    }

    /**
     * 数据插入
     * 需要插入到索引表，内容表，url表，点赞表等
     * @param postDTO
     */
    @Transactional
    void insertPostData(Long id,PostDTO postDTO) {

        // 实现数据插入逻辑
        // 这可能涉及到多个 Mapper

        // 插入到 postContent 表
        PostContent postContent = new PostContent();
        postContent.setPostId(id);
        postContent.setContent(postDTO.getContent());
        // 设置 postContent 的属性
        postContentMapper.insertPostContent(postContent);

        // 插入到 postState 表
        PostStats postState = new PostStats();
        postState.setPostId(id);
        // 设置 postState 的属性
        postStatsMapper.insertPostStats(postState);

        List<PostMedia> postMediaList = new ArrayList<>();
        addMediaToPostMediaList(id,postDTO.getMediaUrls() , postDTO.getType() , postMediaList);
        postMediaMapper.batchInsertPostMedia(postMediaList);

        // 插入到 postIndex 表
        PostIndex postIndex = new PostIndex();
        transferDataFromDTOtoIndex(id , postDTO , postIndex);
        // 设置 postIndex 的属性，例如 postIndex.setSomeProperty(postDTO.getSomeValue());
        postIndexMapper.insertPostIndex(postIndex);

        //这段代码事务粒度很大，所以我们需要如何优化呢？
    }

    private void transferDataFromDTOtoIndex(Long id , PostDTO postDTO, PostIndex postIndex) {
        postIndex.setId(id); // 假设这是一个自动生成的ID
        postIndex.setTitle(postDTO.getTitle());
        postIndex.setUid(postDTO.getUid());
        postIndex.setChannelId(postDTO.getChannelId());
        postIndex.setFlag(postDTO.getFlag());
        postIndex.setType(postDTO.getType());
        postIndex.setProvinceId(postDTO.getProvinceId());
        postIndex.setCityId(postDTO.getCityId());
        postIndex.setCountyId(postDTO.getCountyId());
        postIndex.setIsDelete(0); // 假设新创建的帖子不是删除状态
        postIndex.setLl(postDTO.getLl());
    }

    private void addMediaToPostMediaList(Long id, List<String> mediaUrls, Integer type, List<PostMedia> postMediaList) {
        mediaUrls.stream()
                .map(url -> createPostMedia(id, url, type))
                .forEach(postMediaList::add);
    }

    private PostMedia createPostMedia(Long id, String url, Integer type) {
        PostMedia postMedia = new PostMedia();
        postMedia.setPostId(id);
        postMedia.setMediaType(type);
        postMedia.setMediaUrl(url);
        return postMedia;
    }

}

