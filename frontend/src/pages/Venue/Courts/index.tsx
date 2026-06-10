import { PageContainer, ProTable, ModalForm, ProFormText, ProFormDigit, ProFormSelect } from '@ant-design/pro-components';
import { Button, message, Popconfirm } from 'antd';
import React, { useRef } from 'react';
import { getCourts, createCourt, updateCourt, deleteCourt } from '@/services/venue';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PlusOutlined } from '@ant-design/icons';

const CourtsPage: React.FC = () => {
  const actionRef = useRef<ActionType>();

  const columns: ProColumns<API.Court>[] = [
    {
      title: '编号',
      dataIndex: 'id',
      width: 80,
      search: false,
    },
    {
      title: '场地名称',
      dataIndex: 'name',
      width: 150,
    },
    {
      title: '类型',
      dataIndex: 'type',
      width: 120,
      valueEnum: {
        '羽毛球': { text: '羽毛球' },
        '篮球': { text: '篮球' },
        '网球': { text: '网球' },
        '乒乓球': { text: '乒乓球' },
        '足球': { text: '足球' },
      },
    },
    {
      title: '价格（元/小时）',
      dataIndex: 'pricePerSlot',
      width: 150,
      search: false,
      render: (price) => `¥${price}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      valueEnum: {
        enabled: { text: '启用', status: 'Success' },
        disabled: { text: '禁用', status: 'Default' },
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      width: 180,
      valueType: 'dateTime',
      search: false,
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      search: false,
      render: (_, record) => [
        <ModalForm
          key="edit"
          title="编辑场地"
          trigger={<a>编辑</a>}
          initialValues={record}
          onFinish={async (values) => {
            try {
              await updateCourt(record.id, values);
              message.success('更新成功');
              actionRef.current?.reload();
              return true;
            } catch (error: any) {
              message.error(error.message || '更新失败');
              return false;
            }
          }}
        >
          <ProFormText
            name="name"
            label="场地名称"
            rules={[{ required: true, message: '请输入场地名称' }]}
          />
          <ProFormSelect
            name="type"
            label="类型"
            options={[
              { label: '羽毛球', value: '羽毛球' },
              { label: '篮球', value: '篮球' },
              { label: '网球', value: '网球' },
              { label: '乒乓球', value: '乒乓球' },
              { label: '足球', value: '足球' },
            ]}
            rules={[{ required: true, message: '请选择类型' }]}
          />
          <ProFormDigit
            name="pricePerSlot"
            label="价格（元/小时）"
            min={0}
            fieldProps={{ precision: 2 }}
            rules={[{ required: true, message: '请输入价格' }]}
          />
          <ProFormSelect
            name="status"
            label="状态"
            options={[
              { label: '启用', value: 'enabled' },
              { label: '禁用', value: 'disabled' },
            ]}
            rules={[{ required: true, message: '请选择状态' }]}
          />
        </ModalForm>,
        <Popconfirm
          key="delete"
          title="确认删除"
          description="删除后将无法恢复，确认删除吗？"
          onConfirm={async () => {
            try {
              await deleteCourt(record.id);
              message.success('删除成功');
              actionRef.current?.reload();
            } catch (error: any) {
              message.error(error.message || '删除失败');
            }
          }}
        >
          <a style={{ color: 'red' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <PageContainer title="场地管理" subTitle="统一维护场地类型、价格与开放状态">
      <ProTable<API.Court>
        columns={columns}
        actionRef={actionRef}
        request={async () => {
          try {
            const response = await getCourts();
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
        cardBordered
        pagination={{
          pageSize: 10,
        }}
        toolBarRender={() => [
          <ModalForm
            key="create"
            title="新增场地"
            trigger={
              <Button type="primary" icon={<PlusOutlined />}>
                新增场地
              </Button>
            }
            onFinish={async (values) => {
              try {
                await createCourt(values);
                message.success('创建成功');
                actionRef.current?.reload();
                return true;
              } catch (error: any) {
                message.error(error.message || '创建失败');
                return false;
              }
            }}
          >
            <ProFormText
              name="name"
              label="场地名称"
              placeholder="例如：1号场地"
              rules={[{ required: true, message: '请输入场地名称' }]}
            />
            <ProFormSelect
              name="type"
              label="类型"
              options={[
                { label: '羽毛球', value: '羽毛球' },
                { label: '篮球', value: '篮球' },
                { label: '网球', value: '网球' },
                { label: '乒乓球', value: '乒乓球' },
                { label: '足球', value: '足球' },
              ]}
              rules={[{ required: true, message: '请选择类型' }]}
            />
            <ProFormDigit
              name="pricePerSlot"
              label="价格（元/小时）"
              min={0}
              fieldProps={{ precision: 2 }}
              placeholder="例如：100.00"
              rules={[{ required: true, message: '请输入价格' }]}
            />
          </ModalForm>,
        ]}
      />
    </PageContainer>
  );
};

export default CourtsPage;
