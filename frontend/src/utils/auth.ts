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

export const setAdminToken = (token: string, rememberMe: boolean): void => {
  if (rememberMe) {
    localStorage.setItem('adminToken', token);
    sessionStorage.removeItem('adminToken'); // 清理另一个存储，保持数据一致
  } else {
    sessionStorage.setItem('adminToken', token);
    localStorage.removeItem('adminToken');   // 清理另一个存储
  }
};
export const getAdminToken = (): string | null => {
  // 先读 localStorage（记住我）
  const token = localStorage.getItem('adminToken');
  if (token) return token;
  
  // 再读 sessionStorage（不记住我）
  return sessionStorage.getItem('adminToken');
};
export const hasAdminToken = (): boolean => {
  return !!getAdminToken();
};
export const isAdminTokenValid = (): boolean => {
  const token = getAdminToken();
  if (!token) return false;

  try {
    const parts = token.split('.');
    if (parts.length !== 3) return true;

    let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    // 修复 padding
    const pad = '='.repeat((4 - (base64.length % 4)) % 4);
    const json = atob(base64 + pad);
    const payload = JSON.parse(json);

    if (typeof payload.exp === 'number') {
      return payload.exp * 1000 > Date.now(); // 注意：exp 是秒，Date.now() 是毫秒
    }
    return true;
  } catch (e) {
    console.warn('Token invalid:', e);
    return false;
  }
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
    const parts = token.split('.');
    if (parts.length !== 3) return true;

    let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
   
    const padding = '='.repeat((4 - (base64.length % 4)) % 4);
    const decoded = atob(base64 + padding);

    const payload = JSON.parse(decoded);
    if (typeof payload.exp === 'number') {
      return payload.exp * 1000 > Date.now();
    }
    return true;
  } catch (error) {
    console.warn('Token validation failed:', error);
    return false;
  }
};


export const setUser = (userId:number,username: string, nickname: string, avatar: string, expireIn: number, role: string, rememberMe: boolean): void => {
  if (rememberMe) {
    localStorage.setItem('user', JSON.stringify({ userId, username, nickname, avatar, expireIn, role }));
    sessionStorage.removeItem('username'); // 清理另一个存储，保持数据一致
  } else {
    sessionStorage.setItem('user', JSON.stringify({ userId, username, nickname, avatar, expireIn, role }));
    localStorage.removeItem('username');   // 清理另一个存储
  }
};
export const getUser = (): { userId: number; username: string; nickname: string; avatar: string; expireIn: number; role: string } | null => {
  // 先读 localStorage（记住我）
  const user = localStorage.getItem('user');
  if (user) return JSON.parse(user);
  
  // 再读 sessionStorage（不记住我）
  return JSON.parse(sessionStorage.getItem('user') || 'null');
};
// ===== 退出登录（清理所有）=====
export const logout = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  sessionStorage.removeItem('token');
  sessionStorage.removeItem('user');
};



export const setAdmin = (userId:number,username: string, nickname: string, avatar: string, expireIn: number, role: string, rememberMe: boolean): void => {
  if (rememberMe) {
    localStorage.setItem('admin', JSON.stringify({ userId, username, nickname, avatar, expireIn, role }));
    sessionStorage.removeItem('username'); // 清理另一个存储，保持数据一致
  } else {
    sessionStorage.setItem('admin', JSON.stringify({ userId, username, nickname, avatar, expireIn, role }));
    localStorage.removeItem('username');   // 清理另一个存储
  }
};
export const getAdmin = (): { userId: number; username: string; nickname: string; avatar: string; expireIn: number; role: string } | null => {
  // 先读 localStorage（记住我）
  const admin = localStorage.getItem('admin');
  if (admin) return JSON.parse(admin);
  
  // 再读 sessionStorage（不记住我）
  return JSON.parse(sessionStorage.getItem('admin') || 'null');
};
// ===== 退出登录（清理所有）=====
export const adminLogout = (): void => {
  localStorage.removeItem('adminToken');
  localStorage.removeItem('admin');
  sessionStorage.removeItem('adminToken');
  sessionStorage.removeItem('admin');
};