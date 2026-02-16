package com.health.community.dto;
import com.health.community.common.enumeration.ActivityLevel;
import com.health.community.common.enumeration.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;


@Data
public class HealthProfileDTO implements Serializable {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotNull(message = "性别不能为空")
    private Gender gender;

    @NotNull(message = "身高不能为空")
    @Min(value = 100, message = "身高不能小于100cm")
    @Max(value = 230, message = "身高不能大于230cm")
    private Integer height;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    private LocalDate birthday;

    @NotNull(message = "运动强度不能为空")
    private ActivityLevel activityLevel;

    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "25.0", message = "体重不能小于25kg")
    @DecimalMax(value = "200.0", message = "体重不能大于200kg")
    private Double currentWeight;

    @NotNull(message = "目标体重不能为空")
    @DecimalMin(value = "25.0", message = "目标体重不能小于25kg")
    @DecimalMax(value = "200.0", message = "目标体重不能大于200kg")
    private Double targetWeight;
}
