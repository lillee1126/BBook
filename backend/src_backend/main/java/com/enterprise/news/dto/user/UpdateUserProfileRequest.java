package com.enterprise.news.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Size(max = 30, message = "昵称不能超过 30 字")
        String nickname,

        @Size(max = 255, message = "头像地址不能超过 255 字")
        String avatar,

        @Size(max = 200, message = "简介不能超过 200 字")
        String bio,

        @Size(max = 50, message = "城市不能超过 50 字")
        String location
) {
}