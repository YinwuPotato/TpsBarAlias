# TpsBarAlias

**把 `/tpsbar` 变成 Canvas 内置命令 `/regionbar tps_bar` 的别名。**

Canvas（Folia 系服务端）自带一条按**区域（region）**统计的 TPS 条命令 `/regionbar tps_bar`，名字长、也不好记。
本插件只做一件事：让玩家用 `/tpsbar` 得到**完全相同**的结果 —— 不自己实现 BossBar、不改统计口径、不绕过权限。

适用：**Canvas 26.3**（Paper 系 / Folia 区域线程）。`plugin.yml` 里已声明 `folia-supported: true`。

---

## 1. 为什么用"转发"而不是自己画 BossBar

Canvas 的 `RegionizedTpsBar` 是**按区域**统计的 —— 自己实现一套 BossBar 拿不到同样的数据，
还会和 Canvas 内置的那条条重复显示。所以本插件只是把命令**原样转发**过去：

```java
getServer().dispatchCommand(sender, "regionbar tps_bar");
```

**关键点：转发时保持"以发送者身份执行"**，所以 Canvas 的权限节点
`canvas.command.regionbar` 仍然会被正常检查 —— 没有该权限的玩家用 `/tpsbar` 一样会被拦下，
**不会提权**。本插件的 `tpsbar.use` 只是"能不能用这个别名"，不是"能不能看 TPS 条"。

---

## 2. Folia / Canvas 线程安全

Canvas 是区域化线程（Folia 系），玩家数据不能在别的区域的线程上碰。本插件的处理：

| 情况 | 行为 |
|---|---|
| `/tpsbar`（不带参数，玩家执行） | 玩家命令本身就跑在**该玩家的 region 线程**上 → 直接转发 |
| `/tpsbar <玩家>` | 在**目标玩家的 region 线程**上执行转发（`target.getScheduler().run(...)`），避免跨区域访问 |

---

## 3. 命令与权限

| 命令 | 说明 | 权限 | 默认 |
|---|---|---|---|
| `/tpsbar` | 切换自己的区域 TPS 条（等价 `/regionbar tps_bar`） | `tpsbar.use` | 所有人 |
| `/tpsbar <玩家>` | 切换指定玩家的 TPS 条 | `tpsbar.others` | OP |

> 实际能否显示，仍取决于 Canvas 的 `canvas.command.regionbar` 权限。
> 控制台执行 `/tpsbar`（不带参数）只会收到用法提示。

---

## 4. 构建

需要 **JDK 25**（Canvas 26.3 的 API jar 是较新的 class 版本），以及服务器根目录下的
`libraries\`（Canvas API 与全部依赖）和 `versions\canvas-*.jar`。

双击 **`build.bat`** 即可：脚本会自动在 `..\..\libraries` 与 `..\..\versions` 下**递归查找最新**的
`canvas-api-*.jar` 和 `canvas-*.jar`，不用手改路径。产物：

```
TpsBarAlias-1.0.2.jar
```

---

## 5. 安装

1. 把 `TpsBarAlias-1.0.2.jar` 放进 `<服务器>\Van\plugins\`
2. 重启 Van（Bukkit 插件只在启动时加载）
3. 进游戏敲 `/tpsbar`，应出现与 `/regionbar tps_bar` 相同的 TPS 条

无需配置 —— 本插件**没有 `config.yml`**。

---

## 6. 文件清单

```
TpsBarAlias/
├─ build.bat                              # 一键构建（自动找 API jar 与服务端 jar）
├─ README.md
└─ src/
   ├─ plugin.yml                          # name/version/命令/权限/folia-supported
   └─ org/yinwu/tpsbar/TpsBarPlugin.java  # 全部逻辑（59 行）
```

---

## 7. 已知边界

- **依赖 Canvas 内置命令存在**：`regionbar` 与 `tps_bar` 参数是 Canvas 提供的；换回普通 Paper/Folia
  会找不到 `/regionbar`，此时 `/tpsbar` 会像普通未知命令一样失败。
- `api-version: '1.19'` 是为了兼容性写的宽值，实际只在 Canvas 26.3 上验证过。
- 不做 TPS 数据采集、不做显示样式定制 —— 那些都在 Canvas 里，改 `regionbar` 的配置生效。
