# 多阶段构建 - 阶段1: Maven 构建
FROM maven:3.9.8-eclipse-temurin-17 AS builder

WORKDIR /build

# 先复制依赖描述文件，利用 Docker 缓存加速依赖下载
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

# 复制源码并打包
COPY src ./src
RUN mvn -B -DskipTests clean package

# 多阶段构建 - 阶段2: JRE 运行时
FROM eclipse-temurin:17-jre

WORKDIR /app

# 复制构建产物
COPY --from=builder /build/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
