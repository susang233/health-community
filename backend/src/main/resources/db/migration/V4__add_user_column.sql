-- 给 hc_user 表添加粉丝数
ALTER TABLE hc_user ADD COLUMN followers_count INT NOT NULL DEFAULT 0;

-- 添加关注数
ALTER TABLE hc_user ADD COLUMN following_count INT NOT NULL DEFAULT 0;

-- 添加帖子数
ALTER TABLE hc_user ADD COLUMN post_count INT NOT NULL DEFAULT 0;
-- 初始化粉丝数
UPDATE hc_user u
SET followers_count = (
    SELECT COUNT(*)
    FROM hc_follow f
    WHERE f.followee_id = u.user_id
);

-- 初始化关注数
UPDATE hc_user u
SET following_count = (
    SELECT COUNT(*)
    FROM hc_follow f
    WHERE f.follower_id = u.user_id
);

-- 初始化帖子数（假设帖子表叫 hc_post）
UPDATE hc_user u
SET post_count = (
    SELECT COUNT(*)
    FROM hc_post p
    WHERE p.user_id = u.user_id AND p.status = 'APPROVED'  -- 只统计已发布帖子
);
