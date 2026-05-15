# 变更日志

## v1.44 - 2026-05-13

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
