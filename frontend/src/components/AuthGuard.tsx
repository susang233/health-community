// src/components/AuthGuard.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { getToken } from '@/utils/auth';

export default function AuthGuard() {
  const token = getToken();
  
 
  console.log('AuthGuard - raw token:', JSON.stringify(token));
  console.log('AuthGuard - is token truthy?', !!token);

  if (!token) {
    console.log('无 token，跳转到 /login');
    return <Navigate to="/login" replace />;
  }

  console.log('有 token，渲染子页面');
  return <Outlet />;
}