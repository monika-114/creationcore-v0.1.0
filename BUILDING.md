# 构建《创造核心》v0.1

## 环境

- Minecraft 1.21.1
- NeoForge 21.1.248
- Java 21
- Gradle 9.2.1（与当前 NeoForge 1.21.1 ModDevGradle 官方模板 wrapper 一致）

## 本地构建

本工程没有附带 `gradle-wrapper.jar`。如果电脑已安装 Gradle 9.2.1，可在工程根目录运行：

```bash
gradle build
```

成功后 JAR 位于：

```text
build/libs/creationcore-0.1.0.jar
```

## GitHub Actions 构建

工程内已经包含 `.github/workflows/build.yml`。

把整个工程推送到 GitHub 后，Actions 会：

1. 使用 Java 21；
2. 安装 Gradle 9.2.1；
3. 执行 `gradle build --stacktrace`；
4. 把 `build/libs/*.jar` 上传为名为 `creationcore-0.1.0-neoforge-1.21.1` 的 Artifact。

这也是当前压缩包最推荐的第一次真实编译方式。
