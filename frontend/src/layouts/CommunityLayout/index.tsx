//src/layouts/CommunityLayout/index.tsx
import { Menu } from 'antd';
import type { MenuProps } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';


// 社群侧边栏
const communitySiderItems: MenuProps['items'] = [
  {
    key: 'recommend',
    label: '推荐动态',
  },
  
  {
    key: 'following',
    label: '关注动态',
  },
  {
    key: 'my',
    label: '我的帖子',
  },
];

export default function CommunityLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <div style={{ display: 'flex', gap: 24 }}>
      <Menu
        mode="inline"
        selectedKeys={[location.pathname.split('/').pop() || 'posts']}
        items={communitySiderItems}
        onClick={({ key }) => navigate(`/dashboard/community/${key}`)}
        style={{ width: 200 }}
      />
      <div style={{ flex: 1 }}>
        <Outlet />
      </div>
    </div>
  );
}