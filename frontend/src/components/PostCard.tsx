// components/PostCard.tsx
import { Avatar, Button, Image, message, Space, Tag, Typography } from "antd";
import { HeartOutlined, MessageOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import { useState } from "react";
import type { PostVO } from "@/types/post";
import { router } from "@/router";
import { useNavigate } from "react-router-dom";
import { DEFAULT_AVATAR_URL } from "@/constant";
const { Text } = Typography;
export interface PostCardProps {
  post: PostVO;
  showUserInfo?: boolean;
  onLike?: (postId: number, currentIsLiked: boolean) => Promise<void>;
}

export default function PostCard({
  post,
  showUserInfo = true,
  onLike,
}: PostCardProps) {
  const [likeLoading, setLikeLoading] = useState(false);
  // 处理图片显示逻辑：最多3张，超过3张在第三张上显示 "+X"
  const displayImages = post.postImageList.slice(0, 3);
  const remainingCount = post.postImageList.length - 3;
  const navigate = useNavigate();

  const handleLikeClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!onLike || likeLoading) return;

    setLikeLoading(true);
    try {
      await onLike(post.id, post.isLike);
    } catch (error) {
      message.error("点赞失败，请重试");
    } finally {
      setLikeLoading(false);
    }
  };

  return (
    <div
      key={post.id}
      style={{
        padding: "16px",
        borderBottom: "1px solid #f0f0f0",
        cursor: "pointer",
        height: 307 /* 固定卡片高度 */,
        display: "flex",
        flexDirection: "column",
        position: "relative",
      }}
      onClick={() => {
        // 跳转到帖子详情页
        navigate(`/dashboard/post/${post.id}`);
        console.log("跳转到帖子详情:", post.id);
      }}
    >
      {post.status !== "APPROVED" && (
        <Tag
          color={
            post.status === "PENDING"
              ? "gold" // 待审核 → 金色
              : "red" // 拒绝 → 红色
          }
          style={{
            position: "absolute",
            top: 8,
            right: 8,
            fontSize: "12px",
            padding: "0 6px",
            zIndex: 1,
          }}
        >
          {post.status === "PENDING" ? "审核中" : post.rejectReason}
        </Tag>
      )}
      {showUserInfo && (
        <div style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
          <Avatar src={post.avatarUrl||DEFAULT_AVATAR_URL} size="large" />
          <div>
            <Text strong>{post.nickName}</Text>
            {post.profileText && (
              <Text
                type="secondary"
                style={{ fontSize: "12px", display: "block" }}
              >
                {post.profileText}
              </Text>
            )}
          </div>
        </div>
      )}
      {/* 帖子内容区域 */}
      <div style={{ marginTop: "12px", marginBottom: "12px" }}>
        <Text style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
          {post.content}
        </Text>
      </div>

      {/* 图片区域 */}
      {post.postImageList.length > 0 && (
        <div style={{ marginBottom: "12px" }}>
          <Image.PreviewGroup>
            <Space size={[4, 4]} wrap>
              {displayImages.map((img, index) => {
                // 如果是第三张且还有剩余图片，则叠加一个显示数量的遮罩
                const showOverlay = index === 2 && remainingCount > 0;

                return (
                  <div key={img.id} style={{ position: "relative" }}>
                    <Image
                      src={img.imageUrl}
                      alt={`图片 ${index + 1}`}
                      style={{
                        width: 100,
                        height: 100,
                        objectFit: "cover",
                        borderRadius: 4,
                      }}
                    />
                    {showOverlay && (
                      <div
                        style={{
                          position: "absolute",
                          top: 0,
                          left: 0,
                          right: 0,
                          bottom: 0,
                          backgroundColor: "rgba(0, 0, 0, 0.5)",
                          borderRadius: 4,
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        <Text style={{ color: "white", fontSize: "14px" }}>
                          +{remainingCount}
                        </Text>
                      </div>
                    )}
                  </div>
                );
              })}
            </Space>
          </Image.PreviewGroup>
        </div>
      )}

      {/* 底部操作栏 */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginTop: "auto",
        }}
      >
        {/* 左侧：发布时间 */}
        <Text type="secondary" style={{ fontSize: "12px" }}>
          {dayjs(post.createTime).format("MM-DD HH:mm")}
        </Text>

        {/* 右侧：评论 + 点赞 */}
        <Space size="small">
          {/* 评论按钮 */}
          <Space size="small">
            <MessageOutlined style={{ color: "#8c8c8c" }} />
            <Text type="secondary">{post.commentCount}</Text>
          </Space>

          {/* 点赞按钮 */}
          <Button
            type="text"
            icon={
              post.isLike ? (
                <HeartOutlined style={{ color: "#ff4d4f" }} />
              ) : (
                <HeartOutlined style={{ color: "#8c8c8c" }} />
              )
            }
            onClick={handleLikeClick}
          >
            <span>{post.likeCount}</span>
          </Button>
        </Space>
      </div>
    </div>
  );
}
