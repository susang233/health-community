import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom'; // 👈 新增
import { router } from '@/router'; // 👈 导入你定义的路由

createRoot(document.getElementById('root')!).render(
  <RouterProvider router={router} /> // 👈 关键：用 RouterProvider 包裹
);