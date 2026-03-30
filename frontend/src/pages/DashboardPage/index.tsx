// src/pages/DashboardPage/index.tsx
import { Row, Col, Card, Button, Spin } from "antd";
import { useHealthProfile } from "@/hooks/useHealthProfile";
import ClickableCard from "@/components/ClickableCard";
import { useEffect, useState } from "react";
import { getDailySummary } from "@/services/home";
import type { DailySummaryResult } from "@/types/food";
import styles from "./DashboardPage.module.scss";

const DietCard = ({ hasProfile }: { hasProfile: boolean }) => {
  const [dailySummary, setDailySummary] = useState<DailySummaryResult | null>(
    null,
  );
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!hasProfile) return;

    const fetchSummary = async () => {
      try {
        setLoading(true);
        const res = await getDailySummary();
        setDailySummary(res);
      } catch (error) {
        console.error("获取饮食摘要失败", error);
        // 可选：message.error("加载失败，请重试");
      } finally {
        setLoading(false);
      }
    };

    fetchSummary();
  }, [hasProfile]);

  if (loading) {
    return (
      <ClickableCard to="/dashboard/health/diet" title="饮食热量">
        <div style={{ textAlign: "center", padding: "16px" }}>
          <Spin size="small" />
        </div>
      </ClickableCard>
    );
  }

  if (!dailySummary) {
    return (
      <ClickableCard to="/dashboard/health/diet" title="饮食热量">
        <div>暂无今日数据</div>
      </ClickableCard>
    );
  }

  return (
    <ClickableCard to="/dashboard/health/diet" title="饮食热量">
      <div style={{display:'flex',justifyContent: "space-between"}}>
        <div style={{justifyContent:"center",marginLeft: 50}}>
          <div style={{color: "#828782"}}>还可吃</div>
          <div >
            <span style={{ fontSize: "18px", fontWeight: "bold" }}>{dailySummary.remainingCalories}</span>
            <span style={{color: "#828782"}}> 千卡</span>
          </div>
        </div>
        <div>
            <div style={{display:'flex',marginTop: 8, gap: 28}}>
               
                <div className={styles.nutritionCard} style={{backgroundColor: "#acdbbb"}}>
                    <div>
                        碳水(克)
                    </div>
                    
                    <div style={{textAlign: "center"}}>
                        {dailySummary.actualIntake.carbs}/{dailySummary.nutritionGoal.carbs}
                    </div>

                </div>
                 <div className={styles.nutritionCard} style={{backgroundColor: "#e9c6c6"}}>
                    <div>
                        蛋白质(克)
                    </div>
                    
                    <div style={{textAlign: "center"}}>
                        {dailySummary.actualIntake.protein}/{dailySummary.nutritionGoal.protein}
                    </div>

                </div>
                 <div className={styles.nutritionCard} style={{backgroundColor: "#e2dfc4"}}>
                    <div>
                        脂肪(克)
                    </div>
                    
                    <div style={{textAlign: "center"}}>
                        {dailySummary.actualIntake.fat}/{dailySummary.nutritionGoal.fat}
                    </div>

                </div>


            </div>
        </div>
        
      </div>
      
    </ClickableCard>
  );
};


export default function DashboardPage() {
  const { loading: profileLoading, hasProfile } = useHealthProfile();

  // 如果健康档案还在加载，整个页面显示 Spin
  if (profileLoading) {
    return (
      <div style={{ padding: "24px", textAlign: "center" }}>
        <Spin tip="加载中..." />
      </div>
    );
  }

  return (
    <div style={{ padding: "0 16px" , maxWidth: 1000,margin: "40px auto"}}>
      {/* 第一行：饮食热量 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col span={24}>
          <DietCard hasProfile={hasProfile} />
        </Col>
      </Row>

      {/* 第二行：体重趋势 + 饮水 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <ClickableCard to="/dashboard/health/weight" title="体重趋势">
            <div>后续放图表组件</div>
          </ClickableCard>
        </Col>
        <Col xs={24} md={12}>
          <Card title="喝水" bordered={false}>
            <p>展示今日饮水量</p>
            <p>水杯 UI 占位</p>
          </Card>
        </Col>
      </Row>

      {/* 快捷入口 */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="快捷入口" bordered={false}>
            <Button
              type="link"
              onClick={() => {
                /* 跳转记录页 */
              }}
            >
              记录饮食
            </Button>
            <Button type="link">记录体重</Button>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
