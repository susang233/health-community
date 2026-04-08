package com.health.community.controller.user;
import com.health.community.common.result.Result;
import com.health.community.service.FollowService;
import com.health.community.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

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
    /**
     * 获取当前用户关注的用户列表（我关注的人）
     * @param page 页码（从1开始）
     * @return 用户VO列表
     */
    @GetMapping("/followees")
    public Result<List<UserVO>> getFollowees(@RequestParam(defaultValue = "1") int page) {
        List<UserVO> followees = followService.getFollowee(page);
        return Result.success(followees);
    }

    /**
     * 获取当前用户的粉丝列表（谁关注了我）
     * @param page 页码（从1开始）
     * @return 用户VO列表
     */
    @GetMapping("/followers")
    public Result<List<UserVO>> getFollowers(@RequestParam(defaultValue = "1") int page) {
        List<UserVO> followers = followService.getFollower(page);
        return Result.success(followers);
    }

}