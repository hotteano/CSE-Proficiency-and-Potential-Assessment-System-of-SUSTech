package com.interview.view;

import com.interview.JavaFXApp;
import com.interview.model.Permission;
import com.interview.model.Role;
import com.interview.model.User;
import com.interview.service.AuthService;
import com.interview.service.EvaluationService;
import com.interview.service.InterviewControlService;
import com.interview.service.InterviewRecordService;
import com.interview.service.QuestionService;
import com.interview.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * 主界面（JavaFX）
 * 应用新 CSS 设计
 */
public class MainView extends BorderPane {
    
    private final AuthService authService;
    private final QuestionService questionService;
    private final UserService userService;
    private final InterviewRecordService recordService;
    private final EvaluationService evaluationService;
    
    private Label statusLabel;
    private TabPane tabPane;
    
    public MainView(AuthService authService) {
        this.authService = authService;
        this.questionService = new QuestionService(authService);
        this.userService = new UserService(authService);
        this.recordService = new InterviewRecordService(authService);
        this.evaluationService = new EvaluationService(authService);
        
        // 使用 CSS 类
        getStyleClass().add("bg-secondary");
        
        // 创建菜单栏
        setTop(createMenuBar());
        
        // 创建中心内容
        setCenter(createContent());
        
        // 创建状态栏
        setBottom(createStatusBar());
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");
        
        // 系统菜单
        Menu systemMenu = new Menu("系统");
        MenuItem logoutItem = new MenuItem("🚪 注销登录");
        logoutItem.setOnAction(e -> logout());
        
        MenuItem exitItem = new MenuItem("❌ 退出");
        exitItem.setOnAction(e -> System.exit(0));
        
        systemMenu.getItems().addAll(logoutItem, new SeparatorMenuItem(), exitItem);
        
        // 个人设置菜单
        Menu settingsMenu = new Menu("设置");
        MenuItem changePasswordItem = new MenuItem("🔐 修改密码");
        changePasswordItem.setOnAction(e -> showChangePasswordDialog());
        
        settingsMenu.getItems().add(changePasswordItem);
        
        // 帮助菜单
        Menu helpMenu = new Menu("帮助");
        MenuItem aboutItem = new MenuItem("ℹ️ 关于");
        aboutItem.setOnAction(e -> showAboutDialog());
        
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(systemMenu, settingsMenu, helpMenu);
        
        return menuBar;
    }
    
    private TabPane createContent() {
        tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return tabPane;
        }
        
        Role role = currentUser.getRole();
        
        // 根据角色添加标签页
        InterviewControlService controlService = new InterviewControlService(authService);
        
        switch (role) {
            case ADMIN -> {
                addTab("📚 题目浏览", new QuestionBrowseView(questionService));
                addTab("✏️ 题目管理", new QuestionManageView(questionService));
                addTab("🎲 题目抽取", new QuestionExtractView(questionService));
                addTab("🎤 面试控制", new InterviewControlView(controlService, questionService));
                addTab("📝 面试记录", new InterviewRecordManageView(recordService, true));
                addTab("📊 面试评分", new EvaluationView(authService));
                addTab("📈 评测报告", new ReportView(authService));
                addTab("⚙️ API配置", new LLMConfigView(authService));
                addTab("👥 用户管理", new UserManageView(userService));
            }
            case EXAMINER -> {
                addTab("📚 题目浏览", new QuestionBrowseView(questionService));
                addTab("🎲 题目抽取", new QuestionExtractView(questionService));
                addTab("🎤 面试控制", new InterviewControlView(controlService, questionService));
                addTab("📝 面试记录", new InterviewRecordManageView(recordService, true));
                addTab("📊 面试评分", new EvaluationView(authService));
            }
            case QUESTION_CREATOR -> {
                addTab("📚 题目浏览", new QuestionBrowseView(questionService));
                addTab("✏️ 题目管理", new QuestionManageView(questionService));
            }
            case CANDIDATE -> {
                addTab("📝 我的面试", new InterviewRecordManageView(recordService, false));
                addTab("📈 我的报告", new ReportView(authService));
            }
        }
        
        return tabPane;
    }
    
    private void addTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tabPane.getTabs().add(tab);
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        User currentUser = authService.getCurrentUser();
        
        // 用户角色徽章
        Label roleBadge = new Label();
        roleBadge.getStyleClass().add("badge");
        
        String statusText;
        if (currentUser != null) {
            statusText = String.format("👤 %s", currentUser.getRealName());
            Role role = currentUser.getRole();
            roleBadge.setText(role.getDisplayName());
            
            // 根据角色设置徽章颜色
            switch (role) {
                case ADMIN -> roleBadge.getStyleClass().add("badge-danger");
                case EXAMINER -> roleBadge.getStyleClass().add("badge-primary");
                case QUESTION_CREATOR -> roleBadge.getStyleClass().add("badge-success");
                case CANDIDATE -> roleBadge.getStyleClass().add("badge-info");
            }
        } else {
            statusText = "未登录";
            roleBadge.setText("未知");
            roleBadge.getStyleClass().add("badge-warning");
        }
        
        statusLabel = new Label(statusText);
        statusLabel.getStyleClass().add("text-secondary");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // SSL 状态指示
        Label sslLabel = new Label("🔒 SSL");
        sslLabel.getStyleClass().addAll("badge", "badge-success");
        sslLabel.setTooltip(new Tooltip("数据库连接已加密"));
        
        statusBar.getChildren().addAll(statusLabel, roleBadge, spacer, sslLabel);
        
        return statusBar;
    }
    
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认注销");
        alert.setHeaderText("注销登录");
        alert.setContentText("确定要注销当前账号吗？");
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                authService.logout();
                JavaFXApp.showLoginView();
            }
        });
    }
    
    private void showChangePasswordDialog() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(authService);
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.showAndWait();
    }
    
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于系统");
        alert.setHeaderText("计算机科学与工程能力与潜力测评系统 v2.0");
        alert.setContentText(
            "基于 JavaFX 和 PostgreSQL 的面试管理系统\n\n" +
            "✨ 功能特性：\n" +
            "  • 支持四种角色：管理员、考官、出题人、考生\n" +
            "  • 面试语音录制与智能评测\n" +
            "  • 多维度面试评分体系\n" +
            "  • SSL/HTTPS 安全数据库连接\n\n" +
            "© 2024 Interview System"
        );
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }
}
