import {api} from '@/services/axios';
import type { UserVO } from '@/types/user';



export const updateAvatar = (file: File): Promise<string> => {
  const formData = new FormData();
  formData.append('file', file);

  return api.put<string>('/user/upload-avatar', formData).then(res => res as unknown as string);
};




export const updateNickname = (nickName: string): Promise<string> => {
  return api.put('/user/update-nick-name', { nickName });
};


export const getUserTags =  (): Promise<TagSettingVO> =>  
   api.get('/user/tag-setting');

export const saveTagSetting = (tagSettings: TagSettingVO): Promise<boolean> => {
  return api.post('/user/tag-setting', tagSettings);
};

// 类型定义
export interface TagSettingVO {
  display: 'SHOW' | 'HIDE';
  tags: string[];
}
export const getUserVO = ( userId?:number ) :Promise<UserVO>=> {
  return api.get("/user/profile", {
    params: {    
      userId,
    },
  });
}




// 关注
export const followUser = (userId: number): Promise<void> => {
  return api.post(`/user/follow/${userId}`);
};

// 取消关注
export const unfollowUser = (userId: number): Promise<void> => {
  return api.delete(`/user/follow/${userId}`);
};