import { Card, Steps, Button, theme } from "antd";
import type { StepsProps } from "antd";
import { useState } from "react";
import { ArrowLeftOutlined } from "@ant-design/icons";
import { useNavigate, Outlet ,useLocation} from "react-router-dom";

// 测评步骤标题
const stepTitles = ["性别", "身高", "体重", "生日", "运动量"];
const items: StepsProps["items"] = stepTitles.map((title, index) => ({
  title,
}));




export default function AssessmentPage() {
  const [currentStep, setCurrentStep] = useState(0);
  const navigate = useNavigate();
  const { token } = theme.useToken();
  
  // 处理返回
  const handleBack = () => {
    if (currentStep === 0) {
      navigate(-1); // 第一页返回上一页
    } else {
      setCurrentStep(currentStep - 1); // 否则返回上一题
    }
  };

  // 处理提交
  const handleSubmit = () => {
    console.log("提交测评数据");
    // TODO: 调用 API 提交测评结果
    navigate("/dashboard");
  };

  // 处理下一步
  const handleNext = () => {
    if (currentStep < items.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      // 最后一步，提交测评
      handleSubmit();
    }
  };
  

  return (
    <div>
      <Card
        title={
          <div style={{ display: "flex", alignItems: "center" }}>
            <Button
              icon={<ArrowLeftOutlined />}
              onClick={handleBack}
              type="text"
            >
              返回
            </Button>
             <div style={{ flex: 1, marginLeft: 180 }}>评测</div>
          </div>
        }
        style={{
          maxWidth: 600,
          margin: "40px auto",
          backgroundColor: token.colorBgContainer,
        }}
      >
        {/* 进度条 */}
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            marginBottom: 32,
          }}
        >
          <Steps type="inline" current={currentStep} items={items} />
        </div>

        {/* 👇 所有子页面通过 Outlet 渲染 */}
        <Outlet />

        <div
          style={{ display: "flex", justifyContent: "center", marginTop: 24 }}
        >
          <Button onClick={handleNext} type="primary">
            {currentStep < items.length - 1 ? "下一步" : "提交测评"}
          </Button>
        </div>
      </Card>
    </div>
  );
}
