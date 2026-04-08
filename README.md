# 担保集团业务财务系统

## 项目简介
担保集团业务财务系统是一套完整的担保业务财务管理平台，涵盖基础信息同步、收付款管理、银行对账、银企直连、会计凭证等核心业务模块。

## 技术栈

### 后端 (guarantee-finance-backend)
- **框架**: Spring Boot 3.x
- **安全**: Spring Security + JWT
- **ORM**: MyBatis-Plus
- **缓存**: Redis
- **文档**: Swagger/OpenAPI 3.0
- **数据库**: MySQL 8.0+

### 前端 (guarantee-finance-frontend)
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios

## 项目结构

```
Business-Finance System/
├── guarantee-finance-backend/     # 后端Spring Boot项目
│   ├── src/main/java/com/guarantee/finance/
│   │   ├── config/                # 配置层
│   │   ├── controller/            # 控制层
│   │   ├── service/               # 服务层
│   │   ├── mapper/                # 数据访问层
│   │   ├── entity/                # 实体类
│   │   ├── dto/                   # 数据传输对象
│   │   ├── vo/                    # 视图对象
│   │   ├── common/                # 公共工具类
│   │   └── security/              # 安全模块
│   └── pom.xml
├── guarantee-finance-frontend/    # 前端Vue3项目
│   └── src/
│       ├── api/                   # API接口
│       ├── views/                 # 页面视图
│       ├── components/            # 公共组件
│       ├── store/                 # Pinia状态
│       ├── router/                # 路由配置
│       ├── utils/                 # 工具函数
│       └── layout/                # 布局组件
└── sql/
    └── init.sql                   # 数据库初始化脚本
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 1. 数据库初始化
```bash
mysql -u root -p < sql/init.sql
```

### 2. 启动后端
```bash
cd guarantee-finance-backend
mvn spring-boot:run
```
后端服务运行在: http://localhost:8080

Swagger文档: http://localhost:8080/swagger-ui.html

### 3. 启动前端
```bash
cd guarantee-finance-frontend
npm install
npm run dev
```
前端服务运行在: http://localhost:5173

## 核心功能模块

1. **系统管理** - 机构/用户/角色/菜单/审批流程管理
2. **基础信息同步** - 客户/产品/费率同步
3. **收款单管理** - 收款单录入/审核/分担收入/追偿到款
4. **付款单管理** - 付款单录入/审核/退费/代偿/追回分配
5. **银行对账** - 银行流水导入/自动对账/余额调节
6. **银企直连** - 银行账户配置/支付指令/连接日志
7. **会计平台** - 凭证生成/模板管理/客户核算设置
8. **CFCA签名** - 数字证书管理/签名日志
9. **NC Cloud对接** - NC同步日志/异常处理

## 默认账号
- 用户名: admin
- 密码: admin123
