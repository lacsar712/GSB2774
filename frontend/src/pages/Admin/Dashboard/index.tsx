import { PageContainer } from '@ant-design/pro-components';
import { Card, Col, Row, Skeleton, Statistic, message } from 'antd';
import React, { useEffect, useState } from 'react';
import { getDashboard } from '@/services/admin';

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState<API.AdminDashboardStats>();

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
    <PageContainer title="平台概览" subTitle="核心运营指标总览">
      <Skeleton loading={loading} active>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="球馆总数" value={stats?.totalVenues ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="球馆管理员数" value={stats?.totalVenueAdmins ?? 0} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <Card style={{ borderRadius: 14 }}>
              <Statistic title="普通用户数" value={stats?.totalUsers ?? 0} />
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
              <Statistic title="已支付订单" value={stats?.paidOrders ?? 0} />
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
