package com.onlinestories.report_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/exists")
    Boolean checkUserExistence(@RequestParam String userId);

}
