import { PageContainer, ProCard, ProDescriptions } from '@ant-design/pro-components';
import { Button, message, Modal, Space, Tag } from 'antd';
import React, { useEffect, useState } from 'react';
import { history, useParams } from '@umijs/max';
import { getOrderDetail, payOrder, cancelOrder } from '@/services/app';

const OrderDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<API.Order | null>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // 加载订单详情
  const loadOrder = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const response = await getOrderDetail(Number(id));
      setOrder(response.data);
    } catch (error: any) {
      message.error(error.message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrder();
  }, [id]);

  // 获取订单状态标签
  const getStatusTag = (status: string) => {
    const statusMap: Record<string, { color: string; text: string }> = {
      PENDING_PAY: { color: 'orange', text: '待支付' },
      PAID: { color: 'blue', text: '已支付' },
      COMPLETED: { color: 'green', text: '已完成' },
      CANCELED: { color: 'default', text: '已取消' },
    };
    const config = statusMap[status] || { color: 'default', text: status };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 支付订单
  const handlePay = async () => {
    if (!order) return;
    Modal.confirm({
      title: '确认支付',
      content: `确认支付 ¥${order.amount} 吗？`,
      onOk: async () => {
        setSubmitting(true);
        try {
          await payOrder(order.id);
          message.success('支付成功');
          loadOrder();
        } catch (error: any) {
          message.error(error.message || '支付失败');
        } finally {
          setSubmitting(false);
        }
      },
    });
  };

  // 取消订单
  const handleCancel = async () => {
    if (!order) return;
    Modal.confirm({
      title: '确认取消',
      content: '确认取消该订单吗？取消后将释放该时段。',
      onOk: async () => {
        setSubmitting(true);
        try {
          await cancelOrder(order.id);
          message.success('取消成功');
          loadOrder();
        } catch (error: any) {
          message.error(error.message || '取消失败');
        } finally {
          setSubmitting(false);
        }
      },
    });
  };

  if (!order) {
    return <PageContainer loading={loading} />;
  }

  return (
    <PageContainer
      title="订单详情"
      subTitle="核对订单与预约时段信息"
      onBack={() => history.back()}
      extra={
        order.status === 'PENDING_PAY' && (
          <Space>
            <Button type="primary" onClick={handlePay} loading={submitting} data-testid="order-pay-btn">
              立即支付
            </Button>
            <Button onClick={handleCancel} loading={submitting} data-testid="order-cancel-btn">
              取消订单
            </Button>
          </Space>
        )
      }
    >
      <ProCard title="订单信息" style={{ marginBottom: 16 }}>
        <ProDescriptions column={2}>
          <ProDescriptions.Item label="订单号" copyable>
            {order.orderNo}
          </ProDescriptions.Item>
          <ProDescriptions.Item label="订单状态">
            <span data-testid="order-status">{getStatusTag(order.status)}</span>
          </ProDescriptions.Item>
          <ProDescriptions.Item label="球馆">{order.venueName}</ProDescriptions.Item>
          <ProDescriptions.Item label="场地">{order.courtName}</ProDescriptions.Item>
          <ProDescriptions.Item label="预约日期">{order.slotDate}</ProDescriptions.Item>
          <ProDescriptions.Item label="预约时间">
            {order.startTime} - {order.endTime}
          </ProDescriptions.Item>
          <ProDescriptions.Item label="订单金额">
            <span style={{ color: '#ff4d4f', fontSize: 18, fontWeight: 'bold' }}>
              ¥{order.amount}
            </span>
          </ProDescriptions.Item>
          <ProDescriptions.Item label="创建时间">{order.createdAt}</ProDescriptions.Item>
          {order.paidAt && (
            <ProDescriptions.Item label="支付时间">{order.paidAt}</ProDescriptions.Item>
          )}
          {order.completedAt && (
            <ProDescriptions.Item label="完成时间">{order.completedAt}</ProDescriptions.Item>
          )}
          {order.canceledAt && (
            <ProDescriptions.Item label="取消时间">{order.canceledAt}</ProDescriptions.Item>
          )}
        </ProDescriptions>
      </ProCard>

      {order.status === 'PENDING_PAY' && (
        <ProCard>
          <div style={{ textAlign: 'center', padding: '24px 0' }}>
            <p style={{ fontSize: 16, marginBottom: 16 }}>
              请在 30 分钟内完成支付，超时订单将自动取消
            </p>
            <Space size="large">
              <Button type="primary" size="large" onClick={handlePay} loading={submitting}>
                立即支付 ¥{order.amount}
              </Button>
              <Button size="large" onClick={handleCancel} loading={submitting}>
                取消订单
              </Button>
            </Space>
          </div>
        </ProCard>
      )}
    </PageContainer>
  );
};

export default OrderDetail;
