package com.interview.view;

import com.interview.model.Question;
import com.interview.model.Question.QuestionLevel;
import com.interview.model.Question.QuestionType;
import com.interview.service.QuestionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * 题目浏览视图（JavaFX）
 * 应用新 CSS 设计
 */
public class QuestionBrowseView extends BorderPane {
    
    private final QuestionService questionService;
    
    private TableView<Question> questionTable;
    private TextArea contentArea;
    private TextArea answerArea;
    
    // 搜索组件
    private TextField keywordField;
    private ComboBox<String> typeComboBox;
    private ComboBox<String> difficultyComboBox;
    private ComboBox<String> categoryComboBox;
    private Label countLabel;
    
    public QuestionBrowseView(QuestionService questionService) {
        this.questionService = questionService;
        
        setPadding(new Insets(20));
        getStyleClass().add("bg-secondary");
        
        // 顶部搜索栏
        setTop(createSearchPanel());
        
        // 中心内容分割
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("split-pane");
        splitPane.setDividerPositions(0.5);
        splitPane.getItems().addAll(createQuestionListPanel(), createDetailPanel());
        
        setCenter(splitPane);
        
        // 加载数据
        loadQuestions();
    }
    
    private VBox createSearchPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(0, 0, 15, 0));
        
        // 标题栏
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("📚 题目浏览");
        titleLabel.getStyleClass().add("heading-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // 结果计数徽章
        countLabel = new Label("共 0 条记录");
        countLabel.getStyleClass().addAll("badge", "badge-info");
        
        titleBox.getChildren().addAll(titleLabel, spacer, countLabel);
        
        // 搜索栏卡片
        HBox searchCard = new HBox(12);
        searchCard.getStyleClass().addAll("card-flat", "p-3");
        searchCard.setAlignment(Pos.CENTER_LEFT);
        
        // 关键词
        keywordField = new TextField();
        keywordField.setPromptText("🔍 关键词搜索");
        keywordField.setPrefWidth(150);
        keywordField.getStyleClass().add("text-field");
        
        // 类型
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().add("全部类型");
        for (QuestionType type : QuestionType.values()) {
            typeComboBox.getItems().add(type.getDisplayName());
        }
        typeComboBox.setValue("全部类型");
        typeComboBox.setPrefWidth(130);
        typeComboBox.getStyleClass().add("combo-box");
        
        // 难度
        difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().add("全部难度");
        difficultyComboBox.getItems().add(QuestionLevel.BASIC.getDisplayName());
        difficultyComboBox.getItems().add(QuestionLevel.INTERMEDIATE.getDisplayName());
        difficultyComboBox.getItems().add(QuestionLevel.ADVANCED.getDisplayName());
        difficultyComboBox.getItems().add(QuestionLevel.SPECIALIZATION_THREE.getDisplayName());
        difficultyComboBox.setValue("全部难度");
        difficultyComboBox.setPrefWidth(110);
        difficultyComboBox.getStyleClass().add("combo-box");
        
        // 分类
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().add("全部分类");
        categoryComboBox.setValue("全部分类");
        categoryComboBox.setPrefWidth(130);
        categoryComboBox.getStyleClass().add("combo-box");
        loadCategories();
        
        // 搜索按钮
        Button searchBtn = new Button("🔍 搜索");
        searchBtn.getStyleClass().addAll("button", "button-small");
        searchBtn.setOnAction(e -> searchQuestions());
        
        // 重置按钮
        Button resetBtn = new Button("🔄 重置");
        resetBtn.getStyleClass().addAll("button", "button-secondary", "button-small");
        resetBtn.setOnAction(e -> resetSearch());
        
        searchCard.getChildren().addAll(
            keywordField,
            typeComboBox,
            difficultyComboBox,
            categoryComboBox,
            searchBtn,
            resetBtn
        );
        
        panel.getChildren().addAll(titleBox, searchCard);
        
        return panel;
    }
    
    private VBox createQuestionListPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().addAll("card", "p-3");
        panel.setPadding(new Insets(15));
        
        Label titleLabel = new Label("📋 题目列表");
        titleLabel.getStyleClass().add("subtitle-label");
        
        // 表格
        questionTable = new TableView<>();
        questionTable.getStyleClass().add("table-view");
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Question, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<Question, String> titleCol = new TableColumn<>("标题");
        titleCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getTitle()));
        titleCol.setPrefWidth(200);
        
        TableColumn<Question, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getTypeDisplayName()));
        typeCol.setPrefWidth(90);
        
        TableColumn<Question, String> diffCol = new TableColumn<>("难度");
        diffCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getLevelDisplayName()));
        diffCol.setPrefWidth(80);
        
        TableColumn<Question, String> catCol = new TableColumn<>("分类");
        catCol.setCellValueFactory(cell -> {
            String cat = cell.getValue().getCategory();
            return new SimpleStringProperty(cat != null ? cat : "-");
        });
        catCol.setPrefWidth(100);
        
        questionTable.getColumns().addAll(idCol, titleCol, typeCol, diffCol, catCol);
        
        // 选择事件
        questionTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showQuestionDetail(newVal));
        
        panel.getChildren().addAll(titleLabel, questionTable);
        VBox.setVgrow(questionTable, Priority.ALWAYS);
        
        return panel;
    }
    
    private VBox createDetailPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().addAll("card", "p-3");
        panel.setPadding(new Insets(15));
        
        Label titleLabel = new Label("📝 题目详情");
        titleLabel.getStyleClass().add("subtitle-label");
        
        // 题目内容
        Label contentTitle = new Label("题目内容");
        contentTitle.getStyleClass().add("text-secondary");
        contentArea = new TextArea();
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(10);
        contentArea.getStyleClass().add("text-area");
        contentArea.setPromptText("请选择题目查看详情...");
        
        // 参考答案
        Label answerTitle = new Label("参考答案");
        answerTitle.getStyleClass().add("text-secondary");
        answerArea = new TextArea();
        answerArea.setEditable(false);
        answerArea.setWrapText(true);
        answerArea.setPrefRowCount(8);
        answerArea.getStyleClass().add("text-area");
        answerArea.setPromptText("参考答案将显示在这里...");
        
        panel.getChildren().addAll(titleLabel, contentTitle, contentArea, answerTitle, answerArea);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        VBox.setVgrow(answerArea, Priority.ALWAYS);
        
        return panel;
    }
    
    private void loadQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        updateTable(questions);
    }
    
    private void loadCategories() {
        List<String> categories = questionService.getAllCategories();
        categoryComboBox.getItems().addAll(categories);
    }
    
    private void searchQuestions() {
        String keyword = keywordField.getText().trim();
        QuestionType type = typeComboBox.getSelectionModel().getSelectedIndex() > 0 ? 
            QuestionType.values()[typeComboBox.getSelectionModel().getSelectedIndex() - 1] : null;
        QuestionLevel level = difficultyComboBox.getSelectionModel().getSelectedIndex() > 0 ? 
            QuestionLevel.values()[difficultyComboBox.getSelectionModel().getSelectedIndex() - 1] : null;
        String category = categoryComboBox.getSelectionModel().getSelectedIndex() > 0 ? 
            categoryComboBox.getValue() : null;
        
        List<Question> questions = questionService.searchQuestions(keyword, type, level, category);
        updateTable(questions);
    }
    
    private void resetSearch() {
        keywordField.clear();
        typeComboBox.setValue("全部类型");
        difficultyComboBox.setValue("全部难度");
        categoryComboBox.setValue("全部分类");
        loadQuestions();
    }
    
    private void updateTable(List<Question> questions) {
        questionTable.getItems().clear();
        questionTable.getItems().addAll(questions);
        countLabel.setText("共 " + questions.size() + " 条记录");
    }
    
    private void showQuestionDetail(Question question) {
        if (question != null) {
            contentArea.setText(question.getContent());
            answerArea.setText(question.getAnswer() != null ? question.getAnswer() : "暂无参考答案");
        }
    }
}
