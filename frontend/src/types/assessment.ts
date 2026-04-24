import { Gender } from "./gender";
import { ActivityLevel } from "./activityLevel";
export interface AssessmentData {
 
  gender?: Gender;
  height?: number;
 
  birthday?: string; // YYYY-MM-DD
  activityLevel?: ActivityLevel;
  currentWeight?: number;
  targetWeight?: number;
}

export interface AssessmentResult {
  
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
  
}

 