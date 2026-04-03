package com.example.onlinestories.transaction_service.Service;
import com.example.onlinestories.transaction_service.Client.StoryClient;
import com.example.onlinestories.transaction_service.Entity.UnlockStory;
import com.example.onlinestories.transaction_service.Entity.Wallet;
import com.example.onlinestories.transaction_service.DTO.Response.WalletResponse;
import com.example.onlinestories.transaction_service.Repostiory.UnlockStoryRepository;
import com.example.onlinestories.transaction_service.Repostiory.WalletRepository;
import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.common.story.dto.StoryDTOResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletService {
    WalletRepository walletRepository;
    UnlockStoryRepository unlockStoryRepository;
    MongoTemplate mongoTemplate;
    StoryClient storyClient;

    public WalletResponse createWallet(String userId){
        log.info("Creating wallet for userId: {}", userId);
        Wallet wallet = Wallet.builder()
                .readingTokens(0)
                .writingTokens(0)
                .userId(userId)
                .build();
        var savedWallet = walletRepository.save(wallet);
        log.info("Wallet created with walletId: {}", savedWallet.getWalletId());
        return WalletResponse.builder()
                .walletId(savedWallet.getWalletId())
                .build();

    }

    public void deleteWallet(String userId) {
        log.info("Deleting wallet for userId: {}", userId);
        walletRepository.deleteByUserId(userId);
        log.info("Wallet deleted for userId: {}", userId);
    }

    @Transactional
    public void topUpReadingTokens(String userId, int tokens) {
        log.info("Topping up reading tokens for userId: {}, tokens: {}", userId, tokens);
        if(tokens < 0) {
            log.warn("Invalid token amount: {} for userId: {}", tokens, userId);
            throw new AppException(ErrorCode.INVALID_READING_TOKENS);
        }
        updateReadingTokens(userId, tokens);
    }

    @Transactional
    public void createUnlockStory(String userId, String storyId) {
        log.info("Creating unlock story for userId: {}, storyId: {}", userId, storyId);
        StoryDTOResponse storyDTOResponse = storyClient.checkStoryExistence(storyId);
        if (storyDTOResponse == null) {
            log.warn("Story not found for storyId: {}", storyId);
            throw new AppException(ErrorCode.STORY_NOT_FOUND);
        }

        if(storyDTOResponse.getUnlockPrice() < 0) {
            log.warn("Invalid reading tokens: {} for userId: {}, storyId: {}", storyDTOResponse.getUnlockPrice(), userId, storyId);
            throw new AppException(ErrorCode.INVALID_READING_TOKENS);
        }

        if (checkUnlockStoryExistence(userId, storyId)) {
            log.warn("Unlock story already exists for userId: {}, storyId: {}", userId, storyId);
            throw new AppException(ErrorCode.UNLOCK_ALREADY);
        }

        Wallet walletReader = walletRepository.findByUserId(userId);
        Wallet walletAuthor = walletRepository.findByUserId(storyDTOResponse.getAuthorId());
        if (walletReader == null || walletAuthor == null) {
            log.error("Wallet not found");
            throw new AppException(ErrorCode.WALLET_NOT_FOUND);
        }

        if (walletReader.getReadingTokens() < storyDTOResponse.getUnlockPrice()) {
            log.error("Not enough reading tokens for userId: {}, required: {}, available: {}", userId, storyDTOResponse.getUnlockPrice(), walletReader.getReadingTokens());
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        } else {
            updateReadingTokens(userId, -storyDTOResponse.getUnlockPrice());
            updateWritingTokens(storyDTOResponse.getAuthorId(), storyDTOResponse.getUnlockPrice() * 80 / 100);
        }

        var newUnlockStory = UnlockStory.builder()
                .userId(userId)
                .storyId(storyId)
                .unlockDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
        try{
            unlockStoryRepository.save(newUnlockStory);
            log.info("Unlock story created for userId: {}, storyId: {}", userId, storyId);
        } catch (Exception e) {
            log.error("Error creating unlock story for userId: {}, storyId: {}, error: {}", userId, storyId, e.getMessage());
            throw new AppException(ErrorCode.UNLOCK_ERROR);
        }
    }

    @Transactional
    public void updateReadingTokens(String userId, int tokens) {
        log.info("Updating reading tokens for userId: {}, tokens: {}", userId, tokens);

        try{
            Update update = new Update().inc("readingTokens", tokens);
            Query query = new Query().addCriteria(Criteria.where("userId").is(userId));
            mongoTemplate.updateFirst(query, update, Wallet.class);
            log.info("Reading tokens updated for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error updating reading tokens for userId: {}, tokens: {}, error: {}", userId, tokens, e.getMessage());
            throw new AppException(ErrorCode.UPDATE_TOKEN_ERROR);
        }
    }

    @Transactional
    public void updateWritingTokens(String userId, int tokens) {
        log.info("Updating writing tokens for userId: {}, tokens: {}", userId, tokens);

        try{
            Update update = new Update().inc("writingTokens", tokens);
            Query query = new Query().addCriteria(Criteria.where("userId").is(userId));
            mongoTemplate.updateFirst(query, update, Wallet.class);
            log.info("Writing tokens updated for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error updating writing tokens for userId: {}, tokens: {}, error: {}", userId, tokens, e.getMessage());
            throw new AppException(ErrorCode.UPDATE_TOKEN_ERROR);
        }
    }

    public Boolean checkUnlockStoryExistence(String userId, String storyId) {
        log.info("Checking unlock story existence for userId: {}, storyId: {}", userId, storyId);
        var unlockStory = unlockStoryRepository.findByUserIdAndStoryId(userId, storyId);
        if (unlockStory != null) {
            log.info("Unlock story exists for userId: {}, storyId: {}", userId, storyId);
            return true;
        } else {
            log.info("Unlock story does not exist for userId: {}, storyId: {}", userId, storyId);
            return false;
        }
    }
}
