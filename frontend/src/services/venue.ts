import { request } from '@umijs/max';

/**
 * 获取球馆仪表盘统计
 */
export async function getDashboard() {
  return request<API.Response<API.VenueDashboardStats>>('/venue/dashboard', {
    method: 'GET',
  });
}

/**
 * 获取球馆信息
 */
export async function getVenueProfile() {
  return request<API.Response<API.Venue>>('/venue/profile', {
    method: 'GET',
  });
}

/**
 * 更新球馆信息
 */
export async function updateVenueProfile(data: Partial<API.Venue>) {
  return request<API.Response<void>>('/venue/profile', {
    method: 'PUT',
    data,
  });
}

/**
 * 获取场地列表
 */
export async function getCourts() {
  return request<API.Response<API.Court[]>>('/venue/courts', {
    method: 'GET',
  });
}

/**
 * 创建场地
 */
export async function createCourt(data: API.CreateCourtRequest) {
  return request<API.Response<void>>('/venue/courts', {
    method: 'POST',
    data,
  });
}

/**
 * 更新场地
 */
export async function updateCourt(id: number, data: Partial<API.Court>) {
  return request<API.Response<void>>(`/venue/courts/${id}`, {
    method: 'PUT',
    data,
  });
}

/**
 * 删除场地
 */
export async function deleteCourt(id: number) {
  return request<API.Response<void>>(`/venue/courts/${id}`, {
    method: 'DELETE',
  });
}

/**
 * 生成 Slot
 */
export async function generateSlots(data: API.GenerateSlotsRequest) {
  return request<API.Response<void>>('/venue/slots/generate', {
    method: 'POST',
    data,
  });
}

/**
 * 获取 Slot 列表
 */
export async function getSlots(courtId: number, date?: string) {
  return request<API.Response<API.Slot[]>>('/venue/slots', {
    method: 'GET',
    params: { courtId, date },
  });
}

/**
 * 更新 Slot
 */
export async function updateSlot(id: number, data: Partial<API.Slot>) {
  return request<API.Response<void>>(`/venue/slots/${id}`, {
    method: 'PUT',
    data,
  });
}

/**
 * 获取订单列表
 */
export async function getOrders(params?: {
  status?: string;
  date?: string;
  courtId?: number;
}) {
  return request<API.Response<API.Order[]>>('/venue/orders', {
    method: 'GET',
    params,
  });
}

/**
 * 核销订单
 */
export async function completeOrder(id: number) {
  return request<API.Response<void>>(`/venue/orders/${id}/complete`, {
    method: 'POST',
  });
}
