<template>
  <div class="empty-state">
    <svg v-if="!icon" class="empty-state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
      <circle cx="12" cy="7" r="4"/>
    </svg>
    <img v-else :src="icon" class="empty-state-icon" alt="" />
    <h3 class="empty-state-title">{{ title }}</h3>
    <p v-if="message" class="empty-state-text">{{ message }}</p>
    <button v-if="actionText" class="btn btn-primary" style="margin-top: 16px" @click="$emit('action')">
      {{ actionText }}
    </button>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '' },
  title: { type: String, default: '暂无数据' },
  message: { type: String, default: '' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>
