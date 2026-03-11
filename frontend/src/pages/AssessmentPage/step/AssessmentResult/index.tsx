// src/pages/AssessmentPage/step/AssessmentResult/index.tsx
import { useLocation, useNavigate } from "react-router-dom";
import type { AssessmentResult } from "@/types/assessment";
import { Card, Progress, Button, theme } from "antd";
export default function AssessmentResult() {
  // 👇 从 location.state 中获取数据
  const location = useLocation();
  const result = location.state?.result as AssessmentResult | undefined;
  const { token } = theme.useToken();
  const navigate = useNavigate();
  // 可选：防止直接访问该页面（无数据）
  if (!result) {
    return <div>未找到测评结果，请先完成测评。</div>;
  }

  return (
    <div
      style={{
        backgroundImage: "linear-gradient(135deg, #f5f5f5 0%, #f1f1f1 100%)",
        border: "2px solid #f2efef",
      }}
    >
      <div style={{ marginTop: 40 }}>
        {/* 独立的数字输入框 */}
        <Card
          style={{
            maxWidth: 600,
            margin: "40px auto",
            backgroundColor: token.colorBgContainer,
            textAlign: "center",
          }}
          title="评测结果"
        >
          {/* 进度条 */}
          <div
            style={{
              display: "flex",
              justifyContent: "center",
              marginBottom: 32,
            }}
          >
            <Progress
              percent={100}
              showInfo={false} // 隐藏右侧的百分比数字
              strokeColor={token.colorPrimary} // 主题色（可选）
              style={{ width: "80%" }} // 控制宽度
              strokeWidth={8} // 加粗一点更美观
            />
          </div>
          <div>
            <h3 style={{ textAlign: "center" }}>为你推荐每日热量为：</h3>
          </div>

          <div
            style={{
              minHeight: 100,
              justifyContent: "center",
              marginTop: 50,
              textAlign: "center",
            }}
          >
            {result && (
              <div style={{ fontSize: 30 }}>
                <span style={{ color: "#4bc154" }}>
                  {result.recommendedCalories}
                </span>
                <span> 千卡</span>
              </div>
            )}
          </div>
          <div style={{ display: "flex", margin: "20px 0 0 0" }}>
            <Card
              style={{
                margin: "20px auto",
                minWidth: 200,
                textAlign: "center",
                border: "1px solid #c1edc5",
              }}
            >
              按推荐热量摄入，长期坚持就能达成目标！加油！
            </Card>
          </div>

          <div
            style={{ display: "flex", justifyContent: "center", marginTop: 24 }}
          >
            <Button
              onClick={() => navigate("/dashboard")}
              type="primary"
              style={{ marginBottom: 20 }}
            >
              开启健康之旅
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
