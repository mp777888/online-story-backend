package com.onlinestories.user_service.Service;

import com.onlinestories.user_service.DTO.Request.PackageAddingRequest;
import com.onlinestories.user_service.DTO.Request.PackageRegisterRequest;
import com.onlinestories.user_service.DTO.Request.UserCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserUpdateRequest;
import com.onlinestories.user_service.DTO.Response.PackageResponse;
import com.onlinestories.user_service.DTO.Response.UserResponse;
import com.onlinestories.user_service.Entity.Package;
import com.onlinestories.user_service.Entity.User;
import com.onlinestories.user_service.Entity.Wallet;
import com.onlinestories.user_service.Enum.ServicePackage;
import com.onlinestories.user_service.Repository.PackageRepository;
import com.onlinestories.user_service.Repository.UserRepository;
import com.onlinestories.user_service.Repository.WalletRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {

    final UserRepository userRepository;
    final WalletRepository walletRepository;
    final PackageRepository serviceRepository;
    final Keycloak keycloak;

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

        try (Response response = usersResource.create(userRepresentation)) {
            if (response.getStatus() == 201) {
                log.info("User {} created successfully in Keycloak", request.getUsername());
                 String userId = CreatedResponseUtil.getCreatedId(response);
                 User user = new User();
                 Wallet wallet = new Wallet();
                 wallet.setBalance(0.0);
                 user.setUserId(userId);
                 user.setNickname(request.getUsername());
                 user.setDob(request.getDob());
                 wallet = walletRepository.save(wallet);
                 user.setWalletId(wallet.getWalletId());
                 userRepository.save(user);

                UserResponse userResponse = new UserResponse();
                userResponse.setUserId(userId);
                userResponse.setUsername(request.getUsername());
                userResponse.setNickname(request.getUsername());
                userResponse.setEmail(request.getEmail());
                userResponse.setDob(request.getDob());
                userResponse.setCreatedAt(userRepresentation.getCreatedTimestamp());
                return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
            } else {
                log.error("Failed to create user in Keycloak. Status: {}, Body: {}", response.getStatus(), response.readEntity(String.class));
                return ResponseEntity.status(response.getStatus()).build();
            }
        } catch (Exception e) {
            log.error("Exception while creating user in Keycloak", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<UserResponse> getMyInfo() {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getSubject();
            User user = findUserById(userId);
            UserRepresentation userRep = keycloak.realm(appRealm)
                        .users()
                        .get(userId)
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
            log.error("Error retrieving user info for userId {}: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<UserResponse> updateProfile(UserUpdateRequest request){
        // Implementation for updating user profile goes here
        try{
            User user = findUserById(request.getUserId());
            UserRepresentation userRep = keycloak.realm(appRealm)
                    .users()
                    .get(request.getUserId())
                    .toRepresentation();

            if(request.getPassword() != null){
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setTemporary(false);
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(request.getPassword());
                keycloak.realm(appRealm)
                        .users()
                        .get(request.getUserId())
                        .resetPassword(credential);
            }

            if (request.getNickname() != null) {
                user.setNickname(request.getNickname());
            }

            if (request.getDob() != null) {
                user.setDob(request.getDob());
            }

            userRepository.save(user);

        }
        catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
        return ResponseEntity.ok(new UserResponse());
    }


    public ResponseEntity<PackageResponse> registerService(PackageRegisterRequest request) {
        // Implementation for registering a service package goes here

        try{
            Package pack = new Package();

            List<User> listUsers = List.of(findUserById(request.getUserId()));
            pack.setListUsers(listUsers);
            pack.setStartDate(java.time.LocalDate.now());
            double price = 0.0;
            switch (request.getServicePackage()){
                case "FREE":
                    log.info("Registering FREE service package for userId {}", request.getUserId());
                    pack.setServicePackage(ServicePackage.FREE);
                    break;
                case "PREMIUM":
                    log.info("Registering PREMIUM service package for userId {}", request.getUserId());
                    pack.setServicePackage(ServicePackage.PREMIUM);
                    price = 49.000;
                    break;
                case "GROUP":
                    log.info("Registering GROUP service package for userId {}", request.getUserId());
                    pack.setServicePackage(ServicePackage.GROUP);
                    price = 79.000;
                    break;
                default:
                    log.error("Invalid service package: {}", request.getServicePackage());
            }
            pack.setPrice(price * request.getMonths());
            pack.setEndDate(pack.getStartDate().plusMonths(request.getMonths()));
            serviceRepository.save(pack);

            PackageResponse response = PackageResponse.builder()
                    .serviceId(pack.getServiceId())
                    .servicePackage(pack.getServicePackage().name())
                    .listUsers(pack.getListUsers())
                    .price(pack.getPrice())
                    .startDate(pack.getStartDate())
                    .endDate(pack.getEndDate())
                    .build();
            return ResponseEntity.ok().body(response);
        }
        catch (Exception e){
            log.error("Error registering service package for userId {}: {}", request.getUserId(), e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<PackageResponse> addUserToGroupPackage(PackageAddingRequest request){
        try{
            Package pack = serviceRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Service package not found"));


            UsersResource usersResource = keycloak.realm(appRealm).users();
            UserRepresentation userRep = usersResource.searchByEmail(request.getEmail(),true).stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("User with email " + request.getEmail() + " not found in Keycloak"));
            User user = findUserById(userRep.getId());

            pack.getListUsers().add(user);
            serviceRepository.save(pack);
            return null;
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
