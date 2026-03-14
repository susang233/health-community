package com.health.community.controller.user;
import com.health.community.common.result.Result;
import com.health.community.dto.HealthProfileDTO;
import com.health.community.service.FoodService;
import com.health.community.service.HealthService;
import com.health.community.vo.FoodSearchVO;
import com.health.community.vo.HealthProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController // ← 关键！返回 JSON
@RequestMapping("/user/food") // 建议加统一前缀
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;





    @Operation(
            summary = "搜索食物"
    )
    @GetMapping("/search")
    public Result<FoodSearchVO> checkHealthProfile(String q,Integer page){

        return Result.success(foodService.searchFood(q,page));

    }



}