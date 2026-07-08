import React, { useState, useEffect, useRef } from "react";
import {
  Table,
  Tag,
  Button,
  Space,
  Tooltip,
  Typography,
  Select,
  Pagination,
  Dropdown,
  type MenuProps,
  Modal,
  Spin,
  Image,
  message,
  Input,
  Radio,
  type InputRef,
} from "antd";
import {
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  DownOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import { PostStatus, type AdminPostDetailVO, type Post } from "@/types/post";
import { getPostDetail, getPosts, reviewPost } from "@/services/post";

// 假设的接口类型

// 状态映射
const statusMap = {
  PENDING: { color: "orange", text: "待审核" },
  APPROVED: { color: "green", text: "已通过" },
  REJECTED: { color: "red", text: "已拒绝" },
};

export default function AdminPostsPage() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);

  const pageSize = 20;
  const [total, setTotal] = useState(0);
  const [statusFilter, setStatusFilter] = useState<PostStatus | undefined>(
    undefined,
  );

  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [currentPostDetail, setCurrentPostDetail] =
    useState<AdminPostDetailVO | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // 获取帖子列表
  const fetchPosts = async (page: number, status?: PostStatus) => {
    setLoading(true);
    try {
      const result = await getPosts(page, status);

      setPosts(result.content || []);
      setTotal(result.totalElements || 0);
    } catch (error) {
      console.error("获取帖子列表失败:", error);
    } finally {
      setLoading(false);
    }
  };

  // 处理分页变化
  const handlePaginationChange = (page: number) => {
    setCurrentPage(page);
    fetchPosts(page, statusFilter);
  };

  // 处理状态筛选变化
  const handleStatusChange = (value: PostStatus | undefined) => {
    setStatusFilter(value || undefined);
    setCurrentPage(1); // 重置到第一页
    fetchPosts(1, value || undefined);
  };

  // 查看详情
  const handleViewDetail = async (postId: number) => {
    setDetailLoading(true);
    try {
      const detail = await getPostDetail(postId); // 调用详情接口
      setCurrentPostDetail(detail);
      setDetailModalOpen(true);
    } catch (error) {
      console.error("获取帖子详情失败:", error);
      // 可以加 message.error('获取失败')
    } finally {
      setDetailLoading(false);
    }
  };
  const handleModalClose = () => {
    setDetailModalOpen(false);
    setCurrentPostDetail(null);
  };

  // 审核通过
  const handleApprove = async (id: number,userId:number) => {
    try {
      // 调用审核通过接口

      console.log("通过:", id);
      await reviewPost({ postId: id, status: PostStatus.APPROVED ,userId:userId});
      fetchPosts(currentPage, statusFilter);
      // 可选：提示用户
      message.success("审核通过");
    } catch (error) {
      console.error("审核通过失败:", error);
      message.error("审核通过失败");
    }
  };

  const REJECT_REASONS = [
    "内容低俗或色情",
    "包含人身攻击或引战言论",
    "广告或 spam 信息",
    "涉及违法违规内容",
    "与社区主题无关",
    "其他",
  ];
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [rejectingPostId, setRejectingPostId] = useState<number | null>(null);
  const [selectedRejectReason, setSelectedRejectReason] = useState<string>(
    REJECT_REASONS[0],
  );
  const [customRejectReason, setCustomRejectReason] = useState("");

  const handleOpenRejectModal = (postId: number) => {
    setRejectingPostId(postId);
    setSelectedRejectReason(REJECT_REASONS[0]);
    setCustomRejectReason("");
    setRejectModalOpen(true);
  };
const [rejectConfirmLoading, setRejectConfirmLoading] = useState(false);

  // 提交拒绝
  const handleRejectConfirm = async () => {
    if (!rejectingPostId) return;

    let finalReason = selectedRejectReason;
    if (selectedRejectReason === "其他") {
      finalReason = customRejectReason.trim();
      if (!finalReason) {
        message.warning("请输入拒绝原因");
        return;
      }
    }
      setRejectConfirmLoading(true);
    try {
      await reviewPost({
        postId: rejectingPostId,
        status: PostStatus.REJECTED,
        rejectReason: finalReason,
      });
      message.success("已拒绝");
      setRejectModalOpen(false);
      fetchPosts(currentPage, statusFilter);
    } catch (error) {
      console.error("审核拒绝失败:", error);
      message.error("拒绝失败");
    } finally {
    setRejectConfirmLoading(false);
  };
  };
  // 列配置
  const columns: ColumnsType<Post> = [
    {
      title: "序号",
      key: "index",
      width: 80,
      render: (_, __, index) => (currentPage - 1) * pageSize + index + 1,
    },
    {
      title: "内容",
      dataIndex: "content",
      key: "content",
      width: 400,
      render: (text: string) => (
        <Tooltip title={text} placement="topLeft">
          <Typography.Paragraph
            ellipsis={{ rows: 2, expandable: false }}
            style={{ margin: 0, maxWidth: 380 }}
          >
            {text}
          </Typography.Paragraph>
        </Tooltip>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (status: string) => (
        <Tag
          color={
            statusMap[status as keyof typeof statusMap]?.color || "default"
          }
        >
          {statusMap[status as keyof typeof statusMap]?.text || status}
        </Tag>
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      width: 180,
      render: (time: string) => dayjs(time).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      fixed: "right",
      render: (_, record) => {
        // 审核下拉菜单项
        const auditItems: MenuProps["items"] = [
          {
            key: "approve",
            label: "通过",
            onClick: () => handleApprove(record.id,record.userId),
          },
          {
            key: "reject",
            label: "拒绝",
            onClick: () => handleOpenRejectModal(record.id,record.userId),
          },
        ];

        return (
          <Space size="middle">
            <Button
              icon={<EyeOutlined />}
              size="small"
              onClick={() => handleViewDetail(record.id)}
            >
              查看详情
            </Button>
            {record.status === PostStatus.PENDING && (
              <Dropdown menu={{ items: auditItems }} placement="bottomRight">
                <Button size="small">
                  审核 <DownOutlined />
                </Button>
              </Dropdown>
            )}
          </Space>
        );
      },
    },
  ];

  // 初始化加载
  useEffect(() => {
    fetchPosts(currentPage, statusFilter);
  }, []);
  const otherInputRef = useRef<InputRef>(null);

useEffect(() => {
  if (selectedRejectReason === '其他') {
    otherInputRef.current?.focus();
  }
}, [selectedRejectReason]);

  return (
    <div style={{ padding: "24px" }}>
      <div
        style={{
          marginBottom: "16px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <h2>帖子管理</h2>
        <Select
          placeholder="请选择状态筛选"
          style={{ width: 160 }}
          allowClear
          onChange={handleStatusChange}
          options={[
            { value: PostStatus.PENDING, label: "待审核" },
            { value: PostStatus.APPROVED, label: "已通过" },
            { value: PostStatus.REJECTED, label: "已拒绝" },
          ]}
        />
      </div>

      {/* 固定表格高度 + 内部滚动 */}
      <div
        style={{
          height: "calc(100vh - 310px)" /* 固定表格可视高度 */,
          overflow: "hidden",
          marginBottom: 16,
        }}
      >
        <Table
          columns={columns}
          dataSource={posts}
          rowKey="id"
          loading={loading}
          pagination={false}
          scroll={{
            x: 1000,
            y: "calc(100vh - 340px)" /* 表格内部滚动高度 */,
          }}
        />
      </div>

      <div style={{ marginTop: "16px", textAlign: "right" }}>
        <Pagination
          current={currentPage}
          pageSize={pageSize}
          total={total}
          showQuickJumper
          onChange={handlePaginationChange}
        />
      </div>
      {/* 详情弹窗 */}
      <Modal
        title="帖子详情"
        open={detailModalOpen}
        onCancel={handleModalClose}
        footer={null}
        width={600}
        confirmLoading={detailLoading} // 显示加载状态（可选）
      >
        {detailLoading ? (
          <div style={{ textAlign: "center", padding: "40px 0" }}>
            <Spin tip="加载中..." />
          </div>
        ) : currentPostDetail ? (
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <div>
              <Typography.Title level={5} style={{ margin: 0 }}>
                内容
              </Typography.Title>
              <Typography.Paragraph style={{ whiteSpace: "pre-wrap" }}>
                {currentPostDetail.content}
              </Typography.Paragraph>
            </div>

            {currentPostDetail.imageUrls &&
              currentPostDetail.imageUrls.length > 0 && (
                <div>
                  <Typography.Title level={5} style={{ margin: 0 }}>
                    图片
                  </Typography.Title>
                  <Image.PreviewGroup>
                    <Space size={[8, 8]} wrap>
                      {currentPostDetail.imageUrls.map((url, index) => (
                        <Image
                          key={index}
                          src={url}
                          alt={`图片 ${index + 1}`}
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
                </div>
              )}

            <div>
              <Typography.Text type="secondary">
                状态：
                {statusMap[currentPostDetail.status]?.text ||
                  currentPostDetail.status}{" "}
                • 创建时间：
                {dayjs(currentPostDetail.createTime).format(
                  "YYYY-MM-DD HH:mm:ss",
                )}
              </Typography.Text>
            </div>
          </div>
        ) : null}
      </Modal>
      {/* ========== 拒绝原因弹窗 ========== */}
<Modal
  title="请选择拒绝原因"
  open={rejectModalOpen}
  onCancel={() => setRejectModalOpen(false)}
  onOk={handleRejectConfirm}
  okText="确认拒绝"
  cancelText="取消"
>
  <Radio.Group
    value={selectedRejectReason}
    onChange={(e) => setSelectedRejectReason(e.target.value)}
    style={{ display: 'flex', flexDirection: 'column', gap: 12 }}
  >
    {REJECT_REASONS.map((reason) => (
      <Radio key={reason} value={reason}>
        {reason}
      </Radio>
    ))}
  </Radio.Group>

  {selectedRejectReason === '其他' && (
    <div style={{ marginTop: 16 }}>
      <Typography.Text type="secondary">请说明具体原因：</Typography.Text>
      <Input.TextArea
        value={customRejectReason}
        onChange={(e) => setCustomRejectReason(e.target.value)}
        placeholder="请输入拒绝原因..."
        autoSize={{ minRows: 2, maxRows: 4 }}
        style={{ marginTop: 8 }}
        ref={otherInputRef}
      />
    </div>
  )}
</Modal>
    </div>
  );
}
