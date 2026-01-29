# GitHub Release 草稿

## 发布信息

**标签名称 (Tag)**: `v1.0.0-preview`  
**发布标题 (Release Title)**: `Ecode v1.0.0-preview - 首个预览版本`  
**目标分支 (Target)**: `main` 或当前主分支  
**发布类型**: ✅ 预发布版本 (Pre-release)  

---

## 发布说明 (Release Description)

```markdown
# 🎉 Ecode v1.0.0-preview

这是 Ecode Android 应用的首个预览版本！感谢您的关注和支持。

## ✨ 主要功能

### 用户认证
- ✅ 用户名/密码登录
- ✅ MD5 密码加密
- ✅ 验证码支持
- ✅ Token 自动保持登录
- ✅ 自定义服务器地址配置

### 智能扫码
- ✅ 二维码/条形码扫描 (基于 Google ML Kit)
- ✅ CameraX 相机集成
- ✅ 实时扫码预览
- ✅ 扫码结果确认与提交

### 用户信息
- ✅ 用户详情展示
- ✅ WebView 集成
- ✅ 安全退出登录

## 🛠 技术栈

- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 35)
- **核心库**: OkHttp 4.12.0, FastJSON2 2.0.43, ML Kit, CameraX 1.3.1, Glide 4.16.0

## 📋 系统要求

- Android 8.0 或更高版本
- 摄像头（扫码功能）
- 网络连接

## 📦 安装说明

1. 下载下方的 `app-release.apk` 文件
2. 在 Android 设备上启用"未知来源"安装
3. 点击安装
4. 授予相机和网络权限

## ⚠️ 已知限制

- 当前为预览版本，可能存在未知 bug
- 开发模式下信任所有 SSL 证书（生产环境需配置）
- 仅支持 Android 8.0+

## 📝 完整说明

详细信息请查看：
- [CHANGELOG.md](./CHANGELOG.md)
- [RELEASE_NOTES.md](./RELEASE_NOTES.md)

## 🔮 下一步计划

- 生物识别登录
- 扫码历史记录
- 多语言支持
- 暗黑模式优化

---

**⚠️ 重要提示**: 这是预览版本，建议仅在测试环境中使用。
```

---

## 发布资产 (Release Assets)

需要上传以下文件：

1. **app-release.apk** (必需)
   - 位置: `app/build/outputs/apk/release/app-release.apk`
   - 说明: Android 安装包
   - 文件大小: 约 10-20 MB

2. **app-release-unsigned.apk** (可选)
   - 位置: `app/build/outputs/apk/release/app-release-unsigned.apk`
   - 说明: 未签名的 APK（如果存在）

3. **CHANGELOG.md** (推荐)
   - 附加更新日志文件

4. **RELEASE_NOTES.md** (推荐)
   - 附加完整发布说明

---

## 创建步骤

### 方式 1: GitHub Web 界面

1. 访问仓库页面: https://github.com/xuanyue1024/EcodeForAndroid
2. 点击右侧的 "Releases" 
3. 点击 "Draft a new release"
4. 填写以下信息:
   - **Choose a tag**: 输入 `v1.0.0-preview` (新建标签)
   - **Release title**: 输入 `Ecode v1.0.0-preview - 首个预览版本`
   - **Description**: 复制上面的"发布说明"内容
   - **Attach binaries**: 拖拽或选择 `app-release.apk` 文件
   - ✅ 勾选 "This is a pre-release"
5. 点击 "Save draft" 保存草稿
6. 或点击 "Publish release" 直接发布

### 方式 2: GitHub CLI (gh)

```bash
# 1. 创建标签
git tag -a v1.0.0-preview -m "Release v1.0.0-preview"
git push origin v1.0.0-preview

# 2. 创建发布草稿
gh release create v1.0.0-preview \
  --title "Ecode v1.0.0-preview - 首个预览版本" \
  --notes-file RELEASE_NOTES.md \
  --prerelease \
  --draft \
  app/build/outputs/apk/release/app-release.apk

# 3. 查看草稿
gh release list

# 4. 发布 (确认无误后)
gh release edit v1.0.0-preview --draft=false
```

### 方式 3: Git + GitHub API

```bash
# 创建标签
git tag v1.0.0-preview
git push origin v1.0.0-preview

# 使用 GitHub API 创建 Release
# (需要 GitHub Personal Access Token)
```

---

## 构建 APK 命令

在发布前需要先构建 APK:

```bash
# 确保 gradlew 有执行权限
chmod +x gradlew

# 清理构建
./gradlew clean

# 构建 Release APK
./gradlew assembleRelease

# APK 输出位置
ls -lh app/build/outputs/apk/release/
```

---

## 发布检查清单

- [ ] 代码已合并到主分支
- [ ] 版本号已更新 (app/build.gradle: versionName)
- [ ] CHANGELOG.md 已更新
- [ ] RELEASE_NOTES.md 已创建
- [ ] APK 已成功构建
- [ ] APK 已测试安装和运行
- [ ] Release 草稿已创建
- [ ] Release 资产已上传 (APK)
- [ ] Release 说明已填写
- [ ] 已标记为预发布版本
- [ ] 团队成员已审核
- [ ] 最终发布

---

## 后续步骤

1. **构建 APK**: 由于网络限制，需要在本地环境构建
   ```bash
   ./gradlew assembleRelease
   ```

2. **测试 APK**: 在实际设备上安装测试

3. **创建 Release**: 使用 GitHub Web 界面或 CLI 创建发布草稿

4. **上传 APK**: 将构建好的 APK 附加到 Release

5. **发布**: 检查无误后发布

---

## 注意事项

⚠️ **签名说明**: 
- Debug APK 使用默认签名
- Release APK 建议配置正式签名密钥
- 配置位置: `app/build.gradle` 的 `signingConfigs`

⚠️ **版本管理**:
- 遵循语义化版本 (Semantic Versioning)
- 格式: `major.minor.patch-suffix`
- 当前: `1.0.0-preview`

⚠️ **安全提醒**:
- 不要在 Git 中提交签名密钥文件
- 使用环境变量或加密存储管理密钥
- 生产版本需配置 ProGuard 混淆
