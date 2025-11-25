# MyMoment

MyMoment 是一个使用 Kotlin 构建的纯前端日记类 Android 应用，支持天气/温度/定位等扩展属性、地图轨迹展示、日记搜索与管理、登录验证、夜间模式、字体大小切换等功能。当前版本仅包含前端 UI 与本地内存数据存储，不依赖任何后端或云服务。

## 功能特性

- **用户登录**：简单的用户名/密码输入即可进入首页，并通过 `SharedPreferences` 记住登录状态。
- **主页 + Drawer 菜单**：主页采用 DrawerLayout，左侧抽屉集中提供“全部日记”“轨迹地图”“夜间模式”“大字体模式”“退出登录”等入口。
- **日记 CRUD**：
  - 创建/编辑日记时可录入天气、温度与定位信息（AMap 单次定位获取当前位置）。
  - 日记列表卡片支持编辑、删除弹出菜单。
  - RecyclerView + SwipeRefreshLayout 支持下拉刷新、空态提示、大字体模式。
- **搜索与筛选**：工具栏 SearchView 支持根据标题、内容、位置实时搜索。
- **轨迹地图**：整合高德地图 SDK，在单独页面展示用户曾经记录过的所有定位点。
- **主题 & 字体**：Drawer 提供夜间模式和大字体模式切换开关，实时生效。

## 目录结构

```
app/src/main/
├── java/nuist/cn/mymoment/
│   ├── model/Diary.kt                # 日记数据模型（支持 Parcelable）
│   ├── repository/DiaryRepository.kt # 纯内存数据仓库（仅示例用途）
│   ├── ui/
│   │   ├── login/LoginActivity.kt    # 简单登录入口
│   │   ├── main/MainActivity.kt      # Drawer + 列表 + 搜索 + 偏好切换
│   │   ├── diary/
│   │   │   ├── CreateDiaryActivity.kt
│   │   │   ├── DiaryAdapter.kt
│   │   │   └── DiaryViewModel.kt
│   │   └── map/DiaryMapActivity.kt   # 高德地图轨迹页面
│   └── util/
│       ├── AppPreferences.kt         # 登录/夜间/字体偏好存储
│       └── LocationHelper.kt         # AMap 定位辅助类
├── res/layout/                       # activity_main/login/create_diary/map 等布局
├── res/menu/                         # Drawer、搜索栏、卡片菜单等
├── res/values/strings.xml            # 文案 & AMap Key 占位
└── AndroidManifest.xml               # 权限、Activity 注册、AMap Key
```

## 环境要求

- Android Studio Giraffe 及以上（或最新稳定版）
- JDK 11+
- Android SDK API 24+
- 高德开放平台 Key（替换 `res/values/strings.xml` 中的 `amap_api_key`）

## 运行步骤

1. **Android Studio 方式（推荐）**
   - 打开项目：`File → Open → MyMoment-main`
   - 等待 Gradle 同步完成
   - 连接真机或启动模拟器
   - 点击 Run（或 `Shift + F10` / `Ctrl + R`）运行

2. **Cursor / 命令行方式**
   ```bash
   cd /Users/mengpu/Desktop/大四上/MobileApps/App/MyMoment-main
   chmod +x gradlew
   ./gradlew installDebug
   adb shell am start -n nuist.cn.mymoment/.ui.login.LoginActivity
   ```

3. **首次登录**
   - 打开应用后输入任意用户名和密码即可进入主页
   - Drawer 中可测试夜间模式、大字体、地图等功能

## 注意事项

- **前端演示模式**：所有日记数据存储在内存中（`DiaryRepository`），应用重启或进程被杀掉后数据会丢失；要接入真实后端可自行替换仓库实现。
- **高德 SDK**：务必在 `strings.xml` 填写自己的 `amap_api_key`，否则地图/定位相关功能无法使用。
- **AMap 依赖冲突**：只保留 `com.amap.api:3dmap:latest.integration`（已自带定位与搜索能力），避免再额外添加 `com.amap.api:location` 或 `com.amap.api:search`，否则会出现 `Duplicate class ...` 构建错误。
- **资源命名**：日记编辑界面布局文件为 `res/layout/activity_diary_editor.xml`，请勿复制出重名文件（否则 Gradle 会在打包时生成如 `activity_diary_editor 2.xml` 之类的非法资源名，导致 `parseDebugLocalResources` 失败）。
- **定位权限**：首次点击“获取定位”按钮会触发定位权限请求，拒绝后无法写入位置信息。

## 后续可扩展方向

- 接入真实持久化（本地数据库或远程服务）
- 日记图片/音频附件
- 更精细的搜索与筛选（按日期、天气等）
- 用户真实鉴权

欢迎在此基础上继续迭代，构建属于你的「MyMoment」。

