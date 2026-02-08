package com.onlinestories.user_service.Service;

import com.onlinestories.user_service.Client.TransactionClient;
import com.onlinestories.user_service.DTO.Request.PackageAddingRequest;
import com.onlinestories.user_service.DTO.Request.PackageRegisterRequest;
import com.onlinestories.user_service.DTO.Request.UserCreateRequest;
import com.onlinestories.user_service.DTO.Request.UserUpdateRequest;
import com.onlinestories.user_service.DTO.Response.PackageResponse;
import com.onlinestories.user_service.DTO.Response.UserResponse;
import com.onlinestories.user_service.DTO.Response.WalletResponse;
import com.onlinestories.user_service.Entity.Package;
import com.onlinestories.user_service.Entity.User;
import com.onlinestories.user_service.Enum.ServicePackage;
import com.onlinestories.user_service.Repository.PackageRepository;
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
public class PackageService {

    final UserRepository userRepository;
    final PackageRepository serviceRepository;
    final Keycloak keycloak;
    final TransactionClient transactionClient;

    @Value("${app.keycloak.realm}")
    String appRealm;

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
