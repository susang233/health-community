// src/components/WeightChart.tsx
import { Line } from "@ant-design/charts";
import { Spin, Card } from "antd";
import { useEffect, useState } from "react";
import dayjs from "dayjs";
import type { WeightRecord } from "@/types/health";
import { getWeightHistory } from "@/services/health";

type WeightChartProps = {
  days?: number; // 默认最近7天
  height?: number;
};

export default function WeightChart({ days = 7, height = 200 }: WeightChartProps) {
  const [weightData, setWeightData] = useState<WeightRecord[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadWeightData = async () => {
      setLoading(true);
      try {
        const start = dayjs().subtract(days - 1, 'day').format('YYYY-MM-DD'); // 包含今天共 days 天
        const end = dayjs().format('YYYY-MM-DD');
        const response = await getWeightHistory(start, end);
        setWeightData(response || []);
      } catch (error) {
        console.error("加载体重图表数据失败", error);
        setWeightData([]);
      } finally {
        setLoading(false);
      }
    };

    loadWeightData();
  }, [days]);

  const chartConfig = {
    data: weightData.map(item => ({
      date: item.date,
      weight: item.weight
    })),
    xField: 'date',
    yField: 'weight',
    height: height,
    xAxis: {
      type: 'time',
      mask: 'MM-DD',
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
    autoFit: true,
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height }}>
        <Spin size="small" />
      </div>
    );
  }

  if (weightData.length === 0) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height,
        color: '#999',
        fontSize: '14px'
      }}>
        暂无体重数据
      </div>
    );
  }

  return <Line {...chartConfig} />;
}