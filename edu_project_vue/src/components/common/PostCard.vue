<template>
  <div class="post-card" @click="handleClick">
    <div v-if="showImage && post.coverImage" class="post-card-image">
      <img :src="post.coverImage" :alt="post.title" loading="lazy" />
    </div>
    <div class="post-card-body">
      <h3 class="post-card-title">{{ post.title }}</h3>
      <p class="post-card-summary">{{ summary }}</p>
      <div v-if="showTags && post.tags?.length" class="post-card-tags">
        <span v-for="tag in post.tags" :key="tag" class="badge">{{ tag }}</span>
      </div>
      <div class="post-card-footer">
        <div v-if="showAuthor && post.author" class="post-card-author">
          <img :src="post.author.avatar" :alt="post.author.nickname" class="avatar avatar-sm" />
          <span class="post-card-author-name">{{ post.author.nickname }}</span>
          <span v-if="post.createdAt" class="post-card-date">{{ formatDate(post.createdAt) }}</span>
        </div>
        <div v-if="showStats" class="post-card-stats">
          <span class="stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
            {{ post.likeCount || 0 }}
          </span>
          <span class="stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            {{ post.commentCount || 0 }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  post: { type: Object, required: true },
  showImage: { type: Boolean, default: true },
  showAuthor: { type: Boolean, default: true },
  showStats: { type: Boolean, default: true },
  showTags: { type: Boolean, default: true }
})
const emit = defineEmits(['click'])
const router = useRouter()

const summary = computed(() => {
  return props.post.summary || (props.post.content ? props.post.content.replace(/<[^>]*>/g, '').slice(0, 150) : '')
})

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 2592000000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString('zh-CN')
}

function handleClick() {
  emit('click', props.post)
  if (props.post.id) {
    router.push(`/post/${props.post.id}`)
  }
}
</script>

<style scoped>
.post-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: all var(--transition);
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.post-card-image {
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.post-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.post-card:hover .post-card-image img {
  transform: scale(1.05);
}

.post-card-body {
  padding: var(--spacing-lg);
}

.post-card-title {
  font-size: 1.0625rem;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: var(--spacing-sm);
}

.post-card-summary {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: var(--spacing-md);
}

.post-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: var(--spacing-md);
}

.post-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-sm);
}

.post-card-author {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.post-card-author-name {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-card-date {
  font-size: 0.75rem;
  color: var(--text-muted);
  white-space: nowrap;
}

.post-card-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.stat svg {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .post-card-image {
    height: 140px;
  }
  .post-card-body {
    padding: var(--spacing-md);
  }
}
</style>
