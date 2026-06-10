import { PageContainer, ProCard, ProTable, ProForm, ProFormSelect, ProFormDateRangePicker } from '@ant-design/pro-components';
import { Button, message, Tag, Switch } from 'antd';
import React, { useRef, useState } from 'react';
import { getCourts, generateSlots, getSlots, updateSlot } from '@/services/venue';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import dayjs from 'dayjs';

const SlotsPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [selectedCourtId, setSelectedCourtId] = useState<number>();
  const [selectedDate, setSelectedDate] = useState<string>();

  // 生成时段
  const handleGenerate = async (values: any) => {
    try {
      const { courtId, dateRange } = values;
      await generateSlots({
        courtId,
        startDate: dayjs(dateRange[0]).format('YYYY-MM-DD'),
        endDate: dayjs(dateRange[1]).format('YYYY-MM-DD'),
      });
      message.success('时段生成成功');
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || '生成失败');
    }
  };

  // 更新时段状态
  const handleStatusChange = async (slotId: number, status: string) => {
    try {
      await updateSlot(slotId, { status });
      message.success('更新成功');
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || '更新失败');
    }
  };

  const columns: ProColumns<API.Slot>[] = [
    {
      title: '时段编号',
      dataIndex: 'id',
      width: 80,
      search: false,
    },
    {
      title: '场地',
      dataIndex: 'courtName',
      width: 150,
      search: false,
    },
    {
      title: '日期',
      dataIndex: 'slotDate',
      width: 120,
      search: false,
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      width: 100,
      search: false,
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      width: 100,
      search: false,
    },
    {
      title: '价格',
      dataIndex: 'price',
      width: 100,
      search: false,
      render: (price) => `¥${price}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      search: false,
      render: (_, record) => (
        <Tag color={record.status === 'available' ? 'green' : 'default'}>
          {record.status === 'available' ? '可用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '是否已预约',
      dataIndex: 'reserved',
      width: 120,
      search: false,
      render: (reserved) => (
        <Tag color={reserved ? 'red' : 'blue'}>
          {reserved ? '已预约' : '未预约'}
        </Tag>
      ),
    },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      search: false,
      render: (_, record) => (
        <Switch
          checked={record.status === 'available'}
          disabled={record.reserved}
          checkedChildren="启用"
          unCheckedChildren="禁用"
          onChange={(checked) => {
            handleStatusChange(record.id, checked ? 'available' : 'disabled');
          }}
        />
      ),
    },
  ];

  return (
    <PageContainer title="时段管理" subTitle="批量生成并维护可预约时段">
      <ProCard title="生成时段" style={{ marginBottom: 16 }}>
        <ProForm
          layout="horizontal"
          onFinish={handleGenerate}
          submitter={{
            searchConfig: {
              submitText: '生成时段',
            },
            resetButtonProps: {
              style: { display: 'none' },
            },
          }}
        >
          <ProFormSelect
            name="courtId"
            label="选择场地"
            width="md"
            request={async () => {
              try {
                const response = await getCourts();
                return response.data.map((court: API.Court) => ({
                  label: `${court.name} (${court.type})`,
                  value: court.id,
                }));
              } catch (error) {
                return [];
              }
            }}
            rules={[{ required: true, message: '请选择场地' }]}
            fieldProps={{
              onChange: (value) => setSelectedCourtId(value as number),
            }}
          />
          <ProFormDateRangePicker
            name="dateRange"
            label="日期范围"
            width="md"
            rules={[{ required: true, message: '请选择日期范围' }]}
            fieldProps={{
              disabledDate: (current) => {
                return current && current < dayjs().startOf('day');
              },
            }}
          />
        </ProForm>
      </ProCard>

      <ProCard title="时段列表">
        <ProTable<API.Slot>
          columns={columns}
          actionRef={actionRef}
          request={async (params) => {
            if (!selectedCourtId) {
              return {
                data: [],
                success: true,
              };
            }
            try {
              const response = await getSlots(selectedCourtId, selectedDate);
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
            pageSize: 20,
          }}
          toolbar={{
            title: selectedCourtId ? '请选择日期查看时段' : '请先选择场地',
            actions: [
              <ProFormSelect
                key="date"
                name="date"
                placeholder="选择日期"
                width="md"
                request={async () => {
                  const dates = [];
                  for (let i = 0; i < 7; i++) {
                    const date = dayjs().add(i, 'day');
                    dates.push({
                      label: date.format('YYYY-MM-DD (ddd)'),
                      value: date.format('YYYY-MM-DD'),
                    });
                  }
                  return dates;
                }}
                fieldProps={{
                  onChange: (value) => {
                    setSelectedDate(value as string);
                    actionRef.current?.reload();
                  },
                }}
              />,
            ],
          }}
        />
      </ProCard>
    </PageContainer>
  );
};

export default SlotsPage;
