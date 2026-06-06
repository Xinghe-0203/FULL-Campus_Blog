<template>
  <div class="circle-page">
    <div class="circle-layout">
      <!-- 左侧边栏 - 热门话题 -->
      <aside class="sidebar-left hide-mobile">
        <div class="sidebar-card glass sidebar-card-top">
          <h3 class="sidebar-card-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 9h16M4 15h16M10 3l-2 6M14 15l-2 6M14 3l2 6M10 15l-2 6"/></svg>
            热门话题
          </h3>
          <div v-if="hotTopicsLoading" class="sidebar-loading">
            <span class="spinner-small"></span> 加载中...
          </div>
          <div v-else-if="hotTopics.length" class="sidebar-topic-list">
            <router-link v-for="topic in hotTopics" :key="topic.id" :to="`/topic/${topic.id}`" class="sidebar-topic-item">
              <span class="sidebar-topic-name">#{{ topic.name }}</span>
              <span class="sidebar-topic-count">{{ topic.postCount || 0 }} 篇</span>
            </router-link>
          </div>
          <div v-else class="sidebar-empty">暂无热门话题</div>
        </div>
        <!-- 左侧边栏 - 热门动态 -->
        <div class="sidebar-card glass">
          <h3 class="sidebar-card-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
            热门动态
          </h3>
          <div v-if="hotPostsLoading" class="sidebar-loading">
            <span class="spinner-small"></span> 加载中...
          </div>
          <div v-else-if="hotPosts.length" class="sidebar-post-list">
            <router-link v-for="hp in hotPosts" :key="hp.id" :to="`/circle/${hp.id}`" class="sidebar-post-item">
              <div class="sidebar-post-author">
                <img :src="hp.userAvatar || '/default-avatar.png'" :alt="hp.userNickname" class="sidebar-avatar" />
                <span class="sidebar-post-name">{{ hp.userNickname || hp.userUsername }}</span>
              </div>
              <p class="sidebar-post-text">{{ hp.content?.substring(0, 50) }}{{ hp.content?.length > 50 ? '...' : '' }}</p>
              <div class="sidebar-post-stats">
                <span>{{ formatNumber(hp.likeCount || 0) }} 赞</span>
                <span>{{ formatNumber(hp.commentCount || 0) }} 评论</span>
              </div>
            </router-link>
          </div>
          <div v-else class="sidebar-empty">暂无热门动态</div>
        </div>
      </aside>

      <!-- 主内容区 -->
      <div class="circle-main">
        <div class="circle-container">
          <button class="back-btn glass" @click="router.back()">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
            返回
          </button>

          <div class="create-post glass" v-if="userStore.isLoggedIn" @click="openCreateModal">
            <div class="create-header">
              <img :src="userStore.avatar || '/default-avatar.png'" :alt="userStore.nickname" class="user-avatar" />
              <div class="create-input">分享你的校园生活...</div>
            </div>
          </div>

      <div class="feed-tabs glass">
        <button class="tab-btn" :class="{ active: activeTab === 'recommend' }" @click="switchTab('recommend')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
          推荐
        </button>
        <button v-if="userStore.isLoggedIn" class="tab-btn" :class="{ active: activeTab === 'following' }" @click="switchTab('following')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
          关注
        </button>
      </div>

      <div class="feed-list" ref="feedListRef">
        <div v-if="loading && posts.length === 0" class="loading-container">
          <div v-for="i in 3" :key="i" class="feed-skeleton glass">
            <div class="skeleton-header">
              <div class="skeleton-avatar"></div>
              <div class="skeleton-info">
                <div class="skeleton-name"></div>
                <div class="skeleton-time"></div>
              </div>
            </div>
            <div class="skeleton-content">
              <div class="skeleton-text"></div>
              <div class="skeleton-text short"></div>
              <div class="skeleton-images">
                <div class="skeleton-img"></div>
                <div class="skeleton-img"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="error" class="error-state glass">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <h3>加载失败</h3>
          <p>{{ error }}</p>
          <button class="btn btn-primary" @click="retryLoad">重新加载</button>
        </div>

        <div v-else-if="posts.length === 0" class="empty-state glass">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <h3 class="empty-state-title">{{ activeTab === 'following' ? '还没有关注的内容' : '还没有动态' }}</h3>
          <p class="empty-state-text">{{ activeTab === 'following' ? '去发现更多有趣的人吧' : '快来说点什么吧' }}</p>
        </div>

        <div v-else class="posts">
          <div v-for="(post, index) in posts" :key="post.id" class="feed-item glass" :style="{ animationDelay: `${index * 0.05}s` }">
            <div class="feed-header">
              <router-link :to="`/user/${post.userId}`" class="feed-author">
                <img :src="post.userAvatar || '/default-avatar.png'" :alt="post.userUsername" class="author-avatar" />
                <div class="author-info">
                  <span class="author-name">{{ post.userNickname || post.userUsername }}</span>
                  <span class="feed-time">{{ formatRelativeTime(post.createTime) }}</span>
                </div>
              </router-link>
              <span class="visibility-badge glass-badge" :title="visibilityLabel(post.visibility)">
                <svg v-if="post.visibility === 0" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
                <svg v-else-if="post.visibility === 1" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><path d="M20 8v6"/><path d="M23 11h-6"/></svg>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              </span>
              <div v-if="userStore.isLoggedIn && post.userId === userStore.userId" class="post-owner-actions">
                <button class="action-icon-btn" title="编辑" @click.stop="router.push(`/circle/post/edit/${post.id}`)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"/></svg>
                </button>
                <button class="action-icon-btn" title="删除" @click.stop="deletePost(post)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                </button>
              </div>
            </div>

            <div class="feed-content" @click="router.push(`/circle/${post.id}`)">
              <p class="feed-text">{{ post.content }}</p>

              <div v-if="post.topicNames && post.topicNames.length" class="topic-tags">
                <router-link v-for="(tn, idx) in post.topicNames" :key="tn" :to="`/topic/${post.topicIds?.[idx] || ''}`" class="topic-tag-link glass-chip">#{{ tn }}</router-link>
              </div>

              <div v-if="post.location" class="location-display">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                <span>{{ post.location }}</span>
              </div>

              <div v-if="post.images && post.images.length" class="feed-images" :class="getImageGridClass(post.images.length)">
                <div v-for="(image, idx) in post.images" :key="idx" class="img-wrap" :class="{ 'is-video': isVideo(image) }" @click.stop="openImagePreview(post.images, Number(idx))">
                  <img v-if="!isVideo(image)" :src="image" alt="动态图片" class="feed-image" loading="lazy" />
                  <video v-else :src="image" class="feed-image" muted @click.stop.prevent="playVideo(image)"></video>
                  <span v-if="isVideo(image)" class="play-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
                  </span>
                </div>
              </div>

              <div v-if="post.videos && post.videos.length" class="feed-videos">
                <video v-for="(video, idx) in post.videos" :key="'v-'+idx" :src="video" class="feed-video" controls muted preload="metadata"></video>
              </div>

              <div v-if="post.repostPost" class="repost-card glass-inner" @click.stop="router.push(`/circle/${post.id}`)">
                <div class="repost-header">
                  <img :src="post.repostPost.userAvatar || '/default-avatar.png'" class="repost-avatar" />
                  <span class="repost-author">{{ post.repostPost.userNickname || post.repostPost.userUsername }}</span>
                </div>
                <p v-if="post.repostContent" class="repost-user-content">{{ post.repostContent }}</p>
                <p class="repost-text">{{ post.repostPost.content }}</p>
                <div v-if="post.repostPost.images && post.repostPost.images.length" class="repost-images mini">
                  <img v-for="(img, idx) in post.repostPost.images.slice(0, 3)" :key="idx" :src="img" class="repost-img" />
                  <span v-if="post.repostPost.images.length > 3" class="repost-more">+{{ post.repostPost.images.length - 3 }}</span>
                </div>
              </div>
              <div v-else-if="post.originalPostHidden" class="repost-hidden-notice">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                <span>原内容已隐藏</span>
              </div>
            </div>

            <div class="feed-actions">
              <button class="action-btn" :class="{ liked: post.isLiked }" @click="toggleLike(post)">
                <svg class="like-icon" :class="{ 'animate-pop': post.likeAnim }" width="18" height="18" viewBox="0 0 24 24" :fill="post.isLiked ? 'var(--accent)' : 'none'" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <span>{{ formatNumber(post.likeCount) }}</span>
              </button>
              <button class="action-btn" @click="router.push(`/circle/${post.id}`)">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span>{{ formatNumber(post.commentCount) }}</span>
              </button>
              <button class="action-btn" @click="openRepostModal(post)">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/>
                </svg>
                <span>{{ formatNumber(post.repostCount) }}</span>
              </button>
              <span class="view-count">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                </svg>
                {{ formatNumber(post.viewCount || 0) }}
              </span>
            </div>
          </div>

          <div ref="sentinelRef" class="scroll-sentinel">
            <div v-if="loadingMore" class="loading-spinner">
              <div class="spinner"></div>
              <span>加载更多...</span>
            </div>
            <div v-else-if="!hasMore" class="no-more">没有更多了</div>
</div>
        </div>
      </div>
    </div>
    </div>
  </div>

  <teleport to="body">
      <transition name="modal">
        <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
          <div class="modal-content glass">
            <div class="modal-header">
              <h3>发布动态</h3>
              <button class="close-btn" @click="showCreateModal = false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="create-top">
                <img :src="userStore.avatar || '/default-avatar.png'" class="modal-avatar" />
                <div class="create-info">
                  <span class="create-nickname">{{ userStore.nickname }}</span>
                  <button class="visibility-selector glass-chip" @click="showVisibilityPicker = !showVisibilityPicker">
                    <svg v-if="newPost.visibility === 0" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
                    <svg v-else-if="newPost.visibility === 1" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><path d="M20 8v6"/><path d="M23 11h-6"/></svg>
                    <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                    {{ visibilityOptions[newPost.visibility] }}
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
                  </button>
                  <transition name="dropdown">
                    <div v-if="showVisibilityPicker" class="visibility-dropdown glass">
                      <div v-for="(label, key) in visibilityOptions" :key="key" class="vis-item" :class="{ active: newPost.visibility === Number(key) }" @click="newPost.visibility = Number(key); showVisibilityPicker = false">
                        <span class="vis-icon">
                          <svg v-if="Number(key) === 0" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
                          <svg v-else-if="Number(key) === 1" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><path d="M20 8v6"/><path d="M23 11h-6"/></svg>
                          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                        </span>
                        <div><div class="vis-label">{{ label }}</div><div class="vis-desc">{{ visibilityDescriptions[key] }}</div></div>
                      </div>
                    </div>
                  </transition>
                </div>
              </div>
              <textarea v-model="newPost.content" class="post-textarea" placeholder="分享你的校园生活..." rows="4" @input="autoResize" ref="textareaRef" maxlength="2000"></textarea>
              <div class="char-count" :class="{ warn: newPost.content.length > 1800 }">{{ newPost.content.length }}/2000</div>

              <div class="topic-selector">
                <div v-if="selectedTopic" class="selected-topic">
                  <span class="topic-badge glass-chip">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" y1="22" x2="4" y2="15"/></svg>
                    {{ selectedTopic.name }}
                  </span>
                  <button class="remove-topic" @click="removeTopic" title="移除话题">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  </button>
                </div>
                <div v-else class="topic-input-wrapper">
                  <div class="topic-search-box glass">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                    <input v-model="topicSearch" placeholder="添加话题..." @focus="showTopicDropdown = true" @blur="hideTopicDropdown" />
                  </div>
                  <transition name="dropdown">
                    <div v-if="showTopicDropdown" class="topic-dropdown glass">
                      <div v-if="topicsLoading" class="topic-loading">
                        <span class="spinner-small"></span> 加载中...
                      </div>
                      <div v-else-if="filteredTopics.length">
                        <div v-for="topic in filteredTopics" :key="topic.id" class="topic-dropdown-item" @mousedown.prevent="selectTopic(topic)">
                          <span class="topic-name">#{{ topic.name }}</span>
                          <span class="topic-count">{{ topic.postCount || 0 }} 篇</span>
                        </div>
                        <div class="topic-create-divider"></div>
                        <div class="topic-dropdown-item topic-create-item" @mousedown.prevent="openCreateTopicModal">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                          <span class="topic-create-text">创建话题 "{{ topicSearch || '新话题' }}"</span>
                        </div>
                      </div>
                      <div v-else class="topic-create-item" @mousedown.prevent="openCreateTopicModal">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                        <span class="topic-create-text">创建话题 "{{ topicSearch || '新话题' }}"</span>
                      </div>
                    </div>
                  </transition>
                </div>
              </div>

              <div v-if="newPost.images.length" class="uploaded-images">
                <div v-for="(img, idx) in newPost.images" :key="idx" class="image-item">
                  <img :src="img" alt="" />
                  <button class="remove-image" @click="newPost.images.splice(idx, 1)">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  </button>
                </div>
              </div>

              <div v-if="newPost.videos.length" class="uploaded-videos">
                <div v-for="(video, idx) in newPost.videos" :key="'v-'+idx" class="video-item">
                  <video :src="video" class="video-preview" muted preload="metadata"></video>
                  <button class="remove-video" @click="newPost.videos.splice(idx, 1)">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  </button>
                </div>
              </div>

              <div class="toolbar">
                <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple @change="handleImageUpload" ref="imageInput" hidden />
                <input type="file" accept="video/mp4,video/webm" @change="handleVideoUpload" ref="videoInput" hidden />
                <button class="tool-btn glass-chip" title="图片" @click="(imageInput as HTMLInputElement)?.click()" :disabled="newPost.images.length >= 9">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                  <span>图片</span>
                </button>
                <button class="tool-btn glass-chip" title="视频" @click="(videoInput as HTMLInputElement)?.click()">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2"/></svg>
                  <span>视频</span>
                </button>
                <div class="tool-right">
                  <button class="btn btn-primary btn-sm" @click="publishPost" :disabled="!newPost.content.trim() || publishing">
                    {{ publishing ? '发布中...' : '发布' }}
                  </button>
                </div>
              </div>
              <div v-if="uploading" class="upload-progress-bar">
                <div class="progress-fill" :style="{ width: uploadPercent + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showRepostModal" class="modal-overlay" @click.self="closeRepostModal">
          <div class="modal-content glass">
            <div class="modal-header">
              <h3>转发动态</h3>
              <button class="close-btn" @click="closeRepostModal">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="modal-body">
              <div v-if="currentRepostPost" class="repost-original-card glass-inner">
                <div class="repost-original-header">
                  <img :src="currentRepostPost.userAvatar || '/default-avatar.png'" class="repost-original-avatar" />
                  <span class="repost-original-name">{{ currentRepostPost.userNickname || currentRepostPost.userUsername }}</span>
                </div>
                <p class="repost-original-text">{{ currentRepostPost.content }}</p>
              </div>
              <textarea v-model="repostContent" class="post-textarea" placeholder="说说你的看法..." rows="4" maxlength="2000"></textarea>
              <div class="char-count" :class="{ warn: repostContent.length > 1800 }">{{ repostContent.length }}/2000</div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="closeRepostModal">取消</button>
              <button class="btn btn-primary" @click="confirmRepost" :disabled="reposting">
                {{ reposting ? '转发中...' : '转发' }}
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showCreateTopicModal" class="modal-overlay" @click.self="showCreateTopicModal = false">
          <div class="modal-content glass create-topic-modal">
            <div class="modal-header">
              <h3>创建新话题</h3>
              <button class="close-btn" @click="showCreateTopicModal = false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label class="form-label">话题名称</label>
                <input v-model="newTopic.name" class="form-input" placeholder="请输入话题名称" maxlength="20" />
                <div class="char-count">{{ newTopic.name.length }}/20</div>
              </div>
              <div class="form-group">
                <label class="form-label">话题描述（可选）</label>
                <textarea v-model="newTopic.description" class="form-textarea" placeholder="简单描述一下这个话题..." rows="3" maxlength="200"></textarea>
                <div class="char-count">{{ newTopic.description.length }}/200</div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showCreateTopicModal = false">取消</button>
              <button class="btn btn-primary" @click="confirmCreateTopic" :disabled="!newTopic.name.trim() || creatingTopic">
                {{ creatingTopic ? '创建中...' : '创建' }}
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showPreview" class="modal-overlay preview-overlay" @click.self="showPreview = false">
          <button class="preview-nav prev" @click="previewPrev" v-if="previewImages.length > 1">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
          </button>
          <img :src="previewImages[previewIndex]" class="preview-image" @click.stop />
          <button class="preview-nav next" @click="previewNext" v-if="previewImages.length > 1">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
          </button>
          <button class="preview-close" @click="showPreview = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
          <div class="preview-counter" v-if="previewImages.length > 1">{{ previewIndex + 1 }} / {{ previewImages.length }}</div>
        </div>
      </transition>
    </teleport>
    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { circleApi } from '../../api/circle'
import { useConfirm } from '../../composables/useConfirm'
import { topicApi } from '../../api/topic'
import { mediaApi } from '../../api/media'
import { useUserStore } from '../../stores/user'
import { formatRelativeTime, formatNumber, debounce } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('Circle')

const loading = ref(false)
const loadingMore = ref(false)
const posts = ref<any[]>([])
const activeTab = ref('recommend')
const currentPage = ref(1)
const hasMore = ref(true)
const error = ref('')

const showCreateModal = ref(false)
const publishing = ref(false)
const uploading = ref(false)
const uploadPercent = ref(0)
const showVisibilityPicker = ref(false)

const showRepostModal = ref(false)
const repostContent = ref('')
const currentRepostPost = ref<any>(null)
const reposting = ref(false)

const showPreview = ref(false)
const previewImages = ref<string[]>([])
const previewIndex = ref(0)

const sentinelRef = ref<HTMLElement | null>(null)
const feedListRef = ref<HTMLElement | null>(null) // used in template ref
void feedListRef.value
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const imageInput = ref<HTMLInputElement | null>(null)
const videoInput = ref<HTMLInputElement | null>(null)

const visibilityOptions: Record<number, string> = { 0: '公开', 1: '关注者可见', 2: '仅自己' }
const visibilityDescriptions: Record<number, string> = { 0: '所有人可见', 1: '仅关注的粉丝可见', 2: '只有自己可见' }

const newPost = reactive({
  content: '',
  images: [] as string[],
  videos: [] as string[],
  visibility: 0
})

const topicSearch = ref('')
const showTopicDropdown = ref(false)
const selectedTopic = ref<any>(null)
const allTopics = ref<any[]>([])
const topicsLoading = ref(false)

const showCreateTopicModal = ref(false)
const creatingTopic = ref(false)
const newTopic = reactive({
  name: '',
  description: ''
})

const hotTopics = ref<any[]>([])
const hotTopicsLoading = ref(false)
const hotPosts = ref<any[]>([])
const hotPostsLoading = ref(false)

const debouncedSearchTopics = debounce(() => {
  showTopicDropdown.value = true
}, 300)
void debouncedSearchTopics

const filteredTopics = computed(() => {
  if (!topicSearch.value) return allTopics.value
  const q = topicSearch.value.toLowerCase()
  return allTopics.value.filter(t => t.name.toLowerCase().includes(q))
})

const selectTopic = (topic: any) => {
  selectedTopic.value = topic
  topicSearch.value = ''
  showTopicDropdown.value = false
}

const removeTopic = () => {
  selectedTopic.value = null
  topicSearch.value = ''
}

const hideTopicDropdown = () => {
  setTimeout(() => { showTopicDropdown.value = false }, 200)
}

const openCreateTopicModal = () => {
  showTopicDropdown.value = false
  newTopic.name = topicSearch.value || ''
  newTopic.description = ''
  showCreateTopicModal.value = true
}

const confirmCreateTopic = async () => {
  if (!newTopic.name.trim()) return
  creatingTopic.value = true
  try {
    const res = await topicApi.createTopic({
      name: newTopic.name.trim(),
      description: newTopic.description.trim() || undefined
    })
    const topicId = res.data
    const createdTopic = {
      id: topicId,
      name: newTopic.name.trim(),
      description: newTopic.description.trim(),
      postCount: 0
    }
    allTopics.value.unshift(createdTopic)
    selectedTopic.value = createdTopic
    topicSearch.value = ''
    showCreateTopicModal.value = false
    toast.success('话题创建成功')
  } catch (err: any) {
    logger.error('createTopic error', { error: err.message })
    toast.error(err.response?.data?.message || '话题创建失败')
  } finally {
    creatingTopic.value = false
  }
}

const isVideo = (url: string) => {
  if (!url) return false
  const exts = ['.mp4', '.webm', '.mov', '.avi', '.mkv']
  const lower = url.toLowerCase()
  return exts.some(e => lower.includes(e)) || lower.includes('video')
}

const visibilityLabel = (v: number) => {
  if (v === 0) return '公开'
  if (v === 1) return '关注者可见'
  return '仅自己可见'
}

const getImageGridClass = (count: number) => {
  const n = Math.min(count, 9)
  if (n === 1) return 'grid-1'
  if (n === 2) return 'grid-2'
  if (n === 3) return 'grid-3'
  if (n === 4) return 'grid-4'
  return 'grid-multi'
}

const switchTab = (tab: string) => {
  activeTab.value = tab
}

const fetchPosts = async (reset = false) => {
  if (loading.value && !reset) return
  if (reset) {
    currentPage.value = 1
    hasMore.value = true
    error.value = ''
  }
  loading.value = true
  try {
    const fn = activeTab.value === 'following' ? circleApi.getFollowingFeed : circleApi.getRecommendFeed
    const res = await fn({ pageNum: currentPage.value, pageSize: 20 })
    const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    const mapped = list.map(p => ({ ...p, isLiked: p.isLiked || false, likeAnim: false }))
    posts.value = reset ? mapped : [...posts.value, ...mapped]
    hasMore.value = list.length >= 20
    return true
  } catch (err: any) {
    logger.error('fetchPosts error', { error: err.message })
    error.value = err.response?.data?.message || err.message || '加载失败'
    if (reset) posts.value = []
    return false
  } finally {
    loading.value = false
  }
}

const retryLoad = () => fetchPosts(true)

const loadMore = async () => {
  if (loadingMore.value || loading.value || !hasMore.value) return
  loadingMore.value = true
  currentPage.value++
  const ok = await fetchPosts()
  if (!ok) currentPage.value--
  loadingMore.value = false
}

watch(activeTab, () => fetchPosts(true))

let observer: IntersectionObserver | null = null
const setupObserver = () => {
  if (!sentinelRef.value) return
  observer = new IntersectionObserver((entries) => {
    if (entries[0]?.isIntersecting) loadMore()
  }, { rootMargin: '200px' })
  observer.observe(sentinelRef.value)
}

watch(() => posts.value.length, () => {
  nextTick(() => {
    if (observer) observer.disconnect()
    setupObserver()
  })
})

const { confirm, ConfirmDialog } = useConfirm()

const deletePost = async (post: any) => {
  const ok = await confirm('确定要删除这条动态吗？删除后不可恢复。')
  if (!ok) return
  try {
    await circleApi.deletePost(post.id)
    posts.value = posts.value.filter(p => p.id !== post.id)
    toast.success('删除成功')
  } catch (err: any) {
    logger.error('deletePost error', { error: err.message })
    toast.error(err.response?.data?.message || '删除失败')
  }
}

const toggleLike = async (post: any) => {
  if (!userStore.isLoggedIn) { toast.warning('请先登录'); return }
  if (post._likeLoading) return
  post._likeLoading = true
  const prev = post.isLiked
  post.isLiked = !post.isLiked
  post.likeCount += post.isLiked ? 1 : -1
  post.likeAnim = true
  setTimeout(() => post.likeAnim = false, 400)
  try {
    await circleApi.toggleLike(post.id)
  } catch (err: any) {
    post.isLiked = prev
    post.likeCount += post.isLiked ? 1 : -1
    logger.error('toggleLike error', { error: err.message })
    toast.error(err.response?.data?.message || '点赞失败，请稍后重试')
  } finally {
    post._likeLoading = false
  }
}

const openRepostModal = (post: any) => {
  if (!userStore.isLoggedIn) { toast.warning('请先登录'); return }
  currentRepostPost.value = post
  repostContent.value = ''
  showRepostModal.value = true
}

const closeRepostModal = () => {
  showRepostModal.value = false
  currentRepostPost.value = null
  repostContent.value = ''
}

const confirmRepost = async () => {
  if (!currentRepostPost.value) return
  reposting.value = true
  try {
    await circleApi.repost(currentRepostPost.value.id, repostContent.value)
    closeRepostModal()
    toast.success('转发成功')
    fetchPosts(true)
  } catch (err: any) {
    logger.error('repost error', { error: err.message })
    toast.error(err.response?.data?.message || '转发失败')
  } finally {
    reposting.value = false
  }
}

const openImagePreview = (images: string[], idx: number) => {
  previewImages.value = images
  previewIndex.value = idx
  showPreview.value = true
}

const previewPrev = () => {
  previewIndex.value = (previewIndex.value - 1 + previewImages.value.length) % previewImages.value.length
}

const previewNext = () => {
  previewIndex.value = (previewIndex.value + 1) % previewImages.value.length
}

const playVideo = (url: string) => {
  window.open(url, '_blank')
}

const MAX_IMAGE_SIZE = 10 * 1024 * 1024
const MAX_VIDEO_SIZE = 100 * 1024 * 1024

const handleImageUpload = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const files = Array.from(target.files || [])
  uploading.value = true
  uploadPercent.value = 0
  let uploadedCount = 0
  try {
    for (const file of files) {
      if (file.size > MAX_IMAGE_SIZE) {
        toast.warning(`图片 ${file.name} 超过10MB限制`)
        continue
      }
      if (newPost.images.length >= 9) break
      const res = await mediaApi.uploadFile(file, 'circle', (pe) => {
        if (pe.total) uploadPercent.value = Math.round((pe.loaded / pe.total) * 100)
      })
      newPost.images.push(res.data.fileUrl)
      uploadedCount++
    }
    if (uploadedCount > 0) toast.success('上传完成')
  } catch (err: any) {
    logger.error('upload image error', { error: err.message })
    toast.error(err.response?.data?.message || '图片上传失败')
  } finally {
    uploading.value = false
    uploadPercent.value = 0
    if (imageInput.value) imageInput.value.value = ''
  }
}

const handleVideoUpload = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  if (file.size > MAX_VIDEO_SIZE) {
    toast.warning('视频文件不能超过100MB')
    return
  }
  uploading.value = true
  uploadPercent.value = 0
  try {
    const res = await mediaApi.uploadFile(file, 'circle', (pe) => {
      if (pe.total) uploadPercent.value = Math.round((pe.loaded / pe.total) * 100)
    })
    newPost.videos.push(res.data.fileUrl)
    toast.success('视频上传成功')
  } catch (err: any) {
    logger.error('upload video error', { error: err.message })
    toast.error(err.response?.data?.message || '视频上传失败')
  } finally {
    uploading.value = false
    uploadPercent.value = 0
    if (videoInput.value) videoInput.value.value = ''
  }
}

const autoResize = () => {
  const el = textareaRef.value
  if (el) {
    el.style.height = 'auto'
    el.style.height = el.scrollHeight + 'px'
  }
}

const publishPost = async () => {
  if (!newPost.content.trim()) return
  publishing.value = true
  try {
    await circleApi.createPost({
      content: newPost.content,
      images: newPost.images,
      videos: newPost.videos,
      visibility: newPost.visibility,
      topicIds: selectedTopic.value ? [selectedTopic.value.id] : null,
      allowComment: 1,
      allowRepost: 1
    })
    showCreateModal.value = false
    newPost.content = ''
    newPost.images = []
    newPost.videos = []
    newPost.visibility = 0
    selectedTopic.value = null
    toast.success('发布成功')
    await fetchPosts(true)
  } catch (err: any) {
    logger.error('publish error', { error: err.message })
    toast.error(err.response?.data?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

const openCreateModal = () => {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    router.push('/login')
    return
  }
  showCreateModal.value = true
  nextTick(() => autoResize())
  if (allTopics.value.length === 0) {
    topicsLoading.value = true
    topicApi.getTopicList({ pageNum: 1, pageSize: 100 })
      .then(res => {
        const data = res.data
        allTopics.value = Array.isArray(data) ? data : (data?.records || [])
      })
      .catch(() => {})
      .finally(() => {
        topicsLoading.value = false
      })
  }
}

onMounted(() => {
  fetchPosts(true)
  fetchSidebarData()
  if (route.query.create === 'true') {
    nextTick(() => {
      openCreateModal()
      router.replace({ query: {} })
    })
  }
})

const refreshSidebarData = async () => {
  hotTopicsLoading.value = true
  hotPostsLoading.value = true
  try {
    const res = await topicApi.getHotTopics()
    const data = res.data
    hotTopics.value = Array.isArray(data) ? data : ((data as any)?.records || [])
  } catch { /* ignore */ } finally { hotTopicsLoading.value = false }
  try {
    const res = await circleApi.getRecommendFeed({ pageNum: 1, pageSize: 5 })
    const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    hotPosts.value = list.slice(0, 5)
  } catch { /* ignore */ } finally { hotPostsLoading.value = false }
}

let sidebarTimer: ReturnType<typeof setInterval> | null = null
const fetchSidebarData = async () => {
  await refreshSidebarData()
  sidebarTimer = setInterval(refreshSidebarData, 60000)
}

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
  if (sidebarTimer) clearInterval(sidebarTimer)
})
</script>

<style scoped>
.circle-page {
  position: relative;
  max-width: 1100px;
  margin: 0 auto;
  padding: var(--spacing-md);
  min-height: 100vh;
}

.circle-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 24px;
  align-items: start;
}

.circle-main {
  min-width: 0;
  max-width: 640px;
}

.circle-container {
  width: 100%;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
  margin-bottom: var(--spacing-md);
}

.back-btn:hover {
  background: var(--glass-hover);
  color: var(--primary);
  border-color: var(--primary);
  transform: translateY(-1px);
}

.create-post {
  padding: var(--spacing-md) var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-slow);
}

.create-post:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.create-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-avatar {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-full);
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid var(--border-solid);
  box-shadow: var(--shadow-sm);
}

.create-input {
  flex: 1;
  padding: var(--spacing-sm) var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--radius-full);
  color: var(--text-muted);
  font-size: 0.875rem;
  transition: all var(--transition);
}

.create-post:hover .create-input {
  background: var(--border);
}

.feed-tabs {
  display: flex;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-md);
  border-radius: var(--radius-md);
  padding: var(--spacing-xs);
}

.tab-btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) 0;
  font-size: 0.875rem;
  font-weight: 500;
  background: transparent;
  border: none;
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-slow);
}

.tab-btn.active {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.tab-btn:hover:not(.active) {
  color: var(--primary);
  background: var(--primary-light);
}

.feed-item {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  border-radius: var(--radius-lg);
  transition: all var(--transition-slow);
  animation: fadeUp 0.4s ease both;
}

.feed-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.feed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
}

.feed-author {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  text-decoration: none;
}

.author-avatar {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--border-solid);
  transition: all var(--transition);
}

.feed-author:hover .author-avatar {
  transform: scale(1.05);
  box-shadow: var(--shadow-md);
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  font-size: 0.9375rem;
  color: var(--text-primary);
}

.feed-time {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 2px;
}

.visibility-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: default;
}

.visibility-badge.glass-badge {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
}

.feed-content {
  margin-bottom: var(--spacing-md);
  cursor: pointer;
}

.feed-text {
  font-size: 0.9375rem;
  color: var(--text-primary);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
}

.topic-tag-link {
  display: inline-block;
  padding: 2px 10px;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 500;
  text-decoration: none;
  transition: all var(--transition);
}

.topic-tag-link:hover {
  background: var(--primary);
  color: var(--text-inverse);
  transform: translateY(-1px);
}

.topic-tag-link.glass-chip {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
}

.feed-images {
  display: grid;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-md);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.grid-1 { grid-template-columns: 1fr; max-width: 400px; }
.grid-2 { grid-template-columns: 1fr 1fr; }
.grid-3 { grid-template-columns: 1fr 1fr 1fr; }
.grid-4 { grid-template-columns: 1fr 1fr; }
.grid-multi { grid-template-columns: repeat(3, 1fr); }

.grid-multi .img-wrap:first-child:nth-last-child(5),
.grid-multi .img-wrap:first-child:nth-last-child(5) ~ .img-wrap:nth-child(-n+2) {
  grid-column: span 1;
}

.img-wrap {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: var(--radius-sm);
  background: var(--bg-secondary);
  cursor: pointer;
}

.img-wrap.is-video {
  cursor: pointer;
}

.feed-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.img-wrap:hover .feed-image {
  transform: scale(1.05);
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 44px;
  height: 44px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  pointer-events: none;
  transition: all var(--transition);
}

.img-wrap:hover .play-icon {
  background: var(--primary);
  transform: translate(-50%, -50%) scale(1.1);
}

.feed-videos {
  margin-top: var(--spacing-md);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.feed-video {
  width: 100%;
  max-height: 400px;
  object-fit: contain;
  background: #000;
  border-radius: var(--radius-md);
}

.repost-card {
  margin-top: var(--spacing-md);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition);
}

.repost-card.glass-inner {
  background: var(--surface);
  border: 1px solid var(--border);
}

.repost-card:hover {
  background: var(--primary-light);
  border-color: var(--primary);
}

.repost-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.repost-avatar {
  width: 22px;
  height: 22px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.repost-author {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-secondary);
}

.repost-text {
  font-size: 0.875rem;
  color: var(--text-primary);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.repost-images.mini {
  display: flex;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-sm);
}

.repost-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.repost-more {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--skeleton-base);
  border-radius: var(--radius-sm);
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.feed-actions {
  display: flex;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--glass-border);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-md);
  background: transparent;
  border: none;
  border-radius: var(--radius-full);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.8125rem;
  font-weight: 500;
  transition: all var(--transition);
}

.action-btn:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.action-btn.liked {
  color: var(--accent);
  background: var(--accent-light);
}

.like-icon.animate-pop {
  animation: likePop 0.4s var(--transition-spring);
}

@keyframes likePop {
  0% { transform: scale(1); }
  25% { transform: scale(1.4); }
  50% { transform: scale(0.85); }
  75% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.skeleton-header {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.skeleton-avatar {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-full);
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info { flex: 1; }
.skeleton-name { width: 100px; height: 14px; background: var(--skeleton-base); border-radius: var(--radius-xs); margin-bottom: var(--spacing-sm); animation: shimmer 1.5s infinite; background-size: 200% 100%; }
.skeleton-time { width: 60px; height: 12px; background: var(--skeleton-base); border-radius: var(--radius-xs); animation: shimmer 1.5s infinite; background-size: 200% 100%; }

.skeleton-text { height: 14px; background: var(--skeleton-base); border-radius: var(--radius-xs); margin-bottom: var(--spacing-sm); animation: shimmer 1.5s infinite; background-size: 200% 100%; }
.skeleton-text.short { width: 60%; }
.skeleton-images { display: flex; gap: var(--spacing-xs); margin-top: var(--spacing-md); }
.skeleton-img { flex: 1; aspect-ratio: 1; background: var(--skeleton-base); border-radius: var(--radius-sm); animation: shimmer 1.5s infinite; background-size: 200% 100%; }

.error-state,
.empty-state {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-lg);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-md);
}

.error-state h3,
.empty-state-title {
  margin: var(--spacing-md) 0 var(--spacing-sm);
  font-size: 1rem;
  color: var(--text-primary);
}

.error-state p,
.empty-state-text {
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin-bottom: var(--spacing-md);
}

.error-state svg,
.empty-state svg {
  color: var(--text-muted);
  opacity: 0.6;
}

.scroll-sentinel {
  text-align: center;
  padding: var(--spacing-lg) 0;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  color: var(--text-muted);
  font-size: 0.8125rem;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--border);
  border-top-color: var(--primary);
  border-radius: var(--radius-full);
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.no-more {
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-modal);
  padding: var(--spacing-lg);
}

.modal-content {
  width: 100%;
  max-width: 540px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--glass-border);
}

.modal-header h3 {
  font-size: 1rem;
  font-weight: 600;
}

.close-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}

.close-btn:hover {
  background: var(--error-light);
  color: var(--error);
  border-color: var(--error);
}

.modal-body {
  padding: var(--spacing-lg);
  overflow-y: auto;
  flex: 1;
}

.create-top {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.modal-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid var(--border-solid);
}

.create-info {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.create-nickname {
  font-size: 0.875rem;
  font-weight: 600;
}

.visibility-selector {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 2px 10px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
}

.visibility-selector:hover {
  background: var(--glass-hover);
  border-color: var(--primary);
  color: var(--primary);
}

.visibility-dropdown {
  position: absolute;
  top: calc(100% + var(--spacing-xs));
  left: 0;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: var(--spacing-xs);
  z-index: 100;
  min-width: 220px;
}

.vis-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition);
}

.vis-item:hover,
.vis-item.active {
  background: var(--primary-light);
}

.vis-item.active {
  color: var(--primary);
}

.vis-icon {
  display: inline-flex;
  color: var(--text-muted);
}

.vis-item:hover .vis-icon,
.vis-item.active .vis-icon {
  color: var(--primary);
}

.vis-label { font-size: 0.8125rem; font-weight: 500; }
.vis-desc { font-size: 0.6875rem; color: var(--text-muted); margin-top: 2px; }

.post-textarea {
  width: 100%;
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  padding: var(--spacing-md);
  font-size: 0.9375rem;
  line-height: 1.6;
  resize: none;
  color: var(--text-primary);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  font-family: inherit;
  min-height: 100px;
  box-sizing: border-box;
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
}

.post-textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light), var(--glass-shadow);
  background: var(--glass-hover);
}

.post-textarea::placeholder {
  color: var(--text-muted);
}

.char-count {
  text-align: right;
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: var(--spacing-sm);
  margin-top: var(--spacing-xs);
}

.char-count.warn {
  color: var(--warning);
}

.uploaded-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--bg-secondary);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: var(--spacing-xs);
  right: var(--spacing-xs);
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}

.remove-image:hover {
  background: var(--error);
  transform: scale(1.1);
}

.uploaded-videos {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.video-item {
  position: relative;
  border-radius: var(--radius);
  overflow: hidden;
  background: #000;
  max-width: 100%;
}

.video-preview {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  display: block;
}

.remove-video {
  position: absolute;
  top: var(--spacing-xs);
  right: var(--spacing-xs);
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}

.remove-video:hover {
  background: var(--error);
  transform: scale(1.1);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.tool-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.8125rem;
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
}

.tool-btn:hover:not(:disabled) {
  background: var(--primary-light);
  color: var(--primary);
  border-color: var(--primary);
}

.tool-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.tool-right {
  margin-left: auto;
}

.upload-progress-bar {
  margin-top: var(--spacing-sm);
  height: 4px;
  background: var(--skeleton-base);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-start), var(--primary-end));
  border-radius: var(--radius-full);
  transition: width var(--transition-slow);
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  border-top: 1px solid var(--glass-border);
}

.create-topic-modal .form-group {
  margin-bottom: var(--spacing-md);
}

.create-topic-modal .form-label {
  display: block;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.create-topic-modal .form-input {
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  color: var(--text-primary);
  font-size: 0.9375rem;
  transition: all var(--transition);
  box-sizing: border-box;
  box-shadow: var(--glass-shadow);
}

.create-topic-modal .form-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light), var(--glass-shadow);
}

.create-topic-modal .form-textarea {
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  color: var(--text-primary);
  font-size: 0.9375rem;
  line-height: 1.6;
  resize: vertical;
  font-family: inherit;
  transition: all var(--transition);
  box-sizing: border-box;
  box-shadow: var(--glass-shadow);
}

.create-topic-modal .form-textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light), var(--glass-shadow);
}

.repost-original-card {
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-md);
}

.repost-original-card.glass-inner {
  background: var(--surface);
  border: 1px solid var(--border);
}

.repost-original-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.repost-original-avatar {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.repost-original-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-secondary);
}

.repost-original-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.preview-overlay {
  background: rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: var(--z-toast);
  cursor: zoom-out;
}

.preview-image {
  max-width: 90vw;
  max-height: 85vh;
  object-fit: contain;
  border-radius: var(--radius);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.4);
}

.preview-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}

.preview-nav:hover {
  background: var(--primary);
  border-color: var(--primary);
  transform: translateY(-50%) scale(1.1);
}

.preview-nav.prev { left: var(--spacing-lg); }
.preview-nav.next { right: var(--spacing-lg); }

.preview-close {
  position: absolute;
  top: var(--spacing-lg);
  right: var(--spacing-lg);
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}

.preview-close:hover {
  background: var(--error);
  border-color: var(--error);
  transform: scale(1.1);
}

.preview-counter {
  position: absolute;
  bottom: var(--spacing-lg);
  left: 50%;
  transform: translateX(-50%);
  color: #fff;
  font-size: 0.875rem;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-full);
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity var(--transition-slow) ease;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform var(--transition-slow) ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.95) translateY(10px);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all var(--transition) ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.topic-selector {
  margin-bottom: var(--spacing-md);
}

.selected-topic {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  background: var(--primary-light);
  border-radius: var(--radius-full);
}

.topic-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--primary);
}

.remove-topic {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.1);
  border: none;
  border-radius: var(--radius-full);
  color: var(--primary);
  cursor: pointer;
  transition: all var(--transition);
}

.remove-topic:hover {
  background: var(--error);
  color: #fff;
}

.topic-input-wrapper {
  position: relative;
}

.topic-search-box {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
}

.topic-search-box:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light), var(--glass-shadow);
}

.topic-search-box svg {
  color: var(--text-muted);
  flex-shrink: 0;
}

.topic-search-box input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.8125rem;
  outline: none;
  color: var(--text-primary);
  padding: 0;
}

.topic-search-box input::placeholder {
  color: var(--text-muted);
}

.topic-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  color: var(--text-muted);
  font-size: 0.8125rem;
}

.topic-dropdown {
  position: absolute;
  top: calc(100% + var(--spacing-xs));
  left: 0;
  right: 0;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
}

.topic-dropdown-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  cursor: pointer;
  transition: all var(--transition);
}

.topic-dropdown-item:hover {
  background: var(--primary-light);
}

.topic-dropdown-item:not(:last-child) {
  border-bottom: 1px solid var(--glass-border);
}

.topic-create-divider {
  height: 1px;
  background: var(--glass-border);
  margin: var(--spacing-xs) 0;
}

.topic-create-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  cursor: pointer;
  transition: all var(--transition);
  color: var(--primary);
}

.topic-create-item:hover {
  background: var(--primary-light);
}

.topic-create-text {
  font-size: 0.8125rem;
  font-weight: 500;
}

.topic-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--primary);
}

.topic-count {
  font-size: 0.6875rem;
  color: var(--text-muted);
}

.location-display {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-sm);
  font-size: 0.75rem;
  color: var(--text-muted);
}

.location-display svg {
  color: var(--primary);
}

.view-count {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-md);
  color: var(--text-muted);
  font-size: 0.8125rem;
  margin-left: auto;
}

.view-count svg {
  opacity: 0.6;
}

.repost-user-content {
  font-size: 0.875rem;
  color: var(--text-primary);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: var(--spacing-xs);
  padding-bottom: var(--spacing-xs);
  border-bottom: 1px dashed var(--glass-border);
}

.repost-hidden-notice {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  margin-top: var(--spacing-md);
  background: var(--skeleton-base);
  border-radius: var(--radius-md);
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.repost-hidden-notice svg {
  opacity: 0.5;
}

@media (max-width: 640px) {
  .circle-page {
    padding: var(--spacing-sm);
  }
  .feed-item {
    padding: var(--spacing-md);
  }
  .modal-content {
    max-height: 95vh;
    margin: var(--spacing-sm);
    border-radius: var(--radius-lg);
  }
  .modal-body {
    padding: var(--spacing-md);
  }
  .feed-tabs {
    padding: var(--spacing-xs);
  }
  .preview-nav.prev { left: var(--spacing-sm); }
  .preview-nav.next { right: var(--spacing-sm); }

  .circle-layout {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .sidebar-left {
    display: none !important;
  }

  .circle-main {
    max-width: none;
  }

  .circle-container {
    max-width: 640px;
    margin: 0 auto;
  }
}

.sidebar-card {
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  position: sticky;
  top: calc(var(--navbar-height) + var(--spacing-md));
}

.sidebar-left .sidebar-card-top {
  margin-bottom: var(--spacing-md);
}

.sidebar-card-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--glass-border);
}

.sidebar-topic-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.sidebar-topic-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-xs);
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: all var(--transition);
}

.sidebar-topic-item:hover {
  background: var(--primary-light);
}

.sidebar-topic-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--primary);
}

.sidebar-topic-count {
  font-size: 0.6875rem;
  color: var(--text-muted);
}

.sidebar-post-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.sidebar-post-item {
  display: block;
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: all var(--transition);
  border: 1px solid var(--glass-border);
}

.sidebar-post-item:hover {
  background: var(--primary-light);
  border-color: var(--primary);
}

.sidebar-post-author {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-xs);
}

.sidebar-avatar {
  width: 20px;
  height: 20px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 1px solid var(--border-solid);
}

.sidebar-post-name {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--text-primary);
}

.sidebar-post-text {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.4;
  margin: 0 0 var(--spacing-xs);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.sidebar-post-stats {
  display: flex;
  gap: var(--spacing-sm);
  font-size: 0.6875rem;
  color: var(--text-muted);
}

.sidebar-loading {
  text-align: center;
  padding: var(--spacing-md);
  color: var(--text-muted);
  font-size: 0.8125rem;
}

.sidebar-empty {
  text-align: center;
  padding: var(--spacing-md);
  color: var(--text-muted);
  font-size: 0.8125rem;
}

.spinner-small {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 2px solid var(--border);
  border-top-color: var(--primary);
  border-radius: var(--radius-full);
  animation: spin 0.6s linear infinite;
  vertical-align: middle;
  margin-right: 4px;
}

.post-owner-actions {
  display: flex;
  gap: 2px;
  margin-left: auto;
  align-items: center;
}

.action-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  border-radius: var(--radius);
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition);
}

.action-icon-btn:hover {
  background: var(--primary-light);
  color: var(--primary);
}
</style>
