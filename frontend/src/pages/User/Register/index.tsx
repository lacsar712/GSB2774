import { LockOutlined, MobileOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { message } from 'antd';
import React from 'react';
import { register, login } from '@/services/auth';

const Register: React.FC = () => {
  const { setInitialState } = useModel('@@initialState');

  const handleSubmit = async (values: API.RegisterRequest & { confirmPassword: string }) => {
    // 验证两次密码是否一致
    if (values.password !== values.confirmPassword) {
      message.error('两次输入的密码不一致！');
      return;
    }

    try {
      // 注册
      await register({
        username: values.username,
        password: values.password,
        phone: values.phone,
      });

      message.success('注册成功！正在自动登录...');

      // 自动登录
      const loginResponse = await login({
        username: values.username,
        password: values.password,
      });

      const { token, role, userId } = loginResponse.data;
      localStorage.setItem('token', token);

      await setInitialState((s) => ({
        ...s,
        currentUser: {
          userId,
          username: values.username,
          role,
        },
      }));

      const targetPath =
        role === 'SUPER_ADMIN'
          ? '/admin/dashboard'
          : role === 'VENUE_ADMIN'
            ? '/venue/dashboard'
            : '/app/home';

      // 整页跳转，确保刷新后权限和初始状态一致
      window.location.href = targetPath;
    } catch (error: any) {
      message.error(error.message || '注册失败');
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <LoginForm
          title="球馆预约管理系统"
          subTitle="创建账号，立即开启预约"
          submitter={{
            searchConfig: {
              submitText: '注册并登录',
            },
          }}
          onFinish={async (values) => {
            await handleSubmit(values as any);
          }}
          actions={
            <div style={{ textAlign: 'center' }}>
              已有账号？
              <a
                onClick={() => {
                  window.location.href = '/user/login';
                }}
              >
                立即登录
              </a>
            </div>
          }
        >
          <ProFormText
            name="username"
            fieldProps={{
              size: 'large',
              prefix: <UserOutlined />,
            }}
            placeholder="请输入用户名（3-20个字符）"
            rules={[
              {
                required: true,
                message: '请输入用户名！',
              },
              {
                min: 3,
                max: 20,
                message: '用户名长度必须在 3-20 之间！',
              },
            ]}
          />
          <ProFormText.Password
            name="password"
            fieldProps={{
              size: 'large',
              prefix: <LockOutlined />,
            }}
            placeholder="请输入密码（6-20个字符）"
            rules={[
              {
                required: true,
                message: '请输入密码！',
              },
              {
                min: 6,
                max: 20,
                message: '密码长度必须在 6-20 之间！',
              },
            ]}
          />
          <ProFormText.Password
            name="confirmPassword"
            fieldProps={{
              size: 'large',
              prefix: <LockOutlined />,
            }}
            placeholder="请再次输入密码"
            rules={[
              {
                required: true,
                message: '请再次输入密码！',
              },
            ]}
          />
          <ProFormText
            name="phone"
            fieldProps={{
              size: 'large',
              prefix: <MobileOutlined />,
            }}
            placeholder="请输入手机号（选填）"
          />
        </LoginForm>
      </div>
    </div>
  );
};

export default Register;
