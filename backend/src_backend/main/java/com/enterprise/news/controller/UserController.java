package com.enterprise.news.controller;

import com.enterprise.news.common.ApiResponse;
import com.enterprise.news.dto.user.UpdateUserProfileRequest;
import com.enterprise.news.dto.user.UserHomeResponse;
import com.enterprise.news.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ApiResponse<UserHomeResponse> home(@PathVariable String id) {
        return ApiResponse.ok(userService.getHome(id));
    }

    @PutMapping("/me")
    public ApiResponse<UserHomeResponse> updateProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.ok("资料更新成功", userService.updateProfile(request));
    }
}