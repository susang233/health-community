// src/pages/Community/FolloweeListPage.tsx
import React, { useState, useEffect } from "react";
import { List, Avatar, Typography, Card, Spin, message, Button, Empty } from "antd";
import { UserOutlined } from "@ant-design/icons";
import { getFollowee } from "@/services/follow";
import type { FolloweePageVO } from "@/types/follow";
import type { UserVO } from "@/types/user";
import { useNavigate } from "react-router-dom";
const { Title, Text } = Typography;

export default function FolloweeListPage() {
  const [loading, setLoading] = useState(false);
  const [followees, setFollowees] = useState<UserVO[]>([]);
  const [page, setPage] = useState(1); 
  const [hasNext, setHasNext] = useState(true);
const navigate = useNavigate();
  const fetchData = async () => {
    if (!hasNext) return;
    setLoading(true);
    try {
      const res: FolloweePageVO = await getFollowee(page);
      if (page === 1) {
        setFollowees(res.followees || []);
      } else {
        setFollowees(prev => [...prev, ...(res.followees || [])]);
      }
      setHasNext(res.hasNext);
      setPage(page + 1);
    } catch (err) {
      message.error("加载关注失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return (
    <div style={{ maxWidth: 700, margin: "0 auto", padding: 24 }}>
      <Title level={3}>我的关注</Title>

      <Card>
        <Spin spinning={loading && followees.length === 0}>
          <List
         
            dataSource={followees}
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
                 
                />
              </List.Item>
            )}
          />


          {hasNext && followees.length > 0 && (
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