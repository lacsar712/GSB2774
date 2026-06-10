import { PageContainer } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Modal, Space, Tag } from 'antd';
import React, { useRef } from 'react';
import { history } from '@umijs/max';
import { getMyOrders, payOrder, cancelOrder } from '@/services/app';
import type { ActionType, ProColumns } from '@ant-design/pro-components';

const Orders: React.FC = () => {
  const actionRef = useRef<ActionType>();

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
  const handlePay = async (orderId: number) => {
    Modal.confirm({
      title: '确认支付',
      content: '确认支付该订单吗？',
      onOk: async () => {
        try {
          await payOrder(orderId);
          message.success('支付成功');
          actionRef.current?.reload();
        } catch (error: any) {
          message.error(error.message || '支付失败');
        }
      },
    });
  };

  // 取消订单
  const handleCancel = async (orderId: number) => {
    Modal.confirm({
      title: '确认取消',
      content: '确认取消该订单吗？取消后将释放该时段。',
      onOk: async () => {
        try {
          await cancelOrder(orderId);
          message.success('取消成功');
          actionRef.current?.reload();
        } catch (error: any) {
          message.error(error.message || '取消失败');
        }
      },
    });
  };

  // 查看订单详情
  const handleViewDetail = (orderId: number) => {
    history.push(`/app/orders/${orderId}`);
  };

  const columns: ProColumns<API.Order>[] = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      width: 180,
      copyable: true,
    },
    {
      title: '球馆',
      dataIndex: 'venueName',
      width: 150,
    },
    {
      title: '场地',
      dataIndex: 'courtName',
      width: 120,
    },
    {
      title: '预约日期',
      dataIndex: 'slotDate',
      width: 120,
    },
    {
      title: '预约时间',
      width: 150,
      render: (_, record) => `${record.startTime} - ${record.endTime}`,
    },
    {
      title: '金额',
      dataIndex: 'amount',
      width: 100,
      render: (amount) => <span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>¥{amount}</span>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      filters: true,
      onFilter: true,
      valueEnum: {
        PENDING_PAY: { text: '待支付', status: 'Warning' },
        PAID: { text: '已支付', status: 'Processing' },
        COMPLETED: { text: '已完成', status: 'Success' },
        CANCELED: { text: '已取消', status: 'Default' },
      },
      render: (_, record) => getStatusTag(record.status),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      width: 180,
      valueType: 'dateTime',
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          {record.status === 'PENDING_PAY' && (
            <>
              <Button
                type="primary"
                size="small"
                onClick={() => handlePay(record.id)}
                data-testid={`app-order-pay-${record.id}`}
              >
                支付
              </Button>
              <Button
                size="small"
                onClick={() => handleCancel(record.id)}
                data-testid={`app-order-cancel-${record.id}`}
              >
                取消
              </Button>
            </>
          )}
          <Button
            type="link"
            size="small"
            onClick={() => handleViewDetail(record.id)}
            data-testid={`app-order-detail-${record.id}`}
          >
            详情
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer title="我的订单">
      <ProTable<API.Order>
        columns={columns}
        actionRef={actionRef}
        request={async (params) => {
          try {
            const response = await getMyOrders(params.status);
            return {
              data: response.data,
              success: true,
            };
          } catch (error: any) {
            message.error(error.message || '加载失败');
            return {
              data: [],
              success: false,
            };
          }
        }}
        rowKey="id"
        search={false}
        pagination={{
          pageSize: 10,
        }}
        cardBordered
        scroll={{ x: 1200 }}
      />
    </PageContainer>
  );
};

export default Orders;
