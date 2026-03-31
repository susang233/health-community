export const getHealthLightColor = (level: number): string => {
  switch (level) {
    case 1:
      return "#52c41a";
    case 2:
      return "#faad14";
    case 3:
      return "#ff4d4f";
    default:
      return "#d9d9d9";
  }
};
