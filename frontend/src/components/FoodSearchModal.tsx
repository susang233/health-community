// src/components/diet/FoodSearchModal.tsx
import { Modal, Input, List, Avatar, Button, Spin, message } from "antd";
import { SearchOutlined, LoadingOutlined } from "@ant-design/icons";
import { useState, useEffect, useCallback } from "react";
import type { FoodVO } from "@/types/food";
import { searchFoods } from "@/services/food";
import { getHealthLightColor } from "@/utils/getHealthLightColor";

interface FoodSearchModalProps {
  open: boolean;
  onCancel: () => void;
  onSelect: (food: FoodVO) => void; //  // 可选：如果需要直接在弹窗内显示食物详情，可以添加一个 food 属性
}

export default function FoodSearchModal({
  open,
  onCancel,
  onSelect,
}: FoodSearchModalProps) {
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<FoodVO[]>([]);
  const [loading, setLoading] = useState(false);

  const [hasOpened, setHasOpened] = useState(false);

useEffect(() => {
  if (open && !hasOpened) {
    setKeyword("");
    setResults([]);
    setHasOpened(true);
  }
}, [open, hasOpened]);



  const handleSearch = useCallback(async () => {
    if (!keyword.trim()) return;

    try {
      setLoading(true);
      const res = await searchFoods(keyword.trim(), 1);
      setResults(res.foods || []);
    } catch (error) {
      message.error("搜索失败，请重试");
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, [keyword]);

  const handleSelect = (food: FoodVO) => {
    onSelect(food); // 通知父组件
   
  };

  return (
    <Modal
      title="搜索食物"
      open={open}
      onCancel={onCancel}
      footer={null}
      width={600}
      destroyOnClose // 关闭时销毁，避免残留状态
    >
      <div style={{ display: "flex", marginBottom: 16, gap: 12 }}>
        <Input
          placeholder="请输入食物名称（如：苹果、米饭）"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onPressEnter={(e) => {
            e.preventDefault();
            handleSearch();
          }}
          suffix={loading ? <LoadingOutlined /> : null}
          style={{ width: "80%" }}
        />
        <Button
          type="primary"
          icon={<SearchOutlined />}
          onClick={handleSearch}
          loading={loading}
          block
          style={{ width: "20%" }}
        >
          搜索
        </Button>
      </div>

      <div style={{ marginTop: 16, maxHeight: 400, overflowY: "auto" }}>
        {loading ? (
          <Spin
            tip="搜索中..."
            style={{ display: "block", margin: "20px auto" }}
          />
        ) : results.length > 0 ? (
          <List
            dataSource={results}
            renderItem={(food) => (
              <List.Item
                style={{
                  cursor: "pointer",
                  padding: "12px 0",
                  margin: "auto 15px",
                }}
                onClick={() => handleSelect(food)}
              >
                <List.Item.Meta
                  avatar={
                    <Avatar
                      src={food.imageUrl}
                      shape="square"
                      size={50}
                      fallback="?"
                    />
                  }
                  title={food.name}
                  description={`${food.caloriesPer100g?.toFixed(1) ?? 0} 千卡 / 100${food.isLiquid ? "毫升" : "克"}`}
                style={{width:"80%"}}
                />
                <div style={{width:"20%",display:"flex",justifyContent:"center",alignItems:"center"}}>
                <div
                  style={{
                    width: 8,
                    height: 8,
                    borderRadius: "50%",
                    backgroundColor: getHealthLightColor(food.healthLight ?? 0),
                    border: "1px solid #eee",
                  }}
                /></div>
              </List.Item>
            )}
          />
        ) : keyword ? (
          <div style={{ textAlign: "center", color: "#888", padding: 20 }}>
            未找到 “{keyword}” 相关食物
          </div>
        ) : null}
      </div>
    </Modal>
  );
}
