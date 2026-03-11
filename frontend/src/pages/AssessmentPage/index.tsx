import { Card, Button, theme, message, Progress } from "antd";
import { useState, useEffect } from "react";
import { ArrowLeftOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";

import GenderStep from "./step/GenderStep";
import HeightStep from "./step/HeightStep";
import WeightStep from "./step/WeightStep";
import BirthdayStep from "./step/BirthdayStep";
import ActivityStep from "./step/ActivityStep";
import TargetWeightStep from "./step/TargetWeightStep";
import type { AssessmentData } from "@/types/assessment";
import { getUsername } from "@/utils/auth";
import { saveHealthProfile } from "@/services/health";
// 测评步骤标题
const TOTAL_STEPS = 6;

export default function AssessmentPage() {
  const [currentStep, setCurrentStep] = useState(0);
  const [formData, setFormData] = useState<AssessmentData>({
    username: undefined,
    gender: undefined,
    height: undefined,
    currentWeight: undefined,
    birthday: undefined,
    activityLevel: undefined,
    targetWeight: undefined,
  });
  const navigate = useNavigate();
  const { token } = theme.useToken();

  useEffect(() => {
    const username = getUsername();
    if (username) {
      setFormData((prev) => ({ ...prev, username }));
    }
  }, []);
  // 更新表单数据（供子组件调用）
  const updateFormData = (data: Partial<AssessmentData>) => {
    setFormData((prev) => ({ ...prev, ...data }));
  };
  // 处理返回
  const handleBack = () => {
    if (currentStep === 0) {
      navigate(-1); // 第一页返回上一页
    } else {
      setCurrentStep(currentStep - 1); // 否则返回上一题
    }
  };

  // 验证当前步骤的数据是否有效
  const isCurrentStepValid = () => {
    switch (currentStep) {
      case 0:
        return formData.gender !== undefined;
      case 1:
        return formData.height !== undefined && formData.height > 0;
      case 2:
        return formData.currentWeight !== undefined && formData.currentWeight > 0;
      case 3:
        return formData.birthday !== undefined;
      case 4:
        return formData.activityLevel !== undefined;
      case 5:
        return formData.targetWeight !== undefined && formData.targetWeight > 0;
      default:
        return false;
    }
  };
  // 处理下一步
  const handleNext = () => {
    if (!isCurrentStepValid()) {
      console.log('_Submitting formData:', formData); // 调试输出当前表单数据
      message.error("请填写完整信息后再继续");
      return;
    }
    if (currentStep < TOTAL_STEPS - 1) {
      console.log('_Submitting formData:', formData); // 👈 提交前打印完整数据
      setCurrentStep(currentStep + 1);
    } else {
      // 最后一步，提交测评
      handleSubmit();
    }
  };
  // 处理提交
  const handleSubmit = async () => {
    console.log("提交测评数据");
    const res = await saveHealthProfile(formData);
    if (res) {
      message.success("测评提交成功！");
      // TODO: 调用 API 提交测评结果
      navigate("/assessment/result",{state:{result:res}}); //提交成功后跳转到结果页，并传递测评结果
    } else {
      message.error("测评提交失败，请稍后重试");
    }
  };

  const percent = Math.round((currentStep / (TOTAL_STEPS )) * 100); // 0% ~ 100%

  // 渲染当前步骤
  const renderCurrentStep = () => {
    switch (currentStep) {
      case 0:
        return (
          <GenderStep
            value={formData.gender}
            onChange={(gender) => updateFormData({ gender })}
          />
        );
      case 1:
        return (
          <HeightStep
            value={formData.height}
            gender={formData.gender} // 👈 关键：传递性别影响默认值
            onChange={(height) => updateFormData({ height })}
          />
        );
      case 2:
        return (
          <WeightStep
            value={formData.currentWeight} // 在这一步要计算bmi并赋值给formData，以便下一步使用
            height={formData.height} // 👈 关键：传递身高影响默认值
            onChange={(currentWeight) => updateFormData({ currentWeight })}
          />
        );
      case 3:
        return (
          <BirthdayStep
            value={formData.birthday}
            onChange={(birthday) => updateFormData({ birthday })}
          />
        );
      case 4:
        return (
          <ActivityStep
            value={formData.activityLevel}
            onChange={(activityLevel) => updateFormData({ activityLevel })}
          />
        );
      case 5:
        return (
          <TargetWeightStep
            value={formData.targetWeight}
            height={formData.height} //传递身高计算bmi=20的值为目标体重默认值
            currentWeight={formData.currentWeight} //传递当前体重计算目标体重默认值
            onChange={(targetWeight) => updateFormData({ targetWeight })}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div
      style={{
        backgroundImage: "linear-gradient(135deg, #f5f5f5 0%, #f1f1f1 100%)",
        border: "2px solid #f2efef",
      }}
    >
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
          <Progress
            percent={percent}
            showInfo={false} // 隐藏右侧的百分比数字
            strokeColor={token.colorPrimary} // 主题色（可选）
            style={{ width: "80%" }} // 控制宽度
            strokeWidth={8} // 加粗一点更美观
          />
        </div>

        <div style={{ minHeight: 300, justifyContent: "center" }}>
          {renderCurrentStep()}
        </div>

        <div
          style={{ display: "flex", justifyContent: "center", marginTop: 24 }}
        >
          <Button
            onClick={handleNext}
            type="primary"
            style={{ marginBottom: 20 }}
          >
            {currentStep < TOTAL_STEPS - 1 ? "下一步" : "提交测评"}
          </Button>
        </div>
      </Card>
    </div>
  );
}
