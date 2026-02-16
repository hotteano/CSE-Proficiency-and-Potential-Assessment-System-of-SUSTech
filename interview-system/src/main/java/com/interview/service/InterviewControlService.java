package com.interview.service;

import com.interview.model.*;
import com.interview.util.AudioRecorder;
import org.json.JSONObject;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 面试控制服务
 * 管理面试全流程：录音控制、语音识别、AI分析
 */
public class InterviewControlService {
    
    private final InterviewRecordService recordService;
    private final EvaluationService evaluationService;
    private final LLMManager llmManager;
    private final AudioRecorder audioRecorder;
    
    private InterviewRecord currentRecord;
    private File currentAudioFile;
    
    public InterviewControlService(AuthService authService) {
        this.recordService = new InterviewRecordService(authService);
        this.evaluationService = new EvaluationService(authService);
        this.llmManager = new LLMManager();
        this.audioRecorder = new AudioRecorder();
    }
    
    /**
     * 开始面试
     * 
     * @param candidateUsername 考生用户名
     * @param questions 面试题目列表
     * @param callback 回调
     */
    public void startInterview(String candidateUsername, List<Question> questions,
                               InterviewCallback callback) {
        try {
            // 创建面试记录
            currentRecord = new InterviewRecord();
            currentRecord.setCandidateUsername(candidateUsername);
            currentRecord.setStatus(InterviewRecord.InterviewStatus.IN_PROGRESS);
            
            // 设置关联题目
            StringBuilder questionIds = new StringBuilder();
            for (int i = 0; i < questions.size(); i++) {
                if (i > 0) questionIds.append(",");
                questionIds.append(questions.get(i).getId());
            }
            currentRecord.setQuestionIds(questionIds.toString());
            
            // 保存记录
            String result = recordService.createRecord(candidateUsername, null);
            if (!result.contains("成功")) {
                callback.onError("创建面试记录失败: " + result);
                return;
            }
            
            // 创建音频文件
            String audioFileName = String.format("interview_%s_%d.wav",
                    candidateUsername, System.currentTimeMillis());
            currentAudioFile = new File("voice_records/" + audioFileName);
            
            // 开始录音
            audioRecorder.startRecording(currentAudioFile, new AudioRecorder.RecordingCallback() {
                @Override
                public void onRecordingStarted() {
                    currentRecord.setRecording(true);
                    currentRecord.setRecordingStartTime(LocalDateTime.now());
                    callback.onRecordingStarted();
                }
                
                @Override
                public void onRecordingStopped(File audioFile) {
                    currentRecord.setRecording(false);
                    currentRecord.setVoiceFilePath(audioFile.getAbsolutePath());
                    currentRecord.setVoiceFileName(audioFile.getName());
                    currentRecord.setVoiceFileSize(audioFile.length());
                    
                    // 计算录音时长
                    if (currentRecord.getRecordingStartTime() != null) {
                        long duration = java.time.Duration.between(
                                currentRecord.getRecordingStartTime(),
                                LocalDateTime.now()
                        ).toMillis();
                        currentRecord.setRecordingDuration(duration);
                    }
                    
                    callback.onRecordingStopped(audioFile);
                    
                    // 自动触发后续处理
                    processRecordingAsync(callback);
                }
                
                @Override
                public void onRecordingError(String error) {
                    callback.onError("录音错误: " + error);
                }
                
                @Override
                public void onAmplitudeUpdate(double amplitude) {
                    callback.onAmplitudeUpdate(amplitude);
                }
            });
            
        } catch (Exception e) {
            callback.onError("开始面试失败: " + e.getMessage());
        }
    }
    
    /**
     * 结束面试
     */
    public void stopInterview() {
        if (audioRecorder.isRecording()) {
            audioRecorder.stopRecording();
        }
        if (currentRecord != null) {
            currentRecord.setStatus(InterviewRecord.InterviewStatus.COMPLETED);
        }
    }
    
    /**
     * 异步处理录音：语音识别 -> AI分析
     */
    private void processRecordingAsync(InterviewCallback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                // 步骤1: 语音识别
                callback.onStatusUpdate("正在进行语音识别...");
                String transcribedText = simulateSpeechRecognition();
                currentRecord.setTranscribedText(transcribedText);
                
                // 步骤2: 文本精修
                callback.onStatusUpdate("正在精修文本...");
                String refinedText = refineText(transcribedText);
                currentRecord.setRefinedText(refinedText);
                
                // 步骤3: AI分析
                callback.onStatusUpdate("正在进行AI分析...");
                String aiResult = performAIAnalysis(refinedText);
                currentRecord.setAiAnalysisResult(aiResult);
                currentRecord.setAiAnalysisTime(LocalDateTime.now());
                
                callback.onAnalysisComplete(aiResult);
                
            } catch (Exception e) {
                callback.onError("处理失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 模拟语音识别（实际应调用语音识别API）
     */
    private String simulateSpeechRecognition() {
        // TODO: 集成实际语音识别服务（如科大讯飞、阿里云ASR等）
        try {
            Thread.sleep(2000); // 模拟处理时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "[模拟语音识别结果] 这是从面试录音中识别出的文本内容...";
    }
    
    /**
     * 文本精修
     */
    private String refineText(String rawText) {
        // TODO: 调用文本精修服务或本地处理
        // 去除语气词、修正语法等
        return rawText.replace("嗯", "")
                     .replace("啊", "")
                     .replace("那个", "")
                     .trim();
    }
    
    /**
     * 执行AI分析
     */
    private String performAIAnalysis(String text) {
        // 获取当前题目
        // 调用LLM进行分析
        // 返回JSON格式结果
        
        // 模拟AI返回
        JSONObject result = new JSONObject();
        
        JSONObject scores = new JSONObject();
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            scores.put(dim.name(), 70 + (int)(Math.random() * 20));
        }
        result.put("dimension_scores", scores);
        result.put("analysis", "面试者回答逻辑清晰，展现了较好的专业素养...");
        result.put("strengths", new String[]{"逻辑思维", "表达能力"});
        result.put("weaknesses", new String[]{"深度挖掘", "细节把控"});
        result.put("suggestions", "建议在技术深度上进一步加强...");
        
        return result.toString();
    }
    
    /**
     * 保存AI评分
     */
    public void saveAIScore(String aiResult) {
        try {
            JSONObject result = new JSONObject(aiResult);
            
            EvaluationScore aiScore = new EvaluationScore();
            aiScore.setInterviewRecordId(currentRecord.getId());
            aiScore.setCandidateUsername(currentRecord.getCandidateUsername());
            aiScore.setScoreType(EvaluationScore.ScoreType.AI);
            
            // 解析维度分数
            JSONObject scores = result.getJSONObject("dimension_scores");
            for (EvaluationDimension dim : EvaluationDimension.values()) {
                if (scores.has(dim.name())) {
                    aiScore.setDimensionScore(dim, scores.getInt(dim.name()));
                }
            }
            
            aiScore.setComments(result.optString("analysis"));
            aiScore.setSuggestions(result.optString("suggestions"));
            aiScore.setSubmitted(true);
            
            // 保存到数据库
            // evaluationService.saveAIScore(aiScore);
            
        } catch (Exception e) {
            System.err.println("保存AI评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 面试回调接口
     */
    public interface InterviewCallback {
        void onRecordingStarted();
        void onRecordingStopped(File audioFile);
        void onAmplitudeUpdate(double amplitude);
        void onStatusUpdate(String status);
        void onAnalysisComplete(String aiResult);
        void onError(String error);
    }
    
    // Getters
    public InterviewRecord getCurrentRecord() { return currentRecord; }
    public boolean isRecording() { return audioRecorder.isRecording(); }
    public LLMManager getLlmManager() { return llmManager; }
}
