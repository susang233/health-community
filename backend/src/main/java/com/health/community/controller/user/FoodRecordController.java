package com.health.community.controller.user;

import com.health.community.common.result.Result;
import com.health.community.dto.FoodRecordDTO;
import com.health.community.entity.FoodRecord;
import com.health.community.service.FoodRecordService;
import com.health.community.service.FoodService;
import com.health.community.vo.FoodDetailVO;
import com.health.community.vo.FoodRecordVO;
import com.health.community.vo.FoodSearchVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.health.community.common.constant.MessageConstant.CODE_CAN_NOT_BE_NULL;

@RestController // ← 关键！返回 JSON
@RequestMapping("/user/food-record") // 建议加统一前缀
@RequiredArgsConstructor
@Validated
public class FoodRecordController {

    private final FoodRecordService foodRecordService;





    @Operation(
            summary = "搜索食物"
    )
    @PostMapping("/save")
    public Result save(@RequestBody @Valid FoodRecordDTO foodRecordDTO){
        foodRecordService.save(foodRecordDTO);
        return Result.success();
    }



}