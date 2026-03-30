import api from '@/services/axios';
import type { DailySummaryResult } from '@/types/food';
export const getDailySummary = () :Promise<DailySummaryResult> =>
  api.get('/user/food-record/daily/summary');