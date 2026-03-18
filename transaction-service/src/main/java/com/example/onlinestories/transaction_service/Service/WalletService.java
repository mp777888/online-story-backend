package com.example.onlinestories.transaction_service.Service;
import com.example.onlinestories.transaction_service.Entity.Wallet;
import com.example.onlinestories.transaction_service.DTO.Response.WalletResponse;
import com.example.onlinestories.transaction_service.Repostiory.WalletRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletService {
    final WalletRepository walletRepository;

    public WalletResponse createWallet(String userId){
        log.info("Creating wallet for userId: {}", userId);
        Wallet wallet = Wallet.builder()
                .balance(0.0)
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
}
