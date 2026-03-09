// src/pages/AssessmentPage/step/AssessmentResult/index.tsx
import { useLocation } from 'react-router-dom';
import type { AssessmentResult } from "@/types/assessment";

export default function AssessmentResult() {
  // 👇 从 location.state 中获取数据
  const location = useLocation();
  const result = location.state?.result as AssessmentResult | undefined;

  // 可选：防止直接访问该页面（无数据）
  if (!result) {
    return <div>未找到测评结果，请先完成测评。</div>;
  }

  return (
    <div>
      <h2>测评结果</h2>
      <p>BMI: {result.bmi}</p>
      <p>TDEE: {result.tdee} 千卡/天</p>
      {/* 渲染其他字段 */}
    </div>
  );
}