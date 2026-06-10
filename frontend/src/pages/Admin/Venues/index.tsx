import { PlusOutlined } from '@ant-design/icons';
import { ActionType, ModalForm, PageContainer, ProColumns, ProFormSelect, ProFormText, ProFormTimePicker, ProTable } from '@ant-design/pro-components';
import { Button, message, Tag } from 'antd';
import React, { useRef, useState } from 'react';
import { createVenue, getVenueAdmins, getVenues } from '@/services/admin';

const Venues: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [venueAdmins, setVenueAdmins] = useState<API.VenueAdmin[]>([]);

  // 加载球馆管理员列表
  const loadVenueAdmins = async () => {
    try {
      const response = await getVenueAdmins();
      setVenueAdmins(response.data);
    } catch (error) {
      console.error('加载管理员列表失败:', error);
    }
  };

  // 创建球馆
  const handleCreate = async (values: any) => {
    try {
      // 转换时间格式
      const data: API.CreateVenueRequest = {
        ...values,
        openTime: values.openTime.format('HH:mm'),
        closeTime: values.closeTime.format('HH:mm'),
      };

      await createVenue(data);
      message.success('创建成功');
      setCreateModalVisible(false);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || '创建失败');
    }
  };

  const columns: ProColumns<API.Venue>[] = [
    {
      title: '编号',
      dataIndex: 'id',
      width: 80,
      search: false,
    },
    {
      title: '球馆名称',
      dataIndex: 'name',
      copyable: true,
    },
    {
      title: '地址',
      dataIndex: 'address',
      search: false,
      ellipsis: true,
    },
    {
      title: '联系电话',
      dataIndex: 'phone',
      search: false,
    },
    {
      title: '营业时间',
      search: false,
      render: (_, record) => `${record.openTime} - ${record.closeTime}`,
    },
    {
      title: '管理员',
      dataIndex: 'ownerUsername',
      search: false,
      render: (_, record) => record.ownerUsername || <Tag color="default">未分配</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: {
        enabled: { text: '启用', status: 'Success' },
        disabled: { text: '禁用', status: 'Error' },
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      valueType: 'dateTime',
      search: false,
    },
  ];

  return (
    <PageContainer title="球馆管理" subTitle="新增并维护平台球馆基础信息">
      <ProTable<API.Venue>
        headerTitle="球馆列表"
        actionRef={actionRef}
        rowKey="id"
        search={{
          labelWidth: 120,
        }}
        cardBordered
        toolBarRender={() => [
          <Button
            key="create"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setCreateModalVisible(true);
              loadVenueAdmins();
            }}
          >
            新建球馆
          </Button>,
        ]}
        request={async (params) => {
          const response = await getVenues();
          return {
            data: response.data,
            success: true,
          };
        }}
        columns={columns}
      />

      {/* 创建球馆弹窗 */}
      <ModalForm
        title="新建球馆"
        open={createModalVisible}
        onOpenChange={setCreateModalVisible}
        onFinish={handleCreate}
        width={600}
      >
        <ProFormText
          name="name"
          label="球馆名称"
          placeholder="请输入球馆名称"
          rules={[{ required: true, message: '请输入球馆名称' }]}
        />
        <ProFormText
          name="address"
          label="地址"
          placeholder="请输入地址"
          rules={[{ required: true, message: '请输入地址' }]}
        />
        <ProFormText
          name="phone"
          label="联系电话"
          placeholder="请输入联系电话"
        />
        <ProFormTimePicker
          name="openTime"
          label="营业开始时间"
          placeholder="请选择营业开始时间"
          fieldProps={{
            format: 'HH:mm',
          }}
          rules={[{ required: true, message: '请选择营业开始时间' }]}
        />
        <ProFormTimePicker
          name="closeTime"
          label="营业结束时间"
          placeholder="请选择营业结束时间"
          fieldProps={{
            format: 'HH:mm',
          }}
          rules={[{ required: true, message: '请选择营业结束时间' }]}
        />
        <ProFormSelect
          name="ownerUserId"
          label="球馆管理员"
          placeholder="请选择球馆管理员"
          options={venueAdmins.map((admin) => ({
            label: `${admin.username}${admin.venueName ? ` (已绑定: ${admin.venueName})` : ''}`,
            value: admin.id,
          }))}
          rules={[{ required: true, message: '请选择球馆管理员' }]}
        />
      </ModalForm>
    </PageContainer>
  );
};

export default Venues;
