// src/router/index.tsx
import { createBrowserRouter, Navigate } from "react-router-dom";
import App from "@/App";
import LoginPage from "@/pages/Login";
import AdminLoginPage from "@/pages/admin/AdminLogin";
import RegisterPage from "@/pages/Register";
import AuthGuard from "@/components/AuthGuard";
import HealthLayout from "@/layouts/HealthLayout";
import CommunityLayout from "@/layouts/CommunityLayout";
import DashboardPage from "@/pages/DashboardPage"; // 仪表盘内容组件
import MyPostsPage from "@/pages/Community/MyPostsPage";
import DashboardLayout from "@/layouts/DashboardLayout";
import WeightPage from "@/pages/Health/WeightPage";
import DietPage from "@/pages/Health/DietPage";
import RecommendPage from "@/pages/Community/RecommendPage";
import AssessmentPage from "@/pages/AssessmentPage";
import FollowingPage from "@/pages/Community/FollowingPage";
import UserHomePage from "@/pages/Community/UserHomePage";
import AssessmentResult from "@/pages/AssessmentPage/step/AssessmentResult";
import { getToken, isTokenValid } from "@/utils/auth";
import AdminDashboardLayout from "@/pages/admin/AdminDashboardLayout";
import AdminDashboardPage from "@/pages/admin/AdminDashboardPage";
import AdminFoodsPage from "@/pages/admin/AdminFoodsPage";
import AdminPostsPage from "@/pages/admin/AdminPostsPage";
import AdminAuthGuard from "@/components/AdminAuthGuard";
import AdminCreatePage from "@/pages/admin/AdminCreatePage";
import PostDetailPage from "@/pages/Community/PostDetailPage";
export const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      {
        index: true,
        element: (
          <Navigate
            to={getToken() && isTokenValid() ? "/dashboard" : "/login"}
            replace
          />
        ),
      },
      { path: "/login", element: <LoginPage /> },
      { path: "/register", element: <RegisterPage /> },
      { path: "/admin/login", element: <AdminLoginPage /> },
       {
        element: <AdminAuthGuard />,
        children: [
         
          {
            path: "/admin/dashboard",
            element: <AdminDashboardLayout />,
            children: [
              { index: true, element: <AdminDashboardPage /> }, // /dashboard
              {
                path: "foods",
                element: <AdminFoodsPage />,
               
              },
              {
                path: "posts",
                element: <AdminPostsPage />,
              
              },
               {
                path: "create",
                element: <AdminCreatePage />,
              
              },
            ],
          },
        ],
      },
      {
        element: <AuthGuard />,
        children: [
          {
            path: "/dashboard",
            element: <DashboardLayout />,
            children: [
              { index: true, element: <DashboardPage /> }, // /dashboard
              {
                path: "health",
                element: <HealthLayout />,
                children: [
                  { index: true, element: <Navigate to="diet" replace /> }, // 可选的健康模块首页
                  { path: "weight", element: <WeightPage /> },
                  { path: "diet", element: <DietPage /> },

                  // ... 其他
                ],
              },
              {
                path: "community",
                element: <CommunityLayout />,
                children: [
                  { index: true, element: <Navigate to="recommend" replace /> },
                  { path: "recommend", element: <RecommendPage /> },
                  { path: "following", element: <FollowingPage /> },
                  { path: "user/:userId", element: <UserHomePage /> },
                   { path: "my-posts", element: <MyPostsPage /> },
                  
                  
                ],
              },
                { path: "post/:id", element: <PostDetailPage /> },
            ],
          },
          {
            path: "/assessment",

            children: [
              { index: true, element: <AssessmentPage /> },
              { path: "result", element: <AssessmentResult /> },
            ],
          },
          
        ],
      },
     
    ],
  },
]);
