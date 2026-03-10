import { useEffect } from "react";
import { Slider, InputNumber, Card } from "antd";
import styles from "./WeightStep.module.css";

interface WeightStepProps {
  value?: number;
  height?: number;
  onChange: (weight: number) => void;
}
export default function WeightStep({
  value,
  height,
  onChange,
}: WeightStepProps) {
  const getBmi = (weight: number, height: number) => {
    const heightInMeters = height / 100;
    return parseFloat((weight / (heightInMeters * heightInMeters)).toFixed(1));
  };

  const getDefaultWeight = () => {
    if (value !== undefined) return value; // 已有值优先

    return Math.round(22 * (height / 100) ** 2) < 25
      ? 25
      : Math.round(22 * (height / 100) ** 2); // 根据身高计算默认体重，默认BMI为22
  };

  const currentValue = value ?? getDefaultWeight();

  // 自动设置默认值（如果父组件没传）
  useEffect(() => {
    if (value == null) {
      onChange(getDefaultWeight());
    }
  }, []);

  return (
    <div>
      <div>
        <h3 style={{ textAlign: "center" }}>你的体重是？</h3>
        <p style={{ textAlign: "center", color: "#a29f9f" }}>
          精准体重数据将用于计算你的BMI
        </p>
      </div>
      <div style={{ marginTop: 40 }}>
        <Slider
          className={styles.weightSlider}
          value={currentValue}
          min={25.0}
          max={200.0}
          step={5}
          dots={true}
          marks={{
            25: "25",

            45: "45",
            60: "60",
            80: "80",
            100: "100",

            150: "150",
            200: "200",
          }}
          onChange={(val) => onChange(typeof val === "number" ? val : val[0])}
          style={{ width: 500, margin: "0 auto" }}
        />
      </div>
      <div
        style={{
          marginTop: 30,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <InputNumber
          value={currentValue}
          min={25.0}
          max={200.0}
          step={1}
          precision={1}
          controls={false}
          suffix="kg"
          onChange={(value) => onChange(value ?? currentValue)}
          style={{ width: "80px" }}
        />
      </div>
      <div style={{ display: "flex", margin: "20px 0 0 0" }}>
        <Card
          style={{
            width: 360,
            marginLeft: "auto",
            marginRight: "auto",
            // 显式设置上下 margin 为 0，避免隐式冲突
            marginTop: 0,
            marginBottom: 0,
          }}
          title={
            <div
              style={{ display: "flex", alignItems: "center", width: "300px" }}
            >
              你的BMI是
              <div style={{ flex: 1, marginLeft: 180 }}>
                {getBmi(currentValue, height)}
              </div>
            </div>
          }
          
        >

           <Slider
          className={styles.weightSlider}
          value={getBmi(currentValue, height)}
          min={8}
          max={35}
          
          disabled
          marks={{
            

            18.5: "偏瘦",
            23.9: "正常",
            27.9: "超重",
            35.0: "肥胖"
          }}
         
          style={{  margin: "0 auto" }}
        />
     
        </Card>
      </div>
    </div>
  );
         
  
}
