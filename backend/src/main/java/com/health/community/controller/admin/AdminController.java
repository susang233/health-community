package com.health.community.controller.admin;

import com.health.community.common.annotation.RequireRole;
import com.health.community.common.enumeration.Role;
import com.health.community.common.result.Result;
import com.health.community.dto.AdminCreateDTO;
import com.health.community.dto.LoginDTO;
import com.health.community.dto.RegisterDTO;
import com.health.community.service.AuthService;
import com.health.community.service.UserService;
import com.health.community.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.health.community.common.constant.MessageConstant.CODE_CAN_NOT_BE_NULL;

@RestController // ← 关键！返回 JSON
@RequestMapping("/admin") // 建议加统一前缀
@RequiredArgsConstructor
@Validated
public class AdminController {
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
            summary = "登录"
    )
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO){
        return Result.success(authService.adminLogin(loginDTO));

    }
    @PostMapping("/create-admins")
    @RequireRole("SUPER_ADMIN")
    public Result<Boolean> createAdmin(@Valid @RequestBody AdminCreateDTO dto) {
        return Result.success(authService.createAdmins(dto));

    }


    @Operation(
            summary = "修改头像"
    )
    @PostMapping("/upload-avatar")
    public Result<Boolean> uploadAvatar(@RequestParam MultipartFile file) {

        return Result.success(userService.uploadAvatar(file));
    }

    @Operation(
            summary = "修改昵称"
    )
    @PostMapping("/update-nick-name")
    public Result<Boolean> updateNickName(@RequestParam @Size(min = 1, max = 20,message = CODE_CAN_NOT_BE_NULL)String nickName) {

        return Result.success(userService.updateNickName(nickName));
    }
}

