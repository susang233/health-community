export enum ActivityLevel {
  SEDENTARY = 'SEDENTARY',
  LIGHT = 'LIGHT',
  MODERATE = 'MODERATE',
  HEAVY = 'HEAVY',
  EXTREME = 'EXTREME',
}

export const ActivityLevelLabel: Record<ActivityLevel, string> = {
  [ActivityLevel.SEDENTARY]: '久坐不动',
  [ActivityLevel.LIGHT]: '轻度运动',
  [ActivityLevel.MODERATE]: '中度运动',
  [ActivityLevel.HEAVY]: '重度运动',
  [ActivityLevel.EXTREME]: '超高强度运动',
};