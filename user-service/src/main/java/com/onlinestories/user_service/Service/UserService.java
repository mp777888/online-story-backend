package com.onlinestories.user_service.Service;

import com.onlinestories.user_service.Client.MediaClient;
import com.onlinestories.user_service.Client.TransactionClient;
import com.onlinestories.user_service.DTO.Request.SocialCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserUpdateRequest;
import com.onlinestories.user_service.DTO.Response.UserResponse;
import com.onlinestories.user_service.DTO.Response.WalletResponse;
import com.onlinestories.user_service.Entity.User;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {

    final UserRepository userRepository;
    final Keycloak keycloak;
    final TransactionClient transactionClient;
    final MediaClient mediaClient;

    @Value("${app.keycloak.realm}")
    String appRealm;


    @Transactional
    public ResponseEntity<UserResponse> createUser(UserCreateRequest request) {
        UsersResource usersResource = keycloak.realm(appRealm).users();

        // Check if user already exists in Keycloak
        List<UserRepresentation> existingUsers = usersResource.searchByUsername(request.getUsername(), true);
        if (!existingUsers.isEmpty()) {
            log.error("Username {} already exists in Keycloak", request.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
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

        try (Response response = usersResource.create(userRepresentation)) {

            if (response.getStatus() != 201) {
                log.error("Failed to create user in Keycloak. Status: {}, Body: {}", response.getStatus(), response.readEntity(String.class));
                return ResponseEntity.status(response.getStatus()).build();
            }

            userId = CreatedResponseUtil.getCreatedId(response);
            log.info("User {} created successfully in Keycloak with id {}", request.getUsername(), userId);

            WalletResponse walletResponse = transactionClient.createWallet(userId);
            log.info("Wallet created for userId {}: walletId {}", userId, walletResponse.getWalletId());

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
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (Exception e) {
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

            // rethrow so @Transactional will roll back DB changes
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @Transactional
    public ResponseEntity<UserResponse> createUserViaSocial(
            String userId, String email, SocialCreateRequest request){
        log.info("Creating user for social login with userId: {}", userId);

        if(userRepository.existsById(userId)){
            log.warn("User with userId {} already exists", userId);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
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
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        }
        catch (Exception e){
            log.error("Error creating user for social login: {}", e.getMessage());
            throw new RuntimeException("Failed to create user for social login", e);
        }
    }

    public ResponseEntity<UserResponse> getMyInfo(String userId) {
        try{
            User user = findUserById(userId);
            UserRepresentation userRep = keycloak.realm(appRealm)
                        .users()
                        .get(userId)
                        .toRepresentation();

            UserResponse response = UserResponse.builder()
                    .userId(user.getUserId())
                    .username(userRep.getUsername())
                    .nickname(user.getNickname())
                    .email(userRep.getEmail())
                    .dob(user.getDob())
                    .img(user.getImg())
                    .createdAt(user.getCreatedAt())
                    .build();
            return ResponseEntity.ok().body(response);
        }
        catch (Exception e){
            log.error("Error retrieving user info for userId {}: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<UserResponse> getUserById(String id){
        try{
            User user = findUserById(id);
            UserRepresentation userRep = keycloak.realm(appRealm)
                    .users()
                    .get(id)
                    .toRepresentation();

            UserResponse response = UserResponse.builder()
                    .userId(user.getUserId())
                    .username(userRep.getUsername())
                    .email(userRep.getEmail())
                    .dob(user.getDob())
                    .img(user.getImg())
                    .build();
            return ResponseEntity.ok().body(response);
        }
        catch (Exception e){
            log.error("Error retrieving user info for userId {}: {}", id, e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<UserResponse> updateProfile(String userId, MultipartFile file, UserUpdateRequest request){
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
                String imgUrl = mediaClient.uploadFile(file, "avatars");
                user.setImg(imgUrl);
            }

            if (request.getNickname() != null) {
                user.setNickname(request.getNickname());
            }

            if (request.getDob() != null) {
                user.setDob(request.getDob());
            }

            userRepository.save(user);

            UserResponse response = UserResponse.builder()
                    .userId(user.getUserId())
                    .username(userRep.getUsername())
                    .email(userRep.getEmail())
                    .dob(user.getDob())
                    .img(user.getImg())
                    .createdAt(user.getCreatedAt())
                    .build();
            return ResponseEntity.ok().body(response);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<String> deleteUser(String userId){
        try{
            User user = findUserById(userId);
            keycloak.realm(appRealm).users().get(userId).remove();
            userRepository.delete(user);
            return ResponseEntity.ok().body("User deleted successfully");
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<String> followUser(String userId, String followUserId){
        try{
            User user = findUserById(userId);
            User followUser = findUserById(followUserId);

            user.getFollowingIds().add(followUserId);
            followUser.getFollowerIds().add(userId);

            userRepository.save(user);
            userRepository.save(followUser);
            
            return ResponseEntity.ok().body("Followed user successfully");
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<String> unfollowUser(String userId, String followUserId){
        try{
            User user = findUserById(userId);
            User followUser = findUserById(followUserId);

            user.getFollowingIds().remove(followUserId);
            followUser.getFollowerIds().remove(userId);

            userRepository.save(user);
            userRepository.save(followUser);

            return ResponseEntity.ok().body("Unfollowed user successfully");
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<List<UserResponse>> getFollowers(String userId){
        try{
            User user = findUserById(userId);
            UserRepresentation userRep = keycloak.realm(appRealm)
                    .users()
                    .get(userId)
                    .toRepresentation();
            List<UserResponse> followers = user.getFollowerIds().stream()
                    .map(this::findUserById)
                    .map(follower -> UserResponse.builder()
                            .userId(follower.getUserId())
                            .nickname(follower.getNickname())
                            .img(follower.getImg())
                            .build())
                    .toList();
            return ResponseEntity.ok().body(followers);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<List<UserResponse>> getFollowing(String userId){
        try{
            User user = findUserById(userId);
            UserRepresentation userRep = keycloak.realm(appRealm)
                    .users()
                    .get(userId)
                    .toRepresentation();
            List<UserResponse> following = user.getFollowingIds().stream()
                    .map(this::findUserById)
                    .map(follow -> UserResponse.builder()
                            .userId(follow.getUserId())
                            .nickname(follow.getNickname())
                            .img(follow.getImg())
                            .build())
                    .toList();
            return ResponseEntity.ok().body(following);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }


    public ResponseEntity<Boolean> checkUserExistence(String userId){
        try{
            return ResponseEntity.ok().body(userRepository.existsById(userId));
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
