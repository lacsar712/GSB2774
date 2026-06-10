import { PlusOutlined } from '@ant-design/icons';
import { ActionType, ModalForm, PageContainer, ProColumns, ProFormSelect, ProFormText, ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Space, Tag } from 'antd';
import React, { useRef, useState } from 'react';
import { createVenueAdmin, deleteVenueAdmin, getVenueAdmins, getVenues, updateVenueAdmin } from '@/services/admin';

const VenueAdmins: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [updateModalVisible, setUpdateModalVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<API.VenueAdmin | null>(null);
  const [venues, setVenues] = useState<API.Venue[]>([]);

  // 加载球馆列表
  const loadVenues = async () => {
    try {
      const response = await getVenues();
      setVenues(response.data);
    } catch (error) {
      console.error('加载球馆列表失败:', error);
    }
  };

  // 创建管理员
  const handleCreate = async (values: API.CreateVenueAdminRequest) => {
    try {
      await createVenueAdmin(values);
      message.success('创建成功');
      setCreateModalVisible(false);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || '创建失败');
    }
  };

  // 更新管理员
  const handleUpdate = async (values: API.UpdateVenueAdminRequest) => {
    if (!currentRecord) return;

    try {
      await updateVenueAdmin(currentRecord.id, values);
      message.success('更新成功');
      setUpdateModalVisible(false);
      setCurrentRecord(null);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || '更新失败');
    }
  };

  // 删除管理员
  const handleDelete = async (id: number) => {
    try {
      await deleteVenueAdmin(id);
      message.success('删除成功');
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 启用/禁用
  const handleToggleStatus = async (record: API.VenueAdmin) => {
    const newStatus = record.status === 'enabled' ? 'disabled' : 'enabled';
    try {
      await updateVenueAdmin(record.id, { status: newStatus });
      message.success(newStatus === 'enabled' ? '已启用' : '已禁用');
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || '操作失败');
    }
  };

  // 重置密码
  const handleResetPassword = async (record: API.VenueAdmin) => {
    try {
      await updateVenueAdmin(record.id, { newPassword: '123456' });
      message.success('密码已重置为 123456');
    } catch (error: any) {
      message.error(error.message || '重置失败');
    }
  };

  const columns: ProColumns<API.VenueAdmin>[] = [
    {
      title: '编号',
      dataIndex: 'id',
      width: 80,
      search: false,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      copyable: true,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      search: false,
    },
    {
      title: '绑定球馆',
      dataIndex: 'venueName',
      search: false,
      render: (_, record) => record.venueName || <Tag color="default">未绑定</Tag>,
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
    {
      title: '操作',
      valueType: 'option',
      width: 250,
      render: (_, record) => [
        <a
          key="edit"
          onClick={() => {
            setCurrentRecord(record);
            setUpdateModalVisible(true);
            loadVenues();
          }}
        >
          编辑
        </a>,
        <a
          key="toggle"
          onClick={() => handleToggleStatus(record)}
        >
          {record.status === 'enabled' ? '禁用' : '启用'}
        </a>,
        <Popconfirm
          key="reset"
          title="确认重置密码为 123456？"
          onConfirm={() => handleResetPassword(record)}
        >
          <a>重置密码</a>
        </Popconfirm>,
        <Popconfirm
          key="delete"
          title="确认删除该管理员？"
          onConfirm={() => handleDelete(record.id)}
        >
          <a style={{ color: 'red' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <PageContainer title="球馆管理员管理" subTitle="维护管理员账号、状态与球馆绑定关系">
      <ProTable<API.VenueAdmin>
        headerTitle="球馆管理员列表"
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
              loadVenues();
            }}
          >
            新建管理员
          </Button>,
        ]}
        request={async (params) => {
          const response = await getVenueAdmins();
          return {
            data: response.data,
            success: true,
          };
        }}
        columns={columns}
      />

      {/* 创建管理员弹窗 */}
      <ModalForm
        title="新建球馆管理员"
        open={createModalVisible}
        onOpenChange={setCreateModalVisible}
        onFinish={handleCreate}
      >
        <ProFormText
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          rules={[
            { required: true, message: '请输入用户名' },
            { min: 3, max: 20, message: '用户名长度必须在3-20之间' },
          ]}
        />
        <ProFormText.Password
          name="password"
          label="密码"
          placeholder="请输入密码"
          rules={[
            { required: true, message: '请输入密码' },
            { min: 6, max: 20, message: '密码长度必须在6-20之间' },
          ]}
        />
        <ProFormText
          name="phone"
          label="手机号"
          placeholder="请输入手机号（可选）"
        />
        <ProFormSelect
          name="venueId"
          label="绑定球馆"
          placeholder="请选择球馆（可选）"
          options={venues.map((v) => ({ label: v.name, value: v.id }))}
        />
      </ModalForm>

      {/* 编辑管理员弹窗 */}
      <ModalForm
        title="编辑球馆管理员"
        open={updateModalVisible}
        onOpenChange={setUpdateModalVisible}
        onFinish={handleUpdate}
        initialValues={currentRecord || {}}
      >
        <ProFormSelect
          name="status"
          label="状态"
          options={[
            { label: '启用', value: 'enabled' },
            { label: '禁用', value: 'disabled' },
          ]}
        />
        <ProFormText.Password
          name="newPassword"
          label="新密码"
          placeholder="留空则不修改密码"
        />
        <ProFormSelect
          name="venueId"
          label="绑定球馆"
          placeholder="请选择球馆"
          options={venues.map((v) => ({ label: v.name, value: v.id }))}
        />
      </ModalForm>
    </PageContainer>
  );
};

export default VenueAdmins;
