// GenderStep.tsx
import { Gender } from '@/types/gender';
import maleIcon from '@/assets/icons/male.png';     // 替换为你的图标路径
import femaleIcon from '@/assets/icons/female.png';

interface Props {
  value?: Gender;
  onChange: (gender: Gender) => void;
}

export default function GenderStep({ value, onChange }: Props) {
  return (
    <div>
      <div>
        <h3 style={{ textAlign: 'center' }}>你的性别是？</h3>
      </div>
      <div style={{ display: 'flex', gap: '24px', justifyContent: 'center' ,marginTop: '60px'}}>
        

        <div
          onClick={() => onChange(Gender.FEMALE)}
          style={{
            width: 100,
            height: 100,
            borderRadius: '50%',
            border: value === Gender.FEMALE ? '2px solid #8ee38b' : '2px solid #ffffff',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            background: '#fff',
            padding: '10px',
          }}
        >
          <img src={femaleIcon} alt="女" style={{ width: 90, height: 95 }} />
          <div style={{ textAlign: 'center'}}>女</div>
        </div>
        <div
          onClick={() => onChange(Gender.MALE)}
          style={{
            width: 100,
            height: 100,
            borderRadius: '50%',
            border: value === Gender.MALE ? '2px solid #8ee38b' : '2px solid #ffffff',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            background: '#fff',
            padding: '10px',
          }}
        >
          <img src={maleIcon} alt="男" style={{ width: 80, height: 90 }} />
          <div style={{ textAlign: 'center' }}>男</div>
        </div>
      </div>
    </div>
  );
}