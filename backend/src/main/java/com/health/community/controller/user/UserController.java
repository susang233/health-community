package com.health.community.controller.user;

import com.health.community.common.result.Result;
import com.health.community.dto.RegisterDTO;
import com.health.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController // ← 关键！返回 JSON
@RequestMapping("/user") // 建议加统一前缀
@RequiredArgsConstructor
public class UserController{
    private final UserService userService;

    @GetMapping("/check-username")
    public  Result<Boolean> checkUsername(@Valid @RequestParam String username){
        return Result.success(!userService.isUsernameExists(username));
    }



    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO){
        String username = userService.register(registerDTO);
        return Result.success(username);

    }
}