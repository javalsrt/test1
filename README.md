# 基于RAG智能学习平台

## 项目结构

```
test1pj/
├── backend/              # Spring Boot 后端 (端口 8080)
│   ├── pom.xml
│   └── src/main/java/com/znxsgl/
│       ├── ZnxsglApplication.java
│       ├── config/        # Security、JWT
│       ├── controller/    # AuthController
│       ├── dto/           # LoginRequest/Response
│       ├── entity/        # User
│       ├── repository/    # UserRepository
│       └── service/       # AuthService
│
├── web-admin/            # 教师管理端
│   ├── login.html        # 登录页 (iOS简约风格)
│   └── dashboard.html    # 管理后台仪表盘
│
├── android/              # 学生 Android 端
│   └── app/src/main/
│       ├── java/com/znxsgl/student/
│       │   ├── LoginActivity.java
│       │   ├── model/     # LoginRequest/Response
│       │   └── network/   # Retrofit API
│       └── res/layout/activity_login.xml
│
└── init_data.sql          # 测试账号数据
```

## 测试账号 (密码统一：123456)

| 账号 | 角色 | 平台 |
|------|------|------|
| teacher1 | 教师 | Web 管理端 login.html |
| teacher2 | 教师 | Web 管理端 login.html |
| admin | 管理员 | Web 管理端 login.html |
| student1 | 学生 | Android 端 |
| student2 | 学生 | Android 端 |

## 启动步骤

### 1. 数据库初始化
```sql
-- 确保已执行建表脚本 znxsglTest数据库建表.sql
-- 然后执行初始数据
source init_data.sql;
```

### 2. 启动后端
```bash
cd backend
mvn spring-boot:run
```
后端运行在 http://localhost:8080

### 3. 打开 Web 管理端
直接用浏览器打开 `web-admin/login.html`，或用 Live Server 启动

### 4. Android 端
用 Android Studio 打开 `android/` 目录，编译运行
（默认连接 10.0.2.2:8080，模拟器可访问宿主机）
