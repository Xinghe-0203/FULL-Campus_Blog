<template>
  <div class="search-page">
    <!-- Hero search area -->
    <section class="search-hero" :class="{ 'has-results': hasSearched }">
      <h1 class="search-title">搜索</h1>
      <p class="search-subtitle">发现文章、校友圈、标签和更多内容</p>
      <div class="search-input-wrap">
        <input
          ref="searchInputRef"
          v-model="searchQuery"
          type="text"
          placeholder="输入关键词..."
          class="search-field"
          @keyup.enter="doSearch"
          @focus="onSearchFocus"
          @blur="hideSuggestions"
          maxlength="200"
          aria-label="搜索"
        />
        <button class="search-submit" @click="doSearch" aria-label="执行搜索">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        </button>
        <transition name="dropdown">
          <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-dropdown">
            <button
              v-for="(suggestion, index) in suggestions"
              :key="index"
              class="suggestion-item"
              @mousedown.prevent="selectSuggestion(suggestion)"
            >
              <span class="suggestion-text">{{ suggestion }}</span>
            </button>
          </div>
        </transition>
      </div>

      <!-- Filter pills -->
      <nav class="filter-row" role="tablist" aria-label="搜索类别筛选">
        <button
          v-for="filter in filters"
          :key="filter.value"
          role="tab"
          :aria-selected="activeFilter === filter.value"
          class="filter-pill"
          :class="{ active: activeFilter === filter.value }"
          @click="setFilter(filter.value)"
        >
          {{ filter.label }}
        </button>
      </nav>
    </section>

    <!-- Search history -->
    <section v-if="!hasSearched && searchHistory.length > 0" class="history-section">
      <div class="section-label">
        <span>搜索历史</span>
        <button class="clear-btn" @click="clearSearchHistory">清除</button>
      </div>
      <div class="history-chips">
        <button
          v-for="item in searchHistory"
          :key="item"
          class="history-chip"
          @click="searchQuery = item; doSearch()"
        >
          <span>{{ item }}</span>
          <span class="chip-x" @click.stop="removeHistoryItem(item)" aria-label="删除">&times;</span>
        </button>
      </div>
    </section>

    <!-- Results area -->
    <section v-if="hasSearched" class="results-section">
      <!-- Error state -->
      <div v-if="error" class="state-block state-error">
        <p class="state-text">{{ error }}</p>
        <button class="btn btn-primary btn-sm" @click="doSearch">重试</button>
      </div>

      <!-- Loading skeleton -->
      <div v-else-if="loading && results.length === 0" class="skeleton-list">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <div class="sk-line sk-w60"></div>
          <div class="sk-line sk-w90"></div>
          <div class="sk-line sk-w35"></div>
        </div>
      </div>

      <template v-else>
        <!-- Result count -->
        <p v-if="results.length > 0" class="result-count">
          找到 <strong>{{ totalResults }}</strong> 个结果
        </p>

        <!-- Users -->
        <div v-if="activeFilter === 'users' && results.length > 0" class="result-stack">
          <router-link
            v-for="item in results"
            :key="item.userId"
            :to="`/user/${item.userId}`"
            class="user-card"
          >
            <img :src="item.avatar || defaultAvatar" class="user-avatar" @error="onAvatarError" alt="" />
            <div class="user-meta">
              <span class="user-display">{{ item.nickname || item.username }}</span>
              <span class="user-handle">@{{ item.username }}</span>
            </div>
          </router-link>
        </div>

        <!-- Posts -->
        <div v-else-if="activeFilter === 'posts' && results.length > 0" class="result-stack">
          <router-link
            v-for="item in results"
            :key="item.id"
            :to="`/post/${item.id}`"
            class="post-card"
          >
            <h3 class="post-card-title">{{ item.title }}</h3>
            <p class="post-card-excerpt">{{ truncateText(item.summary, 160) }}</p>
            <div class="post-card-footer">
              <span class="post-card-author">{{ item.nickname || item.username }}</span>
              <span class="post-card-dot">&middot;</span>
              <span class="post-card-time">{{ formatRelativeTime(item.createTime) }}</span>
            </div>
          </router-link>
        </div>

        <!-- Circles -->
        <div v-else-if="activeFilter === 'circles' && results.length > 0" class="result-stack">
          <router-link
            v-for="item in results"
            :key="item.id"
            :to="`/circle/${item.id}`"
            class="circle-card"
          >
            <div class="circle-card-head">
              <img :src="item.userAvatar || defaultAvatar" class="circle-avatar" @error="(e: Event) => { const t = e.target as HTMLImageElement; if (t) t.src = defaultAvatar; }" alt="" />
              <span class="circle-author">{{ item.userNickname || item.userUsername }}</span>
              <span class="circle-time">{{ item.timeAgo || formatRelativeTime(item.createTime) }}</span>
            </div>
            <p class="circle-text">{{ truncateText(item.content, 200) }}</p>
            <div v-if="item.topicNames && item.topicNames.length > 0" class="circle-topic-row">
              <router-link v-for="(topic, idx) in item.topicNames" :key="topic" :to="`/topic/${item.topicIds?.[idx] || ''}`" class="topic-tag">#{{ topic }}</router-link>
            </div>
            <div v-if="item.images && item.images.length > 0" class="circle-img-row">
              <img v-for="(img, idx) in item.images.slice(0, 3)" :key="idx" :src="img" class="circle-thumb" alt="" />
            </div>
          </router-link>
        </div>

        <!-- Tags -->
        <div v-else-if="activeFilter === 'tags' && results.length > 0" class="tag-cloud">
          <router-link
            v-for="item in results"
            :key="item.id"
            :to="`/tag/${item.id}`"
            class="tag-chip"
          >
            {{ item.name }}
          </router-link>
        </div>

        <!-- Topics -->
        <div v-else-if="activeFilter === 'topics' && results.length > 0" class="result-stack">
          <router-link
            v-for="item in results"
            :key="item.id"
            :to="`/topic/${item.id}`"
            class="topic-card"
          >
            <span class="topic-hash">#</span>
            <div class="topic-meta">
              <span class="topic-name">{{ item.name }}</span>
              <span v-if="item.description" class="topic-desc">{{ item.description }}</span>
            </div>
            <span class="topic-count">{{ item.postCount || 0 }} 动态</span>
          </router-link>
        </div>

        <!-- All (aggregated) -->
        <div v-else-if="activeFilter === 'all' && results.length > 0" class="result-stack">
          <template v-for="item in results" :key="item.type + '-' + item.id">
            <!-- Post -->
            <router-link v-if="item.type === 'post'" :to="`/post/${item.id}`" class="post-card">
              <span class="card-type-label">文章</span>
              <h3 class="post-card-title">{{ item.title }}</h3>
              <p class="post-card-excerpt">{{ truncateText(item.summary, 140) }}</p>
              <div class="post-card-footer">
                <span class="post-card-author">{{ item.nickname || item.username }}</span>
                <span class="post-card-dot">&middot;</span>
                <span class="post-card-time">{{ formatRelativeTime(item.createTime) }}</span>
              </div>
            </router-link>
            <!-- Circle -->
            <router-link v-else-if="item.type === 'circle'" :to="`/circle/${item.id}`" class="circle-card">
              <div class="circle-card-head">
                <img :src="item.userAvatar || defaultAvatar" class="circle-avatar" alt="" />
                <span class="circle-author">{{ item.userNickname || item.userUsername }}</span>
                <span class="card-type-label">校友圈</span>
              </div>
              <p class="circle-text">{{ truncateText(item.content, 140) }}</p>
            </router-link>
            <!-- Tag -->
            <router-link v-else-if="item.type === 'tag'" :to="`/tag/${item.id}`" class="tag-inline-card">
              <span class="card-type-label">标签</span>
              <span class="tag-chip-lg">{{ item.name }}</span>
            </router-link>
            <!-- Topic -->
            <router-link v-else-if="item.type === 'topic'" :to="`/topic/${item.id}`" class="topic-card">
              <span class="card-type-label">话题</span>
              <span class="topic-hash">#</span>
              <span class="topic-name">{{ item.name }}</span>
              <span v-if="item.postCount" class="topic-count">{{ item.postCount }} 动态</span>
            </router-link>
          </template>
        </div>

        <!-- Load more -->
        <div v-if="hasMore" class="load-more-row">
          <button class="btn btn-secondary" @click="loadMore" :disabled="loading">
            {{ loading ? '加载中...' : '加载更多' }}
          </button>
        </div>

        <!-- No more -->
        <p v-else-if="results.length > 0" class="state-end">已显示全部结果</p>

        <!-- Empty -->
        <div v-else class="state-block state-empty">
          <p class="state-empty-title">没有找到相关内容</p>
          <p class="state-empty-hint">试试换个关键词，或者切换搜索类别</p>
        </div>
      </template>
    </section>
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
      const [postsRes, circlesRes, tagsRes, topicsRes] = await Promise.allSettled([
        postApi.advancedSearch({ keyword: searchQuery.value, pageNum: 1, pageSize: 10 }),
        circleApi.searchPosts({ keyword: searchQuery.value, pageNum: 1, pageSize: 10 }),
        tagApi.searchTags(searchQuery.value),
        topicApi.searchTopics(searchQuery.value)
      ])

      let total = 0
      results.value = []

      if (postsRes.status === 'fulfilled' && postsRes.value.data) {
        const posts = postsRes.value.data.records || []
        results.value.push(...posts.map(p => ({ ...p, type: 'post' })))
        total += postsRes.value.data.total || 0
      }
      if (circlesRes.status === 'fulfilled' && circlesRes.value.data) {
        const circles = (circlesRes.value.data || []) as unknown as any[]
        results.value.push(...circles.map((c: any) => ({ ...c, type: 'circle' })))
        total += circles.length
      }
      if (tagsRes.status === 'fulfilled' && tagsRes.value.data) {
        const tags = tagsRes.value.data || []
        results.value.push(...tags.map(t => ({ id: t.id, name: t.name, type: 'tag' })))
        total += tags.length
      }
      if (topicsRes.status === 'fulfilled' && topicsRes.value.data) {
        const topics = topicsRes.value.data || []
        results.value.push(...topics.map(tp => ({ ...tp, type: 'topic' })))
        total += topics.length
      }

      totalResults.value = total
      hasMore.value = false
    } else {
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
/* =====================================================
   Search Page — Typography-Forward Minimalist
   ===================================================== */

.search-page {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--spacing-xl) var(--spacing-lg);
}

/* ---------- Hero ---------- */

.search-hero {
  text-align: center;
  padding: var(--spacing-3xl) 0 var(--spacing-xl);
  transition: padding var(--transition-slow);
}

.search-hero.has-results {
  padding: var(--spacing-lg) 0 var(--spacing-md);
}

.search-title {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  font-weight: var(--font-extrabold);
  color: var(--text-primary);
  letter-spacing: -0.03em;
  margin: 0 0 var(--spacing-2);
  line-height: var(--leading-tight);
}

.search-hero.has-results .search-title {
  font-size: var(--text-2xl);
  margin-bottom: var(--spacing-1);
}

.search-subtitle {
  font-size: var(--text-base);
  color: var(--text-muted);
  margin: 0 0 var(--spacing-xl);
  transition: opacity var(--transition), margin var(--transition-slow);
}

.search-hero.has-results .search-subtitle {
  opacity: 0;
  height: 0;
  margin: 0;
  overflow: hidden;
}

/* ---------- Search field ---------- */

.search-input-wrap {
  position: relative;
  max-width: 560px;
  margin: 0 auto var(--spacing-lg);
}

.search-field {
  width: 100%;
  padding: var(--spacing-4) var(--spacing-12) var(--spacing-4) var(--spacing-5);
  font-size: var(--text-lg);
  font-family: var(--font-sans);
  font-weight: var(--font-normal);
  color: var(--text-primary);
  background: var(--surface-solid);
  border: 2px solid var(--border-solid);
  border-radius: var(--radius-xl);
  outline: none;
  transition: border-color var(--transition), box-shadow var(--transition);
  line-height: var(--leading-normal);
}

.search-field::placeholder {
  color: var(--text-muted);
  font-weight: var(--font-normal);
}

.search-field:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 4px var(--primary-light);
}

.search-submit {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary);
  border: none;
  border-radius: var(--radius-lg);
  color: white;
  cursor: pointer;
  transition: background var(--transition), transform var(--transition);
}

.search-submit:hover {
  background: var(--primary-hover);
  transform: translateY(-50%) scale(1.04);
}

.search-submit:active {
  transform: translateY(-50%) scale(0.97);
}

/* Suggestions dropdown */
.suggestions-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  z-index: var(--z-dropdown);
  background: var(--surface-solid);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  max-height: 260px;
  overflow-y: auto;
}

.suggestion-item {
  display: block;
  width: 100%;
  padding: var(--spacing-3) var(--spacing-5);
  font-size: var(--text-base);
  font-family: var(--font-sans);
  color: var(--text-primary);
  background: transparent;
  border: none;
  cursor: pointer;
  text-align: left;
  transition: background var(--transition-fast);
}

.suggestion-item:hover {
  background: var(--primary-light);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity var(--transition), transform var(--transition);
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ---------- Filter pills ---------- */

.filter-row {
  display: flex;
  gap: var(--spacing-2);
  justify-content: center;
  flex-wrap: wrap;
}

.filter-pill {
  padding: var(--spacing-2) var(--spacing-4);
  font-size: var(--text-sm);
  font-family: var(--font-sans);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  background: transparent;
  border: 1.5px solid var(--border-solid);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition);
  line-height: 1;
}

.filter-pill:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.filter-pill.active {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

/* ---------- History ---------- */

.history-section {
  margin-bottom: var(--spacing-xl);
}

.section-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-muted);
  margin-bottom: var(--spacing-3);
}

.clear-btn {
  font-size: var(--text-xs);
  color: var(--text-muted);
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--spacing-1) var(--spacing-2);
  border-radius: var(--radius-sm);
  transition: color var(--transition-fast), background var(--transition-fast);
}

.clear-btn:hover {
  color: var(--error);
  background: var(--error-light);
}

.history-chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-2);
}

.history-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-3);
  font-size: var(--text-sm);
  font-family: var(--font-sans);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.history-chip:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.chip-x {
  font-size: var(--text-base);
  line-height: 1;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}

.chip-x:hover {
  color: var(--error);
}

/* ---------- Results ---------- */

.results-section {
  padding-top: var(--spacing-md);
}

.result-count {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin-bottom: var(--spacing-lg);
}

.result-count strong {
  color: var(--text-primary);
  font-weight: var(--font-semibold);
}

.result-stack {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

/* --- Post card --- */

.post-card {
  display: block;
  padding: var(--spacing-5) var(--spacing-6);
  background: var(--surface-solid);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: border-color var(--transition), box-shadow var(--transition);
}

.post-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.post-card-title {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-2);
  line-height: var(--leading-snug);
  letter-spacing: var(--tracking-tight);
}

.post-card-excerpt {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
  margin: 0 0 var(--spacing-3);
}

.post-card-footer {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.post-card-author {
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.post-card-dot {
  opacity: 0.4;
}

/* --- User card --- */

.user-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
  padding: var(--spacing-4) var(--spacing-5);
  background: var(--surface-solid);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: border-color var(--transition), box-shadow var(--transition);
}

.user-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  object-fit: cover;
  flex-shrink: 0;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-display {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.user-handle {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

/* --- Circle card --- */

.circle-card {
  display: block;
  padding: var(--spacing-5) var(--spacing-6);
  background: var(--surface-solid);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: border-color var(--transition), box-shadow var(--transition);
}

.circle-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.circle-card-head {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-3);
}

.circle-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  object-fit: cover;
  flex-shrink: 0;
}

.circle-author {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.circle-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin-left: auto;
}

.circle-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
  margin: 0 0 var(--spacing-3);
}

.circle-topic-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-2);
  margin-bottom: var(--spacing-3);
}

.topic-tag {
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--primary);
  background: var(--primary-light);
  padding: 2px var(--spacing-2);
  border-radius: var(--radius-full);
  text-decoration: none;
  transition: background var(--transition-fast);
}

.topic-tag:hover {
  background: var(--primary);
  color: white;
}

.circle-img-row {
  display: flex;
  gap: var(--spacing-2);
}

.circle-thumb {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: var(--radius);
}

/* --- Tag cloud --- */

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-2);
}

.tag-chip {
  padding: var(--spacing-2) var(--spacing-4);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  font-family: var(--font-sans);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-full);
  text-decoration: none;
  transition: all var(--transition);
}

.tag-chip:hover {
  background: var(--primary-light);
  color: var(--primary);
  border-color: var(--primary);
}

/* --- Tag inline card (in "all" view) --- */

.tag-inline-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  padding: var(--spacing-4) var(--spacing-5);
  background: var(--surface-solid);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: border-color var(--transition);
}

.tag-inline-card:hover {
  border-color: var(--primary);
}

.tag-chip-lg {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--primary);
}

/* --- Topic card --- */

.topic-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  padding: var(--spacing-4) var(--spacing-5);
  background: var(--surface-solid);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: border-color var(--transition), box-shadow var(--transition);
}

.topic-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.topic-hash {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: var(--font-extrabold);
  color: var(--primary);
  line-height: 1;
  flex-shrink: 0;
}

.topic-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.topic-name {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.topic-desc {
  font-size: var(--text-xs);
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-count {
  font-size: var(--text-xs);
  color: var(--text-muted);
  flex-shrink: 0;
}

/* --- Type label --- */

.card-type-label {
  display: inline-block;
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--primary);
  background: var(--primary-light);
  padding: 1px var(--spacing-2);
  border-radius: var(--radius-sm);
  margin-bottom: var(--spacing-2);
}

/* ---------- States ---------- */

.state-block {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-lg);
}

.state-text {
  font-size: var(--text-sm);
  color: var(--error);
  margin-bottom: var(--spacing-md);
}

.state-empty-title {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-2);
}

.state-empty-hint {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin: 0;
}

.state-end {
  text-align: center;
  font-size: var(--text-xs);
  color: var(--text-muted);
  padding: var(--spacing-lg) 0;
}

/* ---------- Skeleton ---------- */

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.skeleton-card {
  padding: var(--spacing-5) var(--spacing-6);
  background: var(--surface-solid);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.sk-line {
  height: 14px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

.sk-w60 { width: 60%; }
.sk-w90 { width: 90%; }
.sk-w35 { width: 35%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ---------- Load more ---------- */

.load-more-row {
  text-align: center;
  padding: var(--spacing-lg) 0;
}

/* ---------- Responsive ---------- */

@media (max-width: 768px) {
  .search-page {
    padding: var(--spacing-lg) var(--spacing-md);
  }

  .search-hero {
    padding: var(--spacing-xl) 0 var(--spacing-lg);
  }

  .search-hero.has-results {
    padding: var(--spacing-md) 0;
  }

  .search-title {
    font-size: var(--text-3xl);
  }

  .search-hero.has-results .search-title {
    font-size: var(--text-xl);
  }

  .search-field {
    font-size: var(--text-base);
    padding: var(--spacing-3) var(--spacing-12) var(--spacing-3) var(--spacing-4);
  }

  .post-card,
  .circle-card,
  .user-card,
  .topic-card,
  .tag-inline-card {
    padding: var(--spacing-4);
  }

  .post-card-title {
    font-size: var(--text-lg);
  }

  .filter-pill {
    padding: var(--spacing-1_5) var(--spacing-3);
    font-size: var(--text-xs);
  }
}

@media (max-width: 480px) {
  .search-title {
    font-size: var(--text-2xl);
  }

  .search-subtitle {
    font-size: var(--text-sm);
  }

  .circle-thumb {
    width: 56px;
    height: 56px;
  }
}
</style>
