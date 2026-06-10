import { request } from '@umijs/max';

/**
 * 获取平台仪表盘统计
 */
export async function getDashboard() {
  return request<API.Response<API.AdminDashboardStats>>('/admin/dashboard', {
    method: 'GET',
  });
}

/**
 * 获取所有球馆管理员
 */
export async function getVenueAdmins() {
  return request<API.Response<API.VenueAdmin[]>>('/admin/venue-admins', {
    method: 'GET',
  });
}

/**
 * 创建球馆管理员
 */
export async function createVenueAdmin(data: API.CreateVenueAdminRequest) {
  return request<API.Response<void>>('/admin/venue-admins', {
    method: 'POST',
    data,
  });
}

/**
 * 更新球馆管理员
 */
export async function updateVenueAdmin(id: number, data: API.UpdateVenueAdminRequest) {
  return request<API.Response<void>>(`/admin/venue-admins/${id}`, {
    method: 'PUT',
    data,
  });
}

/**
 * 删除球馆管理员
 */
export async function deleteVenueAdmin(id: number) {
  return request<API.Response<void>>(`/admin/venue-admins/${id}`, {
    method: 'DELETE',
  });
}

/**
 * 获取所有球馆
 */
export async function getVenues() {
  return request<API.Response<API.Venue[]>>('/admin/venues', {
    method: 'GET',
  });
}

/**
 * 创建球馆
 */
export async function createVenue(data: API.CreateVenueRequest) {
  return request<API.Response<void>>('/admin/venues', {
    method: 'POST',
    data,
  });
}
