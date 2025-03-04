-- 检查和更新有序集合
local function checkAndUpdate(zsetKey, memberId, score, maxSize)
    local size = redis.call('ZCARD', zsetKey)
    if size >= maxSize then
        redis.call('ZREMRANGEBYRANK', zsetKey, 0, 0)
    end
    redis.call('ZADD', zsetKey, score, memberId)
end

-- 更新粉丝和关注列表
local followersKey = KEYS[1]
local followingKey = KEYS[2]
local userId = ARGV[1]
local followerId = ARGV[2]
local score = ARGV[3]
local maxFollowersSize = 200
local maxFollowingSize = 200

-- 更新粉丝列表
checkAndUpdate(followersKey, followerId, score, maxFollowersSize)

-- 更新关注列表
checkAndUpdate(followingKey, userId, score, maxFollowingSize)


