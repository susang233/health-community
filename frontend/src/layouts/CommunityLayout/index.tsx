//src/layouts/CommunityLayout/index.tsx
import CreatePostForm from '@/pages/Community/CreatePostForm';
import { EditOutlined } from '@ant-design/icons';
import { FloatButton, Menu, Modal } from 'antd';
import type { MenuProps } from 'antd';
import { useState } from 'react';
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
    key: 'my-posts',
    label: '我的帖子',
  },
];

export default function CommunityLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [isPostCreateModalOpen, setIsPostCreateModalOpen] = useState(false);
  const [formKey, setFormKey] = useState(0);

const openPostModal = () => {
  setFormKey(prev => prev + 1); // 每次打开都+1
  setIsPostCreateModalOpen(true);
};

 const handleCancel = () => {
    setIsPostCreateModalOpen(false);
  };

  const handleSuccess = () => {
    setIsPostCreateModalOpen(false);
    // 可选：刷新帖子列表
    // refetchPosts();
  };

const closePostModal = () => {
  setIsPostCreateModalOpen(false);
};
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
       <FloatButton
        icon={<EditOutlined />}
        tooltip="发布新帖子"
        onClick={openPostModal}
        style={{ right: 50, bottom: 50 }}
      />
       <Modal
        title="发布新帖子"
        open={isPostCreateModalOpen}
        onCancel={closePostModal}
        footer={null}
        width={600}
       
      >
        <CreatePostForm
   key={formKey}
    onSuccess={handleSuccess}
    onCancel={handleCancel}
  />
      </Modal>
    </div>
  );
}