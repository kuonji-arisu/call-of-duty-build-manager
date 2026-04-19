package io.github.kuonjiarisu.backend.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "用户名不能为空")
    @Size(max = 80, message = "用户名不能超过 80 个字符")
    String username,

    @NotBlank(message = "密码不能为空")
    String password,

    @NotBlank(message = "验证码 ID 不能为空")
    @Size(max = 80, message = "验证码 ID 不能超过 80 个字符")
    String captchaId,

    @NotBlank(message = "验证码不能为空")
    @Size(max = 20, message = "验证码不能超过 20 个字符")
    String captchaCode
) {
}
