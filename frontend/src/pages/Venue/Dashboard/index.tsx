import { PageContainer } from '@ant-design/pro-components';
import { Card, Col, Row, Skeleton, Statistic, message } from 'antd';
import React, { useEffect, useState } from 'react';
import { getDashboard } from '@/services/venue';

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState<API.VenueDashboardStats>();

  useEffect(() => {
    const loadDashboard = async () => {
      setLoading(true);
      try {
        const response = await getDashboard();
        setStats(response.data);
      } catch (error: any) {
        message.error(error.message || '加载仪表盘失败');
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, []);

  return (
    <PageContainer title="经营看板" subTitle={stats?.venueName || '球馆经营概览'}>
      <Skeleton loading={loading} active>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="场地总数" value={stats?.totalCourts ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="时段总数" value={stats?.totalSlots ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="今日时段数" value={stats?.todaySlots ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="订单总数" value={stats?.totalOrders ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="今日新增订单" value={stats?.todayOrders ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="待支付订单" value={stats?.pendingPayOrders ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="待核销订单" value={stats?.pendingVerificationOrders ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="已完成订单" value={stats?.completedOrders ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="累计成交额" value={stats?.totalRevenue ?? 0} precision={2} prefix="¥" />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="今日成交额" value={stats?.todayRevenue ?? 0} precision={2} prefix="¥" />
            </Card>
          </Col>
        </Row>
      </Skeleton>
    </PageContainer>
  );
};

export default Dashboard;
