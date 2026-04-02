package com.health.community.controller.user;

import com.health.community.common.result.Result;
import com.health.community.dto.PostCreateDTO;
import com.health.community.dto.TagSettingDTO;
import com.health.community.service.FileStorageService;
import com.health.community.service.PostService;
import com.health.community.service.TagSettingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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


    @Operation(
            summary = "发帖"
    )
    @PostMapping("/create")
    public Result<Long> createPost(@Valid @RequestBody PostCreateDTO postCreateDTO) {
        return Result.success(postService.createPost( postCreateDTO));
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


}