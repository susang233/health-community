import type { ActivityLevel } from "./activityLevel";
import type { Gender } from "./gender";

// src/types/profileForm.ts
export interface ProfileFormData {
  gender?: Gender;
  height?: number;
  birthday?: string; // YYYY-MM-DD
  activityLevel?: ActivityLevel;
  currentWeight?: number;
  targetWeight?: number;
}