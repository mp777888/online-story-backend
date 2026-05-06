package com.example.onlinestories.transaction_service.Service;
import com.example.onlinestories.transaction_service.Client.StoryClient;
import com.example.onlinestories.transaction_service.DTO.Response.*;
import com.example.onlinestories.transaction_service.Entity.*;
import com.example.onlinestories.transaction_service.Kafka.Producer.TransactionProducer;
import com.onlinestories.common.transaction.enums.HistoryStatus;
import com.example.onlinestories.transaction_service.Enums.PaymentStatus;
import com.example.onlinestories.transaction_service.Enums.PayoutStatus;
import com.example.onlinestories.transaction_service.Repostiory.*;
import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.common.story.dto.StoryDTOResponse;
import com.onlinestories.common.transaction.event.TransEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletService {
    WalletRepository walletRepository;
    UnlockStoryRepository unlockStoryRepository;
    HistoryRepository historyRepository;
    PayoutHistoryRepository payoutHistoryRepository;
    PendingPaymentRepository pendingPaymentRepository;
    MongoTemplate mongoTemplate;
    StoryClient storyClient;
    TransactionProducer transactionProducer;

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

    public WalletResponse getMyWallet(String userId) {
        log.info("Getting wallet for userId: {}", userId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for userId: {}", userId);
                    return new AppException(ErrorCode.WALLET_NOT_FOUND);
                });

        log.info("Wallet found for userId: {}, walletId: {}", userId, wallet.getWalletId());
        return WalletResponse.builder()
                .walletId(wallet.getWalletId())
                .readingTokens(wallet.getReadingTokens())
                .writingTokens(wallet.getWritingTokens())
                .build();
    }

    @Transactional
    public void topUpReadingTokens(String txnRef) {
        PendingPayment payment = pendingPaymentRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> {
                    log.warn("Pending payment not found for txnRef: {}", txnRef);
                    return new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        if (payment.getStatus() == PaymentStatus.PAID){
            return;
        }
        log.info("Topping up reading tokens for userId: {}, amount: {} VND", payment.getUserId(), payment.getAmount());

        int tokensToAdd = getReadingTokensByAmount(payment.getAmount());
        log.info("Calculated reading tokens to add: {} for userId: {}", tokensToAdd, payment.getUserId());


        try{
            updateReadingTokens(payment.getUserId(), tokensToAdd, HistoryStatus.TOP_UP);
            log.info("Reading tokens topped up for userId: {}, tokens added: {}", payment.getUserId(), tokensToAdd);
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            pendingPaymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Error topping up reading tokens for userId: {}, amount: {} VND, error: {}"
                    , payment.getUserId(), payment.getAmount(), e.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            pendingPaymentRepository.save(payment);
            throw new AppException(ErrorCode.TOP_UP_ERROR);
        }
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

        Wallet walletReader = walletRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for userId: {}", userId);
                    return new AppException(ErrorCode.WALLET_NOT_FOUND);
                });
        Wallet walletAuthor = walletRepository.findByUserId(storyDTOResponse.getAuthorId())
                .orElseThrow(() -> {
                    log.warn("Wallet not found for authorId: {}", storyDTOResponse.getAuthorId());
                    return new AppException(ErrorCode.WALLET_NOT_FOUND);
                });
        if (walletReader == null || walletAuthor == null) {
            log.error("Wallet not found");
            throw new AppException(ErrorCode.WALLET_NOT_FOUND);
        }

        if (walletReader.getReadingTokens() < storyDTOResponse.getUnlockPrice()) {
            log.error("Not enough reading tokens for userId: {}, required: {}, available: {}", userId, storyDTOResponse.getUnlockPrice(), walletReader.getReadingTokens());
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        } else {
            updateReadingTokens(userId, -storyDTOResponse.getUnlockPrice(), HistoryStatus.SPENT);
            updateWritingTokens(storyDTOResponse.getAuthorId(), storyDTOResponse.getUnlockPrice() * 80 / 100, HistoryStatus.EARNED);
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
    public void updateReadingTokens(String userId, int tokens, HistoryStatus status) {
        log.info("Updating reading tokens for userId: {}, tokens: {}", userId, tokens);

        try{
            Update update = new Update().inc("readingTokens", tokens);
            Query query = new Query().addCriteria(Criteria.where("userId").is(userId));
            mongoTemplate.updateFirst(query, update, Wallet.class);
            log.info("Reading tokens updated for userId: {}", userId);

            updateHistory(userId, tokens, status);
        } catch (Exception e) {
            log.error("Error updating reading tokens for userId: {}, tokens: {}, error: {}", userId, tokens, e.getMessage());
            throw new AppException(ErrorCode.UPDATE_TOKEN_ERROR);
        }
    }

    @Transactional
    public void updateWritingTokens(String userId, int tokens, HistoryStatus status) {
        log.info("Updating writing tokens for userId: {}, tokens: {}", userId, tokens);

        try{
            Update update = new Update().inc("writingTokens", tokens);
            Query query = new Query().addCriteria(Criteria.where("userId").is(userId));
            mongoTemplate.updateFirst(query, update, Wallet.class);
            log.info("Writing tokens updated for userId: {}", userId);

            updateHistory(userId, tokens, status);
        } catch (Exception e) {
            log.error("Error updating writing tokens for userId: {}, tokens: {}, error: {}", userId, tokens, e.getMessage());
            throw new AppException(ErrorCode.UPDATE_TOKEN_ERROR);
        }
    }

    public void updateHistory(String userId, int tokenChange, HistoryStatus status) {
        log.info("Updating history for userId: {}, tokenChange: {}", userId, tokenChange);
        History history = History.builder()
                .userId(userId)
                .tokenChange(tokenChange)
                .status(status)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
        try{
            mongoTemplate.save(history);
            log.info("History updated for userId: {}, tokenChange: {}", userId, tokenChange);
            if(status.equals(HistoryStatus.EARNED) || status.equals(HistoryStatus.PAYOUT)){
                TransEvent event = TransEvent.builder()
                        .userId(userId)
                        .tokens(tokenChange)
                        .status(status)
                        .build();
                transactionProducer.sendTransactionEvent(event);
            }
        } catch (Exception e) {
            log.error("Error updating history for userId: {}, tokenChange: {}, error: {}", userId, tokenChange, e.getMessage());
            throw new AppException(ErrorCode.HISTORY_ERROR);
        }
    }

    public Page<HistoryResponse> getMyHistory(
            String userId, String monthStr, int page, int size) {
        log.info("Getting history for userId: {}, month: {}, page: {}, size: {}", userId, monthStr, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<History> historyPage;

        if (monthStr != null && !monthStr.trim().isEmpty()) {
            YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

            historyPage = historyRepository.findByUserIdAndCreatedAtBetween(userId, startOfMonth, endOfMonth, pageable);
        } else {
            historyPage = historyRepository.findByUserId(userId, pageable);
        }

        log.info("History found for userId: {}, totalElements: {}", userId, historyPage.getTotalElements());
        return historyPage.map(history -> HistoryResponse.builder()
                .historyId(history.getHistoryId())
                .status(history.getStatus().name())
                .tokenChange(history.getTokenChange())
                .createdAt(history.getCreatedAt().toString())
                .build());
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

    public void updateStatus(String txnRef, String status) {
        log.info("Updating payment status for txnRef: {}, status: {}", txnRef, status);
        PendingPayment payment = pendingPaymentRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> {
                    log.warn("Pending payment not found for txnRef: {}", txnRef);
                    return new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                });
        payment.setStatus(PaymentStatus.valueOf(status));
        pendingPaymentRepository.save(payment);
        log.info("Payment status updated for txnRef: {}, status: {}", txnRef, status);
    }

    @Transactional
    public void payoutProcess(String authorId, long totalViews, String payoutMonth) {
        log.info("Processing payout for authorId: {}, totalViews: {}, payoutMonth: {}", authorId, totalViews, payoutMonth);
        double payoutAmount = totalViews * 0.5;
        try {
            updateWritingTokens(authorId, (int) payoutAmount, HistoryStatus.PAYOUT);
            PayoutHistory payoutHistory = PayoutHistory.builder()
                    .authorId(authorId)
                    .payoutMonth(payoutMonth)
                    .totalViews(totalViews)
                    .earnedTokens(payoutAmount)
                    .status(PayoutStatus.COMPLETED)
                    .executedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    .build();

            payoutHistoryRepository.save(payoutHistory);

            log.info("Payout processed for authorId: {}, payoutAmount: {}, payoutMonth: {}", authorId, payoutAmount, payoutMonth);

        } catch (Exception e) {
            log.error("Error processing payout for authorId: {}, payoutAmount: {}, payoutMonth: {}, error: {}",
                    authorId, payoutAmount, payoutMonth, e.getMessage());
            throw new AppException(ErrorCode.PAYOUT_ERROR);
        }
    }

    @Transactional
    public void addCheckInTokens(String userId, int tokens) {
        log.info("Adding check-in tokens for userId: {}, tokens: {}", userId, tokens);
        walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        updateReadingTokens(userId, tokens, HistoryStatus.CHECK_IN);
    }

    public Page<PayoutResponse> getPayoutHistory(String payMonth, int page, int size) {
        log.info("Getting payout history for payMonth: {}, page: {}, size: {}", payMonth, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PayoutHistory> historyPage;

        if (payMonth != null && !payMonth.trim().isEmpty()) {
            historyPage = payoutHistoryRepository.findByPayoutMonth(payMonth, pageable);
        } else {
            historyPage = payoutHistoryRepository.findAll(pageable);
        }

        log.info("Found {} payout records", historyPage.getTotalElements());

        return historyPage.map(history -> PayoutResponse.builder()
                .payId(history.getPayId())
                .authorId(history.getAuthorId())
                .payoutMonth(history.getPayoutMonth())
                .totalViews(history.getTotalViews())
                .earnedTokens(history.getEarnedTokens())
                .status(history.getStatus().name())
                .executedAt(history.getExecutedAt())
                .build());
    }

    public Page<PendingPaymentResponse> getPendingPayments(String monthStr, int page, int size) {
        log.info("Getting pending payments for month: {}, page: {}, size: {}", monthStr, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PendingPayment> paymentPage;

        if (monthStr != null && !monthStr.trim().isEmpty()) {
            // Parse chuỗi YYYY-MM sang ngày bắt đầu và kết thúc của tháng
            YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

            paymentPage = pendingPaymentRepository.findByCreatedAtBetween(startOfMonth, endOfMonth, pageable);
        } else {
            paymentPage = pendingPaymentRepository.findAll(pageable);
        }

        log.info("Found {} pending payments", paymentPage.getTotalElements());

        return paymentPage.map(payment -> PendingPaymentResponse.builder()
                .penId(payment.getPenId())
                .txnRef(payment.getTxnRef())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build());
    }

    public List<TopUserTopUpResponse> getTopUsersByTopUpAmount(int limit, String monthStr) {
        log.info("Getting top {} users by top-up amount", limit);

        Criteria matchCriteria = Criteria.where("status").is(PaymentStatus.PAID);
        if (monthStr != null && !monthStr.trim().isEmpty()) {
            YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

            matchCriteria.and("paidAt").gte(startOfMonth).lte(endOfMonth);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                // 1. Lọc giao dịch nạp tiền thành công (và lấy trong tháng nếu có)
                Aggregation.match(matchCriteria),

                // 2. Nhóm theo userId và tính tổng amount
                Aggregation.group("userId")
                        .sum("amount").as("totalAmount"),

                // 3. Sắp xếp giảm dần theo tổng tiền
                Aggregation.sort(Sort.Direction.DESC, "totalAmount"),

                // 4. Giới hạn số lượng user
                Aggregation.limit(limit)
        );

        AggregationResults<TopUserTopUpResponse> results = mongoTemplate.aggregate(
                aggregation,
                "pendingPayment",
                TopUserTopUpResponse.class
        );

        return results.getMappedResults();
    }

    private int getReadingTokensByAmount(int amount) {
        return switch (amount) {
            case 10000 -> 100;
            case 20000 -> 200;
            case 50000 -> 500 + 25;
            case 100000 -> 1000 + 100;
            case 200000 -> 2000 + 300;
            case 500000 -> 5000 + 1000;
            default -> throw new AppException(ErrorCode.INVALID_TOP_UP_AMOUNT);
        };
    }
}
