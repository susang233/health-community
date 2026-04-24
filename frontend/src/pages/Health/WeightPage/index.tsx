import { DatePicker, Button, Card, message, Spin, Typography } from "antd";
import { Line } from "@ant-design/charts";
import { useEffect, useState } from "react";
import dayjs from "dayjs";

import type { WeightRecord } from '@/types/health';
import { getEarliestWeight, getWeightHistory, recordWeight,getLatestWeight } from "@/services/health";

const { RangePicker } = DatePicker;
const { Title, Text } = Typography;



export default function WeightRecordPage() {
  const [weightData, setWeightData] = useState<WeightRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
    dayjs().subtract(6, 'day'), // 最近7天（包含今天）
    dayjs()
  ]);
   const [earliestRecordDate, setEarliestRecordDate] = useState<dayjs.Dayjs | null>(null);

  const [latestWeightRecord, setLatestWeightRecord] = useState<WeightRecord | null>(null);
  // 加载体重数据
  const loadWeightData = async (start: string, end: string) => {
    setLoading(true);
    try {
      const response = await getWeightHistory(start, end);
      setWeightData(response || []);
    } catch (error) {
      message.error("加载体重数据失败");
      setWeightData([]);
    } finally {
      setLoading(false);
    }
  };
 const loadEarliestWeightDate = async () => {
    const earliest = await getEarliestWeight();
    const earliestDateStr=earliest ? earliest.date : null;
    if (earliestDateStr) {
      setEarliestRecordDate(dayjs(earliestDateStr));
    }
  };
  // 处理日期选择变化
  const handleDateChange = (dates: any, dateStrings: [string, string]) => {
    if (dates && dates.length === 2) {
      setDateRange([dates[0], dates[1]]);
      loadWeightData(dateStrings[0], dateStrings[1]);
    }
  };

  // 记录今日体重
  const handleRecordWeight = async () => {
    const today = dayjs().format('YYYY-MM-DD');
    const currentWeight = prompt("请输入今日体重（kg）：");
    
    if (currentWeight === null) return; // 用户取消
    
    const weightNum = parseFloat(currentWeight);
    if (isNaN(weightNum) || weightNum <= 0) {
      message.error("请输入有效的体重数值");
      return;
    }

    try {
      await recordWeight({
        weight: weightNum,
        recordDate: today
      });
      message.success("体重记录成功！");
      // 重新加载数据（包含今天的数据）
      loadLatestWeight();
      loadWeightData(dateRange[0].format('YYYY-MM-DD'), dateRange[1].format('YYYY-MM-DD'));
    } catch (error) {
      message.error("体重记录失败");
    }
  };
  
  const loadLatestWeight = async () => {
  const latest = await getLatestWeight(); 
  setLatestWeightRecord(latest);
};

  

  

  // 图表配置
  const chartConfig = {
    data: weightData.map(item => ({
      date: item.date,
      weight: item.weight
    })),
    xField: 'date',
    yField: 'weight',
    xAxis: {
      type: 'time',
      mask: 'YYYY-MM-DD',
    },
    yAxis: {
      label: {
        formatter: (v: number) => `${v}kg`,
      },
    },
    point: {
      shapeField: 'square',
      sizeField: 4,
    },
    lineStyle: {
      lineWidth: 2,
    },
    tooltip: {
      formatter: (datum: any) => {
        return { name: '体重', value: `${datum.weight}kg` };
      },
    },
  };

  // 初始加载最近7天数据
  useEffect(() => {
    const start = dateRange[0].format('YYYY-MM-DD');
    const end = dateRange[1].format('YYYY-MM-DD');
    loadWeightData(start, end);
    loadLatestWeight();
    loadEarliestWeightDate(); 
  }, []);

  return (
    <div style={{ padding: '20px', maxWidth: 800, margin: '0 auto' }}>
      <Title level={2} style={{ textAlign: 'center', marginBottom: 24 }}>
        体重记录
      </Title>

      {/* 日期选择器 */}
      <Card style={{ marginBottom: 24 }}>
        <RangePicker
          value={dateRange}
          onChange={handleDateChange}
          allowClear={false}
          style={{ width: '100%' }}
           disabledDate={(current) => {
            // 限制未来日期
            const isFuture = current && current > dayjs().endOf('day');
            // 限制早于最早记录的日期
            const isBeforeEarliest = earliestRecordDate 
              ? current && current < earliestRecordDate.startOf('day')
              : false;
            return isFuture || isBeforeEarliest;
        
  }}
        />
      </Card>

      {/* 最新体重显示 */}
      <Card style={{ marginBottom: 24, textAlign: 'center' }}>
        {loading ? (
          <Spin />
        ) : latestWeightRecord? (
          <>
            <Text strong style={{ fontSize: '24px', color: '#1890ff' }}>
              {latestWeightRecord.weight}kg
            </Text>
            <br />
            <Text type="secondary" style={{ fontSize: '14px' }}>
              最新记录：{latestWeightRecord.date}
            </Text>
          </>
        ) : (
          <Text type="secondary">暂无体重记录</Text>
        )}
      </Card>

      {/* 体重折线图 */}
      <Card style={{ marginBottom: 24 }}>
        <div style={{ height: 300 }}>
          {loading ? (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
              <Spin />
            </div>
          ) : weightData.length > 0 ? (
            <Line {...chartConfig} />
          ) : (
            <div style={{ 
              display: 'flex', 
              justifyContent: 'center', 
              alignItems: 'center', 
              height: '100%',
              color: '#999'
            }}>
              暂无数据
            </div>
          )}
        </div>
      </Card>

      {/* 记录按钮 */}
      <Button 
        type="primary" 
        size="large" 
        onClick={handleRecordWeight}
        block
        style={{ height: 50, fontSize: '16px' }}
      >
        记录今日体重
      </Button>
    </div>
  );
}