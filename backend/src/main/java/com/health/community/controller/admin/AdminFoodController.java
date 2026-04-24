package com.health.community.controller.admin;

import com.health.community.common.result.Result;
import com.health.community.service.FoodService;
import com.health.community.vo.FoodDetailVO;
import com.health.community.vo.FoodSearchVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.health.community.common.constant.MessageConstant.CODE_CAN_NOT_BE_NULL;

@RestController // ← 关键！返回 JSON
@RequestMapping("/user/food") // 建议加统一前缀
@RequiredArgsConstructor
@Validated
public class AdminFoodController {

    private final FoodService foodService;





    @Operation(
            summary = "搜索食物"
    )
    @GetMapping("/search")
    public Result<FoodSearchVO> searchFoods(
            @RequestParam
    @NotBlank(message = CODE_CAN_NOT_BE_NULL)
    String q,

    @RequestParam(defaultValue = "1")
    @Min(value = 1, message = "页码必须大于0")
    Integer page){

        return Result.success(foodService.searchFood(q,page));

    }
    @Operation(
            summary = "获取食物详情"
    )
    @GetMapping("/food_detail")
    public Result<FoodDetailVO> getFoodDetail(@RequestParam
                                                       @NotBlank(message = CODE_CAN_NOT_BE_NULL)
                                                       String code){

        return Result.success(foodService.getFoodDetail(code));

    }



}