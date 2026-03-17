// src/services/auth.ts
import api from '@/services/axios';
import type { AssessmentData, AssessmentResult } from '@/types/assessment';





export const checkHealthProfile = () =>
  api.get('/user/health/check-profile');


export const saveHealthProfile =(data: AssessmentData)  :Promise<AssessmentResult>=>
  api.post('/user/health/profile', data);
