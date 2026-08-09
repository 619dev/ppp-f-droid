# PaperPhonePlus — Android 客户端

🌐 **其他语言 / Other Languages:** [English](README_EN.md)

> 本仓库是 [PaperPhonePlus](https://github.com/619dev/Paperphone-plus) 的 **Android 原生客户端**（Capacitor 打包），基于上游前端代码构建，适用于 Google Play 及侧载分发。

[![Upstream](https://img.shields.io/badge/上游仓库-619dev%2FPaperphone--plus-blue?logo=github)](https://github.com/619dev/Paperphone-plus)
[![React](https://img.shields.io/badge/React-19-blue)](#)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7-blue)](#)
[![Capacitor](https://img.shields.io/badge/Capacitor-8-green)](#)
[![Version](https://img.shields.io/badge/版本-2.3.5-orange)](package.json)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](LICENSE)

[![Google Play](https://img.shields.io/badge/Google%20Play-下载-green?logo=google-play)](https://play.google.com/store/apps/details?id=com.fm619.paperphoneplus)

---

## 📖 项目简介

PaperPhonePlus 是一款微信风格的端对端加密即时通讯应用。本仓库为其 Android 原生客户端，使用 [Capacitor](https://capacitorjs.com/) 将 React + TypeScript 前端打包为 Android APK/AAB。

### 核心特性

| 功能 | 说明 |
|------|------|
| 🔐 端对端加密 | 无状态 ECDH + XSalsa20-Poly1305，逐消息临时密钥，前向保密 |
| 🗝️ 安全本地密钥 | 身份私钥和群聊 Sender Keys 由 Android Keystore 保护，聊天缓存使用设备专属 AES-256-GCM 密钥加密 |
| 📹 视频/语音通话 | 私聊与群组会议统一使用 LiveKit SFU（最多 100 人），支持自由讨论与讲课模式 |
| 🎙️ 实时变声 | 语音消息 / 通话支持 3 档变声（0.8x / 1.0x / 1.2x） |
| 📱 会话保持 | 网络中断、普通鉴权失败或服务器地址变化时保留登录状态，仅在服务器明确撤销会话时退出 |
| 📴 离线访问 | 按账户缓存联系人、群组、聊天记录、朋友圈、时间线及媒体，断网时仍可浏览已缓存内容 |
| 👥 群聊 | 最多 2000 人群组，支持加密与未加密两种模式 |
| 💬 消息功能 | 文字、图片、视频、文件、语音消息、消息引用、Emoji 面板、Telegram 贴纸包、已读状态 |
| 🌐 朋友圈 | 发动态（文字+图片/视频）、点赞、评论、标签可见性控制 |
| 📰 时间线 | 小红书风格公开发帖区，双列瀑布流布局，支持匿名发帖 |
| 🔔 消息推送 | FCM + OneSignal + ntfy 多通道推送 |
| 🌐 多语言 | 中文、英文、日语、韩语、法语、德语、俄语、西班牙语 |
| 🔑 两步验证 | Google Authenticator 兼容 TOTP，8 个恢复码 |
| 📷 扫码 | 扫二维码添加好友、加入群聊 |

### 最近更新（v2.3.5）

- 新增本地聊天记录静态加密：使用 Android Keystore 中的设备专属密钥和 AES-256-GCM 加密，密文保存至独立 IndexedDB。
- 身份私钥及群聊 Sender Keys 迁移至系统安全存储，密钥无法随应用备份迁移到其他设备。
- 聊天明文仅保留在运行内存中，持久化前强制移除解密字段；私聊发送中的消息也不会短暂写入明文。
- 自动迁移并删除旧版 localStorage、sessionStorage 和 IndexedDB 中的明文密钥与聊天缓存，同时清理旧的未加密媒体缓存。
- 加密缓存按账号隔离并进行完整性验证，检测到损坏或篡改时安全丢弃，不回退到明文存储。

#### v2.3.3

- 发送语音消息录音期间保持屏幕唤醒，防止自动熄屏、锁屏后出现录音界面无法操作的问题。
- 语音消息最长限制为 120 秒，到达上限后自动停止；变声后的最终音频同样不会超过 120 秒。
- 离开聊天页面时自动释放录音器、计时器和麦克风资源，提高录音流程稳定性。

- 新增通话防休眠功能：私聊及群组的语音、视频通话期间保持屏幕常亮。
- 覆盖来电、呼出、连接中和已接通状态；通话结束或失败后自动恢复系统锁屏策略。
- 使用 Android 原生窗口唤醒标志实现，无需申请额外的后台唤醒锁权限。
- 新增长效刷新令牌：短期访问令牌过期后自动续期，并对并发请求复用同一次刷新。
- 改进 WebSocket 鉴权、心跳和指数退避重连，网络波动后可自动恢复会话。
- 新增按账户隔离的消息发送队列；离线发送的消息会在重连后自动补发并去重。
- 仅在服务器明确撤销会话时退出登录；断网、普通鉴权失败和服务器地址变化均保留本地账户与缓存。
- 应用版本更新至 2.3.2。

---

## 🏗️ 技术架构

```
ppp-android/
├── android/                  # Capacitor Android 原生工程
├── src/                      # React + TypeScript 前端源码
│   ├── App.tsx               # 路由 + 鉴权守卫
│   ├── index.css             # 设计系统（暗色/亮色，玻璃拟态）
│   ├── main.tsx              # React 入口
│   ├── api/                  # HTTP 客户端 + WebSocket 客户端
│   ├── components/           # UI 组件（TabBar、通话覆盖层、QR码等）
│   ├── contexts/             # React Context
│   ├── crypto/               # 加密模块
│   │   ├── ratchet.ts        # ECDH + XSalsa20-Poly1305 加密
│   │   ├── keystore.ts       # Android Keystore 私钥持久化
│   │   └── groupCrypto.ts    # 群组加密（Sender Key 协议）
│   ├── hooks/                # 自定义 Hooks
│   │   └── useGroupCall.ts   # LiveKit 群组会议与主持控制
│   ├── i18n/                 # 多语言支持
│   ├── pages/                # 页面组件
│   ├── store/                # Zustand 状态管理
│   └── utils/                # 工具函数
│       ├── messagePayload.ts # 消息正文与引用信息的兼容编码
│       ├── offlineCache.ts   # 按账户隔离的数据与媒体离线缓存
│       ├── stickerCache.ts   # Telegram 贴纸持久化缓存
│       └── session.ts        # 会话终止信号识别与安全退出
├── capacitor.config.ts       # Capacitor 配置
├── vite.config.ts            # Vite 构建配置
├── package.json              # 依赖管理
└── metadata/                 # F-Droid 构建配方
```

### 技术栈

- **前端框架**：React 19 + TypeScript 5.7
- **构建工具**：Vite 6
- **状态管理**：Zustand 5
- **原生桥接**：Capacitor 8（Android）
- **加密库**：libsodium-wrappers-sumo（WebAssembly，Curve25519 / XSalsa20-Poly1305）
- **抗量子加密**：crystals-kyber-js（CRYSTALS-Kyber 后量子密钥封装）
- **视频通话**：LiveKit SFU（私聊与群组会议）
- **推送通知**：ntfy / 标准 Web Push（不依赖 Play 服务）
- **UI 图标**：Lucide React
- **动画**：Lottie Web
- **二维码**：qrcode + jsqr

---

## 🚀 快速开始

### 前置要求

- [Node.js](https://nodejs.org/) ≥ 18
- [Android Studio](https://developer.android.com/studio) + Android SDK
- JDK 17+

### 安装与构建

```bash
# 1. 克隆仓库
git clone <repo-url> && cd ppp-android

# 2. 安装依赖
npm install

# 3. 构建前端
npm run build

# 4. 同步到 Android 工程
npx cap sync android

# 5. 在 Android Studio 中打开
npx cap open android
```

### 开发模式

```bash
# 启动前端开发服务器
npm run dev

# 在另一个终端运行 Android 实时重载
npx cap run android --livereload --external
```

> **注意**：开发模式下需要配置后端服务器地址。可在登录页面手动输入后端地址，例如 `https://your-server.com`。

---

## 🔧 配置说明

### Capacitor 配置

核心配置位于 [capacitor.config.ts](capacitor.config.ts)：

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `appId` | `com.fm619.paperphoneplus` | Android 应用包名 |
| `appName` | `PaperPhonePlus` | 应用显示名称 |
| `webDir` | `dist` | 前端构建输出目录 |
| `androidScheme` | `https` | WebRTC 和 crypto API 需要 HTTPS |

### F-Droid 推送配置

此版本不包含 Firebase、OneSignal 或 Google Play 服务依赖。服务器支持时，
Android 通知主题通过 ntfy 注册；浏览器中仍可使用基于标准的 Web Push。

---

## 📦 构建发布

### 构建 Release APK/AAB

```bash
# 构建前端
npm run build

# 同步到 Android 工程
npx cap sync android

# 在 Android Studio 中构建签名包
# Build → Generate Signed Bundle / APK
```

### 签名配置

发布产物有意保持未签名，由 F-Droid 使用自己的密钥签名。本地分发时请将
自己的签名配置保存在源码树之外。

---

## 🔗 相关项目

| 项目 | 说明 |
|------|------|
| [Paperphone-plus](https://github.com/619dev/Paperphone-plus) | 上游主仓库（前端 + 后端） |
| [ppp-win](https://github.com/619dev/ppp-win) | Windows 桌面客户端 |
| [ppp-mac](https://github.com/619dev/ppp-mac) | Mac 桌面客户端 |

---

## 🔒 安全模型

```
注册时:
  设备本地生成 IK（身份密钥）+ SPK（签名预密钥）+ 20x OPK（一次性预密钥）
  公钥上传服务器，私钥四层持久化，永不离开设备

发送消息时:
  发送方下载接收方 IK 公钥
  生成临时 ECDH 密钥对（每条消息独立）
  X25519 ECDH → 共享秘密 → XSalsa20-Poly1305 加密
  临时公钥附在消息 header 中，接收方解密后销毁

服务器所见:
  ✅ 密文 blob + 路由元数据（发件人/收件人 UUID）
  ❌ 明文 / 私钥 / 临时密钥 / 通话内容
```

---

## 📄 开源许可

本项目基于 [GNU Affero General Public License v3.0 (AGPL-3.0)](LICENSE) 开源，与上游仓库 [619dev/Paperphone-plus](https://github.com/619dev/Paperphone-plus) 保持一致。

简而言之：
- ✅ 个人和企业均可自由部署和使用
- ✅ 允许修改代码
- ⚠️ 修改后通过网络提供服务时，必须公开修改后的源代码
- ⚠️ 衍生作品必须使用相同协议（AGPL-3.0）
- ⚠️ 必须保留原始版权声明和许可信息

完整协议文本请参阅 [LICENSE](LICENSE) 文件。

---

## 🤝 贡献

欢迎通过 Issue 和 Pull Request 参与贡献！

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/your-feature`)
3. 提交更改 (`git commit -m 'Add your feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request

---

## 📬 联系方式

- Telegram 群组：https://t.me/+vHJtvWJY_gEyMTUx
- 上游仓库 Issues：https://github.com/619dev/Paperphone-plus/issues
