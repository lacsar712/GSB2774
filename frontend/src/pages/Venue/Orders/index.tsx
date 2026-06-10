import { PageContainer, ProTable } from '@ant-design/pro-components';
import { Button, message, Tag, Modal } from 'antd';
import React, { useRef } from 'react';
import { getOrders, completeOrder } from '@/services/venue';
import type { ActionType, ProColumns } from '@ant-design/pro-components';

const OrdersPage: React.FC = () => {
  const actionRef = useRef<ActionType>();

  // 核销订单
  const handleComplete = async (orderId: number) => {
    Modal.confirm({
      title: '确认核销',
      content: '确认核销该订单吗？核销后订单状态将变为已完成。',
      onOk: async () => {
        try {
          await completeOrder(orderId);
          message.success('核销成功');
          actionRef.current?.reload();
        } catch (error: any) {
          message.error(error.message || '核销失败');
        }
      },
    });
  };

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

  const columns: ProColumns<API.Order>[] = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      width: 180,
      copyable: true,
    },
    {
      title: '用户',
      dataIndex: 'username',
      width: 120,
    },
    {
      title: '场地',
      dataIndex: 'courtName',
      width: 150,
    },
    {
      title: '预约日期',
      dataIndex: 'slotDate',
      width: 120,
      valueType: 'date',
    },
    {
      title: '预约时间',
      width: 150,
      search: false,
      render: (_, record) => `${record.startTime} - ${record.endTime}`,
    },
    {
      title: '金额',
      dataIndex: 'amount',
      width: 100,
      search: false,
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
      search: false,
    },
    {
      title: '支付时间',
      dataIndex: 'paidAt',
      width: 180,
      valueType: 'dateTime',
      search: false,
      hideInTable: true,
    },
    {
      title: '完成时间',
      dataIndex: 'completedAt',
      width: 180,
      valueType: 'dateTime',
      search: false,
      hideInTable: true,
    },
    {
      title: '操作',
      width: 120,
      fixed: 'right',
      search: false,
      render: (_, record) => {
        if (record.status === 'PAID') {
          return (
            <Button
              type="primary"
              size="small"
              data-testid={`venue-complete-order-${record.id}`}
              onClick={() => handleComplete(record.id)}
            >
              核销
            </Button>
          );
        }
        return <span style={{ color: '#999' }}>-</span>;
      },
    },
  ];

  return (
    <PageContainer title="订单管理" subTitle="跟进订单支付状态并及时核销">
      <ProTable<API.Order>
        columns={columns}
        actionRef={actionRef}
        request={async (params) => {
          try {
            const response = await getOrders({
              status: params.status,
              date: params.slotDate,
              courtId: params.courtId,
            });
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
        search={{
          labelWidth: 'auto',
        }}
        pagination={{
          pageSize: 10,
        }}
        cardBordered
        scroll={{ x: 1400 }}
      />
    </PageContainer>
  );
};

export default OrdersPage;
