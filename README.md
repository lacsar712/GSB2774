# 球馆预约管理系统

基于 Spring Boot 3.x + React + Ant Design Pro 的球馆预约管理系统。

## 原始需求

> 我是一个计科专业的学生 现在需要写一个毕业设计 我想基于andtp脚手架+spring boot框架开发一个web应用 初步的想法是开发一个球馆预约管理系统 需要有前后端 包括程序管理（超级管理员）后台和客户（球馆管理员）使用后台及用户（预约用户）使用前台 我们先作为PM 对需求进行分析

## 项目概述

本系统是一个完整的球馆预约管理平台，支持三种角色：

- **超级管理员 (SUPER_ADMIN)**: 管理球馆管理员账号和球馆信息
- **球馆管理员 (VENUE_ADMIN)**: 管理场地、生成预约时段、处理订单
- **用户 (USER)**: 浏览球馆、预约场地、管理订单

## 技术栈

### 后端

- Spring Boot 3.2.x
- Java 17
- MyBatis-Plus
- MySQL 8.0
- Spring Security + JWT
- Maven

### 前端

- React 18
- Ant Design Pro
- Umi 4
- TypeScript
- pnpm

### 部署

- Docker
- Docker Compose
- Nginx

## 快速开始

### 测试账号与快捷登录

登录页已提供 3 个快捷登录按钮（开发/演示环境）：

- 管理员端：`admin / 123456`
- 球馆端：`venue_admin1 / 123456`
- 用户端：`user1 / 123456`

### 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- 请使用 `docker compose`（Compose V2）命令

### 使用 Docker Compose 部署（推荐）

1. 克隆项目

```bash
git clone <repository-url>
cd gym-booking-system
```

2. 启动所有服务（首次建议带构建）

```bash
docker compose up -d --build
```

仅重启已构建镜像时，可使用：

```bash
docker compose up -d
```

3. 访问系统

- 前端: http://localhost:18080
- 后端 API: http://localhost:18081/api
- MySQL: localhost:13306

4. 常用维护命令

```bash
# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f

# 停止并删除容器（保留数据库卷）
docker compose down

# 停止并删除容器和数据库卷（危险操作）
docker compose down -v
```

### 本地开发

#### 后端开发

1. 安装 Java 17 和 Maven

2. 启动 MySQL 数据库

```bash
docker run -d \
  --name mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=gym_booking \
  -p 3306:3306 \
  mysql:8.0
```

3. 执行数据库初始化脚本

```bash
mysql -h localhost -u root -p gym_booking < backend/src/main/resources/schema.sql
```

4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

#### 前端开发

1. 安装 Node.js 18+ 和 pnpm

2. 安装依赖

```bash
cd frontend
pnpm install
```

3. 启动开发服务器

```bash
pnpm run dev
```

前端将在 http://localhost:8000 启动

## 项目结构

```
.
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gym/booking/
│   │   │   │   ├── config/    # 配置类
│   │   │   │   ├── controller/# 控制器
│   │   │   │   ├── service/   # 服务层
│   │   │   │   ├── mapper/    # MyBatis Mapper
│   │   │   │   ├── entity/    # 实体类
│   │   │   │   ├── dto/       # 数据传输对象
│   │   │   │   ├── vo/        # 视图对象
│   │   │   │   ├── exception/ # 异常处理
│   │   │   │   ├── util/      # 工具类
│   │   │   │   └── constant/  # 常量
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── schema.sql
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── pages/             # 页面组件
│   │   │   ├── User/          # 用户相关（登录、注册）
│   │   │   ├── App/           # 前台页面
│   │   │   ├── Venue/         # 球馆管理员后台
│   │   │   └── Admin/         # 超级管理员后台
│   │   ├── components/        # 公共组件
│   │   ├── services/          # API 服务
│   │   ├── utils/             # 工具函数
│   │   ├── access.ts          # 权限定义
│   │   ├── app.tsx            # 全局配置
│   │   └── typings.d.ts       # 类型定义
│   ├── config/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── docker-compose.yml          # Docker Compose 配置
├── deploy.sh                   # 部署脚本
└── README.md
```

## 核心功能

### 用户前台

- 注册/登录
- 浏览球馆列表
- 查看球馆详情和场地
- 选择时段预约
- 模拟支付
- 查看我的订单
- 取消未支付订单

### 球馆管理员后台

- 管理球馆信息
- 场地管理（增删改查）
- 生成预约时段
- 查看订单列表
- 核销订单

### 超级管理员后台

- 管理球馆管理员账号
- 查看所有球馆
- 创建/禁用管理员账号

## 代码架构

### 整体架构

本系统采用前后端分离架构，遵循经典的三层架构模式：

```
┌─────────────────────────────────────────────────────────────┐
│                         前端层                               │
│  React + Ant Design Pro + Umi + TypeScript                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │  Pages   │  │ Services │  │Components│                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└─────────────────────────────────────────────────────────────┘
                          ↓ HTTP/REST API
┌─────────────────────────────────────────────────────────────┐
│                         后端层                               │
│  Spring Boot 3.x + Spring Security + JWT                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │Controller│→ │ Service  │→ │  Mapper  │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└─────────────────────────────────────────────────────────────┘
                          ↓ JDBC
┌─────────────────────────────────────────────────────────────┐
│                       数据持久层                             │
│  MySQL 8.0 + MyBatis-Plus                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │  Tables  │  │  Indexes │  │Constraints│                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

### 后端架构设计

#### 1. 分层架构

```
Controller 层（控制器）
    ↓ 接收 HTTP 请求，参数验证
Service 层（业务逻辑）
    ↓ 业务规则，事务管理
Mapper 层（数据访问）
    ↓ SQL 执行，数据映射
Database（数据库）
```

#### 2. 核心模块

**认证与授权模块**

- `SecurityConfig.java` - Spring Security 配置
- `JwtAuthenticationFilter.java` - JWT 认证过滤器
- `JwtUtil.java` - JWT 工具类
- 支持三种角色：SUPER_ADMIN、VENUE_ADMIN、USER

**业务模块**

- **Auth 模块**: 用户注册、登录、获取当前用户
- **Admin 模块**: 超级管理员功能（管理员账号、球馆管理）
- **Venue 模块**: 球馆管理员功能（场地、时段、订单）
- **App 模块**: 用户前台功能（浏览、预约、支付）

**数据模型**

- Entity: 数据库实体映射（User, Venue, Court, Slot, Order, SlotReservation）
- DTO: 数据传输对象（请求参数封装）
- VO: 视图对象（响应数据封装）

#### 3. 关键设计模式

**并发控制**

```java
@Transactional
public Order createOrder(Long userId, Long slotId) {
    // 1. 创建订单
    Order order = new Order();
    orderMapper.insert(order);

    // 2. 插入占用表（slot_id UNIQUE 约束）
    try {
        SlotReservation reservation = new SlotReservation();
        reservation.setSlotId(slotId);
        slotReservationMapper.insert(reservation);
    } catch (DuplicateKeyException e) {
        // 唯一约束冲突，说明已被预约
        throw new BusinessException(3002, "时段已被预约");
    }
    return order;
}
```

**数据权限控制**

```java
private void verifyVenueOwnership(Long venueId) {
    Long currentUserId = getCurrentUserId();
    Venue venue = venueMapper.selectById(venueId);
    if (!venue.getOwnerUserId().equals(currentUserId)) {
        throw new BusinessException(2002, "无权限访问该球馆");
    }
}
```

### 前端架构设计

#### 1. 目录结构

```
src/
├── pages/              # 页面组件（按角色分组）
│   ├── User/          # 登录、注册
│   ├── App/           # 用户前台
│   ├── Venue/         # 球馆管理员后台
│   └── Admin/         # 超级管理员后台
├── services/          # API 服务层
│   ├── auth.ts        # 认证 API
│   ├── admin.ts       # 管理员 API
│   ├── venue.ts       # 球馆管理 API
│   └── app.ts         # 用户前台 API
├── components/        # 公共组件
├── utils/             # 工具函数
├── access.ts          # 权限定义
├── app.tsx            # 全局配置（请求拦截器）
└── typings.d.ts       # TypeScript 类型定义
```

#### 2. 状态管理

使用 Umi 内置的数据流方案：

- 全局状态：用户信息（initialState）
- 组件状态：React Hooks（useState, useRef）
- 表单状态：ProForm 内置状态管理

#### 3. 权限控制

```typescript
// access.ts
export default function access(initialState: { currentUser?: API.CurrentUser }) {
  const { currentUser } = initialState || {};
  return {
    isUser: currentUser?.role === 'USER',
    isVenueAdmin: currentUser?.role === 'VENUE_ADMIN',
    isSuperAdmin: currentUser?.role === 'SUPER_ADMIN',
  };
}

// 路由配置
{
  path: '/venue',
  access: 'isVenueAdmin',
  routes: [...]
}
```

#### 4. API 请求封装

```typescript
// app.tsx - 请求拦截器
request: {
  requestInterceptors: [
    (url, options) => {
      const token = localStorage.getItem('token');
      return {
        url,
        options: {
          ...options,
          headers: {
            ...options.headers,
            Authorization: `Bearer ${token}`,
          },
        },
      };
    },
  ],
}
```

### 数据库设计

系统包含以下核心表：

- `users` - 用户表（支持三种角色）
- `venues` - 球馆表（关联 owner_user_id）
- `courts` - 场地表（关联 venue_id）
- `slots` - 时段表（关联 court_id）
- `orders` - 订单表（关联 user_id, venue_id, court_id, slot_id）
- `slot_reservations` - 时段占用表（slot_id UNIQUE 约束防冲突）

#### 关键设计

**防止并发预约冲突**

```sql
CREATE TABLE slot_reservations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  slot_id BIGINT NOT NULL UNIQUE,  -- 唯一约束
  order_id BIGINT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'locked',
  CONSTRAINT fk_sr_slot FOREIGN KEY (slot_id) REFERENCES slots(id)
);
```

**数据权限隔离**

- venues 表通过 owner_user_id 关联管理员
- 所有查询都需要验证 venue_id 归属

数据库结构定义位于 `backend/src/main/resources/schema.sql`。

## 技术细节

### 1. 认证与授权

#### JWT 认证流程

```
1. 用户登录 → 验证用户名密码
2. 生成 JWT Token（包含 userId, role）
3. 前端保存 Token 到 localStorage
4. 后续请求携带 Token（Authorization: Bearer <token>）
5. 后端验证 Token → 解析用户信息 → 执行业务逻辑
```

#### 权限验证

**后端**

```java
@PreAuthorize("hasRole('VENUE_ADMIN')")
@GetMapping("/venue/courts")
public Result<List<CourtVO>> getCourts() {
    // 只有 VENUE_ADMIN 角色可以访问
}
```

**前端**

```typescript
// 路由级权限
{
  path: '/venue',
  access: 'isVenueAdmin',  // 非 VENUE_ADMIN 无法访问
}

// 组件级权限
<Access accessible={access.isVenueAdmin}>
  <Button>管理场地</Button>
</Access>
```

### 2. 并发控制机制

#### 问题场景

多个用户同时预约同一个时段，如何保证只有一个用户成功？

#### 解决方案

**数据库唯一约束 + 事务**

```java
@Transactional
public Order createOrder(Long userId, Long slotId) {
    // 1. 验证时段状态
    Slot slot = slotMapper.selectById(slotId);
    if (!"available".equals(slot.getStatus())) {
        throw new BusinessException(3001, "时段不可用");
    }

    // 2. 创建订单
    Order order = new Order();
    order.setUserId(userId);
    order.setSlotId(slotId);
    order.setStatus("PENDING_PAY");
    orderMapper.insert(order);

    // 3. 插入占用表（关键：slot_id UNIQUE）
    try {
        SlotReservation reservation = new SlotReservation();
        reservation.setSlotId(slotId);
        reservation.setOrderId(order.getId());
        reservation.setStatus("locked");
        slotReservationMapper.insert(reservation);
    } catch (DuplicateKeyException e) {
        // 唯一约束冲突 → 回滚事务
        throw new BusinessException(3002, "时段已被预约");
    }

    return order;
}
```

**测试验证**

- 10 个并发用户预约同一时段 → 只有 1 个成功
- 100 个高并发用户 → 数据一致性保证
- 并发测试用例位于 `backend/src/test/java/com/gym/booking/service/BookingConcurrencyTest.java`。

### 3. 时段生成算法

#### 需求

根据球馆营业时间（如 09:00-22:00），自动生成每小时的预约时段。

#### 实现

```java
public void generateSlots(Long courtId, LocalDate startDate, LocalDate endDate) {
    Court court = courtMapper.selectById(courtId);
    Venue venue = venueMapper.selectById(court.getVenueId());

    LocalTime openTime = venue.getOpenTime();   // 09:00
    LocalTime closeTime = venue.getCloseTime(); // 22:00

    // 遍历日期范围
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
        LocalTime currentTime = openTime;

        // 按小时生成时段
        while (currentTime.isBefore(closeTime)) {
            Slot slot = new Slot();
            slot.setCourtId(courtId);
            slot.setSlotDate(date);
            slot.setStartTime(currentTime);
            slot.setEndTime(currentTime.plusHours(1));
            slot.setPrice(court.getPricePerSlot());
            slot.setStatus("available");
            slotMapper.insert(slot);

            currentTime = currentTime.plusHours(1);
        }
    }
}
```

**示例**

- 营业时间：09:00-22:00
- 生成时段：09:00-10:00, 10:00-11:00, ..., 21:00-22:00
- 共 13 个时段/天

### 4. 订单状态机

```
PENDING_PAY (待支付)
    ↓ 用户点击"支付"
PAID (已支付)
    ↓ 球馆管理员"核销"
COMPLETED (已完成)

PENDING_PAY (待支付)
    ↓ 用户点击"取消"
CANCELED (已取消)
```

**状态转换规则**

- 只有 PENDING_PAY 可以取消
- 只有 PAID 可以核销
- COMPLETED 和 CANCELED 为终态

### 5. 数据权限控制

#### 球馆管理员数据隔离

**问题**: 球馆管理员 A 不能访问球馆 B 的数据

**解决方案**

```java
// 1. 获取当前用户的 venue_id
private Long getCurrentVenueId() {
    Long userId = getCurrentUserId();
    Venue venue = venueMapper.selectOne(
        new QueryWrapper<Venue>().eq("owner_user_id", userId)
    );
    return venue.getId();
}

// 2. 所有查询都加上 venue_id 过滤
public List<Court> getCourts() {
    Long venueId = getCurrentVenueId();
    return courtMapper.selectList(
        new QueryWrapper<Court>().eq("venue_id", venueId)
    );
}

// 3. 所有更新/删除都验证归属
public void updateCourt(Long courtId, UpdateCourtRequest request) {
    Court court = courtMapper.selectById(courtId);
    verifyVenueOwnership(court.getVenueId());  // 验证归属
    // ... 执行更新
}
```

### 6. 错误处理

#### 统一错误码

```java
public class ErrorCode {
    // 1xxx - 参数错误
    public static final int PARAM_ERROR = 1001;

    // 2xxx - 认证/权限错误
    public static final int UNAUTHORIZED = 2001;
    public static final int FORBIDDEN = 2002;

    // 3xxx - 业务错误
    public static final int SLOT_UNAVAILABLE = 3001;
    public static final int SLOT_RESERVED = 3002;
    public static final int ORDER_STATUS_ERROR = 3003;
}
```

#### 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统异常");
    }
}
```

### 7. 性能优化

#### 数据库索引

```sql
-- 订单查询优化
CREATE INDEX idx_user ON orders(user_id);
CREATE INDEX idx_venue ON orders(venue_id);
CREATE INDEX idx_slot ON orders(slot_id);

-- 时段查询优化
CREATE INDEX idx_court_date ON slots(court_id, slot_date);

-- 占用表查询优化
CREATE UNIQUE INDEX uk_slot ON slot_reservations(slot_id);
```

#### 前端优化

- **代码分割**: 按路由懒加载
- **请求优化**: 使用 ProTable 的分页和筛选
- **缓存策略**: localStorage 缓存 Token

### 8. 测试策略

#### 测试金字塔

```
        /\
       /E2E\        端到端测试（Playwright）
      /------\
     /集成测试 \     API 测试（MockMvc）
    /----------\
   /  单元测试   \   Service 层测试（Mockito）
  /--------------\
```

**测试覆盖**

- 单元测试：18 个（AppServiceTest）
- 集成测试：15 个（AppControllerIntegrationTest）
- 并发测试：3 个（BookingConcurrencyTest）
- 总计：36 个测试用例

测试结果已在本 README 汇总。

## API 文档

### 认证接口

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息

### 超级管理员接口

- `GET /api/admin/venues` - 获取所有球馆
- `GET /api/admin/venue-admins` - 获取管理员列表
- `POST /api/admin/venue-admins` - 创建管理员账号
- `PUT /api/admin/venue-admins/{id}` - 更新管理员

### 球馆管理员接口

- `GET /api/venue/profile` - 获取球馆信息
- `PUT /api/venue/profile` - 更新球馆信息
- `GET /api/venue/courts` - 获取场地列表
- `POST /api/venue/courts` - 创建场地
- `POST /api/venue/slots/generate` - 生成时段
- `GET /api/venue/orders` - 获取订单列表
- `POST /api/venue/orders/{id}/complete` - 核销订单

### 用户前台接口

- `GET /api/app/venues` - 获取球馆列表
- `GET /api/app/venues/{id}` - 获取球馆详情
- `GET /api/app/courts/{id}/slots` - 获取可预约时段
- `POST /api/app/orders` - 创建订单
- `POST /api/app/orders/{id}/pay` - 模拟支付
- `POST /api/app/orders/{id}/cancel` - 取消订单
- `GET /api/app/orders` - 获取我的订单

## 开发计划

当前开发计划聚焦以下阶段：

1. 完成核心业务能力（认证、预约、支付、核销）
2. 补齐自动化测试与回归验证
3. 优化界面体验与部署稳定性

## 测试

### 后端测试

```bash
cd backend
mvn test
```

### 前端测试

```bash
cd frontend
pnpm test
```

### 前端 E2E（Playwright）

```bash
cd frontend
pnpm run test:e2e
```

说明：当前配置使用本机 Chrome 通道执行 E2E。

需求与验收要点已整合在本 README。

## 部署

### 生产环境部署

1. 修改配置文件
   - 修改 `backend/src/main/resources/application.yml` 中的数据库配置
   - 修改 `docker-compose.yml` 中的环境变量

2. 构建并启动

```bash
docker compose up -d --build
```

3. 查看日志

```bash
docker compose logs -f
```

## 常见问题

### 1. 端口冲突

如果 18080、18081 或 13306 端口被占用，请修改 `docker-compose.yml` 中的端口映射。

### 2. 数据库连接失败

确保 MySQL 容器已启动并健康检查通过：

```bash
docker compose ps
```

### 3. 前端无法访问后端 API

检查 Nginx 配置中的代理设置，确保 `backend` 服务名正确。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 联系方式

如有问题，请联系项目维护者。
