import { Gender } from "@/types/gender";
import { Slider, InputNumber } from "@arco-design/web-react";
import styles from './HeightStep.module.css';
import { useEffect } from "react";

interface HeightStepProps {
  value?: number;
  gender?: Gender;
  onChange: (height: number) => void;
}
export default function HeightStep({
  value,
  gender,
  onChange,
}: HeightStepProps) {
  const getDefaultHeight = () => {
    if (value !== undefined) return value; // 已有值优先
    if (gender === Gender.MALE) {
      return 170;}
    if (gender === Gender.FEMALE){ return 160;}
    return 165; // 默认
  };
  const currentValue = value ?? getDefaultHeight();
  useEffect(() => {
  if (value == null && gender != null) {
    const defaultVal = gender === Gender.MALE ? 170 : 160;
    onChange(defaultVal);
  }
}, [gender]);




  return (
    <div>
      <div>
        <h3 style={{textAlign: 'center'}}>你的身高是？</h3>
        <p style={{textAlign:'center', color: '#a29f9f'}}>精准身高数据将用于计算你的BMI</p>
      </div>
      <div style={{ marginTop: 40 }}>
        {/* 独立的数字输入框 */}

        <Slider
          className={styles.heightSlider}
          value={currentValue}
          min={100}
          max={220}
          step={5}
           marks={{
            100: '100',
            140: '140',
            170: '170',
            200: '200',
            220: '220',
          }}
          showTicks
          onChange={(val) => onChange(typeof val === 'number' ? val : val[0])}
          style={{ width: 500, margin: "0 auto" }}
        />
      </div>
      <div
        style={{
          marginTop: 20,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <InputNumber
          value={currentValue}
          min={100}
          max={220}
          step={1}
          onChange={(value) => onChange(value ?? currentValue)}
          style={{ width: "60px" }}
        />
        <span> cm</span>
      </div>
    </div>
  );
}
