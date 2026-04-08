package com.health.community.controller.user;
import com.health.community.common.result.Result;
import com.health.community.dto.HealthProfileDTO;
import com.health.community.dto.WeightRecordDTO;
import com.health.community.service.HealthService;
import com.health.community.service.WeightRecordService;
import com.health.community.vo.HealthProfileVO;
import com.health.community.vo.WeightHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.health.community.common.constant.MessageConstant.CODE_CAN_NOT_BE_NULL;

@RestController // ← 关键！返回 JSON
@RequestMapping("/user/health") // 建议加统一前缀
@RequiredArgsConstructor
@Validated
public class HealthController {

    private final HealthService healthService;
    private final WeightRecordService weightRecordService;



    @Operation(
            summary = "保存修改健康档案"
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
    public Result<Boolean> checkHealthProfile(){

        return Result.success(healthService.isProfileCompleted());

    }

    @Operation(
            summary = "获取健康档案"
    )
    @GetMapping("/profile")
    public Result<HealthProfileVO> getHealthProfile(){

        return Result.success(healthService.getHealthProfile());

    }

    @Operation(
            summary = "获取体重记录"
    )
    @GetMapping("/weight/history")
    public Result<List<WeightHistoryVO>> getWeightHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<WeightHistoryVO> result = weightRecordService.getWeightHistory(startDate, endDate);
        return Result.success(result);
    }
    @Operation(
            summary = "记录体重"
    )
    @PostMapping("/weight-record")
    public Result<Void> recordWeight(@Valid @RequestBody WeightRecordDTO dto) {

        weightRecordService.saveWeightRecord(dto.getWeight(), dto.getRecordDate());
        return Result.success();
    }


}