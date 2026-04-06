package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends MongoRepository<Tag, String> {
    Optional<Tag> findByName(String name);

    // Dùng để làm chức năng gõ autocomplete: tìm tag chứa keyword & xếp theo usageCount giảm dần
    List<Tag> findByNameContainingIgnoreCaseOrderByUsageCountDesc(String name);

    // Lấy top Trending tags
    List<Tag> findTop10ByOrderByUsageCountDesc();
}
