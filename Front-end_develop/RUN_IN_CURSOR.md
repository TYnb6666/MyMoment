# 在 Cursor 中运行 Android 项目指南

## 方法一：使用 Cursor 内置终端运行（推荐）

### 前置准备

1. **确保 Android SDK 已安装并配置环境变量**
   
   在 `~/.zshrc` 或 `~/.bash_profile` 中添加：
   ```bash
   export ANDROID_HOME=$HOME/Library/Android/sdk
   export PATH=$PATH:$ANDROID_HOME/platform-tools
   export PATH=$PATH:$ANDROID_HOME/tools
   export PATH=$PATH:$ANDROID_HOME/tools/bin
   ```
   
   然后执行：
   ```bash
   source ~/.zshrc  # 或 source ~/.bash_profile
   ```

2. **连接 Android 设备或启动模拟器**
   - 物理设备：启用 USB 调试后连接
   - 模拟器：在 Android Studio 中启动，或使用命令行启动

### 在 Cursor 中运行步骤

1. **打开 Cursor 终端**
   - 使用快捷键：`Ctrl + `` (反引号) 或 `Cmd + J`
   - 或菜单：`Terminal → New Terminal`

2. **检查设备连接**
   ```bash
   cd /Users/mengpu/Desktop/大四上/MobileApps/App/MyMoment-main
   adb devices
   ```
   应该看到你的设备列表

3. **构建并安装应用**
   ```bash
   # 给 gradlew 添加执行权限（如果还没有）
   chmod +x gradlew
   
   # 构建并安装到设备
   ./gradlew installDebug
   ```

4. **启动应用**
   ```bash
   adb shell am start -n nuist.cn.mymoment/.MainActivity
   ```

5. **查看日志（可选）**
   ```bash
   # 实时查看应用日志
   adb logcat | grep -i mymoment
   
   # 或者查看所有日志
   adb logcat
   ```

## 方法二：使用 Android Studio（最简单）

虽然你在 Cursor 中编辑代码，但运行 Android 应用最方便的方式还是使用 Android Studio：

1. **在 Android Studio 中打开项目**
   - File → Open → 选择项目目录
   - 等待 Gradle 同步完成

2. **运行应用**
   - 点击绿色运行按钮
   - 或使用快捷键 `Shift + F10` (Mac: `Ctrl + R`)

3. **工作流程建议**
   - 在 Cursor 中编写和编辑代码（更好的 AI 辅助）
   - 在 Android Studio 中运行和调试应用
   - 两个编辑器可以同时打开同一个项目

## 方法三：使用 Cursor 的任务配置（高级）

你可以在 Cursor 中配置任务来快速运行常用命令：

1. **创建 `.vscode/tasks.json`**（Cursor 兼容 VS Code 配置）
2. **配置运行任务**

## 常用命令速查

```bash
# 检查设备
adb devices

# 构建并安装
./gradlew installDebug

# 启动应用
adb shell am start -n nuist.cn.mymoment/.MainActivity

# 卸载应用
adb uninstall nuist.cn.mymoment

# 查看日志
adb logcat | grep MyMoment

# 清除应用数据
adb shell pm clear nuist.cn.mymoment

# 重新构建（清理后构建）
./gradlew clean build

# 只构建 APK（不安装）
./gradlew assembleDebug
```

## 故障排除

### 问题1：找不到 adb 命令
**解决**：配置 Android SDK 环境变量（见方法一的前置准备）

### 问题2：找不到设备
**解决**：
```bash
# 重启 adb 服务
adb kill-server
adb start-server
adb devices
```

### 问题3：Gradle 构建失败
**解决**：
```bash
# 清理构建缓存
./gradlew clean

# 重新同步
./gradlew --refresh-dependencies
```

### 问题4：权限被拒绝
**解决**：
```bash
chmod +x gradlew
```

## 推荐工作流程

1. **开发阶段**：在 Cursor 中编写代码
2. **测试运行**：在 Android Studio 中运行（最方便）
3. **快速测试**：在 Cursor 终端中使用 `./gradlew installDebug` 快速安装
4. **查看日志**：在 Cursor 终端中使用 `adb logcat` 查看日志

