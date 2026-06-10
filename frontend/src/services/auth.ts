import { request } from '@umijs/max';

/**
 * 登录
 */
export async function login(data: API.LoginRequest) {
  return request<API.Response<API.LoginResponse>>('/auth/login', {
    method: 'POST',
    data,
  });
}

/**
 * 注册
 */
export async function register(data: API.RegisterRequest) {
  return request<API.Response<void>>('/auth/register', {
    method: 'POST',
    data,
  });
}

/**
 * 获取当前用户信息
 */
export async function getCurrentUser() {
  return request<API.Response<API.CurrentUser>>('/auth/me', {
    method: 'GET',
  });
}
