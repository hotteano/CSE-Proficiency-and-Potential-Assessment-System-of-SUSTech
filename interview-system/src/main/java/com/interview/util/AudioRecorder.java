package com.interview.util;

import javafx.application.Platform;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 音频录制工具类
 * 支持开始/停止录音，保存为WAV文件
 */
public class AudioRecorder {
    
    // 音频格式：16kHz, 16bit, 单声道, 有符号, 小端
    private static final AudioFormat FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            16000,  // 采样率
            16,     // 采样位数
            1,      // 声道数
            2,      // 帧大小
            16000,  // 帧率
            false   // 小端
    );
    
    private TargetDataLine microphone;
    private AtomicBoolean isRecording;
    private File outputFile;
    private Thread recordingThread;
    private RecordingCallback callback;
    
    /**
     * 录音回调接口
     */
    public interface RecordingCallback {
        void onRecordingStarted();
        void onRecordingStopped(File audioFile);
        void onRecordingError(String error);
        void onAmplitudeUpdate(double amplitude);
    }
    
    public AudioRecorder() {
        this.isRecording = new AtomicBoolean(false);
    }
    
    /**
     * 开始录音
     * 
     * @param outputFile 输出文件
     * @param callback 回调
     */
    public void startRecording(File outputFile, RecordingCallback callback) {
        this.outputFile = outputFile;
        this.callback = callback;
        
        if (isRecording.get()) {
            if (callback != null) {
                callback.onRecordingError("录音已经在进行中");
            }
            return;
        }
        
        recordingThread = new Thread(() -> {
            try {
                // 获取麦克风
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
                if (!AudioSystem.isLineSupported(info)) {
                    Platform.runLater(() -> {
                        if (callback != null) {
                            callback.onRecordingError("系统不支持该音频格式");
                        }
                    });
                    return;
                }
                
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(FORMAT);
                microphone.start();
                
                isRecording.set(true);
                
                Platform.runLater(() -> {
                    if (callback != null) {
                        callback.onRecordingStarted();
                    }
                });
                
                // 开始写入文件
                AudioInputStream audioStream = new AudioInputStream(microphone);
                
                // 使用缓冲写入
                try (FileOutputStream fos = new FileOutputStream(outputFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    
                    // 写入WAV文件头
                    writeWavHeader(bos, 0); // 先写占位符
                    
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long totalBytes = 0;
                    
                    while (isRecording.get() && (bytesRead = audioStream.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;
                        
                        // 计算音量幅值用于UI显示
                        double amplitude = calculateAmplitude(buffer, bytesRead);
                        Platform.runLater(() -> {
                            if (callback != null) {
                                callback.onAmplitudeUpdate(amplitude);
                            }
                        });
                    }
                    
                    // 更新WAV文件头
                    bos.flush();
                    updateWavHeader(outputFile, totalBytes);
                }
                
                microphone.stop();
                microphone.close();
                
                Platform.runLater(() -> {
                    if (callback != null) {
                        callback.onRecordingStopped(outputFile);
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (callback != null) {
                        callback.onRecordingError("录音错误: " + e.getMessage());
                    }
                });
            } finally {
                isRecording.set(false);
            }
        });
        
        recordingThread.setDaemon(true);
        recordingThread.start();
    }
    
    /**
     * 停止录音
     */
    public void stopRecording() {
        isRecording.set(false);
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
        if (recordingThread != null) {
            try {
                recordingThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 检查是否在录音
     */
    public boolean isRecording() {
        return isRecording.get();
    }
    
    /**
     * 计算音频幅值（用于音量显示）
     */
    private double calculateAmplitude(byte[] buffer, int length) {
        long sum = 0;
        int count = 0;
        
        // 16位采样，每2字节一个样本
        for (int i = 0; i < length - 1; i += 2) {
            short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
            sum += Math.abs(sample);
            count++;
        }
        
        if (count == 0) return 0;
        
        double average = (double) sum / count;
        // 归一化到0-1
        return Math.min(average / 32768.0, 1.0);
    }
    
    /**
     * 写入WAV文件头（占位符）
     */
    private void writeWavHeader(OutputStream out, long dataLength) throws IOException {
        ByteArrayOutputStream header = new ByteArrayOutputStream(44);
        DataOutputStream writer = new DataOutputStream(header);
        
        // RIFF chunk
        writer.writeBytes("RIFF");
        writer.writeInt(Integer.reverseBytes((int) (36 + dataLength)));
        writer.writeBytes("WAVE");
        
        // fmt chunk
        writer.writeBytes("fmt ");
        writer.writeInt(Integer.reverseBytes(16)); // Subchunk1Size
        writer.writeShort(Short.reverseBytes((short) 1)); // AudioFormat (PCM)
        writer.writeShort(Short.reverseBytes((short) 1)); // NumChannels
        writer.writeInt(Integer.reverseBytes(16000)); // SampleRate
        writer.writeInt(Integer.reverseBytes(16000 * 2)); // ByteRate
        writer.writeShort(Short.reverseBytes((short) 2)); // BlockAlign
        writer.writeShort(Short.reverseBytes((short) 16)); // BitsPerSample
        
        // data chunk
        writer.writeBytes("data");
        writer.writeInt(Integer.reverseBytes((int) dataLength));
        
        out.write(header.toByteArray());
    }
    
    /**
     * 更新WAV文件头（写入正确的数据长度）
     */
    private void updateWavHeader(File file, long dataLength) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            // 文件大小（RIFF chunk size）
            raf.seek(4);
            raf.writeInt(Integer.reverseBytes((int) (36 + dataLength)));
            
            // 数据大小（data chunk size）
            raf.seek(40);
            raf.writeInt(Integer.reverseBytes((int) dataLength));
        }
    }
    
    /**
     * 测试录音设备是否可用
     */
    public static boolean testMicrophone() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                return false;
            }
            
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(FORMAT);
            line.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
