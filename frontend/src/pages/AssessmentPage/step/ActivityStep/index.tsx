// src/pages/AssessmentPage/steps/ActivityStep.tsx
import { Radio } from 'antd';
import { ActivityLevel, ActivityLevelLabel } from '@/types/activityLevel';

interface Props {
  value?: ActivityLevel;
  onChange: (level: ActivityLevel) => void;
}

export default function ActivityStep({ value, onChange }: Props) {
  return (
    <div>
      <h3>请选择您的日常运动量</h3>
      <Radio.Group
        value={value}
        onChange={(e) => onChange(e.target.value)}
        style={{ width: '100%' }}
      >
        <Radio.Button value={ActivityLevel.SEDENTARY}>
          {ActivityLevelLabel[ActivityLevel.SEDENTARY]}
        </Radio.Button>
        <Radio.Button value={ActivityLevel.LIGHT}>
          {ActivityLevelLabel[ActivityLevel.LIGHT]}
        </Radio.Button>
        <Radio.Button value={ActivityLevel.MODERATE}>
          {ActivityLevelLabel[ActivityLevel.MODERATE]}
        </Radio.Button>
        <Radio.Button value={ActivityLevel.HEAVY}>
          {ActivityLevelLabel[ActivityLevel.HEAVY]}
        </Radio.Button>
        <Radio.Button value={ActivityLevel.EXTREME}>
          {ActivityLevelLabel[ActivityLevel.EXTREME]}
        </Radio.Button>
      </Radio.Group>
    </div>
  );
}