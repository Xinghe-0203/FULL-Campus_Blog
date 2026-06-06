<template>
  <div class="search-page">
    <div class="search-container">
      <div class="search-header glass">
        <div class="header-top">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="header-icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <h1>搜索</h1>
        </div>
        <div class="search-box-wrapper">
          <div class="search-box">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="search-icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索文章、校友圈、用户..."
              @keyup.enter="doSearch"
              @focus="onSearchFocus"
              @blur="hideSuggestions"
              maxlength="200"
              class="search-input"
            />
            <button class="btn btn-primary search-btn" @click="doSearch">搜索</button>
          </div>
          <transition name="dropdown">
            <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-dropdown glass">
              <div
                v-for="(suggestion, index) in suggestions"
                :key="index"
                class="suggestion-item"
                @mousedown.prevent="selectSuggestion(suggestion)"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <span>{{ suggestion }}</span>
              </div>
            </div>
          </transition>
        </div>
        <div class="search-filters">
          <button
            v-for="filter in filters"
            :key="filter.value"
            class="filter-btn"
            :class="{ active: activeFilter === filter.value }"
            @click="setFilter(filter.value)"
          >
            <svg v-if="filter.value === 'all'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
            <svg v-else-if="filter.value === 'posts'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            <svg v-else-if="filter.value === 'circles'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M8 12h8"/><path d="M12 8v8"/></svg>
            <svg v-else-if="filter.value === 'tags'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
            <svg v-else-if="filter.value === 'topics'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="1"/><circle cx="12" cy="5" r="1"/><circle cx="12" cy="19" r="1"/></svg>
            <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            {{ filter.label }}
          </button>
        </div>
      </div>

      <div v-if="!hasSearched && searchHistory.length > 0" class="search-history glass">
        <div class="history-header">
          <div class="history-header-left">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <span>搜索历史</span>
          </div>
          <button class="btn btn-xs btn-ghost" @click="clearSearchHistory">清除</button>
        </div>
        <div class="history-list">
          <div
            v-for="item in searchHistory"
            :key="item"
            class="history-chip"
            @click="searchQuery = item; doSearch()"
          >
            <span>{{ item }}</span>
            <button class="chip-remove" @click.stop="removeHistoryItem(item)">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
        </div>
      </div>

      <div v-if="hasSearched" class="search-results">
        <div v-if="error" class="error-state glass">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="error-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <p>{{ error }}</p>
          <button class="btn btn-primary btn-sm" @click="doSearch">重试</button>
        </div>
        <div v-else-if="loading && results.length === 0" class="loading-skeleton">
          <div v-for="i in 3" :key="i" class="result-item glass">
            <div class="skeleton-title-line w-70"></div>
            <div class="skeleton-title-line w-90"></div>
            <div class="skeleton-text-line w-40"></div>
          </div>
        </div>
        <template v-else>
          <div class="results-header">
            <span>共找到 <strong>{{ totalResults }}</strong> 个结果</span>
          </div>

          <div v-if="results.length > 0" class="result-list">
            <!-- 用户结果 -->
            <template v-if="activeFilter === 'users'">
              <div v-for="item in results" :key="item.userId" class="result-item glass user-result-item">
                <div class="user-result">
                  <img v-if="item.avatar" :src="item.avatar" class="user-avatar" @error="onAvatarError" />
                  <div class="user-info">
                    <router-link :to="`/user/${item.userId}`" class="user-name">
                      {{ item.nickname || item.username }}
                    </router-link>
                    <p class="user-username">@{{ item.username }}</p>
                  </div>
                </div>
              </div>
            </template>

            <!-- 文章结果 -->
            <template v-else-if="activeFilter === 'posts'">
              <div v-for="item in results" :key="item.id" class="result-item glass">
                <div class="result-body">
                  <h3 class="result-title">
                    <router-link :to="`/post/${item.id}`">{{ item.title }}</router-link>
                  </h3>
                  <p class="result-excerpt">{{ truncateText(item.summary, 150) }}</p>
                  <div class="result-meta">
                    <router-link :to="`/user/${item.userId}`" class="result-author">
                      {{ item.nickname || item.username }}
                    </router-link>
                    <span class="result-time">{{ formatRelativeTime(item.createTime) }}</span>
                  </div>
                </div>
              </div>
            </template>

            <!-- 校友圈结果 -->
            <template v-else-if="activeFilter === 'circles'">
              <router-link
                v-for="item in results"
                :key="item.id"
                :to="`/circle/${item.id}`"
                class="result-item glass circle-result-item"
              >
                <div class="circle-result-header">
                  <img v-if="item.userAvatar" :src="item.userAvatar" class="user-avatar-sm" @error="(e: Event) => { const t = e.target as HTMLImageElement; if (t) t.src = defaultAvatar; }" />
                  <div class="circle-user-info">
                    <span class="user-name">
                      {{ item.userNickname || item.userUsername }}
                    </span>
                    <span class="result-time">{{ item.timeAgo || formatRelativeTime(item.createTime) }}</span>
                  </div>
                </div>
                <div class="circle-content">
                  <p>{{ truncateText(item.content, 200) }}</p>
                </div>
                <div v-if="item.topicNames && item.topicNames.length > 0" class="circle-topics">
                  <router-link v-for="(topic, idx) in item.topicNames" :key="topic" :to="`/topic/${item.topicIds?.[idx] || ''}`" class="topic-tag">#{{ topic }}</router-link>
                </div>
                <div v-if="item.images && item.images.length > 0" class="circle-images">
                  <img v-for="(img, idx) in item.images.slice(0, 4)" :key="idx" :src="img" class="circle-image-thumb" />
                </div>
                <div class="result-meta">
                  <span>❤️ {{ item.likeCount || 0 }}</span>
                  <span>💬 {{ item.commentCount || 0 }}</span>
                </div>
              </router-link>
            </template>

            <!-- 标签结果 -->
            <template v-else-if="activeFilter === 'tags'">
              <div v-for="item in results" :key="item.id" class="result-item glass tag-result-item">
                <router-link :to="`/tag/${item.id}`" class="tag-link">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                  <span class="tag-name">{{ item.name }}</span>
                </router-link>
              </div>
            </template>

            <!-- 话题结果 -->
            <template v-else-if="activeFilter === 'topics'">
              <div v-for="item in results" :key="item.id" class="result-item glass topic-result-item">
                <router-link :to="`/topic/${item.id}`" class="topic-link">
                  <div class="topic-icon">#</div>
                  <div class="topic-info">
                    <span class="topic-name">{{ item.name }}</span>
                    <span v-if="item.description" class="topic-desc">{{ item.description }}</span>
                    <span class="topic-stats">{{ item.postCount || 0 }} 动态</span>
                  </div>
                </router-link>
              </div>
            </template>

            <!-- 全部结果 - 聚合显示 -->
            <template v-else-if="activeFilter === 'all'">
              <div v-for="item in results" :key="item.type + '-' + item.id" class="result-item glass">
                <!-- 文章类型 -->
                <template v-if="item.type === 'post'">
                  <div class="result-body">
                    <div class="result-type-badge">文章</div>
                    <h3 class="result-title">
                      <router-link :to="`/post/${item.id}`">{{ item.title }}</router-link>
                    </h3>
                    <p class="result-excerpt">{{ truncateText(item.summary, 150) }}</p>
                    <div class="result-meta">
                      <router-link :to="`/user/${item.userId}`" class="result-author">
                        {{ item.nickname || item.username }}
                      </router-link>
                      <span class="result-time">{{ formatRelativeTime(item.createTime) }}</span>
                    </div>
                  </div>
                </template>
                <!-- 校友圈类型 -->
                <template v-else-if="item.type === 'circle'">
                  <router-link :to="`/circle/${item.id}`" class="circle-all-link">
                    <div class="circle-result-header">
                      <img v-if="item.userAvatar" :src="item.userAvatar" class="user-avatar-sm" />
                      <div class="circle-user-info">
                        <span class="user-name">
                          {{ item.userNickname || item.userUsername }}
                        </span>
                        <span class="result-time">{{ item.timeAgo || formatRelativeTime(item.createTime) }}</span>
                      </div>
                      <span class="result-type-badge">校友圈</span>
                    </div>
                    <p class="result-excerpt">{{ truncateText(item.content, 150) }}</p>
                  </router-link>
                </template>
                <!-- 标签类型 -->
                <template v-else-if="item.type === 'tag'">
                  <div class="tag-result">
                    <span class="result-type-badge">标签</span>
                    <router-link :to="`/tag/${item.id}`" class="tag-link">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                      <span class="tag-name">{{ item.name }}</span>
                    </router-link>
                  </div>
                </template>
                <!-- 话题类型 -->
                <template v-else-if="item.type === 'topic'">
                  <div class="topic-result">
                    <span class="result-type-badge">话题</span>
                    <router-link :to="`/topic/${item.id}`" class="topic-link">
                      <span class="topic-name">#{{ item.name }}</span>
                    </router-link>
                    <span v-if="item.postCount" class="topic-post-count">{{ item.postCount }} 动态</span>
                  </div>
                </template>
              </div>
            </template>
          </div>

          <div v-if="hasMore" class="load-more">
            <button class="btn btn-secondary" @click="loadMore" :disabled="loading">
              <svg v-if="loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="spin"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
              {{ loading ? '加载中...' : '加载更多' }}
            </button>
          </div>

          <div v-else-if="results.length > 0" class="no-more-state glass">
            <p>没有更多结果了</p>
          </div>

          <div v-else class="empty-state glass">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="8" y1="11" x2="14" y2="11"/></svg>
            <p class="empty-title">未找到相关内容</p>
            <p class="empty-text">尝试更换关键词或使用不同的筛选条件</p>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { postApi } from '../../api/post'
import { userApi } from '../../api/user'
import { circleApi } from '../../api/circle'
import { tagApi } from '../../api/tag'
import { topicApi } from '../../api/topic'
import { formatRelativeTime, truncateText, debounce } from '../../utils'
import { useLogger } from '../../utils/logger'

const route = useRoute()
const logger = useLogger('Search')

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 48 48"><rect width="48" height="48" rx="24" fill="#e0e0e0"/><text x="24" y="30" text-anchor="middle" fill="#999" font-size="20" font-family="sans-serif">?</text></svg>')

const onAvatarError = (e: Event) => {
  const target = e.target as HTMLImageElement
  if (target.src !== defaultAvatar) {
    target.src = defaultAvatar
  }
}

const searchQuery = ref('')
const activeFilter = ref('all')
const results = ref<any[]>([])
const totalResults = ref(0)
const hasSearched = ref(false)
const loading = ref(false)
const error = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const hasMore = ref(false)
const pageSize = 20
const searchHistory = ref<string[]>([])
const maxHistoryItems = 10
const suggestions = ref<string[]>([])
const showSuggestions = ref(false)

const onSearchFocus = () => {
  if (suggestions.value.length > 0) {
    showSuggestions.value = true
  }
}

const fetchSuggestions = debounce(async (...args: unknown[]) => {
  const keyword = args[0] as string
  if (!keyword.trim() || activeFilter.value === 'users' || activeFilter.value === 'circles' || activeFilter.value === 'tags' || activeFilter.value === 'topics') {
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  try {
    const res = await postApi.searchSuggest(keyword)
    suggestions.value = res.data || []
    showSuggestions.value = suggestions.value.length > 0
  } catch {
    suggestions.value = []
    showSuggestions.value = false
  }
}, 300)

const selectSuggestion = (suggestion: string) => {
  searchQuery.value = suggestion
  showSuggestions.value = false
  doSearch()
}

const hideSuggestions = () => {
  setTimeout(() => { showSuggestions.value = false }, 150)
}

const filters = [
  { label: '全部', value: 'all' },
  { label: '文章', value: 'posts' },
  { label: '校友圈', value: 'circles' },
  { label: '标签', value: 'tags' },
  { label: '话题', value: 'topics' },
  { label: '用户', value: 'users' }
]

const doSearch = async () => {
  if (!searchQuery.value.trim()) return

  currentPage.value = 1
  error.value = ''
  loading.value = true
  hasSearched.value = true
  showSuggestions.value = false

  try {
    if (activeFilter.value === 'users') {
      const response = await userApi.searchUsers({
        keyword: searchQuery.value,
        pageNum: currentPage.value,
        pageSize: pageSize
      })
      const pageData = (response.data || {}) as any
      results.value = (pageData.records || []).map((u: any) => ({ ...u, type: 'user' }))
      totalResults.value = pageData.total || 0
      totalPages.value = pageData.pages || 1
      hasMore.value = currentPage.value < totalPages.value
    } else if (activeFilter.value === 'circles') {
      const response = await circleApi.searchPosts({
        keyword: searchQuery.value,
        pageNum: currentPage.value,
        pageSize: pageSize
      })
      const data = (response.data || []) as unknown as any[]
      results.value = data.map((c: any) => ({ ...c, type: 'circle' }))
      totalResults.value = data.length
      hasMore.value = false
    } else if (activeFilter.value === 'tags') {
      const response = await tagApi.searchTags(searchQuery.value)
      const data = (response.data || []) as any[]
      results.value = data.map((t: any) => ({ ...t, type: 'tag' }))
      totalResults.value = data.length
      hasMore.value = false
    } else if (activeFilter.value === 'topics') {
      const response = await topicApi.searchTopics(searchQuery.value)
      const data = (response.data || []) as any[]
      results.value = data.map((tp: any) => ({ ...tp, type: 'topic' }))
      totalResults.value = data.length
      hasMore.value = false
    } else if (activeFilter.value === 'all') {
      // 聚合搜索：并行调用多个接口
      const [postsRes, circlesRes, tagsRes, topicsRes] = await Promise.allSettled([
        postApi.advancedSearch({ keyword: searchQuery.value, pageNum: 1, pageSize: 10 }),
        circleApi.searchPosts({ keyword: searchQuery.value, pageNum: 1, pageSize: 10 }),
        tagApi.searchTags(searchQuery.value),
        topicApi.searchTopics(searchQuery.value)
      ])

      let total = 0
      results.value = []

      // 文章结果
      if (postsRes.status === 'fulfilled' && postsRes.value.data) {
        const posts = postsRes.value.data.records || []
        results.value.push(...posts.map(p => ({ ...p, type: 'post' })))
        total += postsRes.value.data.total || 0
      }

      // 校友圈结果
      if (circlesRes.status === 'fulfilled' && circlesRes.value.data) {
        const circles = (circlesRes.value.data || []) as unknown as any[]
        results.value.push(...circles.map((c: any) => ({ ...c, type: 'circle' })))
        total += circles.length
      }

      // 标签结果
      if (tagsRes.status === 'fulfilled' && tagsRes.value.data) {
        const tags = tagsRes.value.data || []
        results.value.push(...tags.map(t => ({ id: t.id, name: t.name, type: 'tag' })))
        total += tags.length
      }

      // 话题结果
      if (topicsRes.status === 'fulfilled' && topicsRes.value.data) {
        const topics = topicsRes.value.data || []
        results.value.push(...topics.map(tp => ({ ...tp, type: 'topic' })))
        total += topics.length
      }

      totalResults.value = total
      hasMore.value = false
    } else {
      // 默认文章搜索
      const response = await postApi.advancedSearch({
        keyword: searchQuery.value,
        pageNum: currentPage.value,
        pageSize: pageSize
      })
      const pageData = response.data || {}
      results.value = (pageData.records || []).map(p => ({ ...p, type: 'post' }))
      totalResults.value = pageData.total || 0
      totalPages.value = pageData.pages || 1
      hasMore.value = currentPage.value < totalPages.value
    }

    if (currentPage.value === 1 && searchQuery.value.trim()) {
      saveSearchHistory(searchQuery.value)
    }
  } catch (err: any) {
    logger.error('Search failed', { error: err.message })
    error.value = err.response?.data?.message || '搜索失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    doSearch()
  }
}

const setFilter = (filter: string) => {
  activeFilter.value = filter
  currentPage.value = 1
  if (hasSearched.value) {
    doSearch()
  }
}

const loadSearchHistory = () => {
  try {
    const history = localStorage.getItem('edu_search_history')
    searchHistory.value = history ? JSON.parse(history) : []
  } catch {
    searchHistory.value = []
  }
}

const saveSearchHistory = (query: string) => {
  if (!query.trim()) return
  const history = searchHistory.value.filter(item => item !== query)
  history.unshift(query)
  if (history.length > maxHistoryItems) {
    history.pop()
  }
  searchHistory.value = history
  try {
    localStorage.setItem('edu_search_history', JSON.stringify(history))
  } catch {
    // ignore
  }
}

const clearSearchHistory = () => {
  searchHistory.value = []
  try {
    localStorage.removeItem('edu_search_history')
  } catch {
    // ignore
  }
}

const removeHistoryItem = (item: string) => {
  searchHistory.value = searchHistory.value.filter(h => h !== item)
  try {
    localStorage.setItem('edu_search_history', JSON.stringify(searchHistory.value))
  } catch {
    // ignore
  }
}

watch(() => route.query.q, (q) => {
  if (q) {
    searchQuery.value = String(q)
    currentPage.value = 1
    results.value = []
    doSearch()
  }
})

watch(searchQuery, (val: string) => {
  fetchSuggestions(val)
})

onMounted(() => {
  if (route.query.q) {
    searchQuery.value = String(route.query.q)
    doSearch()
  }
  loadSearchHistory()
})

onUnmounted(() => {
  fetchSuggestions.cancel()
})
</script>

<style scoped>
.search-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.search-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.search-header {
  padding: var(--spacing-xl);
  border-radius: var(--radius-lg);
  transition: all var(--transition);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  box-shadow: var(--glass-shadow-wet);
  position: relative;
  overflow: hidden;
}

.search-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  pointer-events: none;
}

.header-top {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.header-icon {
  color: var(--primary);
}

.search-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.search-box-wrapper {
  position: relative;
  margin-bottom: var(--spacing-md);
}

.search-box {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  padding: 4px;
  box-shadow: var(--glass-shadow);
  transition: all var(--transition);
}

.search-box:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light), var(--glass-shadow);
}

.search-icon {
  padding: 0 8px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  padding: 10px 8px;
  border: none;
  background: transparent;
  font-size: 1rem;
  color: var(--text-primary);
  outline: none;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-btn {
  padding: 10px 20px;
  flex-shrink: 0;
}

.suggestions-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  z-index: var(--z-dropdown);
  border-radius: var(--radius);
  overflow: hidden;
  max-height: 240px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 10px var(--spacing-md);
  font-size: 0.875rem;
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.suggestion-item:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.suggestion-item svg {
  color: var(--text-muted);
  flex-shrink: 0;
}

.search-filters {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.filter-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 0.875rem;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: var(--shadow-xs);
}

.filter-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-1px);
}

.filter-btn.active {
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  border-color: transparent;
  color: white;
  box-shadow: var(--shadow-sm), var(--shadow-glow-primary);
}

.search-history {
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-lg);
  transition: all var(--transition);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.history-header-left {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.875rem;
  color: var(--text-muted);
  font-weight: 500;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.history-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  cursor: pointer;
  font-size: 0.8125rem;
  color: var(--text-secondary);
  transition: all var(--transition);
}

.history-chip:hover {
  background: var(--primary-light);
  color: var(--primary);
  border-color: var(--primary);
}

.chip-remove {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 2px;
  border-radius: var(--radius-xs);
  transition: all var(--transition);
}

.chip-remove:hover {
  color: var(--error);
  background: var(--error-light);
}

.results-header {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin-bottom: var(--spacing-md);
}

.results-header strong {
  color: var(--primary);
  font-weight: 600;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.result-item {
  padding: var(--spacing-lg);
  border-radius: var(--radius-lg);
  transition: all var(--transition);
}

.result-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.result-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: var(--spacing-xs);
}

.result-title a {
  color: var(--text-primary);
}

.result-title a:hover {
  color: var(--primary);
}

.result-excerpt {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
  line-height: 1.6;
}

.result-meta {
  display: flex;
  gap: var(--spacing-md);
  font-size: 0.75rem;
  color: var(--text-muted);
}

.result-author {
  color: var(--primary);
}

.result-type-badge {
  display: inline-block;
  font-size: 0.7rem;
  padding: 2px 8px;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: var(--radius-full);
  margin-bottom: var(--spacing-xs);
}

.user-result-item {
  padding: var(--spacing-md) var(--spacing-lg);
}

.user-result {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--surface-solid);
  box-shadow: var(--shadow-sm);
}

.user-avatar-sm {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.user-name:hover {
  color: var(--primary);
}

.user-username {
  font-size: 0.875rem;
  color: var(--text-muted);
}

/* 校友圈样式 */
.circle-result-item {
  padding: var(--spacing-md) var(--spacing-lg);
}

.circle-result-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.circle-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.circle-content {
  font-size: 0.9rem;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  line-height: 1.6;
}

.circle-topics {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
}

.topic-tag {
  font-size: 0.8rem;
  color: var(--primary);
  background: var(--primary-light);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  text-decoration: none;
}
.topic-tag:hover {
  background: var(--primary);
  color: white;
}

.circle-images {
  display: flex;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
  flex-wrap: wrap;
}

.circle-image-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius);
}

.circle-all-link {
  display: block;
  text-decoration: none;
  color: inherit;
}

.circle-all-link:hover {
  color: inherit;
}

/* 标签样式 */
.tag-result-item {
  padding: var(--spacing-md) var(--spacing-lg);
}

.tag-link {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-primary);
  text-decoration: none;
}

.tag-link:hover {
  color: var(--primary);
}

.tag-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--primary);
}

.tag-result {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

/* 话题样式 */
.topic-result-item {
  padding: var(--spacing-md) var(--spacing-lg);
}

.topic-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  text-decoration: none;
}

.topic-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: var(--radius);
  font-size: 1.2rem;
  font-weight: 700;
}

.topic-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.topic-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.topic-link:hover .topic-name {
  color: var(--primary);
}

.topic-desc {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.topic-stats {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.topic-result {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.topic-post-count {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-xl);
  border-radius: var(--radius-lg);
}

.empty-icon {
  color: var(--text-muted);
  opacity: 0.3;
  margin-bottom: var(--spacing-md);
}

.empty-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.empty-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.error-state {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-xl);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
}

.error-icon {
  color: var(--error);
  opacity: 0.5;
}

.error-state p {
  color: var(--error);
  font-size: 0.875rem;
}

.no-more-state {
  text-align: center;
  padding: var(--spacing-lg);
  border-radius: var(--radius-lg);
  color: var(--text-muted);
  font-size: 0.875rem;
}

.loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.loading-skeleton .result-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-title-line {
  height: 16px;
  border-radius: 4px;
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-text-line {
  height: 12px;
  border-radius: 4px;
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-title-line.w-70 { width: 70%; }
.skeleton-title-line.w-90 { width: 90%; }
.skeleton-text-line.w-40 { width: 40%; }

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.load-more {
  text-align: center;
  padding: var(--spacing-lg);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all var(--transition);
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 768px) {
  .search-page { padding: var(--spacing-md); }
  .search-header { padding: var(--spacing-md); }
  .result-item { padding: var(--spacing-md); }
  .search-filters { flex-wrap: wrap; }
}
</style>