# Mini-Spotify

轻量级音乐管理客户端（Android）+ 自建后端 API：支持歌单同步、本地缓存（Room）、JWT 鉴权；可选 Google / Spotify OAuth（需在服务端配置环境变量）。

## 仓库结构

| 目录 | 说明 |
|------|------|
| `android/` | Jetpack Compose 客户端（MVVM、Retrofit、Room） |
| `server/` | Node.js（Express）API，SQLite 使用内置 `node:sqlite` |

## 环境要求

- **Node.js**：≥ 22.5（需内置 `node:sqlite`）
- **Android Studio**：较新版本；JDK 17；Android SDK 35 左右即可

## 后端（server）

```bash
cd server
npm install
copy .env.example .env   # Windows；Linux/macOS: cp .env.example .env
# 编辑 .env：至少设置 JWT_SECRET；若用 OAuth 再填 Google/Spotify 客户端信息
npm start
```

启动成功终端会出现：`Mini-Spotify API listening on http://localhost:3000`

- 健康检查：<http://localhost:3000/health>
- 若端口被占用，可设置环境变量 `PORT`（例如 `3001`），并同步修改安卓端 `API_BASE_URL`。

## 安卓客户端（android）

1. 用 **Android Studio** 打开 **`android/`** 目录（不要只打开仓库根目录时忽略 Gradle 工程识别问题的话，也可从根目录导入 `android` 模块）。
2. **Gradle Sync** 完成后，创建/启动模拟器（建议 API 34+）。
3. 默认 **`BuildConfig.API_BASE_URL`** 为模拟器访问本机：`http://10.0.2.2:3000/`。  
   **真机**请改为电脑局域网 IP，例如 `http://192.168.1.100:3000/`，并保证手机与电脑同网段。

### 登录说明

当前 App 内为**登录页**。请先在电脑上调用注册接口创建用户，再在 App 内用相同邮箱与密码登录。

示例（PowerShell）：

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:3000/api/v1/auth/register" `
  -ContentType "application/json" `
  -Body '{"email":"you@example.com","password":"yourPassword","displayName":"昵称"}'
```

然后运行 App，输入上述邮箱与密码登录；在首页可尝试 **「同步歌单」**。

## 主要 API 前缀

- `POST /api/v1/auth/register` — 注册  
- `POST /api/v1/auth/login` — 登录  
- `GET /api/v1/playlists` — 拉取歌单（需 `Authorization: Bearer <token>`）  
- `PUT /api/v1/playlists/sync` — 同步歌单  
- `GET /api/v1/recommendations` — 示例推荐  

## 许可证

若无特别声明，以仓库内文件为准；用于学习/演示时请自行评估第三方依赖与音乐内容版权合规性。
