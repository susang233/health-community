// src/services/auth.ts
import {api} from '@/services/axios';
import type { AssessmentData, AssessmentResult } from '@/types/assessment';
import type { WeightRecord } from '@/types/health';





export const checkHealthProfile = () =>
  api.get('/user/health/check-profile');


export const saveHealthProfile =(data: AssessmentData)  :Promise<AssessmentResult>=>
  api.post('/user/health/profile', data);


export const getHealthProfile = () :Promise<AssessmentResult> =>
  api.get('/user/health/profile');


// 获取体重历史记录
export const getWeightHistory=(startDate: string, endDate: string) :Promise<WeightRecord[]>=>{
  return api.get('/user/health/weight/history', {
    params: { startDate, endDate }
  });
}

// 记录体重
export const recordWeight=(data: { weight: number; recordDate: string }) =>{
  return api.post('/user/health/weight-record', data);
}
export const getLatestWeight=():Promise<WeightRecord> =>{
  return api.get('/user/health/weight/latest');
}
export const getEarliestWeight=():Promise<WeightRecord> =>{
  return api.get('/user/health/weight/earliest');
}
