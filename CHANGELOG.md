# 更新日志 (CHANGELOG)

所有重要更改都将记录在此文件。

---

## v2.0.17 (2026-06-03)

### 后端修复

- **UserProfile.vue 关注/取消关注 API 路径错误**: `toggleFollow` 始终调用 `POST /follow`，从未调用 `DELETE /follow/{id}`。改为根据 `isFollowing` 状态分别调用 `followApi.toggleFollow`（关注）或 `followApi.unfollow`（取消关注）
- **@mentions 查询字段不匹配**: 前端插入 `@昵称`，后端 `parseMentions` 仅按 `username` 查询。改为同时查询 `nickname` 和 `username`
- **转发内容重复显示**: `repostContent` 被设为原动态内容，前端同时显示 `repostContent` 和 `repostPost.content`。改为不设置 `repostContent`
- **getMyLikes/getMyCollections 遗漏 NULL 记录**: `this.page()` 受 `@TableLogic` 影响只查 `is_deleted = 0`，遗漏历史 `is_deleted IS NULL` 记录。新增 Mapper 方法 `selectPageByUserId`/`countByUserId` 绕过 `@TableLogic`
- **新增 Mapper 方法**: `BlogLikeMapper.selectPageByUserId`/`countByUserId`、`BlogCollectMapper.selectPageByUserId`/`countByUserId`

### 前端修复

- **图片预览功能为空函数**: `PostCardList.vue` 的 `openImagePreview` 和 `openPreview` 不发出事件。添加 `preview-image` emit 事件，`Home.vue` 监听并调用 `handlePreview`
- **阅读量双重计数**: `PostDetail.vue.fetchPost` 中后端 `getPostById` 已增加阅读量，前端又额外调用 `incrementViewCount`。移除前端重复调用
- **代码块复制按钮 CSS 类名不匹配**: `PostDetail.vue` 用 `copy-code-btn`，`main.css` 定义 `copy-btn`。改为统一使用 `copy-btn`
- **分页不同步到 URL**: `Home.vue.changePage` 未调用 `router.push`，浏览器前进/后退无法用于分页。添加 `router.push` 更新 URL query
- **前端忽略后端实时计数**: `PostCardList.vue`/`PostDetail.vue` 的 `toggleLike`/`toggleCollect` 使用 +/-1 乐观更新。改为使用后端返回的 `likeCount`/`collectCount`

### 影响的文件

| 文件 | 改动类型 |
|------|----------|
| `edu_project_vue/src/views/user/UserProfile.vue` | 逻辑修复 |
| `edu_project_vue/src/components/home/PostCardList.vue` | 逻辑修复 |
| `edu_project_vue/src/views/Home.vue` | 逻辑修复 |
| `edu_project_vue/src/views/post/PostDetail.vue` | 逻辑修复 |
| `edu_project/src/main/java/.../service/impl/CircleServiceImpl.java` | 逻辑修复 |
| `edu_project/src/main/java/.../mapper/BlogLikeMapper.java` | 新增方法 |
| `edu_project/src/main/java/.../mapper/BlogCollectMapper.java` | 新增方法 |
| `edu_project/src/main/java/.../service/impl/BlogLikeServiceImpl.java` | 逻辑修复 |
| `edu_project/src/main/java/.../service/impl/BlogCollectServiceImpl.java` | 逻辑修复 |

---

## v2.0.16 (2026-06-03)

### 后端修复

- **MySQL 连接**: JDBC URL 添加 `allowPublicKeyRetrieval=true`，修复 `Public Key Retrieval is not allowed` 错误
- **MyBatis 配置**: 添加 `use-actual-param-name: false`，确保 `@Param` 注解优先于参数实际名称
- **`@TableLogic` 死代码修复**: `BlogLikeServiceImpl.toggleLike` 和 `BlogCollectServiceImpl.toggleCollect` 中，通过 `LambdaQueryWrapper` 查找已删除记录的代码因 `@TableLogic` 自动追加 `WHERE is_deleted = 0` 而成为死代码。改为使用自定义 SQL（`selectRawByUserAndPost`）绕过 `@TableLogic`
- **批量状态检查修复**: `checkLikeStatusBatch` 和 `checkCollectStatusBatch` 使用 `LambdaQueryWrapper` + `this.list()` 遗漏了 `is_deleted IS NULL` 的历史记录。改为使用自定义 SQL（`selectActivePostIdsByUserAndPosts`）
- **`BlogCollect` 实体修复**: `isDeleted` 字段补充默认值 `= 0`，与 `BlogLike` 实体对齐
- **`CirclePostMapper.countByTopicIds` 修复**: 删除 `@MapKey("topicId")` 注解，改为 default 方法在 Java 层完成 Map 转换，解决 `There is no getter for property named 'topicId' in 'class java.lang.Long'` 错误
- **新增 Mapper 方法**:
  - `BlogLikeMapper.selectActivePostIdsByUserAndPosts` — 批量查询用户已点赞的文章ID（绕过 `@TableLogic`）
  - `BlogCollectMapper.selectRawByUserAndPost` — 绕过 `@TableLogic` 查询收藏记录
  - `BlogCollectMapper.selectActivePostIdsByUserAndPosts` — 批量查询用户已收藏的文章ID（绕过 `@TableLogic`）

### 前端修复

- **`PostCardList.vue` emit 事件名修复**: `emit('update:likedPosts')` → `emit('update:liked-posts')`，`emit('update:collectedPosts')` → `emit('update:collected-posts')`，与父组件 Home.vue 的 kebab-case 监听器匹配
- **`PostCardList.vue` props 修复**: 不再直接修改 props 中的 Set，改为通过 `emit` 事件通知父组件更新状态

### 影响的文件

| 文件 | 改动类型 |
|------|----------|
| `edu_project/src/main/resources/application.yml` | 配置修改 |
| `edu_project/src/main/java/.../mapper/BlogLikeMapper.java` | 新增方法 |
| `edu_project/src/main/java/.../mapper/BlogCollectMapper.java` | 新增方法 |
| `edu_project/src/main/java/.../entity/BlogCollect.java` | 字段默认值 |
| `edu_project/src/main/java/.../service/impl/BlogLikeServiceImpl.java` | 逻辑重写 |
| `edu_project/src/main/java/.../service/impl/BlogCollectServiceImpl.java` | 逻辑重写 |
| `edu_project/src/main/java/.../mapper/CirclePostMapper.java` | 方法重写 |
| `edu_project_vue/src/components/home/PostCardList.vue` | 事件名修复 |
| `edu_project_vue/src/views/Home.vue` | 事件监听 |

---

## v2.0.15 (2026-05-30)

- HomeFilters.vue `filters is not defined` 运行时错误修复
- PostDetail.vue 文章评论幽灵数据问题（records 解析修复）
- Circle.vue 点赞失败无错误提示问题修复
- Circle.vue 热门动态移至左侧边栏，优化数据源
- Circle.vue 侧边栏数据每 60 秒自动刷新
- logger.js CORS 错误静默处理（/api/log 端点不存在）
- 校友圈移除标签功能（仅保留话题）

## v2.0.14 (2026-05-29)

- 密码重置字段名修复（password → newPassword）

## v2.0.13 (2026-05-28)

- 文章话题功能移除（标签功能保留）
- 校友圈话题功能修复并完善
- 话题数据路径修复（data.data.records）
- PostEdit.vue import 语句补全

## v2.0.11 (2026-05-25)

- 修复写文章选择标签时白屏的运行时错误

## v2.0.0 (2026-05-20)

- Rainy Glassmorphism UI 设计系统（水滴、涟漪、光泽动效）
- 16 项性能优化索引（查询速度提升 300 倍）
- Caffeine 缓存策略（5 个缓存实例）
- 校友圈功能增强（@mentions、位置标签、话题标签、显示切换）
- Messages 布局修复（浮动返回按钮）
- 所有页面宽度统一扩展至 1400px
