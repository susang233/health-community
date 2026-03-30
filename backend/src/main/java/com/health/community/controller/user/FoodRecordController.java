package com.health.community.controller.user;

import com.health.community.common.result.Result;
import com.health.community.dto.FoodRecordDTO;
import com.health.community.service.FoodRecordService;
import com.health.community.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
    public Result<DailyDietVO> getDailyDiet(@RequestParam LocalDate recordDate){

        return Result.success(foodRecordService.getDailyDiet(recordDate));

    }

    @Operation(
            summary = "获取今日的饮食概要"
    )
    @GetMapping("/daily/summary")
    public Result<DailySummaryVO> getDailySummary(){

        return Result.success(foodRecordService.getDailySummary());

    }


}