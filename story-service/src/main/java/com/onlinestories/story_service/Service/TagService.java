package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.Entity.Tag;
import com.onlinestories.story_service.Repository.TagRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagService {

    TagRepository tagRepository;

    /**
     * Hàm này được gọi khi Story được tạo hoặc update
     */
    public Set<String> processTagsForStory(Set<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) return new HashSet<>();

        Set<String> processedTags = new HashSet<>();

        for (String rawTag : rawTags) {
            String normalizedName = normalizeTagName(rawTag);
            if (normalizedName.isEmpty()) continue;

            processedTags.add(normalizedName);

            // Upsert tag trong DB
            Tag tag = tagRepository.findByName(normalizedName).orElseGet(() ->
                    Tag.builder()
                            .name(normalizedName)
                            .displayName("#" + rawTag.trim().replace(" ", ""))
                            .usageCount(0)
                            .build()
            );

            tag.setUsageCount(tag.getUsageCount() + 1);
            tagRepository.save(tag);
        }
        return processedTags;
    }

    /**
     * Gợi ý tag cho người dùng khi đang gõ
     */
    public List<String> suggestTags(String keyword) {
        String normalized = normalizeTagName(keyword);
        return tagRepository.findByNameContainingIgnoreCaseOrderByUsageCountDesc(normalized).stream()
                .map(Tag::getDisplayName)
                .collect(Collectors.toList());
    }

    public List<String> getTrendingTags() {
        return tagRepository.findTop10ByOrderByUsageCountDesc().stream()
                .map(Tag::getDisplayName)
                .collect(Collectors.toList());
    }

    /**
     * Chuẩn hóa
     */
    private String normalizeTagName(String input) {
        if (input == null) return "";
        try {
            String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("")
                    .replaceAll("[^a-zA-Z0-9]", "") // Xóa ký tự đặc biệt và dấu cách
                    .toLowerCase();
        } catch (Exception e) {
            return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        }
    }
}
