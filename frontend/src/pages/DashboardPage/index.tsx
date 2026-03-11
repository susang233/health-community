import { Row, Col, Card, Button, Spin } from "antd";
import { useHealthProfile } from "@/hooks/useHealthProfile";
import { useNavigate } from "react-router-dom";
//src/pages/DashboardPage/index.tsx
// 仪表盘内容组件
import ClickableCard from "@/components/ClickableCard";
export default function DashboardPage() {
  const navigate = useNavigate();
  const { loading } = useHealthProfile();
  if (loading) {
    return <Spin />; // 👈 数据还没回来，显示加载中
  }

  return (
    <div>
      <Row>
        <Col span={24}>
          <ClickableCard to="/dashboard/health/diet" title="饮食热量">
            <div>
              <div>还可吃 xxx千卡</div>
              <div>xxx kcal 下面标饮食</div>
              <div>xxx kcal 下面标运动*活动系数</div>
            </div>
            <div>
                可点击icon早餐
                可点击icon午餐
                可点击icon晚餐
                可点击icon加餐
                可点击icon运动
            </div>
          </ClickableCard>
        </Col>
      </Row>
      <Row>
        <Col span={24}>
          <ClickableCard to="/dashboard/health/weight" title="体重趋势">
            <div>后续放图片组件</div>
          </ClickableCard>
          <p>之后修改为动态数据</p>
        </Col>
      </Row>
      <Row gutter={[16, 8]}>
        <Col span={12}>
          <Card title="喝水">
            <p>展示今日饮水量</p>
            <p>展示水杯ui</p>
          </Card>
        </Col>
        <Col span={12}>
          <ClickableCard to="/dashboard/health/weight" title="体重趋势">
            <div>后续放图片组件</div>
          </ClickableCard>
        </Col>
        <Col span={8}>
          <Card title="快捷入口">
            <Button type="link">记录饮食</Button>
            <Button type="link">记录体重</Button>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
