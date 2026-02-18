# CSS 样式指南

## 更新内容

本次 CSS 更新带来了：
- **现代化配色方案** - 使用流行的 Indigo/Purple 主题
- **丰富的动画效果** - 悬停、点击、加载动画
- **性能优化** - 硬件加速、缓存提示
- **更多组件样式** - Badge、Alert、Card 变体等

## 主要变化

### 1. 新的配色方案
```css
/* 主色调 - 现代靛蓝 */
-fx-primary-color: #4f46e5;      /* 主色 */
-fx-primary-light: #6366f1;      /* 浅色 */
-fx-primary-dark: #4338ca;       /* 深色 */

/* 语义色 */
-fx-success-color: #10b981;      /* 绿色 */
-fx-warning-color: #f59e0b;      /* 橙色 */
-fx-danger-color: #ef4444;       /* 红色 */
-fx-info-color: #3b82f6;         /* 蓝色 */
```

### 2. 阴影系统
```css
-fx-shadow-sm    /* 小阴影 */
-fx-shadow-md    /* 中等阴影 */
-fx-shadow-lg    /* 大阴影 */
-fx-shadow-xl    /* 超大阴影 */
```

### 3. 按钮样式
```java
// 主要按钮
button.getStyleClass().add("button");

// 次要按钮
button.getStyleClass().addAll("button", "button-secondary");

// 成功/危险按钮
button.getStyleClass().addAll("button", "button-success");
button.getStyleClass().addAll("button", "button-danger");

// 幽灵按钮（透明背景）
button.getStyleClass().addAll("button", "button-ghost");

// 图标按钮
button.getStyleClass().addAll("button", "button-icon");
```

### 4. 卡片样式
```java
// 普通卡片
VBox card = new VBox();
card.getStyleClass().add("card");

// 扁平卡片（带边框）
card.getStyleClass().add("card-flat");

// 高程卡片（大阴影）
card.getStyleClass().add("card-elevated");
```

### 5. 表单状态
```java
// 错误状态
textField.getStyleClass().add("field-error");

// 成功状态
textField.getStyleClass().add("field-success");
```

### 6. 动画类
```java
// 淡入动画
node.getStyleClass().add("fade-in");

// 上滑动画
node.getStyleClass().add("slide-up");

// 缩放动画
node.getStyleClass().add("scale-in");

// 脉冲动画（吸引注意）
node.getStyleClass().add("pulse");

// 加载闪烁效果
node.getStyleClass().add("shimmer");
```

### 7. 徽章/标签
```java
// 状态徽章
Label badge = new Label("New");
badge.getStyleClass().addAll("badge", "badge-primary");

// 成功徽章
badge.getStyleClass().addAll("badge", "badge-success");

// 警告徽章
badge.getStyleClass().addAll("badge", "badge-warning");

// 危险徽章
badge.getStyleClass().addAll("badge", "badge-danger");
```

### 8. 警告/提示框
```java
// 成功提示
VBox alert = new VBox();
alert.getStyleClass().add("alert-success");

// 警告提示
alert.getStyleClass().add("alert-warning");

// 错误提示
alert.getStyleClass().add("alert-danger");

// 信息提示
alert.getStyleClass().add("alert-info");
```

## 性能优化

CSS 中已添加性能优化：

1. **硬件加速**
   ```css
   -fx-cache: true;
   -fx-cache-hint: speed;
   ```

2. **过渡动画**
   ```css
   -fx-transition: property duration;
   ```

3. **阴影优化** - 使用预设的阴影变量，避免重复计算

## 使用示例

### 完整的登录表单
```java
VBox loginPanel = new VBox();
loginPanel.getStyleClass().add("login-panel");

Label title = new Label("欢迎回来");
title.getStyleClass().add("title-label");

TextField username = new TextField();
username.setPromptText("用户名");
username.getStyleClass().add("text-field");

PasswordField password = new PasswordField();
password.setPromptText("密码");

Button loginBtn = new Button("登录");
loginBtn.getStyleClass().add("button");
loginBtn.setMaxWidth(Double.MAX_VALUE);

loginPanel.getChildren().addAll(title, username, password, loginBtn);
```

### 数据表格
```java
TableView<User> table = new TableView<>();
table.getStyleClass().add("table-view");

// 添加行悬停效果通过CSS自动处理
```

### 带徽章的列表项
```java
HBox item = new HBox(10);
item.setAlignment(Pos.CENTER_LEFT);

Label name = new Label("张三");
name.getStyleClass().add("text-primary");

Label status = new Label("在线");
status.getStyleClass().addAll("badge", "badge-success");

item.getChildren().addAll(name, status);
```

## 动画持续时间变量

```css
-fx-duration-fast: 100ms;    /* 快速反馈 */
-fx-duration-normal: 200ms;  /* 标准过渡 */
-fx-duration-slow: 300ms;    /* 显著动画 */
```

## 提示

1. **动画性能**：复杂的动画建议在小元素上使用
2. **阴影使用**：避免在大量元素上同时使用大阴影
3. **缓存**：静态内容可以添加 `-fx-cache: true`
4. **响应式**：通过 `-fx-transition` 实现平滑过渡
