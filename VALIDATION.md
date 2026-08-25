# v0.1 验证状态

## 已完成

- 所有 `*.json` 与 `pack.mcmeta` 可解析。
- 10 张游戏内静态材质均检查为 16×16 PNG。
- `creationcore:` 模型/纹理引用完整。
- 核心四个配方文件存在并通过约定内容检查。
- `#creationcore:creative_core_containers` 包含 17 种原版潜影盒。
- Java 源码已通过 `javac` 的解析级语法检查，没有发现缺括号、缺分号、非法语句等 parser-level 错误。
- 内部命名空间已统一为 `creationcore`。

## 当前环境无法完成的验证

本次生成环境中：

- Java 21 可用；
- 没有预装 Gradle；
- 没有 NeoForge/Minecraft Gradle 依赖缓存；
- 容器无法直接下载 Gradle/NeoForge 二进制依赖。

因此 **本次交付没有声称已完成真实 `gradle build`，也没有伪造 JAR**。

工程已经加入 GitHub Actions。推送到 GitHub 后，工作流会使用与 NeoForge 1.21.1 当前 ModDevGradle 模板一致的 Gradle 9.2.1，执行：

```text
python3 tools/validate_project.py
gradle build --stacktrace
```

成功后会上传 `build/libs/*.jar` 作为 Artifact。

## 第一次实机测试建议

按以下顺序测试最容易定位问题：

1. 纸 -> 空白物质。
2. 凋灵出生爆炸吞多个空白物质 -> 击杀 -> 恰好 1 基底物质。
3. 基底物质碰撞、破坏速度、爆炸/龙冲撞免疫。
4. 龙战中心纵轴放多个基底物质 -> 龙死亡 -> 全部消耗 -> 出口传送门完成后恰好 1 创造物质。
5. 合法/非法潜影盒分别扔入末地返回传送门。
6. 创造核心实体重进世界后是否保留，右键是否只领取一次。
7. 锻造创造工作台。
8. 原版普通工作台配方、牛刷怪蛋专属配方、Shift 点击。
9. 三个维度分别测试空桶虚空打捞；特别测试返回路径被方块堵住时不会穿墙。
10. “空”桶右键水、岩浆、炼药锅，确认没有桶类交互。
11. 四玻璃 + “空”桶 -> 3 瓶装“　” + 返还 1 桶。
