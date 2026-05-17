<template>
  <transition name="page" mode="out-in" @before-leave="handleBeforeLeave" @after-enter="handleAfterEnter">
    <slot />
  </transition>
</template>

<script setup>
const emit = defineEmits(['before-leave', 'after-enter'])

function handleBeforeLeave() {
  emit('before-leave')
}

function handleAfterEnter() {
  emit('after-enter')
}
</script>

<style>
.page-enter-active {
  transition: opacity var(--duration-normal) ease, transform var(--duration-normal) cubic-bezier(0.34, 1.56, 0.64, 1);
}

.page-leave-active {
  transition: opacity var(--duration-fast) ease, transform var(--duration-fast) ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.99);
}
</style>
