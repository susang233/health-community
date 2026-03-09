// src/router/index.tsx
import { createBrowserRouter ,Navigate} from 'react-router-dom';
import App from '@/App';
import LoginPage from '@/pages/Login';
import RegisterPage from '@/pages/Register';
import AuthGuard from '@/components/AuthGuard';
import HealthLayout from '@/layouts/HealthLayout';
import CommunityLayout from '@/layouts/CommunityLayout';
import DashboardPage from '@/pages/DashboardPage'; // 仪表盘内容组件

import DashboardLayout from '@/layouts/DashboardLayout';
import WeightPage from '@/pages/Health/WeightPage';


import AssessmentPage from '@/pages/AssessmentPage';
import AssessmentResult from '@/pages/AssessmentPage/step/AssessmentResult';
import { getToken ,isTokenValid} from '@/utils/auth';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
{ 
        index: true, 
        element: <Navigate 
          to={(getToken()&&isTokenValid()) ? '/dashboard' : '/login'} 
          replace 
        /> 
      },
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },

      {
  element: <AuthGuard />,
  children: [
    {
      path: '/dashboard',
      element: <DashboardLayout />,
      children: [
        { index: true, element: <DashboardPage /> }, // /dashboard
        {
                path: 'health',
                element: <HealthLayout />,
                children: [
                  { index: true, element: <Navigate to="weight" replace /> }, // 可选的健康模块首页
                  { path: 'weight', element: <WeightPage /> },
                  
                  // ... 其他
                ]
              },
        {
                path: 'community',
                element: <CommunityLayout />,
                children: [
                  
                  // ... 其他
                ]
              }
      ],
    },
    {
  path: '/assessment',
 
  children: [
    { index: true, element: <AssessmentPage/> },
    { path: 'result', element: <AssessmentResult /> },
   
  ]
}
  ],
},
    ],
  },
]);