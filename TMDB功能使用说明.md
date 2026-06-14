# TMDB 元数据增强功能使用说明

## 功能概述

本次更新为 webhtv 添加了 **TMDB 元数据增强**功能，可以为视频详情页自动匹配并补充来自 TheMovieDataBase (TMDB) 的元数据信息：

- 📝 **影片简介**：自动获取 TMDB 官方翻译的中文简介（优先使用，比站点采集的更准确）
- 🎭 **演员列表**：显示主演信息（当前版本已准备数据，后续可在 UI 显示）
- 🎬 **导演/主创**：补充导演或剧集主创信息
- 🖼️ **海报图片**：站点缺失时使用 TMDB 高清海报
- ⭐ **评分信息**：TMDB 评分（已准备，后续可在 UI 显示）
- 🎞️ **类型标签**：影片类型信息（已准备，后续可在 UI 显示）

---

## 快速开始

### 1. 获取 TMDB API Key

1. 访问 [TMDB 官网](https://www.themoviedb.org/) 注册账号（完全免费）
2. 进入 [API 设置页面](https://www.themoviedb.org/settings/api)
3. 申请 API Key（选择"开发者"选项，几分钟内审批）
4. 复制你的 **API Key (v3 auth)** 或 **Access Token (v4 auth)**

### 2. 在 App 中配置

#### 方法一：通过增强设置（推荐）

1. 打开 App，进入 **设置 → 增强设置**
2. 找到 **「TMDB 元数据配置」** 条目，点击进入
3. 在弹出的配置弹窗中：
   - **API Key 输入框**：粘贴你的 TMDB API Key 或 Access Token
   - **启用 TMDB 元数据增强**：打开开关（默认关闭）
   - **启用站点规则**：
     - 留空 = 对所有站点启用（推荐）
     - 添加关键词 = 仅对匹配关键词的站点启用（如：`豆瓣`、`电影`）
     - 点击「选择站点」= 精确勾选需要增强的站点
   - **已排除站点**：点击「选择站点」可将特定站点加入黑名单
4. 点击 **「确定」** 保存配置

#### 方法二：手动编辑配置文件（高级）

在 `/storage/emulated/0/Android/data/com.fongmi.android.tv/files/.config/` 下创建或编辑 `tmdb_config.json`：

```json
{
  "apiKey": "你的TMDB_API_Key",
  "enabledSites": [],
  "disabledSites": []
}
```

然后在 **增强设置 → TMDB 元数据配置** 中打开「启用 TMDB 元数据增强」开关。

---

## 工作原理

### 自动匹配流程

1. **触发时机**：用户点击视频进入详情页时
2. **匹配过程**：
   - 提取视频标题，清理无关标签（如 `[HD]`、`第1集`、年份等）
   - 向 TMDB 搜索匹配的电影/电视剧
   - 选择最相关的搜索结果（通常是第一条）
3. **数据增强**：
   - **简介**：站点简介为空或过短时，使用 TMDB 简介
   - **海报**：站点缺失海报时，使用 TMDB 海报
   - **导演**：站点缺失导演时，使用 TMDB 主创信息
4. **UI 刷新**：通过 `RefreshEvent.vod()` 机制自动刷新详情页显示

### 站点过滤规则

- **全部启用**（默认）：`enabledSites` 为空时，对所有站点生效
- **关键词匹配**：添加关键词后，仅对站点名称或 Key 包含该关键词的站点生效
  - 示例：添加 `豆瓣` 后，只有 `豆瓣电影`、`db_movie` 等站点会被增强
- **黑名单排除**：`disabledSites` 中的站点永远不会被增强，优先级最高

---

## 当前版本限制与后续计划

### ✅ 已实现

- TMDB API 集成（支持 v3 和 v4 认证）
- 自动搜索匹配视频
- 简介、海报、导演信息增强
- 站点过滤规则（启用规则 + 黑名单）
- 配置界面（Chip 标签样式，类似短剧源配置）
- Mobile 和 Leanback 两个 UI 完整支持

### 🚧 待完善（后续版本）

1. **UI 显示优化**
   - 当前仅在后台增强 `Vod` 对象，UI 暂未显示 TMDB 特有信息（评分、类型、演员）
   - 计划在详情页添加 TMDB 评分徽章、类型标签、演员卡片等
   
2. **TMDB 样式模式**
   - 计划添加专用的"TMDB 样式"开关，类似旧版的 `tmdbMode`
   - 启用后，详情页使用 TMDB 风格的深色背景 + 大幅背景图
   
3. **演员信息展示**
   - 当前已获取演员数据，但 `Vod` 类缺少 `setActor()` 方法
   - 后续可扩展 `Vod` 或在 UI 层直接从 `TmdbUIAdapter` 获取演员列表
   
4. **推荐内容**
   - TMDB API 支持"相似影片"和"推荐"功能，后续可集成到详情页

5. **缓存机制**
   - 当前每次进详情页都会重新搜索匹配
   - 后续可添加本地缓存，减少 API 请求

---

## 技术架构

### 核心类

- **`TmdbConfig`** (`bean/TmdbConfig.java`)：配置管理
  - API Key / Access Token
  - 启用站点规则 (`enabledSites`)
  - 排除站点黑名单 (`disabledSites`)
  
- **`TmdbService`** (`service/TmdbService.java`)：TMDB API 交互
  - 搜索接口 (`/search/multi`)
  - 详情接口 (`/movie/{id}`, `/tv/{id}`)
  - 演员接口 (`/credits`)
  
- **`TmdbUIAdapter`** (`ui/helper/TmdbUIAdapter.java`)：数据适配器
  - 自动匹配：`autoMatch(videoName, vod)`
  - 数据增强：`enrichVod(vod)`
  - 通过 `RefreshEvent.vod()` 推送到 UI
  
- **`TmdbMatcher`** (`ui/helper/TmdbMatcher.java`)：匹配算法
  - 标题清理（移除标签、年份、季集号）
  - 搜索并选择最佳结果
  
- **`TmdbSourceDialog`** (`ui/dialog/TmdbSourceDialog.java`)：配置弹窗
  - API Key 输入
  - 全局启用开关
  - 站点规则管理（Chip 标签 + 站点选择）

### 集成点

- **Mobile VideoActivity** (`mobile/.../VideoActivity.java`)：
  - `initTmdbMode()` 初始化适配器
  - `setDetail(Vod)` 触发自动匹配
  
- **Leanback VideoActivity** (`leanback/.../VideoActivity.java`)：同上

- **SettingEnhance** (Mobile / Leanback)：
  - 新增 **「TMDB 元数据配置」** 条目
  - 点击打开 `TmdbSourceDialog`

---

## 常见问题

### 1. 配置后没有生效？

检查以下几点：
- ✅ **API Key 正确**：在 TMDB 官网复制的完整 Key
- ✅ **全局开关已打开**：增强设置中「启用 TMDB 元数据增强」必须开启
- ✅ **站点未被排除**：检查当前站点是否在黑名单中
- ✅ **网络连接正常**：TMDB API 需要网络访问

### 2. 为什么有些视频匹配不到？

- TMDB 主要收录主流电影和电视剧，一些小众内容可能不存在
- 标题清理算法可能过度清理，导致搜索关键词不准确
- 部分视频标题翻译差异大，TMDB 搜索引擎无法识别

### 3. API Key 有请求次数限制吗？

- TMDB 免费 API 每秒 50 次请求，每天无上限
- 本应用每次进详情页仅发送 2-3 次请求（搜索 + 详情 + 演员）
- 正常使用不会触发限制

### 4. 是否支持简体/繁体中文？

- 支持！TMDB 配置中的 `language` 字段默认为 `zh-CN`
- TMDB API 会优先返回中文翻译的简介和标题
- 如需繁体中文，可修改配置为 `zh-TW`

---

## 示例配置

### 场景一：全站启用

```json
{
  "apiKey": "你的API_Key",
  "enabledSites": [],
  "disabledSites": []
}
```

**效果**：所有站点的详情页都会自动匹配 TMDB 数据。

---

### 场景二：仅对特定关键词启用

```json
{
  "apiKey": "你的API_Key",
  "enabledSites": ["豆瓣", "电影", "movie"],
  "disabledSites": []
}
```

**效果**：只有站点名称或 Key 包含 `豆瓣`、`电影`、`movie` 的站点才会被增强。

---

### 场景三：全站启用 + 排除特定站点

```json
{
  "apiKey": "你的API_Key",
  "enabledSites": [],
  "disabledSites": ["cntv", "iqiyi"]
}
```

**效果**：所有站点启用，但 `cntv` 和 `iqiyi` 被排除。

---

## 致谢

本功能参考了旧版 webhtv 的 TMDB 模块设计，并结合当前架构进行了重新实现。感谢 TMDB 提供免费的电影数据库 API！

---

## 反馈与贡献

如遇到问题或有改进建议，欢迎在 GitHub 提交 Issue 或 Pull Request：

👉 [https://github.com/Silent1566/webhtv](https://github.com/Silent1566/webhtv)

---

**Enjoy! 🎬**
