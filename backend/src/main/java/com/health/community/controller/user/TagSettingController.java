package com.health.community.controller.user;
import com.health.community.common.result.Result;
import com.health.community.dto.HealthProfileDTO;
import com.health.community.dto.TagSettingDTO;
import com.health.community.service.HealthService;
import com.health.community.service.TagSettingService;
import com.health.community.vo.HealthProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController // ← 关键！返回 JSON
@RequestMapping("/user/tag-setting") // 建议加统一前缀
@RequiredArgsConstructor
@Validated
public class TagSettingController {

    private final TagSettingService tagSettingService;


    @Operation(
            summary = "保存修改tag"
    )
    @PostMapping("/tag-setting")
    public Result<Boolean> saveTagSetting(@Valid @RequestBody TagSettingDTO tagSettingDTO) {

        return Result.success(tagSettingService.saveTagSetting( tagSettingDTO));
    }

}