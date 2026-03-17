package com.health.community.controller.user;

import com.health.community.common.result.Result;
import com.health.community.dto.FoodRecordDTO;
import com.health.community.entity.FoodRecord;
import com.health.community.service.FoodRecordService;
import com.health.community.service.FoodService;
import com.health.community.vo.DailyDietVO;
import com.health.community.vo.FoodDetailVO;
import com.health.community.vo.FoodRecordVO;
import com.health.community.vo.FoodSearchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.health.community.common.constant.MessageConstant.CODE_CAN_NOT_BE_NULL;

@RestController // ← 关键！返回 JSON
@RequestMapping("/user/food-record") // 建议加统一前缀
@RequiredArgsConstructor
@Validated
public class FoodRecordController {

    private final FoodRecordService foodRecordService;





    @Operation(
            summary = "保存修改食物记录"
    )
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody @Valid FoodRecordDTO foodRecordDTO){

        return Result.success(foodRecordService.save(foodRecordDTO));
    }

    @Operation(summary = "删除食物记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteFoodRecord(
            @Parameter(description = "食物记录ID", required = true)
            @PathVariable @NotNull Long id
    ) {

        return Result.success(foodRecordService.deleteById(id));
    }

    @Operation(
            summary = "获取对应日期的饮食记录"
    )
    @GetMapping("/daily")
    public Result<DailyDietVO> checkHealthProfile(@RequestParam LocalDate recordDate){

        return Result.success(foodRecordService.getDailyDietVO(recordDate));

    }


}