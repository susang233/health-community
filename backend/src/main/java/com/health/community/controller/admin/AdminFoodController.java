package com.health.community.controller.admin;

import com.health.community.common.annotation.RequireRole;
import com.health.community.common.enumeration.DataSource;

import com.health.community.common.result.Result;
import com.health.community.dto.FoodAddDTO;
import com.health.community.dto.FoodHiddenUpdateDTO;
import com.health.community.dto.FoodUpdateDTO;
import com.health.community.entity.Food;
import com.health.community.service.AdminFoodService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController // ← 关键！返回 JSON
@RequestMapping("/admin/food") // 建议加统一前缀
@RequiredArgsConstructor
@Validated
@RequireRole({"SUPER_ADMIN", "ADMIN"})
public class AdminFoodController {

    private final AdminFoodService foodService;





    @Operation(summary = "获取食物列表（支持按来源筛选，分页）")
    @GetMapping("/page")
    public Result<Page<Food>> adminFoodPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) DataSource dataSource,
            @RequestParam(required = false) Boolean hidden,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        return Result.success(foodService.adminFoodPage(name, dataSource,hidden, page, size));
    }
    @Operation(summary = "添加食物")
    @PostMapping("/add")
    public Result<Void> addFood(@Valid @RequestBody FoodAddDTO foodAddDTO){
        foodService.adminAddFood(foodAddDTO);
        return Result.success();
    }

    @Operation(summary = "修改食物")
    @PutMapping("/update")
    public Result<Void> updateFood(@Valid @RequestBody FoodUpdateDTO foodUpdateDTO){
        foodService.adminUpdateFood(foodUpdateDTO);
        return Result.success();
    }
    @Operation(summary = "修改食物隐藏状态")

    @PutMapping("/updateHidden")
    @RequireRole({"SUPER_ADMIN", "ADMIN"})
    public Result<Boolean> updateHidden(@Valid @RequestBody FoodHiddenUpdateDTO dto) {
        return Result.success(foodService.changeHidden(dto.getCode(), dto.getHidden()));
    }

    @Operation(summary = "上传食物图片")
    @PostMapping("/upload-image")
    public Result<String> uploadFoodImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = foodService.uploadFoodImage(file);
        return Result.success(imageUrl);
    }
    @Operation(summary = "删除食物")
    @DeleteMapping("/delete/{code}")
    @RequireRole({"SUPER_ADMIN", "ADMIN"})
    public Result<Void> deleteFood(@PathVariable String code) {
        foodService.deleteFood(code);
        return Result.success();
    }



}