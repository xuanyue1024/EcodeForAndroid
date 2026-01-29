# Ecode Android

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Version](https://img.shields.io/badge/version-1.0.0--preview-orange.svg)](https://github.com/xuanyue1024/EcodeForAndroid/releases)

一个基于 Android 的企业码扫描应用，支持用户认证、二维码/条形码扫描和用户信息管理。

[English](README_EN.md) | 简体中文

## ✨ 功能特性

### 🔐 用户认证
- 用户名/密码登录
- MD5 密码加密保护
- 验证码验证支持
- Token 自动保持登录状态
- 自定义服务器地址配置

### 📸 智能扫码
- 高性能二维码/条形码扫描（基于 Google ML Kit）
- 流畅的相机体验（CameraX）
- 支持多种码制（QR Code、EAN、UPC 等）
- 实时扫码预览和反馈
- 扫码结果确认与提交

### 👤 用户管理
- 用户详细信息展示
- WebView 富文本内容展示
- 安全退出登录功能

## 📱 系统要求

- **最低 Android 版本**: Android 8.0 (API 26)
- **推荐 Android 版本**: Android 10.0 (API 29) 或更高
- **必需权限**: 相机、网络
- **存储空间**: 约 20 MB

## 🚀 快速开始

### 下载安装

1. 从 [Releases](https://github.com/xuanyue1024/EcodeForAndroid/releases) 页面下载最新版本的 APK
2. 在 Android 设备上启用"未知来源"安装权限
3. 点击 APK 文件进行安装
4. 授予必要的权限（相机、网络）

### 首次配置

1. **设置服务器地址**
   - 启动应用后，点击"设置服务器"
   - 输入您的服务器 URL
   - 保存配置

2. **登录账户**
   - 输入用户名和密码
   - 如需验证码，点击图片刷新
   - 点击"登录"按钮

3. **开始使用**
   - 登录成功后进入用户信息页面
   - 点击扫码按钮开始扫描二维码

## 🛠 开发构建

### 环境要求

- Android Studio Arctic Fox (2020.3.1) 或更高版本
- JDK 8 或更高版本
- Gradle 8.7
- Android SDK 35

### 克隆项目

```bash
git clone https://github.com/xuanyue1024/EcodeForAndroid.git
cd EcodeForAndroid
```

### 构建项目

```bash
# 清理项目
./gradlew clean

# 构建 Debug 版本
./gradlew assembleDebug

# 构建 Release 版本
./gradlew assembleRelease
```

### 运行测试

```bash
# 运行单元测试
./gradlew test

# 运行 Android 测试
./gradlew connectedAndroidTest
```

### 代码检查

```bash
# 运行 Lint 检查
./gradlew lint

# 修复 Lint 问题
./gradlew lintFix
```

## 📚 技术栈

### 核心技术
- **语言**: Java 8
- **构建工具**: Gradle 8.7
- **Android Gradle Plugin**: 8.5.2
- **最小 SDK**: 26 (Android 8.0)
- **目标 SDK**: 35 (Android 14)

### 主要依赖库

| 库名 | 版本 | 用途 |
|------|------|------|
| [OkHttp](https://square.github.io/okhttp/) | 4.12.0 | HTTP 客户端 |
| [FastJSON2](https://github.com/alibaba/fastjson2) | 2.0.43 | JSON 处理 |
| [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning) | 17.3.0 | 条码识别 |
| [CameraX](https://developer.android.com/training/camerax) | 1.3.1 | 相机功能 |
| [Glide](https://github.com/bumptech/glide) | 4.16.0 | 图片加载 |
| [Material Components](https://material.io/develop/android) | 1.12.0 | UI 组件 |
| [AndroidX WebKit](https://developer.android.com/jetpack/androidx/releases/webkit) | 1.10.0 | WebView |

## 📂 项目结构

```
EcodeForAndroid/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/github/ecode/
│   │   │   │   ├── MainActivity.java          # 登录界面
│   │   │   │   ├── ScanActivity.java          # 扫码界面
│   │   │   │   ├── ScanConfirmActivity.java   # 扫码确认
│   │   │   │   ├── UserInfoActivity.java      # 用户信息
│   │   │   │   ├── model/                     # 数据模型
│   │   │   │   ├── ui/                        # UI 组件
│   │   │   │   └── util/                      # 工具类
│   │   │   ├── res/                           # 资源文件
│   │   │   └── AndroidManifest.xml
│   │   └── test/                              # 测试文件
│   └── build.gradle                           # 应用级构建配置
├── gradle/                                    # Gradle 配置
├── build.gradle                               # 项目级构建配置
├── settings.gradle                            # 项目设置
├── CHANGELOG.md                               # 更新日志
├── RELEASE_NOTES.md                           # 发布说明
└── README.md                                  # 本文件
```

## 🔒 安全说明

- ✅ 密码使用 MD5 加密传输
- ✅ Token 身份验证机制
- ✅ HTTPS 协议支持
- ⚠️ **开发模式**: 当前版本在开发模式下信任所有 SSL 证书
- ⚠️ **生产环境**: 部署到生产环境前需要配置正确的证书验证

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

### 代码规范

- 遵循 Java 代码规范
- 类名使用 PascalCase
- 方法和变量使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 添加必要的注释和文档

## 📝 更新日志

详见 [CHANGELOG.md](CHANGELOG.md)

## 🗺 开发路线图

### v1.1.0
- [ ] 生物识别登录（指纹/面部识别）
- [ ] 扫码历史记录
- [ ] 离线模式支持
- [ ] 数据同步功能

### v1.2.0
- [ ] 多语言支持（英文、繁体中文）
- [ ] 暗黑模式优化
- [ ] 性能优化
- [ ] UI/UX 改进

### v2.0.0
- [ ] 架构重构（MVVM）
- [ ] 依赖注入（Hilt）
- [ ] Kotlin 迁移
- [ ] 提升单元测试覆盖率

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 📞 联系方式

- **项目主页**: https://github.com/xuanyue1024/EcodeForAndroid
- **问题反馈**: https://github.com/xuanyue1024/EcodeForAndroid/issues
- **Pull Requests**: https://github.com/xuanyue1024/EcodeForAndroid/pulls

## 🙏 致谢

感谢以下开源项目：

- [OkHttp](https://square.github.io/okhttp/) - HTTP 客户端
- [FastJSON2](https://github.com/alibaba/fastjson2) - JSON 处理
- [Google ML Kit](https://developers.google.com/ml-kit) - 机器学习工具包
- [CameraX](https://developer.android.com/training/camerax) - 相机库
- [Glide](https://github.com/bumptech/glide) - 图片加载
- [Material Design](https://material.io/) - 设计系统

---

**⭐ 如果这个项目对您有帮助，请给一个 Star！**
