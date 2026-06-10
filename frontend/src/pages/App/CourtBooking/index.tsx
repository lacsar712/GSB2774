import { PageContainer, ProCard } from '@ant-design/pro-components';
import { Button, DatePicker, Empty, message, Modal, Space, Tag, Timeline } from 'antd';
import React, { useEffect, useState } from 'react';
import { history, useParams } from '@umijs/max';
import { getCourtSlots, createOrder } from '@/services/app';
import { ClockCircleOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';

const CourtBooking: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [slots, setSlots] = useState<API.Slot[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs());
  const [submitting, setSubmitting] = useState(false);

  // 加载时段列表
  const loadSlots = async (date: Dayjs) => {
    if (!id) return;
    setLoading(true);
    try {
      const response = await getCourtSlots(Number(id), date.format('YYYY-MM-DD'));
      setSlots(response.data);
    } catch (error: any) {
      message.error(error.message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSlots(selectedDate);
  }, [id, selectedDate]);

  // 日期变化
  const handleDateChange = (date: Dayjs | null) => {
    if (date) {
      setSelectedDate(date);
    }
  };

  // 预约时段
  const handleBookSlot = (slot: API.Slot) => {
    if (slot.reserved) {
      message.warning('该时段已被预约');
      return;
    }

    Modal.confirm({
      title: '确认预约',
      content: (
        <div>
          <p>场地：{slot.courtName}</p>
          <p>日期：{slot.slotDate}</p>
          <p>时间：{slot.startTime} - {slot.endTime}</p>
          <p>价格：¥{slot.price}</p>
        </div>
      ),
      onOk: async () => {
        setSubmitting(true);
        try {
          const response = await createOrder({ slotId: slot.id });
          message.success('预约成功！');
          // 跳转到订单详情页
          history.push(`/app/orders/${response.data.id}`);
        } catch (error: any) {
          if (error.code === 3002) {
            message.error('该时段已被其他用户预约，请选择其他时段');
            // 重新加载时段列表
            loadSlots(selectedDate);
          } else {
            message.error(error.message || '预约失败');
          }
        } finally {
          setSubmitting(false);
        }
      },
    });
  };

  // 获取时段状态标签
  const getSlotStatusTag = (slot: API.Slot) => {
    if (slot.reserved) {
      return <Tag color="red" icon={<CloseCircleOutlined />}>已预约</Tag>;
    }
    if (slot.status === 'available') {
      return <Tag color="green" icon={<CheckCircleOutlined />}>可预约</Tag>;
    }
    return <Tag color="default">不可用</Tag>;
  };

  return (
    <PageContainer
      title="场地预约"
      subTitle="选择合适日期与时段完成预约"
      onBack={() => history.back()}
    >
      <ProCard style={{ marginBottom: 16 }}>
        <Space>
          <span>选择日期：</span>
          <DatePicker
            value={selectedDate}
            onChange={handleDateChange}
            disabledDate={(current) => current && current < dayjs().startOf('day')}
            format="YYYY-MM-DD"
          />
        </Space>
      </ProCard>

      <ProCard title={`可预约时段 (${selectedDate.format('YYYY-MM-DD')})`} loading={loading}>
        {slots.length === 0 ? (
          <Empty description="暂无可预约时段" />
        ) : (
          <Timeline
            items={slots.map((slot) => ({
              color: slot.reserved ? 'red' : slot.status === 'available' ? 'green' : 'gray',
              dot: <ClockCircleOutlined />,
              children: (
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 0',
                  }}
                >
                  <div>
                    <Space>
                      <span style={{ fontSize: 16, fontWeight: 'bold' }}>
                        {slot.startTime} - {slot.endTime}
                      </span>
                      {getSlotStatusTag(slot)}
                    </Space>
                    <div style={{ marginTop: 4, color: '#666' }}>
                      价格：<span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>¥{slot.price}</span>
                    </div>
                  </div>
                  <Button
                    type="primary"
                    data-testid={`book-slot-${slot.id}`}
                    disabled={slot.reserved || slot.status !== 'available' || submitting}
                    onClick={() => handleBookSlot(slot)}
                  >
                    {slot.reserved ? '已预约' : '立即预约'}
                  </Button>
                </div>
              ),
            }))}
          />
        )}
      </ProCard>
    </PageContainer>
  );
};

export default CourtBooking;
