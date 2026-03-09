export interface AssessmentData {
    username?: string;
  gender?: 'male' | 'female';
  height?: number;
  weight?: number;
  birthday?: string; // YYYY-MM-DD
  activityLevel?: number;
  currentWeight?: number;
  targetWeight?: number;
  bmi?: number;
  bmr?: number;
  tdee?: number;
  recommendedCalories?: number;
}

        