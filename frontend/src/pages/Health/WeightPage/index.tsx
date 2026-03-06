import { Row, Col, Card, Button } from 'antd';
//src/pages/DashboardPage/index.tsx
// 仪表盘内容组件
export default function WeightPage() {
  return (
    <div>
      <h2>欢迎回来！</h2>
      <Row gutter={16}>
        <Col span={8}>
          <Card title="今日进度">
            <p>饮食: 1200/2000 kcal</p>
            <p>喝水: 1.5/2.5 L</p>
          </Card>
        </Col>
        <Col span={8}>
       <Card title="体重趋势">
            {/* 放图表组件 */}
          </Card>
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