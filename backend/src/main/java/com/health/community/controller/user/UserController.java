package com.health.community.controller.user;

import com.health.community.common.enumeration.Role;
import com.health.community.common.exception.BusinessException;
import com.health.community.common.result.Result;
import com.health.community.dto.LoginDTO;
import com.health.community.dto.RegisterDTO;
import com.health.community.dto.UpdateNickNameDTO;
import com.health.community.service.AuthService;
import com.health.community.service.UserService;
import com.health.community.vo.LoginVO;
import com.health.community.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.health.community.common.constant.MessageConstant.CODE_CAN_NOT_BE_NULL;

@RestController // ← 关键！返回 JSON
@RequestMapping("/user") // 建议加统一前缀
@RequiredArgsConstructor
@Validated
public class UserController{
    private final UserService userService;
    private final AuthService authService;

    @Operation(
            summary = "检查账号唯一性"
    )
    @GetMapping("/check-username")
    public  Result<Boolean> checkUsername(@RequestParam
                                              @NotBlank(message = CODE_CAN_NOT_BE_NULL)
                                              String username){

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
    @Operation(
            summary = "登录"
    )
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO){
        return Result.success(authService.login(loginDTO, Role.USER));

    }

    @Operation(
            summary = "修改头像"
    )
    @PutMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestParam MultipartFile file) {

        return Result.success(userService.uploadAvatar(file));
    }

    @Operation(
            summary = "修改昵称"
    )
    @PutMapping("/update-nick-name")
    public Result<String> updateNickName(@Valid @RequestBody UpdateNickNameDTO dto) {

        return Result.success(userService.updateNickName(dto.getNickName()));
    }

    @Operation(
            summary = "获取用户社交主页信息"
    )
    @GetMapping("/profile")
    public  Result<UserVO> getUserVO(@RequestParam(required = false) Integer userId){

        return Result.success(userService.findUserVOByUserId(userId));
    }
}

