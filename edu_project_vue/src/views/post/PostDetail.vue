<template>
  <!-- Reading progress bar -->
  <div class="reading-progress-bar" :style="{ width: readProgress + '%' }"></div>

  <div class="post-detail-page">
    <!-- Hero cover - full width, parallax -->
    <div v-if="post?.coverImage && !isLoading" class="hero-cover" ref="heroCoverRef">
      <div class="hero-cover-inner" :style="{ transform: `translateY(${parallaxOffset}px)` }">
        <img :src="post.coverImage" :alt="post.title" />
      </div>
      <div class="hero-cover-overlay"></div>
      <div class="hero-cover-grain"></div>
    </div>

    <!-- Main content area -->
    <div class="post-layout">
      <!-- Article column -->
      <main class="post-main">
        <!-- Back button -->
        <button class="back-btn" @click="router.back()">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
          <span>返回</span>
        </button>

        <!-- Loading State -->
        <div v-if="isLoading" class="loading-state">
          <div class="loading-skeleton">
            <div class="skeleton-line skeleton-title"></div>
            <div class="skeleton-line skeleton-subtitle"></div>
            <div class="skeleton-avatar-row">
              <div class="skeleton-circle"></div>
              <div class="skeleton-line skeleton-name"></div>
            </div>
            <div class="skeleton-line" v-for="i in 8" :key="i" :style="{ width: `${70 + Math.random() * 30}%` }"></div>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="error && !post" class="error-state">
          <div class="error-icon-wrap">
            <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <h2>{{ error }}</h2>
          <div class="error-actions">
            <button @click="fetchPost" class="btn btn-primary">重新加载</button>
            <router-link to="/" class="btn btn-outline">返回首页</router-link>
          </div>
        </div>

        <!-- Article content -->
        <article v-else-if="post" class="post-article" :class="{ 'has-cover': post.coverImage }">
          <!-- Title area (below hero if cover exists) -->
          <header class="post-header animate-in">
            <!-- Tags above title -->
            <div v-if="post.tags && post.tags.length" class="post-tags-top">
              <span
                v-for="(tag, idx) in post.tags"
                :key="tag.id"
                class="tag-pill"
                :style="{ animationDelay: (Number(idx) * 60) + 'ms' }"
              >
                {{ tag.name }}
              </span>
            </div>

            <h1 class="post-title">{{ post.title }}</h1>

            <!-- Meta row: time, views, reading time -->
            <div class="post-meta-row">
              <span class="meta-item">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                {{ formatRelativeTime(post.createTime) }}
              </span>
              <span class="meta-dot"></span>
              <span class="meta-item">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                {{ post.viewCount || 0 }} 阅读
              </span>
              <span class="meta-dot"></span>
              <span class="meta-item reading-time">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
                约 {{ estimatedReadTime }} 分钟
              </span>
            </div>
          </header>

          <!-- Author card -->
          <div class="author-card animate-in" style="animation-delay: 100ms">
            <router-link :to="`/user/${post.userId}`" class="author-info">
              <div class="author-avatar-wrap">
                <img :src="post.avatar || '/default-avatar.png'" :alt="post.username" class="author-avatar" />
              </div>
              <div class="author-meta">
                <span class="author-name">{{ post.nickname || post.username }}</span>
                <span class="author-label">作者</span>
              </div>
            </router-link>
            <div class="author-actions">
              <button
                v-if="userStore.isLoggedIn && userStore.userId !== post.userId"
                class="btn btn-follow"
                :class="{ 'is-following': isFollowing }"
                @click="toggleFollow"
                :disabled="isTogglingFollow"
              >
                <svg v-if="!isFollowing" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg>
                {{ isFollowing ? '已关注' : '关注' }}
              </button>
              <router-link
                v-if="userStore.isLoggedIn && userStore.userId !== post.userId"
                :to="`/messages?userId=${post.userId}`"
                class="btn btn-ghost btn-sm"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4 20-7z"/></svg>
                私信
              </router-link>
              <router-link v-if="userStore.isLoggedIn && userStore.userId === post.userId" :to="`/post-edit/${post.id}`" class="btn btn-ghost btn-sm">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                编辑
              </router-link>
            </div>
          </div>

          <!-- Article body -->
          <div class="post-body animate-in" style="animation-delay: 200ms">
            <div class="markdown-body" v-html="renderedContent" ref="contentRef"></div>
          </div>

          <!-- Article footer: tags + actions -->
          <footer class="post-footer animate-in" style="animation-delay: 300ms">
            <!-- Bottom tags -->
            <div v-if="post.tags && post.tags.length" class="post-tags-bottom">
              <span
                v-for="tag in post.tags"
                :key="tag.id"
                class="tag-pill tag-pill-ghost"
              >
                #{{ tag.name }}
              </span>
            </div>

            <!-- Action buttons -->
            <div class="post-actions">
              <button
                class="action-btn"
                :class="{ active: isLiked }"
                @click="toggleLike"
                :disabled="!userStore.isLoggedIn || isTogglingLike"
              >
                <span class="action-icon-wrap">
                  <svg width="20" height="20" viewBox="0 0 24 24" :fill="isLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                </span>
                <span class="action-label">{{ post.likeCount || 0 }}</span>
              </button>
              <button
                class="action-btn"
                :class="{ active: isCollected }"
                @click="toggleCollect"
                :disabled="!userStore.isLoggedIn || isTogglingCollect"
              >
                <span class="action-icon-wrap">
                  <svg width="20" height="20" viewBox="0 0 24 24" :fill="isCollected ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
                  </svg>
                </span>
                <span class="action-label">{{ post.collectCount || 0 }}</span>
              </button>
              <button class="action-btn" @click="sharePost">
                <span class="action-icon-wrap">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="18" cy="5" r="3"/>
                    <circle cx="6" cy="12" r="3"/>
                    <circle cx="18" cy="19" r="3"/>
                    <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                    <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                  </svg>
                </span>
                <span class="action-label">{{ post.shareCount || 0 }}</span>
              </button>
              <div class="action-divider"></div>
              <button class="action-btn action-btn-report" @click="openReport" title="举报">
                <span class="action-icon-wrap">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" y1="22" x2="4" y2="15"/></svg>
                </span>
              </button>
            </div>
          </footer>
        </article>

        <!-- Comment section -->
        <section v-if="post" class="comment-section animate-in" style="animation-delay: 400ms">
          <div class="comment-header-bar">
            <h3 class="section-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              评论
            </h3>
            <span class="comment-count-badge">{{ comments.length }}</span>
          </div>

          <!-- Comment input -->
          <div v-if="userStore.isLoggedIn" class="comment-input-wrap">
            <div v-if="replyingTo" class="reply-indicator">
              <div class="reply-indicator-content">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 17 4 12 9 7"/><path d="M20 18v-2a4 4 0 0 0-4-4H4"/></svg>
                <span>回复 <strong>@{{ replyingTo.nickname || replyingTo.username }}</strong></span>
              </div>
              <button @click="cancelReply" class="cancel-reply-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <textarea
              v-model="commentContent"
              :placeholder="replyingTo ? '写下你的回复...' : '写下你的评论...'"
              rows="3"
              maxlength="1000"
              class="comment-textarea"
            ></textarea>
            <div class="comment-input-footer">
              <span class="char-count" :class="{ 'near-limit': commentContent.length > 900 }">{{ commentContent.length }}/1000</span>
              <button
                class="btn btn-primary btn-sm"
                @click="submitComment"
                :disabled="!commentContent.trim()"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                {{ replyingTo ? '发表回复' : '发表评论' }}
              </button>
            </div>
          </div>
          <div v-else class="login-hint">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
            <div class="login-hint-text">
              <router-link to="/login" class="login-link">登录</router-link>
              <span>后参与评论互动</span>
            </div>
          </div>

          <!-- Comment list -->
          <div class="comment-list">
            <div v-if="commentsError" class="empty-hint error-hint">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <p>评论加载失败，请稍后重试</p>
              <button @click="fetchComments" class="btn btn-sm btn-outline">点击重试</button>
            </div>
            <div v-else-if="comments.length === 0" class="empty-hint">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              <p>暂无评论，快来抢沙发吧</p>
            </div>
            <TransitionGroup name="comment-list" tag="div" class="comment-items">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <router-link :to="`/user/${comment.userId}`" class="comment-avatar-link">
                  <img :src="comment.userAvatar || '/default-avatar.png'" :alt="comment.username" class="comment-avatar" />
                </router-link>
                <div class="comment-body">
                  <div class="comment-header-row">
                    <div class="comment-author-info">
                      <router-link :to="`/user/${comment.userId}`" class="comment-author">
                        {{ comment.nickname || comment.username }}
                      </router-link>
                      <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
                    </div>
                  </div>
                  <div class="comment-content" v-html="sanitizeText(comment.content)"></div>
                  <div class="comment-actions">
                    <button
                      v-if="userStore.isLoggedIn"
                      class="comment-action-btn"
                      @click="replyTo(comment)"
                    >
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 17 4 12 9 7"/><path d="M20 18v-2a4 4 0 0 0-4-4H4"/></svg>
                      回复
                    </button>
                    <button
                      v-if="userStore.isLoggedIn && (userStore.userId === comment.userId || userStore.isAdmin)"
                      class="comment-action-btn comment-action-delete"
                      @click="deleteComment(comment.id)"
                    >
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                      删除
                    </button>
                  </div>

                  <!-- Replies -->
                  <div v-if="comment.replies && comment.replies.length" class="reply-list">
                    <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                      <router-link :to="`/user/${reply.userId}`" class="reply-avatar-link">
                        <img :src="reply.userAvatar || '/default-avatar.png'" :alt="reply.username" class="reply-avatar" />
                      </router-link>
                      <div class="reply-body">
                        <div class="reply-header-row">
                          <router-link :to="`/user/${reply.userId}`" class="reply-author">
                            {{ reply.nickname || reply.username }}
                          </router-link>
                          <span class="reply-time">{{ formatRelativeTime(reply.createTime) }}</span>
                        </div>
                        <div class="reply-content" v-html="sanitizeText(reply.content)"></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </TransitionGroup>
          </div>
        </section>
      </main>

      <!-- Sidebar -->
      <aside v-if="post && !isLoading" class="post-sidebar">
        <!-- Floating action bar (desktop) -->
        <div class="sidebar-actions" :class="{ 'is-scrolled': showSidebarActions }">
          <button
            class="sidebar-action-btn"
            :class="{ active: isLiked }"
            @click="toggleLike"
            :disabled="!userStore.isLoggedIn || isTogglingLike"
            title="点赞"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" :fill="isLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <span class="sidebar-action-count">{{ post?.likeCount || 0 }}</span>
          </button>
          <button
            class="sidebar-action-btn"
            :class="{ active: isCollected }"
            @click="toggleCollect"
            :disabled="!userStore.isLoggedIn || isTogglingCollect"
            title="收藏"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" :fill="isCollected ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
            </svg>
            <span class="sidebar-action-count">{{ post?.collectCount || 0 }}</span>
          </button>
          <button class="sidebar-action-btn" @click="sharePost" title="分享">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
              <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
            </svg>
            <span class="sidebar-action-count">{{ post?.shareCount || 0 }}</span>
          </button>
          <div class="sidebar-action-divider"></div>
          <button class="sidebar-action-btn" @click="scrollToComments" title="评论">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <span class="sidebar-action-count">{{ post?.commentCount || 0 }}</span>
          </button>
        </div>

        <!-- Table of contents -->
        <div v-if="toc.length > 0" class="toc-card">
          <h3 class="sidebar-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
            目录
          </h3>
          <nav class="toc-nav">
            <a
              v-for="(item, index) in toc"
              :key="item.id"
              :href="`#${item.id}`"
              class="toc-link"
              :class="[`level-${item.level}`, { active: activeTocId === item.id }]"
              @click.prevent="scrollToHeading(index)"
            >
              <span class="toc-indicator"></span>
              <span class="toc-text">{{ item.text }}</span>
            </a>
          </nav>
        </div>

        <!-- Article info card -->
        <div class="info-card">
          <h3 class="sidebar-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
            文章信息
          </h3>
          <div class="info-grid">
            <div class="info-stat">
              <span class="info-stat-value">{{ post?.viewCount || 0 }}</span>
              <span class="info-stat-label">阅读</span>
            </div>
            <div class="info-stat">
              <span class="info-stat-value">{{ post?.likeCount || 0 }}</span>
              <span class="info-stat-label">点赞</span>
            </div>
            <div class="info-stat">
              <span class="info-stat-value">{{ post?.collectCount || 0 }}</span>
              <span class="info-stat-label">收藏</span>
            </div>
            <div class="info-stat">
              <span class="info-stat-value">{{ post?.commentCount || 0 }}</span>
              <span class="info-stat-label">评论</span>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- Mobile floating action bar -->
    <div v-if="post && !isLoading" class="mobile-actions" :class="{ visible: showMobileActions }">
      <button class="mobile-action-btn" :class="{ active: isLiked }" @click="toggleLike" :disabled="!userStore.isLoggedIn || isTogglingLike">
        <svg width="18" height="18" viewBox="0 0 24 24" :fill="isLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
        <span>{{ post?.likeCount || 0 }}</span>
      </button>
      <button class="mobile-action-btn" :class="{ active: isCollected }" @click="toggleCollect" :disabled="!userStore.isLoggedIn || isTogglingCollect">
        <svg width="18" height="18" viewBox="0 0 24 24" :fill="isCollected ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
          <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
        </svg>
        <span>{{ post?.collectCount || 0 }}</span>
      </button>
      <button class="mobile-action-btn" @click="sharePost">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
          <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
        </svg>
        <span>分享</span>
      </button>
      <button class="mobile-action-btn" @click="scrollToComments">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        <span>{{ comments.length }}</span>
      </button>
    </div>

    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { postApi } from '../../api/post'
import { commentApi } from '../../api/comment'
import { likeApi } from '../../api/like'
import { collectApi } from '../../api/collect'
import { followApi } from '../../api/follow'
import { shareApi } from '../../api/share'
import { useUserStore } from '../../stores/user'
import { formatRelativeTime, copyToClipboard } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'
import { useConfirm } from '../../composables/useConfirm'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('PostDetail')
const { confirm, ConfirmDialog } = useConfirm()

const post = ref<any>(null)
const comments = ref<any[]>([])
const isLiked = ref(false)
const isCollected = ref(false)
const isFollowing = ref(false)
const readProgress = ref(0)
const commentContent = ref('')
const toc = ref<any[]>([])
const activeTocId = ref('')
const isLoading = ref(false)
const error = ref<string | boolean>(false)
const commentsError = ref(false)
const replyingTo = ref<any>(null)
// @ts-expect-error -- used as template ref
const contentRef = ref<HTMLElement | null>(null)
const heroCoverRef = ref<HTMLElement | null>(null)
const parallaxOffset = ref(0)
const showSidebarActions = ref(false)
const showMobileActions = ref(false)

// Estimated reading time (approx 400 chars/min for Chinese)
const estimatedReadTime = computed(() => {
  if (!post.value?.content) return 1
  return Math.max(1, Math.ceil(post.value.content.length / 400))
})

// Render Markdown content
const renderedContent = computed(() => {
  if (!post.value?.content) return ''
  const rawHtml = marked.parse(post.value.content) as string
  const sanitized = DOMPurify.sanitize(rawHtml, {
    USE_PROFILES: { html: true },
    ADD_ATTR: ['target', 'referrerpolicy'],
    FORBID_TAGS: ['style', 'script', 'iframe', 'form'],
    FORBID_ATTR: ['onerror', 'onload', 'onclick']
  })
  return sanitized.replace(/<a\s/g, '<a target="_blank" rel="noopener noreferrer" referrerpolicy="no-referrer" ')
    .replace(/<img\s/g, '<img referrerpolicy="no-referrer" loading="lazy" ')
})

// XSS protection
const sanitizeText = (text: string) => {
  if (!text) return ''
  return DOMPurify.sanitize(text, { ALLOWED_TAGS: [] })
}

// Extract TOC from markdown
const extractTocFromMarkdown = (markdown: string) => {
  if (!markdown) return []
  const headingRegex = /^(#{1,4})\s+(.+)$/gm
  const items = []
  let match
  while ((match = headingRegex.exec(markdown)) !== null) {
    items.push({
      id: `heading-${items.length}`,
      text: match[2] || '',
      level: (match[1] || '').length
    })
  }
  return items
}

// Add IDs to DOM headings
const addHeadingIds = () => {
  const headings = document.querySelectorAll('.markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4')
  headings.forEach((el, index) => {
    if (!el.id) el.id = `heading-${index}`
  })
}

// Scroll to heading
const scrollToHeading = (index: number) => {
  const headings = document.querySelectorAll('.markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4')
  const target = headings[index]
  if (target) {
    const offset = 80
    const top = target.getBoundingClientRect().top + window.scrollY - offset
    window.scrollTo({ top, behavior: 'smooth' })
    activeTocId.value = toc.value[index]?.id || ''
  }
}

// Scroll to comments section
const scrollToComments = () => {
  const el = document.querySelector('.comment-section')
  if (el) {
    const offset = 80
    const top = el.getBoundingClientRect().top + window.scrollY - offset
    window.scrollTo({ top, behavior: 'smooth' })
  }
}

// Code block copy button event delegation
const handleCodeCopyClick = async (e: Event) => {
  const target = e.target as HTMLElement
  const btn = target.closest('.copy-btn')
  if (!btn) return
  const block = btn.closest('pre')
  if (!block) return
  const code = block.querySelector('code')?.textContent || block.textContent
  try {
    await navigator.clipboard.writeText(code)
    btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>已复制'
    btn.classList.add('copied')
    setTimeout(() => {
      btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>复制'
      btn.classList.remove('copied')
    }, 2000)
  } catch {
    toast.error('复制失败')
  }
}

// Add copy buttons to code blocks
const addCodeCopyButtons = () => {
  const markdownBody = document.querySelector('.markdown-body')
  if (!markdownBody || (markdownBody as any)._copyDelegate) return
  ;(markdownBody as any)._copyDelegate = true
  markdownBody.addEventListener('click', handleCodeCopyClick)
  const codeBlocks = markdownBody.querySelectorAll('pre')
  codeBlocks.forEach((block) => {
    if (block.querySelector('.copy-btn')) return
    const btn = document.createElement('button')
    btn.className = 'copy-btn'
    btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>复制'
    block.appendChild(btn)
  })
}

// Fetch post details
const fetchPost = async () => {
  isLoading.value = true
  error.value = false

  try {
    const response = await postApi.getPostById(String(route.params.id))

    if (!response.data) {
      error.value = true
      return
    }

    post.value = response.data

    if (userStore.isLoggedIn) {
      try {
        await Promise.all([
          checkLikeStatus(),
          checkCollectStatus(),
          checkFollowStatus()
        ])
      } catch (err: any) {
        logger.warn('Failed to fetch initial status', { error: err.message })
      }
    }

    try {
      const shareResponse = await shareApi.getShareCount(String(route.params.id))
      post.value.shareCount = shareResponse.data ?? 0
    } catch (err: any) {
      post.value.shareCount = 0
    }
  } catch (err: any) {
    logger.error('Failed to fetch post', { error: err.message })
    if (err.response?.status === 404) {
      error.value = '文章不存在或已被删除'
    } else if (err.response?.status === 403) {
      error.value = '抱歉，您无权查看此文章'
    } else {
      error.value = '加载失败，请稍后重试'
    }
  } finally {
    isLoading.value = false
  }
}

// Extract TOC after content loads
watch(() => post.value?.content, async (content) => {
  toc.value = content ? extractTocFromMarkdown(content) : []
  if (content) {
    await nextTick()
    addHeadingIds()
    addCodeCopyButtons()
  }
})

// Fetch comments
const fetchComments = async () => {
  commentsError.value = false
  try {
    const response = await commentApi.getCommentsByPostId(String(route.params.id), {} as any)
    comments.value = response.data?.records || response.data || []
  } catch (err: any) {
    logger.error('Failed to fetch comments', { error: err.message })
    commentsError.value = true
  }
}

// Check like status
const checkLikeStatus = async () => {
  try {
    const response = await likeApi.checkLikeStatus(String(route.params.id))
    isLiked.value = !!(response.data as any)?.liked
  } catch (err: any) {
    logger.error('Failed to check like status', { error: err.message })
  }
}

// Check collect status
const checkCollectStatus = async () => {
  try {
    const response = await collectApi.checkCollectStatus(String(route.params.id))
    isCollected.value = !!(response.data as any)?.collected
  } catch (err: any) {
    logger.error('Failed to check collect status', { error: err.message })
  }
}

// Check follow status
const checkFollowStatus = async () => {
  if (!post.value?.userId) return
  try {
    const response = await followApi.checkFollowStatus(post.value.userId)
    isFollowing.value = !!(response.data as any)?.following
  } catch (err: any) {
    logger.error('Failed to check follow status', { error: err.message })
  }
}

// Debounce states
const isTogglingLike = ref(false)
const isTogglingCollect = ref(false)
const isTogglingFollow = ref(false)

// Toggle like
const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (isTogglingLike.value) return
  isTogglingLike.value = true

  const prevLiked = isLiked.value
  const prevCount = post.value.likeCount
  isLiked.value = !isLiked.value
  post.value.likeCount = (post.value.likeCount || 0) + (isLiked.value ? 1 : -1)

  try {
    const res = await likeApi.toggleLike(String(route.params.id))
    const data = res.data as any
    if (data?.action) {
      isLiked.value = data.action === 'like'
    }
    if (data?.likeCount !== undefined) {
      post.value.likeCount = data.likeCount
    }
  } catch (err: any) {
    isLiked.value = prevLiked
    post.value.likeCount = prevCount
    logger.error('Failed to toggle like', { error: err.message })
    toast.error('操作失败')
  } finally {
    isTogglingLike.value = false
  }
}

// Toggle collect
const toggleCollect = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (isTogglingCollect.value) return
  isTogglingCollect.value = true

  const prevCollected = isCollected.value
  const prevCount = post.value.collectCount
  isCollected.value = !isCollected.value
  post.value.collectCount = (post.value.collectCount || 0) + (isCollected.value ? 1 : -1)

  try {
    const res = await collectApi.toggleCollect(String(route.params.id))
    const data = res.data as any
    if (data?.action) {
      isCollected.value = data.action === 'collect'
    }
    if (data?.collectCount !== undefined) {
      post.value.collectCount = data.collectCount
    }
  } catch (err: any) {
    isCollected.value = prevCollected
    post.value.collectCount = prevCount
    logger.error('Failed to toggle collect', { error: err.message })
    toast.error('操作失败')
  } finally {
    isTogglingCollect.value = false
  }
}

const openReport = () => {
  router.push(`/report/post/${post.value.id}`)
}

// Toggle follow
const toggleFollow = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!post.value?.userId) return
  if (isTogglingFollow.value) return
  isTogglingFollow.value = true

  const prevFollowing = isFollowing.value
  isFollowing.value = !isFollowing.value

  try {
    const res = await followApi.toggleFollow(post.value.userId)
    const data = res.data as any
    if (data?.action) {
      isFollowing.value = data.action === 'follow'
    }
  } catch (err: any) {
    isFollowing.value = prevFollowing
    logger.error('Failed to toggle follow', { error: err.message })
    toast.error('操作失败')
  } finally {
    isTogglingFollow.value = false
  }
}

// Submit comment
const submitComment = async () => {
  if (!commentContent.value.trim()) return

  try {
    const data: any = {
      postId: String(route.params.id),
      content: commentContent.value
    }

    if (replyingTo.value) {
      data.parentId = replyingTo.value.id
    }

    await commentApi.createComment(data)
    commentContent.value = ''
    replyingTo.value = null
    await fetchComments()
    toast.success('评论成功')
  } catch (err: any) {
    logger.error('Failed to submit comment', { error: err.message })
    toast.error('评论失败')
  }
}

// Delete comment
const deleteComment = async (commentId: number | string) => {
  const ok = await confirm('确定删除这条评论吗？')
  if (!ok) return

  try {
    await commentApi.deleteComment(commentId)
    await fetchComments()
  } catch (err: any) {
    logger.error('Failed to delete comment', { error: err.message })
    toast.error('删除失败')
  }
}

// Reply to comment
const replyTo = (comment: any) => {
  replyingTo.value = comment
  commentContent.value = `@${comment.nickname || comment.username} `
  document.querySelector('.comment-input-wrap')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

// Cancel reply
const cancelReply = () => {
  replyingTo.value = null
}

// Share post
const sharePost = async () => {
  const url = window.location.href
  const success = await copyToClipboard(url)
  if (success) {
    toast.success('链接已复制到剪贴板')
    try {
      await shareApi.recordShare(String(route.params.id), 'web')
      post.value.shareCount = (post.value.shareCount || 0) + 1
    } catch (err: any) {
      logger.warn('Failed to record share', { error: err.message })
    }
  } else {
    toast.error('复制失败，请手动复制链接')
  }
}

// Scroll handler: progress, parallax, TOC highlight, sidebar visibility
const handleScroll = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  readProgress.value = docHeight > 0 ? Math.min(100, Math.round((scrollTop / docHeight) * 100)) : 0

  // Parallax for hero cover
  if (heroCoverRef.value) {
    parallaxOffset.value = scrollTop * 0.35
  }

  // Show sidebar actions after scrolling past the article header
  showSidebarActions.value = scrollTop > 400
  showMobileActions.value = scrollTop > 300

  // TOC active heading
  const headings = document.querySelectorAll('.markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4')
  let currentId = ''
  headings.forEach((heading: Element) => {
    if (heading.getBoundingClientRect().top <= 100) {
      currentId = heading.id
    }
  })
  activeTocId.value = currentId
}

onMounted(() => {
  fetchPost()
  fetchComments()
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  const markdownBody = document.querySelector('.markdown-body')
  if (markdownBody && (markdownBody as any)._copyDelegate) {
    markdownBody.removeEventListener('click', handleCodeCopyClick)
    delete (markdownBody as any)._copyDelegate
  }
})

watch(() => route.params.id, () => {
  post.value = null
  comments.value = []
  toc.value = []
  error.value = false
  isLoading.value = true
  commentContent.value = ''
  replyingTo.value = null
  isLiked.value = false
  isCollected.value = false
  isFollowing.value = false
  commentsError.value = false
  parallaxOffset.value = 0
  window.scrollTo({ top: 0 })
  fetchPost()
  fetchComments()
})
</script>

<style scoped>
/* ========================================
   Reading Progress Bar
   ======================================== */
.reading-progress-bar {
  position: fixed;
  top: 0;
  left: 0;
  height: 2px;
  background: var(--primary);
  z-index: 9999;
  transition: width 0.1s linear;
}

/* ========================================
   Page Layout — Editorial Split
   ======================================== */
.post-detail-page {
  position: relative;
  /* Subtle paper-grain texture on the page background */
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='g'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23g)' opacity='0.018'/%3E%3C/svg%3E");
}

.post-layout {
  display: flex;
  gap: var(--spacing-2xl);
  align-items: flex-start;
  max-width: var(--container-xl);
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  padding-top: var(--spacing-lg);
  padding-bottom: var(--spacing-4xl);
}

.post-main {
  flex: 1;
  min-width: 0;
  /* 680px reading column — ~65 chars wide, editorial standard */
  max-width: 680px;
  margin: 0 auto;
}

/* ========================================
   Hero Cover — Full Bleed
   ======================================== */
.hero-cover {
  position: relative;
  width: 100vw;
  margin-left: calc(-50vw + 50%);
  height: 520px;
  overflow: hidden;
}

.hero-cover-inner {
  position: absolute;
  inset: -80px 0;
  will-change: transform;
}

.hero-cover-inner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.05) 0%,
    rgba(0, 0, 0, 0.15) 60%,
    var(--background) 100%
  );
  pointer-events: none;
}

.hero-cover-grain {
  position: absolute;
  inset: 0;
  opacity: 0.025;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  pointer-events: none;
}

/* ========================================
   Back Button
   ======================================== */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  color: var(--text-muted);
  cursor: pointer;
  font-size: 0.8125rem;
  font-family: var(--font-sans);
  transition: all var(--transition);
  margin-bottom: var(--spacing-xl);
}

.back-btn:hover {
  color: var(--text-primary);
  border-color: var(--text-muted);
  transform: translateX(-3px);
}

/* ========================================
   Entrance Animation
   ======================================== */
.animate-in {
  animation: contentReveal 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes contentReveal {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========================================
   Post Article
   ======================================== */
.post-article {
  position: relative;
}

/* ========================================
   Post Header — Editorial Title
   ======================================== */
.post-header {
  margin-bottom: var(--spacing-2xl);
}

.post-tags-top {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  padding: 3px 12px;
  font-size: 0.6875rem;
  font-weight: 600;
  font-family: var(--font-sans);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: var(--radius-full);
  animation: tagSlideIn 0.4s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes tagSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(6px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* Editorial display title — Fraunces serif, tight tracking */
.post-title {
  font-family: var(--font-display);
  font-size: 3rem;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: -0.035em;
  margin-bottom: var(--spacing-lg);
  overflow-wrap: break-word;
  word-break: break-word;
  /* Optical size for large display — Fraunces variable axis */
  font-variation-settings: 'opsz' 72;
}

.post-meta-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
  font-size: 0.8125rem;
  font-family: var(--font-sans);
  color: var(--text-muted);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.meta-item svg {
  opacity: 0.5;
}

.meta-dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--text-muted);
  opacity: 0.4;
}

/* ========================================
   Author Card — Minimal Editorial
   ======================================== */
.author-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) 0;
  margin-bottom: var(--spacing-2xl);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  transition: all var(--transition);
}

.author-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  text-decoration: none;
}

.author-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.author-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  object-fit: cover;
  transition: all var(--transition);
}

.author-info:hover .author-avatar {
  box-shadow: 0 0 0 2px var(--primary);
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-weight: 600;
  font-size: 0.9375rem;
  font-family: var(--font-sans);
  color: var(--text-primary);
}

.author-label {
  font-size: 0.75rem;
  font-family: var(--font-sans);
  color: var(--text-muted);
}

.author-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.btn-follow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  font-size: 0.8125rem;
  font-weight: 500;
  font-family: var(--font-sans);
  background: var(--primary);
  color: white;
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition);
}

.btn-follow:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.btn-follow.is-following {
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border);
}

.btn-follow.is-following:hover:not(:disabled) {
  color: var(--primary);
  border-color: var(--primary);
}

/* ========================================
   Post Body — Editorial Typography
   ======================================== */
.post-body {
  padding-bottom: var(--spacing-2xl);
  border-bottom: 1px solid var(--border);
  margin-bottom: var(--spacing-xl);
}

/* Enhanced markdown styles — editorial reading experience */
.post-body :deep(.markdown-body) {
  font-family: var(--font-display);
  font-size: 1.125rem;
  line-height: 1.8;
  color: var(--text-primary);
  letter-spacing: 0.005em;
  /* Optical size for body text */
  font-variation-settings: 'opsz' 24;
}

.post-body :deep(.markdown-body h1),
.post-body :deep(.markdown-body h2),
.post-body :deep(.markdown-body h3),
.post-body :deep(.markdown-body h4) {
  position: relative;
  margin-top: 2.4em;
  margin-bottom: 0.6em;
  font-family: var(--font-display);
  font-weight: 700;
  line-height: 1.25;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.post-body :deep(.markdown-body h1) {
  font-size: 2rem;
  font-variation-settings: 'opsz' 48;
}

.post-body :deep(.markdown-body h2) {
  font-size: 1.625rem;
  padding-bottom: 0.5em;
  border-bottom: 1px solid var(--border);
  font-variation-settings: 'opsz' 36;
}

.post-body :deep(.markdown-body h3) {
  font-size: 1.375rem;
  font-variation-settings: 'opsz' 30;
}

.post-body :deep(.markdown-body h4) {
  font-size: 1.125rem;
  font-weight: 600;
}

.post-body :deep(.markdown-body p) {
  margin-bottom: 1.5em;
  font-family: var(--font-display);
  font-variation-settings: 'opsz' 24;
}

/* Blockquote — left accent line + soft background */
.post-body :deep(.markdown-body blockquote) {
  padding: var(--spacing-md) var(--spacing-lg);
  margin: 2em 0;
  border-left: 3px solid var(--primary);
  background: var(--primary-subtle);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--text-secondary);
  font-style: italic;
  font-family: var(--font-display);
}

.post-body :deep(.markdown-body blockquote p) {
  margin-bottom: 0.5em;
}

.post-body :deep(.markdown-body blockquote p:last-child) {
  margin-bottom: 0;
}

/* Code blocks — monospace, subtle dark background */
.post-body :deep(.markdown-body pre) {
  margin: 2em 0;
  padding: var(--spacing-lg);
  background: var(--gray-900);
  border: none;
  border-radius: var(--radius-md);
  overflow-x: auto;
  position: relative;
}

.post-body :deep(.markdown-body pre code) {
  color: var(--gray-200);
  font-family: var(--font-mono);
  font-size: 0.875rem;
  line-height: 1.75;
  background: transparent;
  letter-spacing: 0;
}

.post-body :deep(.markdown-body pre .copy-btn) {
  position: absolute;
  top: var(--spacing-sm);
  right: var(--spacing-sm);
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
  font-family: var(--font-sans);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-sm);
  color: var(--gray-400);
  cursor: pointer;
  opacity: 0;
  transition: all var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.post-body :deep(.markdown-body pre:hover .copy-btn) {
  opacity: 1;
}

.post-body :deep(.markdown-body pre .copy-btn:hover) {
  background: rgba(255, 255, 255, 0.16);
  color: var(--gray-100);
}

.post-body :deep(.markdown-body pre .copy-btn.copied) {
  background: var(--success);
  border-color: var(--success);
  color: white;
}

.post-body :deep(.markdown-body code) {
  padding: 0.15em 0.4em;
  font-size: 0.85em;
  font-family: var(--font-mono);
  background: var(--primary-subtle);
  border-radius: var(--radius-xs);
  color: var(--primary);
}

.post-body :deep(.markdown-body img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: 2em 0;
  box-shadow: var(--shadow-sm);
}

.post-body :deep(.markdown-body table) {
  width: 100%;
  margin: 2em 0;
  border-collapse: collapse;
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--border-solid);
}

.post-body :deep(.markdown-body th) {
  background: var(--bg-secondary);
  font-weight: 600;
  font-family: var(--font-sans);
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-solid);
  text-align: left;
  font-size: 0.875rem;
}

.post-body :deep(.markdown-body td) {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-solid);
  text-align: left;
  font-size: 0.875rem;
}

.post-body :deep(.markdown-body ul),
.post-body :deep(.markdown-body ol) {
  padding-left: 1.5em;
  margin: 1em 0;
}

.post-body :deep(.markdown-body li) {
  margin: 0.5em 0;
  line-height: 1.8;
}

.post-body :deep(.markdown-body li::marker) {
  color: var(--primary);
}

.post-body :deep(.markdown-body hr) {
  margin: 3em 0;
  border: none;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-strong), transparent);
}

.post-body :deep(.markdown-body a) {
  color: var(--primary);
  text-decoration: none;
  background-image: linear-gradient(var(--primary), var(--primary));
  background-size: 100% 1px;
  background-position: 0 100%;
  background-repeat: no-repeat;
  transition: background-size var(--transition);
}

.post-body :deep(.markdown-body a:hover) {
  background-size: 100% 2px;
  color: var(--primary-hover);
}

.post-body :deep(.markdown-body strong) {
  font-weight: 700;
  color: var(--text-primary);
}

/* ========================================
   Post Footer — Tags & Actions
   ======================================== */
.post-footer {
  padding-top: var(--spacing-lg);
}

.post-tags-bottom {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xl);
}

.tag-pill-ghost {
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--border);
  font-weight: 400;
  text-transform: none;
  letter-spacing: normal;
}

.tag-pill-ghost:hover {
  color: var(--primary);
  border-color: var(--primary);
}

/* Action buttons — clean editorial style */
.post-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) 0;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.875rem;
  font-family: var(--font-sans);
}

.action-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform var(--transition-spring);
}

.action-btn:hover:not(:disabled) .action-icon-wrap {
  transform: scale(1.1);
}

.action-btn:hover:not(:disabled) {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-subtle);
}

.action-btn.active {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

.action-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.action-label {
  font-weight: 600;
  font-size: 0.8125rem;
}

.action-divider {
  width: 1px;
  height: 20px;
  background: var(--border);
  margin: 0 var(--spacing-xs);
}

.action-btn-report {
  padding: 10px;
  border-color: transparent;
}

.action-btn-report:hover:not(:disabled) {
  color: var(--error);
  border-color: var(--error);
  background: var(--error-light);
}

/* ========================================
   Comment Section — Spacious Editorial
   ======================================== */
.comment-section {
  padding: var(--spacing-2xl) 0;
  margin-top: var(--spacing-xl);
}

.comment-header-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-family: var(--font-display);
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.01em;
}

.comment-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  font-size: 0.8125rem;
  font-weight: 700;
  font-family: var(--font-sans);
  background: var(--primary);
  color: white;
  border-radius: var(--radius-full);
}

/* Comment input */
.comment-input-wrap {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
  border-radius: var(--radius-lg);
  background: var(--surface);
  border: 1px solid var(--border);
  transition: all var(--transition);
}

.comment-input-wrap:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.reply-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--primary-subtle);
  border-radius: var(--radius) var(--radius) 0 0;
  margin: calc(-1 * var(--spacing-lg)) calc(-1 * var(--spacing-lg)) var(--spacing-md);
  border-bottom: 1px solid var(--border);
}

.reply-indicator-content {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8125rem;
  font-family: var(--font-sans);
  color: var(--primary);
}

.reply-indicator strong {
  font-weight: 600;
}

.cancel-reply-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}

.cancel-reply-btn:hover {
  color: var(--error);
  background: var(--error-light);
}

.comment-textarea {
  width: 100%;
  padding: var(--spacing-md);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  resize: vertical;
  min-height: 100px;
  font-size: 0.9375rem;
  font-family: var(--font-sans);
  background: transparent;
  color: var(--text-primary);
  transition: all var(--transition);
  line-height: 1.6;
}

.comment-textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.comment-textarea::placeholder {
  color: var(--text-muted);
}

.comment-input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--spacing-sm);
}

.char-count {
  font-size: 0.75rem;
  font-family: var(--font-sans);
  color: var(--text-muted);
  transition: color var(--transition);
}

.char-count.near-limit {
  color: var(--warning);
  font-weight: 600;
}

/* Login hint */
.login-hint {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
  border-radius: var(--radius-lg);
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-muted);
}

.login-hint svg {
  opacity: 0.35;
  flex-shrink: 0;
}

.login-hint-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9375rem;
  font-family: var(--font-sans);
}

.login-link {
  color: var(--primary);
  font-weight: 600;
  text-decoration: none;
}

.login-link:hover {
  text-decoration: underline;
}

/* Comment list */
.comment-list {
  display: flex;
  flex-direction: column;
}

.comment-items {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.comment-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-lg) 0;
  border-bottom: 1px solid var(--border);
  transition: all var(--transition);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar-link {
  flex-shrink: 0;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
  transition: all var(--transition);
}

.comment-item:hover .comment-avatar {
  box-shadow: 0 0 0 2px var(--primary-light);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.comment-author {
  font-weight: 600;
  font-size: 0.9375rem;
  font-family: var(--font-sans);
  color: var(--text-primary);
  text-decoration: none;
  transition: color var(--transition);
}

.comment-author:hover {
  color: var(--primary);
}

.comment-time {
  font-size: 0.75rem;
  font-family: var(--font-sans);
  color: var(--text-muted);
}

.comment-content {
  font-size: 0.9375rem;
  font-family: var(--font-sans);
  color: var(--text-primary);
  line-height: 1.7;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-sm);
}

.comment-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  font-family: var(--font-sans);
  color: var(--text-muted);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  transition: all var(--transition);
}

.comment-action-btn:hover {
  color: var(--primary);
  background: var(--primary-subtle);
}

.comment-action-delete:hover {
  color: var(--error);
  background: var(--error-light);
}

/* Reply list */
.reply-list {
  margin-top: var(--spacing-md);
  padding-left: var(--spacing-md);
  border-left: 2px solid var(--border);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.reply-item {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius);
  transition: all var(--transition);
}

.reply-item:hover {
  background: var(--primary-subtle);
}

.reply-avatar-link {
  flex-shrink: 0;
}

.reply-avatar {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-header-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 3px;
}

.reply-author {
  font-size: 0.8125rem;
  font-weight: 600;
  font-family: var(--font-sans);
  color: var(--text-primary);
  text-decoration: none;
}

.reply-author:hover {
  color: var(--primary);
}

.reply-time {
  font-size: 0.6875rem;
  font-family: var(--font-sans);
  color: var(--text-muted);
}

.reply-content {
  font-size: 0.8125rem;
  font-family: var(--font-sans);
  color: var(--text-primary);
  line-height: 1.6;
}

/* Empty state */
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  text-align: center;
  padding: var(--spacing-2xl);
  color: var(--text-muted);
}

.empty-hint p {
  margin: 0;
}

.error-hint {
  color: var(--error);
}

/* Comment list transitions */
.comment-list-enter-active {
  animation: commentSlideIn 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.comment-list-leave-active {
  animation: commentSlideOut 0.3s ease;
}

@keyframes commentSlideIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes commentSlideOut {
  to {
    opacity: 0;
    transform: translateX(-30px);
  }
}

/* ========================================
   Sidebar — Clean Editorial
   ======================================== */
.post-sidebar {
  width: 260px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  scrollbar-width: none;
}

.post-sidebar::-webkit-scrollbar {
  display: none;
}

/* Sidebar floating actions */
.sidebar-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md);
  border-radius: var(--radius-xl);
  background: var(--surface);
  border: 1px solid var(--border);
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.sidebar-actions.is-scrolled {
  opacity: 1;
  transform: translateX(0);
}

.sidebar-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px;
  width: 52px;
  background: transparent;
  border: none;
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  position: relative;
}

.sidebar-action-btn:hover {
  color: var(--primary);
  background: var(--primary-subtle);
}

.sidebar-action-btn.active {
  color: var(--primary);
}

.sidebar-action-btn.active::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--primary);
}

.sidebar-action-count {
  font-size: 0.6875rem;
  font-weight: 600;
  font-family: var(--font-sans);
  color: inherit;
}

.sidebar-action-divider {
  width: 24px;
  height: 1px;
  background: var(--border);
  margin: var(--spacing-xs) 0;
}

/* TOC card */
.toc-card,
.info-card {
  padding: var(--spacing-lg);
  border-radius: var(--radius-xl);
  background: var(--surface);
  border: 1px solid var(--border);
  transition: all var(--transition);
}

.toc-card:hover,
.info-card:hover {
  border-color: var(--border-strong);
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 0.8125rem;
  font-weight: 700;
  font-family: var(--font-sans);
  color: var(--text-secondary);
  margin: 0 0 var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--border);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.sidebar-title svg {
  opacity: 0.5;
}

/* TOC navigation */
.toc-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 360px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--border) transparent;
}

.toc-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 6px 10px;
  font-size: 0.8125rem;
  font-family: var(--font-sans);
  color: var(--text-muted);
  text-decoration: none;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
  position: relative;
}

.toc-indicator {
  width: 2px;
  height: 0;
  background: var(--primary);
  border-radius: var(--radius-full);
  transition: all var(--transition);
  flex-shrink: 0;
}

.toc-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toc-link:hover {
  color: var(--text-primary);
  background: var(--primary-subtle);
}

.toc-link:hover .toc-indicator {
  height: 60%;
}

.toc-link.active {
  color: var(--primary);
  font-weight: 600;
  background: var(--primary-subtle);
}

.toc-link.active .toc-indicator {
  height: 100%;
}

.toc-link.level-2 { padding-left: var(--spacing-md); }
.toc-link.level-3 { padding-left: var(--spacing-lg); }
.toc-link.level-4 { padding-left: var(--spacing-xl); }

/* Info card grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-sm);
}

.info-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: var(--spacing-md) var(--spacing-sm);
  border-radius: var(--radius);
  transition: all var(--transition);
}

.info-stat:hover {
  background: var(--primary-subtle);
}

.info-stat-value {
  font-size: 1.25rem;
  font-weight: 800;
  font-family: var(--font-sans);
  color: var(--text-primary);
  line-height: 1;
}

.info-stat-label {
  font-size: 0.6875rem;
  font-family: var(--font-sans);
  color: var(--text-muted);
  font-weight: 500;
}

/* ========================================
   Mobile Floating Actions
   ======================================== */
.mobile-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: none;
  align-items: center;
  justify-content: space-around;
  padding: var(--spacing-sm) var(--spacing-md);
  padding-bottom: calc(var(--spacing-sm) + env(safe-area-inset-bottom, 0px));
  background: var(--surface);
  border-top: 1px solid var(--border);
  z-index: var(--z-sticky);
  transform: translateY(100%);
  transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.mobile-actions.visible {
  transform: translateY(0);
}

.mobile-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 8px 16px;
  background: none;
  border: none;
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.6875rem;
  font-weight: 500;
  font-family: var(--font-sans);
}

.mobile-action-btn:active {
  transform: scale(0.92);
}

.mobile-action-btn.active {
  color: var(--primary);
}

/* ========================================
   Loading Skeleton
   ======================================== */
.loading-state {
  padding: var(--spacing-2xl);
  border-radius: var(--radius-xl);
  background: var(--surface);
  border: 1px solid var(--border);
}

.loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.skeleton-line {
  height: 16px;
  background: linear-gradient(90deg, var(--skeleton-base) 0%, var(--skeleton-highlight) 50%, var(--skeleton-base) 100%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
  border-radius: var(--radius-sm);
  width: 100%;
}

.skeleton-title {
  height: 36px;
  width: 80%;
  margin-bottom: var(--spacing-sm);
}

.skeleton-subtitle {
  height: 20px;
  width: 50%;
}

.skeleton-avatar-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin: var(--spacing-md) 0;
}

.skeleton-circle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(90deg, var(--skeleton-base) 0%, var(--skeleton-highlight) 50%, var(--skeleton-base) 100%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
  flex-shrink: 0;
}

.skeleton-name {
  width: 120px;
  height: 20px;
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ========================================
   Error State
   ======================================== */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-4xl) var(--spacing-xl);
  border-radius: var(--radius-xl);
  background: var(--surface);
  border: 1px solid var(--border);
  text-align: center;
}

.error-icon-wrap {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--error-light);
  color: var(--error);
  opacity: 0.7;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.error-state h2 {
  color: var(--text-primary);
  font-size: 1.25rem;
  font-family: var(--font-display);
  margin: 0;
}

.error-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.btn-outline {
  padding: 0.5rem 1.25rem;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  font-family: var(--font-sans);
  transition: all var(--transition);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.btn-outline:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-subtle);
}

/* ========================================
   Responsive Design
   ======================================== */
@media (max-width: 1200px) {
  .post-sidebar {
    width: 220px;
  }
}

@media (max-width: 992px) {
  .post-layout {
    flex-direction: column;
    padding-left: var(--spacing-lg);
    padding-right: var(--spacing-lg);
  }

  .post-main {
    max-width: none;
    margin: 0;
  }

  .post-sidebar {
    width: 100%;
    position: static;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-md);
    max-height: none;
    overflow: visible;
  }

  .sidebar-actions {
    grid-column: 1 / -1;
    flex-direction: row;
    justify-content: center;
    opacity: 1;
    transform: none;
  }

  .sidebar-action-btn {
    flex-direction: row;
    width: auto;
    gap: 6px;
    padding: 8px 14px;
  }

  .sidebar-action-divider {
    width: 1px;
    height: 24px;
    margin: 0 var(--spacing-xs);
  }
}

@media (max-width: 768px) {
  .post-layout {
    padding-left: var(--spacing-md);
    padding-right: var(--spacing-md);
  }

  .hero-cover {
    height: 320px;
  }

  .post-title {
    font-size: 2.25rem;
  }

  .post-meta-row {
    font-size: 0.75rem;
  }

  .author-card {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: flex-start;
    padding: var(--spacing-md) 0;
  }

  .author-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .post-actions {
    flex-wrap: wrap;
    gap: var(--spacing-xs);
  }

  .action-btn {
    padding: 8px 12px;
    font-size: 0.8125rem;
  }

  .post-sidebar {
    grid-template-columns: 1fr;
  }

  .mobile-actions {
    display: flex;
  }

  .comment-section {
    padding: var(--spacing-xl) 0;
  }

  .comment-item {
    padding: var(--spacing-md) 0;
  }

  .post-body :deep(.markdown-body) {
    font-size: 1rem;
  }

  .post-body :deep(.markdown-body pre) {
    padding: var(--spacing-md);
    border-radius: var(--radius);
  }
}

@media (max-width: 480px) {
  .hero-cover {
    height: 240px;
  }

  .post-title {
    font-size: 1.75rem;
  }

  .post-header {
    margin-bottom: var(--spacing-lg);
  }

  .comment-input-wrap {
    padding: var(--spacing-md);
  }

  .comment-textarea {
    min-height: 80px;
    font-size: 0.875rem;
  }
}
</style>
