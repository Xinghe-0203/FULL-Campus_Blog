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
.page-enter-active,
.page-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
