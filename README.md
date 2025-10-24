# LYC Music Player

一个基于 Fabric 的 Minecraft 音乐播放器模组，支持在线搜索和播放音乐。
注意：在线API路径需下载源码后自行填入，返回资源需为MP3格式
详见com.rain.config.ModConfig 及 com.rain.network.MusicAPIClient.searchMusic

## ✨ 功能特性

- 🎵 在线搜索音乐
- 🎮 游戏内音乐播放器界面
- 📝 播放列表管理
- 🔄 多种播放模式（顺序播放、随机播放、单曲循环）
- ⌨️ 快捷键支持

## 📋 环境要求

- Minecraft 版本：1.21.10
- Fabric Loader：0.17.3+
- Fabric API：0.136.0+
- Java：21+

## 🔧 安装方法

1. 确保已安装 [Fabric Loader](https://fabricmc.net/use/)
2. 下载最新版本的模组 jar 文件
3. 将 jar 文件放入 Minecraft 的 `mods` 文件夹
4. 启动游戏即可使用

## 🎮 使用方法

### 打开播放器
在游戏中按下快捷键（M）打开音乐播放器界面

### 主要功能
- **搜索音乐**：在搜索标签页输入歌曲名称或歌手名称进行搜索
- **播放控制**：使用播放器标签页的控制按钮进行播放、上一曲、下一曲等操作
- **播放列表**：在播放列表标签页管理你的歌曲队列
- **设置选项**：在设置标签页调整播放模式

### 命令支持
模组还提供了一些客户端命令，可在聊天栏中使用 /music help

## 🛠️ 开发构建

### 前置要求
- JDK 21
- Gradle

### 构建步骤
```bash
# 克隆仓库
git clone <repository-url>
cd LYC-Music-Player

# 构建项目
./gradlew build

# 构建产物位于 build/libs/ 目录
```

## 📂 项目结构

```
src/main/java/com/rain/
├── audio/          # 音频播放相关
├── command/        # 命令系统
├── config/         # 配置管理
├── gui/            # 图形界面
├── manager/        # 管理器类
├── model/          # 数据模型
├── network/        # 网络请求
└── util/           # 工具类
```

## 👤 作者

落雨川

## ⚠️ 免责声明

本模组仅供学习交流使用，音乐资源来自网络，版权归原作者所有。

## 📝 更新日志

### v1.1.0
- 初始版本发布
- 支持基本的音乐搜索和播放功能
- 实现播放列表管理
- 添加多种播放模式

---

如果觉得这个项目对你有帮助，欢迎给个 ⭐ Star！
