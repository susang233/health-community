// src/layouts/DashboardLayout.tsx

import {
  UserOutlined,
  LaptopOutlined,
  DashboardOutlined,
  LogoutOutlined,
} from "@ant-design/icons";
import type { MenuProps } from "antd";
import { Breadcrumb, Layout, Menu, theme, Avatar, Dropdown } from "antd";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { logout } from "@/utils/auth";
import { useHealthProfile } from "@/hooks/useHealthProfile";
const { Header, Content } = Layout; // 👈 不再需要 Sider

// 顶部菜单（key 对应路由路径）
const topMenuItems: MenuProps["items"] = [
  { key: "dashboard", icon: <DashboardOutlined />, label: "数据总览" },
  { key: "health", icon: <UserOutlined />, label: "记录" },
  { key: "community", icon: <LaptopOutlined />, label: "社群" },
];

export default function DashboardLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { requireProfile } = useHealthProfile();

  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  // 根据当前路径判断顶部菜单选中项
  const getSelectedTopMenu = () => {
    if (location.pathname === "/dashboard") return "dashboard";
    if (location.pathname.startsWith("/dashboard/health")) return "health";
    if (location.pathname.startsWith("/dashboard/community"))
      return "community";
    return "dashboard";
  };
  // 配置表便于扩展
  const pageTitleMap: Record<string, string> = {
    "/dashboard": "数据总览",
    "/dashboard/health/weight": "体重记录",
    "/dashboard/health/diet": "饮食记录",
    "/dashboard/health/water": "喝水记录",
    "/dashboard/health/sleep": "睡眠记录",
    "/dashboard/community/recommend": "推荐动态",
    "/dashboard/community/following": "关注动态",
    "/dashboard/community/my": "我的帖子",
    
  };
  const getPageTitle = () => {
    return pageTitleMap[location.pathname] || "健康管理";
  };

  // 顶部菜单点击
  const handleTopMenuClick = ({ key }: { key: string }) => {
    if (key === "dashboard") {
      navigate("/dashboard");
    } else if (key === "health") {
      // 点击"记录"菜单，需要检查档案
      requireProfile(() => {
        // 有档案才跳转到健康模块
        navigate("/dashboard/health/weight");
      });
    } else if (key === "community") {
      // 点击"社群"菜单，直接跳转
      requireProfile(() => {
        // 有档案才跳转到健康模块
         navigate("/dashboard/community/recommend");
      });
    
    
    } else {
      // 跳转到模块首页（会自动匹配子路由 index）
      navigate(`/dashboard/${key}`);
    }
  };

  // 退出登录
  const handleLogout = () => {
    logout(); // 清除 token 和用户信息
    navigate("/login");
  };

  const userMenu = {
    items: [
      { key: "profile", label: "个人资料", icon: <UserOutlined /> },
      {
        key: "logout",
        label: "退出登录",
        icon: <LogoutOutlined />,
        danger: true,
      },
    ],
    onClick: ({ key }: { key: string }) => {
      if (key === "logout") handleLogout();
    },
  };

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* 顶部导航 */}
      <Header
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <div style={{ display: "flex", alignItems: "center" }}>
          <div
            style={{
              color: "white",
              fontSize: 20,
              fontWeight: "bold",
              marginRight: 40,
            }}
          >
            🏥
          </div>
          <Menu
            theme="dark"
            mode="horizontal"
            selectedKeys={[getSelectedTopMenu()]} // 👈 动态计算
            items={topMenuItems}
            onClick={handleTopMenuClick}
            style={{ flex: 1, minWidth: 300 }} // 确保菜单有足够空间展示
          />
        </div>

        <Dropdown menu={userMenu} placement="bottomRight">
          <div
            style={{
              color: "white",
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              gap: 8,
            }}
          >
            <Avatar icon={<UserOutlined />} />
            <span>张三</span>
          </div>
        </Dropdown>
      </Header>

      {/* 主内容区 */}
      <Layout style={{ padding: "0 24px 24px" }}>
        <Breadcrumb style={{ margin: "16px 0" }}>
          <Breadcrumb.Item>首页</Breadcrumb.Item>
          <Breadcrumb.Item>{getPageTitle()}</Breadcrumb.Item>
        </Breadcrumb>

        <Content
          style={{
            padding: 24,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            minHeight: 280,
          }}
        >
          {/*  所有子页面通过 Outlet 渲染 */}
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
