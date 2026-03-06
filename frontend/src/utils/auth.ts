// src/utils/auth.ts

/**
 * 获取 token（优先从 localStorage 读，没有再读 sessionStorage）
 */
export const getToken = (): string | null => {
  // 先读 localStorage（记住我）
  const token = localStorage.getItem('token');
  if (token) return token;
  
  // 再读 sessionStorage（不记住我）
  return sessionStorage.getItem('token');
};

/**
 * 设置 token（根据 rememberMe 决定存储位置）
 */
export const setToken = (token: string, rememberMe: boolean): void => {
  if (rememberMe) {
    localStorage.setItem('token', token);
    sessionStorage.removeItem('token'); // 清理另一个存储，保持数据一致
  } else {
    sessionStorage.setItem('token', token);
    localStorage.removeItem('token');   // 清理另一个存储
  }
};

/**
 * 清除 token（两个存储都清）
 */
export const removeToken = (): void => {
  localStorage.removeItem('token');
  sessionStorage.removeItem('token');
};

/**
 * 检查是否有 token（用于判断登录状态）
 */
export const hasToken = (): boolean => {
  return !!getToken();
};

export const isTokenValid = (): boolean => {
  const token = getToken();
  if (!token) return false;

  try {
    // 1. 检查是否是 JWT 格式（至少包含两个点）
    const parts = token.split('.');
    if (parts.length !== 3) {
      // 不是 JWT，当作永久有效（或无效，根据业务）
      // 通常：非 JWT token 由后端控制有效期，前端无法判断 → 默认视为有效
      return true;
    }

    // 2. 解码 payload
    const payloadBase64 = parts[1];
    // 补全 Base64 padding（有些 JWT 省略了 =）
    const paddedBase64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
    const decoded = atob(paddedBase64 + '=='.slice((paddedBase64.length % 4) || 0));

    const payload = JSON.parse(decoded);

    // 3. 检查 exp 是否存在且未过期
    if (typeof payload.exp === 'number') {
      return payload.exp * 1000 > Date.now();
    }

    // 无 exp 字段 → 永不过期
    return true;
  } catch (error) {
    // 任何解析错误都视为无效
    console.warn('Token validation failed:', error);
    return false;
  }
};

