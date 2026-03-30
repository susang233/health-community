// src/services/auth.ts
import api from '@/services/axios';
import type { DailyDietVO, FoodDetailVO, FoodRecordDTO, FoodSearchVO } from '@/types/food';





export const getDailyDiet = (recordDate: string) :Promise<DailyDietVO> =>
 api.get('/user/food-record/daily', {
    params: {
      recordDate: recordDate 
    }
  });


export const saveFoodRecord =(data: FoodRecordDTO)  :Promise<boolean> =>
  api.post('/user/food-record/save', data);



  export const deleteFoodRecord = (id: number): Promise<boolean> =>
  api.delete(`/user/food-record/${id}`);

  export const getFoodDetail = (code: string) :Promise<FoodDetailVO> =>
 api.get('/user/food/food_detail', {
    params: {
      code:code 
    }
  });

  export const searchFoods = (q: string, page: number = 1): Promise<FoodSearchVO> =>
  api.get("/user/food/search", {
    params: {
      q,
      page,
    },
  })