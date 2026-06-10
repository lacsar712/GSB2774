import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Button, message } from 'antd';
import React, { useState } from 'react';
import { login } from '@/services/auth';

const roleTitleMap: Record<string, string> = {
  SUPER_ADMIN: '平台管理员后台',
  VENUE_ADMIN: '球馆管理后台',
  USER: '用户预约中心',
};

const quickLoginAccounts = [
  { username: 'admin', label: '管理员端' },
  { username: 'venue_admin1', label: '球馆端' },
  { username: 'user1', label: '用户端' },
];

const Login: React.FC = () => {
  const { setInitialState } = useModel('@@initialState');
  const [quickLoginLoading, setQuickLoginLoading] = useState<string>('');

  const handleSubmit = async (values: API.LoginRequest) => {
    try {
      const response = await login(values);
      const { token, role, userId } = response.data;

      // 保存 token 到 localStorage
      localStorage.setItem('token', token);

      message.success('登录成功！');

      // 更新全局状态
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
      message.error(error.message || '登录失败');
    }
  };

  const handleQuickLogin = async (username: string) => {
    setQuickLoginLoading(username);
    try {
      await handleSubmit({
        username,
        password: '123456',
      });
    } finally {
      setQuickLoginLoading('');
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <LoginForm
          title="球馆预约管理系统"
          subTitle="欢迎登录，开启便捷预约"
          submitter={{
            searchConfig: {
              submitText: '登录',
            },
            submitButtonProps: {
              'data-testid': 'login-submit',
            },
          }}
          onFinish={async (values) => {
            await handleSubmit(values as API.LoginRequest);
          }}
          actions={
            <div style={{ textAlign: 'center' }}>
              还没有账号？
              <a
                onClick={() => {
                  window.location.href = '/user/register';
                }}
              >
                立即注册
              </a>
            </div>
          }
        >
          <ProFormText
            name="username"
            fieldProps={{
              size: 'large',
              prefix: <UserOutlined />,
              'data-testid': 'login-username',
            }}
            placeholder="请输入用户名"
            rules={[
              {
                required: true,
                message: '请输入用户名！',
              },
            ]}
          />
          <ProFormText.Password
            name="password"
            fieldProps={{
              size: 'large',
              prefix: <LockOutlined />,
              'data-testid': 'login-password',
            }}
            placeholder="请输入密码"
            rules={[
              {
                required: true,
                message: '请输入密码！',
              },
            ]}
          />

          <div style={{ marginTop: -6, fontSize: 12, color: '#8c8c8c' }}>
            登录后将自动进入对应页面（{Object.values(roleTitleMap).join(' / ')}）
          </div>

          <div className="quick-login-wrap">
            <div className="quick-login-title">快捷登录</div>
            <div className="quick-login-buttons">
              {quickLoginAccounts.map((account) => (
                <Button
                  key={account.username}
                  onClick={() => handleQuickLogin(account.username)}
                  loading={quickLoginLoading === account.username}
                  data-testid={`quick-login-${account.username}`}
                >
                  {account.label}
                </Button>
              ))}
            </div>
            <div className="quick-login-account-hint">账号：admin / venue_admin1 / user1（默认密码 123456）</div>
          </div>
        </LoginForm>
      </div>
    </div>
  );
};

export default Login;
