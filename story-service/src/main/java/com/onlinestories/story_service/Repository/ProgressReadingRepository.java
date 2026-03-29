package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.ProgressReading;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressReadingRepository extends MongoRepository<ProgressReading, String> {
    Optional<ProgressReading> findByUserIdAndChapterId(String userId, String chapterId);

    @Aggregation(pipeline = {
            "{ '$match': { 'chapterId': ?0 } }",
            "{ '$group': { '_id': null, 'avgPercentage': { '$avg': '$percentageRead' } } }"
    })
    Float calculateAveragePercentageReadByChapterId(String chapterId);
}
