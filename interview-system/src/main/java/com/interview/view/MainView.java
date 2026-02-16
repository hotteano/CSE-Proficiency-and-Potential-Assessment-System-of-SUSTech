package com.interview.view;

import com.interview.JavaFXApp;
import com.interview.model.Permission;
import com.interview.model.Role;
import com.interview.model.User;
import com.interview.service.AuthService;
import com.interview.service.InterviewRecordService;
import com.interview.service.QuestionService;
import com.interview.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 主界面（JavaFX）
 */
public class MainView extends BorderPane {
    
    private final AuthService authService;
    private final QuestionService questionService;
    private final UserService userService;
    private final InterviewRecordService recordService;
    
    private Label statusLabel;
    private TabPane tabPane;
    
    public MainView(AuthService authService) {
        this.authService = authService;
        this.questionService = new QuestionService(authService);
        this.userService = new UserService(authService);
        this.recordService = new InterviewRecordService(authService);
        
        setStyle("-fx-background-color: #f5f5f5;");
        
        // 创建菜单栏
        setTop(createMenuBar());
        
        // 创建中心内容
        setCenter(createContent());
        
        // 创建状态栏
        setBottom(createStatusBar());
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // 系统菜单
        Menu systemMenu = new Menu("系统");
        MenuItem logoutItem = new MenuItem("注销登录");
        logoutItem.setOnAction(e -> logout());
        
        MenuItem exitItem = new MenuItem("退出");
        exitItem.setOnAction(e -> System.exit(0));
        
        systemMenu.getItems().addAll(logoutItem, new SeparatorMenuItem(), exitItem);
        
        // 个人设置菜单
        Menu settingsMenu = new Menu("个人设置");
        MenuItem changePasswordItem = new MenuItem("修改密码");
        changePasswordItem.setOnAction(e -> showChangePasswordDialog());
        
        settingsMenu.getItems().add(changePasswordItem);
        
        // 帮助菜单
        Menu helpMenu = new Menu("帮助");
        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.setOnAction(e -> showAboutDialog());
        
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(systemMenu, settingsMenu, helpMenu);
        
        return menuBar;
    }
    
    private TabPane createContent() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: white;");
        
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return tabPane;
        }
        
        Role role = currentUser.getRole();
        
        // 根据角色添加标签页
        switch (role) {
            case ADMIN -> {
                addTab("题目浏览", new QuestionBrowseView(questionService));
                addTab("题目管理", new QuestionManageView(questionService));
                addTab("题目抽取", new QuestionExtractView(questionService));
                addTab("面试记录", new InterviewRecordManageView(recordService, true));
                addTab("用户管理", new UserManageView(userService));
            }
            case EXAMINER -> {
                addTab("题目浏览", new QuestionBrowseView(questionService));
                addTab("题目抽取", new QuestionExtractView(questionService));
                addTab("面试记录", new InterviewRecordManageView(recordService, true));
            }
            case QUESTION_CREATOR -> {
                addTab("题目浏览", new QuestionBrowseView(questionService));
                addTab("题目管理", new QuestionManageView(questionService));
            }
            case CANDIDATE -> {
                addTab("面试中心", new CandidateInterviewView(recordService));
                addTab("我的面试记录", new InterviewRecordManageView(recordService, false));
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
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #dee2e6 transparent transparent transparent;");
        
        User currentUser = authService.getCurrentUser();
        String statusText = currentUser != null ? 
            String.format("当前用户: %s (%s)", currentUser.getRealName(), currentUser.getRoleDisplayName()) :
            "未登录";
        
        statusLabel = new Label(statusText);
        statusLabel.setFont(Font.font(12));
        statusLabel.setTextFill(Color.web("#666"));
        
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }
    
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认");
        alert.setHeaderText("注销登录");
        alert.setContentText("确定要注销登录吗？");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                authService.logout();
                JavaFXApp.showLoginView();
            }
        });
    }
    
    private void showChangePasswordDialog() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(authService);
        dialog.showAndWait();
    }
    
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("面试题目抽取系统 v2.0");
        alert.setContentText(
            "基于 JavaFX 和 PostgreSQL 的面试管理系统\n\n" +
            "支持四种角色：管理员、考官、试题编制者、考生\n" +
            "支持面试语音录制功能\n\n" +
            "© 2024 Interview System"
        );
        alert.showAndWait();
    }
}
