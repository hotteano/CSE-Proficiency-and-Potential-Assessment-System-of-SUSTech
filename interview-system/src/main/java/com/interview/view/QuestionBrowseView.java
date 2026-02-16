package com.interview.view;

import com.interview.model.Question;
import com.interview.model.Question.Difficulty;
import com.interview.model.Question.QuestionType;
import com.interview.service.QuestionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * 题目浏览视图（JavaFX）
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
        
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white;");
        
        // 顶部搜索栏
        setTop(createSearchPanel());
        
        // 中心内容分割
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);
        splitPane.getItems().addAll(createQuestionListPanel(), createDetailPanel());
        
        setCenter(splitPane);
        
        // 加载数据
        loadQuestions();
    }
    
    private VBox createSearchPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(0, 0, 10, 0));
        
        Label titleLabel = new Label("题目浏览");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        // 关键词
        keywordField = new TextField();
        keywordField.setPromptText("关键词搜索");
        keywordField.setPrefWidth(150);
        
        // 类型
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().add("全部");
        for (QuestionType type : QuestionType.values()) {
            typeComboBox.getItems().add(type.getDisplayName());
        }
        typeComboBox.setValue("全部");
        typeComboBox.setPrefWidth(120);
        
        // 难度
        difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().add("全部");
        for (Difficulty diff : Difficulty.values()) {
            difficultyComboBox.getItems().add(diff.getDisplayName());
        }
        difficultyComboBox.setValue("全部");
        difficultyComboBox.setPrefWidth(100);
        
        // 分类
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().add("全部");
        categoryComboBox.setValue("全部");
        categoryComboBox.setPrefWidth(120);
        loadCategories();
        
        Button searchBtn = new Button("搜索");
        searchBtn.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white;");
        searchBtn.setOnAction(e -> searchQuestions());
        
        Button resetBtn = new Button("重置");
        resetBtn.setOnAction(e -> resetSearch());
        
        searchBox.getChildren().addAll(
            new Label("关键词:"), keywordField,
            new Label("类型:"), typeComboBox,
            new Label("难度:"), difficultyComboBox,
            new Label("分类:"), categoryComboBox,
            searchBtn, resetBtn
        );
        
        panel.getChildren().addAll(titleLabel, searchBox);
        
        return panel;
    }
    
    private VBox createQuestionListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(5));
        
        Label titleLabel = new Label("题目列表");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        
        // 表格
        questionTable = new TableView<>();
        
        TableColumn<Question, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(50);
        
        TableColumn<Question, String> titleCol = new TableColumn<>("标题");
        titleCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getTitle()));
        titleCol.setPrefWidth(200);
        
        TableColumn<Question, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getTypeDisplayName()));
        typeCol.setPrefWidth(100);
        
        TableColumn<Question, String> diffCol = new TableColumn<>("难度");
        diffCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getDifficultyDisplayName()));
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
        
        // 计数标签
        countLabel = new Label("共 0 条记录");
        countLabel.setFont(Font.font(12));
        
        panel.getChildren().addAll(titleLabel, questionTable, countLabel);
        VBox.setVgrow(questionTable, Priority.ALWAYS);
        
        return panel;
    }
    
    private VBox createDetailPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(5));
        
        Label titleLabel = new Label("题目详情");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        
        // 题目内容
        Label contentTitle = new Label("题目内容:");
        contentTitle.setFont(Font.font(null, FontWeight.BOLD, 12));
        contentArea = new TextArea();
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(8);
        
        // 参考答案
        Label answerTitle = new Label("参考答案:");
        answerTitle.setFont(Font.font(null, FontWeight.BOLD, 12));
        answerArea = new TextArea();
        answerArea.setEditable(false);
        answerArea.setWrapText(true);
        answerArea.setPrefRowCount(6);
        
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
        Difficulty difficulty = difficultyComboBox.getSelectionModel().getSelectedIndex() > 0 ? 
            Difficulty.values()[difficultyComboBox.getSelectionModel().getSelectedIndex() - 1] : null;
        String category = categoryComboBox.getSelectionModel().getSelectedIndex() > 0 ? 
            categoryComboBox.getValue() : null;
        
        List<Question> questions = questionService.searchQuestions(keyword, type, difficulty, category);
        updateTable(questions);
    }
    
    private void resetSearch() {
        keywordField.clear();
        typeComboBox.setValue("全部");
        difficultyComboBox.setValue("全部");
        categoryComboBox.setValue("全部");
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
