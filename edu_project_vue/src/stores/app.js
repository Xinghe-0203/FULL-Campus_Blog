import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const globalLoading = ref(false)
  const onlineStatus = ref(navigator.onLine)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setGlobalLoading(val) {
    globalLoading.value = val
  }

  function setOnlineStatus(val) {
    onlineStatus.value = val
  }

  return {
    sidebarCollapsed,
    globalLoading,
    onlineStatus,
    toggleSidebar,
    setGlobalLoading,
    setOnlineStatus
  }
})
