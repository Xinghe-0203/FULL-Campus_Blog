<template>
  <div class="search-page">
    <div class="search-container">
      <div class="search-header card">
        <h1>搜索</h1>
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="搜索文章、用户..."
            @keyup.enter="doSearch"
            maxlength="200"
          />
          <button class="btn btn-primary" @click="doSearch">搜索</button>
        </div>
        <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-dropdown">
          <div
            v-for="(suggestion, index) in suggestions"
            :key="index"
            class="suggestion-item"
            @mousedown.prevent="selectSuggestion(suggestion)"
          >
            {{ suggestion }}
          </div>
        </div>
        <div class="search-filters">
          <button 
            v-for="filter in filters" 
            :key="filter.value"
            class="filter-btn"
            :class="{ active: activeFilter === filter.value }"
            @click="setFilter(filter.value)"
          >
            {{ filter.label }}
          </button>
        </div>
      </div>

      <!-- Search History -->
      <div v-if="!hasSearched && searchHistory.length > 0" class="search-history">
        <div class="history-header">
          <span>搜索历史</span>
          <button class="btn-text" @click="clearSearchHistory">清除</button>
        </div>
        <div class="history-list">
          <div
            v-for="item in searchHistory"
            :key="item"
            class="history-item"
            @click="searchQuery = item; doSearch()"
          >
            <span class="history-text">{{ item }}</span>
            <button class="history-remove" @click.stop="removeHistoryItem(item)">x</button>
          </div>
        </div>
      </div>

      <div v-if="hasSearched" class="search-results">
        <div v-if="error" class="error-state">
          <p>{{ error }}</p>
        </div>
        <div v-else-if="loading && results.length === 0" class="loading-state">
          <p>搜索中...</p>
        </div>
        <template v-else>
          <div class="results-header">
            <span>共找到 {{ totalResults }} 个结果</span>
          </div>

          <div v-if="results.length > 0" class="result-list">
          <template v-if="activeFilter === 'users'">
            <div v-for="item in results" :key="item.userId" class="result-item card">
              <div class="user-result">
                <img v-if="item.avatar" :src="item.avatar" class="user-avatar" />
                <div class="user-info">
                  <router-link :to="`/user/${item.userId}`" class="user-name">
                    {{ item.nickname || item.username }}
                  </router-link>
                  <p class="user-username">@{{ item.username }}</p>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <div v-for="item in results" :key="item.id" class="result-item card">
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
        </div>
        
        <div v-if="hasMore" class="load-more">
          <button class="btn btn-ghost" @click="loadMore" :disabled="loading">
            {{ loading ? '加载中...' : '加载更多' }}
          </button>
        </div>
        
        <div v-else-if="results.length > 0" class="empty-state">
          <p>没有更多结果了</p>
        </div>

        <div v-else class="empty-state">
          <p>未找到相关内容</p>
        </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { postApi } from '../../api/post'
import { userApi } from '../../api/user'
import { formatRelativeTime, truncateText, debounce } from '../../utils'
import { useLogger } from '../../utils/logger'

const route = useRoute()
const logger = useLogger('Search')

const searchQuery = ref('')
const activeFilter = ref('all')
const results = ref([])
const totalResults = ref(0)
const hasSearched = ref(false)
const loading = ref(false)
const error = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const hasMore = ref(false)
const pageSize = 20
const searchHistory = ref([])
const maxHistoryItems = 10
const suggestions = ref([])
const showSuggestions = ref(false)

const fetchSuggestions = debounce(async (keyword) => {
  if (!keyword.trim() || activeFilter.value === 'users') {
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

const selectSuggestion = (suggestion) => {
  searchQuery.value = suggestion
  showSuggestions.value = false
  doSearch()
}

const filters = [
  { label: '全部', value: 'all' },
  { label: '文章', value: 'posts' },
  { label: '用户', value: 'users' }
]

const doSearch = async () => {
  if (!searchQuery.value.trim()) return

  error.value = ''
  loading.value = true
  hasSearched.value = true
  
  try {
    let response
    if (activeFilter.value === 'users') {
      response = await userApi.searchUsers({
        keyword: searchQuery.value,
        page: currentPage.value,
        pageSize: pageSize
      })
    } else {
      response = await postApi.advancedSearch({
        keyword: searchQuery.value,
        pageNum: currentPage.value,
        pageSize: pageSize
      })
    }
    const pageData = response.data || {}
    if (currentPage.value === 1) {
      results.value = pageData.records || []
    } else {
      results.value = [...results.value, ...(pageData.records || [])]
    }
    totalResults.value = pageData.total || 0
    totalPages.value = pageData.pages || 1
    hasMore.value = currentPage.value < totalPages.value
    // Save to search history on successful first-page search
    if (currentPage.value === 1) {
      saveSearchHistory(searchQuery.value)
    }
  } catch (err) {
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

const setFilter = (filter) => {
  activeFilter.value = filter
  currentPage.value = 1
  if (hasSearched.value) {
    doSearch()
  }
}

// Search history management
const loadSearchHistory = () => {
  try {
    const history = localStorage.getItem('edu_search_history')
    searchHistory.value = history ? JSON.parse(history) : []
  } catch {
    searchHistory.value = []
  }
}

const saveSearchHistory = (query) => {
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
    // ignore storage errors
  }
}

const clearSearchHistory = () => {
  searchHistory.value = []
  try {
    localStorage.removeItem('edu_search_history')
  } catch {
    // ignore storage errors
  }
}

const removeHistoryItem = (item) => {
  searchHistory.value = searchHistory.value.filter(h => h !== item)
  try {
    localStorage.setItem('edu_search_history', JSON.stringify(searchHistory.value))
  } catch {
    // ignore storage errors
  }
}

watch(() => route.query.q, (q) => {
  if (q) {
    searchQuery.value = q
    currentPage.value = 1
    results.value = []
    doSearch()
  }
})

watch(searchQuery, (val) => {
  fetchSuggestions(val)
})

onMounted(() => {
  if (route.query.q) {
    searchQuery.value = route.query.q
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
  max-width: 800px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.search-header {
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
}

.search-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: var(--spacing-md);
}

.search-box {
  position: relative;
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.search-box input {
  flex: 1;
  padding: var(--spacing-md);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  font-size: 1rem;
}

.search-box input:focus {
  outline: none;
  border-color: var(--primary);
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 100;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: 240px;
  overflow-y: auto;
}

.suggestion-item {
  padding: 10px 16px;
  font-size: 0.875rem;
  color: var(--text-primary);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.suggestion-item:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.search-filters {
  display: flex;
  gap: var(--spacing-sm);
}

.filter-btn {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 0.875rem;
  background: none;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}

.filter-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.filter-btn.active {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

.results-header {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin-bottom: var(--spacing-md);
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.result-item {
  padding: var(--spacing-lg);
}

.result-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
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

.user-result {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
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

.empty-state {
  text-align: center;
  padding: var(--spacing-2xl);
}

.empty-state p {
  color: var(--text-muted);
}

.error-state {
  text-align: center;
  padding: var(--spacing-2xl);
}

.error-state p {
  color: #F44336;
}

.loading-state {
  text-align: center;
  padding: var(--spacing-2xl);
}

.loading-state p {
  color: var(--text-muted);
}

.load-more {
  text-align: center;
  padding: var(--spacing-lg);
}

.search-history {
  margin-top: var(--spacing-lg);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
  font-size: 0.875rem;
  color: var(--text-muted);
}

.btn-text {
  background: none;
  border: none;
  color: var(--primary);
  cursor: pointer;
  font-size: 0.875rem;
}

.btn-text:hover {
  text-decoration: underline;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.history-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  background: var(--bg-secondary);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 0.875rem;
}

.history-item:hover {
  background: var(--border);
}

.history-text {
  color: var(--text-secondary);
}

.history-remove {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 0.75rem;
  padding: 0;
  line-height: 1;
}

.history-remove:hover {
  color: var(--text-primary);
}
</style>
