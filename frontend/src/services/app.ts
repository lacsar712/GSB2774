import { request } from '@umijs/max';

/**
 * 获取球馆列表
 */
export async function getVenues(keyword?: string) {
  return request<API.Response<API.Venue[]>>('/app/venues', {
    method: 'GET',
    params: { keyword },
  });
}

/**
 * 获取球馆详情
 */
export async function getVenueDetail(id: number) {
  return request<API.Response<API.Venue>>(`/app/venues/${id}`, {
    method: 'GET',
  });
}

/**
 * 获取球馆的场地列表
 */
export async function getVenueCourts(venueId: number) {
  return request<API.Response<API.Court[]>>(`/app/venues/${venueId}/courts`, {
    method: 'GET',
  });
}

/**
 * 获取场地的可预约 Slot
 */
export async function getCourtSlots(courtId: number, date: string) {
  return request<API.Response<API.Slot[]>>(`/app/courts/${courtId}/slots`, {
    method: 'GET',
    params: { date },
  });
}

/**
 * 创建订单
 */
export async function createOrder(data: { slotId: number }) {
  return request<API.Response<API.Order>>('/app/orders', {
    method: 'POST',
    data,
  });
}

/**
 * 模拟支付
 */
export async function payOrder(id: number) {
  return request<API.Response<void>>(`/app/orders/${id}/pay`, {
    method: 'POST',
  });
}

/**
 * 取消订单
 */
export async function cancelOrder(id: number) {
  return request<API.Response<void>>(`/app/orders/${id}/cancel`, {
    method: 'POST',
  });
}

/**
 * 获取我的订单
 */
export async function getMyOrders(status?: string) {
  return request<API.Response<API.Order[]>>('/app/orders', {
    method: 'GET',
    params: { status },
  });
}

/**
 * 获取订单详情
 */
export async function getOrderDetail(id: number) {
  return request<API.Response<API.Order>>(`/app/orders/${id}`, {
    method: 'GET',
  });
}
