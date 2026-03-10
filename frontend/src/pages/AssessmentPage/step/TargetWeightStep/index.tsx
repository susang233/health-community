import { useEffect, useMemo } from "react";
import { Slider, InputNumber, Card } from "antd";
import styles from "./TargetWeightStep.module.css";
interface TargetWeightStepProps {
  value?: number;
  height?: number;
  currentWeight?: number;
  onChange: (targetWeight: number) => void;
}

export default function TargetWeightStep({ value, height, currentWeight, onChange }: TargetWeightStepProps) {

 const heightInMeters = useMemo(() => {
    return height ? height / 100 : 1.7;
  }, [height]);

  const getDefaultTargetWeight = () => {
    if (value !== undefined) return value;

    const defaultTargetWeight = Math.round(20 * heightInMeters ** 2);// 以 BMI=20 作为目标体重的默认值
    
    return defaultTargetWeight < 25 ? 25 : defaultTargetWeight;
  };

  const currentTargetWeight = () => value ?? getDefaultTargetWeight();

    useEffect(() => {
      if (value == null && height != null) {
        const defaultTargetWeight = getDefaultTargetWeight();
        onChange(defaultTargetWeight);
      }
    }, [value, height, onChange]);
  
    const currentTargetWeightValue = currentTargetWeight();

  return (
     <div>
      <div>
        <h3 style={{ textAlign: "center" }}>你的目标体重是？</h3>
        
      </div>
      <div style={{ marginTop: 40 }}>
        <Slider
          className={styles.weightSlider}
          value={currentTargetWeightValue}
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
          onChange={(val) => onChange(val)}
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
          value={currentTargetWeightValue}
          min={25.0}
          max={200.0}
          step={1}
          precision={1}
          controls={false}
          suffix="kg"
          onChange={(value) => onChange(value)}
          style={{ width: "80px" }}
        />
      </div>
      <div style={{ display: "flex", margin: "20px 0 0 0" }}>
        
{currentTargetWeightValue < currentWeight ? (
          <Card style={{ margin: "20px auto", width: 400, textAlign: "center", color: "#66ca63" }}>
            将减重 {Math.round(((currentWeight - currentTargetWeightValue) / currentWeight) * 100)}%！相信你可以哦，我们陪你一起加油！
          </Card>
        ) : null}
      </div>
      
    </div>
  );
}