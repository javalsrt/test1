/*
 Navicat Premium Dump SQL

 Source Server         : lsrt
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : znxsgltest

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 30/05/2026 14:27:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名',
  `user_id` bigint NOT NULL COMMENT '发送者用户ID',
  `sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '发送者姓名',
  `sender_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'student/teacher/ai',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course`(`course_name` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_course_time`(`course_name` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `chat_message_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程聊天消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (1, '数据结构与算法基础', 10, '张明', 'student', '111', 0, '2026-05-24 22:07:49');
INSERT INTO `chat_message` VALUES (2, 'Python程序设计', 10, '张明', 'student', '6546545', 0, '2026-05-24 22:07:58');
INSERT INTO `chat_message` VALUES (3, '数据结构与算法基础', 10, '张明', 'student', '564894549564', 0, '2026-05-24 22:08:05');
INSERT INTO `chat_message` VALUES (4, 'Python程序设计', 10, '张明', 'student', '你好', 0, '2026-05-24 22:39:34');
INSERT INTO `chat_message` VALUES (5, 'Python程序设计', 10, '张明', 'student', '你好', 0, '2026-05-24 22:40:13');
INSERT INTO `chat_message` VALUES (6, 'Python程序设计', 10, '张明', 'student', 'ni', 0, '2026-05-24 22:40:53');
INSERT INTO `chat_message` VALUES (7, 'Python程序设计', 10, '张明', 'ai', '（AI 暂未返回内容，请稍后重试）', 0, '2026-05-24 22:41:34');
INSERT INTO `chat_message` VALUES (8, 'Python程序设计', 10, '张明', 'ai', '（AI 暂未返回内容，请稍后重试）', 0, '2026-05-24 22:42:13');
INSERT INTO `chat_message` VALUES (9, 'Python程序设计', 10, '张明', 'ai', '（AI 暂未返回内容，请稍后重试）', 0, '2026-05-24 22:42:53');
INSERT INTO `chat_message` VALUES (10, '数据结构与算法基础', 10, '张明', 'student', '你好', 0, '2026-05-24 22:43:49');
INSERT INTO `chat_message` VALUES (11, '数据结构与算法基础', 10, '张明', 'student', '你好', 0, '2026-05-24 22:44:07');
INSERT INTO `chat_message` VALUES (12, '数据结构与算法基础', 10, '张明', 'ai', '（AI 未返回内容，请检查模型名是否正确）', 0, '2026-05-24 22:45:50');
INSERT INTO `chat_message` VALUES (13, '数据结构与算法基础', 10, '张明', 'ai', '（AI 未返回内容，请检查模型名是否正确）', 0, '2026-05-24 22:46:08');
INSERT INTO `chat_message` VALUES (14, '数据结构与算法基础', 10, '张明', 'student', '你好', 0, '2026-05-24 22:46:40');
INSERT INTO `chat_message` VALUES (15, '数据结构与算法基础', 10, '张明', 'student', '什么是二叉树', 0, '2026-05-24 22:47:04');
INSERT INTO `chat_message` VALUES (16, '数据结构与算法基础', 10, '张明', 'student', '什么是二叉树', 0, '2026-05-24 22:47:33');
INSERT INTO `chat_message` VALUES (17, '数据结构与算法基础', 10, '张明', 'ai', '你好！欢迎来到《数据结构与算法基础》课程学习～  \n我是你的智能助教，可以帮你解答关于**线性表、栈与队列、树与二叉树、图、查找、排序**等核心内容的问题，也可以协助你理解算法思想、复杂度分析、代码实现等。\n\n请问今天你想了解哪部分内容？😊', 0, '2026-05-24 22:48:41');
INSERT INTO `chat_message` VALUES (18, '数据结构与算法基础', 10, '张明', 'ai', '在《数据结构与算法基础》课程中，**二叉树（Binary Tree）** 是一种重要的非线性数据结构，其定义如下：\n\n> **二叉树** 是由一个根节点和两棵互不相交的子树组成的有序树，这两棵子树分别称为**左子树**和**右子树**，且左、右子树本身也都是二叉树。\n\n### 核心特征：\n1. **每个节点最多有两个子节点**：即左孩子和右孩子；\n2. **子树有左右之分**：即使某节点只有一个孩子，也必须明确是左孩子还是右孩子（这是“有序”的体现）；\n3. **递归定义**：空树（null）也是二叉树；若 T 是二叉树，则其左子树 L 和右子树 R 也都是二叉树。\n\n### 示例（图形表示）：\n```\n        A\n       / \\\n      B   C\n     / \\   \\\n    D   E   F\n```\n- A 是根节点；\n- B 是 A 的左孩子，C 是 A 的右孩子；\n- D、E 是 B 的左、右孩子；\n- F 是 C 的右孩子（C 无左孩子）；\n- D、E、F 均为叶子节点（无子节点）。\n\n### 常见类型（课程重点）：\n- **满二叉树**：除叶子层外，每层节点数都达到最大，且所有叶子都在最底层。\n- **完全二叉树**：除最后一层外，其余层均满；最后一层节点靠左连续排列（堆常用此结构）。\n- **二叉搜索树（BST）**：左子树所有节点值 < 根节点值 < 右子树所有节点值（后续章节重点）。\n\n### 存储方式：\n- **链式存储**（最常用）：每个节点含 `data`、`leftChild`、`rightChild` 指针；\n- **顺序存储**（适用于完全二叉树）：用数组按层序编号存储（如 `i` 节点的左孩子为 `2i+1`，右孩子为 `2i+2`）。\n\n需要我进一步讲解遍历方法（前序/中序/后序/层序）、性质（如 n 个节点的二叉树有 n+1 个空指针），或画图演示吗？', 0, '2026-05-24 22:49:05');
INSERT INTO `chat_message` VALUES (19, '数据结构与算法基础', 10, '张明', 'ai', '在《数据结构与算法基础》中，**二叉树（Binary Tree）** 是一种重要的非线性数据结构，其定义如下：\n\n> **二叉树** 是由一个根节点和两棵互不相交的子树组成的有序树，这两棵子树分别称为**左子树**和**右子树**，且它们本身也都是二叉树。\n\n### 核心特征：\n1. **每个节点最多有两个子节点**：即左孩子和右孩子（可以为空）；\n2. **子树有序**：左子树和右子树有严格区分，不能互换（例如：左空右非空 ≠ 左非空右空）；\n3. **递归结构**：二叉树是递归定义的——空树是二叉树；若 T 是二叉树，则 `T = (root, left-subtree, right-subtree)` 也是二叉树，其中 left-subtree 和 right-subtree 均为二叉树。\n\n### 基本术语：\n- **根节点（Root）**：没有父节点的唯一节点（空树无根）；\n- **叶子节点（Leaf）**：没有子节点的节点；\n- **度（Degree）**：节点的子节点个数（二叉树中节点度 ∈ {0,1,2}）；\n- **深度/高度（Depth/Height）**：从根到某节点的路径长度（根深度为0或1，依约定而定）；整棵树的高度是根节点的高度；\n- **层（Level）**：根为第1层（或0层），子节点比父节点大1层。\n\n### 常见类型（后续课程会深入）：\n- 满二叉树（Full Binary Tree）：每个非叶节点都有两个子节点；\n- 完全二叉树（Complete Binary Tree）：除最后一层外，其余层全满，且最后一层节点靠左对齐；\n- 二叉搜索树（BST）：左子树所有节点值 < 根 < 右子树所有节点值；\n- 平衡二叉树（如AVL树、红黑树）：左右子树高度差受限，保证操作效率。\n\n### 示例（图形表示）：\n```\n        A\n       / \\\n      B   C\n     / \\   \\\n    D   E   F\n```\n这是一棵含6个节点的二叉树，其中：\n- 根为 A；\n- B 是 A 的左孩子，C 是 A 的右孩子；\n- D、E 是 B 的左右孩子；\n- F 是 C 的右孩子；\n- D、E、F 是叶子节点。\n\n---\n\n📌 提示：二叉树是后续学习**遍历（前序/中序/后序/层序）**、**线索化**、**堆（Heap）**、**二叉搜索树**等高级结构的基础，请务必掌握其基本性质与递归思想。\n\n需要我为你画出递归遍历过程，或用代码实现吗？', 0, '2026-05-24 22:49:34');
INSERT INTO `chat_message` VALUES (20, '数据结构与算法基础', 10, '张明', 'student', '什么是二叉树', 0, '2026-05-24 22:52:15');
INSERT INTO `chat_message` VALUES (21, '数据结构与算法基础', 10, '张明', 'ai', '在《数据结构与算法基础》课程中，**二叉树（Binary Tree）** 是一种重要的非线性数据结构，其定义如下：\n\n> **二叉树** 是由一个根节点和两棵互不相交的子树组成的有序树，这两棵子树分别称为**左子树**和**右子树**，且左、右子树本身也都是二叉树。\n\n### 核心特征：\n1. **每个节点最多有两个子节点**：即左孩子和右孩子；\n2. **子树有左右之分**：即使某节点只有一个孩子，也必须明确是左孩子还是右孩子（这是“有序”的体现）；\n3. **递归定义**：空树（无节点）也是二叉树；若T是一个二叉树，则它可表示为：  \n   `T = (root, left-subtree, right-subtree)`，其中 left-subtree 和 right-subtree 均为二叉树。\n\n### 举例：\n```\n       A\n      / \\\n     B   C\n    / \\   \\\n   D   E   F\n```\n这是一个典型的二叉树：A 是根节点，B 是 A 的左孩子，C 是 A 的右孩子；D、E 是 B 的左右孩子，F 是 C 的右孩子。\n\n### 常见类型（后续会深入学习）：\n- 满二叉树（Full Binary Tree）  \n- 完全二叉树（Complete Binary Tree）  \n- 平衡二叉树（AVL Tree）  \n- 二叉搜索树（BST）\n\n### 存储方式：\n- **链式存储**：每个节点包含数据域、左指针、右指针（最常用）；\n- **顺序存储**：用数组按层次编号存储（适用于完全二叉树）。\n\n如果你需要进一步了解二叉树的遍历（前序、中序、后序、层序）、构造、应用（如表达式树、Huffman树）或相关算法（如求深度、叶子结点数等），欢迎继续提问！', 0, '2026-05-24 22:52:28');
INSERT INTO `chat_message` VALUES (22, '微信小程序设计', 10, '张明', 'student', '你好', 0, '2026-05-24 22:58:01');
INSERT INTO `chat_message` VALUES (23, '微信小程序设计', 10, '张明', 'ai', '你好！欢迎来到《微信小程序设计》课程～😊  \n我是你的智能助教，可以帮你解答关于微信小程序开发的各类问题，比如：\n\n- 小程序基础结构（app.json、page.json、wxml、wxss、js）\n- 页面跳转与路由（navigator、wx.navigateTo 等）\n- 数据绑定与事件处理\n- 组件使用（view、text、button、scroll-view 等）\n- 云开发（云函数、云数据库、云存储）\n- 实战项目指导（如：todo list、天气小程序、电商首页等）\n\n你目前在学习哪部分内容？或者有什么具体问题想探讨？我可以为你详细讲解～ 🚀', 0, '2026-05-24 22:58:06');
INSERT INTO `chat_message` VALUES (24, '微信小程序设计', 10, '张明', 'student', '什么是云开发？', 0, '2026-05-24 22:58:26');
INSERT INTO `chat_message` VALUES (25, '微信小程序设计', 10, '张明', 'ai', '在《微信小程序设计》课程中，**云开发（Cloud Development）** 是微信官方提供的一套**后端云服务解决方案**，旨在帮助开发者**无需搭建和运维服务器**，即可快速实现小程序的后端能力。\n\n### 云开发的核心特点：\n1. **免运维**  \n   微信提供云函数、云数据库、云存储等基础设施，开发者无需购买服务器、配置环境或管理运维。\n\n2. **一体化开发体验**  \n   与微信开发者工具深度集成，支持本地调试云函数、可视化管理数据库等。\n\n3. **安全可控**  \n   所有资源通过微信账号授权访问，天然具备用户身份认证（如 `wx.login` 获取 openid），并支持权限控制（如数据库的读写权限规则）。\n\n---\n\n### 云开发的三大核心能力：\n\n| 能力 | 说明 |\n|------|------|\n| **云函数（Cloud Functions）** | 在云端运行的 Node.js 函数，可处理业务逻辑、调用第三方 API、操作数据库等。前端通过 `wx.cloud.callFunction` 调用。 |\n| **云数据库（Cloud Database）** | 基于 JSON 文档型的 NoSQL 数据库，支持实时数据监听、聚合查询、权限控制等，通过 `wx.cloud.database()` 操作。 |\n| **云存储（Cloud Storage）** | 用于存储图片、视频、文件等静态资源，支持上传/下载、签名访问、CDN 加速等。 |\n\n---\n\n### 典型应用场景举例：\n- 用户登录后自动创建个人资料（云函数 + 云数据库）\n- 上传头像到云存储，并将链接存入数据库\n- 实现“点赞”功能：前端调用云函数更新计数器（避免直接操作数据库带来的安全风险）\n\n> 📌 提示：在课程实验中，我们常使用云开发来构建“待办事项”、“留言墙”、“问卷收集”等小程序后端，是本课程重点掌握的技术模块之一。\n\n如果你需要，我可以为你提供一个简单的云开发入门示例代码 👇  \n是否想看？', 0, '2026-05-24 22:58:38');
INSERT INTO `chat_message` VALUES (26, '微信小程序设计', 10, '张明', 'student', '什么是云开发？', 0, '2026-05-24 23:05:12');
INSERT INTO `chat_message` VALUES (27, '微信小程序设计', 10, '张明', 'ai', '云开发（Cloud Development）是微信小程序提供的一套**原生支持的后端云服务**，旨在帮助开发者**无需搭建和维护服务器**，即可快速构建具备完整前后端能力的小程序应用。\n\n### 云开发的核心组成（三大基础能力）：\n\n1. **云函数（Cloud Functions）**\n   - 运行在云端的 Node.js 函数，可被小程序端调用。\n   - 用于处理业务逻辑、访问数据库、调用第三方 API 等。\n   - 支持定时触发、HTTP 触发等多种触发方式。\n   - 示例：用户登录验证、数据统计、发送模板消息等。\n\n2. **云数据库（Cloud Database）**\n   - 基于 MongoDB 的 JSON 文档型数据库，支持实时数据监听。\n   - 数据库操作通过 SDK 在小程序端或云函数中完成，**无需写后端接口**。\n   - 提供安全规则（Security Rules），控制读写权限（如仅登录用户可写入自己的数据）。\n\n3. **云存储（Cloud Storage）**\n   - 用于存储文件（如图片、音频、视频、PDF 等）。\n   - 每个文件有唯一访问路径（URL），支持上传、下载、删除等操作。\n   - 支持按用户/角色设置访问权限。\n\n### 附加能力：\n- **云调用（Cloud API）**：在云函数中直接调用微信开放接口（如发送模板消息、获取用户信息、支付回调等），**免鉴权**。\n- **环境管理**：支持多环境（如开发、测试、生产），便于团队协作与灰度发布。\n- **日志与监控**：提供云函数运行日志、性能监控、错误告警等运维能力。\n\n### 优势：\n✅ 快速上线：省去服务器部署、运维成本  \n✅ 安全可控：基于微信生态的身份体系与权限控制  \n✅ 成本低：免费额度充足，适合中小型项目与学习实践  \n\n> 📌 在《微信小程序设计》课程中，我们会在“后端能力拓展”章节重点讲解云开发的实操，包括：创建云环境、编写云函数、数据库增删改查、云存储上传图片等。欢迎继续提问具体操作步骤！\n\n需要我为你演示一个「用户上传头像 + 存入数据库」的云开发完整案例吗？', 0, '2026-05-24 23:05:27');
INSERT INTO `chat_message` VALUES (28, '微信小程序设计', 10, '张明', 'student', '什么是云开发？', 0, '2026-05-24 23:06:22');
INSERT INTO `chat_message` VALUES (29, '微信小程序设计', 10, '张明', 'ai', '云开发（Cloud Development）是微信小程序提供的一套**原生支持的后端云服务解决方案**，它让开发者无需搭建和维护自己的服务器，即可快速实现小程序的后端能力，如数据库、文件存储、云函数、用户登录等。\n\n### 云开发的核心能力包括：\n\n1. **云数据库（Cloud Database）**  \n   - 基于 MongoDB 的 JSON 文档型数据库  \n   - 支持实时数据监听、权限控制、聚合查询等  \n   - 数据直接在小程序端通过 SDK 操作（需配置安全规则）\n\n2. **云存储（Cloud Storage）**  \n   - 用于存储图片、视频、文件等非结构化数据  \n   - 提供上传、下载、删除等 API  \n   - 支持按路径管理文件与访问权限控制\n\n3. **云函数（Cloud Functions）**  \n   - 运行在云端的 Node.js 函数  \n   - 可被小程序端调用，用于处理敏感逻辑（如支付、数据校验）、定时任务等  \n   - 具备自动伸缩、免运维特性  \n   - 可访问数据库、存储、HTTP 请求等资源\n\n4. **云调用（Cloud API）**  \n   - 在云函数中直接调用微信开放接口（如发送模板消息、获取用户信息等）  \n   - 无需自己申请 AppID/Secret，由云环境自动鉴权\n\n5. **统一身份认证（Login）**  \n   - `wx.login()` + 云函数可实现安全的用户登录与身份识别  \n   - 支持自定义用户体系或与微信 openid 绑定\n\n---\n\n### 优势：\n- ✅ **零运维**：无需购买服务器、部署环境  \n- ✅ **快速上线**：前后端一体化开发，适合 MVP 验证  \n- ✅ **安全可控**：通过安全规则与云函数隔离敏感操作  \n- ✅ **成本低**：基础功能免费，按量计费（尤其适合学生项目/小型应用）\n\n---\n\n### 示例场景：\n> 开发一个“校园失物招领”小程序：  \n> - 用户拍照上传物品 → 存入**云存储**  \n> - 物品信息（标题、地点、时间）写入**云数据库**  \n> - 管理员通过**云函数**审核并推送通知  \n> - 所有操作均在小程序端 + 云开发完成，无传统后端\n\n---\n\n📌 **注意**：使用云开发前需在【微信公众平台】开通「云开发」环境，并在项目中初始化（`wx.cloud.init()`）。\n\n需要我为你演示一个简单的云开发实战步骤吗？比如：从创建环境到写一个云函数存数据？', 0, '2026-05-24 23:06:43');
INSERT INTO `chat_message` VALUES (30, '微信小程序设计', 10, '张明', 'student', '你好', 0, '2026-05-24 23:07:22');
INSERT INTO `chat_message` VALUES (31, '微信小程序设计', 10, '张明', 'ai', '你好！欢迎来到《微信小程序设计》课程～😊  \n我是你的智能助教，可以帮你解答关于微信小程序开发的各类问题，比如：\n\n- 小程序基础结构（app.json、page.json、WXML、WXSS、JS）\n- 页面跳转与路由（`wx.navigateTo`、`wx.redirectTo` 等）\n- 数据绑定与事件处理\n- 云开发（云函数、云数据库、云存储）\n- 组件使用（如 `view`、`text`、`button`、自定义组件等）\n- 调试技巧与真机预览\n\n你目前在学习哪部分内容？或者有什么具体问题想了解？我可以为你详细讲解～ 📱✨', 0, '2026-05-24 23:07:26');
INSERT INTO `chat_message` VALUES (32, '高级网站技术', 10, '张明', 'student', '高级网站技术都有哪些？', 0, '2026-05-24 23:17:32');
INSERT INTO `chat_message` VALUES (33, '高级网站技术', 10, '张明', 'ai', '《高级网站技术》课程通常涵盖现代Web开发中较深入、复杂的技术与架构，旨在培养学生构建高性能、可扩展、安全的Web应用的能力。以下是一些核心内容（具体可能因学校/教材略有差异）：\n\n### 一、前端高级技术\n- **现代JavaScript框架**：React、Vue、Angular 的深入使用（组件化、状态管理如 Redux / Pinia / NgRx、服务端渲染 SSR / 静态站点生成 SSG）\n- **TypeScript**：类型系统、泛型、装饰器等高级特性\n- **前端工程化**：\n  - 构建工具：Webpack、Vite、Rollup 配置与优化\n  - 包管理：npm / yarn / pnpm 高级用法\n  - CI/CD 与自动化测试（Jest、Cypress、Playwright）\n- **Web性能优化**：懒加载、代码分割、缓存策略、Lighthouse 分析、Core Web Vitals\n\n### 二、后端高级技术\n- **服务端框架**：\n  - Node.js（Express/Koa/NestJS）、Python（Django/FastAPI）、Java（Spring Boot）、Go（Gin/Echo）等的进阶开发\n- **RESTful API 与 GraphQL**：\n  - 设计规范、版本控制、错误处理、文档（Swagger/OpenAPI）\n  - GraphQL 查询优化、Schema stitching、订阅（Subscriptions）\n- **微服务架构**：\n  - 服务拆分原则、API网关（如 Kong、Nginx）、服务发现（Consul、Eureka）\n  - 分布式事务（Saga、TCC）、熔断降级（Hystrix、Resilience4j）\n\n### 三、数据库与存储\n- **NoSQL 数据库**：MongoDB、Redis（缓存/消息队列）、Elasticsearch（全文检索）\n- **关系型数据库高级应用**：索引优化、查询执行计划、分库分表（ShardingSphere）\n- **ORM/ODM 进阶**：Sequelize、TypeORM、Prisma、MyBatis Plus 等的性能调优与事务控制\n\n### 四、部署与运维\n- **容器化与编排**：Docker 镜像优化、Kubernetes 基础部署与扩缩容\n- **云原生技术**：Serverless（AWS Lambda、Vercel、Cloud Functions）、FaaS 构\n- **DevOps 实践**：GitLab CI / GitHub Actions 自动化流水线、监控（Prometheus + Grafana）、日志收集（ELK）\n\n### 五、安全与合规\n- **Web安全防护**：XSS、CSRF、SQL注入防御；JWT/OAuth2.0 认证授权\n- **HTTPS/TLS 深入**：证书管理、HSTS、CSP 策略\n- **隐私合规**：GDPR、Cookie 合规处理（如 CCPA）\n\n### 六、前沿方向（选讲）\n- **WebAssembly（Wasm）**：在浏览器中运行高性能代码\n- **PWA（渐进式Web应用）**：离线支持、推送通知、安装到桌面\n- **边缘计算与CDN集成**：如 Cloudflare Workers、Edge Functions\n\n> 📌 提示：本课程常以一个综合项目（如电商系统、社交平台）贯穿始终，要求学生整合上述技术完成全栈开发。\n\n如果你有具体的教材名称或教学大纲，我可以帮你进一步细化重点内容～', 0, '2026-05-24 23:17:56');
INSERT INTO `chat_message` VALUES (34, '高级网站技术', 10, '张明', 'student', '什么蛋糕好吃？', 0, '2026-05-24 23:22:11');
INSERT INTO `chat_message` VALUES (35, '高级网站技术', 10, '张明', 'ai', '你好！不过我主要是《高级网站技术》课程的智能助教，专注于帮助你理解 Web 开发相关的知识，比如 HTML/CSS/JavaScript、前后端框架、RESTful API、数据库集成、部署优化等内容 😊\n\n如果你对“蛋糕”感兴趣——也许是在做美食类网站项目？比如想用 React/Vue 做一个甜品推荐系统？或者用 Node.js + Express 搭建一个蛋糕店在线订购平台？我可以帮你设计架构、写接口、甚至用 JSON 数据模拟蛋糕菜单～ 🎂💻\n\n欢迎随时告诉我你的课程相关需求！', 0, '2026-05-24 23:22:15');
INSERT INTO `chat_message` VALUES (36, '高级网站技术', 10, '张明', 'student', '如何学好这门课程', 0, '2026-05-24 23:23:02');
INSERT INTO `chat_message` VALUES (37, '高级网站技术', 10, '张明', 'ai', '学好《高级网站技术》这门课程，关键在于**理论扎实 + 实践深入 + 项目驱动 + 持续迭代**。以下是我为你整理的系统性学习建议，结合课程特点（通常涵盖：现代前端框架、后端架构、RESTful API、数据库优化、部署运维、安全与性能等）：\n\n---\n\n### ✅ 一、明确课程目标与知识图谱\n先理清本课程的核心模块（参考常见大纲）：\n- 前端进阶：React/Vue/Next.js 等框架原理与工程化  \n- 后端开发：Node.js / Python Flask/Django / Java Spring Boot 等  \n- 全栈整合：前后端分离、JWT/OAuth2 认证、WebSocket、GraphQL  \n- 数据层：SQL/NoSQL 优化、ORM、缓存（Redis）、消息队列  \n- 部署与运维：Docker、Nginx、CI/CD（GitHub Actions / Jenkins）  \n- 安全与性能：XSS/CSRF防护、CORS、懒加载、服务端渲染（SSR）、PWA  \n\n📌 **行动建议**：  \n- 获取课程 syllabus（教学大纲），标出重点章节；  \n- 绘制自己的“知识地图”，标注已掌握/待突破点。\n\n---\n\n### ✅ 二、分阶段学习策略\n\n| 阶段 | 目标 | 方法 |\n|------|------|------|\n| 📘 **基础巩固期**（1–2周） | 理解核心概念，避免“只会抄代码” | - 精读教材/官方文档（如 MDN、Vue Guide、Express.js 官网）<br>- 手写简易版轮子（如：手写一个 mini-router、简易状态管理） |\n| 💻 **实践深化期**（3–6周） | 从“能跑”到“懂原理” | - 每章配套小实验（如：用原生 JS 实现 Vue 响应式、用 Express 写 REST API）<br>- 对比不同方案（如：传统 MVC vs. SPA vs. SSR） |\n| 🧩 **项目整合期**（持续） | 构建完整产品思维 | - 个人项目：博客系统 → 电商后台 → 实时协作工具<br>- 使用真实技术栈（如：Next.js + Prisma + PostgreSQL + Vercel）<br>- 强制自己写单元测试 & 性能分析报告 |\n| 🔍 **拓展反思期** | 跳出课程看行业 | - 阅读 GitHub 热门项目源码（如：Nuxt.js、Strapi）<br>- 关注 Web.dev、CSS-Tricks、InfoQ 技术趋势 |\n\n---\n\n### ✅ 三、高效学习技巧\n- **费曼学习法**：尝试向同学/自己讲解“为什么用 Redis 缓存而不用内存对象？”  \n- **问题驱动学习**：遇到报错不要直接搜解决方案，先查文档 + 看源码调用栈  \n- **代码审查习惯**：提交前自问：是否可读？是否可测？是否安全？是否可扩展？  \n- **善用工具链**：  \n  - DevTools 深度调试（Network、Performance、Memory）  \n  - Chrome Lighthouse 做性能审计  \n  - Postman / Insomnia 测试 API  \n  - Git 提交规范（如 Conventional Commits）\n\n---\n\n### ✅ 四、课程专属加分项\n- 主动参与课堂 Demo 演示（哪怕只是改一行代码并解释影响）  \n- 在作业中加入“设计决策说明”（例如：为何选 MongoDB 而非 MySQL？）  \n- 提交 GitHub 仓库时附上 `ARCHITECTURE.md` 文档，展示系统设计思考  \n\n> 🌟 小提醒：这门课不是“学会语法”，而是培养**构建健壮、可维护、可扩展的 Web 系统的能力**。你写的每一行代码，都应该能回答：“它解决了什么问题？代价是什么？如何改进？”\n\n---\n\n如果你告诉我你当前的进度（比如：刚学完 Express，还是正在做 React 项目？），我可以为你定制一份**个性化学习计划表** 😊  \n需要吗？', 0, '2026-05-24 23:23:27');
INSERT INTO `chat_message` VALUES (38, '高级网站技术', 10, '张明', 'student', 'we', 0, '2026-05-24 23:24:44');
INSERT INTO `chat_message` VALUES (39, '高级网站技术', 10, '张明', 'ai', '你好！请问你是想了解《高级网站技术》课程的某个具体主题吗？比如：\n\n- 前端框架（如 React、Vue、Angular）  \n- 后端开发（Node.js、Express、RESTful API 设计）  \n- 数据库与 ORM（MongoDB、MySQL、Sequelize、Mongoose）  \n- 全栈项目架构与部署（Docker、Nginx、CI/CD）  \n- 安全性（JWT、OAuth、CORS、XSS/CSRF 防护）  \n- 性能优化（懒加载、服务端渲染 SSR、CDN、缓存策略）  \n\n请告诉我你的具体问题或学习目标，我会为你提供针对性的帮助 😊', 0, '2026-05-24 23:24:48');
INSERT INTO `chat_message` VALUES (40, '多媒体课件设计与开发', 10, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 08:30-09:55', 1, '2026-05-29 16:04:05');
INSERT INTO `chat_message` VALUES (41, '多媒体课件设计与开发', 11, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 08:30-09:55', 0, '2026-05-29 16:04:05');
INSERT INTO `chat_message` VALUES (42, '多媒体课件设计与开发', 12, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 08:30-09:55', 0, '2026-05-29 16:04:05');
INSERT INTO `chat_message` VALUES (43, '多媒体课件设计与开发', 13, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 08:30-09:55', 0, '2026-05-29 16:04:05');
INSERT INTO `chat_message` VALUES (44, '多媒体课件设计与开发', 10, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 1, '2026-05-29 16:23:29');
INSERT INTO `chat_message` VALUES (45, '多媒体课件设计与开发', 11, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 16:23:29');
INSERT INTO `chat_message` VALUES (46, '多媒体课件设计与开发', 12, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 16:23:29');
INSERT INTO `chat_message` VALUES (47, '多媒体课件设计与开发', 13, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 16:23:29');
INSERT INTO `chat_message` VALUES (48, '多媒体课件设计与开发', 10, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 1, '2026-05-29 16:25:03');
INSERT INTO `chat_message` VALUES (49, '多媒体课件设计与开发', 11, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 16:25:03');
INSERT INTO `chat_message` VALUES (50, '多媒体课件设计与开发', 12, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 16:25:03');
INSERT INTO `chat_message` VALUES (51, '多媒体课件设计与开发', 13, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 16:25:03');
INSERT INTO `chat_message` VALUES (52, '多媒体课件设计与开发', 10, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 1, '2026-05-29 16:57:40');
INSERT INTO `chat_message` VALUES (53, '多媒体课件设计与开发', 11, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 16:57:40');
INSERT INTO `chat_message` VALUES (54, '多媒体课件设计与开发', 12, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 16:57:40');
INSERT INTO `chat_message` VALUES (55, '多媒体课件设计与开发', 13, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 16:57:40');
INSERT INTO `chat_message` VALUES (56, '多媒体课件设计与开发', 10, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 1, '2026-05-29 17:09:20');
INSERT INTO `chat_message` VALUES (57, '多媒体课件设计与开发', 11, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 17:09:20');
INSERT INTO `chat_message` VALUES (58, '多媒体课件设计与开发', 12, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 17:09:20');
INSERT INTO `chat_message` VALUES (59, '多媒体课件设计与开发', 13, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周日 19:10-21:30', 0, '2026-05-29 17:09:20');
INSERT INTO `chat_message` VALUES (60, '多媒体课件设计与开发', 10, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周五 08:30-09:55', 1, '2026-05-29 17:09:41');
INSERT INTO `chat_message` VALUES (61, '多媒体课件设计与开发', 11, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周五 08:30-09:55', 0, '2026-05-29 17:09:41');
INSERT INTO `chat_message` VALUES (62, '多媒体课件设计与开发', 12, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周五 08:30-09:55', 0, '2026-05-29 17:09:41');
INSERT INTO `chat_message` VALUES (63, '多媒体课件设计与开发', 13, '张明远', 'teacher', '📢 课程「多媒体课件设计与开发」排课已更新：周五 08:30-09:55', 0, '2026-05-29 17:09:41');
INSERT INTO `chat_message` VALUES (64, '多媒体课件设计与开发', 10, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第18周 · 周六 1-2节', 1, '2026-05-29 17:14:28');
INSERT INTO `chat_message` VALUES (65, '多媒体课件设计与开发', 11, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第18周 · 周六 1-2节', 0, '2026-05-29 17:14:28');
INSERT INTO `chat_message` VALUES (66, '多媒体课件设计与开发', 12, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第18周 · 周六 1-2节', 0, '2026-05-29 17:14:28');
INSERT INTO `chat_message` VALUES (67, '多媒体课件设计与开发', 13, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第18周 · 周六 1-2节', 0, '2026-05-29 17:14:28');
INSERT INTO `chat_message` VALUES (68, '多媒体课件设计与开发', 10, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周六 1-2节', 1, '2026-05-29 17:15:15');
INSERT INTO `chat_message` VALUES (69, '多媒体课件设计与开发', 11, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周六 1-2节', 0, '2026-05-29 17:15:15');
INSERT INTO `chat_message` VALUES (70, '多媒体课件设计与开发', 12, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周六 1-2节', 0, '2026-05-29 17:15:15');
INSERT INTO `chat_message` VALUES (71, '多媒体课件设计与开发', 13, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周六 1-2节', 0, '2026-05-29 17:15:15');
INSERT INTO `chat_message` VALUES (72, '多媒体课件设计与开发', 10, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 2-3节', 1, '2026-05-29 17:15:30');
INSERT INTO `chat_message` VALUES (73, '多媒体课件设计与开发', 11, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 2-3节', 0, '2026-05-29 17:15:30');
INSERT INTO `chat_message` VALUES (74, '多媒体课件设计与开发', 12, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 2-3节', 0, '2026-05-29 17:15:30');
INSERT INTO `chat_message` VALUES (75, '多媒体课件设计与开发', 13, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 2-3节', 0, '2026-05-29 17:15:31');
INSERT INTO `chat_message` VALUES (76, '多媒体课件设计与开发', 10, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 3-3节', 1, '2026-05-29 17:22:20');
INSERT INTO `chat_message` VALUES (77, '多媒体课件设计与开发', 11, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 3-3节', 0, '2026-05-29 17:22:20');
INSERT INTO `chat_message` VALUES (78, '多媒体课件设计与开发', 12, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 3-3节', 0, '2026-05-29 17:22:20');
INSERT INTO `chat_message` VALUES (79, '多媒体课件设计与开发', 13, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 3-3节', 0, '2026-05-29 17:22:20');
INSERT INTO `chat_message` VALUES (80, '多媒体课件设计与开发', 10, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 2-3节', 1, '2026-05-29 17:22:35');
INSERT INTO `chat_message` VALUES (81, '多媒体课件设计与开发', 11, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 2-3节', 0, '2026-05-29 17:22:35');
INSERT INTO `chat_message` VALUES (82, '多媒体课件设计与开发', 12, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 2-3节', 0, '2026-05-29 17:22:35');
INSERT INTO `chat_message` VALUES (83, '多媒体课件设计与开发', 13, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第17周 · 周日 2-3节', 0, '2026-05-29 17:22:35');
INSERT INTO `chat_message` VALUES (84, '多媒体课件设计与开发', 10, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 4-5节', 1, '2026-05-29 17:23:03');
INSERT INTO `chat_message` VALUES (85, '多媒体课件设计与开发', 11, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 4-5节', 0, '2026-05-29 17:23:03');
INSERT INTO `chat_message` VALUES (86, '多媒体课件设计与开发', 12, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 4-5节', 0, '2026-05-29 17:23:03');
INSERT INTO `chat_message` VALUES (87, '多媒体课件设计与开发', 13, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 4-5节', 0, '2026-05-29 17:23:03');
INSERT INTO `chat_message` VALUES (88, '多媒体课件设计与开发', 10, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 5-6节', 1, '2026-05-29 17:23:23');
INSERT INTO `chat_message` VALUES (89, '多媒体课件设计与开发', 11, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 5-6节', 0, '2026-05-29 17:23:23');
INSERT INTO `chat_message` VALUES (90, '多媒体课件设计与开发', 12, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 5-6节', 0, '2026-05-29 17:23:23');
INSERT INTO `chat_message` VALUES (91, '多媒体课件设计与开发', 13, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 5-6节', 0, '2026-05-29 17:23:23');
INSERT INTO `chat_message` VALUES (92, '多媒体课件设计与开发', 10, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 4-5节', 1, '2026-05-30 01:50:10');
INSERT INTO `chat_message` VALUES (93, '多媒体课件设计与开发', 11, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 4-5节', 0, '2026-05-30 01:50:10');
INSERT INTO `chat_message` VALUES (94, '多媒体课件设计与开发', 12, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 4-5节', 0, '2026-05-30 01:50:10');
INSERT INTO `chat_message` VALUES (95, '多媒体课件设计与开发', 13, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 4-5节', 0, '2026-05-30 01:50:10');
INSERT INTO `chat_message` VALUES (96, '多媒体课件设计与开发', 10, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 5-6节', 1, '2026-05-30 01:50:23');
INSERT INTO `chat_message` VALUES (97, '多媒体课件设计与开发', 11, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 5-6节', 0, '2026-05-30 01:50:23');
INSERT INTO `chat_message` VALUES (98, '多媒体课件设计与开发', 12, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 5-6节', 0, '2026-05-30 01:50:23');
INSERT INTO `chat_message` VALUES (99, '多媒体课件设计与开发', 13, '张明远', 'teacher', '课程「多媒体课件设计与开发」排课已更新\n第15周 · 周日 5-6节', 0, '2026-05-30 01:50:23');

-- ----------------------------
-- Table structure for checkin_record
-- ----------------------------
DROP TABLE IF EXISTS `checkin_record`;
CREATE TABLE `checkin_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `checkin_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实际签到方式',
  `location` json NULL COMMENT '签到位置',
  `checkin_time` datetime NOT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '0缺勤 1正常 2迟到 3补签',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_user`(`task_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `checkin_record_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `checkin_task` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `checkin_record_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '签到记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of checkin_record
-- ----------------------------

-- ----------------------------
-- Table structure for checkin_task
-- ----------------------------
DROP TABLE IF EXISTS `checkin_task`;
CREATE TABLE `checkin_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL,
  `course_id` bigint NULL DEFAULT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `checkin_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'gps/qrcode/face/beacon',
  `location` json NULL COMMENT '位置信息{\"lat\":xx,\"lng\":xx,\"radius\":200}',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0未开始 1进行中 2已结束',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `checkin_task_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `checkin_task_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '签到任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of checkin_task
-- ----------------------------

-- ----------------------------
-- Table structure for class_info
-- ----------------------------
DROP TABLE IF EXISTS `class_info`;
CREATE TABLE `class_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级名称',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '院系',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_info
-- ----------------------------
INSERT INTO `class_info` VALUES (1, '计算机科学与技术2023级1班', '计算机科学与技术', '计算机与信息工程学院', '2023', '2026-05-24 21:00:49');
INSERT INTO `class_info` VALUES (2, '教育技术学2023级1班', '教育技术学', '教育科学学院', '2023', '2026-05-24 21:00:49');

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `course_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程编号',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '授课教师ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '开课院系',
  `semester` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学期 如2025-2026-2',
  `course_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '必修' COMMENT '必修/选修',
  `credit` decimal(3, 1) NULL DEFAULT NULL COMMENT '学分',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '课程描述',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_semester`(`semester` ASC) USING BTREE,
  INDEX `idx_course_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `dept_id`(`dept_id` ASC) USING BTREE,
  CONSTRAINT `course_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `course_ibfk_2` FOREIGN KEY (`dept_id`) REFERENCES `department` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (1, '数据结构与算法基础', 'CS101', 3, 1, '2025-2026-2', '必修', 4.0, '线性表、树、图、排序算法', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (2, 'Python程序设计', 'CS102', 4, 1, '2025-2026-2', '必修', 3.0, 'Python基础语法与数据分析', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (3, '微信小程序设计', 'CS103', 2, 1, '2025-2026-2', '必修', 3.0, 'WXML/WXSS/JS开发', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (4, '高级网站技术', 'CS104', 5, 1, '2025-2026-2', '限选', 3.0, '前端框架与后端API', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (5, '多媒体课件设计与开发', 'CS105', 1, 1, '2025-2026-2', '限选', 2.0, '课件制作工具与设计', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (6, '计算机综合实训', 'CS106', 6, 1, '2025-2026-2', '必修', 4.0, '综合项目开发实践', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (7, '教师书写技能', 'EDU201', 8, 2, '2025-2026-2', '必修', 2.0, '板书与书写训练', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (8, '教师口语', 'EDU202', 8, 2, '2025-2026-2', '必修', 2.0, '普通话与教学表达', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (9, '微课制作', 'EDU203', 8, 2, '2025-2026-2', '限选', 2.0, '微课视频制作', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (10, '教师资格考试实务', 'EDU204', 8, 2, '2025-2026-2', '限选', 2.0, '教师资格考试辅导', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (11, '班主任工作技能训练', 'EDU205', 2, 1, '2025-2026-2', '限选', 1.0, '班级管理技能', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (12, '中国近现代史纲要', 'PUB101', 7, 3, '2025-2026-2', '必修', 3.0, '中国近现代史', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (13, '形势与政策', 'PUB102', 7, 3, '2025-2026-2', '必修', 1.0, '时事政策教育', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (14, '安全教育(六)', 'PUB103', 7, 3, '2025-2026-2', '必修', 1.0, '校园安全教育', '2026-05-24 21:00:49');
INSERT INTO `course` VALUES (24, 'Java基础设计', NULL, 1, NULL, '2025-2026-2', '必修', 2.0, NULL, '2026-05-28 16:44:42');

-- ----------------------------
-- Table structure for course_class
-- ----------------------------
DROP TABLE IF EXISTS `course_class`;
CREATE TABLE `course_class`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `semester` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学期',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_class`(`course_id` ASC, `class_id` ASC, `semester` ASC) USING BTREE,
  INDEX `class_id`(`class_id` ASC) USING BTREE,
  CONSTRAINT `course_class_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `course_class_ibfk_2` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程-班级关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_class
-- ----------------------------
INSERT INTO `course_class` VALUES (1, 1, 1, '2025-2026-2');
INSERT INTO `course_class` VALUES (8, 1, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (2, 2, 1, '2025-2026-2');
INSERT INTO `course_class` VALUES (9, 2, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (3, 3, 1, '2025-2026-2');
INSERT INTO `course_class` VALUES (10, 3, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (4, 4, 1, '2025-2026-2');
INSERT INTO `course_class` VALUES (11, 4, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (5, 5, 1, '2025-2026-2');
INSERT INTO `course_class` VALUES (12, 5, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (6, 6, 1, '2025-2026-2');
INSERT INTO `course_class` VALUES (13, 6, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (14, 7, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (15, 8, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (16, 9, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (17, 10, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (18, 11, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (19, 12, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (20, 13, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (21, 14, 2, '2025-2026-2');
INSERT INTO `course_class` VALUES (29, 24, 1, '2025-2026-2');
INSERT INTO `course_class` VALUES (30, 24, 2, '2025-2026-2');

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '院系名称',
  `dept_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '院系编码',
  `dept_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '教学单位/行政单位',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '院系部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES (1, '计算机与信息工程学院', 'CS', '教学单位', '2026-05-24 21:00:49');
INSERT INTO `department` VALUES (2, '教育科学学院', 'EDU', '教学单位', '2026-05-24 21:00:49');
INSERT INTO `department` VALUES (3, '学生工作处', 'STU', '行政单位', '2026-05-24 21:00:49');

-- ----------------------------
-- Table structure for document
-- ----------------------------
DROP TABLE IF EXISTS `document`;
CREATE TABLE `document`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NULL DEFAULT NULL COMMENT '关联课程，可为空',
  `file_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '阿里云OSS路径',
  `file_size` bigint NULL DEFAULT NULL COMMENT '字节',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'pdf/docx/ppt/jpg/png',
  `doc_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '其他' COMMENT '课件/笔记/试卷/参考书/其他',
  `ocr_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'OCR提取文本',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI摘要',
  `status` tinyint NULL DEFAULT 0 COMMENT '0审核中 1已发布 2驳回',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_doc_user_category`(`user_id` ASC, `doc_category` ASC) USING BTREE,
  INDEX `idx_doc_user_course`(`user_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_doc_status`(`status` ASC) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `document_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `document_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of document
-- ----------------------------

-- ----------------------------
-- Table structure for document_chunk
-- ----------------------------
DROP TABLE IF EXISTS `document_chunk`;
CREATE TABLE `document_chunk`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `document_id` bigint NOT NULL,
  `chunk_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文本分块内容',
  `vector_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '向量库(阿里云DashVector)中的ID',
  `chunk_index` int NOT NULL COMMENT '分块序号',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chunk_doc`(`document_id` ASC) USING BTREE,
  CONSTRAINT `document_chunk_ibfk_1` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文档分块表(向量检索)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of document_chunk
-- ----------------------------

-- ----------------------------
-- Table structure for focus_session
-- ----------------------------
DROP TABLE IF EXISTS `focus_session`;
CREATE TABLE `focus_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `duration_seconds` int NULL DEFAULT 0,
  `started_at` datetime NULL DEFAULT NULL,
  `finished_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `focus_session_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of focus_session
-- ----------------------------
INSERT INTO `focus_session` VALUES (1, 10, 2, '2026-05-25 07:50:44', '2026-05-25 07:50:46', '2026-05-25 15:50:46');
INSERT INTO `focus_session` VALUES (2, 10, 23, '2026-05-25 08:11:33', '2026-05-25 08:11:57', '2026-05-25 16:11:57');
INSERT INTO `focus_session` VALUES (3, 10, 9, '2026-05-25 08:12:05', '2026-05-25 08:12:14', '2026-05-25 16:12:14');
INSERT INTO `focus_session` VALUES (4, 10, 11, '2026-05-25 08:12:30', '2026-05-25 08:12:41', '2026-05-25 16:12:41');
INSERT INTO `focus_session` VALUES (5, 10, 3, '2026-05-25 08:13:15', '2026-05-25 08:13:19', '2026-05-25 16:13:19');
INSERT INTO `focus_session` VALUES (6, 10, 57, '2026-05-25 08:17:31', '2026-05-25 08:17:59', '2026-05-25 16:17:59');
INSERT INTO `focus_session` VALUES (7, 10, 16, '2026-05-25 08:23:15', '2026-05-25 08:23:36', '2026-05-25 16:23:36');
INSERT INTO `focus_session` VALUES (8, 14, 6, '2026-05-25 08:23:47', '2026-05-25 08:23:52', '2026-05-25 16:23:52');
INSERT INTO `focus_session` VALUES (9, 16, 3, '2026-05-25 08:24:26', '2026-05-25 08:24:30', '2026-05-25 16:24:30');
INSERT INTO `focus_session` VALUES (10, 10, 7, '2026-05-25 08:29:30', '2026-05-25 08:29:39', '2026-05-25 16:29:39');
INSERT INTO `focus_session` VALUES (11, 12, 4, '2026-05-25 08:31:58', '2026-05-25 08:32:03', '2026-05-25 16:32:03');
INSERT INTO `focus_session` VALUES (12, 13, 119, '2026-05-25 09:02:25', '2026-05-25 09:04:26', '2026-05-25 17:04:26');
INSERT INTO `focus_session` VALUES (13, 12, 679, '2026-05-25 09:09:40', '2026-05-25 09:21:03', '2026-05-25 17:21:03');

-- ----------------------------
-- Table structure for learning_goal
-- ----------------------------
DROP TABLE IF EXISTS `learning_goal`;
CREATE TABLE `learning_goal`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `goal_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `goal_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '考试/考证/技能/其他',
  `target_date` date NULL DEFAULT NULL,
  `progress` int NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `learning_goal_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习目标表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of learning_goal
-- ----------------------------

-- ----------------------------
-- Table structure for learning_plan
-- ----------------------------
DROP TABLE IF EXISTS `learning_plan`;
CREATE TABLE `learning_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NULL DEFAULT NULL COMMENT '关联课程，可为空',
  `plan_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `plan_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '计划详细内容',
  `start_date` date NULL DEFAULT NULL,
  `end_date` date NULL DEFAULT NULL,
  `progress` int NULL DEFAULT 0 COMMENT '进度百分比 0-100',
  `status` tinyint NULL DEFAULT 0 COMMENT '0待开始 1进行中 2已完成 3已放弃',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `learning_plan_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `learning_plan_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of learning_plan
-- ----------------------------

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL COMMENT '发布者(user.id, role=3)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'all/class/major/user',
  `target_ids` json NULL COMMENT '目标ID列表',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '普通' COMMENT '紧急/重要/普通',
  `send_time` datetime NULL DEFAULT NULL COMMENT '定时发送时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notif_time`(`created_at` ASC) USING BTREE,
  INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------

-- ----------------------------
-- Table structure for notification_receipt
-- ----------------------------
DROP TABLE IF EXISTS `notification_receipt`;
CREATE TABLE `notification_receipt`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notification_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `is_read` tinyint NULL DEFAULT 0 COMMENT '0未读 1已读',
  `read_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notif_user`(`notification_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_receipt_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
  CONSTRAINT `notification_receipt_ibfk_1` FOREIGN KEY (`notification_id`) REFERENCES `notification` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `notification_receipt_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知接收状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification_receipt
-- ----------------------------

-- ----------------------------
-- Table structure for qa_message
-- ----------------------------
DROP TABLE IF EXISTS `qa_message`;
CREATE TABLE `qa_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，同一会话消息聚合',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `answer` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_chunks` json NULL COMMENT '引用的文档分块ID，如[12,45,78]',
  `context_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '综合' COMMENT 'schedule/doc/plan/focus/综合',
  `feedback` tinyint NULL DEFAULT 0 COMMENT '0无 1赞 2踩',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qa_user_time`(`user_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_qa_session`(`session_id` ASC) USING BTREE,
  CONSTRAINT `qa_message_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI问答消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qa_message
-- ----------------------------

-- ----------------------------
-- Table structure for schedule
-- ----------------------------
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NULL DEFAULT NULL,
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `day_of_week` int NOT NULL COMMENT '星期 1-7',
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `start_node` int NOT NULL COMMENT '开始节次 1-13',
  `step` int NOT NULL COMMENT '持续节数',
  `classroom` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '教室',
  `semester` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学期',
  `weeks` json NOT NULL COMMENT '上课周次，如[1,2,3,4,5,6,7,8]',
  `status` tinyint NULL DEFAULT 1 COMMENT '1=正常 0=已下架',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_schedule_user_day`(`user_id` ASC, `day_of_week` ASC) USING BTREE,
  INDEX `idx_schedule_user_semester`(`user_id` ASC, `semester` ASC) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `schedule_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `schedule_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 711 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课表/排课表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of schedule
-- ----------------------------
INSERT INTO `schedule` VALUES (1, 10, NULL, '数据结构与算法基础', 1, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (2, 11, NULL, '数据结构与算法基础', 1, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (3, 12, NULL, '数据结构与算法基础', 1, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (4, 13, NULL, '数据结构与算法基础', 1, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (8, 10, NULL, 'Python程序设计', 1, '10:10:00', '11:35:00', 3, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (9, 11, NULL, 'Python程序设计', 1, '10:10:00', '11:35:00', 3, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (10, 12, NULL, 'Python程序设计', 1, '10:10:00', '11:35:00', 3, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (11, 13, NULL, 'Python程序设计', 1, '10:10:00', '11:35:00', 3, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (15, 10, NULL, '微信小程序设计', 1, '11:40:00', '12:20:00', 5, 1, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (16, 11, NULL, '微信小程序设计', 1, '11:40:00', '12:20:00', 5, 1, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (17, 12, NULL, '微信小程序设计', 1, '11:40:00', '12:20:00', 5, 1, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (18, 13, NULL, '微信小程序设计', 1, '11:40:00', '12:20:00', 5, 1, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (36, 10, NULL, 'Python程序设计', 2, '10:10:00', '11:35:00', 3, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (37, 11, NULL, 'Python程序设计', 2, '10:10:00', '11:35:00', 3, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (38, 12, NULL, 'Python程序设计', 2, '10:10:00', '11:35:00', 3, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (39, 13, NULL, 'Python程序设计', 2, '10:10:00', '11:35:00', 3, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (43, 10, NULL, '数据结构与算法基础', 2, '11:40:00', '12:20:00', 5, 1, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (44, 11, NULL, '数据结构与算法基础', 2, '11:40:00', '12:20:00', 5, 1, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (45, 12, NULL, '数据结构与算法基础', 2, '11:40:00', '12:20:00', 5, 1, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (46, 13, NULL, '数据结构与算法基础', 2, '11:40:00', '12:20:00', 5, 1, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (50, 10, NULL, '微信小程序设计', 2, '14:20:00', '15:45:00', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (51, 11, NULL, '微信小程序设计', 2, '14:20:00', '15:45:00', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (52, 12, NULL, '微信小程序设计', 2, '14:20:00', '15:45:00', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (53, 13, NULL, '微信小程序设计', 2, '14:20:00', '15:45:00', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (57, 10, NULL, '计算机综合实训', 2, '16:00:00', '17:25:00', 8, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (58, 11, NULL, '计算机综合实训', 2, '16:00:00', '17:25:00', 8, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (59, 12, NULL, '计算机综合实训', 2, '16:00:00', '17:25:00', 8, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (60, 13, NULL, '计算机综合实训', 2, '16:00:00', '17:25:00', 8, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (64, 10, NULL, '数据结构与算法基础', 3, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (65, 11, NULL, '数据结构与算法基础', 3, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (66, 12, NULL, '数据结构与算法基础', 3, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (67, 13, NULL, '数据结构与算法基础', 3, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (78, 10, NULL, 'Python程序设计', 3, '11:40:00', '12:20:00', 5, 1, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (79, 11, NULL, 'Python程序设计', 3, '11:40:00', '12:20:00', 5, 1, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (80, 12, NULL, 'Python程序设计', 3, '11:40:00', '12:20:00', 5, 1, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (81, 13, NULL, 'Python程序设计', 3, '11:40:00', '12:20:00', 5, 1, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (85, 10, NULL, '计算机综合实训', 3, '14:20:00', '15:45:00', 6, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (86, 11, NULL, '计算机综合实训', 3, '14:20:00', '15:45:00', 6, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (87, 12, NULL, '计算机综合实训', 3, '14:20:00', '15:45:00', 6, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (88, 13, NULL, '计算机综合实训', 3, '14:20:00', '15:45:00', 6, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (106, 10, NULL, '微信小程序设计', 4, '14:20:00', '15:45:00', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (107, 11, NULL, '微信小程序设计', 4, '14:20:00', '15:45:00', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (108, 12, NULL, '微信小程序设计', 4, '14:20:00', '15:45:00', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (109, 13, NULL, '微信小程序设计', 4, '14:20:00', '15:45:00', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (113, 10, NULL, '计算机综合实训', 5, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (114, 11, NULL, '计算机综合实训', 5, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (115, 12, NULL, '计算机综合实训', 5, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (116, 13, NULL, '计算机综合实训', 5, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (120, 10, NULL, '计算机综合实训', 5, '11:40:00', '12:20:00', 5, 1, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (121, 11, NULL, '计算机综合实训', 5, '11:40:00', '12:20:00', 5, 1, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (122, 12, NULL, '计算机综合实训', 5, '11:40:00', '12:20:00', 5, 1, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (123, 13, NULL, '计算机综合实训', 5, '11:40:00', '12:20:00', 5, 1, '地信-B303(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (127, 10, NULL, 'Python程序设计', 5, '14:20:00', '15:45:00', 6, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (128, 11, NULL, 'Python程序设计', 5, '14:20:00', '15:45:00', 6, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (129, 12, NULL, 'Python程序设计', 5, '14:20:00', '15:45:00', 6, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (130, 13, NULL, 'Python程序设计', 5, '14:20:00', '15:45:00', 6, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (134, 10, NULL, 'Python程序设计', 5, '16:00:00', '17:25:00', 8, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (135, 11, NULL, 'Python程序设计', 5, '16:00:00', '17:25:00', 8, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (136, 12, NULL, 'Python程序设计', 5, '16:00:00', '17:25:00', 8, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (137, 13, NULL, 'Python程序设计', 5, '16:00:00', '17:25:00', 8, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (155, 14, NULL, 'Python程序设计', 1, '14:20:00', '15:45:00', 6, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (156, 15, NULL, 'Python程序设计', 1, '14:20:00', '15:45:00', 6, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (157, 16, NULL, 'Python程序设计', 1, '14:20:00', '15:45:00', 6, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (158, 17, NULL, 'Python程序设计', 1, '14:20:00', '15:45:00', 6, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (162, 14, NULL, 'Python程序设计', 1, '16:00:00', '17:25:00', 8, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (163, 15, NULL, 'Python程序设计', 1, '16:00:00', '17:25:00', 8, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (164, 16, NULL, 'Python程序设计', 1, '16:00:00', '17:25:00', 8, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (165, 17, NULL, 'Python程序设计', 1, '16:00:00', '17:25:00', 8, 2, '武1-316(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (169, 14, NULL, '计算机综合实训', 1, '19:10:00', '20:35:00', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (170, 15, NULL, '计算机综合实训', 1, '19:10:00', '20:35:00', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (171, 16, NULL, '计算机综合实训', 1, '19:10:00', '20:35:00', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (172, 17, NULL, '计算机综合实训', 1, '19:10:00', '20:35:00', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (176, 14, NULL, '微信小程序设计', 2, '08:30:00', '09:55:00', 1, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (177, 15, NULL, '微信小程序设计', 2, '08:30:00', '09:55:00', 1, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (178, 16, NULL, '微信小程序设计', 2, '08:30:00', '09:55:00', 1, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (179, 17, NULL, '微信小程序设计', 2, '08:30:00', '09:55:00', 1, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (183, 14, NULL, '微信小程序设计', 2, '10:10:00', '11:35:00', 3, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (184, 15, NULL, '微信小程序设计', 2, '10:10:00', '11:35:00', 3, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (185, 16, NULL, '微信小程序设计', 2, '10:10:00', '11:35:00', 3, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (186, 17, NULL, '微信小程序设计', 2, '10:10:00', '11:35:00', 3, 2, '地信-B402(机房)', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (190, 14, NULL, '教师口语', 2, '14:20:00', '15:45:00', 6, 2, '武1-114', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (191, 15, NULL, '教师口语', 2, '14:20:00', '15:45:00', 6, 2, '武1-114', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (192, 16, NULL, '教师口语', 2, '14:20:00', '15:45:00', 6, 2, '武1-114', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (193, 17, NULL, '教师口语', 2, '14:20:00', '15:45:00', 6, 2, '武1-114', '2025-2026-2', '[1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (211, 14, NULL, '安全教育(六)', 2, '16:00:00', '17:25:00', 8, 2, '实验2-A504', '2025-2026-2', '[8]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (212, 15, NULL, '安全教育(六)', 2, '16:00:00', '17:25:00', 8, 2, '实验2-A504', '2025-2026-2', '[8]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (213, 16, NULL, '安全教育(六)', 2, '16:00:00', '17:25:00', 8, 2, '实验2-A504', '2025-2026-2', '[8]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (214, 17, NULL, '安全教育(六)', 2, '16:00:00', '17:25:00', 8, 2, '实验2-A504', '2025-2026-2', '[8]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (218, 14, NULL, '班主任工作技能训练', 2, '08:30:00', '09:55:00', 1, 2, '武1-203', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (219, 15, NULL, '班主任工作技能训练', 2, '08:30:00', '09:55:00', 1, 2, '武1-203', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (220, 16, NULL, '班主任工作技能训练', 2, '08:30:00', '09:55:00', 1, 2, '武1-203', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (221, 17, NULL, '班主任工作技能训练', 2, '08:30:00', '09:55:00', 1, 2, '武1-203', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (225, 14, NULL, '班主任工作技能训练', 2, '10:10:00', '11:35:00', 3, 2, '武1-203', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (226, 15, NULL, '班主任工作技能训练', 2, '10:10:00', '11:35:00', 3, 2, '武1-203', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (227, 16, NULL, '班主任工作技能训练', 2, '10:10:00', '11:35:00', 3, 2, '武1-203', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (228, 17, NULL, '班主任工作技能训练', 2, '10:10:00', '11:35:00', 3, 2, '武1-203', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (232, 14, NULL, '数据结构与算法基础', 3, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (233, 15, NULL, '数据结构与算法基础', 3, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (234, 16, NULL, '数据结构与算法基础', 3, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (235, 17, NULL, '数据结构与算法基础', 3, '08:30:00', '09:55:00', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (239, 14, NULL, '数据结构与算法基础', 3, '10:10:00', '11:35:00', 3, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (240, 15, NULL, '数据结构与算法基础', 3, '10:10:00', '11:35:00', 3, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (241, 16, NULL, '数据结构与算法基础', 3, '10:10:00', '11:35:00', 3, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (242, 17, NULL, '数据结构与算法基础', 3, '10:10:00', '11:35:00', 3, 2, '地信-B403(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (246, 14, NULL, '中国近现代史纲要', 3, '14:20:00', '16:40:00', 6, 3, '实验2-A402', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 11, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (247, 15, NULL, '中国近现代史纲要', 3, '14:20:00', '16:40:00', 6, 3, '实验2-A402', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 11, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (248, 16, NULL, '中国近现代史纲要', 3, '14:20:00', '16:40:00', 6, 3, '实验2-A402', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 11, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (249, 17, NULL, '中国近现代史纲要', 3, '14:20:00', '16:40:00', 6, 3, '实验2-A402', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 11, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (253, 14, NULL, '形势与政策', 3, '15:05:00', '16:40:00', 7, 2, '实验2-A402', '2025-2026-2', '[14]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (254, 15, NULL, '形势与政策', 3, '15:05:00', '16:40:00', 7, 2, '实验2-A402', '2025-2026-2', '[14]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (255, 16, NULL, '形势与政策', 3, '15:05:00', '16:40:00', 7, 2, '实验2-A402', '2025-2026-2', '[14]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (256, 17, NULL, '形势与政策', 3, '15:05:00', '16:40:00', 7, 2, '实验2-A402', '2025-2026-2', '[14]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (260, 14, NULL, '形势与政策', 3, '14:20:00', '16:40:00', 6, 3, '实验2-A402', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (261, 15, NULL, '形势与政策', 3, '14:20:00', '16:40:00', 6, 3, '实验2-A402', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (262, 16, NULL, '形势与政策', 3, '14:20:00', '16:40:00', 6, 3, '实验2-A402', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (263, 17, NULL, '形势与政策', 3, '14:20:00', '16:40:00', 6, 3, '实验2-A402', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (267, 14, NULL, '计算机综合实训', 3, '19:10:00', '20:35:00', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (268, 15, NULL, '计算机综合实训', 3, '19:10:00', '20:35:00', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (269, 16, NULL, '计算机综合实训', 3, '19:10:00', '20:35:00', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (270, 17, NULL, '计算机综合实训', 3, '19:10:00', '20:35:00', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (274, 14, NULL, '教师书写技能', 4, '08:30:00', '09:55:00', 1, 2, '武2-108', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (275, 15, NULL, '教师书写技能', 4, '08:30:00', '09:55:00', 1, 2, '武2-108', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (276, 16, NULL, '教师书写技能', 4, '08:30:00', '09:55:00', 1, 2, '武2-108', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (277, 17, NULL, '教师书写技能', 4, '08:30:00', '09:55:00', 1, 2, '武2-108', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (281, 14, NULL, '教师书写技能', 4, '10:10:00', '11:35:00', 3, 2, '智慧板书实训室', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (282, 15, NULL, '教师书写技能', 4, '10:10:00', '11:35:00', 3, 2, '智慧板书实训室', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (283, 16, NULL, '教师书写技能', 4, '10:10:00', '11:35:00', 3, 2, '智慧板书实训室', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (284, 17, NULL, '教师书写技能', 4, '10:10:00', '11:35:00', 3, 2, '智慧板书实训室', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (288, 14, NULL, '教师资格考试实务', 4, '14:20:00', '17:25:00', 6, 4, '武1-214', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (289, 15, NULL, '教师资格考试实务', 4, '14:20:00', '17:25:00', 6, 4, '武1-214', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (290, 16, NULL, '教师资格考试实务', 4, '14:20:00', '17:25:00', 6, 4, '武1-214', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (291, 17, NULL, '教师资格考试实务', 4, '14:20:00', '17:25:00', 6, 4, '武1-214', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (309, 14, NULL, '计算机综合实训', 4, '14:20:00', '15:45:00', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (310, 15, NULL, '计算机综合实训', 4, '14:20:00', '15:45:00', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (311, 16, NULL, '计算机综合实训', 4, '14:20:00', '15:45:00', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (312, 17, NULL, '计算机综合实训', 4, '14:20:00', '15:45:00', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (316, 14, NULL, '计算机综合实训', 4, '16:00:00', '17:25:00', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (317, 15, NULL, '计算机综合实训', 4, '16:00:00', '17:25:00', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (318, 16, NULL, '计算机综合实训', 4, '16:00:00', '17:25:00', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (319, 17, NULL, '计算机综合实训', 4, '16:00:00', '17:25:00', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (323, 14, NULL, '计算机综合实训', 4, '08:30:00', '09:55:00', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (324, 15, NULL, '计算机综合实训', 4, '08:30:00', '09:55:00', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (325, 16, NULL, '计算机综合实训', 4, '08:30:00', '09:55:00', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (326, 17, NULL, '计算机综合实训', 4, '08:30:00', '09:55:00', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (330, 14, NULL, '计算机综合实训', 4, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (331, 15, NULL, '计算机综合实训', 4, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (332, 16, NULL, '计算机综合实训', 4, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (333, 17, NULL, '计算机综合实训', 4, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (337, 14, NULL, '微课制作', 5, '08:30:00', '09:55:00', 1, 2, '实验3-301(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (338, 15, NULL, '微课制作', 5, '08:30:00', '09:55:00', 1, 2, '实验3-301(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (339, 16, NULL, '微课制作', 5, '08:30:00', '09:55:00', 1, 2, '实验3-301(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (340, 17, NULL, '微课制作', 5, '08:30:00', '09:55:00', 1, 2, '实验3-301(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (344, 14, NULL, '微课制作', 5, '10:10:00', '11:35:00', 3, 2, '实验3-301(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (345, 15, NULL, '微课制作', 5, '10:10:00', '11:35:00', 3, 2, '实验3-301(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (346, 16, NULL, '微课制作', 5, '10:10:00', '11:35:00', 3, 2, '实验3-301(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (347, 17, NULL, '微课制作', 5, '10:10:00', '11:35:00', 3, 2, '实验3-301(机房)', '2025-2026-2', '[1, 2, 3, 4, 6, 10, 12, 13, 16, 17, 18]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (365, 14, NULL, '计算机综合实训', 5, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (366, 15, NULL, '计算机综合实训', 5, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (367, 16, NULL, '计算机综合实训', 5, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (368, 17, NULL, '计算机综合实训', 5, '10:10:00', '11:35:00', 3, 2, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (372, 14, NULL, '计算机综合实训', 5, '11:40:00', '12:20:00', 5, 1, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (373, 15, NULL, '计算机综合实训', 5, '11:40:00', '12:20:00', 5, 1, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (374, 16, NULL, '计算机综合实训', 5, '11:40:00', '12:20:00', 5, 1, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (375, 17, NULL, '计算机综合实训', 5, '11:40:00', '12:20:00', 5, 1, '地信-B303(机房)', '2025-2026-2', '[9]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (379, 14, NULL, '计算机综合实训', 5, '14:20:00', '15:45:00', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (380, 15, NULL, '计算机综合实训', 5, '14:20:00', '15:45:00', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (381, 16, NULL, '计算机综合实训', 5, '14:20:00', '15:45:00', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (382, 17, NULL, '计算机综合实训', 5, '14:20:00', '15:45:00', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (386, 14, NULL, '计算机综合实训', 5, '16:00:00', '17:25:00', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (387, 15, NULL, '计算机综合实训', 5, '16:00:00', '17:25:00', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (388, 16, NULL, '计算机综合实训', 5, '16:00:00', '17:25:00', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (389, 17, NULL, '计算机综合实训', 5, '16:00:00', '17:25:00', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (393, 14, NULL, '计算机综合实训', 5, '08:30:00', '09:55:00', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (394, 15, NULL, '计算机综合实训', 5, '08:30:00', '09:55:00', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (395, 16, NULL, '计算机综合实训', 5, '08:30:00', '09:55:00', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (396, 17, NULL, '计算机综合实训', 5, '08:30:00', '09:55:00', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (400, 14, NULL, '中国近现代史纲要', 6, '08:30:00', '11:35:00', 1, 4, '网络虚拟教室武鸣003', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (401, 15, NULL, '中国近现代史纲要', 6, '08:30:00', '11:35:00', 1, 4, '网络虚拟教室武鸣003', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (402, 16, NULL, '中国近现代史纲要', 6, '08:30:00', '11:35:00', 1, 4, '网络虚拟教室武鸣003', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (403, 17, NULL, '中国近现代史纲要', 6, '08:30:00', '11:35:00', 1, 4, '网络虚拟教室武鸣003', '2025-2026-2', '[15]', 1, '2026-05-24 21:00:49');
INSERT INTO `schedule` VALUES (483, 10, 4, '高级网站技术', 7, '10:10:00', '11:35:00', 2, 2, '', '2025-2026-2', '[15]', 1, '2026-05-28 15:12:08');
INSERT INTO `schedule` VALUES (484, 11, 4, '高级网站技术', 7, '10:10:00', '11:35:00', 2, 2, '', '2025-2026-2', '[15]', 1, '2026-05-28 15:12:08');
INSERT INTO `schedule` VALUES (485, 12, 4, '高级网站技术', 7, '10:10:00', '11:35:00', 2, 2, '', '2025-2026-2', '[15]', 1, '2026-05-28 15:12:08');
INSERT INTO `schedule` VALUES (486, 13, 4, '高级网站技术', 7, '10:10:00', '11:35:00', 2, 2, '', '2025-2026-2', '[15]', 1, '2026-05-28 15:12:08');
INSERT INTO `schedule` VALUES (487, 10, 4, '高级网站技术', 7, '11:40:00', '12:20:00', 3, 1, '', '2025-2026-2', '[15]', 1, '2026-05-28 15:12:08');
INSERT INTO `schedule` VALUES (488, 11, 4, '高级网站技术', 7, '11:40:00', '12:20:00', 3, 1, '', '2025-2026-2', '[15]', 1, '2026-05-28 15:12:08');
INSERT INTO `schedule` VALUES (489, 12, 4, '高级网站技术', 7, '11:40:00', '12:20:00', 3, 1, '', '2025-2026-2', '[15]', 1, '2026-05-28 15:12:08');
INSERT INTO `schedule` VALUES (490, 13, 4, '高级网站技术', 7, '11:40:00', '12:20:00', 3, 1, '', '2025-2026-2', '[15]', 1, '2026-05-28 15:12:08');
INSERT INTO `schedule` VALUES (655, 14, 5, '多媒体课件设计与开发', 0, '00:00:00', '00:00:00', 0, 0, '', '2025-2026-2', '[]', 1, '2026-05-29 16:24:37');
INSERT INTO `schedule` VALUES (656, 15, 5, '多媒体课件设计与开发', 0, '00:00:00', '00:00:00', 0, 0, '', '2025-2026-2', '[]', 1, '2026-05-29 16:24:37');
INSERT INTO `schedule` VALUES (657, 16, 5, '多媒体课件设计与开发', 0, '00:00:00', '00:00:00', 0, 0, '', '2025-2026-2', '[]', 1, '2026-05-29 16:24:37');
INSERT INTO `schedule` VALUES (658, 17, 5, '多媒体课件设计与开发', 0, '00:00:00', '00:00:00', 0, 0, '', '2025-2026-2', '[]', 1, '2026-05-29 16:24:37');
INSERT INTO `schedule` VALUES (707, 10, 5, '多媒体课件设计与开发', 7, '16:00:00', '17:25:00', 5, 2, '', '2025-2026-2', '[15]', 1, '2026-05-30 01:50:23');
INSERT INTO `schedule` VALUES (708, 11, 5, '多媒体课件设计与开发', 7, '16:00:00', '17:25:00', 5, 2, '', '2025-2026-2', '[15]', 1, '2026-05-30 01:50:23');
INSERT INTO `schedule` VALUES (709, 12, 5, '多媒体课件设计与开发', 7, '16:00:00', '17:25:00', 5, 2, '', '2025-2026-2', '[15]', 1, '2026-05-30 01:50:23');
INSERT INTO `schedule` VALUES (710, 13, 5, '多媒体课件设计与开发', 7, '16:00:00', '17:25:00', 5, 2, '', '2025-2026-2', '[15]', 1, '2026-05-30 01:50:23');

-- ----------------------------
-- Table structure for student_status
-- ----------------------------
DROP TABLE IF EXISTS `student_status`;
CREATE TABLE `student_status`  (
  `user_id` bigint NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'idle' COMMENT 'focusing/idle',
  `last_active` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  CONSTRAINT `student_status_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_status
-- ----------------------------
INSERT INTO `student_status` VALUES (10, 'idle', '2026-05-25 16:29:37');
INSERT INTO `student_status` VALUES (12, 'idle', '2026-05-25 17:21:00');
INSERT INTO `student_status` VALUES (13, 'idle', '2026-05-25 17:04:24');
INSERT INTO `student_status` VALUES (14, 'idle', '2026-05-25 16:23:52');
INSERT INTO `student_status` VALUES (16, 'idle', '2026-05-25 16:24:28');

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教工号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` tinyint NULL DEFAULT NULL COMMENT '0女 1男',
  `title` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职称 教授/副教授/讲师/助教',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属院系',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像OSS地址',
  `research_direction` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '研究方向',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '教师简介',
  `status` tinyint NULL DEFAULT 1 COMMENT '0离职 1在职',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `teacher_no`(`teacher_no` ASC) USING BTREE,
  INDEX `idx_teacher_dept`(`dept_id` ASC) USING BTREE,
  INDEX `idx_teacher_name`(`real_name` ASC) USING BTREE,
  CONSTRAINT `teacher_ibfk_1` FOREIGN KEY (`dept_id`) REFERENCES `department` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, 'T2023001', '张明远', NULL, '副教授', 1, 'zhangmy@univ.edu.cn', NULL, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-24 21:00:49');
INSERT INTO `teacher` VALUES (2, 'T2023002', '李伟强', NULL, '讲师', 1, 'liwq@univ.edu.cn', NULL, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-24 21:00:49');
INSERT INTO `teacher` VALUES (3, 'T2023003', '王丽华', NULL, '教授', 1, 'wanglh@univ.edu.cn', NULL, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-24 21:00:49');
INSERT INTO `teacher` VALUES (4, 'T2023004', '陈建峰', NULL, '讲师', 1, 'chenjf@univ.edu.cn', NULL, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-24 21:00:49');
INSERT INTO `teacher` VALUES (5, 'T2023005', '吴志远', NULL, '副教授', 1, 'wuzy@univ.edu.cn', NULL, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-24 21:00:49');
INSERT INTO `teacher` VALUES (6, 'T2023006', '冯海涛', NULL, '讲师', 1, 'fengh@univ.edu.cn', NULL, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-24 21:00:49');
INSERT INTO `teacher` VALUES (7, 'T2023007', '郑国栋', NULL, '讲师', 3, 'zhenggd@univ.edu.cn', NULL, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-24 21:00:49');
INSERT INTO `teacher` VALUES (8, 'T2023008', '赵雅文', NULL, '副教授', 2, 'zhaoyw@univ.edu.cn', NULL, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-24 21:00:49');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像OSS地址',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `role` tinyint NOT NULL DEFAULT 1 COMMENT '1学生 2教师 3管理员',
  `class_id` bigint NULL DEFAULT NULL COMMENT '班级ID',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年级',
  `status` tinyint NULL DEFAULT 1 COMMENT '0禁用 1正常',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `student_no`(`student_no` ASC) USING BTREE,
  INDEX `idx_user_class`(`class_id` ASC) USING BTREE,
  INDEX `idx_user_status`(`status` ASC) USING BTREE,
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, NULL, 'admin', '$2a$10$rvZS5UeASnVqg3q2I0rBn.PPuuDLE7dIWKeS9y66zUx/2.mYTCpnK', '系统管理员', NULL, 'admin@univ.edu.cn', NULL, 3, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', NULL);
INSERT INTO `user` VALUES (2, NULL, 'zhangmy', '$2a$10$SNK3QLbEp6USeEABw2o66uW4IqotDyYJkECWPqooCPVsB.vqJRqFW', '张明远', NULL, 'zhangmy@univ.edu.cn', NULL, 2, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-30 01:32:11');
INSERT INTO `user` VALUES (3, NULL, 'liwq', '$2a$10$ZaGnuM/lDsPeSdNqI.6JhOgHrZSJX.FZzmAxw35MlD6xmu7/a85kG', '李伟强', NULL, 'liwq@univ.edu.cn', NULL, 2, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', NULL);
INSERT INTO `user` VALUES (4, NULL, 'wanglh', '$2a$10$/uHNv.zhtzMRNS4ufrBLB.WbJou8k.pZU/X0Ftam0qup9/TFBllLy', '王丽华', NULL, 'wanglh@univ.edu.cn', NULL, 2, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', NULL);
INSERT INTO `user` VALUES (5, NULL, 'chenjf', '$2a$10$X4dA.oFZpflsNTSGERYyDedHe/vQdBOclbicoNTwvEDrfzOhoeYYu', '陈建峰', NULL, 'chenjf@univ.edu.cn', NULL, 2, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', NULL);
INSERT INTO `user` VALUES (6, NULL, 'wuzy', '$2a$10$vF1NmxHMnu7rTRFUrELAZux8GyajrcxxxsUt15JDjv0yhX3mKtT42', '吴志远', NULL, 'wuzy@univ.edu.cn', NULL, 2, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', '2026-05-28 15:13:25');
INSERT INTO `user` VALUES (7, NULL, 'fengh', '$2a$10$2rFaFmSpuDBd0wbQqUVNt.UHClIkxeIYjYf9Qo.ep6.sbA7R5cCTK', '冯海涛', NULL, 'fengh@univ.edu.cn', NULL, 2, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', NULL);
INSERT INTO `user` VALUES (8, NULL, 'zhenggd', '$2a$10$nbX5amshgqvuy8P8ZnUw3ukjCLfNcvuiSHil89jreFuhHB1qKWzfK', '郑国栋', NULL, 'zhenggd@univ.edu.cn', NULL, 2, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', NULL);
INSERT INTO `user` VALUES (9, NULL, 'zhaoyw', '$2a$10$xZjneEz4wx12SX5ana6HpOz0LKUb.loJFK2iOdkSe8o/aqv/LZGku', '赵雅文', NULL, 'zhaoyw@univ.edu.cn', NULL, 2, NULL, NULL, NULL, 1, '2026-05-24 21:00:49', NULL);
INSERT INTO `user` VALUES (10, '20240101001', 'student1', '$2a$10$jHN9lXZ2mzBt4IP4zzA5X.H2YOFAyt/xTWKM0SzDlRDR4MQBuR5z6', '张明', NULL, NULL, NULL, 1, 1, '计算机科学与技术', '2023', 1, '2026-05-24 21:00:49', '2026-05-30 01:39:53');
INSERT INTO `user` VALUES (11, '20240101002', 'student2', '$2a$10$CcJgYGwyKoSO2b.Fg9/hxuNGd/Br21lNdYSwkTSZvCa8iF14LJDBW', '李婷', NULL, NULL, NULL, 1, 1, '计算机科学与技术', '2023', 1, '2026-05-24 21:00:49', '2026-05-25 16:24:43');
INSERT INTO `user` VALUES (12, '20240101003', 'student3', '$2a$10$cx5ZJAKU.ol3IPupUF7yneKd58b4cTdjyHkI0xlOnefKO4qzMsj86', '王浩', NULL, NULL, NULL, 1, 1, '计算机科学与技术', '2023', 1, '2026-05-24 21:00:49', '2026-05-25 17:09:37');
INSERT INTO `user` VALUES (13, '20240101004', 'student4', '$2a$10$x3JQKk2dS6f7jlJrQJGma.lQA.DwpZko1SyzVfFlFwn.sAv5PgThq', '陈雪', NULL, NULL, NULL, 1, 1, '计算机科学与技术', '2023', 1, '2026-05-24 21:00:49', '2026-05-25 17:02:23');
INSERT INTO `user` VALUES (14, '20250102001', 'student5', '$2a$10$3mm3KeXTx94FdhvuWrGsxuSL3bOrkIBkqIRq4T4FYKROfk9qrrdGK', '刘晓雨', NULL, NULL, NULL, 1, 2, '教育技术学', '2023', 1, '2026-05-24 21:00:49', '2026-05-26 19:59:11');
INSERT INTO `user` VALUES (15, '20250102002', 'student6', '$2a$10$IVTbatKCLTt4iwgj.H49muDdPtI96IQm1kA87TqTIVRE3KgvHvBfe', '赵海龙', NULL, NULL, NULL, 1, 2, '教育技术学', '2023', 1, '2026-05-24 21:00:49', '2026-05-27 21:49:54');
INSERT INTO `user` VALUES (16, '20250102003', 'student7', '$2a$10$zSJqdTeo925Ax8om7u1Bl.1EyEbugiyBLYu5FEKlYp1a25qJIOYK6', '孙悦然', NULL, NULL, NULL, 1, 2, '教育技术学', '2023', 1, '2026-05-24 21:00:49', '2026-05-25 16:24:04');
INSERT INTO `user` VALUES (17, '20250102004', 'student8', '$2a$10$1h3KYd25wsBMHCJofbga2erdIwDkJWYIZsMpI0iQt2A1UuWxxn4CS', '周明达', NULL, NULL, NULL, 1, 2, '教育技术学', '2023', 1, '2026-05-24 21:00:49', NULL);

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `total_focus_minutes` int NULL DEFAULT 0 COMMENT '累计专注时长',
  `total_qa_count` int NULL DEFAULT 0 COMMENT '累计问答次数',
  `total_docs` int NULL DEFAULT 0 COMMENT '累计文档数',
  `active_days` int NULL DEFAULT 0 COMMENT '活跃天数',
  `weak_knowledge_points` json NULL COMMENT '薄弱知识点 [\"数据结构\",\"算法\"]',
  `learning_habit` json NULL COMMENT '学习习惯 {\"peak_hour\":\"晚上\",\"style\":\"视觉型\"}',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `user_profile_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户画像表(AI辅助)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_profile
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
