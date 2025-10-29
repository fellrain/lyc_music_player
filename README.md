# LYC Music Player

一个基于 Fabric 的 Minecraft 音乐播放器模组，支持在线搜索和播放音乐。
注意：在线API路径需下载源码后自行填入，返回资源需为MP3格式
详见 [ModConfig](https://github.com/fellrain/lyc_music_player/blob/master/src/main/java/com/rain/common/config/ModConfig.java)

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
5. 服务端下载 [Fabric Server](https://fabricmc.net/use/server/)

## 🎮 使用方法

### 打开播放器
在游戏中按下快捷键（M）打开音乐播放器界面

### 主要功能
- **搜索音乐**：在搜索标签页输入歌曲名称或歌手名称进行搜索
- **播放控制**：使用播放器标签页的控制按钮进行播放、上一曲、下一曲等操作
- **播放列表**：在播放列表标签页管理你的歌曲队列
- **收藏选项**：歌曲列表内选择收藏
- **分类选项**：创建歌曲分类/可创建分类并添加歌曲到指定分类
- **设置选项**：在设置标签页调整播放模式和选择音乐平台API
- **数据持久化**：数据持久化到磁盘，重启游戏不会丢失
- **服务端分享**：服务端运行时可点击分享给所有玩家

### 命令支持
模组还提供了一些客户端命令，可在聊天栏中使用 /music help

## 🛠️ 开发构建

### 前置要求
- JDK 21
- Gradle

### 构建步骤
```bash
# 克隆仓库
git clone https://github.com/fellrain/lyc_music_player.git
cd LYC-Music-Player

# 构建项目
./gradlew build

# 构建产物位于 build/libs/ 目录
```

## 🔌 扩展开发

### 添加新的音乐平台API

模组采用策略设计，支持动态切换不同的音乐平台API。要添加新的音乐平台，请按以下步骤操作：

1. **创建策略实现类**
   在 `com.rain.client.network.strategy` 包中创建新的策略类，实现 `MusicApiStrategy` 接口。

2. **实现接口方法**
   实现以下必需方法：
   - `searchMusic(String query)`: 搜索音乐
   - `getLyric(String trackId)`: 获取歌词
   - `getStrategyName()`: 返回策略的唯一名称

3. **数据结构转换**
   确保将平台特定的适配为统一的实体：
   - `SearchResult`: 搜索结果
   - `MusicTrack`: 音乐轨道
   - `Lyric`: 歌词

4. **注册策略**
   策略通过SPI自动加载，或者在 `MusicApiStrategyFactory` 手动注册。

## 👤 作者

落雨川

### 版权声明和使用规定
1.允许修改和发布修改后的版本，但需要保留代码中关于原作者（落雨川）和项目的所有原始信息；
2.禁止对原版本和修改后的版本采取任何形式的收费行为；

## ⚠️ 免责声明

本模组仅供学习交流使用，音乐资源来自网络，版权归原作者所有。

## 📝 更新日志

### v1.7.0
- 初始版本发布
- 支持基本的音乐搜索和播放功能
- 实现播放列表管理
- 添加多种播放模式
- 增加收藏功能
- 增加分类功能
- 增加数据持久化磁盘功能
- 增加实时歌词及翻译歌词
- 增加实时歌曲进度条
- 增加服务端分享功能
- 增加多平台API策略支持

---

如果觉得这个项目对你有帮助，欢迎给个 ⭐ Star！