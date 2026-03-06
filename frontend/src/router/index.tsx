// src/router/index.tsx
import { createBrowserRouter } from 'react-router-dom';
import App from '@/App';
import LoginPage from '@/pages/Login';
import RegisterPage from '@/pages/Register';
import AuthGuard from '@/components/AuthGuard';
import DashboardPage from '@/pages/DashboardPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },

      {
        element: <AuthGuard />,  // 👈 拦截器
        children: [
          { path: '/', element: <DashboardPage /> },
          { path: '/dashboard', element: <DashboardPage /> },
          // 其他需要登录的页面放这里
        ],
      },
    ],
  },
]);