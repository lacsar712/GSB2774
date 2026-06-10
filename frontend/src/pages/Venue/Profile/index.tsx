import { PageContainer, ProCard, ProDescriptions, ProForm, ProFormText, ProFormTimePicker } from '@ant-design/pro-components';
import { message } from 'antd';
import React, { useEffect, useState } from 'react';
import { getVenueProfile, updateVenueProfile } from '@/services/venue';

const Profile: React.FC = () => {
  const [venue, setVenue] = useState<API.Venue | null>(null);
  const [loading, setLoading] = useState(false);
  const [editMode, setEditMode] = useState(false);

  // 加载球馆信息
  const loadVenue = async () => {
    setLoading(true);
    try {
      const response = await getVenueProfile();
      setVenue(response.data);
    } catch (error: any) {
      message.error(error.message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVenue();
  }, []);

  // 更新球馆信息
  const handleUpdate = async (values: any) => {
    try {
      const data = {
        ...values,
        openTime: values.openTime?.format('HH:mm'),
        closeTime: values.closeTime?.format('HH:mm'),
      };

      await updateVenueProfile(data);
      message.success('更新成功');
      setEditMode(false);
      loadVenue();
    } catch (error: any) {
      message.error(error.message || '更新失败');
    }
  };

  if (!venue) {
    return <PageContainer loading={loading} />;
  }

  return (
    <PageContainer title="球馆资料" subTitle="维护对外展示信息与营业时间">
      <ProCard
        title="球馆信息"
        style={{ borderRadius: 14 }}
        extra={
          !editMode && (
            <a onClick={() => setEditMode(true)}>编辑</a>
          )
        }
      >
        {!editMode ? (
          <ProDescriptions column={2}>
            <ProDescriptions.Item label="球馆名称">{venue.name}</ProDescriptions.Item>
            <ProDescriptions.Item label="联系电话">{venue.phone}</ProDescriptions.Item>
            <ProDescriptions.Item label="地址" span={2}>{venue.address}</ProDescriptions.Item>
            <ProDescriptions.Item label="营业时间">
              {venue.openTime} - {venue.closeTime}
            </ProDescriptions.Item>
            <ProDescriptions.Item label="状态">
              {venue.status === 'enabled' ? '营业中' : '已停业'}
            </ProDescriptions.Item>
          </ProDescriptions>
        ) : (
          <ProForm
            onFinish={handleUpdate}
            initialValues={venue}
            submitter={{
              searchConfig: {
                submitText: '保存',
                resetText: '取消',
              },
              resetButtonProps: {
                onClick: () => setEditMode(false),
              },
            }}
          >
            <ProFormText
              name="name"
              label="球馆名称"
              rules={[{ required: true, message: '请输入球馆名称' }]}
            />
            <ProFormText
              name="address"
              label="地址"
              rules={[{ required: true, message: '请输入地址' }]}
            />
            <ProFormText
              name="phone"
              label="联系电话"
            />
            <ProFormTimePicker
              name="openTime"
              label="营业开始时间"
              fieldProps={{ format: 'HH:mm' }}
              rules={[{ required: true, message: '请选择营业开始时间' }]}
            />
            <ProFormTimePicker
              name="closeTime"
              label="营业结束时间"
              fieldProps={{ format: 'HH:mm' }}
              rules={[{ required: true, message: '请选择营业结束时间' }]}
            />
          </ProForm>
        )}
      </ProCard>
    </PageContainer>
  );
};

export default Profile;
