// src/pages/AdminCreatePage.tsx
import React, { useState, useEffect } from "react";
import {
  Table,
  Button,
  Space,
  Typography,
  Input,
  Pagination,
  Modal,
  message,
  Form,
  Card,
  Result,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
} from "@ant-design/icons";
import {
  getAdmins,
  createAdmin,
  resetAdminPassword,
} from "@/services/admin"; 
import type {
  AdminPageQuery,
  AdminCreateDTO,
  AdminPageVO,
} from "@/types/user"; 
import { isAdminTokenValid, isSuperAdmin } from "@/utils/auth";

const { Title } = Typography;

export default function AdminCreatePage() {
  
 // 只超级管理员能访问
const hasPermission = isSuperAdmin() && isAdminTokenValid();

  // 列表
  const [data, setData] = useState<AdminPageVO["content"][]>([]);
  const [loading, setLoading] = useState(false);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);

  // 搜索
  const [name, setName] = useState("");

  // 添加弹窗
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [addForm] = Form.useForm();
  const [addLoading, setAddLoading] = useState(false);

  // 重置密码确认
  const [resetPwdId, setResetPwdId] = useState<number | null>(null);

  // 密码展示弹窗（新增）
  const [pwdModalVisible, setPwdModalVisible] = useState(false);
  const [showPwdText, setShowPwdText] = useState("");

  // 获取列表
  const fetchList = async () => {
  
    setLoading(true);
    try {
      const params: AdminPageQuery = {
        page: current,
        size: pageSize,
        name: name || undefined,
      };
      const res = await getAdmins(params);
      setData(res.content || []);
      setTotal(res.totalElements || 0);
    } catch (err) {
      console.error(err);
      if (err instanceof Error) {
              message.error(err.message); //  用 Ant Design 的提示组件
            } else {
             message.error("加载失败");
            }
      
    } finally {
      setLoading(false);
    }
  };

  // 搜索
  const handleSearch = () => {
    setCurrent(1);
    fetchList();
  };

  // 重置搜索
  const handleReset = () => {
    setName("");
    setCurrent(1);
    fetchList();
  };

  // 添加提交
  const handleAddSubmit = async (values: AdminCreateDTO) => {
    setAddLoading(true);
    try {
      const params = { ...values };
      if (!params.password) {
        delete params.password;
      }

      const res = await createAdmin(params);
      setShowPwdText(`创建成功！初始密码：${res.rawPassword}（请立即保存）`);
      setPwdModalVisible(true);
      
      setAddModalOpen(false);
      addForm.resetFields();
      fetchList();
    } catch (err) {
      console.error(err);
      message.error("创建失败");
    } finally {
      setAddLoading(false);
    }
  };

  // 重置密码
  const handleResetPwd = async () => {
    if (!resetPwdId) return;
    try {
      const newPwd = await resetAdminPassword(resetPwdId);
      setShowPwdText(`重置成功！新密码：${newPwd}（请立即保存）`);
      setPwdModalVisible(true);
    } catch (error) {
      message.error("重置失败");
    } finally {
      setResetPwdId(null);
    }
  };

  useEffect(() => {
    if (hasPermission) {
      fetchList();
    }
    console.log("权限校验结果:", hasPermission);
  }, [current, pageSize, hasPermission]);

  const columns: ColumnsType<AdminPageVO["content"][0]> = [
     {
      title: "序号",
      width: 80,
      align: "center",
      render: (_, __, index) => index + 1,
    },
    {
      title: "用户名",
      dataIndex: "username",
      width: 180,
    },
    {
      title: "昵称",
      dataIndex: "nickname",
      width: 180,
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      align: "center",
      render: (_, record) => (
        <Space>
          <Button
            type="text"
            icon={<ReloadOutlined />}
            onClick={() => setResetPwdId(record.userId)}
          >
            重置密码
          </Button>
        </Space>
      ),
    },
  ];

  // 无权限直接返回无权限页面
  if (!hasPermission) {
    return (
      <Result
        status="403"
        title="403"
        subTitle="抱歉，你没有权限访问此页面"
      />
    );
  }

  return (
    <div style={{ padding: "24px", height: "100vh", display: "flex", flexDirection: "column" }}>
      {/* 头部 */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 16,
        }}
      >
        <Title level={2} style={{ margin: 0 }}>
          管理员管理
        </Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setAddModalOpen(true);
            addForm.resetFields();
          }}
        >
          添加管理员
        </Button>
      </div>

      {/* 搜索栏 */}
      <div
        style={{
          marginBottom: 16,
          display: "flex",
          gap: 16,
          flexWrap: "wrap",
          alignItems: "center",
        }}
      >
        <Input
          placeholder="搜索用户名/昵称"
          value={name}
          onChange={(e) => setName(e.target.value)}
          onPressEnter={handleSearch}
          style={{ width: 260 }}
          prefix={<SearchOutlined />}
          allowClear
        />
        <Button onClick={handleReset}>重置</Button>
        <Button type="primary" onClick={handleSearch}>
          搜索
        </Button>
      </div>

      {/* 列表 */}
      <div style={{ flex: 1, minHeight: 0 }}>
        <Card
          bodyStyle={{
            height: "calc(100vh - 230px)",
            display: "flex",
            flexDirection: "column",
            padding: "12px 0",
          }}
        >
          <div style={{ flex: 1, overflow: "auto" }}>
            <Table
              rowKey="userId"
              columns={columns}
              dataSource={data}
              loading={loading}
              pagination={false}
              scroll={{ y: "calc(100vh - 320px)" }}
            />
          </div>

          {/* 分页 */}
          <div style={{ marginTop: 16, textAlign: "right" }}>
            <Pagination
              current={current}
              pageSize={pageSize}
              total={total}
              showSizeChanger
              showQuickJumper
              pageSizeOptions={["10", "20", "50"]}
              onChange={(page, size) => {
                setCurrent(page);
                setPageSize(size);
              }}
            />
          </div>
        </Card>
      </div>

      {/* 添加弹窗 */}
      <Modal
        title="添加管理员"
        open={addModalOpen}
        onCancel={() => setAddModalOpen(false)}
        footer={null}
        width={500}
        destroyOnClose
      >
        <Form
          form={addForm}
          layout="vertical"
          onFinish={handleAddSubmit}
          initialValues={{ password: "" }}
        >
          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: "请输入用户名" }]}
          >
            <Input placeholder="请输入用户名" />
          </Form.Item>

          <Form.Item
            label="密码（不填则自动生成）"
            name="password"
          >
            <Input.Password placeholder="选填" />
          </Form.Item>

          <div style={{ textAlign: "right", marginTop: 16 }}>
            <Space>
              <Button onClick={() => setAddModalOpen(false)}>取消</Button>
              <Button type="primary" htmlType="submit" loading={addLoading}>
                确认创建
              </Button>
            </Space>
          </div>
        </Form>
      </Modal>

      {/* 重置密码确认 */}
      <Modal
        title="确认重置密码"
        open={!!resetPwdId}
        onOk={handleResetPwd}
        onCancel={() => setResetPwdId(null)}
        okText="确认重置"
        cancelText="取消"
      >
        <p>确定要重置该管理员的密码吗？</p>
        <p>新密码会弹窗提示，请务必保存。</p>
      </Modal>

      {/* 密码展示弹窗（必须点击确认关闭） */}
      <Modal
        title="操作成功"
        open={pwdModalVisible}
        onOk={() => setPwdModalVisible(false)}
        onCancel={() => setPwdModalVisible(false)}
        okText="确定"
        cancelButtonProps={{ style: { display: "none" } }}
      >
        <p style={{ fontSize: "14px", fontWeight: 500 }}>{showPwdText}</p>
      </Modal>
    </div>
  );
}