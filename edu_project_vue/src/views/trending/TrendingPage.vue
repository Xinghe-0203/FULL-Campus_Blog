<template>
  <div class="trending-page">
    <div class="trending-container">
      <div class="page-header">
        <div class="header-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2">
            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
          </svg>
        </div>
        <div>
          <h1>热搜榜单</h1>
          <p class="header-subtitle">发现校园中最热门的内容</p>
        </div>
      </div>

      <div class="tabs">
        <button class="tab-btn" :class="{ active: activeTab === 'content' }" @click="switchTab('content')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
          </svg>
          热门内容
        </button>
        <button class="tab-btn" :class="{ active: activeTab === 'tags' }" @click="switchTab('tags')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <line x1="7" y1="7" x2="7.01" y2="7"/>
          </svg>
          热门标签
        </button>
        <button class="tab-btn" :class="{ active: activeTab === 'topics' }" @click="switchTab('topics')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
            <line x1="4" y1="22" x2="4" y2="15"/>
          </svg>
          热门话题
        </button>
      </div>

      <div class="trending-content">
        <div v-if="(activeTab === 'content' && contentLoading) || (activeTab === 'tags' && tagsLoading) || (activeTab === 'topics' && topicsLoading)" class="loading-section">
          <div v-for="i in 8" :key="i" class="trending-skeleton">
            <div class="skeleton-rank"></div>
            <div class="skeleton-info">
              <div class="skeleton-title"></div>
              <div class="skeleton-meta"></div>
            </div>
          </div>
        </div>

        <div v-else-if="error" class="error-state">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <h3>{{ error }}</h3>
          <button class="btn btn-primary" @click="retryLoad">重新加载</button>
        </div>

        <div v-else-if="activeTab === 'content' && !contentLoading && hotContent.length === 0" class="empty-state">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
          </svg>
          <p>暂无热门内容</p>
        </div>

        <div v-else-if="activeTab === 'tags' && !tagsLoading && hotTags.length === 0" class="empty-state">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <line x1="7" y1="7" x2="7.01" y2="7"/>
          </svg>
          <p>暂无热门标签</p>
        </div>

        <div v-else-if="activeTab === 'topics' && !topicsLoading && hotTopics.length === 0" class="empty-state">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
            <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
            <line x1="4" y1="22" x2="4" y2="15"/>
          </svg>
          <p>暂无热门话题</p>
        </div>

        <div v-if="activeTab === 'content' && hotContent.length > 0" class="rank-list">
          <transition-group name="rank">
            <div
              v-for="(item, index) in hotContent"
              :key="item.id"
              class="rank-item content-item"
              @click="handleContentClick(item)"
            >
              <div class="rank-number" :class="{ top: index < 3 }">
                <template v-if="index === 0">🔥</template>
                <template v-else-if="index === 1">🔥</template>
                <template v-else-if="index === 2">🔥</template>
                <span class="num">{{ index + 1 }}</span>
              </div>
              <div class="rank-content">
                <div class="rank-title">
                  <span class="type-badge" :class="item.type === 0 ? 'type-article' : 'type-post'">
                    {{ item.type === 0 ? '文章' : '动态' }}
                  </span>
                  <span v-if="item.type === 0" class="title-text">{{ item.title }}</span>
                  <span v-else class="content-text">{{ truncateContent(item.content) }}</span>
                  <span class="rank-badge" v-if="index === 0" style="background:linear-gradient(135deg,#f59e0b,#ef4444)">热</span>
                  <span class="rank-badge" v-else-if="index === 1" style="background:linear-gradient(135deg,#f97316,#f59e0b)">热</span>
                  <span class="rank-badge" v-else-if="index === 2" style="background:linear-gradient(135deg,#f97316,#f59e0b)">热</span>
                  <span class="rank-badge new" v-else-if="index < 5">新</span>
                </div>
                <div class="rank-user">
                  <img
                    :src="item.avatar || '/default-avatar.png'"
                    class="user-avatar"
                    @error="e => e.target.src = '/default-avatar.png'"
                  />
                  <span class="user-nickname">{{ item.nickname || item.username }}</span>
                </div>
                <div v-if="item.type === 1 && item.topics && item.topics.length > 0" class="rank-topics">
                  <span v-for="topic in item.topics" :key="topic" class="topic-tag">#{{ topic }}</span>
                </div>
                <div v-if="item.type === 1 && item.images && item.images.length > 0" class="rank-images">
                  <div class="image-row">
                    <img
                      v-for="(img, i) in item.images.slice(0, 4)"
                      :key="i"
                      :src="img"
                      class="thumb-img"
                      @error="e => e.target.style.display = 'none'"
                    />
                    <span v-if="item.images.length > 4" class="more-images">+{{ item.images.length - 4 }}</span>
                  </div>
                </div>
                <div class="rank-meta">
                  <span class="rank-score">🔥 {{ formatScore(item.score) }}</span>
                  <span v-if="item.type === 0" class="rank-views">{{ item.viewCount || 0 }} 阅读</span>
                  <span class="rank-likes">{{ item.likeCount || 0 }} 赞</span>
                  <span class="rank-comments">{{ item.commentCount || 0 }} 评论</span>
                  <span v-if="item.type === 1" class="rank-shares">{{ item.shareCount || 0 }} 分享</span>
                </div>
              </div>
            </div>
          </transition-group>
          <div v-if="hasMoreContent" class="load-more">
            <button class="btn btn-outline" @click="loadMoreContent" :disabled="loadingMore">
              {{ loadingMore ? '加载中...' : '加载更多' }}
            </button>
          </div>
        </div>

        <div v-if="activeTab === 'tags' && hotTags.length > 0" class="tag-cloud-section">
          <div class="tag-grid">
            <div
              v-for="tag in hotTags"
              :key="tag.id"
              class="tag-card"
              :style="{
                fontSize: getTagFontSize(tag.postCount),
                background: getTagColor(tag.postCount)
              }"
              @click="router.push(`/search?keyword=${encodeURIComponent(tag.name)}`)"
            >
              <span class="tag-hash">#</span>
              <span class="tag-name">{{ tag.name }}</span>
              <span class="tag-count">{{ tag.postCount }}</span>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'topics' && hotTopics.length > 0" class="topic-list">
          <div
            v-for="topic in hotTopics"
            :key="topic.id"
            class="topic-card"
            @click="router.push(`/search?keyword=${encodeURIComponent(topic.name)}`)"
          >
            <div class="topic-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
                <line x1="4" y1="22" x2="4" y2="15"/>
              </svg>
            </div>
            <div class="topic-info">
              <h3 class="topic-name">#{{ topic.name }}</h3>
              <p class="topic-desc" v-if="topic.description">{{ topic.description }}</p>
              <span class="topic-count">{{ topic.postCount || 0 }} 篇讨论</span>
            </div>
            <svg class="topic-arrow" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { trendingApi } from '../../api/trending'
import { topicApi } from '../../api/topic'
import { useLogger } from '../../utils/logger'

const router = useRouter()
const logger = useLogger('Trending')

const activeTab = ref('content')
const contentLoading = ref(true)
const loadingMore = ref(false)
const tagsLoading = ref(false)
const topicsLoading = ref(false)
const error = ref('')

const hotContent = ref([])
const hotTags = ref([])
const hotTopics = ref([])
const currentContentPage = ref(1)
const hasMoreContent = ref(true)
const PAGE_SIZE = 20

const tagsColors = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
  'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)',
  'linear-gradient(135deg, #f5576c 0%, #ff6f00 100%)',
  'linear-gradient(135deg, #667eea 0%, #4facfe 100%)'
]

const formatScore = (score) => {
  if (!score) return '0'
  if (score >= 10000) return (score / 10000).toFixed(1) + 'w'
  if (score >= 1000) return (score / 1000).toFixed(1) + 'k'
  return Math.round(score).toString()
}

const truncateContent = (text, maxLength = 80) => {
  if (!text) return ''
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

const getTagFontSize = (count) => {
  if (!count) return '14px'
  const sizes = ['14px', '16px', '18px', '20px', '22px', '24px']
  const idx = Math.min(Math.floor(count / 5), sizes.length - 1)
  return sizes[idx]
}

const getTagColor = (count) => {
  if (!count) return tagsColors[0]
  return tagsColors[Math.min(Math.floor(count / 3), tagsColors.length - 1)]
}

const handleContentClick = (item) => {
  if (item.type === 0) {
    router.push(`/post/${item.id}`)
  } else if (item.type === 1) {
    router.push(`/circle/${item.id}`)
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'content' && hotContent.value.length === 0 && !contentLoading.value) {
    fetchHotContent(true)
  } else if (tab === 'tags' && hotTags.value.length === 0 && !tagsLoading.value) {
    fetchHotTags()
  } else if (tab === 'topics' && hotTopics.value.length === 0 && !topicsLoading.value) {
    fetchHotTopics()
  }
}

const fetchHotContent = async (reset = false) => {
  if (reset) {
    currentContentPage.value = 1
    hasMoreContent.value = true
  }
  try {
    const res = await trendingApi.getHotContent({ pageNum: currentContentPage.value, pageSize: PAGE_SIZE })
    const data = res.data
    const records = data?.records || []
    hotContent.value = reset ? records : [...hotContent.value, ...records]
    hasMoreContent.value = (data?.pages || 0) > currentContentPage.value
  } catch (err) {
    logger.error('fetchHotContent error', { error: err.message })
    throw err
  }
}

const fetchHotTags = async () => {
  tagsLoading.value = true
  try {
    const res = await trendingApi.getHotTags()
    const data = res.data
    hotTags.value = data?.records || data || []
  } catch (err) {
    logger.error('fetchHotTags error', { error: err.message })
  } finally {
    tagsLoading.value = false
  }
}

const fetchHotTopics = async () => {
  topicsLoading.value = true
  try {
    const res = await topicApi.getHotTopics()
    hotTopics.value = res.data || []
  } catch (err) {
    logger.error('fetchHotTopics error', { error: err.message })
  } finally {
    topicsLoading.value = false
  }
}

const loadMoreContent = async () => {
  if (loadingMore.value || !hasMoreContent.value) return
  loadingMore.value = true
  currentContentPage.value++
  try {
    await fetchHotContent()
  } catch {
    currentContentPage.value--
  } finally {
    loadingMore.value = false
  }
}

const retryLoad = () => {
  error.value = ''
  contentLoading.value = true
  initLoad()
}

const initLoad = async () => {
  contentLoading.value = true
  error.value = ''
  try {
    await fetchHotContent(true)
  } catch (err) {
    error.value = '加载失败'
  }
  contentLoading.value = false
}

onMounted(() => {
  initLoad()
})
</script>

<style scoped>
.trending-page {
  max-width: 740px;
  margin: 0 auto;
  padding: 20px;
}

.trending-container {
  animation: fadeUp 0.4s ease both;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.header-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  line-height: 1.3;
}

.header-subtitle {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin: 2px 0 0;
}

.tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 3px;
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 500;
  background: transparent;
  border: none;
  border-radius: 10px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn.active {
  background: var(--surface);
  color: var(--text-primary);
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.tab-btn:hover:not(.active) {
  color: var(--text-primary);
}

.loading-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.trending-skeleton {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: var(--surface);
  border-radius: 12px;
}

.skeleton-rank {
  width: 32px;
  height: 32px;
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 8px;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
}

.skeleton-title {
  height: 16px;
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 8px;
  width: 70%;
}

.skeleton-meta {
  height: 12px;
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  width: 40%;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.error-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.error-state h3 {
  margin: 12px 0 16px;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-muted);
}

.empty-state p {
  margin-top: 12px;
  font-size: 14px;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.rank-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.rank-item:hover {
  background: var(--background);
}

.rank-number {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-weight: 700;
  font-size: 13px;
  border-radius: 8px;
  background: var(--bg-secondary);
  color: var(--text-muted);
  position: relative;
}

.rank-number.top {
  background: linear-gradient(135deg, #fef3c7, #fed7aa);
  font-size: 0;
}

.rank-number.top .num {
  font-size: 13px;
  color: #92400e;
}

.rank-content {
  flex: 1;
  min-width: 0;
}

.rank-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 6px;
  word-break: break-word;
  flex-wrap: wrap;
}

.rank-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  color: #fff;
  flex-shrink: 0;
  line-height: 1.4;
}

.rank-badge.new {
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
}

.rank-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-muted);
  flex-wrap: wrap;
}

.rank-score {
  color: var(--warning);
  font-weight: 600;
}

.rank-views,
.rank-likes,
.rank-comments,
.rank-shares {
  color: var(--text-muted);
}

.load-more {
  text-align: center;
  padding: 20px 0 10px;
}

.btn-outline {
  padding: 8px 24px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: 20px;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-outline:hover {
  background: var(--background);
  border-color: var(--text-muted);
}

.tag-cloud-section {
  padding: 10px 0;
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.tag-card {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 20px;
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  font-weight: 500;
}

.tag-card:hover {
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.tag-hash {
  opacity: 0.7;
}

.tag-count {
  opacity: 0.8;
  font-size: 0.85em;
  margin-left: 2px;
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.topic-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.topic-card:hover {
  background: var(--background);
}

.topic-icon {
  width: 44px;
  height: 44px;
  background: var(--primary-light);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
  flex-shrink: 0;
}

.topic-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.topic-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin: 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topic-count {
  font-size: 12px;
  color: var(--text-muted);
}

.topic-arrow {
  flex-shrink: 0;
}

.topic-info {
  flex: 1;
  min-width: 0;
}

/* --- New styles for content tab --- */

.content-item {
  flex-direction: row;
}

.type-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 4px;
  flex-shrink: 0;
  line-height: 1.5;
}

.type-article {
  background: #dbeafe;
  color: #1d4ed8;
}

.type-post {
  background: #d1fae5;
  color: #047857;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-secondary);
  font-weight: 400;
  font-size: 14px;
}

.rank-user {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
}

.user-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.user-nickname {
  font-size: 12px;
  color: var(--text-muted);
}

.rank-topics {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}

.topic-tag {
  font-size: 11px;
  color: var(--primary);
  background: var(--primary-light);
  padding: 1px 8px;
  border-radius: 10px;
  white-space: nowrap;
}

.rank-images {
  margin-top: 6px;
}

.image-row {
  display: flex;
  gap: 4px;
  align-items: center;
}

.thumb-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--border);
}

.more-images {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border-radius: 6px;
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
}

@media (max-width: 600px) {
  .trending-page { padding: 12px; }
  .page-header h1 { font-size: 1.25rem; }
  .header-icon { width: 44px; height: 44px; }
  .rank-item { padding: 12px; }
  .rank-title { font-size: 14px; }
  .topic-card { padding: 12px; }
}
</style>
