// src/pages/Login.tsx
import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { adminLogin } from "@/services/auth"; //  普通导入（函数）
import type { LoginData } from "@/services/auth"; //  类型导入（加 type）
import {  setAdminToken , getAdminToken,setAdmin} from "@/utils/auth";
import { Card, Form, Input, Button, Typography, message, Checkbox } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";

import styles from "./Login.module.css";

const { Title } = Typography;

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  
  const [form] = Form.useForm(); //  表单实例


  
  //有token的用户访问登录页，直接跳转到仪表盘
  useEffect(() => {
  if (getAdminToken()) {
    navigate('/admin/dashboard', { replace: true });
    
  }
}, [navigate]);


// LoginPage.tsx

  const onFinish = async (values: LoginData) => {
    setLoading(true);
    try {
      const res = await adminLogin(values);
      setAdminToken(res.token,values.rememberMe); // 注意：如果用了拦截器改造，res 就是 data，不是 res.data
      setAdmin(res.userId,res.username, res.nickname,res.avatar,res.expireIn,res.role,values.rememberMe);
      message.success("登录成功！");
      navigate("/admin/dashboard");
    } catch (error) {
      if (error instanceof Error) {
        message.error(error.message); //  用 Ant Design 的提示组件
      } else {
        message.error("网络异常，请稍后重试");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <Card className={styles.loginCard}>
        <Title level={2} style={{ textAlign: "center", marginBottom: 32 }}>
          管理员登录
        </Title>

        <Form
          name="login"
          form={form}
          onFinish={onFinish}
          autoComplete="off"
          layout="vertical"
          size="large"
          initialValues={{ rememberMe: true }}
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: "请输入用户名" }]}
          >
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: "请输入密码" }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item name="rememberMe" valuePropName="checked" label={null}>
            <Checkbox>记住我，下次不需要重新登录</Checkbox>
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={loading}
             
            >
              登录
            </Button>
          </Form.Item>

         
        </Form>
      </Card>
    </div>
  );
}
