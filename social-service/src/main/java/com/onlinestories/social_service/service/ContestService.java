package com.onlinestories.social_service.service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.social_service.client.MediaClient;
import com.onlinestories.social_service.dto.request.ContestRequest;
import com.onlinestories.social_service.dto.response.ContestResponse;
import com.onlinestories.social_service.dto.response.ParticipateResponse;
import com.onlinestories.social_service.model.Contest;
import com.onlinestories.social_service.model.ContestParticipation;
import com.onlinestories.social_service.repository.ContestEntryRepository;
import com.onlinestories.social_service.repository.ContestParticipationRepository;
import com.onlinestories.social_service.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContestService {
    ContestRepository contestRepository;
    ContestParticipationRepository participationRepository;
    ContestEntryRepository contestEntryRepository;
    MongoTemplate mongoTemplate;
    MediaClient mediaClient;

    public ContestResponse createContest(ContestRequest request, MultipartFile image) {
        log.info("Creating contest with title: {}", request.getTitle());

        Contest contest = Contest.builder()
                .title(request.getTitle())
                .topic(request.getTopic())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .updatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .isActive(true)
                .build();

        if(image != null && !image.isEmpty()) {
            try{
                String imageUrl = mediaClient.uploadFile(image, "contest-images");
                contest.setImg(imageUrl);
            } catch (Exception e) {
                log.error("Failed to upload contest image: {}", e.getMessage());
            }
        }

        try{
            Contest savedContest = contestRepository.save(contest);
            log.info("Contest created with ID: {}", savedContest.getId());
            return ContestResponse.builder()
                    .id(savedContest.getId())
                    .title(savedContest.getTitle())
                    .description(savedContest.getDescription())
                    .imageUrl(savedContest.getImg())
                    .startDate(savedContest.getStartDate().toString())
                    .endDate(savedContest.getEndDate().toString())
                    .totalEntries(0)
                    .totalVotes(0)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create contest: {}", e.getMessage());
            throw new AppException(ErrorCode.CREATE_CONTEST_FAILED);
        }
    }

//    public ParticipateResponse participateInContest(String contestId, String userId) {
//        log.info("User {} is trying to participate in contest {}", userId, contestId);
//
//        Contest contest = findById(contestId);
//        if (!contest.isActive() || contest.getEndDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
//            log.warn("Contest {} is not active or has already ended", contestId);
//            throw new AppException(ErrorCode.CONTEST_NOT_ACTIVE);
//        }
//
//        boolean alreadyParticipated = participationRepository.existsByContestIdAndUserId(contestId, userId);
//        if (alreadyParticipated) {
//            log.warn("User {} has already participated in contest {}", userId, contestId);
//            throw new AppException(ErrorCode.ALREADY_PARTICIPATED);
//        }
//
//        try {
//            participationRepository.save(
//                    ContestParticipation.builder()
//                            .contestId(contestId)
//                            .userId(userId)
//                            .hasParticipated(true)
//                            .participatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
//                            .build()
//            );
//            log.info("User {} successfully participated in contest {}", userId, contestId);
//            return ParticipateResponse.builder()
//                    .contestId(contestId)
//                    .userId(userId)
//                    .hasParticipated(true)
//                    .participatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
//                    .build();
//        } catch (Exception e) {
//            log.error("Failed to participate in contest: {}", e.getMessage());
//            throw new AppException(ErrorCode.PARTICIPATE_CONTEST_FAILED);
//        }
//    }




    public Contest findById(String contestId) {
        return contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));
    }
}