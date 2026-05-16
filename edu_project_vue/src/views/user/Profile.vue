<template>
  <div class="profile-page">
    <button class="back-btn" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>
    <aside class="profile-sidebar">
      <div class="sidebar-card card">
        <h3 class="sidebar-title">个人中心</h3>
        <nav class="sidebar-nav">
          <router-link to="/drafts" class="sidebar-link">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/></svg>
            我的草稿
          </router-link>
          <router-link to="/collections" class="sidebar-link">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
            我的收藏
          </router-link>
          <router-link to="/following" class="sidebar-link">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            我的关注
          </router-link>
          <router-link to="/followers" class="sidebar-link">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            我的粉丝
          </router-link>
          <router-link to="/my-reports" class="sidebar-link">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><circle cx="12" cy="16" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="8" r="1"/></svg>
            我的举报
          </router-link>
        </nav>
      </div>
    </aside>

    <div class="profile-main">
      <div v-if="pageError" class="error-card card">
        <div class="error-card-body">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="error-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <p>{{ pageError }}</p>
          <button class="btn btn-primary" @click="initLoad">重新加载</button>
        </div>
      </div>

      <template v-else>
        <div class="hero-section card">
          <div class="hero-cover">
            <img :src="getSafeImageUrl(user?.coverImage, '/default-cover.jpg')" alt="" />
            <div class="hero-gradient"></div>
            <button class="cover-upload-btn" title="更换封面图" @click="triggerCoverUpload" :disabled="coverUploading">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                <circle cx="12" cy="13" r="4"/>
              </svg>
            </button>
            <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" ref="coverInputRef" hidden @change="handleCoverUpload" />
          </div>
          <div class="hero-info">
            <div v-if="loading.user" class="hero-skeleton">
              <div class="skeleton avatar-skeleton"></div>
              <div class="skeleton skeleton-title"></div>
              <div class="skeleton skeleton-bio"></div>
              <div class="skeleton skeleton-stats-row"></div>
            </div>
            <template v-else>
              <div class="hero-avatar-wrapper">
                <img :src="user?.avatar || '/default-avatar.png'" :alt="user?.nickname" class="hero-avatar" @error="onAvatarError" />
              </div>
              <h1 class="hero-name">{{ user?.nickname || user?.username }}</h1>
              <p class="hero-bio">{{ user?.bio || '这个人很懒，什么都没写' }}</p>
              <div class="hero-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ stats.postCount || 0 }}</span>
                  <span class="stat-label">文章</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ stats.likeCount || 0 }}</span>
                  <span class="stat-label">获赞</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ stats.followerCount || 0 }}</span>
                  <span class="stat-label">粉丝</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ stats.followingCount || 0 }}</span>
                  <span class="stat-label">关注</span>
                </div>
              </div>
            </template>
            <div class="hero-actions">
              <router-link to="/profile-edit" class="btn btn-primary">编辑资料</router-link>
              <router-link to="/password-change" class="btn btn-ghost">修改密码</router-link>
            </div>
          </div>
        </div>

        <div class="tabs-section card">
          <div class="tabs-header" ref="tabsRef">
            <button v-for="(tab, i) in tabs" :key="tab.key" :ref="el => { if (el) tabEls[i] = el }" class="tab-btn" :class="{ active: activeTab === tab.key }" @click="switchTab(tab.key, i)">
              {{ tab.label }}
            </button>
            <div class="tabs-slider" :style="sliderStyle"></div>
          </div>
          <div class="tabs-body">
            <div v-show="activeTab === 'posts'" class="tab-panel">
              <div v-if="loading.posts" class="skeleton-list">
                <div v-for="n in 3" :key="n" class="skeleton-card-item">
                  <div class="skeleton skeleton-card-title"></div>
                  <div class="skeleton skeleton-card-text"></div>
                  <div class="skeleton skeleton-card-text short"></div>
                  <div class="skeleton skeleton-card-meta"></div>
                </div>
              </div>
              <div v-else-if="postsError" class="tab-error">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <p>{{ postsError }}</p>
                <button class="btn btn-sm btn-primary" @click="fetchPosts">重试</button>
              </div>
              <div v-else-if="posts.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/></svg>
                <p class="empty-title">还没有发表文章</p>
                <router-link to="/post-edit" class="btn btn-primary">写文章</router-link>
              </div>
              <div v-else>
                <div v-for="post in posts" :key="post.id" class="post-card">
                  <div class="post-card-body">
                    <h3 class="post-card-title">
                      <router-link :to="`/post/${post.id}`">{{ post.title }}</router-link>
                    </h3>
                    <p class="post-card-excerpt">{{ truncateText(post.summary, 120) }}</p>
                    <div class="post-card-meta">
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                        {{ formatRelativeTime(post.createTime) }}
                      </span>
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                        {{ post.viewCount || 0 }}
                      </span>
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                        {{ post.likeCount || 0 }}
                      </span>
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                        {{ post.commentCount || 0 }}
                      </span>
                    </div>
                  </div>
                  <div class="post-card-actions">
                    <router-link :to="`/post-edit/${post.id}`" class="post-action-btn edit">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      编辑
                    </router-link>
                    <button class="post-action-btn delete" @click="openConfirm('删除文章', `确定删除「${post.title}」吗？删除后不可恢复。`, () => deletePost(post.id))">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                      删除
                    </button>
                  </div>
                </div>
                <div v-if="postsTotalPages > 1" class="pagination-section">
                  <div class="pagination">
                    <button class="pagination-btn" :disabled="postsPage <= 1" @click="postsPage--; fetchPosts()">上一页</button>
                    <button v-for="p in postsTotalPages" :key="p" class="pagination-btn" :class="{ active: p === postsPage }" @click="postsPage = p; fetchPosts()">{{ p }}</button>
                    <button class="pagination-btn" :disabled="postsPage >= postsTotalPages" @click="postsPage++; fetchPosts()">下一页</button>
                  </div>
                  <span class="pagination-info">共 {{ postsTotal }} 篇</span>
                </div>
              </div>
            </div>

            <div v-show="activeTab === 'likes'" class="tab-panel">
              <div v-if="loading.likes" class="skeleton-list">
                <div v-for="n in 3" :key="n" class="skeleton-card-item">
                  <div class="skeleton skeleton-card-title"></div>
                  <div class="skeleton skeleton-card-text"></div>
                  <div class="skeleton skeleton-card-meta"></div>
                </div>
              </div>
              <div v-else-if="likesError" class="tab-error">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <p>{{ likesError }}</p>
                <button class="btn btn-sm btn-primary" @click="fetchLikes">重试</button>
              </div>
              <div v-else-if="likes.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                <p class="empty-title">还没有点赞过文章</p>
                <router-link to="/" class="btn btn-primary">去发现</router-link>
              </div>
              <div v-else>
                <div v-for="like in likes" :key="like.likeId" class="post-card">
                  <div class="post-card-body">
                    <h3 class="post-card-title">
                      <router-link :to="`/post/${like.postId}`">{{ like.title }}</router-link>
                    </h3>
                    <p class="post-card-excerpt">{{ truncateText(like.summary, 120) }}</p>
                    <div class="post-card-meta">
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                        点赞于 {{ formatRelativeTime(like.likeTime) }}
                      </span>
                    </div>
                  </div>
                </div>
                <div v-if="likesTotalPages > 1" class="pagination-section">
                  <div class="pagination">
                    <button class="pagination-btn" :disabled="likesPage <= 1" @click="likesPage--; fetchLikes()">上一页</button>
                    <button v-for="p in likesTotalPages" :key="p" class="pagination-btn" :class="{ active: p === likesPage }" @click="likesPage = p; fetchLikes()">{{ p }}</button>
                    <button class="pagination-btn" :disabled="likesPage >= likesTotalPages" @click="likesPage++; fetchLikes()">下一页</button>
                  </div>
                  <span class="pagination-info">共 {{ likesTotal }} 条</span>
                </div>
              </div>
            </div>

            <div v-show="activeTab === 'comments'" class="tab-panel">
              <div v-if="loading.comments" class="skeleton-list">
                <div v-for="n in 3" :key="n" class="skeleton-card-item">
                  <div class="skeleton skeleton-card-title"></div>
                  <div class="skeleton skeleton-card-text"></div>
                  <div class="skeleton skeleton-card-meta"></div>
                </div>
              </div>
              <div v-else-if="commentsError" class="tab-error">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <p>{{ commentsError }}</p>
                <button class="btn btn-sm btn-primary" @click="fetchComments">重试</button>
              </div>
              <div v-else-if="comments.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                <p class="empty-title">还没有发表过评论</p>
              </div>
              <div v-else>
                <div v-for="comment in comments" :key="comment.id" class="comment-card">
                  <div class="comment-bubble">{{ comment.content }}</div>
                  <div class="comment-meta">
                    <span>评论于</span>
                    <router-link :to="`/post/${comment.postId}`" class="comment-post-title">{{ comment.postTitle }}</router-link>
                    <span class="comment-dot">·</span>
                    <span>{{ formatRelativeTime(comment.createTime) }}</span>
                  </div>
                </div>
                <div v-if="commentsTotalPages > 1" class="pagination-section">
                  <div class="pagination">
                    <button class="pagination-btn" :disabled="commentsPage <= 1" @click="commentsPage--; fetchComments()">上一页</button>
                    <button v-for="p in commentsTotalPages" :key="p" class="pagination-btn" :class="{ active: p === commentsPage }" @click="commentsPage = p; fetchComments()">{{ p }}</button>
                    <button class="pagination-btn" :disabled="commentsPage >= commentsTotalPages" @click="commentsPage++; fetchComments()">下一页</button>
                  </div>
                  <span class="pagination-info">共 {{ commentsTotal }} 条</span>
                </div>
              </div>
            </div>

            <div v-show="activeTab === 'circle'" class="tab-panel">
              <div v-if="loading.circles" class="skeleton-list">
                <div v-for="n in 3" :key="n" class="skeleton-card-item">
                  <div class="skeleton skeleton-card-title"></div>
                  <div class="skeleton skeleton-card-text"></div>
                  <div class="skeleton skeleton-card-text short"></div>
                </div>
              </div>
              <div v-else-if="circlesError" class="tab-error">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <p>{{ circlesError }}</p>
                <button class="btn btn-sm btn-primary" @click="fetchCircles">重试</button>
              </div>
              <div v-else-if="circles.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                <p class="empty-title">还没有发布校友圈动态</p>
                <router-link to="/circle" class="btn btn-primary">去发布</router-link>
              </div>
              <div v-else>
                <div v-for="post in circles" :key="post.id" class="post-card">
                  <div class="post-card-body">
                    <p class="post-card-excerpt">{{ truncateText(post.content, 120) }}</p>
                    <div class="post-card-meta">
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                        {{ post.likeCount || 0 }}
                      </span>
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                        {{ post.commentCount || 0 }}
                      </span>
                      <span>{{ formatRelativeTime(post.createTime) }}</span>
                      <router-link :to="`/circle/${post.id}`" class="post-action-link">查看详情</router-link>
                    </div>
                  </div>
                </div>
                <div v-if="circlesTotalPages > 1" class="pagination-section">
                  <div class="pagination">
                    <button class="pagination-btn" :disabled="circlesPage <= 1" @click="circlesPage--; fetchCircles()">上一页</button>
                    <button v-for="p in circlesTotalPages" :key="p" class="pagination-btn" :class="{ active: p === circlesPage }" @click="circlesPage = p; fetchCircles()">{{ p }}</button>
                    <button class="pagination-btn" :disabled="circlesPage >= circlesTotalPages" @click="circlesPage++; fetchCircles()">下一页</button>
                  </div>
                  <span class="pagination-info">共 {{ circlesTotal }} 条</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <ConfirmDialog />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api/user'
import { postApi } from '../../api/post'
import { likeApi } from '../../api/like'
import { commentApi } from '../../api/comment'
import { followApi } from '../../api/follow'
import { mediaApi } from '../../api/media'
import { circleApi } from '../../api/circle'
import { formatRelativeTime, truncateText, getSafeImageUrl } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'
import { useConfirm } from '../../composables/useConfirm'

const userStore = useUserStore()
const router = useRouter()
const logger = useLogger('Profile')

const pageError = ref('')
const user = ref(null)
const stats = ref({})

const coverUploading = ref(false)
const coverInputRef = ref(null)

const loading = reactive({ user: false, posts: false, likes: false, comments: false, circles: false })

const posts = ref([])
const likes = ref([])
const comments = ref([])

const postsPage = ref(1)
const postsTotal = ref(0)
const postsTotalPages = ref(1)
const pageSize = 10

const likesPage = ref(1)
const likesTotal = ref(0)
const likesTotalPages = ref(1)

const commentsPage = ref(1)
const commentsTotal = ref(0)
const commentsTotalPages = ref(1)

const postsError = ref('')
const likesError = ref('')
const commentsError = ref('')

const circles = ref([])
const circlesPage = ref(1)
const circlesTotal = ref(0)
const circlesTotalPages = ref(1)
const circlesError = ref('')

const activeTab = ref('posts')
const activeTabIndex = ref(0)
const tabEls = ref([])
const tabsRef = ref(null)

const tabs = [
  { key: 'posts', label: '我的文章' },
  { key: 'circle', label: '校友圈' },
  { key: 'likes', label: '我的点赞' },
  { key: 'comments', label: '我的评论' }
]

const { confirm, ConfirmDialog } = useConfirm()

const sliderStyle = computed(() => {
  const el = tabEls.value[activeTabIndex.value]
  if (!el) return { width: '0px' }
  return {
    width: `${el.offsetWidth}px`,
    transform: `translateX(${el.offsetLeft}px)`
  }
})

function onAvatarError(e) {
  e.target.src = '/default-avatar.png'
}

const triggerCoverUpload = () => {
  coverInputRef.value?.click()
}

const handleCoverUpload = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  coverUploading.value = true
  try {
    const res = await mediaApi.uploadFile(file, 'article')
    if (res.data?.fileUrl) {
      await userApi.updateCoverImage(res.data.fileUrl)
      if (user.value) user.value.coverImage = res.data.fileUrl
      toast.success('封面图更新成功')
    }
  } catch (err) {
    logger.error('cover upload error', { error: err.message })
    toast.error(err.response?.data?.message || '封面上传失败')
  } finally {
    coverUploading.value = false
    if (coverInputRef.value) coverInputRef.value.value = ''
  }
}

async function openConfirm(title, message, onConfirm) {
  const ok = await confirm(message, title)
  if (ok) onConfirm()
}

function switchTab(key, index) {
  activeTab.value = key
  activeTabIndex.value = index
  if (key === 'posts' && posts.value.length === 0 && !postsError.value) fetchPosts()
  else if (key === 'circle' && circles.value.length === 0 && !circlesError.value) fetchCircles()
  else if (key === 'likes' && likes.value.length === 0 && !likesError.value) fetchLikes()
  else if (key === 'comments' && comments.value.length === 0 && !commentsError.value) fetchComments()
}

async function initLoad() {
  pageError.value = ''
  loading.user = true
  try {
    const [userResponse, statsResponse] = await Promise.all([
      userApi.getUserById(userStore.userId),
      followApi.getFollowCounts(userStore.userId)
    ])
    user.value = userResponse.data

    const countsData = statsResponse.data || {}
    stats.value = {
      ...countsData,
      postCount: 0,
      likeCount: 0
    }
  } catch (error) {
    logger.error('Failed to fetch user', { error: error.message })
    pageError.value = '加载用户信息失败，请稍后重试'
  } finally {
    loading.user = false
  }
}

async function fetchPosts() {
  loading.posts = true
  postsError.value = ''
  try {
    const response = await postApi.getMyPosts({ pageNum: postsPage.value, pageSize })
    const data = response.data || {}
    posts.value = data.records || []
    postsTotal.value = data.total || 0
    postsTotalPages.value = data.pages || 1
  } catch (error) {
    logger.error('Failed to fetch posts', { error: error.message })
    postsError.value = error.response?.data?.message || '加载文章列表失败'
  } finally {
    loading.posts = false
  }
}

async function fetchLikes() {
  loading.likes = true
  likesError.value = ''
  try {
    const response = await likeApi.getMyLikes({ pageNum: likesPage.value, pageSize })
    const data = response.data || {}
    likes.value = data.records || []
    likesTotal.value = data.total || 0
    likesTotalPages.value = data.pages || 1
  } catch (error) {
    logger.error('Failed to fetch likes', { error: error.message })
    likesError.value = error.response?.data?.message || '加载点赞列表失败'
  } finally {
    loading.likes = false
  }
}

async function fetchComments() {
  loading.comments = true
  commentsError.value = ''
  try {
    const response = await commentApi.getMyComments({ pageNum: commentsPage.value, pageSize })
    const data = response.data || {}
    comments.value = data.records || []
    commentsTotal.value = data.total || 0
    commentsTotalPages.value = data.pages || 1
  } catch (error) {
    logger.error('Failed to fetch comments', { error: error.message })
    commentsError.value = error.response?.data?.message || '加载评论列表失败'
  } finally {
    loading.comments = false
  }
}

const fetchCircles = async () => {
  if (!user.value?.id) return
  loading.circles = true
  circlesError.value = ''
  try {
    const res = await circleApi.getUserPosts(user.value.id, { pageNum: circlesPage.value, pageSize })
    const data = res.data || {}
    if (Array.isArray(data)) {
      circles.value = data
      circlesTotal.value = data.length
      circlesTotalPages.value = 1
    } else {
      circles.value = data.records || []
      circlesTotal.value = data.total || 0
      circlesTotalPages.value = data.pages || 1
    }
  } catch (err) {
    logger.error('fetch circles error', { error: err.message })
    circlesError.value = err.response?.data?.message || '加载失败'
    circles.value = []
  } finally {
    loading.circles = false
  }
}

async function deletePost(postId) {
  try {
    await postApi.deletePost(postId)
    posts.value = posts.value.filter(p => p.id !== postId)
    postsTotal.value = Math.max(0, postsTotal.value - 1)
    if (posts.value.length === 0 && postsPage.value > 1) {
      postsPage.value--
      await fetchPosts()
    }
    toast.success('删除成功')
  } catch (error) {
    logger.error('Failed to delete post', { error: error.message })
    toast.error(error.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  initLoad()
  nextTick(() => fetchPosts())
})
</script>

<style scoped>
.profile-page { grid-column: 1 / -1; }
.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; grid-column: 1 / -1; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 24px;
  align-items: start;
}

.profile-sidebar {
  position: sticky;
  top: 84px;
}

.sidebar-card {
  padding: 20px;
}

.sidebar-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 16px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius);
  color: var(--text-secondary);
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s ease;
  text-decoration: none;
}

.sidebar-link:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.hero-section {
  overflow: visible;
}

.hero-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.hero-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 50%, rgba(0,0,0,0.5));
}

.cover-upload-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  width: 36px; height: 36px;
  border-radius: 50%;
  background: rgba(0,0,0,0.5);
  color: #fff;
  border: 2px solid rgba(255,255,255,0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease, background 0.3s ease;
  z-index: 2;
}

.hero-cover:hover .cover-upload-btn {
  opacity: 1;
}

.cover-upload-btn:hover {
  background: rgba(0,0,0,0.7);
}

.cover-upload-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.hero-info {
  padding: 0 24px 24px;
  position: relative;
}

.hero-avatar-wrapper {
  margin-top: -44px;
  margin-bottom: 12px;
}

.hero-avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--surface);
  box-shadow: var(--shadow-md);
}

.hero-name {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.hero-bio {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.hero-stats {
  display: flex;
  gap: 28px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.hero-actions {
  display: flex;
  gap: 8px;
}

.hero-skeleton {
  padding: 12px 0;
}

.avatar-skeleton {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  margin-bottom: 16px;
}

.skeleton-title {
  width: 160px;
  height: 24px;
  margin-bottom: 8px;
}

.skeleton-bio {
  width: 240px;
  height: 16px;
  margin-bottom: 16px;
}

.skeleton-stats-row {
  width: 320px;
  height: 40px;
  margin-bottom: 16px;
}

.tabs-section {
  margin-top: 0;
}

.tabs-header {
  display: flex;
  position: relative;
  border-bottom: 1px solid var(--border);
  padding: 0;
}

.tab-btn {
  flex: 1;
  padding: 14px 20px;
  font-size: 0.875rem;
  font-weight: 500;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.3s ease;
  position: relative;
  z-index: 1;
}

.tab-btn:hover {
  color: var(--primary);
}

.tab-btn.active {
  color: var(--primary);
}

.tabs-slider {
  position: absolute;
  bottom: 0;
  height: 2px;
  background: var(--primary);
  border-radius: 2px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 2;
}

.tabs-body {
  padding: 20px;
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-card-item {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
}

.skeleton-card-title {
  width: 60%;
  height: 20px;
  margin-bottom: 10px;
}

.skeleton-card-text {
  width: 90%;
  height: 14px;
  margin-bottom: 6px;
}

.skeleton-card-text.short {
  width: 50%;
}

.skeleton-card-meta {
  width: 200px;
  height: 14px;
  margin-top: 10px;
}

.post-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
  gap: 16px;
}

.post-card:last-child {
  border-bottom: none;
}

.post-card-body {
  flex: 1;
  min-width: 0;
}

.post-card-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 6px;
  line-height: 1.4;
}

.post-card-title a {
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.2s;
}

.post-card-title a:hover {
  color: var(--primary);
}

.post-card-excerpt {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-card-meta {
  display: flex;
  gap: 16px;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.post-card-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-card-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  padding-top: 4px;
}

.post-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  font-size: 0.75rem;
  font-weight: 500;
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  background: transparent;
  color: var(--text-muted);
}

.post-action-link {
  font-weight: 500;
  color: var(--primary);
  text-decoration: none;
  font-size: 0.75rem;
}

.post-action-link:hover {
  text-decoration: underline;
}

.post-action-btn.edit:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.post-action-btn.delete:hover {
  background: rgba(239, 68, 68, 0.1);
  color: var(--error);
}

.comment-card {
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}

.comment-card:last-child {
  border-bottom: none;
}

.comment-bubble {
  font-size: 0.875rem;
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 8px;
  padding: 12px 16px;
  background: var(--background);
  border-radius: var(--radius-md);
  position: relative;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: var(--text-muted);
  flex-wrap: wrap;
}

.comment-post-title {
  font-weight: 500;
  color: var(--primary);
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comment-dot {
  color: var(--text-muted);
}

.tab-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 20px;
  color: var(--text-muted);
}

.tab-error p {
  font-size: 0.875rem;
}

.error-card {
  text-align: center;
}

.error-card-body {
  padding: 60px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.error-icon {
  color: var(--text-muted);
  opacity: 0.5;
}

.error-card-body p {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.empty-state {
  padding: 60px 24px;
  text-align: center;
}

.empty-icon {
  color: var(--text-muted);
  opacity: 0.3;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 0.9375rem;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.pagination-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
  margin-top: 4px;
}

.pagination-info {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  animation: fadeIn 0.2s ease;
}

.modal-dialog {
  width: 380px;
  max-width: 90vw;
  padding: 28px;
}

.modal-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.modal-message {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 24px;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@media (max-width: 768px) {
  .profile-page {
    grid-template-columns: 1fr;
    padding: 16px;
    gap: 16px;
  }

  .profile-sidebar {
    display: none;
  }

  .hero-cover {
    height: 140px;
  }

  .hero-avatar {
    width: 72px;
    height: 72px;
  }

  .hero-info {
    padding: 0 16px 16px;
  }

  .hero-stats {
    gap: 20px;
  }

  .stat-value {
    font-size: 1.0625rem;
  }

  .tabs-body {
    padding: 16px;
  }

  .post-card {
    flex-direction: column;
  }

  .post-card-actions {
    width: 100%;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid var(--border);
  }

  .post-card-meta {
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>
