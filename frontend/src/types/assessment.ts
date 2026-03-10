import { Gender } from "./gender";
import { ActivityLevel } from "./activityLevel";
export interface AssessmentData {
  username?: string;
  gender?: Gender;
  height?: number;
 
  birthday?: string; // YYYY-MM-DD
  activityLevel?: ActivityLevel;
  currentWeight?: number;
  targetWeight?: number;
}

export interface AssessmentResult {
  username?: string;
  gender?: Gender;
  height?: number;
 
  birthday?: string; // YYYY-MM-DD
  activityLevel?: ActivityLevel;
  currentWeight?: number;
  targetWeight?: number;
  bmi: number;
  bmr: number;
  tdee: number;
  recommendedCalories: number;
  // ...其他字段
}

 