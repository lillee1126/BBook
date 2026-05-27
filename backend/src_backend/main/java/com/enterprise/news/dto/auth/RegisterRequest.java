package com.enterprise.news.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度需为 3-20 位")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 30, message = "密码长度需为 6-30 位")
        String password,

        @NotBlank(message = "昵称不能为空")
        @Size(min = 2, max = 20, message = "昵称长度需为 2-20 位")
        String nickname
) {
}