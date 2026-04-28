// src/services/food.ts
import {api,adminApi} from '@/services/axios';
import type { DailyDietVO, FoodDetailVO, FoodPageQuery, FoodPageVO, FoodRecordDTO, FoodSearchVO, FoodUpdateDTO,FoodAddDTO } from '@/types/food';





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

  

export const getFoods=(params: FoodPageQuery) :Promise<FoodPageVO>=> {
  return adminApi.get('/admin/food/page', { params });
}

export const updateFood=( data: FoodUpdateDTO)=> {
  return adminApi.put('/admin/food/update', data);
}

export const addFood=( data: FoodAddDTO)=> {
  return adminApi.post('/admin/food/add', data);//不传code
}

export const deleteFood = (code: string) => {
  return adminApi.delete(`/admin/food/delete/${code}`);
}


export const updateHidden = (code: string, hidden: boolean) => {
  return adminApi.put('/admin/food/updateHidden', { code, hidden }); 
};

export const uploadFoodImage = (formData: FormData): Promise<string> => {
  return adminApi.post("/admin/food/upload-image", formData);
  
};//添加修改图片的时候需要先调用这个接口获取Url再调用修改/增加上传url