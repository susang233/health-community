// src/components/AuthGuard.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { hasToken ,isTokenValid, logout} from '@/utils/auth';

export default function AuthGuard() {
  
  
 

  if (!hasToken() || !isTokenValid()) {
    console.log('无 token/token失效，跳转到 /login');
    logout(); // 清除无效 token
    return <Navigate to="/login" replace />;
  }

  console.log('有 token，渲染子页面');
  return <Outlet />;
}