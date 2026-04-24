// src/layouts/DashboardLayout.tsx

import {
  UserOutlined,
  LaptopOutlined,
  DashboardOutlined,
  LogoutOutlined,
} from "@ant-design/icons";
import type { MenuProps } from "antd";
import {
  Breadcrumb,
  Layout,
  Menu,
  theme,
  Avatar,
  Dropdown,
  Upload,
  message,
  Tooltip,
  Button,
  Modal,
  Input,
  Form,
  Checkbox,
   Radio, Space, Tabs,
   InputNumber,
   Select,
   DatePicker
} from "antd";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { logout } from "@/utils/auth";
import { useHealthProfile } from "@/hooks/useHealthProfile";
import {
  EditOutlined,
  SettingOutlined,
  MedicineBoxOutlined,
  CameraOutlined,
} from "@ant-design/icons";
import {  useState } from "react";
import { DEFAULT_AVATAR_URL } from "@/constant";

import { getUser } from "@/utils/auth";
import { getUserTags, saveTagSetting, updateAvatar, updateNickname, type TagSettingVO } from "@/services/user";

import { getHealthProfile, saveHealthProfile } from "@/services/health";
import type { ProfileFormData } from "@/types/profileForm";
import { ActivityLevelLabel } from "@/types/activityLevel";
import dayjs from "dayjs";
const { Header, Content } = Layout; 

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

  const user = getUser();
  const [nickname, setNickname] = useState(user?.nickname || "用户");
  const [isEditingNickname, setIsEditingNickname] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState(user?.avatar || "");
  const [isSettingsModalOpen, setIsSettingsModalOpen] = useState(false);
 

  // 头像上传（3MB限制）
  const handleAvatarUpload = async (file: File) => {
    const isLt3M = file.size / 1024 / 1024 < 3;
    if (!isLt3M) {
      message.error("头像不能超过 3MB!");
      return false;
    }

    try {
      const newAvatarUrl = await updateAvatar(file); // 现在类型是 string ✅

      // 更新 Storage
      if (user) {
        const updatedUser = { ...user, avatar: newAvatarUrl };

         if (localStorage.getItem("user")) {
        localStorage.setItem("user", JSON.stringify(updatedUser));
      } else {
        sessionStorage.setItem("user", JSON.stringify(updatedUser));
      }
      }

      // 更新状态，触发 UI 刷新
      setAvatarUrl(newAvatarUrl);
      message.success("头像上传成功");
    } catch (err) {
      message.error("头像上传失败");
    }

    return false;
  };
  const handleSaveNickname = async () => {
    if (!nickname.trim()) {
      message.error("昵称不能为空");
      return;
    }
    if (nickname.trim() === user?.nickname) {
      setIsEditingNickname(false);
      return; // 未修改，直接退出编辑
    }

    try {
      // 调用 API 更新昵称
      const newNickname = await updateNickname(nickname.trim());

      // 更新 Storage
      if (user) {
        const updatedUser = { ...user, nickname: newNickname };
        if (localStorage.getItem("user")) {
        localStorage.setItem("user", JSON.stringify(updatedUser));
      } else {
        sessionStorage.setItem("user", JSON.stringify(updatedUser));
      }
      }

      message.success("昵称修改成功");
      setIsEditingNickname(false);
    } catch (err) {
      message.error("昵称修改失败");
    }
  };

  //设置弹窗，获取数据
const [initialTagSettings, setInitialTagSettings] = useState<TagSettingVO>({
  display: 'SHOW',
  tags: [],
});


const [initialProfile, setInitialProfile] = useState<ProfileFormData>({});
// 表单实例
const [profileForm] = Form.useForm();
const [tagForm] = Form.useForm();
const [loading, setLoading] = useState(false);
// 在 openSettingsModal 中，保存一份原始快照
const [originalProfile, setOriginalProfile] = useState<ProfileFormData | null>(null);

const openSettingsModal = async () => {
  setLoading(true);
  try {
    const assessment = await getHealthProfile(); // 返回 AssessmentResult
    const tags = await getUserTags();

    // 只取可编辑字段作为表单初始值
    const profileInit: ProfileFormData = {
      gender: assessment?.gender,
      height: assessment?.height,
      birthday: assessment?.birthday ? dayjs(assessment.birthday) : undefined,// 注意：DatePicker 需要 dayjs 对象或 string
      activityLevel: assessment?.activityLevel,
      currentWeight: assessment?.currentWeight,
      targetWeight: assessment?.targetWeight,
    };
    const tagInit = {
  display: tags?.display ?? 'SHOW', // 确保是 'SHOW'/'HIDE'
  tags: tags?.tags ?? [],
};

setInitialTagSettings(tagInit);
tagForm.setFieldsValue(tagInit);
setOriginalProfile(profileInit); // 保存原始值方便对比
    setInitialProfile(profileInit);
    profileForm.setFieldsValue({
      ...profileInit,
      // 如果 birthday 是 string，AntD DatePicker 可以直接接受（需配合 format）
    });

    // ... 标签部分不变
    setIsSettingsModalOpen(true);
  } catch (err) {
    message.error("加载设置失败");
  } finally {
    setLoading(false);
  }
};


  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();
  //保存健康档案
const handleSaveProfile = async () => {
  try {
    const values = await profileForm.validateFields();
    
    const payload = {
    ...values,
    birthday: values.birthday?.format('YYYY-MM-DD') || undefined,
    //     转回字符串，匹配后端期望的 JSON 格式
  };

  await saveHealthProfile(payload); 
    
    
    message.success('资料保存成功');
    closeSettingsModal();
  } catch (err) {
    message.error('保存失败，请检查输入');
  }
};



//保存tag
const handleSaveTags = async () => {
  try {
    const values = await tagForm.validateFields();
    
    
    await saveTagSetting(values); // values 包含所有可编辑字段
    
    message.success('资料保存成功');
    closeSettingsModal();
  } catch (err) {
    message.error('保存失败，请检查输入');
  }
};
const isProfileModified = () => {
  if (!originalProfile) return false;
  const currentValues = profileForm.getFieldsValue();
  
  // AntD DatePicker 在 format="YYYY-MM-DD" 时，getFieldsValue() 返回 string！
  // 所以 currentValues.birthday 也是 string | undefined

  return (
    currentValues.gender !== originalProfile.gender ||
    currentValues.height !== originalProfile.height ||
    currentValues.currentWeight !== originalProfile.currentWeight ||
    currentValues.targetWeight !== originalProfile.targetWeight ||
    currentValues.birthday !== originalProfile.birthday || //  直接比字符串
    currentValues.activityLevel !== originalProfile.activityLevel
  );
};
// 重置表单并关闭弹窗
const closeSettingsModal = () => {
  setIsSettingsModalOpen(false);
  profileForm.resetFields();
  tagForm.resetFields();
};

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
            selectedKeys={[getSelectedTopMenu()]} // 动态计算
            items={topMenuItems}
            onClick={handleTopMenuClick}
            style={{ flex: 1, minWidth: 300 }} // 确保菜单有足够空间展示
          />
        </div>
        <Dropdown
          trigger={["hover"]}
          placement="bottomRight"
          popupRender={() => (
            <div
              style={{
                width: 280,
                padding: 16,
                background: "#fff",
                borderRadius: 8,
              }}
            >
              {/* 头像 + 昵称 + 修改昵称 */}
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 12,
                  marginBottom: 16,
                }}
              >
                <Upload
                  showUploadList={false}
                  beforeUpload={handleAvatarUpload}
                >
                  <div style={{ position: "relative", cursor: "pointer" }}>
                    <Avatar
                      src={avatarUrl || DEFAULT_AVATAR_URL}
                      size={48}
                      icon={<UserOutlined />}
                    />
                    {/* 相机图标（修复圆形） */}
                    <div
                      style={{
                        position: "absolute",
                        bottom: -2,
                        right: -2,
                        width: 20, // 宽高固定
                        height: 20, //  宽高固定
                        background: "#fff",
                        borderRadius: "50%", // 正圆
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

                <div
                  style={{
                    flex: 1,
                    display: "flex",
                    alignItems: "center",
                    gap: 4,
                  }}
                >
                  {isEditingNickname ? (
                    <div
                      style={{ display: "flex", alignItems: "center", gap: 4 }}
                    >
                      <Input
                        value={nickname}
                        onChange={(e) =>
                          setNickname(e.target.value.slice(0, 20))
                        } // 防止粘贴超长内容
                        onBlur={handleSaveNickname}
                        onPressEnter={handleSaveNickname}
                        autoFocus
                        size="small"
                        maxLength={20} //限制最大输入长度
                        style={{ width: 100 }}
                      />
                      <span style={{ fontSize: 12, color: "#999" }}>
                        {nickname.length}/20
                      </span>
                    </div>
                  ) : (
                    <>
                      <Tooltip title={nickname}>
                        <span
                          style={{
                            overflow: "hidden",
                            whiteSpace: "nowrap",
                            textOverflow: "ellipsis",
                            maxWidth: 100,
                            fontSize: 14,
                          }}
                        >
                          {nickname}
                        </span>
                      </Tooltip>
                      <EditOutlined
                        style={{
                          cursor: "pointer",
                          color: "#1890ff",
                          fontSize: 14,
                        }}
                        onClick={() => setIsEditingNickname(true)}
                      />
                    </>
                  )}
                </div>
              </div>

              <Menu
                items={[
                  {
                    key: "settings",
                    label: "设置",
                    icon: <SettingOutlined />,
                    onClick: openSettingsModal,
                  },
                  {
                    key: "assessment",
                    label: "健康测评",
                    icon: <MedicineBoxOutlined />,
                    onClick: () => navigate("/assessment"),
                  },
                  { type: "divider" },
                  {
                    key: "logout",
                    label: "退出登录",
                    icon: <LogoutOutlined />,
                    danger: true,
                    onClick: handleLogout,
                  },
                ]}
                style={{ border: "none" }}
              />
            </div>
          )}
        >
          {/* 右上角显示区 */}
          <div
            style={{
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              gap: 8,
              padding: "8px 0",
              minWidth: 0, //  关键：允许子元素 shrink
            }}
          >
            <Avatar src={avatarUrl || DEFAULT_AVATAR_URL} size="small" />
            <span
              style={{
                fontSize: 14,
                color: "#fff",
                maxWidth: 100, // 最大宽度
                overflow: "hidden",
                textOverflow: "ellipsis",
                whiteSpace: "nowrap",
                minWidth: 0, // 防止 flex 子项最小宽度干扰
              }}
            >
              {nickname}
            </span>
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
      {/* ===== 设置弹窗 ===== */}
<Modal
  title="设置"
  open={isSettingsModalOpen}
  onCancel={closeSettingsModal}
  footer={null}
  width={600}
  destroyOnClose // 关闭时销毁，避免状态残留
>
  <Tabs
    items={[
      {
  key: 'profile',
  label: '用户资料',
  children: (
    <Form
      form={profileForm}
      layout="vertical"
      initialValues={initialProfile}
    >
      {/* 性别 */}
      <Form.Item label="性别" name="gender">
        <Radio.Group>
          <Radio value="MALE">男</Radio>
          <Radio value="FEMALE">女</Radio>
        </Radio.Group>
      </Form.Item>

      {/* 身高 */}
      <Form.Item label="身高 (cm)" name="height">
        <InputNumber min={100} max={250} placeholder="请输入身高" style={{ width: '100%' }} />
      </Form.Item>

      {/* 当前体重 */}
      <Form.Item label="当前体重 (kg)" name="currentWeight">
        <InputNumber min={30} max={200} placeholder="请输入当前体重" style={{ width: '100%' }} />
      </Form.Item>

      {/* 目标体重 */}
      <Form.Item label="目标体重 (kg)" name="targetWeight">
        <InputNumber min={30} max={200} placeholder="请输入目标体重" style={{ width: '100%' }} />
      </Form.Item>

      {/* 生日 */}
      <Form.Item label="出生日期" name="birthday">
        <DatePicker
          style={{ width: '100%' }}
          format="YYYY-MM-DD"
          placeholder="选择生日"
        />
      </Form.Item>

      {/* 活动水平 */}
      <Form.Item label="日常活动水平" name="activityLevel">
  <Select placeholder="请选择活动水平">
    {Object.entries(ActivityLevelLabel).map(([value, label]) => (
      <Select.Option key={value} value={value}>
        {label}
      </Select.Option>
    ))}
  </Select>
</Form.Item>

      {/* 保存按钮 */}
      <Form.Item>
        <Button
          type="primary"
          onClick={handleSaveProfile}
          disabled={!isProfileModified()}
        >
          保存资料
        </Button>
      </Form.Item>
    </Form>
  ),
},
      {
        key: 'tags',
        label: '标签设置',
        children: (
          <Form
            form={tagForm}
            layout="vertical"
            
          >
            <Form.Item label="我的个人标签" />

            <Form.Item name="display" >
  <Radio.Group>
    <Radio value="SHOW">展示</Radio>    
    <Radio value="HIDE">不展示</Radio>   
  </Radio.Group>
</Form.Item>




            <div style={{ marginBottom: 8, color: '#999', fontSize: 12 }}>
              从下列标签中选1～2个身份吧～（可不选）
            </div>

            <Form.Item
              name="tags"
              rules={[
                {
                  validator: (_, value) => {
                    if (!value || value.length <= 2) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('最多选择2个标签'));
                  },
                },
              ]}
            >
              <Checkbox.Group>
                <Space direction="vertical">
                  {['学生党', '上班族', '宝妈', '健身党'].map((tag) => (
                    <Checkbox key={tag} value={tag}>
                      {tag}
                    </Checkbox>
                  ))}
                </Space>
              </Checkbox.Group>
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                onClick={handleSaveTags}
                // 可加脏检查，此处简化
              >
                保存设置
              </Button>
            </Form.Item>
          </Form>
        ),
      },
    ]}
/>
</Modal>
    </Layout>
  );
}
