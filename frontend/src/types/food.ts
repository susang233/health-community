// src/types/food.ts

export enum MealType {
  BREAKFAST = "BREAKFAST",
  LUNCH = "LUNCH",
  DINNER = "DINNER",
  SNACK = "SNACK",
}

export interface NutritionGoal {
  protein: number; // 克
  fat: number; // 克
  carbs: number; // 克
}

export interface ActualIntake {
  protein: number;
  fat: number;
  carbs: number;
}

export interface FoodRecordVO {
  id: number;
  foodCode: string;
  name: string;
  imageUrl: string;
  mealType: MealType;
  weight: number; // 摄入重量（克）
  isLiquid: boolean; // true → 显示 ml，false → 显示 g
  calories: number; // 千卡
  protein: number; // 克
  fat: number; // 克
  carbs: number; // 克
  healthLight: number; // 1=绿, 2=黄, 3=红
  recordTime: string; // "2026-03-25"
    //每100g基准值
  caloriesPer100g: number;
  proteinPer100g?: number;
  fatPer100g?: number;
  carbsPer100g?: number;
}


export interface MealRecordVO {
  type: MealType;
  suggestedMinCalories: number;
  suggestedMaxCalories: number;
  actualCalories: number;
  actualFat: number;
  actualCarbs: number;
  actualProtein: number;
  foods: FoodRecordVO[];
}

export interface DailyDietVO {
  recommendedCalories: number; // 推荐总热量
  remainingCalories: number; // 还可吃热量
  nutritionGoal: NutritionGoal;
  actualCalories: number;
  actualIntake: ActualIntake;
  meals: MealRecordVO[];
  recordDate: string; // "2026-03-25"
}

export interface DailySummaryResult {
  remainingCalories: number;
  nutritionGoal: NutritionGoal;
  actualIntake: ActualIntake;
}

export interface FoodRecordDTO {
  id?: number;
  recordTime: string; // YYYY-MM-DDTHH:mm:ss
  foodCode: string;
  name: string;
  mealType: MealType;
  weight: number; // 克
}




// 重命名：用 FoodDetailVO 作为标准食物模型
export type FoodDetailVO = {
  code: string;
  name: string;
  imageUrl?: string;
  isLiquid: boolean;
  healthLight: number;
  caloriesPer100g: number;
  proteinPer100g?: number;
  fatPer100g?: number;
  carbsPer100g?: number;
};

// 搜索结果也用这个类型（缺失字段运行时为 undefined）
export interface FoodSearchVO {
  page: number;
  foods: FoodDetailVO[]; // 统一类型
}