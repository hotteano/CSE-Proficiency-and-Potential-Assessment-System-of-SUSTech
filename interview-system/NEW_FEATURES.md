# 面试系统新增功能说明

## 一、功能概览

本次更新新增了以下核心功能：

1. **HTTPS/SSL安全连接** - 数据库和API通信加密
2. **集成录音系统** - 考官控制面试录音，自动上传处理
3. **语音识别** - 自动将录音转写为文本并精修
4. **多模型AI支持** - 支持DeepSeek、GPT-4等模型，可切换配置
5. **自动AI评测** - 面试结束后自动调用AI分析并生成JSON结果
6. **17维度评测体系** - 5大类别17个维度的综合能力评测

---

## 二、使用流程

### 1. 系统配置（管理员）

#### 配置SSL/HTTPS（可选）
```properties
# config.properties
ssl.enabled=true
ssl.mode=verify-full
ssl.truststore.path=/path/to/truststore.jks
ssl.truststore.password=your_password
```

#### 配置大模型API
1. 登录管理员账号
2. 进入「API配置」标签页
3. 点击「添加配置」
4. 填写以下信息：
   - 配置名称：如"DeepSeek-Production"
   - 提供商：选择 DeepSeek-Thinking（默认）
   - 模型名称：deepseek-reasoner
   - API端点：https://api.deepseek.com/v1/chat/completions
   - API Key：您的API密钥
   - 超时时间：60秒
5. 点击保存，并设为默认配置

**支持的模型提供商：**
- DeepSeek / DeepSeek-Thinking（默认）
- OpenAI / GPT-4 Turbo
- Azure OpenAI
- Anthropic Claude
- 本地模型

---

### 2. 面试流程（考官）

#### 开始面试
1. 进入「面试控制」标签页
2. 确认考生已在系统中注册
3. 选择面试题目（从下方题目列表）
4. 点击「开始面试」按钮

#### 面试进行中
- 系统会自动开始录音
- 红色闪烁指示灯表示正在录音
- 音量条实时显示录音音量
- 计时器显示已录制时长

#### 结束面试
1. 考生回答完所有题目后，点击「结束面试」
2. 系统自动停止录音并保存音频文件
3. 自动触发后续处理流程

#### 自动处理流程
面试结束后，系统会自动执行以下操作：

```
录音文件 → 语音识别 → 文本精修 → AI分析 → 生成JSON结果
   ↓                                         ↓
保存到服务器                              保存到数据库
   ↓                                         ↓
等待评委评分                           展示分析结果
```

处理状态会实时显示在日志区域：
- 「正在进行语音识别...」
- 「正在精修文本...」
- 「正在进行AI分析...」
- 「AI分析完成」

---

### 3. 评分流程（考官）

#### 人工评分
1. 进入「面试评分」标签页
2. 选择要评分的面试记录
3. 为17个维度分别打分（0-100分滑块）
4. 填写评语、评分理由和发展建议
5. 点击「提交评分」

#### 查看AI评分
- AI分析结果会自动加载
- 可以看到AI给出的各维度分数和分析
- 人工评分和AI评分可以交叉对比

---

### 4. 查看报告

#### 综合评测报告
1. 进入「评测报告」标签页
2. 选择考生的面试记录
3. 查看以下内容：
   - 综合评分和评级（S/A/B/C/D）
   - 5大类别得分柱状图
   - 17维度详细分数
   - 优势维度分析
   - 待提升维度分析
   - 发展建议
   - 适合的岗位方向

---

## 三、数据库表结构更新

### 新增表

#### 1. llm_configs - 大模型配置表
```sql
id, name, provider, model_name, api_key, api_endpoint
is_default, enabled, timeout, params, created_at, updated_at
```

#### 2. evaluation_scores - 评测分数表
```sql
id, interview_record_id, candidate_username, evaluator_username
score_type (HUMAN/AI), comments, reasoning, suggestions
scored_at, submitted
```

#### 3. evaluation_dimension_scores - 维度分数表
```sql
id, evaluation_score_id, dimension_name, score
```

### 更新表

#### interview_records - 新增字段
```sql
transcribed_text          -- 语音识别文本
refined_text             -- 精修后文本
ai_analysis_result       -- AI分析结果(JSON)
ai_raw_response          -- AI原始返回
ai_analysis_time         -- AI分析时间
is_recording             -- 是否正在录音
recording_start_time     -- 录音开始时间
recording_duration       -- 录音时长
question_ids             -- 关联题目ID
```

---

## 四、文件目录结构

```
interview-system/
├── voice_records/          # 语音文件存储目录
│   └── interview_*.wav    # 面试录音文件
├── config.properties      # 配置文件
├── llm_configs.dat        # LLM配置缓存
└── ...
```

---

## 五、API Key安全说明

1. **加密存储**：所有API Key使用AES加密后存储在数据库中
2. **访问控制**：只有管理员可以查看和修改API配置
3. **传输安全**：支持HTTPS连接，防止中间人攻击

---

## 六、注意事项

### 录音功能
- 确保考官电脑已连接麦克风
- 录音过程中请勿关闭程序
- 录音文件自动保存到 `voice_records/` 目录

### AI分析
- 需要配置有效的API Key才能使用AI分析
- AI分析可能需要几秒钟到几十秒不等
- 网络不稳定时可能导致分析失败

### 语音识别
- 当前使用模拟实现，实际部署时需要集成真实ASR服务
- 支持科大讯飞、阿里云ASR、百度语音等主流服务

---

## 七、待完善功能

1. **真实语音识别集成** - 需接入科大讯飞/阿里云ASR API
2. **实时语音转写** - 面试过程中实时显示识别结果
3. **批量AI分析** - 支持批量处理历史面试记录
4. **报告导出PDF** - 将评测报告导出为PDF格式
5. **数据可视化优化** - 添加雷达图等更多图表类型

---

## 八、运行测试

```bash
# 编译项目
mvn clean package -s settings.xml -DskipTests

# 运行程序
java -jar target/interview-system-1.0-SNAPSHOT.jar --db-password 1234

# 登录管理员账号
# 用户名: admin
# 密码: admin123
```
