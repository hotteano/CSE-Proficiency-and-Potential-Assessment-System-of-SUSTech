package com.interview.service;

import java.io.File;

/**
 * 语音识别服务接口
 * 支持多种语音识别引擎
 */
public interface SpeechRecognitionService {
    
    /**
     * 识别语音文件，返回文本
     * 
     * @param audioFile 语音文件
     * @return 识别的文本内容
     */
    RecognitionResult recognize(File audioFile);
    
    /**
     * 识别并精修文本
     * 去除语气词、修正语法等
     * 
     * @param audioFile 语音文件
     * @return 精修后的文本
     */
    RecognitionResult recognizeAndRefine(File audioFile);
    
    /**
     * 检查服务是否可用
     */
    boolean isAvailable();
    
    /**
     * 获取服务名称
     */
    String getServiceName();
    
    /**
     * 识别结果
     */
    class RecognitionResult {
        private String rawText;      // 原始识别文本
        private String refinedText;  // 精修后文本
        private double confidence;   // 置信度
        private long duration;       // 语音时长（毫秒）
        private String language;     // 识别语言
        
        // Getters and Setters
        public String getRawText() { return rawText; }
        public void setRawText(String rawText) { this.rawText = rawText; }
        
        public String getRefinedText() { return refinedText; }
        public void setRefinedText(String refinedText) { this.refinedText = refinedText; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }
}
