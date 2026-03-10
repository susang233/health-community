// src/main.tsx
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { ConfigProvider } from 'antd'; // 👈 新增导入
import { router } from '@/router';
import '@arco-design/web-react/dist/css/arco.css'; 
import 'antd-mobile/es/global';
// 👇 定义你的绿色主题
const theme = {
  token: {
    colorPrimary: '#52c41a', // 主色：Ant Design 官方绿色（健康、清新）
    // 可选：微调其他设计变量
    // borderRadius: 6,
    // fontSize: 14,
    // colorBgContainer: '#f8fff9',
  },
};

createRoot(document.getElementById('root')!).render(
  // 👇 先 ConfigProvider，再 RouterProvider
  <ConfigProvider theme={theme}>
    <RouterProvider router={router} />
  </ConfigProvider>
);