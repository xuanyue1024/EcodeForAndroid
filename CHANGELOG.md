# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-preview] - 2026-01-29

### 功能特性 (Features)

#### 用户认证系统
- ✅ 用户登录功能
  - 支持用户名/密码登录
  - MD5 密码加密
  - 验证码支持（可选）
  - Token 持久化存储
  - 自动登录（Token 有效期内）
- ✅ 服务器地址配置
  - 自定义服务器 URL
  - 本地保存服务器配置

#### 扫码功能
- ✅ 二维码/条形码扫描
  - 基于 Google ML Kit 的条码识别
  - CameraX 相机集成
  - 实时扫码预览
- ✅ 扫码确认界面
  - 扫码结果展示
  - 扫码数据提交

#### 用户信息管理
- ✅ 用户信息展示
  - 显示用户详细信息
  - WebView 集成展示
- ✅ 注销登录功能

### 技术架构 (Technical)

#### 开发环境
- Android SDK 35
- Min SDK 26 (Android 8.0+)
- Java 8
- Gradle Build System

#### 核心依赖
- **网络请求**: OkHttp 4.12.0
- **JSON 解析**: FastJSON2 2.0.43
- **相机功能**: CameraX 1.3.1
- **条码识别**: ML Kit Barcode Scanning 17.3.0
- **图片加载**: Glide 4.16.0
- **UI 组件**: Material Design 3
- **WebView**: AndroidX WebKit 1.10.0

#### 安全特性
- MD5 密码加密传输
- Token 身份验证
- HTTPS 支持

### 已知限制 (Known Limitations)
- 当前版本为预览版（Preview）
- 仅支持 Android 8.0 及以上版本
- 需要相机权限才能使用扫码功能
- 需要网络权限进行 API 通信

### 安装要求 (Installation Requirements)
- Android 8.0 (API 26) 或更高版本
- 摄像头（扫码功能）
- 网络连接

---

## 下一版本计划 (Next Version Plans)
- [ ] 生物识别登录支持
- [ ] 离线模式
- [ ] 扫码历史记录
- [ ] 多语言支持
- [ ] 暗黑模式优化
