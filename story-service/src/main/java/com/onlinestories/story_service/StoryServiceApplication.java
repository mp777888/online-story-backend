package com.onlinestories.story_service;

import com.onlinestories.story_service.Entity.Chapter;
import com.onlinestories.story_service.Entity.ChapterVersion;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication(scanBasePackages = {
		"com.onlinestories.story_service",
		"com.onlinestories.common"
})
@EnableFeignClients(basePackages = "com.onlinestories.story_service.Client")
@EnableDiscoveryClient
@EnableScheduling
public class StoryServiceApplication {

	@Bean
	public CommandLineRunner migrateData(MongoTemplate mongoTemplate) {
		return args -> {
			// Tìm tất cả version có isPublished = true
			Query query = new Query(Criteria.where("isPublished").is(true));
			List<ChapterVersion> versions = mongoTemplate.find(query, ChapterVersion.class);

			int count = 0;
			for (ChapterVersion version : versions) {
				Chapter chapter = mongoTemplate.findById(version.getChapterId(), Chapter.class);
				if (chapter != null && chapter.getPublishedVersionId() == null) {
					chapter.setPublishedVersionId(version.getChapterVersionId());
					mongoTemplate.save(chapter);
					count++;
				}
			}
			System.out.println("Migrate thành công " + count + " chapters.");
		};
	}


	public static void main(String[] args) {
		SpringApplication.run(StoryServiceApplication.class, args);
	}

}
