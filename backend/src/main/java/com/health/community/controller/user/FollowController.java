package com.health.community.controller.user;

import com.health.community.common.enumeration.PostListType;
import com.health.community.common.result.Result;
import com.health.community.dto.PostCreateDTO;
import com.health.community.service.FileStorageService;
import com.health.community.service.FollowService;
import com.health.community.service.PostService;
import com.health.community.vo.PostVO;
import com.health.community.vo.UserPostVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.health.community.common.constant.MessageConstant.CODE_CAN_NOT_BE_NULL;

@RestController
@RequestMapping("/user/follow")
@RequiredArgsConstructor
@Validated
public class FollowController {

    private final FollowService followService;



    @Operation(
            summary = "关注"
    )
    @PostMapping("/{userId}")
    public Result<Void> follow(@PathVariable @NotNull(message = "用户ID不能为空")  Integer userId) {


        followService.follow( userId);
        return Result.success();
    }
    @Operation(
            summary = "取消关注"
    )
    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(@PathVariable Integer userId) {

        followService.unfollow( userId);
        return Result.success();
    }
}