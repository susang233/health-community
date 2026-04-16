package com.health.community.controller.user;

import com.health.community.common.enumeration.PostListType;
import com.health.community.common.result.Result;
import com.health.community.dto.CommentCreateDTO;
import com.health.community.dto.PostCreateDTO;
import com.health.community.dto.PostDTO;
import com.health.community.service.CommentService;
import com.health.community.service.FileStorageService;
import com.health.community.service.PostService;
import com.health.community.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user/post")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;
    private final CommentService commentService;


    @Operation(
            summary = "发帖"
    )
    @PostMapping("/create")
    public Result<Long> createPost(@Valid @RequestBody PostCreateDTO postCreateDTO) {
        return Result.success(postService.createPost(postCreateDTO));
    }

    @Operation(
            summary = "上传发帖图片"
    )
    @PostMapping("/upload-post-images")
    public Result<List<String>> uploadPostImages(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("未选择文件");
        }

        if (files.size() > 9) {
            throw new IllegalArgumentException("最多上传9张图片");
        }

        List<String> urls = files.stream()
                .filter(file -> !file.isEmpty())
                .map(fileStorageService::uploadPostImage)
                .toList();

        return Result.success(urls);
    }
    @Operation(
            summary = "获取帖子列表"
    )
    @GetMapping("/index")
    public Result<PostIndexVO> getPostList(
            @RequestParam(defaultValue = "RECOMMEND") PostListType type,
            @RequestParam(defaultValue = "1") int page

    ) {
        return Result.success(postService.getPostList(type, page));
    }
    @Operation(
            summary = "获取用户主页帖子列表"
    )
    @GetMapping
    public Result<UserPostVO> getUserPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer userId
    ) {
        return Result.success(postService.getUserPostList(page, userId));
    }
    @Operation(
            summary = "编辑帖子"
    )
    @PutMapping
    public Result<Boolean> updatePost(@RequestBody PostDTO postDTO) {

        return Result.success(postService.updatePost(postDTO));
    }
    @Operation(
            summary = "删除帖子"
    )
    @DeleteMapping("/{postId}")
    public Result<Boolean> deletePost(@PathVariable Long postId) {

        return Result.success(postService.deletePost(postId));

    }


    @Operation(
            summary = "获取帖子详情"
    )
    @GetMapping("/{postId}")
    public Result<PostVO> getPostVO(@PathVariable Long postId) {

        return Result.success(postService.getPostVO(postId));

    }


    @Operation(summary = "发表评论")
    @PostMapping("/comment/create")
    public Result<CommentVO> createComment( @Valid @RequestBody CommentCreateDTO dto
            ) {

        return Result.success(commentService.createComment(dto));
    }

    @Operation(summary = "分页获取评论列表")
    @GetMapping("/comment/{postId}")
    public Result<CommentListVO> getCommentList(
            @PathVariable @NotNull(message = "帖子ID不能为空") Long postId,
            @RequestParam(defaultValue = "1") Integer page) {


        return Result.success(commentService.getCommentVOList(postId, page));
    }

}