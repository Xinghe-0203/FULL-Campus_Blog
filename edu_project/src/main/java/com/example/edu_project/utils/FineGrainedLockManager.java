package com.example.edu_project.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 细粒度锁管理器
 * 用于解决同一用户对同一资源的并发操作问题（如点赞、收藏、关注等）
 * 使用带过期时间的LRU缓存，避免内存泄漏
 */
public class FineGrainedLockManager {

    private static final int MAX_LOCKS_SIZE = 10000;
    private static final long LOCK_EXPIRE_MS = 300000; // 5分钟

    private final ConcurrentMap<String, LockEntry> locks = new ConcurrentHashMap<>();

    private static class LockEntry {
        final Object lock;
        volatile long lastAccessTime;

        LockEntry(Object lock) {
            this.lock = lock;
            this.lastAccessTime = System.currentTimeMillis();
        }

        void touch() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - lastAccessTime > LOCK_EXPIRE_MS;
        }
    }

    /**
     * 获取锁，如果锁已过期则移除并返回新锁
     * 当锁数量超过MAX_LOCKS_SIZE时，触发主动清理移除过期锁
     * 每次访问都会刷新lastAccessTime，避免正在活跃使用的锁被清理
     * @param lockKey 锁的key
     * @return 锁对象
     */
    public Object getLock(String lockKey) {
        // 主动清理过期锁，防止内存泄漏
        if (locks.size() >= MAX_LOCKS_SIZE) {
            locks.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }

        // 使用compute原子操作确保线程安全：
        // 1. 如果锁不存在或已过期，创建新锁
        // 2. 如果锁有效，更新访问时间并返回
        // 整个检查-创建/更新过程在单一原子操作中完成，不存在竞态窗口
        LockEntry entry = locks.compute(lockKey, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new LockEntry(new Object());
            }
            existing.touch();
            return existing;
        });
        return entry.lock;
    }

    private static final FineGrainedLockManager INSTANCE = new FineGrainedLockManager();

    public static FineGrainedLockManager getInstance() {
        return INSTANCE;
    }
}
