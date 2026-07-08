import React from "react";
import { Navigate } from "react-router-dom";
import { message } from "antd";
import { isSuperAdmin } from "@/utils/auth";

export default function SuperAdminGuard({ children }: { children: React.ReactNode }) {
  if (!isSuperAdmin()) {
    message.warning("无权限访问此页面");
    return <Navigate to="/admin/dashboard" replace />;
  }
  return <>{children}</>;
}