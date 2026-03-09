import { Radio } from 'antd';
import { Gender } from '@/types/gender';

interface Props {
  value?: Gender;
  onChange: (gender: Gender) => void;
}

export default function GenderStep({ value, onChange }: Props) {
  return (
    <Radio.Group value={value} onChange={(e) => onChange(e.target.value)}>
      <Radio value={Gender.MALE}>男</Radio>
      <Radio value={Gender.FEMALE}>女</Radio>
    </Radio.Group>
  );
}