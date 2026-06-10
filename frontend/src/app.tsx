import { history, RunTimeLayoutConfig } from '@umijs/max';
import { message } from 'antd';
import './global.less';

const roleLabelMap: Record<string, string> = {
  SUPER_ADMIN: '平台管理员',
  VENUE_ADMIN: '球馆管理员',
  USER: '普通用户',
};

// 全局初始化数据配置
export async function getInitialState(): Promise<{ currentUser?: API.CurrentUser }> {
  const token = localStorage.getItem('token');

  if (token) {
    try {
      // 获取当前用户信息
      const response = await fetch('/api/auth/me', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        return {
          currentUser: data.data,
        };
      }
    } catch (error) {
      console.error('Failed to fetch user info:', error);
    }
  }

  return {};
}

// 运行时配置
export const layout: RunTimeLayoutConfig = ({ initialState }) => {
  const currentUser = initialState?.currentUser;

  return {
    logo: 'https://img.alicdn.com/tfs/TB1YHEpwUT1gK0jSZFhXXaAtVXa-28-27.svg',
    menu: {
      locale: false,
    },
    rightContentRender: false,
    menuFooterRender: (props) => {
      if (!currentUser) {
        return null;
      }

      const roleLabel = roleLabelMap[currentUser.role] || currentUser.role;

      if (props?.collapsed) {
        return (
          <div className="layout-user-footer-collapsed" title={`${currentUser.username}（${roleLabel}）`}>
            {currentUser.username.slice(0, 1).toUpperCase()}
          </div>
        );
      }

      return (
        <div className="layout-user-footer">
          <div className="layout-user-name">{currentUser.username}</div>
          <div className="layout-user-role">{roleLabel}</div>
        </div>
      );
    },
    onPageChange: () => {
      const { location } = history;
      const { currentUser } = initialState || {};
      const token = localStorage.getItem('token');

      // 未登录重定向到登录页
      if (!currentUser && !token && location.pathname !== '/user/login' && location.pathname !== '/user/register') {
        history.push('/user/login');
      }
    },
  };
};

// 请求拦截器
export const request = {
  timeout: 10000,
  errorConfig: {
    errorHandler: (error: any) => {
      message.error(error.message || '请求失败');
    },
  },
  requestInterceptors: [
    (url: string, options: any) => {
      let requestUrl = url;

      // 统一补齐 API 前缀，兼容本地开发代理与容器 Nginx 反向代理
      if (requestUrl.startsWith('/') && !requestUrl.startsWith('/api')) {
        requestUrl = `/api${requestUrl}`;
      }

      const token = localStorage.getItem('token');
      if (token) {
        options.headers = {
          ...options.headers,
          Authorization: `Bearer ${token}`,
        };
      }
      return { url: requestUrl, options };
    },
  ],
  responseInterceptors: [
    (response: any) => {
      const data = response.data;
      if (data.code !== 0 && data.code !== 200) {
        message.error(data.message || '请求失败');
        throw new Error(data.message);
      }
      return response;
    },
  ],
};
