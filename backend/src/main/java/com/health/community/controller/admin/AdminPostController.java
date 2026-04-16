package com.health.community.controller.admin;

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

    @Operation(summary = "获取待审核帖子列表（分页）")
    @GetMapping("/pending")
    public Result<Page<Post>> getPendingPosts(@RequestParam(defaultValue = "1") int page) {
        return Result.success(adminPostService.getPendingPosts(page));
    }

    @Operation(summary = "获取所有帖子列表（分页）")
    @GetMapping
    public Result<Page<Post>> getPosts(@RequestParam(defaultValue = "1") int page) {
        return Result.success(adminPostService.getPosts(page));
    }

    @Operation(summary = "查看帖子详情")
    @GetMapping("/{postId}")
    public Result<AdminPostDetailVO> getPostDetail(@PathVariable Long postId) {
        return Result.success(adminPostService.getPostDetail(postId));
    }

    @Operation(summary = "审核帖子")
    @PostMapping("/review")
    public Result<Boolean> reviewPost(@Valid @RequestBody PostReviewDTO reviewDTO) {
        adminPostService.reviewPost(reviewDTO);
        return Result.success(true);
    }




}