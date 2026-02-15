package com.health.community.dto;
import com.health.community.common.enumeration.ActivityLevel;
import com.health.community.common.enumeration.Gender;
import jakarta.validation.constraints.NotBlank;;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;



@Data  // Getter/Setter/ToString/Equals/HashCode
public class HealthProfileDTO implements Serializable {

    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "性别不能为空")
    private Gender gender;

    @NotBlank(message = "身高不能为空")
    private Integer height; // cm

    @NotBlank(message = "出生日期不能为空")
    private LocalDate birthday; // Java 8+

    @NotBlank(message = "运动强度不能为空")
    private ActivityLevel activityLevel;

    @NotBlank(message = "体重不能为空")
    private Double currentWeight; // kg

    @NotBlank(message = "目标体重不能为空")
    private Double targetWeight; // kg





}