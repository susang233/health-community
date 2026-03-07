// src/hooks/useHealthProfile.ts
import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { message, Modal } from 'antd';
import { checkHealthProfile } from '@/services/health';
import { getUsername, logout } from '@/utils/auth';

export const useHealthProfile = () => {
  const [hasProfile, setHasProfile] = useState(false);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchProfile = useCallback(async () => {
    try {
      const username = getUsername();
      if (!username) {
        message.error('请先登录');
        logout();//清除残留
        navigate('/login');
        return;
      }
      const res = await checkHealthProfile(username);
      setHasProfile(res);
    } catch (error) {
      message.error('获取健康档案失败');
      setHasProfile(false);
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const requireProfile = useCallback((callback?: () => void) => {
    if (hasProfile) {
      callback?.();
    } else {
      Modal.confirm({
        title: '完善健康档案',
        content: '您还没有完善健康档案，完善后才能使用该功能哦！',
        okText: '去完善',
        cancelText: '稍后',
        onOk: () => navigate('/assessment'),
      });
    }
  }, [hasProfile, navigate]);

  return {
    hasProfile,
    loading,
    requireProfile,
    refreshProfile: fetchProfile,
  };
};