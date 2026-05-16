<template>
  <div
    class="file-uploader"
    :class="{ 'is-dragover': isDragOver, 'is-disabled': disabled }"
    @dragenter.prevent="onDragEnter"
    @dragover.prevent="onDragOver"
    @dragleave.prevent="onDragLeave"
    @drop.prevent="onDrop"
  >
    <input
      ref="fileInput"
      type="file"
      :accept="accept"
      :multiple="multiple"
      class="file-input-hidden"
      @change="onFileChange"
    />
    <div class="upload-zone" @click="triggerFileInput">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
        <polyline points="17 8 12 3 7 8"/>
        <line x1="12" y1="3" x2="12" y2="15"/>
      </svg>
      <p class="upload-text">{{ dragText }}</p>
      <p v-if="hint" class="upload-hint">{{ hint }}</p>
    </div>
    <div v-if="files.length" class="file-list">
      <div v-for="(file, index) in files" :key="index" class="file-item">
        <img v-if="isImage(file)" :src="file.url || file.preview" class="file-preview" />
        <div v-else class="file-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        </div>
        <div class="file-info">
          <span class="file-name">{{ file.name }}</span>
          <span class="file-size">{{ formatSize(file.size) }}</span>
        </div>
        <button class="file-remove" @click="removeFile(index)" :disabled="disabled">&times;</button>
      </div>
    </div>
    <div v-if="error" class="upload-error">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  accept: { type: String, default: 'image/*' },
  maxSize: { type: Number, default: 5 * 1024 * 1024 },
  multiple: { type: Boolean, default: false },
  maxCount: { type: Number, default: 1 },
  disabled: { type: Boolean, default: false },
  hint: { type: String, default: '' }
})
const emit = defineEmits(['files-change', 'file-remove'])

const fileInput = ref(null)
const isDragOver = ref(false)
const files = ref([])
const error = ref('')

const isImage = (file) => file.type?.startsWith('image/')

const dragText = computed(() => {
  if (isDragOver.value) return '释放以上传文件'
  return `点击或拖拽文件到此处${props.multiple ? '' : '（支持单个文件）'}`
})

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / 1024 / 1024).toFixed(1) + 'MB'
}

function triggerFileInput() {
  if (!props.disabled) fileInput.value?.click()
}

function validateFiles(rawFiles) {
  const valid = []
  for (const f of rawFiles) {
    if (props.maxSize && f.size > props.maxSize) {
      error.value = `文件"${f.name}"超过大小限制（${formatSize(props.maxSize)}）`
      return null
    }
    valid.push(f)
  }
  return valid
}

function addFiles(rawFiles) {
  error.value = ''
  const valid = validateFiles(rawFiles)
  if (!valid) return

  let newFiles = [...files.value, ...valid.map(f => {
    if (f.type?.startsWith('image/')) {
      try { return Object.assign(f, { preview: URL.createObjectURL(f) }) } catch { return f }
    }
    return f
  })]
  if (!props.multiple) {
    newFiles = [newFiles[newFiles.length - 1]]
  } else if (props.maxCount && newFiles.length > props.maxCount) {
    error.value = `最多上传 ${props.maxCount} 个文件`
    newFiles = newFiles.slice(0, props.maxCount)
  }

  files.value = newFiles
  emit('files-change', [...files.value])
}

function onFileChange(e) {
  if (e.target.files?.length) {
    addFiles(Array.from(e.target.files))
  }
  if (fileInput.value) fileInput.value.value = ''
}

function onDragEnter() { isDragOver.value = true }
function onDragOver() { isDragOver.value = true }
function onDragLeave() { isDragOver.value = false }
function onDrop(e) {
  isDragOver.value = false
  if (e.dataTransfer.files?.length) {
    addFiles(Array.from(e.dataTransfer.files))
  }
}

function removeFile(index) {
  const file = files.value[index]
  if (file?.preview) URL.revokeObjectURL(file.preview)
  files.value.splice(index, 1)
  error.value = ''
  emit('file-remove', index)
  emit('files-change', [...files.value])
}

defineExpose({ files, reset: () => { files.value = []; error.value = '' } })
</script>

<style scoped>
.file-uploader {
  width: 100%;
}

.file-input-hidden {
  display: none;
}

.upload-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 32px 24px;
  border: 2px dashed var(--border);
  border-radius: var(--radius);
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition);
  background: var(--background);
}

.upload-zone:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-light);
}

.is-dragover .upload-zone {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-light);
}

.is-disabled {
  opacity: 0.6;
  pointer-events: none;
}

.upload-text {
  font-size: 0.9rem;
}

.upload-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.file-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}

.file-preview {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  flex-shrink: 0;
}

.file-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-size: 0.8125rem;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.file-remove {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: var(--radius);
  transition: all var(--transition);
}

.file-remove:hover {
  background: rgba(239, 68, 68, 0.1);
  color: var(--error);
}

.upload-error {
  margin-top: 8px;
  font-size: 0.8125rem;
  color: var(--error);
}
</style>
