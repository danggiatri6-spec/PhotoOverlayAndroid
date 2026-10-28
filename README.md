# 照片叠加（Android）

一个极简 Android 图片叠加工具：选两张照片、分别调节透明度、预览并导出合成图片。

## 功能
- 选择一张底图和一张叠加图（使用 Android 系统图片选择器）
- 两张图各自独立调节透明度（0–100%）
- 实时预览合成效果
- 导出 JPG 到系统相册 `Pictures/PhotoOverlay`
- 不需要登录，不上传照片，所有处理均在本机完成

## 技术栈
- Kotlin
- Jetpack Compose + Material 3
- Android MediaStore
- 最低 Android 8.0（API 26）

## 用 Android Studio 打开
1. 克隆或下载本仓库。
2. Android Studio 选择 **Open**，打开仓库根目录。
3. 等待 Gradle 同步。如果提示安装 Android SDK 36，请在 SDK Manager 安装。
4. 连接 Android 手机，开启开发者选项和 USB 调试，点击 Run。

## 下载 APK
仓库的 GitHub Actions 会在推送到 `main` 后自动编译；也可以在 **Actions → Build Android APK → Run workflow** 手动触发。
编译成功后，打开最新一次工作流记录，在页面底部 **Artifacts** 下载 `PhotoOverlay-debug-apk`。解压后里面的 `app-debug.apk` 是可安装的调试版 APK。

注意：这是用于个人测试的 debug APK，不是发布签名包。构建产物保留 14 天。

## 当前版本说明
- 底图尺寸决定导出尺寸。
- 叠加图按比例缩放到可容纳在底图内，并居中放置。
- 导出格式为 JPG，因此输出不保留透明背景。
- 当前版本不支持移动/缩放叠加图、裁剪和撤销。
