<template>
  <div class="post-edit-page">
    <button class="back-btn" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>
    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
    <div v-else class="edit-container">
      <div class="editor-header card">
        <div class="header-top">
          <input v-model="form.title" type="text" class="title-input" placeholder="请输入文章标题..." maxlength="200" />
          <div class="header-actions">
            <span class="save-status" :class="saveStatus">{{ saveStatusText }}</span>
            <button class="btn btn-ghost btn-sm" @click="saveDraft" :disabled="saving || !form.title && !form.content">保存草稿</button>
            <button class="btn btn-secondary btn-sm" @click="showPreview = !showPreview">{{ showPreview ? '✏️ 编辑' : '👁️ 预览' }}</button>
            <button class="btn btn-primary btn-sm" @click="publishPost" :disabled="publishing || !form.title.trim() || !form.content.trim()">{{ publishing ? '发布中...' : '发布' }}</button>
          </div>
        </div>
        <div class="header-meta">
          <span>字数：{{ wordCount }}</span>
          <span>·</span>
          <span>预计阅读：{{ readingTime }}分钟</span>
          <span v-if="form.category" class="meta-category">· 分类：{{ categoryLabel(form.category) }}</span>
          <span v-if="route.params.id && postInfo.createTime" class="meta-time">· 创建于 {{ formatTime(postInfo.createTime) }}</span>
        </div>
      </div>

      <div class="editor-body card">
        <div v-if="!showPreview" class="editor-main">
          <div class="toolbar">
            <div class="toolbar-group">
              <button class="tool-btn" title="撤销 (Ctrl+Z)" @click="undoAction" :disabled="!canUndo"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 14 4 9 9 4"/><path d="M20 20v-7a4 4 0 0 0-4-4H4"/></svg></button>
              <button class="tool-btn" title="重做 (Ctrl+Y)" @click="redoAction" :disabled="!canRedo"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 14 20 9 15 4"/><path d="M4 20v-7a4 4 0 0 0 4-4h12"/></svg></button>
            </div>
            <span class="toolbar-divider"></span>
            <div class="toolbar-group">
              <select class="font-size-select" title="字号" @change="insertHeading($event.target.value)" :value="currentHeading">
                <option value="">正文</option>
                <option value="1">标题1</option>
                <option value="2">标题2</option>
                <option value="3">标题3</option>
                <option value="4">标题4</option>
              </select>
            </div>
            <span class="toolbar-divider"></span>
            <div class="toolbar-group">
              <button class="tool-btn" title="粗体 (Ctrl+B)" @click="insertMarkdown('**', '**')"><b>B</b></button>
              <button class="tool-btn" title="斜体 (Ctrl+I)" @click="insertMarkdown('*', '*')"><i>I</i></button>
              <button class="tool-btn" title="删除线" @click="insertMarkdown('~~', '~~')"><s>S</s></button>
              <button class="tool-btn" title="下划线 (Ctrl+U)" @click="insertMarkdown('<u>', '</u>')"><span style="text-decoration:underline">U</span></button>
              <button class="tool-btn" title="高亮" @click="insertMarkdown('<mark>', '</mark>')"><span style="background:yellow;color:black;padding:0 2px;border-radius:2px">H</span></button>
            </div>
            <span class="toolbar-divider"></span>
            <div class="toolbar-group">
              <button class="tool-btn" title="行内代码" @click="insertCode"><code style="font-size:10px">&lt;&gt;</code></button>
              <button class="tool-btn" title="代码块" @click="insertCodeBlock"><code style="font-size:10px">&lt;/&gt;</code></button>
            </div>
            <span class="toolbar-divider"></span>
            <div class="toolbar-group">
              <button class="tool-btn" title="标题2" @click="insertMarkdown('## ', '')"><span style="font-size:11px;font-weight:700">H2</span></button>
              <button class="tool-btn" title="标题3" @click="insertMarkdown('### ', '')"><span style="font-size:10px;font-weight:700">H3</span></button>
              <button class="tool-btn" title="引用" @click="insertMarkdown('> ', '')"><span style="font-size:12px">❝</span></button>
            </div>
            <span class="toolbar-divider"></span>
            <div class="toolbar-group">
              <button class="tool-btn" title="无序列表" @click="insertMarkdown('- ', '')"><span style="font-size:14px">•</span></button>
              <button class="tool-btn" title="有序列表" @click="insertMarkdown('1. ', '')"><span style="font-size:12px">1.</span></button>
              <button class="tool-btn" title="任务列表" @click="insertMarkdown('- [ ] ', '')"><span style="font-size:12px">☐</span></button>
            </div>
            <span class="toolbar-divider"></span>
            <div class="toolbar-group">
              <button class="tool-btn" title="居左" @click="insertAlignLeft"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="17" y1="10" x2="3" y2="10"/><line x1="21" y1="6" x2="3" y2="6"/><line x1="21" y1="14" x2="3" y2="14"/><line x1="17" y1="18" x2="3" y2="18"/></svg></button>
              <button class="tool-btn" title="居中" @click="insertAlignCenter"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="10" x2="6" y2="10"/><line x1="21" y1="6" x2="3" y2="6"/><line x1="21" y1="14" x2="3" y2="14"/><line x1="18" y1="18" x2="6" y2="18"/></svg></button>
              <button class="tool-btn" title="居右" @click="insertAlignRight"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="21" y1="10" x2="7" y2="10"/><line x1="21" y1="6" x2="3" y2="6"/><line x1="21" y1="14" x2="3" y2="14"/><line x1="21" y1="18" x2="7" y2="18"/></svg></button>
            </div>
            <span class="toolbar-divider"></span>
            <div class="toolbar-group">
              <button class="tool-btn" title="链接" @click="insertLink"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg></button>
              <button class="tool-btn" title="上传图片" @click="$refs.imageInput.click()"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg></button>
              <button class="tool-btn" title="表格" @click="insertMarkdown('\n| 列1 | 列2 | 列3 |\n| --- | --- | --- |\n| 内容 | 内容 | 内容 |\n', '')"><span style="font-size:11px">⊞</span></button>
            </div>
            <span class="toolbar-divider"></span>
            <div class="toolbar-group">
              <button class="tool-btn" title="分割线" @click="insertMarkdown('\n---\n', '')"><span style="font-size:14px">—</span></button>
              <button class="tool-btn" title="清除格式" @click="clearFormat"><span style="font-size:10px">⌫</span></button>
            </div>
            <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" ref="imageInput" hidden @change="uploadContentImage" />
            <div v-if="uploadingImage" class="upload-progress">
              <div class="upload-progress-bar" :style="{ width: uploadProgress + '%' }"></div>
              <span class="upload-progress-text">上传中... {{ uploadProgress }}%</span>
            </div>
          </div>
          <textarea v-model="form.content" class="content-input" placeholder="请输入文章内容（支持Markdown语法）..." @keydown.tab.prevent="insertTab" @keydown.ctrl.z.prevent="undoAction" @keydown.ctrl.y.prevent="redoAction" @keydown.ctrl.b.prevent="insertMarkdown('**', '**')" @keydown.ctrl.i.prevent="insertMarkdown('*', '*')" @keydown.ctrl.u.prevent="insertMarkdown('<u>', '</u>')" @keydown.shift.space.prevent="insertMarkdown('<mark>', '</mark>')" ref="contentTextarea" maxlength="50000"></textarea>
        </div>
        <div v-else class="editor-preview">
          <div v-if="form.content" class="markdown-body" v-html="renderedContent"></div>
          <div v-else class="empty-preview">暂无内容可预览</div>
        </div>
      </div>

      <div class="editor-sidebar">
        <div class="sidebar-section card">
          <h3 class="sidebar-title">文章信息</h3>
          <div class="form-group">
            <label class="form-label">摘要</label>
            <textarea v-model="form.summary" class="summary-input" placeholder="文章摘要（选填，用于列表展示）" maxlength="500" rows="3"></textarea>
            <span class="char-hint">{{ form.summary.length }}/500</span>
          </div>
        </div>

        <div class="sidebar-section card">
          <h3 class="sidebar-title">封面图</h3>
          <div class="cover-upload">
            <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" @change="handleCoverUpload" ref="coverInput" hidden />
            <div v-if="form.coverImage" class="cover-preview">
              <img :src="form.coverImage" alt="封面图" />
              <button class="remove-cover" @click="removeCover">✕</button>
            </div>
            <button v-else class="upload-btn" @click="$refs.coverInput.click()">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              <span>点击上传封面图</span>
            </button>
          </div>
        </div>

        <div class="sidebar-section card">
          <h3 class="sidebar-title">分类</h3>
          <select v-model="form.category" class="form-input">
            <option value="">请选择分类</option>
            <option value="tech">技术</option>
            <option value="life">生活</option>
            <option value="study">学习</option>
            <option value="other">其他</option>
          </select>
        </div>

        <div class="sidebar-section card">
          <h3 class="sidebar-title">标签</h3>
          <div class="tag-input-container">
            <div class="selected-tags">
              <span v-for="tag in selectedTags" :key="tag.id || tag.name" class="tag-badge">
                {{ tag.name }}<button class="remove-tag" @click="removeTag(tag)">✕</button>
              </span>
            </div>
            <input v-model="tagInput" type="text" class="tag-input" placeholder="输入标签搜索..." @keyup.enter="addTag" @input="filterTags" maxlength="50" />
          </div>
          <div v-if="filteredTags.length" class="suggested-tags">
            <button v-for="tag in filteredTags" :key="tag.id" class="suggested-tag" @click="addSuggestedTag(tag)">{{ tag.name }}</button>
          </div>
          <div v-if="tagInput.trim() && !allTags.find(t => t.name === tagInput.trim())" class="no-tags">
            <button class="btn btn-text btn-xs" @click="createTagAndAdd">创建「{{ tagInput }}」标签</button>
          </div>
        </div>

        <div class="sidebar-section card">
          <h3 class="sidebar-title">话题</h3>
          <select v-model="form.topicId" class="form-input">
            <option :value="null">不选择话题</option>
            <option v-for="topic in topics" :key="topic.id" :value="topic.id">{{ topic.name }}</option>
            <option v-if="form.topicId && !topics.find(t => t.id === form.topicId)" :value="form.topicId">{{ savedTopicName || '当前话题' }}</option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { postApi } from '../../api/post'
import { tagApi } from '../../api/tag'
import { topicApi } from '../../api/topic'
import { mediaApi } from '../../api/media'
import { useUserStore } from '../../stores/user'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('PostEdit')

const form = reactive({ title: '', content: '', summary: '', category: '', coverImage: '', tags: [], topicId: null })
const postInfo = reactive({ createTime: '', viewCount: 0, likeCount: 0 })
const showPreview = ref(false)
const saving = ref(false)
const publishing = ref(false)
const tagInput = ref('')
const selectedTags = ref([])
const allTags = ref([])
const filteredTags = ref([])
const topics = ref([])
const savedTopicName = ref('')
const currentDraftId = ref(null)
const contentTextarea = ref(null)
const coverInput = ref(null)
const saveStatus = ref('') // '' | 'saving' | 'saved'
const uploadingImage = ref(false)
const uploadProgress = ref(0)
const isLoading = ref(false)
const dirty = ref(false)

let autoSaveTimer = null
watch([() => form.title, () => form.content, () => form.summary, () => form.category, () => form.coverImage, () => form.topicId, selectedTags], () => {
  dirty.value = true
  clearTimeout(autoSaveTimer)
  autoSaveTimer = setTimeout(() => { if (form.title || form.content) autoSave() }, 5000)
}, { deep: true })

const renderedContent = computed(() => {
  if (!form.content) return ''
  return DOMPurify.sanitize(marked.parse(form.content))
})
const wordCount = computed(() => form.content.replace(/\s/g, '').length)
const readingTime = computed(() => Math.max(1, Math.ceil(wordCount.value / 300)))
const saveStatusText = computed(() => {
  if (saveStatus.value === 'saving') return '保存中...'
  if (saveStatus.value === 'saved') return '已保存'
  return ''
})

function categoryLabel(cat) {
  const map = { tech: '技术', life: '生活', study: '学习', other: '其他' }
  return map[cat] || cat
}

function formatTime(t) {
  if (!t) return ''
  try { return new Date(t).toLocaleString('zh-CN') } catch { return t }
}

function insertMarkdown(before, after) {
  const ta = contentTextarea.value
  if (!ta) return
  const start = ta.selectionStart, end = ta.selectionEnd
  const text = form.content
  const selected = text.substring(start, end)
  saveHistory()
  form.content = text.substring(0, start) + before + selected + after + text.substring(end)
  nextTick(() => { ta.focus(); ta.setSelectionRange(start + before.length, start + before.length + selected.length) })
}

function insertCode() {
  insertMarkdown('`', '`')
}

function insertCodeBlock() {
  insertMarkdown('```\n', '\n```')
}

function insertAlignLeft() {
  insertMarkdown('<div style=\u0022text-align:left\u0022>', '</div>')
}

function insertAlignCenter() {
  insertMarkdown('<div style=\u0022text-align:center\u0022>', '</div>')
}

function insertAlignRight() {
  insertMarkdown('<div style=\u0022text-align:right\u0022>', '</div>')
}

function insertLink() {
  const ta = contentTextarea.value
  if (!ta) return
  const start = ta.selectionStart, end = ta.selectionEnd
  const selected = form.content.substring(start, end) || '链接文字'
  const linkText = `[${selected}](url)`
  saveHistory()
  form.content = form.content.substring(0, start) + linkText + form.content.substring(end)
}

function insertTab(e) {
  const ta = e.target
  const start = ta.selectionStart, end = ta.selectionEnd
  saveHistory()
  form.content = form.content.substring(0, start) + '  ' + form.content.substring(end)
  nextTick(() => ta.setSelectionRange(start + 2, start + 2))
}

// Undo/Redo functionality
const history = ref([''])
const historyIndex = ref(0)
const currentHeading = ref('')

const canUndo = computed(() => historyIndex.value > 0)
const canRedo = computed(() => historyIndex.value < history.value.length - 1)

function saveHistory() {
  if (history.value[historyIndex.value] === form.content) return
  if (historyIndex.value < history.value.length - 1) {
    history.value = history.value.slice(0, historyIndex.value + 1)
  }
  history.value.push(form.content)
  historyIndex.value = history.value.length - 1
  if (history.value.length > 50) {
    history.value.shift()
    historyIndex.value--
  }
}

let historyTimer = null
watch(() => form.content, () => {
  clearTimeout(historyTimer)
  historyTimer = setTimeout(() => saveHistory(), 2000)
})

function undoAction() {
  if (canUndo.value) {
    historyIndex.value--
    form.content = history.value[historyIndex.value]
  }
}

function redoAction() {
  if (canRedo.value) {
    historyIndex.value++
    form.content = history.value[historyIndex.value]
  }
}

function insertHeading(level) {
  if (!level) return
  const prefix = '#'.repeat(parseInt(level) + 1) + ' '
  const ta = contentTextarea.value
  if (!ta) return
  const start = ta.selectionStart
  const lineStart = form.content.lastIndexOf('\n', start - 1) + 1
  saveHistory()
  form.content = form.content.substring(0, lineStart) + prefix + form.content.substring(lineStart)
  currentHeading.value = ''
}

function clearFormat() {
  const ta = contentTextarea.value
  if (!ta) return
  const start = ta.selectionStart, end = ta.selectionEnd
  if (start === end) return
  const selected = form.content.substring(start, end)
  let cleaned = selected
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/\*(.+?)\*/g, '$1')
    .replace(/~~(.+?)~~/g, '$1')
    .replace(/`(.+?)`/g, '$1')
    .replace(/<u>(.+?)<\/u>/g, '$1')
    .replace(/<mark>(.+?)<\/mark>/g, '$1')
  saveHistory()
  form.content = form.content.substring(0, start) + cleaned + form.content.substring(end)
}

async function uploadContentImage(e) {
  const file = e.target.files?.[0]
  if (!file) return
  uploadingImage.value = true
  try {
    const res = await mediaApi.uploadFile(file, 'article')
    const url = res.data?.fileUrl || res.data?.url
    if (url) {
      const ta = contentTextarea.value
      const start = ta?.selectionStart ?? form.content.length
      const md = `\n![图片](${url})\n`
      saveHistory()
      form.content = form.content.substring(0, start) + md + form.content.substring(ta?.selectionEnd ?? start)
    }
    toast.success('图片已插入')
  } catch (err) {
    logger.error('upload content image error', { error: err.message })
    toast.error(err.response?.data?.message || '图片上传失败')
  } finally {
    uploadingImage.value = false
    e.target.value = ''
  }
}

// ========== 标签 ==========

const fetchTags = async () => {
  try {
    const response = await tagApi.getTagList()
    allTags.value = response.data || []
  } catch (error) {
    logger.error('Failed to fetch tags', { error: error.message })
  }
}

const fetchTopics = async () => {
  try {
    const response = await topicApi.getTopicList()
    topics.value = response.data || []
  } catch (error) {
    logger.error('Failed to fetch topics', { error: error.message })
  }
}

function filterTags() {
  const input = tagInput.value.trim().toLowerCase()
  if (!input) { filteredTags.value = []; return }
  filteredTags.value = allTags.value.filter(t => t.name.toLowerCase().includes(input) && !selectedTags.value.find(s => s.id === t.id)).slice(0, 8)
}

function addTag() {
  const name = tagInput.value.trim()
  if (!name) return
  const existing = allTags.value.find(t => t.name === name)
  if (existing) {
    if (!selectedTags.value.find(s => s.id === existing.id)) selectedTags.value.push(existing)
  } else {
    if (!selectedTags.value.find(s => s.name === name)) selectedTags.value.push({ name })
  }
  tagInput.value = ''
  filteredTags.value = []
}

function addSuggestedTag(tag) {
  if (!selectedTags.value.find(s => s.id === tag.id)) selectedTags.value.push(tag)
  tagInput.value = ''
  filteredTags.value = []
}

function removeTag(tag) {
  selectedTags.value = selectedTags.value.filter(s => tag.id ? s.id !== tag.id : s.name !== tag.name)
}

async function createTagAndAdd() {
  const name = tagInput.value.trim()
  if (!name || selectedTags.value.find(s => s.name === name)) return
  try {
    const res = await tagApi.createTag({ name })
    const newTag = res.data
    if (newTag && newTag.id) {
      selectedTags.value.push(newTag)
    }
    await fetchTags()
    tagInput.value = ''
    toast.success('标签已创建')
  } catch (err) {
    logger.error('create tag error', { error: err.message })
    toast.error(err.response?.data?.message || '创建标签失败')
  }
}

// ========== 封面图 ==========

async function handleCoverUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const res = await mediaApi.uploadFile(file)
    form.coverImage = res.data?.fileUrl || res.data?.url || ''
    toast.success('封面图上传成功')
  } catch (err) {
    logger.error('cover upload error', { error: err.message })
    toast.error(err.response?.data?.message || '上传封面图失败')
  }
  if (coverInput.value) coverInput.value.value = ''
}

function removeCover() { form.coverImage = '' }

// ========== 文章数据 ==========

const fetchPost = async () => {
  if (!route.params.id) return
  try {
    const res = await postApi.getPostById(route.params.id)
    const post = res.data
    form.title = post.title || ''
    form.content = post.content || ''
    form.summary = post.summary || ''
    form.category = post.category || ''
    form.coverImage = post.coverImage || ''
    form.topicId = post.topicId || null
    savedTopicName.value = post.topicName || ''
    selectedTags.value = post.tags || []
    postInfo.createTime = post.createTime
    postInfo.viewCount = post.viewCount || 0
    postInfo.likeCount = post.likeCount || 0
  } catch (err) {
    logger.error('fetch post error', { error: err.message })
    toast.error(err.response?.data?.message || '加载文章失败')
  }
}

const fetchDraft = async () => {
  try {
    const res = await postApi.getLatestDraft()
    if (res.data) {
      currentDraftId.value = res.data?.draftId || null
      form.title = res.data.title || ''
      form.content = res.data.content || ''
      form.summary = res.data.summary || ''
      form.category = res.data.category || ''
      selectedTags.value = res.data.tags || []
      form.coverImage = res.data.coverImage || ''
      form.topicId = res.data.topicId || null
    }
  } catch (err) {
    logger.error('fetch draft error', { error: err.message })
    toast.error(err.response?.data?.message || '加载草稿失败')
  }
}

const fetchDraftById = async (draftId) => {
  try {
    const res = await postApi.getDraft(draftId)
    if (res.data) {
      currentDraftId.value = draftId
      form.title = res.data.title || ''
      form.content = res.data.content || ''
      form.summary = res.data.summary || ''
      form.category = res.data.category || ''
      selectedTags.value = res.data.tags || []
      form.coverImage = res.data.coverImage || ''
      form.topicId = res.data.topicId || null
    }
  } catch (err) {
    logger.error('fetch draft by id error', { error: err.message })
    toast.error(err.response?.data?.message || '加载草稿失败')
  }
}

// ========== 保存与发布 ==========

const saveDraft = async () => {
  if (!form.title && !form.content) { toast.warning('请输入标题或内容'); return }
  saving.value = true
  saveStatus.value = 'saving'
  try {
    const data = { title: form.title, content: form.content, summary: form.summary, category: form.category, coverImage: form.coverImage, tagIds: selectedTags.value.filter(t => t.id).map(t => t.id), tagNames: selectedTags.value.map(t => t.name), topicId: form.topicId || undefined, draftId: currentDraftId.value || undefined }
    if (route.params.id) data.postId = Number(route.params.id)
    const res = await postApi.saveDraft(data)
    currentDraftId.value = res.data || currentDraftId.value
    saveStatus.value = 'saved'
    setTimeout(() => { if (saveStatus.value === 'saved') saveStatus.value = '' }, 3000)
  } catch (err) {
    logger.error('save draft error', { error: err.message })
    saveStatus.value = ''
    toast.error(err.response?.data?.message || '保存草稿失败')
  } finally { saving.value = false }
}

const publishPost = async () => {
  if (!form.title.trim()) { toast.warning('请输入文章标题'); return }
  if (!form.content.trim()) { toast.warning('请输入文章内容'); return }
  if (!form.category) { toast.warning('请选择文章分类'); return }
  if (form.content.length > 50000) { toast.warning('文章内容不能超过50000字符'); return }

  publishing.value = true
  try {
    const postData = { title: form.title, content: form.content, summary: form.summary, category: form.category, coverImage: form.coverImage, tagIds: selectedTags.value.filter(t => t.id).map(t => t.id), tagNames: selectedTags.value.map(t => t.name), topicId: form.topicId || undefined }

    if (route.params.id) {
      await postApi.updatePost(route.params.id, postData)
      toast.success('文章更新成功')
    } else {
      await postApi.createPost(postData)
      toast.success('文章发布成功')
    }

    if (currentDraftId.value) {
      try { await postApi.deleteDraft(currentDraftId.value) } catch { /* ignore */ }
    }
    router.push('/')
  } catch (err) {
    logger.error('publish error', { error: err.message })
    toast.error(err.response?.data?.message || '发布失败')
  } finally { publishing.value = false }
}

const autoSave = async () => {
  if (!dirty.value) return
  if (!form.title && !form.content) return
  saveStatus.value = 'saving'
  try {
    const data = { title: form.title, content: form.content, summary: form.summary, category: form.category, coverImage: form.coverImage, tagIds: selectedTags.value.filter(t => t.id).map(t => t.id), tagNames: selectedTags.value.map(t => t.name), topicId: form.topicId || undefined, draftId: currentDraftId.value || undefined }
    if (route.params.id) data.postId = Number(route.params.id)
    const res = await postApi.saveDraft(data)
    currentDraftId.value = res.data || currentDraftId.value
    dirty.value = false
    saveStatus.value = 'saved'
    setTimeout(() => { if (saveStatus.value === 'saved') saveStatus.value = '' }, 3000)
  } catch { dirty.value = true; saveStatus.value = '' }
}

function handleBeforeUnload(e) {
  if (dirty.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

onMounted(async () => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  isLoading.value = true
  await fetchTags()
  await fetchTopics()
  if (route.params.id) {
    await fetchPost()
  } else if (route.query.draft) {
    await fetchDraftById(route.query.draft)
  } else {
    await fetchDraft()
  }
  isLoading.value = false
  history.value = [form.content]
  historyIndex.value = 0
  dirty.value = false
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  if (autoSaveTimer) { clearTimeout(autoSaveTimer); autoSaveTimer = null }
})

onBeforeRouteLeave((to, from) => {
  if (dirty.value) {
    const answer = window.confirm('有未保存的更改，确定要离开吗？')
    if (!answer) return false
  }
})
</script>

<style scoped>
.post-edit-page { max-width: 1100px; margin: 0 auto; padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.edit-container { display: grid; grid-template-columns: 1fr 320px; gap: 16px; align-items: start; }
.editor-header { grid-column: 1 / -1; padding: 16px 20px; }
.header-top { display: flex; align-items: center; gap: 12px; }
.title-input { flex: 1; border: none; font-size: 1.5rem; font-weight: 700; background: transparent; color: var(--text-primary); overflow-wrap: break-word; word-break: break-word; }
.title-input:focus { outline: none; }
.title-input::placeholder { color: var(--text-muted); }
.header-actions { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.save-status { font-size: 0.75rem; color: var(--text-muted); min-width: 60px; text-align: right; }
.save-status.saving { color: var(--primary); }
.save-status.saved { color: #4CAF50; }
.header-meta { display: flex; align-items: center; gap: 6px; margin-top: 8px; font-size: 0.75rem; color: var(--text-muted); }
.meta-category, .meta-time { color: var(--text-muted); }

.editor-body { grid-column: 1; min-height: 500px; padding: 0; overflow: hidden; }
.toolbar { display: flex; align-items: center; gap: 2px; padding: 8px 12px; border-bottom: 1px solid var(--border); background: var(--bg-secondary, #f8f9fa); flex-wrap: wrap; }
.toolbar-group { display: flex; align-items: center; gap: 2px; }
.tool-btn { width: 32px; height: 32px; border: none; background: transparent; border-radius: 6px; cursor: pointer; font-size: 0.875rem; color: var(--text-secondary); display: flex; align-items: center; justify-content: center; transition: background 0.2s; flex-shrink: 0; }
.tool-btn:hover:not(:disabled) { background: var(--border); color: var(--text-primary); }
.tool-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.tool-btn b { font-size: 0.875rem; }
.tool-btn i { font-size: 0.875rem; }
.tool-btn s { font-size: 0.75rem; }
.tool-btn code { font-size: 0.625rem; }
.toolbar-divider { width: 1px; height: 20px; background: var(--border); margin: 0 4px; flex-shrink: 0; }
.h-icon { font-size: 0.75rem; font-weight: 700; }
.font-size-select { width: auto; height: 28px; padding: 0 8px; border: 1px solid var(--border); border-radius: 6px; background: transparent; font-size: 0.75rem; color: var(--text-secondary); cursor: pointer; }
.font-size-select:focus { outline: none; border-color: var(--primary); }
.upload-progress { position: relative; width: 100px; height: 20px; background: var(--border); border-radius: 4px; overflow: hidden; margin-left: 8px; flex-shrink: 0; }
.upload-progress-bar { position: absolute; left: 0; top: 0; height: 100%; background: var(--primary); transition: width 0.3s; }
.upload-progress-text { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 0.625rem; color: #fff; text-shadow: 0 1px 2px rgba(0,0,0,0.3); }
.content-input { width: 100%; min-height: 460px; padding: 20px; border: none; font-size: 1rem; line-height: 1.8; color: var(--text-primary); background: transparent; resize: vertical; }
.content-input:focus { outline: none; }
.editor-preview { padding: 20px; min-height: 460px; }

.editor-sidebar { grid-column: 2; display: flex; flex-direction: column; gap: 16px; }
.sidebar-section { padding: 16px; }
.sidebar-title { font-size: 0.8125rem; font-weight: 600; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid var(--border); }
.form-group { margin-bottom: 0; }
.form-label { display: block; font-size: 0.75rem; font-weight: 500; color: var(--text-secondary); margin-bottom: 6px; }
.summary-input { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: 8px; font-size: 0.875rem; color: var(--text-primary); background: transparent; resize: vertical; line-height: 1.5; }
.summary-input:focus { outline: none; border-color: var(--primary); }
.char-hint { display: block; text-align: right; font-size: 0.6875rem; color: var(--text-muted); margin-top: 4px; }
.form-input { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: 8px; font-size: 0.875rem; background: transparent; color: var(--text-primary); }
.form-input:focus { outline: none; border-color: var(--primary); }

.cover-upload { margin-top: 4px; }
.cover-preview { position: relative; display: inline-block; width: 100%; }
.cover-preview img { width: 100%; max-height: 160px; object-fit: cover; border-radius: 8px; }
.remove-cover { position: absolute; top: -8px; right: -8px; width: 24px; height: 24px; background: #ef4444; color: #fff; border: none; border-radius: 50%; cursor: pointer; font-size: 0.875rem; display: flex; align-items: center; justify-content: center; }
.upload-btn { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 24px; background: var(--bg-secondary, #f8f9fa); border: 2px dashed var(--border); border-radius: 8px; color: var(--text-muted); cursor: pointer; transition: all 0.2s; width: 100%; }
.upload-btn:hover { border-color: var(--primary); color: var(--primary); }

.tag-input-container { display: flex; flex-wrap: wrap; gap: 6px; padding: 8px; border: 1px solid var(--border); border-radius: 8px; min-height: 40px; }
.selected-tags { display: flex; flex-wrap: wrap; gap: 4px; }
.tag-badge { display: flex; align-items: center; gap: 3px; padding: 2px 8px; background: var(--primary-light, #e8f0fe); color: var(--primary, #1a73e8); border-radius: 6px; font-size: 0.75rem; }
.remove-tag { background: none; border: none; color: inherit; cursor: pointer; font-size: 0.875rem; padding: 0; line-height: 1; opacity: 0.7; }
.remove-tag:hover { opacity: 1; }
.tag-input { flex: 1; min-width: 80px; border: none; font-size: 0.8125rem; background: transparent; color: var(--text-primary); }
.tag-input:focus { outline: none; }
.suggested-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 8px; }
.suggested-tag { padding: 2px 10px; font-size: 0.75rem; background: var(--bg-secondary, #f8f9fa); color: var(--text-secondary); border: 1px solid var(--border); border-radius: 12px; cursor: pointer; transition: all 0.2s; }
.suggested-tag:hover { border-color: var(--primary); color: var(--primary); }
.no-tags { margin-top: 8px; }
.btn-text { background: none; border: none; color: var(--primary); cursor: pointer; font-size: 0.75rem; padding: 4px 0; }
.btn-text:hover { text-decoration: underline; }
.btn-xs { font-size: 0.75rem; }

.empty-preview { display: flex; align-items: center; justify-content: center; height: 300px; color: var(--text-muted); }

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-muted);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }

@media (max-width: 900px) {
  .edit-container { grid-template-columns: 1fr; }
  .editor-sidebar { grid-column: 1; }
  .header-top { flex-direction: column; align-items: stretch; }
  .header-actions { justify-content: flex-end; }
  .post-edit-page { padding: 12px; }
}

@media (max-width: 768px) {
  .editor-body {
    min-height: 300px;
  }

  .content-input {
    min-height: 300px;
    padding: 16px;
    font-size: 0.9375rem;
  }

  .toolbar {
    overflow-x: auto;
    flex-wrap: nowrap;
    -webkit-overflow-scrolling: touch;
  }
}
</style>
