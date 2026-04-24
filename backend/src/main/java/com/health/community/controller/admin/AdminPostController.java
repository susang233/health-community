package com.health.community.controller.admin;

import com.health.community.common.enumeration.PostStatus;
import com.health.community.common.result.Result;
import com.health.community.dto.PostReviewDTO;
import com.health.community.entity.Post;
import com.health.community.service.AdminPostService;
import com.health.community.vo.AdminPostDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/post")
@RequiredArgsConstructor
@Validated
public class AdminPostController {

    private final AdminPostService adminPostService;



    @Operation(summary = "查看帖子详情")
    @GetMapping("/{postId}")
    public Result<AdminPostDetailVO> getPostDetail(@PathVariable Long postId) {
        return Result.success(adminPostService.getPostDetail(postId));
    }
    @Operation(summary = "获取帖子列表（支持按状态筛选，分页）")
    @GetMapping
    public Result<Page<Post>> getPosts(

            @RequestParam(required = false) PostStatus status,

            @RequestParam(defaultValue = "1") int page
    ) {
        return Result.success(adminPostService.getPosts(status, page));
    }


    @Operation(summary = "审核帖子")
    @PostMapping("/review")
    public Result<Boolean> reviewPost(@Valid @RequestBody PostReviewDTO reviewDTO) {
        adminPostService.reviewPost(reviewDTO);
        return Result.success(true);
    }




}