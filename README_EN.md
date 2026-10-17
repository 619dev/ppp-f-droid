# PaperPhonePlus — Android Client

🌐 **其他语言 / Other Languages:** [中文](README.md)

> This repository is the **Android native client** (Capacitor-wrapped) of [PaperPhonePlus](https://github.com/619dev/Paperphone-plus), built from the upstream frontend codebase for F-Droid distribution.

[![Upstream](https://img.shields.io/badge/Upstream-619dev%2FPaperphone--plus-blue?logo=github)](https://github.com/619dev/Paperphone-plus)
[![React](https://img.shields.io/badge/React-19-blue)](#)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7-blue)](#)
[![Capacitor](https://img.shields.io/badge/Capacitor-8-green)](#)
[![Version](https://img.shields.io/badge/Version-2.6.0-orange)](package.json)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](LICENSE)


---

## 📖 Overview

PaperPhonePlus is a WeChat-style end-to-end encrypted instant messaging application. This repository contains the Android native client, which uses [Capacitor](https://capacitorjs.com/) to package the React + TypeScript frontend into an Android APK/AAB.

### Key Features

| Feature | Description |
|---------|-------------|
| 🔐 End-to-End Encryption | X25519 + XSalsa20-Poly1305, per-message ephemeral keys and Ed25519 signatures |
| ✅ Identity Verification | Mutual verification using a shared text-appearance password, identity-change warnings and signature status |
| 🗝️ Secure Local Keys | Identity and group Sender Keys are protected by Android Keystore; chat caches use a device-bound AES-256-GCM key |
| 📹 Video/Voice Calls | LiveKit SFU for both direct and group calls (up to 100 participants), with discussion and lecture modes |
| 🎙️ Real-time Voice Changer | Voice messages & calls support 3 modes (0.8x / 1.0x / 1.2x) |
| 📱 Session Persistence | Keeps users signed in through network loss, ordinary authorization failures, and server URL changes; signs out only on an explicit server revocation |
| 📴 Offline Access | Account-isolated caching for contacts, groups, chats, Moments, Timeline, and media keeps previously loaded content available offline |
| 👥 Group Chat | Up to 2,000 members; new messages use Sender Key encryption and identity signatures with historical key epochs |
| 💬 Rich Messaging | Text, images, video, files, voice messages, message replies, emoji panel, Telegram sticker packs, read receipts |
| 🌐 Moments | Post updates (text + images/video), likes, comments, tag-based visibility |
| 📰 Timeline | Xiaohongshu-style public feed with waterfall layout, anonymous posting |
| 🔔 Push Notifications | ntfy free-software notifications without Google Play Services |
| 🌐 Multi-language | Chinese, English, Japanese, Korean, French, German, Russian, Spanish |
| 🔑 Two-Factor Auth | Google Authenticator-compatible TOTP with 8 recovery codes |
| 📷 QR Code Scanning | Scan to add friends or join groups |

### Changelog

The complete release history has moved to [changelog.md](changelog.md).

Version `2.6.0` adds signed private and group messaging, mutual identity verification, identity-change warnings, historical group-key epochs, reliable history sync, and authenticated attachment downloads. Trust records remain protected by Android Keystore.

### 2.6.0 Upgrade and Validation

Upgrade the server and all participating clients together to versions compatible with the 2.6.0 signed protocol. Older clients cannot parse signed messages. Legacy unsigned history is labelled; after an identity is pinned, unsigned messages from that contact are rejected. Group rotation retains available historical key epochs but cannot recover keys lost before the upgrade.

```bash
npm run test:security
npm run test:attachments
npm run test:i18n
npm run build
```

---

## 🔐 Extra-encrypted text appearance: design and security boundary

This feature is **extra insurance on top of the existing end-to-end encryption (E2EE)**. It does not replace E2EE with a visual encoding, and it never bypasses or weakens the original encryption. In 2.6.0, signed private messages use X25519 key agreement, XSalsa20-Poly1305 encryption and Ed25519 signatures; groups use Sender Key encryption and Ed25519 signatures. Identity private keys and group Sender Keys remain protected by Android Keystore.

When enabled, every message is processed in this order:

1. The sender first protects the message body with the extra password shared by both participants or by all group members. PBKDF2-SHA-256 (210,000 iterations and a random salt) derives an AES-256-GCM key; every message has an independent random IV and an authentication tag for integrity.
2. The complete extra-encryption frame (version, salt, IV, and ciphertext) is then encoded with one of eight selectable text appearances. This is not merely decorative character substitution: the visible characters carry the extra encrypted ciphertext.
3. That appearance ciphertext then enters the project's original encryption path: private-chat E2EE or group Sender Key encryption. The server still receives the original E2EE/Sender-Key ciphertext plus metadata required for delivery.
4. The recipient reverses the order: first decrypt the original E2EE/Sender-Key layer, then decode the text-appearance frame and decrypt the body with the extra password.

The extra password is never uploaded, synchronized automatically, or distributed by the server. Both people in a private chat must set the same password; every group member who needs to read the plaintext must also set that same password. Text appearances do not need to match: every message carries its own appearance identifier, so the recipient automatically detects and decodes the sender's choice. For example, one person may send Buddhist text while another sends Hangul; if the extra password matches, both decrypt normally. A user's appearance setting controls only the ciphertext appearance of messages they send. If the password is missing, locked, or different, messages are still sent and received normally and the original E2EE layer still decrypts successfully, but the app can display only the appearance ciphertext—not the original text.

The app does not persist the extra password. While unlocked it exists only in the current process memory; locally, the app stores only a random salt and AES-GCM verification data used to check whether an entered password is correct. Users can lock immediately or automatically 5, 15, 30, or 60 minutes after the app leaves the foreground. After sign-in and secure account-state restoration, the app prompts for the text-appearance encryption password when the feature is enabled but still locked; users may cancel and continue viewing appearance ciphertext only. This layer adds an independent shared secret beyond E2EE; it does not replace a strong password, device lock, or system secure storage, and it cannot provide absolute protection on a fully compromised device while the password remains in memory.

## 🏗️ Architecture

```
ppp-f-droid/
├── android/                  # Capacitor Android native project
├── src/                      # React + TypeScript frontend source
│   ├── App.tsx               # Router + auth guard
│   ├── index.css             # PaperPhone design system (light/dark and accessibility preferences)
│   ├── main.tsx              # React entry point
│   ├── api/                  # HTTP client + WebSocket client
│   ├── components/           # UI components (TabBar, call overlay, QR code, etc.)
│   ├── contexts/             # React contexts
│   ├── crypto/               # Encryption modules
│   │   ├── ratchet.ts        # ECDH + XSalsa20-Poly1305 encryption
│   │   ├── keystore.ts       # Android Keystore-backed private key persistence
│   │   └── groupCrypto.ts    # Group encryption (Sender Key protocol)
│   ├── hooks/                # Custom hooks
│   │   └── useGroupCall.ts   # LiveKit group meetings and host controls
│   ├── i18n/                 # Internationalization
│   ├── pages/                # Page components
│   ├── store/                # Zustand state management
│   └── utils/                # Utility functions
│       ├── messagePayload.ts # Compatible message body and reply metadata encoding
│       ├── offlineCache.ts   # Account-isolated offline data and media cache
│       ├── stickerCache.ts   # Persistent Telegram sticker cache
│       └── session.ts        # Session termination detection and safe sign-out
├── capacitor.config.ts       # Capacitor configuration
├── vite.config.ts            # Vite build configuration
├── package.json              # Dependency management
└── scripts/                  # F-Droid source checks and unsigned build
```

### Tech Stack

- **Frontend Framework**: React 19 + TypeScript 5.7
- **Build Tool**: Vite 6
- **State Management**: Zustand 5
- **Native Bridge**: Capacitor 8 (Android)
- **Crypto Library**: libsodium-wrappers-sumo (WebAssembly, Curve25519 / XSalsa20-Poly1305)
- **Compatibility Crypto Dependency**: crystals-kyber-js remains available; the 2.6.0 signed path uses X25519. The current registration field `kem_pub` carries an Ed25519 signing public key, not a working post-quantum registration key.
- **Video Calls**: LiveKit SFU (direct and group calls)
- **Push Notifications**: ntfy (free software, without Google Play Services)
- **UI Icons**: Lucide React
- **Animations**: Lottie Web
- **QR Code**: qrcode + jsqr

---

## 🚀 Getting Started

### Prerequisites

- [Node.js](https://nodejs.org/) ≥ 22
- [Android Studio](https://developer.android.com/studio) + Android SDK
- JDK 21 (the Gradle configuration uses Temurin 21)
- Android SDK Platform 36; `minSdkVersion 24`, `targetSdkVersion 36`

### Installation & Build

```bash
# 1. Clone the repository
git clone https://github.com/619dev/ppp-f-droid.git && cd ppp-f-droid

# 2. Install dependencies
npm ci

# 3. Build the frontend
npm run build

# 4. Sync to Android project
npx cap sync android

# 5. Open in Android Studio
npx cap open android
```

### Development Mode

```bash
# Start the frontend dev server
npm run dev

# In another terminal, run Android with live reload
npx cap run android --livereload --external
```

> **Note**: In development mode, you need to configure the backend server address. You can manually enter the backend URL on the login page, e.g., `https://your-server.com`.

---

## 🔧 Configuration

### Capacitor Configuration

Core settings are in [capacitor.config.ts](capacitor.config.ts):

| Setting | Value | Description |
|---------|-------|-------------|
| `appId` | `com.fm619.paperphoneplus` | Android application package name |
| `appName` | `PaperPhonePlus` | App display name |
| `webDir` | `dist` | Frontend build output directory |
| `androidScheme` | `https` | Required for WebRTC and crypto APIs |

### ntfy Notifications

Use a free-software ntfy client with a compatible server configuration. The app registers its notification topic after sign-in and does not use Firebase or Google Play Services.

### Build Release APK/AAB

```bash
# Build the frontend
npm run build

# Sync to Android project
npx cap sync android

# Validate native code and build a test APK
cd android
./gradlew --no-daemon assembleDebug lintRelease

# Build an unsigned release APK; F-Droid builds and signs from source
./gradlew --no-daemon assembleRelease
```

### F-Droid Build

Version `2.6.0` (`versionCode 26000`) retains only ntfy notifications, without FCM, Google Services plugins, OneSignal or release signing secrets. Run `npm run check:fdroid` for source checks and `sh scripts/build-fdroid.sh` for an unsigned release APK.

F-Droid metadata targets `2.6.0` (`26000`), pinned to the source for `v2.6.0`. GitHub Releases provide a locally release-signed APK and an unsigned build. F-Droid builds from source and signs independently; APKs from these channels cannot update each other directly.

Download: [GitHub Release v2.6.0](https://github.com/619dev/ppp-f-droid/releases/tag/v2.6.0). F-Droid inclusion: [GitLab MR !44373](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/44373). The profile footer displays `PaperPhonePlus v2.6.0`.

---

## 🔗 Related Projects

| Project | Description |
|---------|-------------|
| [Paperphone-plus](https://github.com/619dev/Paperphone-plus) | Upstream main repository (frontend + backend) |
| [ppp-win](https://github.com/619dev/ppp-win) | Windows desktop client |
| [ppp-mac](https://github.com/619dev/ppp-mac) | Mac desktop client |

---

## 🔒 Security Model

- Identity private keys, group Sender Keys and identity trust records use Android Keystore-backed native storage. Chat caches are encrypted per account; decrypted message bodies remain in process memory.
- Private messages use per-message ephemeral X25519 keys and XSalsa20-Poly1305 encryption. Ed25519 signatures bind sender, recipient, message type and ciphertext; group signatures also bind the group, key epoch and message context.
- Mutual verification uses challenges and receipts protected by the shared text-appearance password. A signature alone does not establish ownership of an unknown identity. Changed identities require verification again; group sending requires verified member identities.
- The server sees ciphertext, public identity/signing keys and delivery metadata. Message signing does not end-to-end encrypt uploaded file bytes.
- See [tests/README.md](tests/README.md) for the protocol, boundaries and manual acceptance checks.

---

## 📄 License

This project is licensed under the [GNU Affero General Public License v3.0 (AGPL-3.0)](LICENSE), consistent with the upstream repository [619dev/Paperphone-plus](https://github.com/619dev/Paperphone-plus).

In summary:
- ✅ Free to deploy and use by individuals and enterprises
- ✅ Modification is permitted
- ⚠️ If you provide a modified version as a network service, you must release the modified source code
- ⚠️ Derivative works must use the same license (AGPL-3.0)
- ⚠️ Original copyright notices and license information must be preserved

See the full license text in the [LICENSE](LICENSE) file.

---

## 🤝 Contributing

Contributions via Issues and Pull Requests are welcome!

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 📬 Contact

- Telegram Group: https://t.me/+vHJtvWJY_gEyMTUx
- Upstream Issues: https://github.com/619dev/Paperphone-plus/issues
