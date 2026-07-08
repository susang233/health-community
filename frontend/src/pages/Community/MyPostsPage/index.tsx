import { getUserPostList, likePost } from "@/services/post";
import { type PostVO } from "@/types/post";
import { Avatar, message, Typography } from "antd";
import { useEffect, useState } from "react";
import InfiniteScroll from "react-infinite-scroll-component";
import PostCard from "@/components/PostCard";
import type { UserVO } from "@/types/user";
import { getUserVO } from "@/services/user";
import { DEFAULT_AVATAR_URL } from "@/constant";
import { useNavigate } from "react-router-dom"; // 🔥 导入路由跳转

const { Text } = Typography;

export default function MyPostsPage() {
  const navigate = useNavigate(); // 🔥 初始化跳转
  const [posts, setPosts] = useState<PostVO[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [userProfile, setUserProfile] = useState<UserVO | null>(null);
  const [profileLoading, setProfileLoading] = useState(true);

  // 加载用户资料
  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        const profile = await getUserVO();
        setUserProfile(profile);
      } catch (error) {
        message.error("加载用户信息失败");
      } finally {
        setProfileLoading(false);
      }
    };
    loadUserProfile();
  }, []);

  const fetchPosts = async (page: number) => {
    if (loading) return;
    setLoading(true);
    try {
      const response = await getUserPostList(page);
      const newPosts = response.posts;

      setPosts((prev) => {
        const existingIds = new Set(prev.map((p) => p.id));
        const uniqueNewPosts = newPosts.filter((post) => !existingIds.has(post.id));
        return [...prev, ...uniqueNewPosts];
      });
      setHasMore(response.hasNext);
      setCurrentPage(page);
    } catch (error) {
      console.error("获取帖子失败:", error);
      setHasMore(false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts(1);
  }, []);

  const loadMore = () => {
    fetchPosts(currentPage + 1);
  };

  const handleLike = async (postId: number, currentIsLiked: boolean) => {
    setPosts(
      posts.map((post) =>
        post.id === postId
          ? {
              ...post,
              isLike: !currentIsLiked,
              likeCount: currentIsLiked ? post.likeCount - 1 : post.likeCount + 1,
            }
          : post
      )
    );
    try {
      const result = await likePost(postId);
      setPosts(
        posts.map((post) =>
          post.id === postId ? { ...post, isLike: result.liked, likeCount: result.likeCount } : post
        )
      );
    } catch (error) {
      message.error("操作失败");
    }
  };

  return (
    <div
      id="scroll-target"
      style={{
        padding: "8px",
        paddingBottom: "60px",
        height: 600,
        overflowY: "auto",
        border: "1px solid #f0f0f0",
        borderRadius: 8,
      }}
    >
      {/* 用户信息卡片 + 点击跳转 */}
      {profileLoading ? (
        <div style={{ padding: "16px", textAlign: "center" }}>加载中...</div>
      ) : userProfile ? (
        <div
          style={{
            padding: "16px",
            backgroundColor: "#fafafa",
            borderRadius: 8,
            marginBottom: "16px",
            display: "flex",
            alignItems: "center",
            height: 100,
            gap: 12,
          }}
        >
          <Avatar src={userProfile.avatarUrl || DEFAULT_AVATAR_URL} size={64} />
          <div>
            <Text strong style={{ fontSize: "18px" }}>
              {userProfile.nickName}
            </Text>
            <div style={{ marginTop: 8, display: "flex", gap: 24 }}>
              {/* 点击 关注 跳转 */}
              <span
                onClick={() => navigate("/dashboard/community/followees")}
                style={{ cursor: "pointer", color: "#1890ff" }}
              >
                <Text type="secondary">关注</Text>{" "}
                <Text strong>{userProfile.followingCount}</Text>
              </span>

              {/*  点击 粉丝 跳转 */}
              <span
                onClick={() => navigate("/dashboard/community/followers")}
                style={{ cursor: "pointer", color: "#1890ff" }}
              >
                <Text type="secondary">粉丝</Text>{" "}
                <Text strong>{userProfile.followersCount}</Text>

              </span>
              <span>
                <Text type="secondary">帖子数</Text>{" "}
              <Text strong>{userProfile.postCount}</Text>
              
                </span>

            </div>
          </div>
        </div>
      ) : null}

      <InfiniteScroll
        dataLength={posts.length}
        next={loadMore}
        hasMore={hasMore}
        loader={<div style={{ textAlign: "center", padding: "16px" }}>加载中...</div>}
        endMessage={<div style={{ textAlign: "center", padding: "16px", color: "#8c8c8c" }}>没有更多了</div>}
        scrollableTarget="scroll-target"
      >
        {posts.map((post) => (
          <PostCard key={post.id} post={post} onLike={handleLike} showUserInfo={false} />
        ))}
      </InfiniteScroll>
    </div>
  );
}