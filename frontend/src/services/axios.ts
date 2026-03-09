// src/services/axios.ts
import { getToken} from '@/utils/auth';
import axios from 'axios';

export interface ApiError extends Error {
  code: number;
  data?: unknown;
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,  // 👈 关键！
  timeout: 10000,
});

// 请求拦截器：自动加上 token
api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
// 响应拦截器：统一处理业务 code
api.interceptors.response.use(
  (response) => {
    // 即使 HTTP 状态是 200，也要检查业务 code
    const { code, message, data } = response.data;

    // 假设后端约定：code === 200 表示成功
    if (code === 200) {
      // 成功时，只返回真正的 data
      return data;
    } else {
     const error: ApiError = new Error(message || '请求失败') as ApiError;
      error.code = code;
      error.data = response.data;
      error.message = message || '请求失败';
      throw error;
    }
  },
  (error) => {
    // 真正的网络错误（如超时、500、404 等）
    return Promise.reject(error);
  }
);

export default api;