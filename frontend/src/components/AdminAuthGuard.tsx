// src/components/AdminAuthGuard.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { hasAdminToken ,isAdminTokenValid, adminLogout} from '@/utils/auth';

export default function AdminAuthGuard() {
  
  
 

  if (!hasAdminToken() || !isAdminTokenValid()) {
    console.log('无 admintoken/token失效，跳转到 /admin/login');
    adminLogout(); // 清除无效 token
    return <Navigate to="/admin/login" replace />;
  }

  console.log('有 admintoken，渲染子页面');
  return <Outlet />;
}