package com.health.community.controller.user;
import com.health.community.common.result.Result;
import com.health.community.dto.HealthProfileDTO;
import com.health.community.service.HealthService;
import com.health.community.vo.HealthProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.health.community.common.constant.MessageConstant.CODE_CAN_NOT_BE_NULL;

@RestController // ← 关键！返回 JSON
@RequestMapping("/user/health") // 建议加统一前缀
@RequiredArgsConstructor
@Validated
public class HealthController {

    private final HealthService healthService;



    @Operation(
            summary = "保存健康档案"
    )
    @PostMapping("/profile")
    public Result<HealthProfileVO> saveHealthProfile(@Valid @RequestBody HealthProfileDTO healthProfileDTO){
        HealthProfileVO healthProfileVO = healthService.saveHealthProfile(healthProfileDTO);
        return Result.success(healthProfileVO);

    }

    @Operation(
            summary = "检查健康档案存在"
    )
    @GetMapping("/check-profile")
    public Result checkHealthProfile(@RequestParam
                                         @NotBlank(message = CODE_CAN_NOT_BE_NULL)
                                         String username){

        return Result.success(healthService.isProfileCompleted(username));

    }



}