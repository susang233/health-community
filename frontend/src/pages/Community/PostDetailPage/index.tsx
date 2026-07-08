// src/pages/PostDetailPage.tsx
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { followUser, unfollowUser } from "@/services/user";

import {
  Avatar,
  Button,
  Card,
  Image,
  Input,
  List,
  message,
  Space,
  Tag,
  Typography,
  Skeleton,
  Modal,
  Dropdown,
  type MenuProps,
} from "antd";
import {
  HeartOutlined,
  HeartFilled,
  MessageOutlined,
  UserAddOutlined,
  UserDeleteOutlined,
  EllipsisOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";
const { confirm } = Modal;

dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

import type { PostDetail, PostVO } from "@/types/post";
import type { CommentListVO, CommentVO } from "@/types/post";
import {
  getUserPostDetail as fetchPostDetail,
  likePost,
  addComment,
  getPostComments as fetchComments,
  deletePost,
} from "@/services/post";
import CreatePostForm from "../CreatePostForm";
import { getUser } from "@/utils/auth";
import { DEFAULT_AVATAR_URL } from "@/constant";

const { Text, Title } = Typography;
const { TextArea } = Input;



export default function PostDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const postId = Number(id);

  const [post, setPost] = useState<PostDetail | null>(null);
  const [comments, setComments] = useState<CommentVO[]>([]);
  const [hasMoreComments, setHasMoreComments] = useState(true);
  const [commentPage, setCommentPage] = useState(1);
  const [commentContent, setCommentContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [commentLoading, setCommentLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
 const [isPostCreateModalOpen, setIsPostCreateModalOpen] = useState(false);
  
  // ====== 加载帖子详情 ======
  useEffect(() => {
    if (!postId || isNaN(postId)) {
      message.error("无效的帖子 ID");
      navigate("/community/recommend");
      return;
    }

    const loadPost = async () => {
      try {
        setLoading(true);
        const data = await fetchPostDetail(postId);
        setPost(data);
        console.log("帖子详情数据:", data);

        // 同时加载第一页评论
        const commentData = await fetchComments(postId, 1);
        setComments(commentData.commentVOList);
        setHasMoreComments(commentData.hasNext);
        setCommentPage(1);
      } catch (error) {
        console.error("加载帖子失败:", error);
        message.error("帖子加载失败");
        navigate("/community/recommend");
      } finally {
        setLoading(false);
      }
    };

    loadPost();
  }, [postId]);
  // ====== 辅助函数 ======
const renderStatusTag = (status: string,rejectReason:string) => {
  if (status === "PENDING") {
    return <Tag color="orange">审核中</Tag>;
  }
  
  if (status === "REJECTED") {
    // 正确逻辑：有原因就显示，没有就显示“已拒绝”
    return <Tag color="red">{rejectReason || "已拒绝"}</Tag>;
  }
  return null; // APPROVED 不显示标签
};
  const refreshPostOnly = async () => {
  const data = await fetchPostDetail(postId);
  setPost(data);
};


  // ====== 点赞逻辑 ======
  const handleLike = async () => {
    if (!post) return;
    if (post.isLike === null) {
      message.warning("请先登录");
      navigate("/login");
      return;
    }

    const prevIsLiked = post.isLike;
    const prevLikeCount = post.likeCount;

    // 乐观更新
    setPost({
      ...post,
      isLike: !prevIsLiked,
      likeCount: prevIsLiked ? prevLikeCount - 1 : prevLikeCount + 1,
    });

    try {
      await likePost(postId);
    } catch (error) {
      // 回滚
      setPost({ ...post, isLike: prevIsLiked, likeCount: prevLikeCount });
      message.error("点赞失败");
    }
  };

  // ====== 关注/取消关注 ======
  // ====== 关注/取消关注（带取关确认）======
  const handleToggleFollow = async () => {
    if (!post) return;
    if (post.author.isFollow === null) {
      message.warning("请先登录");
      navigate("/login");
      return;
    }

    const prevIsFollow = post.author.isFollow;
    const targetUserId = post.author.userId;

    // 如果是“取关”，先弹确认框
    if (prevIsFollow) {
      confirm({
        title: "确定要取消关注吗？",
        content: "取消关注后，你将不再收到该用户的动态更新。",
        okText: "确定",
        okType: "default", // 取关用普通按钮（避免红色危险感过强）
        cancelText: "取消",
        onOk: async () => {
          // 用户确认后，执行取关逻辑
          setPost({
            ...post,
            author: {
              ...post.author,
              isFollow: false, // 乐观更新为未关注
            },
          });

          try {
            await unfollowUser(targetUserId);
            // 成功：UI 已更新，无需额外操作
          } catch (error) {
            // 失败：回滚为“已关注”
            setPost({
              ...post,
              author: {
                ...post.author,
                isFollow: true,
              },
            });
            message.error("取消关注失败");
          }
        },
        // onCancel: 不需要处理，UI 保持原状
      });
      return; //  提前返回，避免继续执行下面的逻辑
    }

    // ====== 如果是“关注”（prevIsFollow === false），直接操作 ======
    setPost({
      ...post,
      author: {
        ...post.author,
        isFollow: true,
      },
    });

    try {
      await followUser(targetUserId);
    } catch (error) {
      // 回滚
      setPost({
        ...post,
        author: {
          ...post.author,
          isFollow: false,
        },
      });
      message.error("关注失败");
    }
  };

  // ====== 提交评论 ======
  const handleCommentSubmit = async () => {
    if (!post || !commentContent.trim()) return;
    if (post.isLike === null) {
      message.warning("请先登录");
      navigate("/login");
      return;
    }

    if (commentContent.length > 200) {
      message.warning("评论不能超过200字");
      return;
    }

    setSubmitting(true);
    try {
      await addComment(postId, commentContent.trim());

      message.success("评论成功");

      // 重新加载第一页评论
      const freshComments = await fetchComments(postId, 1);
      setComments(freshComments.commentVOList);
      setHasMoreComments(freshComments.hasNext);
      setCommentPage(1);
      setCommentContent("");
      setPost({ ...post, commentCount: post.commentCount + 1 });
    } catch (error: any) {
      message.error(error?.message || "评论失败");
    } finally {
      setSubmitting(false);
    }
  };

  // ====== 加载更多评论 ======
  const loadMoreComments = async () => {
    if (!post || !hasMoreComments || commentLoading) return;
    const nextPage = commentPage + 1;
    setCommentLoading(true);
    try {
      const res = await fetchComments(postId, nextPage);
      setComments([...comments, ...res.commentVOList]);
      setHasMoreComments(res.hasNext);
      setCommentPage(nextPage);
    } catch (error) {
      message.error("加载更多评论失败");
    } finally {
      setCommentLoading(false);
    }
  };
  const navigateToUser = (userId: number) => {
    const currentUser=getUser();
    if(currentUser?.userId===userId){
      navigate("/dashboard/community/my-posts");
      return;
    }
    navigate(`/dashboard/community/user/${userId}`);
  };
  // ====== 渲染 ======
  if (loading) {
    return (
      <div style={{ padding: 24, maxWidth: 600, margin: "0 auto" }}>
        <Skeleton active />
      </div>
    );
  }

  if (!post) return null;
  const isOwnPost = post.isOwnPost || false;
  // ====== 删除帖子 ======
  const handleDeletePost = async () => {
    confirm({
      title: "确定要删除这篇帖子吗？",
      content: "删除后无法恢复",
      okText: "删除",
      okType: "danger",
      cancelText: "取消",
      onOk: async () => {
        try {
          await deletePost(postId);
          message.success("帖子已删除");
          navigate("/dashboard/community/my-posts"); // 删除后跳回自己的主页
        } catch (error) {
          message.error("删除失败");
        }
      },
    });
  };

  // ====== 编辑帖子 ======
  const handleEditPost = () => {
    setIsPostCreateModalOpen(true);
  };

  const menuItems: MenuProps["items"] = [
    {
      key: "edit",
      label: "编辑",
      onClick: handleEditPost,
    },
    {
      key: "delete",
      label: "删除",
      danger: true,
      onClick: handleDeletePost,
    },
  ];
 

  const handleCancel = () => {
    setIsPostCreateModalOpen(false);
  };

  const handleSuccess = () => {
    setIsPostCreateModalOpen(false);
    // 可选：刷新帖子列表
    refreshPostOnly(); // 刷新帖子详情，获取最新内容和图片
   
  };

  const closePostModal = () => {
    setIsPostCreateModalOpen(false);
  };

  return (
    <div style={{ padding: "16px", maxWidth: 600, margin: "0 auto" }}>
      {/* 返回按钮（可选） */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 16,
        }}
      >
        <Button type="link" onClick={() => navigate(-1)}>
          ← 返回
        </Button>

        {/* 右上角操作菜单：仅自己可见 */}
        {isOwnPost && (
          <Dropdown
            menu={{ items: menuItems }}
            trigger={["click"]}
            placement="bottomRight"
          >
            <Button type="text" icon={<EllipsisOutlined />} />
          </Dropdown>
        )}
      </div>

      {/* ====== 作者信息区 ====== */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: 12,
          marginBottom: 24,
        }}
      >
        <div
          onClick={() => navigateToUser(post.author.userId)}
          style={{ cursor: "pointer" }}
        >
          <Avatar src={post.author.avatarUrl||DEFAULT_AVATAR_URL} size={48} />
        </div>
        <div style={{ flex: 1 }}>
          <Space>
            <Text strong>{post.author.nickName}</Text>
            {renderStatusTag(post.status, post.rejectReason)}
          </Space>
          {post.author.profileText && (
            <Text type="secondary" style={{ fontSize: 12, display: "block" }}>
              {post.author.profileText}
            </Text>
          )}
        </div>
        {!isOwnPost && post.author.isFollow !== null && (
          <Button
            size="small"
            type={post.author.isFollow ? "default" : "primary"}
            icon={
              post.author.isFollow ? (
                <UserDeleteOutlined />
              ) : (
                <UserAddOutlined />
              )
            }
            onClick={handleToggleFollow}
            disabled={post.author.isFollow === null}
          >
            {post.author.isFollow ? "已关注" : "关注"}
          </Button>
        )}
      </div>

      {/* ====== 帖子内容 ====== */}
      <Card
        bodyStyle={{ padding: "16px 0" }}
        style={{ marginBottom: 24, border: "none", boxShadow: "none" }}
      >
        <div
          style={{ marginBottom: 12, whiteSpace: "pre-wrap", lineHeight: 1.6 }}
        >
          {post.content}
        </div>

        {/* 图片列表 */}
        {post.postImageList.length > 0 && (
          <Image.PreviewGroup>
            <Space size={[8, 8]} wrap>
              {post.postImageList.map((img) => (
                <Image
                  key={img.id}
                  src={img.imageUrl}
                  alt={`post-img-${img.id}`}
                  style={{
                    width: 100,
                    height: 100,
                    objectFit: "cover",
                    borderRadius: 4,
                  }}
                />
              ))}
            </Space>
          </Image.PreviewGroup>
        )}

        {/* 底部操作栏 */}
        <div
          style={{
            marginTop: 16,
            display: "flex",
            justifyContent: "space-between",
            color: "#8c8c8c",
            fontSize: 12,
          }}
        >
          <span>{dayjs(post.createTime).format("YYYY-MM-DD HH:mm")}</span>
          <Space size={16}>
            <Space size={4} onClick={handleLike} style={{ cursor: "pointer" }}>
              {post.isLike ? (
                <HeartFilled style={{ color: "#ff4d4f" }} />
              ) : (
                <HeartOutlined />
              )}
              <Text>{post.likeCount}</Text>
            </Space>
            <Space size={4}>
              <MessageOutlined />
              <Text>{post.commentCount}</Text>
            </Space>
          </Space>
        </div>
      </Card>

      {/* ====== 评论输入框 ====== */}
      <div style={{ marginBottom: 24 }}>
        <TextArea
          rows={2}
          placeholder="写下你的评论..."
          value={commentContent}
          onChange={(e) => setCommentContent(e.target.value)}
          onPressEnter={(e) => {
            if (e.shiftKey) return;
            e.preventDefault();
            handleCommentSubmit();
          }}
          maxLength={200}
          showCount
          disabled={post.isLike === null} // 未登录禁用
        />
        <div style={{ textAlign: "right", marginTop: 8 }}>
          <Button
            type="primary"
            size="small"
            loading={submitting}
            onClick={handleCommentSubmit}
            disabled={!commentContent.trim()}
          >
            发送
          </Button>
        </div>
      </div>

      {/* ====== 评论列表 ====== */}
      <Title level={5}>评论 ({post.commentCount})</Title>
      <List
        itemLayout="horizontal"
        dataSource={comments}
        renderItem={(comment) => (
          <List.Item style={{ padding: "8px 0" }}>
            <List.Item.Meta
              avatar={
                <div
                  onClick={() => navigateToUser(comment.userVO.userId)}
                  style={{ cursor: "pointer" }}
                >
                  <Avatar src={comment.userVO.avatarUrl||DEFAULT_AVATAR_URL} size={32} />
                </div>
              }
              title={
                <Text strong style={{ fontSize: 14 }}>
                  {comment.userVO.nickName}
                </Text>
              }
              description={
                <>
                  <div
                    style={{ fontSize: 14, lineHeight: 1.5, marginBottom: 4 }}
                  >
                    {comment.content}
                  </div>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {dayjs(comment.createTime).fromNow()}
                  </Text>
                </>
              }
            />
          </List.Item>
        )}
        loadMore={
          hasMoreComments ? (
            <div style={{ textAlign: "center", margin: "16px 0" }}>
              <Button onClick={loadMoreComments} loading={commentLoading}>
                加载更多
              </Button>
            </div>
          ) : comments.length > 0 ? (
            <div
              style={{
                textAlign: "center",
                color: "#8c8c8c",
                padding: "8px 0",
              }}
            >
              没有更多评论了
            </div>
          ) : null
        }
        locale={{ emptyText: "暂无评论" }}
      />
      <Modal
        title="发布新帖子"
        open={isPostCreateModalOpen}
        onCancel={closePostModal}
        footer={null}
        width={600}
      >
        <CreatePostForm
          initialData={{
            id: post.id,
            content: post.content,
            imageUrls: post.postImageList.map((img) => img.imageUrl),
          }}
          onSuccess={handleSuccess}
          onCancel={handleCancel}
        />
      </Modal>
    </div>
  );
}
