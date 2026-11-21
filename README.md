# 矿物追踪 OreTracker

Minecraft 1.20.1 Forge 单机矿物追踪模组，纯客户端运行。

## 功能

- 附近矿物描边，可穿墙显示
- 从玩家身体位置向矿物绘制彩色 Tracer 连线
- 按矿物类型筛选：煤炭、铁、铜、金、红石、青金石、钻石、绿宝石、下界石英、远古残骸
- 中文主菜单
- 扫描半径、垂直范围、连线距离、连线开关均可调整
- 自动兼容使用原版矿物标签的模组矿物

## 运行环境

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- 仅客户端，单人生存使用

## 下载与安装

构建完成后，模组位于：

```text
build/libs/oretracker-1.20.1-1.1.3.jar
```

把 jar 放入 `.minecraft/mods` 即可。

## 默认按键

| 按键 | 功能 |
| --- | --- |
| `H` | 开关矿物追踪 |
| `O` | 打开矿物追踪主菜单 |

默认参数：

- 扫描半径 32 格
- 垂直范围 ±20 格
- 连线距离 48 格
- 最多显示最近的 48 条连线

## 构建

需要 JDK 17，项目自带 Gradle Wrapper。

Windows：

```powershell
.\gradlew.bat build
```

Linux / macOS：

```bash
./gradlew build
```

产物：

```text
build/libs/oretracker-1.20.1-1.1.3.jar
```

## 配置

客户端配置保存在：

```text
config/oretracker-client.properties
```

也可以通过主菜单修改并保存。

## 项目结构

```text
src/main/java/com/wkrs14/oretracker/
  OreTrackerMod.java
  client/
    ClientEvents.java
    ClientSetup.java
    OreOutlineRenderer.java
    OreScanner.java
    OreTrackerConfig.java
    OreType.java
    TrackerKeys.java
    TrackerScreen.java
src/main/resources/
  assets/oretracker/lang/
  META-INF/mods.toml
```

## 发布到 GitHub

```bash
git remote add origin https://github.com/<你的用户名>/oretracker.git
git push -u origin main
```

如果使用 GitHub CLI：

```bash
gh repo create oretracker --public --source=. --remote=origin --push
```

## 许可

未授予开源许可，保留所有权利。详见 `LICENSE.txt`。

Minecraft 是 Mojang Studios 的商标。Forge 由 Forge Development LLC 维护。

