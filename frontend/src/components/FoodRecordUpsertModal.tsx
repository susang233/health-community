// src/components/diet/FoodUpsertModal.tsx
import {
  Modal,
  Form,
  InputNumber,
  Button,
  Space,
  Popconfirm,
  DatePicker,
  Select,
  TimePicker,
  Typography,
  Card,
} from "antd";
import { type FoodRecordVO } from "@/types/food";
import {
  parseRecordTime,
  buildRecordTime,
  getDefaultMealType,
} from "@/utils/time";
import { useState } from "react";

const { Text } = Typography;

const getHealthLightColor = (level: number): string => {
  switch (level) {
    case 1:
      return "#52c41a";
    case 2:
      return "#faad14";
    case 3:
      return "#ff4d4f";
    default:
      return "#d9d9d9";
  }
};
interface FoodRecordUpsertModalProps {
  open: boolean;
  food: Omit<FoodRecordVO, "id"> & { id?: number };
  onCancel: () => void;
  onConfirm: (food: Omit<FoodRecordVO, "id"> & { id?: number }) => void;
  onDelete?: (id: number) => void;
  loading?: boolean;
}

export default function FoodRecordUpsertModal({
  open,
  food,
  onCancel,
  onConfirm,
  onDelete,
  loading = false,
}: FoodRecordUpsertModalProps) {
  const [form] = Form.useForm();
  const isEditMode = !!food.id;

  // 拆分 recordTime 为 date 和 time
  const { date: initialDate, time: initialTime } = parseRecordTime(
    food.recordTime,
  );

  // 初始化所有字段（包括不可变字段如 name、imageUrl 等）
  const initialValues = {
    ...food, // 保留所有原始字段（name, caloriesPer100g 等）
    mealType: food.mealType || getDefaultMealType(),
    weight: food.weight ?? 100,
    recordDate: initialDate,
    eatTime: initialTime,
  };

  const initialWeight = food.weight ?? 100;
  const factor = initialWeight / 100;
  const [displayValues, setDisplayValues] = useState({
    calories: Number((food.caloriesPer100g * factor).toFixed(1)),
    protein: Number(((food.proteinPer100g ?? 0) * factor).toFixed(1)),
    fat: Number(((food.fatPer100g ?? 0) * factor).toFixed(1)),
    carbs: Number(((food.carbsPer100g ?? 0) * factor).toFixed(1)),
  });
  const handleOpenChange = (newOpen: boolean) => {
    if (!newOpen) {
      form.resetFields();
      onCancel();
    }
  };

  // 提交函数
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields(); // 获取表单值：{ mealType, weight, recordDate, eatTime }

      // 合成 recordTime
      const newRecordTime = buildRecordTime(values.recordDate, values.eatTime);
      if (!newRecordTime) {
        console.error("无法构建 recordTime");
        return;
      }

      // 合并：保留原始 food 的不可变字段 + 更新可变字段
      const finalFood: Omit<FoodRecordVO, "id"> & { id?: number } = {
        ...food, // 包含 name, imageUrl, foodCode, caloriesPer100g 等
        mealType: values.mealType,
        weight: values.weight,
        recordTime: newRecordTime, // 替换为新合成的时间
      };

      onConfirm(finalFood); // 传回完整对象
    } catch (error) {
      console.error("表单验证失败", error);
    }
  };
  // 重量变化时实时计算营养值
  const handleWeightChange = (weight: number | null) => {
    if (weight == null || weight <= 0) return;

    const factor = weight / 100;
    const newValues = {
      calories: Number((food.caloriesPer100g * factor).toFixed(1)),
      protein: Number(((food.proteinPer100g ?? 0) * factor).toFixed(1)),
      fat: Number(((food.fatPer100g ?? 0) * factor).toFixed(1)),
      carbs: Number(((food.carbsPer100g ?? 0) * factor).toFixed(1)),
    };

    setDisplayValues(newValues);
    form.setFieldsValue(newValues); // 同步到表单（用于提交）
  };

  return (
    <Modal
      title={
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            margin: "auto 25px",
          }}
        >
          <span>{isEditMode ? "编辑饮食记录" : "添加饮食记录"}</span>
          {isEditMode && onDelete && (
            <Popconfirm
              title="确定删除该食物记录？"
              onConfirm={() => onDelete!(food.id!)}
              okText="删除"
              cancelText="取消"
              okButtonProps={{ danger: true }}
            >
              <Button danger size="small" type="text">
                删除
              </Button>
            </Popconfirm>
          )}
        </div>
      }
      open={open}
      onCancel={() => handleOpenChange(false)}
      footer={[
        <Button key="cancel" onClick={() => handleOpenChange(false)}>
          取消
        </Button>,
        <Button
          key="submit"
          type="primary"
          onClick={handleSubmit}
          loading={loading}
        >
          {isEditMode ? "保存" : "添加"}
        </Button>,
      ]}
    >
      <Form form={form} initialValues={initialValues} layout="vertical">
        {/* 顶部 */}
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            marginBottom: 16,
          }}
        >
          <Space>
            <Form.Item name="recordDate" noStyle>
              <DatePicker />
            </Form.Item>

            <Form.Item name="mealType" noStyle>
              <Select
                style={{ width: 100 }}
                options={[
                  { value: "BREAKFAST", label: "早餐" },
                  { value: "LUNCH", label: "午餐" },
                  { value: "DINNER", label: "晚餐" },
                  { value: "SNACK", label: "加餐" },
                ]}
              />
            </Form.Item>
          </Space>

          <Form.Item name="eatTime" noStyle>
            <TimePicker placeholder="用餐时间" format="HH:mm" />
          </Form.Item>
        </div>
        <Card size="small" style={{ marginBottom: 16 }}>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: 20,
              margin: "auto 20px",
            }}
          >
            {food.imageUrl && (
              <img
                src={food.imageUrl}
                alt={food.name}
                style={{
                  width: 60,
                  height: 60,
                  objectFit: "cover",
                  borderRadius: 16,
                }}
              />
            )}
            <Text strong style={{ fontSize: 20, flex: 1 }}>
              {food.name}
            </Text>
            <div
              style={{
                width: 12,
                height: 12,
                borderRadius: "50%",
                backgroundColor: getHealthLightColor(food.healthLight ?? 0),
                border: "1px solid #eee",
              }}
            />
          </div>
          <div
            style={{
              marginTop: 12,
              display: "flex",
              justifyContent: "space-between",
              margin: "auto 20px",
            }}
          >
            <Text style={{ fontSize: 18, fontWeight: "bold",marginTop:8 }}>
              {displayValues.calories.toFixed(1)} 千卡
            </Text>
            <div style={{ textAlign: "right", fontSize: 14 }}>
              {[
                { label: "碳水", value: displayValues.carbs, color: "#52c41a" }, // 绿色
                {
                  label: "蛋白质",
                  value: displayValues.protein,
                  color: "#ef494b",
                }, // 红色
                { label: "脂肪", value: displayValues.fat, color: "#faad14" }, // 黄色
              ].map((item, index) => (
                <div
                  key={index}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    width: "100%",
                    alignItems: "center", // 确保内容垂直居中对齐
                  }}
                >
                  <span
                    style={{
                      display: "flex",
                      alignItems: "center",
                      minWidth: 40,
                    }}
                  >
                    <span
                      style={{
                        width: 8,
                        height: 8,
                        borderRadius: "50%",
                        backgroundColor: item.color,
                        marginRight: 4, // 调整圆点与文字之间的间距
                      }}
                    />
                    {item.label}：
                  </span>
                  <span style={{ whiteSpace: "nowrap", textAlign: "right" }}>
                    {item.value.toFixed(1)}g
                  </span>
                </div>
              ))}
            </div>
          </div>
        </Card>

        {/* 重量 */}
        <Form.Item
          name="weight"
          label="食用份量"
          rules={[{ required: true, message: "请输入重量" }]}
          style={{display:"flex",justifyContent:"center"}}
        >
          <InputNumber
            suffix={food.isLiquid ? "毫升" : "克"}
            min={1}
            style={{ width: "100px" }}
            variant="underlined"
            onChange={handleWeightChange}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
