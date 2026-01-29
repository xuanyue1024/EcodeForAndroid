# 快速发布指南 (Quick Release Guide)

## 🚀 三步发布流程

### 1️⃣ 构建 APK (本地环境)

```bash
# 在您的本地电脑上执行
cd /path/to/EcodeForAndroid
chmod +x gradlew
./gradlew clean assembleRelease
```

**输出位置**: `app/build/outputs/apk/release/app-release.apk`

---

### 2️⃣ 创建 Release (GitHub Web)

1. 访问: https://github.com/xuanyue1024/EcodeForAndroid/releases
2. 点击 **"Draft a new release"**
3. 填写信息:
   - Tag: `v1.0.0-preview`
   - Title: `Ecode v1.0.0-preview - 首个预览版本`
   - Description: 复制 `RELEASE_DRAFT.md` 中的模板
   - 上传: `app-release.apk`
   - ✅ 勾选 "This is a pre-release"

---

### 3️⃣ 发布

点击 **"Save draft"** (保存草稿) 或 **"Publish release"** (直接发布)

---

## 📁 关键文件

| 文件 | 用途 |
|------|------|
| **RELEASE_SUMMARY.md** | 📋 完整发布总结 (推荐阅读) |
| **RELEASE_DRAFT.md** | 🎯 GitHub Release 创建指南 |
| **RELEASE_NOTES.md** | 📝 详细发布说明 |
| **CHANGELOG.md** | 📜 版本更新日志 |
| **README.md** | 📖 项目主页文档 |

---

## ⚡ 使用 CLI (可选)

```bash
# 如果安装了 gh CLI
gh release create v1.0.0-preview \
  --title "Ecode v1.0.0-preview - 首个预览版本" \
  --notes-file RELEASE_NOTES.md \
  --prerelease --draft \
  app/build/outputs/apk/release/app-release.apk
```

---

## ✅ 完成检查

- [ ] APK 构建成功
- [ ] Release 创建完成
- [ ] APK 已上传
- [ ] 标记为预发布
- [ ] 草稿已保存/已发布

---

**详细说明**: 请查看 `RELEASE_SUMMARY.md`
