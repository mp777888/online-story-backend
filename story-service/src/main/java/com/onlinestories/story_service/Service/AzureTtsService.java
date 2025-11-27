package com.onlinestories.story_service.Service;

import com.microsoft.cognitiveservices.speech.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AzureTtsService {
    @Value("${azure.speech.key}")
    private String speechKey;

    @Value("${azure.speech.region}")
    private String speechRegion;


    public byte[] synthesizeText(String text, String language) {
        try {
            // 1. Cấu hình Speech
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            String voiceName = language.equals("en") ? "en-US-JennyNeural" : "vi-VN-HoaiMyNeural";
            speechConfig.setSpeechSynthesisVoiceName(voiceName);

            // 2. Cấu hình Output là NULL (để nó không phát ra loa server, cũng không lưu file)
            // Chúng ta chỉ muốn lấy dữ liệu trong bộ nhớ (in-memory)
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null);

            // 3. Thực hiện chuyển đổi
            SpeechSynthesisResult result = synthesizer.SpeakText(text);

            // 4. Xử lý kết quả
            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                // Trả về mảng byte âm thanh (định dạng WAV mặc định)
                return result.getAudioData();
            } else if (result.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(result);
                System.out.println("CANCELED: Reason=" + cancellation.getReason());
                System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                throw new RuntimeException("Azure TTS Error: " + cancellation.getErrorDetails());
            }

            result.close();
            synthesizer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
