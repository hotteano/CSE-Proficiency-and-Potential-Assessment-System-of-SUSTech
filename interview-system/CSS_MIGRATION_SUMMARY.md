# CSS 样式迁移总结

## 概述

已将所有 JavaFX 视图文件从旧的内联样式迁移到新的 CSS 类系统。

## 已更新的文件列表

### 主要视图
| 文件 | 主要改动 |
|-----|---------|
| `LoginView.java` | 登录面板卡片化，标题、按钮样式类 |
| `MainView.java` | 菜单栏、标签页、状态栏徽章样式 |
| `RegisterDialog.java` | 表单卡片、角色说明卡片、按钮图标 |
| `QuestionBrowseView.java` | 搜索卡片、表格样式、详情面板 |
| `QuestionManageView.java` | 按钮栏卡片、表格样式 |
| `UserManageView.java` | 卡片布局、徽章状态显示 |
| `InterviewRecordManageView.java` | 卡片布局、按钮图标 |
| `InterviewControlView.java` | 录音控制卡片、状态徽章、计时器样式 |
| `EvaluationView.java` | 评分面板卡片、进度条样式 |
| `ReportView.java` | 报告面板卡片、图表样式 |
| `QuestionExtractView.java` | 抽取设置卡片、结果展示 |
| `LLMConfigView.java` | 配置表格、表单验证样式 |
| `UserEditDialog.java` | 表单样式、验证错误样式 |
| `ChangePasswordDialog.java` | 密码表单、提示卡片 |
| `QuestionEditDialog.java` | 题目表单、等级说明卡片 |

## 主要 CSS 类使用说明

### 布局类
```java
// 背景
getStyleClass().add("bg-secondary");

// 卡片
panel.getStyleClass().addAll("card", "p-3");
panel.getStyleClass().addAll("card-flat", "p-3");

// 对话框
dialog.getDialogPane().getStyleClass().add("dialog-pane");
```

### 按钮类
```java
// 主要按钮
button.getStyleClass().addAll("button", "button-success");
button.getStyleClass().addAll("button", "button-danger");
button.getStyleClass().addAll("button", "button-secondary");

// 按钮大小
button.getStyleClass().add("button-small");
button.getStyleClass().add("button-large");
```

### 表单类
```java
// 输入字段
textField.getStyleClass().add("text-field");
passwordField.getStyleClass().add("password-field");
textArea.getStyleClass().add("text-area");
comboBox.getStyleClass().add("combo-box");

// 错误状态
field.getStyleClass().add("field-error");
label.getStyleClass().add("label-danger");
```

### 表格类
```java
tableView.getStyleClass().add("table-view");
```

### 徽章类
```java
label.getStyleClass().addAll("badge", "badge-success");
label.getStyleClass().addAll("badge", "badge-warning");
label.getStyleClass().addAll("badge", "badge-danger");
label.getStyleClass().addAll("badge", "badge-info");
label.getStyleClass().addAll("badge", "badge-primary");
```

### 标题类
```java
label.getStyleClass().add("title-label");      // 大标题
label.getStyleClass().add("heading-label");    // 页面标题
label.getStyleClass().add("subtitle-label");   // 副标题
```

## 图标使用

为按钮和标签添加了 Emoji 图标，提升视觉体验：

| 功能 | 图标 |
|-----|------|
| 登录 | 🔐 |
| 注册 | 📝 |
| 保存 | 💾 |
| 删除 | 🗑️ |
| 刷新 | 🔄 |
| 添加 | ➕ |
| 编辑 | ✏️ |
| 搜索 | 🔍 |
| 播放 | ▶️ |
| 抽取 | 🎲 |
| 导出 | 📥 |
| AI分析 | 🤖 |
| 面试 | 🎤 |
| 报告 | 📊 |
| 用户 | 👥 |
| 题目 | 📚 |
| 设置 | ⚙️ |
| 警告 | ⚠️ |
| 成功 | ✅ |
| 错误 | ❌ |

## 性能优化

新 CSS 样式表包含以下优化：
- 硬件加速：`-fx-cache: true`
- 过渡动画：`-fx-transition`
- 优化的阴影系统

## 如何添加新视图

1. 移除所有 `setStyle()` 内联样式
2. 使用 `getStyleClass().add()` 添加 CSS 类
3. 为按钮添加图标前缀
4. 确保对话框添加 `dialog-pane` 类

示例：
```java
public class MyNewView extends BorderPane {
    public MyNewView() {
        // 背景
        getStyleClass().add("bg-secondary");
        setPadding(new Insets(20));
        
        // 标题
        Label title = new Label("📋 页面标题");
        title.getStyleClass().add("heading-label");
        
        // 卡片面板
        VBox card = new VBox(15);
        card.getStyleClass().addAll("card", "p-3");
        
        // 按钮
        Button btn = new Button("✅ 确认");
        btn.getStyleClass().addAll("button", "button-success");
        
        // 表格
        TableView<MyData> table = new TableView<>();
        table.getStyleClass().add("table-view");
    }
}
```

## 注意事项

1. 所有视图现在依赖 `styles.css`，确保该文件在资源目录中
2. 如需使用暗色主题，可切换 `styles-dark.css`
3. 表单验证时动态添加/移除 `field-error` 类
4. 徽章颜色根据状态动态切换
