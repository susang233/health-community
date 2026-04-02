package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.common.enumeration.ActivityLevel;
import com.health.community.common.enumeration.Gender;

import com.health.community.common.exception.BusinessException;
import com.health.community.dto.HealthProfileDTO;
import com.health.community.entity.HealthProfile;
import com.health.community.entity.User;
import com.health.community.repository.HealthProfileRepository;
import com.health.community.repository.UserRepository;
import com.health.community.vo.HealthProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import static com.health.community.common.constant.MessageConstant.ACCOUNT_OR_PASSWORD_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor // 自动生成构造器
public class HealthService {
    private final UserRepository userRepository;
    private final HealthProfileRepository healthProfileRepository;
    private static final double WEIGHT_TOLERANCE = 0.05; // 允许 50 克误差



    //用于在未完善健康档案前对相关功能进行限制
    public boolean isProfileCompleted() {
        Integer userId = UserContext.getCurrentUserId();
        //使用userId再去查询
        return healthProfileRepository.existsByUserId(userId);
    }

    private void validateHealthData(HealthProfileDTO dto) {
        // 1. 基本范围校验（和 DTO 重复，但为了安全）
        if (dto.getHeight() < 100 || dto.getHeight() > 230) {
            throw new BusinessException("身高必须在100-230cm之间");
        }

        // 2. 业务规则校验（DTO 注解做不到的）
        if (dto.getTargetWeight() < dto.getCurrentWeight() * 0.5) {
            throw new BusinessException("目标体重不能低于当前体重的50%");
        }

        if (dto.getTargetWeight() > dto.getCurrentWeight() * 1.5) {
            throw new BusinessException("目标体重不能高于当前体重的150%");
        }

        // 3. 年龄合理性校验（DTO 注解做不到的）
        int age = Period.between(dto.getBirthday(), LocalDate.now()).getYears();
        if (age < 14 || age > 100) {
            throw new BusinessException("年龄必须在14-100岁之间");
        }
    }

    public HealthProfileVO saveHealthProfile(@Valid HealthProfileDTO healthProfileDTO) {
        log.info("health",healthProfileDTO);

        Integer userId = UserContext.getCurrentUserId();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ACCOUNT_OR_PASSWORD_ERROR));
        //计算tdee 和bmi，bmr，推荐热量
        //bmi
        validateHealthData(healthProfileDTO);//校验参数合法性
        Integer height =  healthProfileDTO.getHeight();
        Double currentWeight = healthProfileDTO.getCurrentWeight();

        // 1. 先计算原始BMI值
        double bmi = currentWeight / Math.pow(height / 100.0, 2);
        double bmiRounded = Math.round(bmi * 10) / 10.0;


        //bmr
        Gender gender = healthProfileDTO.getGender();


        LocalDate birthday = healthProfileDTO.getBirthday();
        int age = Period.between(birthday, LocalDate.now()).getYears();
        //男
        // 2. 计算 BMR
        double bmr = Gender.MALE.equals(healthProfileDTO.getGender())
                ? 10 * currentWeight + 6.25 *  height - 5 * age + 5
                : 10 * currentWeight + 6.25 *  height - 5 * age - 161;



        //tdee
        ActivityLevel activityLevel = healthProfileDTO.getActivityLevel();
        int tdee =(int) Math.round(activityLevel.getCoefficient()*bmr);

        //每日推荐热量

        Double targetWeight = healthProfileDTO.getTargetWeight();

        //减肥用户（目标体重<当前体重）
        //推荐热量=每日总消耗（TDEE）-400

        int recommendedCalories;

        if (Math.abs(targetWeight - currentWeight) <= WEIGHT_TOLERANCE) {
            recommendedCalories = tdee;
        } else if (targetWeight < currentWeight) {
            recommendedCalories = tdee - 400;
        } else {
            recommendedCalories = tdee + 300;
        }


        if (Gender.FEMALE.equals(gender)) {
            recommendedCalories = Math.max(1200, recommendedCalories);
        } else {
            recommendedCalories = Math.max(1500, recommendedCalories);
        }







        //通过userId查询该用户是否有健康档案

        Optional<HealthProfile> existingOpt = healthProfileRepository.findByUserId(user.getUserId());
        //有健康档案，则更新数据
        if(existingOpt.isPresent()){
            // 通过 userId 查询健康档案
            HealthProfile existing = existingOpt.get();

                // 更新现有记录

            existing.setGender(gender);
            existing.setHeight(height);
            existing.setBirthday(healthProfileDTO.getBirthday());
            existing.setActivityLevel(activityLevel);
            existing.setCurrentWeight(currentWeight);
            existing.setTargetWeight(targetWeight);
            existing.setBmi(bmiRounded);
            existing.setBmr(bmr);
            existing.setTdee(tdee);
            existing.setRecommendedCalories(recommendedCalories);

            healthProfileRepository.save(existing); // JPA 会自动 update

        }else{
            //无健康档案，插入数据
            HealthProfile healthProfile = HealthProfile.builder()
                    .userId(user.getUserId())
                    .gender(gender)
                    .height(height)
                    .birthday(healthProfileDTO.getBirthday())
                    .activityLevel(activityLevel)
                    .currentWeight(currentWeight)
                    .targetWeight(targetWeight)
                    .bmi(bmiRounded)
                    .bmr(bmr)
                    .tdee(tdee)
                    .recommendedCalories(recommendedCalories)
                    .build();
            healthProfileRepository.save(healthProfile);
            log.info("健康档案保存成功: userId={}, 目标={}, 推荐热量={}",
                    user.getUserId(),
                    targetWeight < currentWeight ? "减重" :
                            targetWeight > currentWeight ? "增重" : "维持",
                    recommendedCalories);

        }


        //返回vo
        return HealthProfileVO.builder()

                .gender(gender)
                .height(height)
                .birthday(healthProfileDTO.getBirthday())
                .activityLevel(activityLevel)
                .currentWeight(currentWeight)
                .targetWeight(targetWeight)
                .bmi(bmiRounded)
                .bmr(bmr)
                .tdee(tdee)
                .recommendedCalories(recommendedCalories)
                .build();
    }

public Integer getRecommendedCalories(Integer userId){

        return healthProfileRepository.findByUserId(userId)
                .map(HealthProfile::getRecommendedCalories)
                .orElseThrow(() -> new BusinessException("请先完善健康档案"));

}

    public HealthProfileVO getHealthProfile() {
        try{
            Integer currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new BusinessException("未登录！");
            }
            HealthProfile healthProfile = healthProfileRepository.findByUserId(currentUserId)

                    .orElseThrow(() -> new BusinessException("请先完善健康档案"));
            return HealthProfileVO.builder().gender(healthProfile.getGender())
                    .height(healthProfile.getHeight())
                    .birthday(healthProfile.getBirthday())
                    .activityLevel(healthProfile.getActivityLevel())
                    .currentWeight(healthProfile.getCurrentWeight())
                    .targetWeight(healthProfile.getTargetWeight())
                    .bmi(healthProfile.getBmi())
                    .bmr(healthProfile.getBmr())
                    .tdee(healthProfile.getTdee())
                    .recommendedCalories(healthProfile.getRecommendedCalories())
                    .build();
        } catch (Exception e) {
            throw new BusinessException("获取个人资料失败");
        }


    }
}
