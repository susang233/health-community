// src/services/auth.ts
import api from '@/services/axios';


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


export const login = (data: LoginData) =>
  api.post('/user/login', data);

export const register = (data: RegisterData) :Promise<string> =>
  api.post('/user/register', data);