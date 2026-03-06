// src/components/AuthGuard.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { hasToken ,isTokenValid} from '@/utils/auth';

export default function AuthGuard() {
  
  
 

  if (!hasToken() || !isTokenValid()) {
    console.log('无 token，跳转到 /login');
    return <Navigate to="/login" replace />;
  }

  console.log('有 token，渲染子页面');
  return <Outlet />;
}