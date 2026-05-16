<template>
  <div class="follow-page">
    <button class="back-btn" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>
    <div class="page-header">
      <h1>我的粉丝</h1>
      <span class="page-count">{{ total }} 人</span>
    </div>

    <div v-if="loading && followers.length === 0" class="skeleton-list">
      <div v-for="n in 3" :key="n" class="skeleton-user-item">
        <div class="skeleton skeleton-avatar"></div>
        <div class="skeleton-user-info">
          <div class="skeleton skeleton-user-name"></div>
          <div class="skeleton skeleton-user-bio"></div>
        </div>
      </div>
    </div>

    <div v-else-if="error" class="error-card card">
      <p>{{ error }}</p>
      <button class="btn btn-sm btn-primary" @click="fetchData">重试</button>
    </div>

    <div v-else-if="followers.length === 0" class="empty-state card">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
      <p class="empty-title">还没有粉丝</p>
      <p class="empty-text">发布优质内容吸引更多关注吧</p>
      <router-link to="/post-edit" class="btn btn-primary">写文章</router-link>
    </div>

    <div v-else class="user-list">
      <div v-for="user in followers" :key="user.id" class="user-card card">
        <router-link :to="`/user/${user.id}`" class="user-card-left">
          <img :src="user.avatar || '/default-avatar.png'" :alt="user.nickname" class="user-avatar" @error="e => e.target.src = '/default-avatar.png'" />
          <div class="user-info">
            <span class="user-name">{{ user.nickname || user.username }}</span>
            <span class="user-bio">{{ user.bio || '这个人很懒，什么都没写' }}</span>
          </div>
        </router-link>
        <div v-if="userStore.isLoggedIn" class="user-card-right">
          <button class="btn btn-sm" :class="user.isFollowing ? 'btn-secondary' : 'btn-primary'" @click="toggleFollow(user)" :disabled="followLoadingId === user.id">
            <template v-if="followLoadingId === user.id">
              <span class="loading-dots"></span>
            </template>
            <template v-else>
              <svg v-if="user.isFollowing" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="8" x2="23" y2="13"/><line x1="23" y1="8" x2="18" y2="13"/></svg>
              <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
              {{ user.isFollowing ? '已关注' : '关注' }}
            </template>
          </button>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination-section">
        <div class="pagination">
          <button class="pagination-btn" :disabled="page <= 1" @click="page--; fetchData()">上一页</button>
          <button v-for="p in totalPages" :key="p" class="pagination-btn" :class="{ active: p === page }" @click="page = p; fetchData()">{{ p }}</button>
          <button class="pagination-btn" :disabled="page >= totalPages" @click="page++; fetchData()">下一页</button>
        </div>
        <span class="pagination-info">共 {{ total }} 人</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { followApi } from '../../api/follow'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('Followers')
const followers = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const total = ref(0)
const totalPages = ref(1)
const pageSize = 10
const followLoadingId = ref(null)

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const response = await followApi.getFollowers(userStore.userId, { pageNum: page.value, pageSize })
    const data = response.data || {}
    followers.value = (data.records || []).map(u => ({ ...u, isFollowing: u.isFollowing ?? false }))
    total.value = data.total || 0
    totalPages.value = data.pages || 1
  } catch (err) {
    logger.error('Failed to fetch followers', { error: err.message })
    error.value = '加载粉丝列表失败'
  } finally {
    loading.value = false
  }
}

async function toggleFollow(user) {
  followLoadingId.value = user.id
  const prevFollowing = user.isFollowing
  try {
    if (user.isFollowing) {
      await followApi.unfollow(user.id)
      user.isFollowing = false
    } else {
      await followApi.toggleFollow(user.id)
      user.isFollowing = true
    }
    toast.success(user.isFollowing ? '已关注' : '已取消关注')
  } catch (err) {
    user.isFollowing = prevFollowing
    logger.error('Failed to toggle follow', { error: err.message })
    toast.error(err.response?.data?.message || '操作失败')
  } finally {
    followLoadingId.value = null
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.follow-page {
  max-width: 680px;
  margin: 0 auto;
  padding: 24px;
}
.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }

.page-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.page-count {
  font-size: 0.875rem;
  color: var(--text-muted);
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-user-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
}

.skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.skeleton-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-user-name {
  width: 120px;
  height: 16px;
}

.skeleton-user-bio {
  width: 200px;
  height: 13px;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  gap: 12px;
  transition: all 0.2s;
}

.user-card:hover {
  box-shadow: var(--shadow);
}

.user-card-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
  text-decoration: none;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.user-bio {
  font-size: 0.8125rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-card-right {
  flex-shrink: 0;
}

.loading-dots {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid transparent;
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-card {
  padding: 40px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.error-card p {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.empty-state {
  padding: 80px 24px;
  text-align: center;
}

.empty-icon {
  color: var(--text-muted);
  opacity: 0.3;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.empty-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.pagination-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding-top: 20px;
}

.pagination-info {
  font-size: 0.75rem;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .follow-page {
    padding: 16px;
  }

  .user-card {
    padding: 14px;
  }

  .user-avatar {
    width: 40px;
    height: 40px;
  }
}
</style>
