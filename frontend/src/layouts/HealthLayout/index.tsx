//src/layouts/HealthLayout/index.tsx
import { Menu } from 'antd';
import type { MenuProps } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
// 健康数据侧边栏
const healthSiderItems: MenuProps['items'] = [
  {
    key: 'weight',
    label: '体重记录',
  },
  {
    key: 'food',
    label: '饮食记录',
  },
  {
    key: 'water',
    label: '喝水记录',
  },
  {
    key: 'sleep',
    label: '睡眠记录',
  },
];

export default function HealthLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <div style={{ display: 'flex', gap: 24 }}>
      <Menu
        mode="inline"
        selectedKeys={[location.pathname.split('/').pop() || 'weight']}
        items={healthSiderItems}
        onClick={({ key }) => navigate(`/dashboard/health/${key}`)}
        style={{ width: 200 }}
      />
      <div style={{ flex: 1 }}>
        <Outlet />
      </div>
    </div>
  );
}