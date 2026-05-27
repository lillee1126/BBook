package com.enterprise.news.service;

import com.enterprise.news.dto.user.UpdateUserProfileRequest;
import com.enterprise.news.dto.user.UserHomeResponse;

public interface UserService {

    UserHomeResponse getHome(String userId);

    UserHomeResponse updateProfile(UpdateUserProfileRequest request);
}