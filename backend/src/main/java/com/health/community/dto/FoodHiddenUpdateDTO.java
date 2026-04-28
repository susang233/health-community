package com.health.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data  // Getter/Setter/ToString/Equals/HashCode
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
@Builder

public class FoodHiddenUpdateDTO {

    @NotBlank
    private String code;

    @NotNull
    private Boolean hidden;
    // getter & setter
}