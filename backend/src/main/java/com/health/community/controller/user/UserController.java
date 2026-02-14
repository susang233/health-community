package com.health.community.controller.user;

import com.health.community.common.result.Result;
import com.health.community.dto.LoginDTO;
import com.health.community.dto.RegisterDTO;
import com.health.community.service.UserService;
import com.health.community.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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


    @Operation(
            summary = "注册"
    )
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO){
        String username = userService.register(registerDTO);
        return Result.success(username);

    }

//    @PostMapping("/login")
//    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO){
//
//
//        return;
//
//    }
}