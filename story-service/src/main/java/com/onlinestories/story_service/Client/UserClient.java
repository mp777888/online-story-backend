package com.onlinestories.story_service.Client;


import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.common.user.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") String id);
}
