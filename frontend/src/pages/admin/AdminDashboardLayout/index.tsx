import { useState, useEffect } from "react";
import { useNavigate, useLocation, Outlet } from "react-router-dom";
import {
  Layout,
  Menu,
  Avatar,
  Dropdown,
  message,
  Upload,
  Input,
  Tooltip,
} from "antd";
import {
  CameraOutlined,
  EditOutlined,
  LogoutOutlined,
  UserOutlined,
  DashboardOutlined,
  CoffeeOutlined, // 饮食图标
  MessageOutlined, // 帖子图标
} from "@ant-design/icons";
import { updateAdminAvatar, updateAdminNickname } from "@/services/admin";
import { getAdmin, adminLogout } from "@/utils/auth"; // 注意：用 adminLogout
import { DEFAULT_AVATAR_URL } from "@/constant";

const { Header, Sider, Content } = Layout;

export default function AdminDashboardLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const admin = getAdmin();

  const [nickname, setNickname] = useState(admin?.nickname || "管理员");
  const [isEditingNickname, setIsEditingNickname] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState(admin?.avatar || "");
  const defaultAvatar = "/src/assets/icons/female.png"; // 确保路径正确

  // 获取当前选中的菜单项（根据路径）
  const getCurrentMenuItem = () => {
    if (location.pathname === "/admin/dashboard") return "dashboard";
    if (location.pathname === "/admin/dashboard/foods") return "foods";
    if (location.pathname === "/admin/dashboard/posts") return "posts";
    if (location.pathname === "/admin/dashboard/create") return "create";
    return "dashboard";
  };

  // 头像上传
  const handleAvatarUpload = async (file: File) => {
    if (file.size / 1024 / 1024 >= 3) {
      message.error("头像不能超过 3MB!");
      return false;
    }
    try {
      const newAvatarUrl = await updateAdminAvatar(file);
      if (admin) {
        const updatedAdmin = { ...admin, avatar: newAvatarUrl };
        localStorage.setItem("admin", JSON.stringify(updatedAdmin));
      }
      setAvatarUrl(newAvatarUrl);
      message.success("头像上传成功");
    } catch (err) {
      message.error("头像上传失败");
    }
    return false;
  };

  // 保存昵称
  const handleSaveNickname = async () => {
    if (!nickname.trim()) {
      message.error("昵称不能为空");
      return;
    }
    if (nickname.trim() === admin?.nickname) {
      setIsEditingNickname(false);
      return;
    }
    try {
      const newNickname = await updateAdminNickname(nickname.trim());
      if (admin) {
        const updatedAdmin = { ...admin, nickname: newNickname };
        localStorage.setItem("admin", JSON.stringify(updatedAdmin));
      }
      message.success("昵称修改成功");
      setIsEditingNickname(false);
    } catch (err) {
      message.error("昵称修改失败");
    }
  };

  // 退出登录
  const handleLogout = () => {
    adminLogout(); // 清除 admin token
    navigate("/admin/login", { replace: true });
  };

  // 侧边栏菜单项
  const menuItems = [
    {
      key: "dashboard",
      icon: <DashboardOutlined />,
      label: "首页",
      onClick: () => navigate("/admin/dashboard"),
    },
    {
      key: "foods",
      icon: <CoffeeOutlined />,
      label: "饮食管理",
      onClick: () => navigate("/admin/dashboard/foods"),
    },
    {
      key: "posts",
      icon: <MessageOutlined />,
      label: "帖子管理",
      onClick: () => navigate("/admin/dashboard/posts"),
    },
    {
      key: "create",
      icon: <UserOutlined />,
      label: "管理员创建",
      onClick: () => navigate("/admin/dashboard/create"),
    },
  ];

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* 顶部 Header */}
      <Header
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "0 24px",
          background: "#fff",
          boxShadow: "0 1px 4px rgba(0,0,0,0.1)",
        }}
      >
        <div style={{ fontSize: 24, fontWeight: "bold", color: "#1890ff" }}>
          🏥 后台管理
        </div>

        {/* 右上角用户下拉菜单 */}
        <Dropdown
          trigger={["hover"]}
          placement="bottomRight"
          dropdownRender={() => (
            <div style={{ width: 280, padding: 16, background: "#fff", borderRadius: 8 }}>
              <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
                <Upload showUploadList={false} beforeUpload={handleAvatarUpload}>
                  <div style={{ position: "relative", cursor: "pointer" }}>
                    <Avatar src={avatarUrl || DEFAULT_AVATAR_URL} size={48} />
                    <div
                      style={{
                        position: "absolute",
                        bottom: -2,
                        right: -2,
                        width: 20,
                        height: 20,
                        background: "#fff",
                        borderRadius: "50%",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        boxShadow: "0 0 2px rgba(0,0,0,0.2)",
                      }}
                    >
                      <CameraOutlined style={{ fontSize: 12 }} />
                    </div>
                  </div>
                </Upload>

                <div style={{ flex: 1, display: "flex", alignItems: "center", gap: 4 }}>
                  {isEditingNickname ? (
                    <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
                      <Input
                        value={nickname}
                        onChange={(e) => setNickname(e.target.value.slice(0, 20))}
                        onBlur={handleSaveNickname}
                        onPressEnter={handleSaveNickname}
                        autoFocus
                        size="small"
                        maxLength={20}
                        style={{ width: 100 }}
                      />
                      <span style={{ fontSize: 12, color: "#999" }}>{nickname.length}/20</span>
                    </div>
                  ) : (
                    <>
                      <Tooltip title={nickname}>
                        <span style={{ maxWidth: 100, overflow: "hidden", textOverflow: "ellipsis" }}>
                          {nickname}
                        </span>
                      </Tooltip>
                      <EditOutlined
                        style={{ cursor: "pointer", color: "#1890ff", fontSize: 14 }}
                        onClick={() => setIsEditingNickname(true)}
                      />
                    </>
                  )}
                </div>
              </div>

              <div
                style={{
                  padding: "8px 16px",
                  cursor: "pointer",
                  color: "#ff4d4f",
                  display: "flex",
                  alignItems: "center",
                  gap: 8,
                }}
                onClick={handleLogout}
              >
                <LogoutOutlined />
                退出登录
              </div>
            </div>
          )}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 8, cursor: "pointer" }}>
            <Avatar src={avatarUrl || DEFAULT_AVATAR_URL} size="small" />
            <span style={{ color: "#333", maxWidth: 100, overflow: "hidden", textOverflow: "ellipsis" }}>
              {nickname}
            </span>
          </div>
        </Dropdown>
      </Header>

      {/* 主体：侧边栏 + 内容区 */}
      <Layout>
        {/* 侧边栏 Sider */}
        <Sider
          width={200}
          style={{
            background: "#fff",
            height: "calc(100vh - 64px)", // 减去 Header 高度
            overflowY: "auto",
          }}
        >
          <Menu
            mode="inline"
            selectedKeys={[getCurrentMenuItem()]}
            items={menuItems}
            style={{ border: "none" }}
          />
        </Sider>

        {/* 内容区 */}
        <Layout style={{ padding: "24px" }}>
          <Content
            style={{
              background: "#fff",
              padding: 24,
              borderRadius: 8,
              minHeight: "calc(100vh - 120px)",
            }}
          >
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
}