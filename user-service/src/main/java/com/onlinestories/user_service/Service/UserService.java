package com.onlinestories.user_service.Service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.common.user.event.UserEvent;
import com.onlinestories.user_service.Client.MediaClient;
import com.onlinestories.user_service.Client.TransactionClient;
import com.onlinestories.user_service.DTO.Request.SocialCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserUpdateRequest;
import com.onlinestories.user_service.DTO.Response.CheckInStatusResponse;
import com.onlinestories.user_service.DTO.Response.UserResponse;
import com.onlinestories.user_service.DTO.Response.WalletResponse;
import com.onlinestories.user_service.Entity.User;

import com.onlinestories.user_service.Kafka.Producer.UserEventProducer;
import com.onlinestories.user_service.Repository.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {

    final UserRepository userRepository;
    final Keycloak keycloak;
    final TransactionClient transactionClient;
    final MediaClient mediaClient;
    final UserEventProducer userEventProducer;

    @Value("${app.keycloak.realm}")
    String appRealm;


    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        UsersResource usersResource = keycloak.realm(appRealm).users();

        // Check if user already exists in Keycloak
        List<UserRepresentation> existingUsers = usersResource.searchByUsername(request.getUsername(), true);
        if (!existingUsers.isEmpty()) {
            log.error("Username {} already exists in Keycloak", request.getUsername());
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(request.getUsername());
        userRepresentation.setEmail(request.getEmail());
        userRepresentation.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        userRepresentation.setCredentials(Collections.singletonList(credential));

        String userId = null;
        String walletId = null;

        try (Response response = usersResource.create(userRepresentation)) {

            if (response.getStatus() != 201) {
                log.error("Failed to create user in Keycloak. Status: {}, Body: {}", response.getStatus(), response.readEntity(String.class));
                throw new AppException(ErrorCode.USER_CREATION_FAILED);
            }

            userId = CreatedResponseUtil.getCreatedId(response);
            log.info("User {} created successfully in Keycloak with id {}", request.getUsername(), userId);

            WalletResponse walletResponse = transactionClient.createWallet(userId);
            log.info("Wallet created for userId {}: walletId {}", userId, walletResponse.getWalletId());
            walletId = walletResponse.getWalletId();

            User user = new User();
            user.setUserId(userId);
            user.setNickname(request.getUsername());
            user.setDob(request.getDob());
            user.setCreatedAt(java.time.LocalDateTime.now());
            user.setWalletId(walletResponse.getWalletId());

            userRepository.save(user); // if this fails, exception will be thrown and handled below

            UserResponse userResponse = new UserResponse();
            userResponse.setUserId(userId);
            userResponse.setUsername(request.getUsername());
            userResponse.setNickname(request.getUsername());
            userResponse.setEmail(request.getEmail());
            userResponse.setDob(request.getDob());
            userResponse.setCreatedAt(user.getCreatedAt());

            UserEvent userCreatedEvent = UserEvent.builder()
                    .userId(userId)
                    .nickname(request.getUsername())
                    .description("")
                    .img("")
                    .build();
            userEventProducer.sendUserCreatedEvent(userCreatedEvent);

            return userResponse;
        }
        catch (Exception e) {
            log.error("Exception while creating user (compensating actions will run): {}", e.getMessage(), e);

            // if Keycloak user was created, remove it to avoid orphaned entry
            if (userId != null) {
                try {
                    keycloak.realm(appRealm).users().get(userId).remove();
                    log.info("Removed Keycloak user {}", userId);
                } catch (Exception ex) {
                    log.error("Failed to remove Keycloak user {} during rollback: {}", userId, ex.getMessage(), ex);
                }

            }

            if(walletId != null){
                try {
                    transactionClient.deleteWallet(walletId);
                    log.info("Deleted wallet {} during rollback", walletId);
                } catch (Exception ex) {
                    log.error("Failed to delete wallet {} during rollback: {}", walletId, ex.getMessage(), ex);
                }
            }

            // rethrow so @Transactional will roll back DB changes
            throw new AppException(ErrorCode.USER_CREATION_FAILED);
        }
    }

    @Transactional
    public UserResponse createUserViaSocial(
            String userId, String email, SocialCreateRequest request){
        log.info("Creating user for social login with userId: {}", userId);

        if(userRepository.existsById(userId)){
            log.warn("User with userId {} already exists", userId);
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        try{
            WalletResponse walletResponse = transactionClient.createWallet(userId);
            log.info("Wallet created for userId {}: walletId {}", userId, walletResponse.getWalletId());

            User user = new User();
            user.setUserId(userId);
            user.setNickname(request.getNickname());
            user.setDob(request.getDob());
            user.setCreatedAt(java.time.LocalDateTime.now());
            user.setWalletId(walletResponse.getWalletId());

            userRepository.save(user);
            UserResponse userResponse = new UserResponse();
            userResponse.setUserId(userId);
            userResponse.setNickname(request.getNickname());
            userResponse.setEmail(email);
            userResponse.setDob(request.getDob());
            userResponse.setCreatedAt(user.getCreatedAt());

            UserEvent userCreatedEvent = UserEvent.builder()
                    .userId(userId)
                    .nickname(request.getNickname())
                    .description("")
                    .img("")
                    .build();
            userEventProducer.sendUserCreatedEvent(userCreatedEvent);
            return userResponse;
        }
        catch (Exception e){
            log.error("Error creating user for social login: {}", e.getMessage());
            throw new AppException(ErrorCode.USER_CREATION_FAILED);
        }
    }

    public UserResponse getMyInfo(String userId) {
        try{
            User user = findUserById(userId);
            UserRepresentation userRep = keycloak.realm(appRealm)
                        .users()
                        .get(userId)
                        .toRepresentation();

            return UserResponse.builder()
                    .userId(user.getUserId())
                    .username(userRep.getUsername())
                    .nickname(user.getNickname())
                    .email(userRep.getEmail())
                    .dob(user.getDob())
                    .img(user.getImg())
                    .description(user.getDescription())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
        catch (Exception e){
            log.error("Error retrieving profile for userId {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    public UserResponse getUserById(String id){
        try{
            User user = findUserById(id);
            UserRepresentation userRep = keycloak.realm(appRealm)
                    .users()
                    .get(id)
                    .toRepresentation();

            return UserResponse.builder()
                    .userId(user.getUserId())
                    .username(userRep.getUsername())
                    .email(userRep.getEmail())
                    .nickname(user.getNickname())
                    .description(user.getDescription())
                    .dob(user.getDob())
                    .img(user.getImg())
                    .build();
        }
        catch (Exception e){
            log.error("Error retrieving profile for userId {}: {}", id, e.getMessage());
            throw e;
        }
    }

    public UserResponse updateProfile(String userId, MultipartFile file, UserUpdateRequest request){
        // Implementation for updating user profile goes here
        try{
            User user = findUserById(userId);
            UserRepresentation userRep = keycloak.realm(appRealm)
                    .users()
                    .get(userId)
                    .toRepresentation();

            if(request.getPassword() != null){
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setTemporary(false);
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(request.getPassword());
                keycloak.realm(appRealm)
                        .users()
                        .get(userId)
                        .resetPassword(credential);
            }

            if(file != null && !file.isEmpty()){
                if(user.getImg() != null) {
                    String message = mediaClient.deleteFile(user.getImg(), "image");
                    log.info("Deleted old avatar for userId {}: {}", userId, message);
                }
                String imgUrl = mediaClient.uploadFile(file, "avatars");
                user.setImg(imgUrl);
            }

            if (request.getNickname() != null) {
                user.setNickname(request.getNickname());
            }

            if (request.getDob() != null) {
                user.setDob(request.getDob());
            }

            if (request.getDescription() != null) {
                user.setDescription(request.getDescription());
            }

            userRepository.save(user);


            UserEvent userUpdatedEvent = UserEvent.builder()
                    .userId(userId)
                    .nickname(user.getNickname())
                    .description(user.getDescription())
                    .img(user.getImg())
                    .build();
            userEventProducer.sendUserUpdatedEvent(userUpdatedEvent);

            return UserResponse.builder()
                    .userId(user.getUserId())
                    .username(userRep.getUsername())
                    .email(userRep.getEmail())
                    .nickname(user.getNickname())
                    .description(user.getDescription())
                    .dob(user.getDob())
                    .img(user.getImg())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public void deleteUser(String userId){
        try{
            User user = findUserById(userId);
            keycloak.realm(appRealm).users().get(userId).remove();
            userRepository.delete(user);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void followUser(String userId, String followUserId){
        try{
            User user = findUserById(userId);
            User followUser = findUserById(followUserId);

            user.getFollowingIds().add(followUserId);
            followUser.getFollowerIds().add(userId);

            userRepository.save(user);
            userRepository.save(followUser);

        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void unfollowUser(String userId, String followUserId){
        try{
            User user = findUserById(userId);
            User followUser = findUserById(followUserId);

            user.getFollowingIds().remove(followUserId);
            followUser.getFollowerIds().remove(userId);

            userRepository.save(user);
            userRepository.save(followUser);

        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public List<UserResponse> getFollowers(String userId){
        try{
            User user = findUserById(userId);
            UserRepresentation userRep = keycloak.realm(appRealm)
                    .users()
                    .get(userId)
                    .toRepresentation();
            return user.getFollowerIds().stream()
                    .map(this::findUserById)
                    .map(follower -> UserResponse.builder()
                            .userId(follower.getUserId())
                            .nickname(follower.getNickname())
                            .img(follower.getImg())
                            .build())
                    .toList();
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public List<UserResponse> getFollowing(String userId){
        try{
            User user = findUserById(userId);
            UserRepresentation userRep = keycloak.realm(appRealm)
                    .users()
                    .get(userId)
                    .toRepresentation();
            return user.getFollowingIds().stream()
                    .map(this::findUserById)
                    .map(follow -> UserResponse.builder()
                            .userId(follow.getUserId())
                            .nickname(follow.getNickname())
                            .img(follow.getImg())
                            .build())
                    .toList();
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public Boolean isFollowing(String userId, String targetUserId){
        try{
            User user = findUserById(userId);
            return user.getFollowingIds().contains(targetUserId);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    @Transactional
    public String dailyCheckIn(String userId) {
        log.info("User {} is attempting to check in", userId);
        User user = findUserById(userId);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        if (user.getCheckInDates() == null) {
            user.setCheckInDates(new HashSet<>());
        }

        if (user.getCheckInDates().contains(today)) {
            throw new AppException(ErrorCode.ALREADY_CHECKED_IN);
        }

        // streak
        LocalDate yesterday = today.minusDays(1);
        if (user.getCheckInDates().contains(yesterday)) {
            user.setCheckInStreak(user.getCheckInStreak() + 1);
        } else {
            user.setCheckInStreak(1);
        }

        user.getCheckInDates().add(today);
        int tokenToAdd = 10;
        if(user.getCheckInDates().size() > 7){
            tokenToAdd += 20;
        }
        else if(user.getCheckInDates().size() > 5){
            tokenToAdd += 15;
        }
        else if(user.getCheckInDates().size() > 3){
            tokenToAdd += 10;
        }
        transactionClient.addCheckInTokens(userId, tokenToAdd);

        userRepository.save(user);
        log.info("User {} checked in successfully on {}", userId, today);
        return "Điểm danh thành công!";
    }

    public List<CheckInStatusResponse> getWeeklyCheckInStatus(String userId) {
        User user = findUserById(userId);
        Set<LocalDate> checkInDates = user.getCheckInDates() != null ? user.getCheckInDates() : new HashSet<>();

        List<CheckInStatusResponse> weeklyStatus = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startOfWeek.plusDays(i);

            CheckInStatusResponse dayStatus = CheckInStatusResponse.builder()
                    .date(currentDate)
                    .dayOfWeek(currentDate.getDayOfWeek().name())
                    .isCheckedIn(checkInDates.contains(currentDate))
                    .isPastOrToday(!currentDate.isAfter(today))
                    .isToday(currentDate.isEqual(today))
                    .build();

            weeklyStatus.add(dayStatus);
        }

        return weeklyStatus;
    }

    public Boolean checkUserExistence(String userId){
        try{
            return userRepository.existsById(userId);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
