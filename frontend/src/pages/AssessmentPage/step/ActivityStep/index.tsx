// src/pages/AssessmentPage/steps/ActivityStep.tsx
import { Radio } from "antd";
import { ActivityLevel, ActivityLevelLabel } from "@/types/activityLevel";

interface Props {
  value?: ActivityLevel;
  onChange: (level: ActivityLevel) => void;
}

export default function ActivityStep({ value, onChange }: Props) {
  return (
    <div>
      <div>
        <h3 style={{ textAlign: "center" }}>你平时的运动量是？</h3>
        <p style={{ textAlign: "center", color: "#a29f9f" }}>
          运动量是组成基础代谢水平密不可分的一部分
        </p>
      </div>

      <Radio.Group
        value={value}
        onChange={(e) => onChange(e.target.value)}
        style={{display: "flex", justifyContent: "center",alignItems: "center",}}
         vertical
         
      >
        <Radio.Button value={ActivityLevel.SEDENTARY} style={{height:55,textAlign:"center",width:160}}>
          {ActivityLevelLabel[ActivityLevel.SEDENTARY]}
         <div style={{fontSize: 12, color: "#a29f9f" }}>久坐不动 </div>
        </Radio.Button>
        <Radio.Button value={ActivityLevel.LIGHT}  style={{height:55,textAlign:"center",width:160}}>
          {ActivityLevelLabel[ActivityLevel.LIGHT]}
          
           <div style={{fontSize: 12, color: "#a29f9f" }}>每周小于3次运动 </div>
        </Radio.Button>
        <Radio.Button value={ActivityLevel.MODERATE} style={{height:55,textAlign:"center",width:160}}>
          {ActivityLevelLabel[ActivityLevel.MODERATE]}
          <div style={{fontSize: 12, color: "#a29f9f" }}>每周3-5次运动 </div>
        </Radio.Button>
        <Radio.Button value={ActivityLevel.HEAVY} style={{height:55,textAlign:"center",width:160}}>
          {ActivityLevelLabel[ActivityLevel.HEAVY]}
          <div style={{fontSize: 12, color: "#a29f9f" }}>每周6-7次运动 </div>
        </Radio.Button>
        <Radio.Button value={ActivityLevel.EXTREME} style={{height:55,textAlign:"center",width:160}}>
          {ActivityLevelLabel[ActivityLevel.EXTREME]}
         
           <div style={{fontSize: 12, color: "#a29f9f" }}>运动员/体力劳动工作者</div>
        </Radio.Button>
      </Radio.Group>
    </div>
  );
}
