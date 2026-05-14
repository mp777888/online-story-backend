package com.onlinestories.story_service.service;

import com.microsoft.cognitiveservices.speech.*;
import com.onlinestories.story_service.enums.Language;
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

        SpeechSynthesizer synthesizer = null;
        SpeechSynthesisResult result = null;

        try {

            log.info("Initializing Azure TTS...");
            log.info("Speech region: {}", speechRegion);
            log.info("Speech key exists: {}", speechKey != null);

            // 1. Cấu hình Speech
            SpeechConfig speechConfig =
                    SpeechConfig.fromSubscription(speechKey, speechRegion);

            String voiceName = language.equals(Language.ENGLISH.name())
                    ? "en-US-JennyNeural"
                    : "vi-VN-HoaiMyNeural";

            speechConfig.setSpeechSynthesisVoiceName(voiceName);

            // Định dạng âm thanh xuất ra là MP3
            speechConfig.setSpeechSynthesisOutputFormat(
                    SpeechSynthesisOutputFormat.Audio16Khz64KBitRateMonoMp3
            );

            // 2. Cấu hình Output là NULL
            // Chúng ta không phát ra loa server và không lưu file
            // Chỉ lấy dữ liệu audio trong memory
            synthesizer = new SpeechSynthesizer(speechConfig, null);

            log.info("Calling Azure Speech synthesis...");

            // 3. Thực hiện chuyển đổi
            result = synthesizer.SpeakText(text);

            log.info("Azure result reason: {}", result.getReason());

            // 4. Xử lý kết quả
            if (result.getReason()
                    == ResultReason.SynthesizingAudioCompleted) {

                // Lấy dữ liệu audio dạng byte[]
                byte[] audioData = result.getAudioData();

                log.info(
                        "Audio generated successfully. Size={}",
                        audioData != null ? audioData.length : 0
                );

                // Validate dữ liệu audio
                if (audioData == null || audioData.length == 0) {
                    throw new RuntimeException(
                            "Azure returned empty audio data"
                    );
                }

                return audioData;

            } else if (result.getReason() == ResultReason.Canceled) {

                SpeechSynthesisCancellationDetails cancellation =
                        SpeechSynthesisCancellationDetails.fromResult(result);

                log.error(
                        "CANCELED: Reason={}",
                        cancellation.getReason()
                );

                log.error(
                        "CANCELED: ErrorDetails={}",
                        cancellation.getErrorDetails()
                );

                throw new RuntimeException(
                        "Azure TTS Error: "
                                + cancellation.getErrorDetails()
                );
            }

            throw new RuntimeException(
                    "Unexpected Azure TTS result: "
                            + result.getReason()
            );

        } catch (Exception e) {

            log.error("Azure speech synthesis failed", e);

            throw new RuntimeException(
                    "Failed to synthesize speech",
                    e
            );

        } finally {

            // Giải phóng resource tránh memory leak
            if (result != null) {
                result.close();
            }

            if (synthesizer != null) {
                synthesizer.close();
            }
        }
    }
}
