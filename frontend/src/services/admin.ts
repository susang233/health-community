import {adminApi} from '@/services/axios';
import type { AdminCreateDTO ,AdminInitVO,AdminPageQuery,AdminPageVO} from '@/types/user';



export const updateAdminAvatar = (file: File): Promise<string> => {
  const formData = new FormData();
  formData.append('file', file);

  return adminApi.put<string>('/admin/upload-avatar', formData).then(res => res as unknown as string);
};




export const updateAdminNickname = (nickName: string): Promise<string> => {
  return adminApi.put('/admin/update-nick-name', { nickName });
};



export const createAdmin = (data: AdminCreateDTO): Promise<AdminInitVO> => {
  return adminApi.post("/admin/create-admins", data);
  
};

export const getAdmins=(params: AdminPageQuery) :Promise<AdminPageVO>=> {
  return adminApi.get('/admin/page', { params });
}

  export const resetAdminPassword = (userId: number): Promise<string> =>
  adminApi.post(`/admin/${userId}/reset-password`);