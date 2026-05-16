# 变更日志

## v1.49 - 2026-05-16

### 🐛 前端 Bug 修复

#### 编译错误修复 (3处)
- **PostEdit.vue** - 修复模板表达式中反引号/双引号导致 Vue 编译器解析失败
  - `insertMarkdown('\`', '\`')` → `insertCode()` 函数
  - `insertMarkdown('\`\`\`\n', '\n\`\`\`')` → `insertCodeBlock()` 函数
  - `insertMarkdown('<div style="...">', '</div>')` → `insertAlignLeft/Center/Right()` 函数
- **Toast.vue** - 修复 `<TransitionGroup>` 在 `<Teleport>` 中导致 build 失败的问题

#### 私信功能修复 (CRITICAL)
- **Messages.vue** - 修复私信无法使用、私信页面展示错误的多项问题
  - 会话匹配：使用 `user.id` 而非 `conversationId` 匹配，解决从用户主页跳转私信不显示的问题
  - 新建会话：若无历史会话自动创建虚拟会话并获取用户信息
  - 发送消息：修复 `receiverId` 使用正确用户ID而非会话ID
  - 消息顺序：后端返回倒序，前端反转后正确展示（旧→新）
  - 消息发送后自动刷新获取真实会话ID
  - 多余 `}` 和重复代码块导致页面空白的问题
- **MessageServiceImpl.java** - 后端返回消息按时间倒序排列（最新在前），前端反转展示
- **MessageVO.java** - 添加缺失的 `senderId`/`receiverId` 字段
- **message.js** (API) - 补充4个缺失的私信API端点（received/sent/read/delete）

#### Back按钮修复
- **PostDetail.vue** - 修复返回按钮在 grid 布局中不显示（添加 `grid-column: 1 / -1`）
- **Profile.vue** - 修复返回按钮在 2 列 grid 布局中不显示
- **PostEdit.vue** - 添加缺失的"返回"按钮
- **CirclePost.vue** - 添加缺失的"返回"按钮
- **UserProfile/Messages.vue** - 添加 `goBack()` 函数（history 回退，无历史时跳首页）

#### PostEdit.vue 编辑器修复
- **Undo/Redo** - 修复 Undo/Redo 历史记录不生效问题（`saveHistory()` 改为在修改内容前调用）
- **自动保存** - 添加打字 2 秒自动保存到历史记录
- **草稿加载** - 支持从草稿列表跳转的 `?draft=draftId` 查询参数；加载草稿时补全tags/封面图
- **标签创建** - 修复无标签时"创建标签"按钮不显示的问题（条件从 `allTags.length === 0` 改为输入框有内容）
- **历史初始化** - 加载草稿/文章后正确初始化 Undo 历史

#### 其他前端修复
- **PostDetail.vue** - 添加从文章详情直接发私信的"私信"按钮；修复 `likeCount`/`collectCount` 可能变成 `NaN` 的问题
- **Toast.vue** - 修复鼠标悬停暂停再恢复时进度条跳转到100%的问题
- **Modal.vue** - 修复多模态框叠加时 body scroll 锁定计数不共享的问题
- **UserProfile.vue** - 切换用户 profile 时重置校友圈数据，避免显示上一个用户的动态
- **Profile.vue** - 切换 Tab 后自动重试加载（err状态下），添加 `goBack()` 函数
- **Followers.vue** - 修复取消关注实际调用关注API的问题（添加 `isFollowing` 判断）
- **MyReports.vue** - 修复 `targetType` 大小写不匹配后端、举报状态映射反转
- **Search.vue** - 添加搜索建议 `@blur` 关闭；修复 `--bg-secondary` CSS变量缺失
- **Statistics.vue** - 删除不存在的 `stats.tagStats?.totalTags` 引用
- **TrendingPage.vue** - 修复分页判断逻辑（使用后端 `pages` 替代 `records.length`）
- **PostSearch.vue** - 添加加载中spinner动画
- **PostEdit.vue** - 图片上传后添加undo历史记录

#### API 层补充
- **media.js** - 补充6个缺失的媒体API端点（batch upload/delete/getById/bind/getByPost/list）
- **circle.js** - 补充2个缺失的校友圈API端点（delete/search）
- **message.js** - 补充4个缺失的私信API端点（received/sent/read/delete）
- **stores/app.js** - 创建缺失的 Pinia app store（sidebarCollapsed/globalLoading/onlineStatus）

#### CSS 修复
- **main.css** - 添加全局缺失的 `--bg-secondary` CSS变量（light/dark模式）
- **Navbar.vue** - 修复主题切换按钮缩进

### 🐛 后端 Bug 修复

#### CRITICAL - 点赞/收藏无法重复操作
- **BlogLikeServiceImpl** - 修复 `toggleLike()` 未过滤 `is_deleted=1` 导致取消点赞后无法再次点赞的问题
  - 查询时过滤软删除记录；软删除记录走恢复路径
  - `getMyLikes()` / `checkLikeStatusBatch()` 补充 `isDeleted` 过滤
- **BlogCollectServiceImpl** - 同上修复收藏toggle、列表、批量检查

#### CRITICAL - 登录锁定 SQL 语法错误
- **SysUserMapper.java** - `DATE_ADD(NOW(), #{lockMinutes}, 'MINUTE')` → `DATE_ADD(NOW(), INTERVAL #{lockMinutes} MINUTE)`（缺少 INTERVAL 关键字，会导致SQL运行时异常）

#### 私信功能修复
- **MessageVO.java** - 添加 `senderId`/`receiverId` 字段（`BeanUtils.copyProperties` 静默丢失）
- **MessageServiceImpl.java** - 会话列表按 `lastMessageTime` 降序排序，添加空值保护

#### 用户/关注修复
- **UserConverter.java** - 补充 `email` 字段复制（UserVO.email 恒为 null）
- **FollowController.java** - 修复 `checkFollow` 返回当前用户的 followingCount 而非目标用户的
- **UserVO.java** - 添加 `@JsonProperty("userId") getUserId()` 方法，兼容前端 `item.userId` 引用

#### 认证/安全修复
- **JwtAuthenticationFilter.java** - 4项修复：
  - 客户端 `X-Trace-Id` 未被存入 MDC
  - MDC 未在请求结束后清理（内存泄漏）
  - `/api/user/refresh` 路径重复检查
  - catch 块中 `filterChain.doFilter()` 可能被调用两次
- **SecurityConfig.java** - `GET /statistics/community` 和 `GET /media/post/**` 添加 `permitAll()` 公开访问
- **MediaController.java** - 移除 `getPostMedia()` 上不必要的登录检查

#### 其他后端修复
- **TrendingServiceImpl.java** - 修复 `viewCount`/`likeCount`/`commentCount` 可能为 null 时的 NPE
- **CollectStatusVO.java** - 添加缺失的 `collectCount` 字段

### ✨ 新功能
- **PostDetail.vue** - 文章详情页添加"私信"按钮，点击跳转 `/messages?userId=X`

### 📝 文档更新
- CHANGELOG.md - 追加 v1.49 完整变更记录
- 版本号同步更新至 v1.49

## v1.50 - 2026-05-16

### 🐛 导航白屏修复 (CRITICAL)
- **App.vue** - 修复页面跳转时只显示顶栏、下方白屏的问题
  - 添加 `onErrorCaptured` 错误边界，捕获路由组件加载失败并显示重试按钮
  - 替换 3px 进度条为居中全屏加载动画（spinner + "加载中..."）
  - 添加 `@enter-cancelled` / `@leave-cancelled` 事件处理，防止加载状态卡死
  - 导航开始时立即重置滚动位置
- **router/index.js** - 添加 `router.onError` 全局处理异步组件加载失败（显示 toast + 跳转首页）

### 🐛 私信功能深度修复
- **Messages.vue** - 10项修复：
  - `sender.id` 类型比较 `===` → `==`（Long vs String 匹配失败）
  - 空消息区域添加 `min-height: 200px` 防止折叠
  - 默认头像改为内联 SVG data-URI + `@error` 降级处理
  - `sendMessage` 提取 `trim()` 后的 content，确保结构匹配后端
  - 抽取 `scrollToBottom()` 辅助函数，统一初始/发送/轮询时滚动
  - 活跃会话高亮：虚拟会话时按 `user.id` 比较
  - 轮询时同时刷新活跃会话消息（仅用户已在底部时自动滚动）
  - 发送失败后 `finally` 清空输入框
- **main.js** - Pinia 初始化提前到 `app.use(router)` 之前，消除路由守卫竞态条件

### 🐛 基础设施修复
- **api/index.js** - 添加请求失败自动重试机制（2次重试 + 指数退避）+ 错误消息传播
- **stores/user.js** - `updateUserInfo` 添加字段白名单过滤，防止敏感字段（password）被存储
- **router/index.js** - 添加导航失败检测

### 🎨 全页面显示优化

#### Navbar/App/Home
- **App.vue** - 移除多余的 `padding-top` media query 覆盖，统一 60px 匹配导航栏高度
- **Home.vue** - 修复筛选 Tab 指示器对齐；修复侧边栏 sticky 位置
- **Navbar.vue** - 所有未读数徽章统一为 `99+` 封顶显示

#### 文章相关页面
- **PostDetail.vue** - 评论区和侧边栏条件渲染（无文章时隐藏）；分享计数即时更新；长标题换行处理；移动端响应式（封面高度/内边距）
- **PostEdit.vue** - 长标题换行处理；移动端编辑器高度从 460px 降到 300px；工具栏横向滚动支持
- **PostSearch.vue** - 硬编码颜色/间距替换为 CSS 变量

#### 用户页面
- **Profile/Collections/Following.vue** - 移除 40-50 行未使用的模态框 CSS
- **PasswordChange.vue** - 硬编码颜色 → `var(--error)`
- **MyReports.vue** - 错误/空状态改为卡片+图标；修复分页；移动端适配
- **Notifications.vue** - 加载骨架屏；错误/空状态卡片化；移动端适配

#### 校友圈页面
- **Circle.vue** - textarea 添加 `box-sizing: border-box`
- **CircleDetail.vue** - 4图grid修复为2列；转发模态添加字符限制；转发卡片添加缩略图
- **CirclePost.vue** - 文本域自动高度调整

#### 管理后台
- **Dashboard/Users/Posts/Reports/Statistics.vue** - 骨架屏硬编码颜色 → CSS变量；错误状态颜色修复；`empty-cell` 样式补充

#### 其他
- **Search.vue** - 搜索中状态改为骨架屏；错误颜色替换
- **TrendingPage.vue** - 13处暗色模式修复（所有硬编码颜色 → CSS变量）
- **Toast.vue** - z-index 提升至 11000（确保在 Modal 之上）；永久 toast 进度条全宽显示
- **Modal.vue** - `show` watcher 添加 `immediate: true`，修复初始为 true 时不锁定滚动

### 🔧 后端验证
- **MessageController/MessageServiceImpl** - 验证所有字段与前端匹配（ConversationVO.conversationId = partner userId，MessageVO.sender，顺序反转等）

### 📝 文档更新
- CHANGELOG.md - 追加 v1.50 完整变更记录
- 版本号同步更新至 v1.50

## v1.48 - 2026-05-15

### 🔒 安全修复
- **验证码并发竞态** - `EmailServiceImpl.verifyCode()` 添加 synchronized 同步块，防止同一验证码被多次使用

### 🐛 Bug 修复
- **前端数据显示** - `Home.vue` 修复热门标签/文章/统计数据访问方式
  - `getHotTags`: `response?.records`（原 `response?.data?.records`）
  - `getHotPosts`: `response?.records`（原 `response.data.records`）
  - `getCommunityStats`: `response`（原 `response.data`）

### 📝 架构设计固化
- **不使用Redis** - 确认使用 Caffeine 本地缓存，文档明确标注
- **内容直接发布** - 用户发布文章 `status=1` 直接可见，无需审核流程

### 📝 文档更新
- README.md - 新增"核心设计"章节，固化两条设计要求
- CLAUDE.md - 更新缓存策略说明
- 版本号统一更新至 v1.48

## v1.47 - 2026-05-15

### 🐛 Bug 修复

#### 前后端联调
- **批量状态检查** - `Home.vue` 修复 `checkLikeStatusBatch/checkCollectStatusBatch` 响应按索引匹配
- **Refresh Token解包** - `api/index.js:132` 修复 `response.data.data` → `response.data`
- **HTTP方法不匹配** - `api/media.js:45` POST → PUT 匹配后端 `@PutMapping`
- **草稿列表API路径** - `api/post.js:71` `/draft/my` → `/post/draft/my`
- **sendRegisterCode请求体** - `api/user.js` 补充 `username` 参数匹配后端 `@NotBlank` 校验

#### 字段名不匹配
- **PostDetail.vue** - `post.userAvatar` → `post.avatar`
- **Profile.vue** - 点赞列表字段全修正(`like.id`→`like.likeId`, `postTitle`→`title`, `postContent`→`summary`, `createTime`→`likeTime`)
- **Profile/UserProfile.vue** - 文章摘要 `post.content` → `post.summary`
- **Circle.vue** - `nickname/username` → `userNickname/userUsername`
- **CircleDetail.vue** - 评论用户13处字段从扁平改为 `comment.user?.xxx`
- **Messages.vue** - `response.data` → `response.data?.records`(Page解包)；`partnerUserId` → `conversationId`
- **UserProfile.vue** - 统计数字加 `|| 0` 保护

#### 功能修复
- **头像URL安全过滤** - `utils/index.js` `isSafeUrl()` 增加相对路径支持(`url.startsWith('/')`)
- **Circle发布后消失** - `Circle.vue` 发布后切换到推荐tab刷新；`fetchPosts` 加强响应格式兼容
- **Profile封面图上传承诺** - `Profile.vue` 添加封面图上传按钮(相机图标悬浮)
- **Profile校友圈Tab** - 后端新增 `GET /circle/user/{userId}` 端点 + 前端 Tab 展示
- **handleAuthError不清空store** - `api/index.js` 401处理增加 Pinia store 清空

### ✨ 新功能
- **通用组件库** - 创建7个组件: Skeleton(6种类型), EmptyState, Modal, ImagePreview, PostCard, PageTransition, useConfirm
- **Toast增强** - 进度条指示器、鼠标悬停暂停、操作按钮支持
- **sort排序支持** - `PostQueryRequest` 新增 `sort` 字段, `getPostList` 支持 latest/hot/essence 三种排序
- **草稿列表后端端点** - `BlogPostController` 新增 `GET /post/draft/my` 端点

### 🎨 前端美化
- **Home.vue** - iOS风格筛选滑块、卡片入场stagger动画、悬浮阴影增强、点赞/收藏pop动画、TransitionGroup切换
- **Circle.vue** - 卡片式feed流、图片grid布局、无限滚动(IntersectionObserver)、发布弹窗可见性选择、点赞optimistic update
- **Profile.vue** - 封面Hero区200px渐变、头像88px白边、四栏统计、Tab滑条动画、骨架屏/空状态/错误+重试
- **UserProfile.vue** - 封面区、关注loading态、粉丝数实时增减、分页骨架屏
- **ProfileEdit.vue** - 头像悬浮相机遮罩、浮动标签、字数统计、loading动画
- **Drafts/Collections/Following/Followers.vue** - 卡片布局、分页、确认对话框、骨架屏

### 🔧 后端增强
- **FULLTEXT全文索引** - `数据库表.sql` 添加 `ft_post_title_content` 索引
- **配置key修复** - `application.yml` 添加 `avatar.allowed-domains` 映射
- **CircleController** - 新增 `GET /circle/user/{userId}` 端点(用户动态列表)
- **CircleService** - 新增 `getUserPosts` 方法(含可见性过滤)
- **SecurityConfig** - 清理未使用的 `@Bean` 方法(-39行)

### 📝 文档更新
- `campus_blog.md` - 修复密码找回API路径；更新实体/表/Mapper数量(22)
- `README.md` - 补充 `blog_share` 表、版本号
- `CLAUDE.md` - 修复引用路径、表数量
- `.env.example` - 补充 `AVATAR_ALLOWED_DOMAINS` 变量

### ⚡ 性能优化
- **速率限制** - `RateLimitInterceptor` 从4个接口扩展到14个(阅读/发布/评论/点赞/上传/私信等)
- **关注isDeleted过滤** - `FollowServiceImpl` 查询补充软删除过滤
- **管理员列表过滤** - `SysUserServiceImpl` 补充 `is_deleted` 和 `status` 过滤

### 🔒 安全修复
- **昵称/bio XSS** - `SysUserServiceImpl` 增加 `htmlSanitizer.sanitizePlainText()` 清洗
- **like/check权限** - `SecurityConfig` 移除 `permitAll`，改为需登录
- **统计数据权限** - `SecurityConfig` statistics 改为 `.authenticated()`
- **角色信息隐藏** - `UserConverter` `toUserVO` 隐藏精确角色
- **封面图URL验证** - `SysUserServiceImpl` 增加URL格式校验

## v1.45 - 2026-05-14

### 📝 文档清理与更新
- **删除过时文件** - 移除空的 `sql/` 目录
- **15个子代理全面检查** - 对项目所有模块进行并行深度审查
- **6个补充检查** - 额外维度验证确保完整性
- **README.md** - 版本号更新至 v1.45
- **CLAUDE.md（后端）** - 开发状态补充 **设备管理**
- **CLAUDE.md（前端）** - 页面组件数量修正 (38→33)、API模块数修正 (16→17)
- **campus_blog.md** - 版本号与文档版本同步更新至 v1.45
- **CHANGELOG.md** - 追加本轮变更记录

### 🐛 Bug 修复
- **Token刷新** - 修复 `api/index.js` 响应解包逻辑
- **主题切换** - 修复暗色模式状态持久化
- **限流配置** - 修复 `RateLimitInterceptor` 配置项兼容性

### 🔒 安全修复 v2.0 (20个Agent审计修复)

#### 高优先级修复
- **NPE空指针风险** - `SysUserServiceImpl`、`ReportServiceImpl` 等6处使用 `Objects.equals()` 替代直接 `equals()` 调用
- **JwtException吞没** - `SysUserController` catch块改为 `log.warn + throw BusinessException(401)`
- **管理员自我封禁** - `AdminUserController` 添加 `id != adminId` 检查防止管理员封禁自己
- **事件事务发布** - `BlogLike/BlogCollect/BlogComment/FollowServiceImpl` 改为 `TransactionSynchronizationManager.afterCommit()` 发布事件
- **SaveDraftRequest暴露字段** - 删除 `createTime`/`updateTime` 字段，由服务端自动管理
- **selectHotPosts无LIMIT** - `BlogTrendingMapper` 添加分页支持
- **前端Token存储** - `api/index.js` 增加Token指纹验证和XSRF Token防护

#### 配置增强
- **BCrypt强度可配置** - `SecurityConfig` 改为 `@Value("${bcrypt.strength:12}")` 配置
- **限流阈值可配置** - `RateLimitProperties` 扩展为23个接口的限流配置，`RateLimitInterceptor` 完全使用配置值

#### 编译修复
- **SysUserController日志** - 添加 `@Slf4j` 注解修复 `log` 字段缺失

### 🎨 前端安全增强
- **Token指纹验证** - 即使XSS窃取Token，攻击者也无法在第三方域名使用
- **XSRF Token** - 提供额外的CSRF防护层
- **Token异常检测** - 可检测Token是否被篡改

## v1.46 - 2026-05-15

### 🔒 安全修复 v2.1
- **私信权限检查** - `MessageServiceImpl.markAsRead()` 确认已有 receiver 验证
- **统计数据权限** - `/statistics/**` 确认只需登录即可访问公开社区数据（设计正确）

### 📝 文档更新
- **版本同步** - README.md、CLAUDE.md 版本号保持一致
