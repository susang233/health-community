// src/pages/Community/FollowerListPage.tsx
import React, { useState, useEffect } from "react";
import { List, Avatar, Typography, Card, Spin, message, Button, Empty } from "antd";
import { UserOutlined } from "@ant-design/icons";
import { getFollower } from "@/services/follow";
import type { FollowerPageVO } from "@/types/follow";
import type { UserVO } from "@/types/user";
import { useNavigate } from "react-router-dom";
const { Title, Text } = Typography;

export default function FollowerListPage() {
  const [loading, setLoading] = useState(false);
  const [followers, setFollowers] = useState<UserVO[]>([]);
  const [page, setPage] = useState(1); // 改为从第 1 页开始
  const [hasNext, setHasNext] = useState(true);
const navigate = useNavigate();
  const fetchData = async () => {
    if (!hasNext) return;
    setLoading(true);
    try {
      const res: FollowerPageVO = await getFollower(page);
      if (page === 1) {
        setFollowers(res.followers || []);
      } else {
        setFollowers(prev => [...prev, ...(res.followers || [])]);
      }
      setHasNext(res.hasNext);
      setPage(page + 1);
    } catch (err) {
      message.error("加载粉丝失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return (
    <div style={{ maxWidth: 700, margin: "0 auto", padding: 24 }}>
      <Title level={3}>我的粉丝</Title>

      <Card>
        <Spin spinning={loading && followers.length === 0}>
          <List
            dataSource={followers}
            renderItem={(item) => (
               <List.Item
                             style={{ cursor: "pointer" }}
                              onClick={() => {
                                
                                 navigate(`/dashboard/community/user/${item.userId}`);
                              }}
                            >
                <List.Item.Meta
                  avatar={<Avatar src={item.avatarUrl} icon={<UserOutlined />} />}
                  title={item.nickName || "用户"}
                  description={<Text type="secondary">ID: {item.userId}</Text>}
                />
              </List.Item>
            )}
          />


          {hasNext && followers.length > 0 && (
            <div style={{ textAlign: "center", marginTop: 16 }}>
              <Button onClick={fetchData} loading={loading}>
                加载更多
              </Button>
            </div>
          )}
        </Spin>
      </Card>
    </div>
  );
}