package com.interview.model;

import java.util.Arrays;
import java.util.List;

/**
 * 评测维度枚举
 * 计算机科学能力和潜力测评体系的五个大维度
 */
public enum EvaluationDimension {
    
    // ========== 技能（Skill）==========
    PROGRAMMING_SKILL("编程能力", "Programming Skills", 
        "评估面试者的编程基础、代码实现能力和算法应用能力", 
        Category.SKILL, 25),
    
    FRAMEWORK_ENV("经典框架和环境配置", "Familiarity with Classic Frameworks and Environment Setup",
        "评估对主流开发框架、工具和环境的熟悉程度",
        Category.SKILL, 15),
    
    CODE_QUALITY("工程优化能力与代码品味", "Engineering Optimization and Code Quality",
        "评估代码规范性、可读性、可维护性和优化能力",
        Category.SKILL, 15),
    
    DEBUGGING("代码纠错能力", "Code Debugging Skills",
        "评估发现和修复代码问题的能力",
        Category.SKILL, 10),
    
    SYSTEM_DESIGN("系统设计能力", "System Design Skills",
        "评估架构设计、模块划分和系统扩展性设计能力",
        Category.SKILL, 20),
    
    // ========== 学术与研究潜力 ==========
    CREATIVE_EXPRESSION("创意表述能力", "Creative Expression Ability",
        "评估清晰表达创新想法和概念的能力",
        Category.RESEARCH, 15),
    
    RESEARCH_INTUITION("研究直觉与品味", "Research Intuition and Taste",
        "评估对研究方向和创新点的敏感度和判断力",
        Category.RESEARCH, 20),
    
    IDEA_VALIDATION("创新点的验证能力", "Ability to Validate Innovative Ideas",
        "评估验证创新想法可行性和有效性的能力",
        Category.RESEARCH, 20),
    
    THEORETICAL_BUILDING("理论系统构建能力", "Ability to Build Theoretical Systems",
        "评估构建完整理论体系和框架的能力",
        Category.RESEARCH, 20),
    
    // ========== 沟通能力 ==========
    RIGOR("严谨性", "Clarity and Precision of Expression",
        "评估表达的准确性、清晰度和专业术语使用",
        Category.COMMUNICATION, 20),
    
    LOGIC("逻辑性", "Logical Coherence",
        "评估论述的逻辑结构和推理过程的连贯性",
        Category.COMMUNICATION, 20),
    
    PERSUASIVENESS("说服力", "Persuasiveness",
        "评估观点阐述的说服力和影响力",
        Category.COMMUNICATION, 15),
    
    // ========== 数学能力 ==========
    BASIC_MATH("基本算术能力", "Basic Arithmetic Skills",
        "评估基础数学运算和计算能力",
        Category.MATHEMATICS, 10),
    
    MATH_MODELING("数学建模能力", "Mathematical Modeling Skills",
        "评估将实际问题抽象为数学模型的能力",
        Category.MATHEMATICS, 25),
    
    PROOF_ABILITY("数学证明能力", "Mathematical Proof Skills",
        "评估数学推理和严格证明的能力",
        Category.MATHEMATICS, 20),
    
    // ========== 设计与商业远见 ==========
    PRODUCT_DESIGN("产品设计能力", "Product Design Skills",
        "评估产品功能设计、用户体验和交互设计能力",
        Category.BUSINESS, 20),
    
    BUSINESS_INSIGHT("商业与市场洞察力", "Business and Market Insight",
        "评估对市场趋势、商业模式和竞争格局的理解",
        Category.BUSINESS, 20),
    
    OPEN_SOURCE("开源产品设计与维护能力", "Open Source Product Design and Maintenance Skills",
        "评估开源项目的规划、开发和社区运营能力",
        Category.BUSINESS, 15);
    
    private final String displayName;
    private final String englishName;
    private final String description;
    private final Category category;
    private final int defaultWeight; // 默认权重（百分比）
    
    EvaluationDimension(String displayName, String englishName, String description, 
                        Category category, int defaultWeight) {
        this.displayName = displayName;
        this.englishName = englishName;
        this.description = description;
        this.category = category;
        this.defaultWeight = defaultWeight;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public int getDefaultWeight() {
        return defaultWeight;
    }
    
    /**
     * 获取大维度分类
     */
    public enum Category {
        SKILL("技能", "Skill", "编程和技术实现能力"),
        RESEARCH("学术与研究潜力", "Academic and Research Potential", "创新思维和研究能力"),
        COMMUNICATION("沟通能力", "Communication Skills", "表达和交流能力"),
        MATHEMATICS("数学能力", "Mathematical Ability", "数学推理和建模能力"),
        BUSINESS("设计与商业远见", "Design and Business Acumen", "产品思维和商业洞察力");
        
        private final String displayName;
        private final String englishName;
        private final String description;
        
        Category(String displayName, String englishName, String description) {
            this.displayName = displayName;
            this.englishName = englishName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getEnglishName() {
            return englishName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 根据分类获取所有维度
     */
    public static List<EvaluationDimension> getByCategory(Category category) {
        return Arrays.stream(values())
                .filter(d -> d.category == category)
                .toList();
    }
    
    /**
     * 获取分类下所有维度的总权重
     */
    public static int getTotalWeightByCategory(Category category) {
        return Arrays.stream(values())
                .filter(d -> d.category == category)
                .mapToInt(EvaluationDimension::getDefaultWeight)
                .sum();
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
