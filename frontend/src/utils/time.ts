// utils/time.ts
import dayjs from 'dayjs';

// 拆分 ISO 字符串（如 "2026-03-15T05:44:40"）
export const parseRecordTime = (recordTime: string | null) => {
  if (!recordTime) {
    return { date: dayjs(), time: null };
  }

  // 用 dayjs 解析 ISO 字符串
  const dt = dayjs(recordTime, 'YYYY-MM-DDTHH:mm:ss', true);
  
  if (!dt.isValid()) {
    return { date: dayjs(), time: null };
  }

  const hours = dt.hour();
  const minutes = dt.minute();
  const seconds = dt.second();

  // 判断是否为 00:00:00（未记录时间）
  const isZeroTime = hours === 0 && minutes === 0 && seconds === 0;

  return {
    date: dt,
    time: isZeroTime ? null : dt,
  };
};

// 合并为 ISO 字符串（不含时区）
export const buildRecordTime = (
  date: dayjs.Dayjs | null,
  time: dayjs.Dayjs | null
): string | null => {
  if (!date) return null;

  let finalDateTime = date.startOf('day'); // 默认 00:00:00

  if (time) {
    // 覆盖时间为用户选择的时间（保留时分秒）
    finalDateTime = finalDateTime
      .hour(time.hour())
      .minute(time.minute())
      .second(time.second());
  }
  // 输出格式：2026-03-15T05:44:40
  return finalDateTime.format('YYYY-MM-DDTHH:mm:ss');
};

export const getDefaultMealType = () => {
  const hour = dayjs().hour();
  if (hour >= 4 && hour < 11) return "BREAKFAST";
  if (hour >= 11 && hour < 15) return "LUNCH";
  if (hour >= 15 || hour < 4) return "DINNER";
  return "DINNER";
};