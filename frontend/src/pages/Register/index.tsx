// src/pages/register.tsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { register }  from "@/services/auth";  // 👈 普通导入（函数）
import type { RegisterData } from "@/services/auth";  // 👈 类型导入（加 type）
import { Card, Form, Input, Button, Typography, message } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";
import styles from "./Register.module.css";

const { Title } = Typography;
  
export default function RegisterPage() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  

  const onFinish = async (values: RegisterData) => {
    setLoading(true);
    try {
      const username = await register(values);
      
      message.success("注册成功！");
      navigate("/login", { state: { username } }); // 传递刚注册的账号
      
    } catch (error) {
      if (error instanceof Error) {
        message.error(error.message); // ✅ 用 Ant Design 的提示组件
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
          注册
        </Title>

        <Form
          name="register"
          onFinish={onFinish}
          autoComplete="off"
          layout="vertical"
          size="large"
          
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
          <Form.Item
            name="confirmPassword"
            rules={[{ required: true, message: "请确认密码" }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="确认密码" />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={loading}
              style={{ height: 44, fontSize: 16 }}
            >
              注册
            </Button>
          </Form.Item>

          <div style={{ textAlign: "center" }}>
            <Link to="/login">已有账号？立即登录</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
}