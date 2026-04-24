// src/services/auth.ts
import {api} from '@/services/axios';


export interface LoginData {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterData {
  username: string;
  password: string;
  confirmPassword: string;
}

export interface LoginVO {
  token: string;
  userId: number;
  username: string;
  nickname: string;
  avatar: string;
  role: string;
  expireIn: number; 
}
 
export const login = (data: LoginData):Promise<LoginVO> =>
  api.post('/user/login', data);

export const register = (data: RegisterData) :Promise<string> =>
  api.post('/user/register', data);

export const adminLogin = (data: LoginData):Promise<LoginVO> =>
  api.post('/admin/login', data);