// src/services/auth.ts
import api from '@/services/axios';





export const checkHealthProfile = (username:string) =>
  api.get('/user/health/check-profile', { params: { username } });



