<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  images: { type: Array, default: () => [] },
  initialIndex: { type: Number, default: 0 },
  show: { type: Boolean, default: false }
})
const emit = defineEmits(['close'])
const currentIndex = ref(props.initialIndex)

watch(() => props.show, (val) => {
  if (val) {
    currentIndex.value = props.initialIndex
    document.addEventListener('keydown', handleKeydown)
  } else {
    document.removeEventListener('keydown', handleKeydown)
  }
})

const prev = () => { if (currentIndex.value > 0) currentIndex.value-- }
const next = () => { if (currentIndex.value < props.images.length - 1) currentIndex.value++ }

const handleKeydown = (e) => {
  if (e.key === 'Escape') emit('close')
  if (e.key === 'ArrowLeft') prev()
  if (e.key === 'ArrowRight') next()
}

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})


</script>

<template>
  <Teleport to="body">
    <Transition name="preview">
      <div v-if="show" class="preview-overlay" role="dialog" aria-modal="true" @click.self="emit('close')" tabindex="0">
        <button class="preview-close" aria-label="关闭" @click="emit('close')">&times;</button>
        <button v-if="images.length > 1" class="preview-nav prev" aria-label="上一张" @click="prev">&#8249;</button>
        <img :src="images[currentIndex]" class="preview-img" @click.stop />
        <button v-if="images.length > 1" class="preview-nav next" aria-label="下一张" @click="next">&#8250;</button>
        <div class="preview-counter">{{ currentIndex + 1 }} / {{ images.length }}</div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.preview-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.9);
  z-index: 20000;
  cursor: zoom-out;
}

.preview-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: white;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  z-index: 1;
  transition: background var(--transition);
}

.preview-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.preview-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  color: white;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  z-index: 1;
  transition: background var(--transition);
}

.preview-nav:hover {
  background: rgba(255, 255, 255, 0.3);
}

.preview-nav.prev {
  left: 20px;
}

.preview-nav.next {
  right: 20px;
}

.preview-img {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  border-radius: var(--radius);
  cursor: default;
}

.preview-counter {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 6px 16px;
  font-size: 0.875rem;
  color: white;
  background: rgba(0, 0, 0, 0.5);
  border-radius: var(--radius-full);
}

/* 过渡动画 */
.preview-enter-active {
  transition: opacity 0.3s ease;
}
.preview-leave-active {
  transition: opacity 0.3s ease;
}
.preview-enter-from {
  opacity: 0;
}
.preview-leave-to {
  opacity: 0;
}
</style>
