import { getUserPostList, likePost } from "@/services/post";
import { type PostVO } from "@/types/post";
import { Avatar, Button, message, Typography, Modal } from "antd";
import { useEffect, useState } from "react";
import InfiniteScroll from "react-infinite-scroll-component";
import PostCard from "@/components/PostCard";
import type { UserVO } from "@/types/user";
import { followUser, getUserVO, unfollowUser } from "@/services/user";
import { useParams } from "react-router-dom";
import { getUser } from "@/utils/auth";
import { DEFAULT_AVATAR_URL } from "@/constant";
const { Text } = Typography;
const { confirm } = Modal;

export default function UserHomePage() {
  const [posts, setPosts] = useState<PostVO[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [userProfile, setUserProfile] = useState<UserVO | null>(null);
  const [profileLoading, setProfileLoading] = useState(true);
  const { userId } = useParams<{ userId: string }>();
  const targetUserId = Number(userId);
  const currentUserId = getUser()?.userId;
  // 新增：加载用户资料
  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        const profile = await getUserVO(targetUserId); // 调用获取当前用户资料的接口
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
      const response = await getUserPostList(page, targetUserId);
      const newPosts = response.posts;

      setPosts((prev) => {
        const existingIds = new Set(prev.map((p) => p.id));
        const uniqueNewPosts = newPosts.filter(
          (post) => !existingIds.has(post.id),
        );
        return [...prev, ...uniqueNewPosts];
      });
      setHasMore(response.hasNext);
      setCurrentPage(page);
    } catch (error) {
      console.error("获取帖子失败:", error);
      setHasMore(false); // 出错时停止加载
    } finally {
      setLoading(false);
    }
  };
  //初始加载第一页
  useEffect(() => {
    fetchPosts(1);
  }, []);

  // 加载下一页
  const loadMore = () => {
    fetchPosts(currentPage + 1);
  };
  const handleLike = async (postId: number, currentIsLiked: boolean) => {
    console.log("【开始点赞】", postId, currentIsLiked);
    // 2. 乐观更新：立即更新 UI

    setPosts(
      posts.map((post) =>
        post.id === postId
          ? {
              ...post,
              isLike: !currentIsLiked,
              likeCount: currentIsLiked
                ? post.likeCount - 1
                : post.likeCount + 1,
            }
          : post,
      ),
    );
    try {
      // 3. 调用真实 API
      const result = await likePost(postId);

      // 4. 用服务端返回的数据覆盖本地状态，确保一致性
      setPosts(
        posts.map((post) =>
          post.id === postId
            ? { ...post, isLike: result.liked, likeCount: result.likeCount }
            : post,
        ),
      );
    } catch (error) {
      // 5. 失败：回滚状态 + 提示
      setPosts(
        posts.map((post) =>
          post.id === postId
            ? {
                ...post,
                isLike: currentIsLiked, // 恢复原状态
                likeCount: currentIsLiked
                  ? post.likeCount + 1
                  : post.likeCount - 1,
              }
            : post,
        ),
      );
      message.error("操作失败，请重试");
    }
  };

  const handleToggleFollow = async () => {
    const prevIsFollow = userProfile?.isFollow;

    // 如果是“取关”，先弹确认框
    if (prevIsFollow) {
      confirm({
        title: "确定要取消关注吗？",
        content: "取消关注后，你将不再收到该用户的动态更新。",
        okText: "确定",
        okType: "default", // 取关用普通按钮（避免红色危险感过强）
        cancelText: "取消",
        onOk: async () => {
          if (!userProfile || !currentUserId) return; // 安全防护

          // 用户确认后，执行取关逻辑
          setUserProfile({ ...userProfile!, isFollow: false });

          try {
            await unfollowUser(targetUserId);
            // 成功：UI 已更新，无需额外操作
          } catch (error) {
            // 失败：回滚为“已关注”
            setUserProfile({
              ...userProfile,

              isFollow: true, // 乐观更新为未关注
            });
            message.error("取消关注失败");
          }
        },
        // onCancel: 不需要处理，UI 保持原状
      });
      return; //  提前返回，避免继续执行下面的逻辑
    }

    // ====== 如果是“关注”（prevIsFollow === false），直接操作 ======
    setUserProfile({ ...userProfile!, isFollow: true });

    try {
      await followUser(targetUserId);
    } catch (error) {
      // 回滚
     setUserProfile({ ...userProfile!, isFollow: false });
      message.error("关注失败");
    }
  };

  return (
    <div
      id="scroll-target"
      style={{
        padding: "8px",
        paddingBottom: "60px",
        height: 600, // 固定高度
        overflowY: "auto",
        border: "1px solid #f0f0f0",
        borderRadius: 8,
      }}
    >
      {/*  用户信息卡片 */}
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
          <Avatar src={userProfile.avatarUrl||DEFAULT_AVATAR_URL} size={64} />
          <div>
            <Text strong style={{ fontSize: "18px" }}>
              {userProfile.nickName}
            </Text>
             {/* 关注按钮 */}
              {targetUserId !== currentUserId && (
                <Button
                  type={userProfile.isFollow ? "default" : "primary"}
                  onClick={handleToggleFollow}
                  style={{marginLeft:80}}
                >
                  {userProfile.isFollow ? "已关注" : "关注"}
                </Button>
              )}
            <div style={{ marginTop: 8, display: "flex", gap: 24 }}>
              <span>
                <Text type="secondary">关注</Text>{" "}
                <Text strong>{userProfile.followingCount}</Text>
              </span>
              <span>
                <Text type="secondary">粉丝</Text>{" "}
                <Text strong>{userProfile.followersCount}</Text>
              </span>
             
            </div>
            {/* 可选：关注按钮（自己主页可不显示） */}
            {/* <Button type="primary" size="small">
            {userProfile.isFollow ? '已关注' : '关注'}
          </Button> */}
          </div>
        </div>
      ) : null}
      <InfiniteScroll
        dataLength={posts.length}
        next={loadMore}
        hasMore={hasMore}
        loader={
          <div style={{ textAlign: "center", padding: "16px" }}>加载中...</div>
        }
        endMessage={
          <div
            style={{ textAlign: "center", padding: "16px", color: "#8c8c8c" }}
          >
            没有更多了
          </div>
        }
        scrollableTarget="scroll-target"
      >
        {posts.map((post) => (
          <PostCard
            key={post.id}
            post={post}
            onLike={handleLike}
            showUserInfo={false}
          />
        ))}
      </InfiniteScroll>
    </div>
  );
}
