# 欣铁面试项目开发任务说明

## 一、项目简介

本项目是一个基于 Spring Boot + Vue 3 的简化版用户管理系统，作为技术面试的开发任务基础。

### 技术栈

| 层级   | 技术                                                   |
| ------ | ------------------------------------------------------ |
| 后端   | Spring Boot 4.0.6, Java 26, JPA/Hibernate, Flyway, JWT |
| 前端   | Vue 3.4, Vite 5, TypeScript, Element Plus, Pinia       |
| 数据库 | MySQL 8.0                                              |
| 构建   | Maven 3.6+, npm                                        |

### 已有功能

- **用户登录**：基于 JWT 的身份认证，登录后获取 token
- **用户管理**：账号的增、删、改、查（CRUD）操作
- **侧边栏导航**：左侧导航栏，当前仅包含"用户管理"菜单

---

## 二、开发环境搭建

### 1. 数据库准备

在您自己的环境中安装MySql数据库，新建xt_interview
数据库连接信息（默认）：

- 地址：`localhost:3306`
- 用户名：`root`
- 密码：`Tlq_888888`
- 数据库名：`xt_interview`

### 2. 默认账号

- 用户名：`admin`
- 密码：`123456`

---

## 三、开发任务

请基于现有项目，完成以下 **2 个必做任务** 和 **1 个选做任务**。

---

### 任务一：部门管理模块（必做）

#### 需求描述

新增一个部门管理模块，实现部门的增删改查功能。

#### 功能要求

1. **数据库**：
   - 新建 `部门` 相关的 Flyway 迁移脚本
   - 要求脚本创建 `sys_department` 表，字段如下：

   | 字段        | 类型                                                           | 说明                         |
   | ----------- | -------------------------------------------------------------- | ---------------------------- |
   | id          | BIGINT, AUTO_INCREMENT, 主键                                   | 部门ID                       |
   | name        | VARCHAR(100), NOT NULL                                         | 部门名称                     |
   | code        | VARCHAR(50), NOT NULL, UNIQUE                                  | 部门编码                     |
   | description | VARCHAR(500)                                                   | 部门描述                     |
   | status      | INT DEFAULT 1                                                  | 状态：0-禁用，1-启用         |
   | deleted     | INT DEFAULT 0                                                  | 是否删除：0-未删除，1-已删除 |
   | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP                             | 创建时间                     |
   | update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间                     |
   - 插入 2 条测试数据（如：研发部、运维部）

2. **后端**：
   - 参考用户管理模块，创建 `部门管理模块`所需的 实体类、接口、Service、实现、Controller。
   - 实现以下接口

   | 方法   | 路径                    | 说明               |
   | ------ | ----------------------- | ------------------ |
   | GET    | `/api/departments`      | 获取所有部门       |
   | GET    | `/api/departments/{id}` | 根据ID获取部门     |
   | POST   | `/api/departments`      | 创建部门           |
   | PUT    | `/api/departments/{id}` | 更新部门           |
   | DELETE | `/api/departments/{id}` | 删除部门（软删除） |

3. **前端**：
   - 新增 部门管理的页面：`DepartmentManagement.vue` 页面，参考 `UserManagement.vue` 的实现方式
   - 在侧边栏导航中新增"部门管理"菜单项
   - 在路由配置中新增部门管理路由
   - 实现新增部门相关的 API 封装

---

### 任务二：用户-部门关联（必做）

#### 需求描述

在任务一的基础上，为用户添加所属部门，实现用户与部门的关联。

#### 功能要求

1. **数据库**：
   - 新建 Flyway 迁移脚本，给 `sys_account` 表新增 `department_id` 字段（BIGINT, 可空, 外键关联 `sys_department.id`）

2. **后端**：
   - 修改 `Account` 模块相关代码，增加部门相关信息，确保Account的增删改查结构表结构一致。

3. **前端**：
   - 用户管理页面的添加/编辑表单中新增"所属部门"下拉选择框
   - 用户列表表格中新增"所属部门"列，显示部门名称
   - 添加/编辑时部门为可选字段

---

### 任务三：用户列表搜索与分页（选做）

#### 需求描述

当用户数量较多时，需要支持按条件搜索和分页展示。

#### 功能要求

1. **后端**：
   - 新增 `GET /api/accounts/page` 分页查询接口
   - 支持按用户名模糊搜索、按状态筛选、按权限级别筛选
   - 支持分页参数 `page`（页码，从0开始）和 `size`（每页条数）
   - 返回分页数据：`{ list, totalElements, totalPages, currentPage, pageSize }`

2. **前端**：
   - 用户管理页面新增搜索栏（用户名输入框、状态下拉、权限级别下拉、搜索按钮）
   - 表格底部新增 `el-pagination` 分页组件
   - 搜索和翻页时调用分页接口

---

## 四、提交要求

### 1. 代码提交

您可以选择以下任一方式提交：

**方式一：Fork 仓库（推荐）**

1. 访问 https://github.com/iamtcby5188/xt_interview ，点击右上角 **Fork** 将仓库复制到您自己的 GitHub 账号
2. Fork 后请在 Settings 中将仓库设为 **Public**，方便我们查看
3. 克隆您 fork 后的仓库到本地，完成任务后 commit 并 push
4. 将您的仓库地址发给我们（如 `https://github.com/您的用户名/xt_interview`）

**方式二：ZIP 包开发**

1. 从以下地址下载项目 ZIP 包：https://github.com/iamtcby5188/xt_interview/archive/refs/heads/master.zip
2. 解压后进行开发，完成后将代码打包为 ZIP 文件发给我们

**开发环境下载**

如果您需要开发环境，可以从以下链接下载（包含 JDK、Maven、Node.js 等）：

- 链接: https://pan.baidu.com/s/1VRjdpA5fk89emDmuJpEZsA?pwd=a5p7
- 提取码: a5p7

### 2. 验收标准

| 验收项         | 要求                                            |
| -------------- | ----------------------------------------------- |
| 后端编译       | `mvn compile` 无报错                            |
| 后端启动       | `mvn spring-boot:run` 正常启动，Flyway 迁移成功 |
| 前端启动       | `npm run dev` 正常启动，页面可访问              |
| 登录功能       | 使用 admin/123456 可正常登录                    |
| 任务一         | 部门 CRUD 功能正常，侧边栏显示部门管理菜单      |
| 任务二         | 用户关联部门正常，添加/编辑用户时可选择部门     |
| 任务三（选做） | 搜索和分页功能正常                              |

---
