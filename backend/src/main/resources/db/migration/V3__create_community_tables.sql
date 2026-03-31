-- 帖子表
CREATE TABLE hc_post
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT         NOT NULL,
    content       TEXT        NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reject_reason TEXT,
    like_count    INT         NOT NULL DEFAULT 0,
    comment_count INT         NOT NULL DEFAULT 0,
    create_time   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,

    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 帖子图片
CREATE TABLE hc_post_image
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT       NOT NULL,
    image_url  VARCHAR(500) NOT NULL,
    sort_index INT          NOT NULL
);

-- 评论
CREATE TABLE hc_comment
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT       NOT NULL,
    user_id     INT          NOT NULL,
    content     VARCHAR(200) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 点赞（修复版）
CREATE TABLE hc_post_like
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT NOT NULL,
    user_id     INT    NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_post_user ON hc_post_like (post_id, user_id);

-- 关注（修复版）
CREATE TABLE hc_follow
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id INT NOT NULL,
    followee_id INT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_follower_followee ON hc_follow (follower_id, followee_id);

-- 标签设置
CREATE TABLE hc_tag_setting
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT         NOT NULL UNIQUE,
    display     VARCHAR(20) NOT NULL DEFAULT 'SHOW',
    tags        VARCHAR(1000),
    create_time TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE hc_user_tag
(
    tag_setting_id BIGINT NOT NULL,
    tag_name VARCHAR(255),
    PRIMARY KEY (tag_setting_id, tag_name),
    FOREIGN KEY (tag_setting_id) REFERENCES hc_tag_setting(id)
);