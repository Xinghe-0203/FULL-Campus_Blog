<script setup>
defineProps({
  type: {
    type: String,
    default: 'post-card',
    validator: v => ['post-card', 'circle-item', 'user-card', 'detail', 'comment', 'profile'].includes(v)
  },
  count: { type: Number, default: 1, validator: v => v >= 0 && Number.isInteger(v) }
})
</script>

<template>
  <div v-for="i in count" :key="i" class="skeleton-wrapper" :class="type">
    <template v-if="type === 'post-card'">
      <div class="sk-img skeleton" />
      <div class="sk-body">
        <div class="sk-line w-70" />
        <div class="sk-line w-100" />
        <div class="sk-line w-40" />
        <div class="sk-tags">
          <span class="sk-tag skeleton" />
          <span class="sk-tag skeleton" />
        </div>
      </div>
    </template>

    <template v-else-if="type === 'circle-item'">
      <div class="sk-row">
        <div class="sk-avatar skeleton" />
        <div class="sk-body">
          <div class="sk-line w-60" />
          <div class="sk-line w-90" />
        </div>
      </div>
      <div class="sk-img skeleton" />
    </template>

    <template v-else-if="type === 'user-card'">
      <div class="sk-row">
        <div class="sk-avatar skeleton" />
        <div class="sk-body">
          <div class="sk-line w-50" />
          <div class="sk-line w-80" />
        </div>
      </div>
    </template>

    <template v-else-if="type === 'detail'">
      <div class="sk-line w-60 skeleton" style="height: 32px; margin-bottom: 16px" />
      <div class="sk-line w-30 skeleton" style="height: 14px; margin-bottom: 24px" />
      <div class="sk-img skeleton" style="height: 300px" />
      <div v-for="j in 5" :key="j" class="sk-line w-100 skeleton" />
    </template>

    <template v-else-if="type === 'comment'">
      <div class="sk-row">
        <div class="sk-avatar skeleton" style="width: 36px; height: 36px" />
        <div class="sk-body">
          <div class="sk-line w-40" style="height: 12px" />
          <div class="sk-line w-100" />
          <div class="sk-line w-60" />
        </div>
      </div>
    </template>

    <template v-else-if="type === 'profile'">
      <div class="sk-profile-header">
        <div class="sk-avatar skeleton" style="width: 80px; height: 80px" />
        <div class="sk-line w-40" style="height: 24px" />
        <div class="sk-line w-60" style="height: 14px" />
      </div>
      <div class="sk-tabs">
        <div v-for="j in 3" :key="j" class="sk-tab skeleton" />
      </div>
    </template>
  </div>
</template>

<style scoped>
.skeleton-wrapper {
  padding: 16px;
  background: var(--surface);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}

.sk-img {
  width: 100%;
  height: 180px;
  border-radius: var(--radius);
  margin-bottom: 12px;
}

.sk-body {
  flex: 1;
}

.sk-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.sk-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  flex-shrink: 0;
}

.sk-line {
  height: 14px;
  margin-bottom: 10px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
}

.w-100 { width: 100% }
.w-90 { width: 90% }
.w-80 { width: 80% }
.w-70 { width: 70% }
.w-60 { width: 60% }
.w-50 { width: 50% }
.w-40 { width: 40% }
.w-30 { width: 30% }

.sk-tags {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.sk-tag {
  width: 60px;
  height: 24px;
  border-radius: var(--radius-full);
}

.sk-tabs {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.sk-tab {
  width: 80px;
  height: 32px;
  border-radius: var(--radius);
}

.sk-profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 0;
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
