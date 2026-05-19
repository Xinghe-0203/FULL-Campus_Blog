/**
 * 图片预览组合式函数
 */
import { ref } from 'vue'

export function useImagePreview() {
  const previewShow = ref(false)
  const previewImages = ref([])
  const previewIndex = ref(0)

  const openPreview = (images, index = 0) => {
    previewImages.value = images
    previewIndex.value = index
    previewShow.value = true
  }

  const closePreview = () => {
    previewShow.value = false
  }

  const previewPrev = () => {
    previewIndex.value = (previewIndex.value - 1 + previewImages.value.length) % previewImages.value.length
  }

  const previewNext = () => {
    previewIndex.value = (previewIndex.value + 1) % previewImages.value.length
  }

  return {
    previewShow,
    previewImages,
    previewIndex,
    openPreview,
    closePreview,
    previewPrev,
    previewNext
  }
}