import { Card, Button } from "antd";
import type { CardProps } from "antd";
import { useNavigate } from "react-router-dom";
import { useHealthProfile } from "@/hooks/useHealthProfile";

interface ClickableCardProps extends CardProps {
  to: string;
   requireProfile?: boolean; // 是否检查健康档案，默认为 true
  placeholder?: React.ReactNode; // 自定义未完善时的占位内容
}

export default function ClickableCard({ to,  requireProfile = true,
  placeholder,
  children,...props }: ClickableCardProps) {
  const navigate = useNavigate();
  const { hasProfile, requireProfile: showProfileModal  } = useHealthProfile();
const handleCardClick = () => {
    if (!requireProfile || hasProfile) {
      // 不需要检查 或 已有档案 → 直接跳转
      navigate(to);
    } else {
      // 需要检查但没有档案 → 弹出引导
      showProfileModal(() => navigate(to));
    }
  };

   // 处理“去完善”按钮点击
  const handleExtraClick = (e: React.MouseEvent) => {
    e.stopPropagation(); // 阻止事件冒泡到卡片
    showProfileModal(() => navigate(to));
  };


  
   // 根据是否有档案决定卡片内容
  const renderContent = () => {
    // 需要检查且没有档案 → 显示占位内容
    if (requireProfile && !hasProfile) {
      return placeholder || (
        <div style={{ color: "#999", textAlign: "center", padding: "20px" }}>
          完善健康档案后可见
        </div>
      );
    }
    
    // 有档案 → 显示传入的子组件
    return children;
  };

  // 判断是否需要显示“去完善”按钮
  const shouldShowExtra = requireProfile && !hasProfile;

  return (
    <Card
      onClick={handleCardClick}
      {...props}
      hoverable
      style={{ cursor: 'pointer', ...props.style }}
      extra={
       shouldShowExtra && (
          <Button type="link" onClick={handleExtraClick}>
            去完善
          </Button>
        )
      }
    >
       {renderContent()}
    </Card>
  );
}