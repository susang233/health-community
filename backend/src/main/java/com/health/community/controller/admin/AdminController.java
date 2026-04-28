package com.health.community.controller.admin;

import com.health.community.common.annotation.RequireRole;
import com.health.community.common.enumeration.DataSource;
import com.health.community.common.enumeration.Role;
import com.health.community.common.result.Result;
import com.health.community.dto.AdminCreateDTO;
import com.health.community.dto.LoginDTO;
import com.health.community.dto.RegisterDTO;
import com.health.community.dto.UpdateNickNameDTO;
import com.health.community.entity.Food;
import com.health.community.entity.User;
import com.health.community.service.AuthService;
import com.health.community.service.UserService;
import com.health.community.vo.AdminInitVO;
import com.health.community.vo.AdminVO;
import com.health.community.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public Result<AdminInitVO> createAdmin(@Valid @RequestBody AdminCreateDTO dto) {
        return Result.success(authService.createAdmin(dto));

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


    @Operation(summary = "分页获取管理员列表")
    @RequireRole("SUPER_ADMIN")
    @GetMapping("/page")
    public Result<Page<AdminVO>> adminFoodPage(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        return Result.success(userService.adminPage(name,page, size));
    }
    @Operation(summary = "超级管理员重置管理员密码")
    @PostMapping("/{userId}/reset-password")
    @RequireRole("SUPER_ADMIN")
    public Result<String> resetAdminPassword(@PathVariable Integer userId) {

        return Result.success(authService.resetPasswordToRandom(userId)); // 返回新明文密码
    }
}

