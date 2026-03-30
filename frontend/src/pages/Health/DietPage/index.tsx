import type {
  DailyDietVO,
  MealRecordVO,
  FoodRecordVO,
  FoodRecordDTO,
  FoodVO,
} from "@/types/food";
import { useCallback, useEffect, useState } from "react";
import {
  getDailyDiet,
  saveFoodRecord,
  deleteFoodRecord,
  getFoodDetail,
} from "@/services/food";
import dayjs from "dayjs";
import {
  Card,
  Progress,
  DatePicker,
  theme,
  Typography,
  Space,
  List,
  Avatar,
  message,
  Input,
  Button
} from "antd";

import { SearchOutlined } from '@ant-design/icons';

import styles from "./DietPage.module.scss";
import type { DatePickerProps } from "antd";
import { RightOutlined } from "@ant-design/icons";
import FoodRecordUpsertModal from "@/components/FoodRecordUpsertModal";
import { getDefaultMealType } from "@/utils/time";
import FoodSearchModal from "@/components/FoodSearchModal";
const { Text } = Typography;
const { Search } = Input;
const MEAL_TYPE_MAP: Record<string, string> = {
  BREAKFAST: "早餐",
  LUNCH: "午餐",
  DINNER: "晚餐",
  SNACK: "加餐",
};

export default function DietPage() {
  const [dailyDiet, setDailyDiet] = useState<DailyDietVO | null>(null);
  const [currentDate, setCurrentDate] = useState(dayjs().format("YYYY-MM-DD"));
  const [editingFood, setEditingFood] = useState<FoodRecordVO | null>(null); // 控制弹窗数据
  const [loading, setLoading] = useState(false);
  const loadData = async () => {
    try {
      const res = await getDailyDiet(currentDate);
      setDailyDiet(res);
    } catch (error) {
      console.error("获取饮食记录失败", error);
    }
  };
  useEffect(() => {
    loadData();
  }, [currentDate]);

  const caloriesPercent = dailyDiet
    ? Math.round(
        (dailyDiet.actualCalories / dailyDiet.recommendedCalories) * 100,
      )
    : 0; // 0% ~ 100%
  const fatPercent = dailyDiet
    ? Math.round(
        (dailyDiet.actualIntake.fat / dailyDiet.nutritionGoal.fat) * 100,
      )
    : 0;
  const proteinPercent = dailyDiet
    ? Math.round(
        (dailyDiet.actualIntake.protein / dailyDiet.nutritionGoal.protein) *
          100,
      )
    : 0;

  const carbsPercent = dailyDiet
    ? Math.round(
        (dailyDiet.actualIntake.carbs / dailyDiet.nutritionGoal.carbs) * 100,
      )
    : 0;
  const remainingCalories = dailyDiet?.remainingCalories ?? 0;

  const isOverLimit = remainingCalories < 0;
  const { token } = theme.useToken();
  const caloriesStrokeColor =
    dailyDiet && dailyDiet.actualCalories / dailyDiet.recommendedCalories > 1
      ? "#ff4d4f"
      : token.colorPrimary;
  const onChange: DatePickerProps["onChange"] = (_, dateString) => {
    if (typeof dateString === "string") {
      setCurrentDate(dateString);
    }
  };
  const handleSaveFood = async (
    food: Omit<FoodRecordVO, "id"> & { id?: number },
  ) => {
    try {
      setLoading(true);
      const foodDTO: FoodRecordDTO = {
        id: food.id,
        foodCode: food.foodCode,
        name: food.name,
        mealType: food.mealType,
        weight: food.weight,
        recordTime: food.recordTime,
      };
      await saveFoodRecord(foodDTO);
      // 检查日期是否变更
      const originalDate = editingFood
        ? dayjs(editingFood.recordTime).format("YYYY-MM-DD")
        : null;

      const newDate = dayjs(food.recordTime).format("YYYY-MM-DD");

      if (originalDate !== newDate) {
        setCurrentDate(newDate);
      } else {
        await loadData();
      }

      message.success(food.id ? "修改成功" : "添加成功");

      // 关闭弹窗
      setEditingFood(null);
    } catch (error) {
      message.error("操作失败，请重试");
    } finally {
      setLoading(false);
    }
  };

  // 删除
  const handleDeleteFood = async (id: number) => {
    try {
      setLoading(true);
      await deleteFoodRecord(id);
      message.success("删除成功");
      await loadData(); // 刷新
      setEditingFood(null);
    } catch (error) {
      message.error("删除失败");
    } finally {
      setLoading(false);
    }
  };
  // 打开“编辑”弹窗
  const handleEditFood = useCallback((food: FoodRecordVO) => {
    setEditingFood(food);
  }, []);

  // 渲染单个餐次卡片
  const renderMealCard = (meal: MealRecordVO) => {
    const {
      type,
      actualCalories,
      foods,
      suggestedMinCalories,
      suggestedMaxCalories,
    } = meal;

    const isMainMeal = ["BREAKFAST", "LUNCH", "DINNER"].includes(type);

    // 热量颜色逻辑
    let calorieColor = "#888"; // 默认灰色（加餐/运动）
    if (isMainMeal && suggestedMinCalories && suggestedMaxCalories) {
      calorieColor =
        actualCalories > suggestedMaxCalories ? "#ff4d4f" : "#52c41a";
    }

    return (
      <Card
        key={type}
        style={{ marginBottom: 16 }}
        onClick={() => {
          // TODO: 跳转详情页或弹窗
          console.log("跳转到", type, "详情");
        }}
        hoverable
      >
        {/* 上部分 */}
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "8px 0",
          }}
        >
          <Text strong style={{ fontSize: "18px" }}>
            {MEAL_TYPE_MAP[type]}
          </Text>

          {suggestedMinCalories ? (
            <Text type="secondary">
              建议 {suggestedMinCalories} - {suggestedMaxCalories} 千卡
            </Text>
          ) : null}

          <Space>
            <Text style={{ color: calorieColor }}>
              {Math.round(actualCalories)}{" "}
              <span style={{ color: "#888" }}>千卡</span>
            </Text>
            <RightOutlined style={{ color: "#888" }} />
          </Space>
        </div>

        {/* 下部分：食物列表 */}
        {foods && foods.length > 0 ? (
          <List
            dataSource={foods}
            renderItem={(food: FoodRecordVO) => (
              <List.Item
                style={{
                  padding: "8px 0",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
                onClick={(e) => {
                  e.stopPropagation(); // 阻止冒泡到卡片
                  handleEditFood(food); // 直接调用函数
                }}
              >
                <List.Item.Meta
                  avatar={
                    <Avatar shape="square" src={food.imageUrl} size={50} />
                  }
                  title={food.name}
                  description={`${Math.round(food.weight)}${food.isLiquid ? '毫升' : '克'}`}
                  style={{ flex: 1 }}
                />
                <Text style={{ color: "#aaa" }}>
                  {Math.round(food.calories)} 千卡
                  <RightOutlined />
                </Text>
              </List.Item>
            )}
          />
        ) : (
          <Text type="secondary">暂无食物记录</Text>
        )}
      </Card>
    );
  };

  // 按顺序定义要显示的餐次（只显示存在的）
  const mealOrder = ["BREAKFAST", "LUNCH", "DINNER", "SNACK", "EXERCISE"];
  const existingMeals = dailyDiet?.meals
    .filter((meal) => mealOrder.includes(meal.type))
    .sort((a, b) => mealOrder.indexOf(a.type) - mealOrder.indexOf(b.type));



const handleSelectFood = async (basicFood: { code: string; name: string; imageUrl?: string }) => {
  try {
    // 调用详情接口获取完整营养数据
    const detail = await getFoodDetail(basicFood.code);

    // 构造完整的新记录（含 per100g 字段）
    const newRecord: Omit<FoodRecordVO, "id"> = {
      foodCode: detail.code,
      name: detail.name,
      imageUrl: detail.imageUrl,
      weight: 100,
      mealType: getDefaultMealType(),
      recordTime: dayjs().format("YYYY-MM-DDTHH:mm:ss"),
      
      // 实际摄入量（按 100g 计算）
      calories: detail.caloriesPer100g,
      protein: detail.proteinPer100g ?? 0,
      fat: detail.fatPer100g ?? 0,
      carbs: detail.carbsPer100g ?? 0,

      // 保存 per100g 基准值
      caloriesPer100g: detail.caloriesPer100g,
      proteinPer100g: detail.proteinPer100g,
      fatPer100g: detail.fatPer100g,
      carbsPer100g: detail.carbsPer100g,

      healthLight: detail.healthLight,
      isLiquid: detail.isLiquid,
    };

    setEditingFood(newRecord);
  } catch (error) {
    message.error("获取食物详情失败");
  }
};
  return (
    <div>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          margin: "20px auto",
        }}
      >
        <DatePicker
          onChange={onChange}
          needConfirm
          
          style={{ border: "1px solid #e7f1e5" }}
        />
        <Button type="primary" icon={<SearchOutlined /> }  onClick={() => setSearchModalOpen(true)}>
        搜索食物
      </Button>
      </div>
      {dailyDiet ? (
        <Card>
          <div>
            <div style={{ display: "flex", justifyContent: "center" }}>
              <Progress
                type="circle"
                percent={caloriesPercent}
                size={150}
                strokeColor={caloriesStrokeColor}
                format={() => (
                  <div style={{ textAlign: "center" }}>
                    <div style={{ fontSize: "12px", color: "#888" }}>
                      {isOverLimit ? "已超量" : "还可摄入"}
                    </div>
                    <div
                      style={{
                        fontSize: "18px",
                        fontWeight: "bold",
                        color: isOverLimit ? "#ff4d4f" : "#52c41a",
                      }}
                    >
                      {Math.abs(Math.round(remainingCalories))}
                    </div>
                    <div style={{ fontSize: "12px", color: "#888" }}>
                      推荐热量{dailyDiet.recommendedCalories}
                    </div>
                  </div>
                )}
              />
            </div>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                marginTop: 20,
              }}
            >
              <div className={styles.nutrition}>
                <div>碳水化合物</div>
                <Progress
                  percent={carbsPercent}
                  showInfo={false}
                  strokeColor={token.colorPrimary}
                  railColor="#dff0d1"
                />
                <div style={{ color: "#828782" }}>
                  {dailyDiet.actualIntake.carbs}/{dailyDiet.nutritionGoal.carbs}
                  克
                </div>
              </div>
              <div className={styles.nutrition}>
                <div>蛋白质</div>
                <Progress
                  percent={proteinPercent}
                  showInfo={false}
                  strokeColor="#e98585"
                  railColor="#f0d1d1"
                />
                <div style={{ color: "#828782" }}>
                  {dailyDiet.actualIntake.protein}/
                  {dailyDiet.nutritionGoal.protein}克
                </div>
              </div>
              <div className={styles.nutrition}>
                <div>脂肪</div>
                <Progress
                  percent={fatPercent}
                  showInfo={false}
                  strokeColor="#f7ba40"
                  railColor="#f0edd1"
                />
                <div style={{ color: "#828782" }}>
                  {dailyDiet.actualIntake.fat}/{dailyDiet.nutritionGoal.fat}克
                </div>
              </div>
            </div>
          </div>
        </Card>
      ) : (<div></div>
         )}
      <div>
        {dailyDiet ? (
          <div style={{ margin: "20px auto" }}>
            {existingMeals?.map(renderMealCard)}
          </div>
        ) : (
          <div>暂无饮食记录</div>
        )}
      </div>
      {editingFood && (
        <FoodRecordUpsertModal
          open={true}
          food={editingFood}
          onCancel={() => setEditingFood(null)}
          onConfirm={handleSaveFood}
          onDelete={handleDeleteFood}
          loading={loading}
        />
      )}

      <FoodSearchModal onSelect={handleSelectFood} />
    </div>
  );
}
