CREATE ALIAS IF NOT EXISTS DATE_ADD AS '
java.sql.Timestamp dateAdd(java.sql.Timestamp d, int n, String u)
    throws Exception
{
    java.util.Calendar c = java.util.Calendar.getInstance();
    c.setTime(d);
    if ("MINUTE".equalsIgnoreCase(u)) {
        c.add(java.util.Calendar.MINUTE, n);
    } else if ("SECOND".equalsIgnoreCase(u)) {
        c.add(java.util.Calendar.SECOND, n);
    } else if ("HOUR".equalsIgnoreCase(u)) {
        c.add(java.util.Calendar.HOUR, n);
    }
    return new java.sql.Timestamp(c.getTimeInMillis());
}
';

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    cover_image VARCHAR(500),
    bio VARCHAR(500),
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user',
    status INT DEFAULT 1,
    follower_count INT DEFAULT 0,
    following_count INT DEFAULT 0,
    login_fail_count INT DEFAULT 0,
    lock_until TIMESTAMP NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS blog_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(500),
    content CLOB,
    category VARCHAR(50),
    cover_url VARCHAR(500),
    view_count BIGINT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    collect_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    status INT DEFAULT 0,
    reviewer_id BIGINT,
    review_time TIMESTAMP NULL,
    reject_reason VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS blog_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS blog_trending (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    score DOUBLE DEFAULT 0,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    date DATE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);
