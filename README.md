# Call of Duty Build Manager

一个用于整理《Call of Duty》武器、配件和配装的小型个人管理工具。

它不是完整的 Gunsmith 模拟器，也不是多人协作平台。它更像一个本地优先的资料库：你可以在后台维护武器和配件数据，在公开页面浏览武器库，也可以在当前浏览器里保存自己的配装思路。

## 适合谁

- 想把常用武器、配件、推荐配装整理成可浏览资料库的玩家。
- 想维护一份个人 COD 配装记录，而不是散落在截图、笔记和聊天记录里的玩家。
- 想要一个前后端分离的轻量 Web 项目作为个人管理工具继续扩展的开发者。

## 现在能做什么

- 浏览武器库，并按名称、武器分类、代际、收藏状态筛选。
- 查看单把武器的可用配件和推荐配装。
- 在浏览器本地创建、编辑、删除单代际个人配装。
- 登录后台维护武器、配件、属性词条和推荐配装。
- 在后台维护“某把武器可使用哪些配件”的绑定关系。
- 配置基础设置，例如站点标题和默认代际筛选。

## 三个使用区域

### Public 浏览

公开页面不需要登录，主要用于查看已经维护好的资料：

- 武器列表
- 武器详情
- 可用配件
- 推荐配装
- 推荐配装详情

这些内容来自后端数据库，适合作为自己的 COD 武器资料库。

### 本地配装

“我的配装”保存在当前浏览器的 `localStorage` 中，不需要登录，也不会写入后端用户系统。

这部分适合记录自己的临时想法、个人方案和还不想放进后台推荐列表的配装；每条配装只属于一个代际。

### 后台管理

后台页面需要登录，用来维护会进入公共浏览区的数据：

- 武器管理
- 配件管理
- 武器配件绑定
- 属性词条
- 推荐配装
- 后台设置

后台推荐配装会持久化到 MySQL，并在公开页面中展示。每条推荐配装只属于一个代际。

## 当前范围

这个项目当前定位是中小型个人 Web 应用，优先保证清楚、可维护、能日常使用。

当前不包含：

- 完整 Gunsmith 数值模拟
- 多用户个人空间
- OAuth / 第三方登录
- 桌面端文件导入导出
- 云同步或协作功能

## 技术概览

- 前端：Vue 3、TypeScript、Vite、Pinia、Vue Router、Naive UI、Tailwind CSS
- 后端：Spring Boot、Spring Security、MyBatis、MySQL、Redis
- Java：JDK 21

这里的技术栈只是项目运行所需背景；README 不展开内部架构细节。

## 快速开始

### 运行要求

- JDK 21
- Node.js 20.19+ 或 22.12+
- pnpm
- MySQL
- Redis

### 启动后端

应用不会在启动时自动建表。首次运行前先创建数据库，并手动执行表结构脚本：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS cod_build_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p cod_build_manager < backend/src/main/resources/schema.sql
```

Windows PowerShell 可在仓库根目录执行同样的 `mysql` 命令。

```bash
cd backend
./gradlew bootRun
```

Windows PowerShell 可使用：

```powershell
cd backend
.\gradlew.bat bootRun
```

默认会连接本机 MySQL 和 Redis。必要时通过环境变量覆盖：

```bash
APP_DATASOURCE_URL=jdbc:mysql://localhost:3306/cod_build_manager?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
APP_DATASOURCE_USERNAME=root
APP_DATASOURCE_PASSWORD=123456
APP_REDIS_HOST=localhost
APP_REDIS_PORT=6379
```

后端默认使用 `dev` profile。本地开发会使用 dev-only JWT secret；正式部署请使用 `prod` profile 并显式提供密钥、数据库、Redis 和 CORS 来源：

```bash
SPRING_PROFILES_ACTIVE=prod
APP_DATASOURCE_URL=jdbc:mysql://localhost:3306/cod_build_manager?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
APP_DATASOURCE_USERNAME=cod_user
APP_DATASOURCE_PASSWORD=change-this-password
APP_REDIS_HOST=localhost
APP_REDIS_PORT=6379
APP_CORS_ALLOWED_ORIGINS=https://example.com
APP_AUTH_JWT_SECRET=change-this-to-a-long-random-secret-at-least-32-chars
```

应用不会自动创建后台管理员。新部署需要先生成密码哈希：

```bash
cd backend
./gradlew passwordHash --args "your-strong-password"
```

Windows PowerShell：

```powershell
cd backend
.\gradlew.bat passwordHash --args "your-strong-password"
```

然后把输出的 BCrypt 哈希写入 `users` 表：

```sql
INSERT INTO users (
  id,
  username,
  password_hash,
  role,
  display_name,
  enabled,
  last_login_at,
  created_at,
  updated_at
) VALUES (
  CONCAT('user_', REPLACE(UUID(), '-', '')),
  'admin',
  'replace-with-passwordHash-output',
  'ADMIN',
  '管理员',
  TRUE,
  NULL,
  CURRENT_TIMESTAMP(6),
  CURRENT_TIMESTAMP(6)
);
```

### 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

默认 API 地址是 `http://localhost:8080/api`。如需调整：

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

前端开发服务器默认由 Vite 输出访问地址，通常是 `http://localhost:5173`。

## 常用开发命令

前端构建与类型检查：

```bash
cd frontend
pnpm build
```

后端测试：

```bash
cd backend
./gradlew test
```

Windows PowerShell：

```powershell
cd backend
.\gradlew.bat test
```

## 数据说明

MySQL 是后台数据的主要来源。`backend/src/main/resources/schema.sql` 是手动初始化脚本，用于创建必要表结构和基础设置；应用启动时不会自动执行它。

Redis 当前用于后台登录验证码。

浏览器本地配装只保存在当前浏览器，不会自动同步到服务器。

## 开发状态

项目已经完成前后端分离和主要边界整理，后续更适合做小步功能补充、体验打磨和数据维护，而不是继续大规模重构。

## License

仓库当前未声明 License。
