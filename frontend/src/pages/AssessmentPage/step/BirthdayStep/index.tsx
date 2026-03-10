import { useEffect, useState } from "react";
import { DatePicker } from "antd-mobile";
import { Button } from "antd";
interface BirthdayStepProps {
  value?: string;

  onChange: (birthday: string) => void;
}
export default function BirthdayStep({ value, onChange }: BirthdayStepProps) {
  const getDefaultBirthday = () => {
    if (value !== undefined) return value; // 已有值优先
    const today = new Date();
    const defaultYear = today.getFullYear() - 22; // 默认年龄22岁

    return `${defaultYear}-01-01`; // 默认生日22年前的1月1日
  };
  const currentValue = value ? new Date(value) : new Date(getDefaultBirthday());

  useEffect(() => {
    if (value == null) {
      const defaultVal = getDefaultBirthday();
      onChange(defaultVal);
    }
  }, [value, onChange]); // ✅ 安全：value 从 null → 非 null 后不再触发
  const [visible, setVisible] = useState(false);
  // 在 BirthdayStep 组件内
  const getAge = (date: Date) => {
    const today = new Date();
    let age = today.getFullYear() - date.getFullYear();
    const m = today.getMonth() - date.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < date.getDate())) {
      age--;
    }
    return age;
  };

  const age = getAge(currentValue);
  return (
    <div>
      <div>
        <h3 style={{ textAlign: "center" }}>你的出生日期是？</h3>
        <p style={{ textAlign: "center", color: "#a29f9f" }}>
          年龄与基础代谢水平、身体活动水平息息相关
        </p>
      </div>
      <div style={{ display: "flex", justifyContent: "center", marginTop: 50 }}>
        <Button
          type="text"
          onClick={() => {
            setVisible(true);
          }}
          style={{ width: 300, height: 100, fontSize: 30 }}
        >
          {currentValue ? currentValue.toLocaleDateString() : "请选择出生日期"}
        </Button>
        <DatePicker
          
          defaultValue={currentValue}
          visible={visible}
          onClose={() => {
            setVisible(false);
          }}
          min={new Date(new Date().getFullYear() - 80, 0, 1)} //最大80岁
          max={new Date(new Date().getFullYear() - 14, 11, 31)} // 12月31日 //最小14岁
          onConfirm={(date) => {
            const y = date.getFullYear();
            const m = String(date.getMonth() + 1).padStart(2, "0");
            const d = String(date.getDate()).padStart(2, "0");
            onChange(`${y}-${m}-${d}`);
          }}
          mouseWheel={true}
        />
      </div>
      <div
        style={{
          marginTop: 20,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          fontSize: 24,
          color: "#66ca63",
        }}
      >
        {age} 岁
      </div>
      <div
        style={{
          width: 300,

          margin: "20px auto",
          color: "#a29f9f",
        }}
      >
        {age < 49
          ? "基础代谢高，身体活动水平高，拥有体重管理的先天优势！"
          : "基础代谢、身体活动水平均有所降低。别担心，科学的体重管理方案也能帮你达成目标！"}
      </div>
    </div>
  );
}
