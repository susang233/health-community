import { useEffect, useMemo } from "react";
import { Slider, InputNumber, Card } from "antd";
import styles from "./WeightStep.module.css";

interface WeightStepProps {
  value?: number;
  height?: number;
  onChange: (currentWeight: number) => void;
}

export default function WeightStep({
  value,
  height,
  onChange,
}: WeightStepProps) {
  // 处理 height 为 undefined 的情况
  const heightInMeters = useMemo(() => {
    return height ? height / 100 : 1.7;
  }, [height]);

  const getDefaultWeight = () => {
    if (value !== undefined) return value;

    const defaultWeight = Math.round(22 * heightInMeters ** 2);
    return defaultWeight < 25 ? 25 : defaultWeight;
  };

  const currentWeight = () => value ?? getDefaultWeight();

  const bmi = useMemo(() => {
    console.log("【计算 BMI】"); // 仅依赖变化时打印
    const weight = currentWeight();
    if (heightInMeters <= 0 || weight <= 0) return 0;
    return parseFloat((weight / heightInMeters ** 2).toFixed(1));
  }, [value, heightInMeters]); // 使用 value 和 height 作为依赖项

  // 单独处理默认值（只在 value 为 null 且 height 有效时）
  useEffect(() => {
    if (value == null && height != null) {
      const defaultWeight = getDefaultWeight();
      onChange(defaultWeight);
    }
  }, [value, height, onChange]);

  const currentWeightValue = currentWeight();

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
          value={currentWeightValue}
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
          value={currentWeightValue}
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
        <Card
          style={{
            width: 378,
            marginLeft: "auto",
            marginRight: "auto",
            marginTop: 0,
            marginBottom: 0,
          }}
          title={
            <div
              style={{ display: "flex", alignItems: "center", width: "300px" }}
            >
              你的BMI是
              <div style={{ flex: 1, marginLeft: 170 }}>
                <div> </div>
                {bmi}
                <span style={{ marginLeft: 3, color: "#57c641" }}>
                  {bmi < 18.5
                    ? "偏瘦"
                    : bmi < 24
                      ? "理想"
                      : bmi < 28
                        ? "超重"
                        : "肥胖"}
                </span>
              </div>
            </div>
          }
        >
          <Slider
            className={styles.weightSlider}
            value={parseFloat(bmi)} // 确保 slider 接收数值而非字符串
            min={8}
            max={35}
            disabled
            marks={{
              8: "偏瘦",
              18.5: "理想",
              23.9: "超重",
              27.9: "肥胖",
            }}
            style={{ margin: "0 auto", width: 300 }}
          />
          <div style={{ marginTop: 50 }}>
            <p>
              {bmi < 18.5
                ? "偏瘦，可将管理目标调整为塑形，提升身体素质。"
                : bmi < 24
                  ? "标准体重，进阶管理需注意提升代谢水平，可采取16:8饮食法，配合运动。"
                  : bmi < 28
                    ? "超重了，稍微努力一下就能拥有完美身材。加油！我们陪你一起努力！"
                    : "肥胖，首要任务是改善代谢功能，控制糖分和热量，健康变瘦。"}
            </p>
          </div>
        </Card>
      </div>
      <div
        style={{
          width: 300,
          display: "flex",
          justifyContent: "center",
          margin: "0 auto",
        }}
      >
        <p style={{ color: "#a29f9f", textAlign: "center" }}>
          注：BMI是目前诊断肥胖最广泛使用的方法和指标； BMI计算公式：体重（kg）/
          [身高（m）*身高（m）]
        </p>
      </div>
    </div>
  );
}
