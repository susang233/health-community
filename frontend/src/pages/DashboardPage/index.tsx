import { Row, Col, Card, Button,Spin } from "antd";
import { useHealthProfile } from "@/hooks/useHealthProfile";

//src/pages/DashboardPage/index.tsx
// 仪表盘内容组件
import ClickableCard from "@/components/ClickableCard";
export default function DashboardPage() {
  const {  loading} = useHealthProfile();
   if (loading) {
    return <Spin />;  // 👈 数据还没回来，显示加载中
  }
  
  return (
    <div>
      <Row>
        <Col span={24}>
          <Card title="饮食记录">
            <p>之后修改为动态数据</p>
          </Card>
        </Col>
      </Row>
      <Row>
        <Col span={24}>
          <ClickableCard
            to="/dashboard/health/weight"
            title="体重趋势"
            
          >
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
