import { getPostList, likePost } from "@/services/post";
import { PostListType, type PostVO } from "@/types/post";
import { message } from "antd";
import { useEffect, useState } from "react";
import InfiniteScroll from "react-infinite-scroll-component";
import PostCard from "@/components/PostCard";
export default function FollowingPage() {
  const [posts, setPosts] = useState<PostVO[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);

  const fetchPosts = async (page: number) => {
    if (loading) return;
    setLoading(true);
    try {
      const response = await getPostList(page, PostListType.FOLLOWING);
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
      console.error("获取推荐帖子失败:", error);
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
          <PostCard key={post.id} post={post} onLike={handleLike} />
        ))}
      </InfiniteScroll>
    </div>
  );
}
