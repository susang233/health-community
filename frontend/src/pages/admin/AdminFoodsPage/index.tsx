// src/pages/AdminFoodsPage/index.tsx
import React, { useState, useEffect } from "react";
import {
  Table,
  Tag,
  Button,
  Space,
  Tooltip,
  Typography,
  Select,
  Pagination,
  Modal,
  Image,
  message,
  Form,
  InputNumber,
  Switch,
  Radio,
  Input,
  Upload,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import {
  DeleteOutlined,
  EditOutlined,
  SearchOutlined,
  PlusOutlined,
  PictureOutlined,
} from "@ant-design/icons";
import type {
  Food,
  DataSource,
  FoodUpdateDTO,
  FoodAddDTO,
} from "@/types/food";
import {
  getFoods,
  updateFood,
  deleteFood,
  updateHidden,
  addFood,
  uploadFoodImage,
} from "@/services/food";
import type { RcFile, UploadFile } from "antd/es/upload/interface";

const { Title } = Typography;

// 映射
const dataSourceMap: Record<DataSource, string> = {
  BOOHEE: "薄荷",
  ADMIN: "后台添加",
};

const healthLightMap: Record<number, { text: string; color: string }> = {
  0: { text: "无", color: "default" },
  1: { text: "绿灯", color: "success" },
  2: { text: "黄灯", color: "warning" },
  3: { text: "红灯", color: "error" },
};

export default function AdminFoodsPage() {
  const [foods, setFoods] = useState<Food[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [total, setTotal] = useState(0);

  // 搜索关键字
  const [nameKeyword, setNameKeyword] = useState<string>("");

  // 筛选条件
  const [dataSourceFilter, setDataSourceFilter] = useState<
    DataSource | undefined
  >(undefined);
  const [hiddenFilter, setHiddenFilter] = useState<boolean | undefined>(undefined);

  // 编辑弹窗
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingFood, setEditingFood] = useState<Food | null>(null);
  const [editLoading, setEditLoading] = useState(false);
  const [editImageList, setEditImageList] = useState<UploadFile[]>([]); // 🔥 修复

  // 新增弹窗
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [addForm] = Form.useForm();
  const [addLoading, setAddLoading] = useState(false);
  const [addImageList, setAddImageList] = useState<UploadFile[]>([]); // 🔥 修复

  // 删除确认
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  // 图片上传前校验
  const beforeImageUpload = (file: RcFile) => {
    const isImage = file.type.startsWith("image/");
    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isImage) {
      message.error("只能上传图片格式文件");
      return false;
    }
    if (!isLt10M) {
      message.error("图片大小不能超过 10MB");
      return false;
    }
    return true;
  };

  // 获取食物列表
  const fetchFoods = async () => {
    setLoading(true);
    try {
      const result = await getFoods({
        page: currentPage,
        size: pageSize,
        dataSource: dataSourceFilter,
        hidden: hiddenFilter,
        name: nameKeyword || undefined,
      });
      setFoods(result.content || []);
      setTotal(result.totalElements || 0);
    } catch (error) {
      console.error("获取食物列表失败:", error);
      message.error("加载失败");
    } finally {
      setLoading(false);
    }
  };

  // 搜索
  const handleSearch = () => {
    setCurrentPage(1);
    fetchFoods();
  };

  useEffect(() => {
    fetchFoods();
  }, [currentPage, pageSize, dataSourceFilter, hiddenFilter]);

  // 分页
  const handlePaginationChange = (page: number, size?: number) => {
    setCurrentPage(page);
    if (size !== undefined) {
      setPageSize(size);
    }
  };

  // 打开编辑
  const handleEdit = (food: Food) => {
    setEditingFood({ ...food });
    if (food.imageUrl) {
      setEditImageList([
        {
          uid: "-edit-img",
          name: "food-img.jpg",
          status: "done",
          url: food.imageUrl,
        } as UploadFile,
      ]);
    } else {
      setEditImageList([]);
    }
    setEditModalOpen(true);
  };

  // 保存编辑
  const handleEditSave = async () => {
    if (!editingFood) return;
    if (editImageList.length === 0) {
      message.error("请上传食物图片");
      return;
    }
    setEditLoading(true);
    try {
      let imageUrl = editingFood.imageUrl;
      const file = editImageList[0];
      if (file?.originFileObj) {
        const formData = new FormData();
        formData.append("file", file.originFileObj);
        imageUrl = await uploadFoodImage(formData);
      }
      const updateData: FoodUpdateDTO = {
        code: editingFood.code,
        name: editingFood.name,
        imageUrl,
        healthLight: editingFood.healthLight,
        caloriesPer100g: editingFood.caloriesPer100g,
        proteinPer100g: editingFood.proteinPer100g,
        fatPer100g: editingFood.fatPer100g,
        carbsPer100g: editingFood.carbsPer100g,
        isLiquid: editingFood.isLiquid,
      };
      await updateFood(updateData);
      message.success("更新成功");
      setEditModalOpen(false);
      setEditImageList([]);
      fetchFoods();
    } catch (error) {
      message.error("更新失败");
    } finally {
      setEditLoading(false);
    }
  };

  // 删除
  const handleDelete = async () => {
    if (!deleteConfirmId) return;
    const foodToDelete = foods.find((f) => f.id === deleteConfirmId);
    if (!foodToDelete) {
      message.error("未找到该食物");
      return;
    }
    try {
      await deleteFood(foodToDelete.code);
      message.success("删除成功");
      setDeleteConfirmId(null);
      fetchFoods();
    } catch (error) {
      message.error("删除失败");
    }
  };

  // 新增食物提交
  const handleAddFood = async (values: any) => {
    if (addImageList.length === 0 || !addImageList[0]?.originFileObj) {
      message.error("请上传食物图片");
      return;
    }
    setAddLoading(true);
    try {
      const formData = new FormData();
      formData.append("file", addImageList[0].originFileObj);
      const imageUrl = await uploadFoodImage(formData);

      const data: FoodAddDTO = {
        name: values.name,
        imageUrl,
        healthLight: values.healthLight,
        caloriesPer100g: values.caloriesPer100g,
        proteinPer100g: values.proteinPer100g,
        fatPer100g: values.fatPer100g,
        carbsPer100g: values.carbsPer100g,
        isLiquid: values.isLiquid,
      };
      await addFood(data);

      message.success("添加成功！");
      setAddModalOpen(false);
      addForm.resetFields();
      setAddImageList([]);
      fetchFoods();
    } catch (err) {
      console.error(err);
      message.error("添加失败");
    } finally {
      setAddLoading(false);
    }
  };

  const columns: ColumnsType<Food> = [
    {
      title: "序号",
      width: 80,
      align: "center",
      render: (_, __, index) => index + 1,
    },
    {
      title: "食物名称",
      dataIndex: "name",
      key: "name",
      width: 120,
      render: (text) => (
        <Tooltip title={text}>
          <Typography.Paragraph ellipsis style={{ margin: 0 }}>
            {text}
          </Typography.Paragraph>
        </Tooltip>
      ),
    },
    {
      title: "图片",
      dataIndex: "imageUrl",
      width: 100,
      render: (url) =>
        url ? <Image src={url} width={60} height={60} preview={false} /> : "-",
    },
    { title: "热量(kcal/100g)", dataIndex: "caloriesPer100g", width: 100 },
    {
      title: "来源",
      dataIndex: "dataSource",
      width: 120,
      render: (source) => (
        <Tag>{dataSourceMap[source as DataSource] || source}</Tag>
      ),
    },
    {
      title: "健康灯",
      dataIndex: "healthLight",
      width: 100,
      render: (light) => {
        const { text, color } = healthLightMap[light] || { text: "未知", color: "default" };
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      width: 180,
      render: (time) => dayjs(time).format("YYYY-MM-DD HH:mm"),
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      fixed: "right",
      render: (_, record) => (
        <Space size="middle">
          <Switch
            checked={!record.hidden}
            checkedChildren="显示"
            unCheckedChildren="隐藏"
            onChange={async (checked) => {
              const newHidden = !checked;
              try {
                await updateHidden(record.code, newHidden);
                message.success(`已${newHidden ? "隐藏" : "显示"}`);
                fetchFoods();
              } catch (error) {
                message.error("操作失败");
              }
            }}
            style={{ width: 60 }}
          />
          <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button
            icon={<DeleteOutlined />}
            size="small"
            danger
            onClick={() => setDeleteConfirmId(record.id)}
            disabled={record.dataSource !== "ADMIN"}
            title={record.dataSource !== "ADMIN" ? "仅可删除后台添加的食物" : ""}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: "24px", height: "100vh", display: "flex", flexDirection: "column" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <Title level={2} style={{ margin: 0 }}>食物管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => {
          setAddModalOpen(true);
          addForm.resetFields();
          setAddImageList([]);
        }}>
          添加食物
        </Button>
      </div>

      <div style={{ marginBottom: 16, display: "flex", gap: 16, flexWrap: "wrap", alignItems: "center" }}>
        <Input
          placeholder="搜索食物名称"
          value={nameKeyword}
          onChange={(e) => setNameKeyword(e.target.value)}
          onPressEnter={handleSearch}
          style={{ width: 260 }}
          prefix={<SearchOutlined />}
          allowClear
          onClear={handleSearch}
        />
        <Button type="primary" onClick={handleSearch}>搜索</Button>
        <Select
          placeholder="数据源"
          allowClear
          value={dataSourceFilter}
          onChange={setDataSourceFilter}
          options={[
            { value: "BOOHEE", label: "薄荷" },
            { value: "ADMIN", label: "后台添加" },
          ]}
          style={{ width: 160 }}
        />
        <Select
          placeholder="是否隐藏"
          allowClear
          value={hiddenFilter}
          onChange={setHiddenFilter}
          options={[
            { value: false, label: "显示" },
            { value: true, label: "隐藏" },
          ]}
          style={{ width: 160 }}
        />
      </div>

      <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div style={{ height: "calc(100vh - 250px)", overflow: "hidden", marginBottom: 16 }}>
          <Table
            columns={columns}
            dataSource={foods}
            rowKey="id"
            loading={loading}
            pagination={false}
            scroll={{ x: 1000, y: "calc(100vh - 340px)" }}
          />
        </div>
      </div>

      <div style={{ marginTop: 16, textAlign: "right" }}>
        <Pagination
          current={currentPage}
          pageSize={pageSize}
          total={total}
          showSizeChanger
          showQuickJumper
          pageSizeOptions={[10, 20, 50]}
          onChange={handlePaginationChange}
          onShowSizeChange={(current, size) => handlePaginationChange(current, size)}
        />
      </div>

      {/* ===================== 编辑弹窗 ===================== */}
      <Modal
        title="编辑食物"
        open={editModalOpen}
        onCancel={() => {
          setEditModalOpen(false);
          setEditImageList([]);
        }}
        onOk={handleEditSave}
        confirmLoading={editLoading}
        width={600}
        destroyOnClose
      >
        {editingFood && (
          <Form layout="vertical">
            <Form.Item label="食物名称" required>
              <Input
                value={editingFood.name}
                onChange={(e) => setEditingFood({ ...editingFood, name: e.target.value })}
                style={{ width: "100%" }}
              />
            </Form.Item>

            <Form.Item label="食物图片" required>
              <Upload
                listType="picture-card"
                fileList={editImageList}
                beforeUpload={beforeImageUpload}
                onChange={({ fileList }) => setEditImageList(fileList)}
                onRemove={() => { setEditImageList([]); }}
                showUploadList={{ showPreviewIcon: true }}
                
              >
                {editImageList.length === 0 && (
                  <div>
                    <PictureOutlined />
                    <div>上传图片</div>
                  </div>
                )}
              </Upload>
            </Form.Item>

            <Form.Item label="热量 (kcal/100g)" required>
              <InputNumber
                value={editingFood.caloriesPer100g}
                onChange={(value) => setEditingFood({ ...editingFood, caloriesPer100g: value as number })}
                min={0}
                style={{ width: "100%" }}
              />
            </Form.Item>

            <Form.Item label="蛋白质 (g/100g)" required>
              <InputNumber
                value={editingFood.proteinPer100g}
                onChange={(value) => setEditingFood({ ...editingFood, proteinPer100g: value as number })}
                min={0}
                style={{ width: "100%" }}
              />
            </Form.Item>

            <Form.Item label="脂肪 (g/100g)" required>
              <InputNumber
                value={editingFood.fatPer100g}
                onChange={(value) => setEditingFood({ ...editingFood, fatPer100g: value as number })}
                min={0}
                style={{ width: "100%" }}
              />
            </Form.Item>

            <Form.Item label="碳水 (g/100g)" required>
              <InputNumber
                value={editingFood.carbsPer100g}
                onChange={(value) => setEditingFood({ ...editingFood, carbsPer100g: value as number })}
                min={0}
                style={{ width: "100%" }}
              />
            </Form.Item>

            <Form.Item label="健康灯" required>
              <Radio.Group
                value={editingFood.healthLight}
                onChange={(e) => setEditingFood({ ...editingFood, healthLight: e.target.value })}
              >
                <Radio value={0}>无</Radio>
                <Radio value={1}>绿</Radio>
                <Radio value={2}>黄</Radio>
                <Radio value={3}>红</Radio>
              </Radio.Group>
            </Form.Item>

            <Form.Item label="是否液体" required>
              <Switch
                checked={editingFood.isLiquid}
                onChange={(checked) => setEditingFood({ ...editingFood, isLiquid: checked })}
              />
            </Form.Item>
          </Form>
        )}
      </Modal>

      {/* ===================== 添加弹窗 ===================== */}
      <Modal
        title="添加食物"
        open={addModalOpen}
        onCancel={() => {
          setAddModalOpen(false);
          addForm.resetFields();
          setAddImageList([]);
        }}
        footer={null}
        width={600}
        destroyOnClose
      >
        <Form
          form={addForm}
          layout="vertical"
          onFinish={handleAddFood}
          initialValues={{ healthLight: 0, isLiquid: false }}
        >
          <Form.Item
            label="食物名称"
            name="name"
            rules={[{ required: true, message: "请输入食物名称" }]}
          >
            <Input />
          </Form.Item>

          <Form.Item label="食物图片" required>
            <Upload
              listType="picture-card"
              fileList={addImageList}
              beforeUpload={beforeImageUpload}
              onChange={({ fileList }) => setAddImageList(fileList)}
              onRemove={() => { setAddImageList([]); }}
              showUploadList={{ showPreviewIcon: true, showRemoveIcon: true }}
              
            >
              {addImageList.length === 0 && (
                <div>
                  <PictureOutlined />
                  <div>上传</div>
                </div>
              )}
            </Upload>
          </Form.Item>

          <Form.Item
            label="热量 (kcal/100g)"
            name="caloriesPer100g"
            rules={[{ required: true, message: "请输入热量" }]}
          >
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item
            label="蛋白质 (g/100g)"
            name="proteinPer100g"
            rules={[{ required: true, message: "请输入蛋白质" }]}
          >
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item
            label="脂肪 (g/100g)"
            name="fatPer100g"
            rules={[{ required: true, message: "请输入脂肪" }]}
          >
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item
            label="碳水 (g/100g)"
            name="carbsPer100g"
            rules={[{ required: true, message: "请输入碳水" }]}
          >
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item
            label="健康灯"
            name="healthLight"
            rules={[{ required: true, message: "请选择健康灯" }]}
          >
            <Radio.Group>
              <Radio value={0}>无</Radio>
              <Radio value={1}>绿</Radio>
              <Radio value={2}>黄</Radio>
              <Radio value={3}>红</Radio>
            </Radio.Group>
          </Form.Item>

          <Form.Item
            label="是否液体"
            name="isLiquid"
            valuePropName="checked"
            rules={[{ required: true, message: "请选择是否液体" }]}
          >
            <Switch />
          </Form.Item>

          <div style={{ textAlign: "right", marginTop: 16 }}>
            <Space>
              <Button onClick={() => setAddModalOpen(false)}>取消</Button>
              <Button type="primary" htmlType="submit" loading={addLoading}>确认添加</Button>
            </Space>
          </div>
        </Form>
      </Modal>

      {/* 删除确认 */}
      <Modal
        title="确认删除"
        open={!!deleteConfirmId}
        onOk={handleDelete}
        onCancel={() => setDeleteConfirmId(null)}
        okText="删除"
        okButtonProps={{ danger: true }}
      >
        <p>确定要删除该食物吗？此操作不可恢复。</p>
      </Modal>
    </div>
  );
}