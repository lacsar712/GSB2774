declare namespace API {
  // 当前用户信息
  type CurrentUser = {
    userId: number;
    username: string;
    role: 'SUPER_ADMIN' | 'VENUE_ADMIN' | 'USER';
  };

  // 登录请求
  type LoginRequest = {
    username: string;
    password: string;
  };

  // 登录响应
  type LoginResponse = {
    token: string;
    role: string;
    userId: number;
  };

  // 注册请求
  type RegisterRequest = {
    username: string;
    password: string;
    phone?: string;
  };

  // 通用响应
  type Response<T = any> = {
    code: number;
    message: string;
    data: T;
  };

  // 球馆管理员
  type VenueAdmin = {
    id: number;
    username: string;
    phone?: string;
    status: string;
    venueId?: number;
    venueName?: string;
    createdAt: string;
  };

  // 创建球馆管理员请求
  type CreateVenueAdminRequest = {
    username: string;
    password: string;
    phone?: string;
    venueId?: number;
  };

  // 更新球馆管理员请求
  type UpdateVenueAdminRequest = {
    status?: string;
    newPassword?: string;
    venueId?: number;
  };

  // 球馆
  type Venue = {
    id: number;
    name: string;
    address: string;
    phone?: string;
    openTime: string;
    closeTime: string;
    ownerUserId?: number;
    ownerUsername?: string;
    status: string;
    createdAt: string;
  };

  // 创建球馆请求
  type CreateVenueRequest = {
    name: string;
    address: string;
    phone?: string;
    openTime: string;
    closeTime: string;
    ownerUserId: number;
  };

  // 场地
  type Court = {
    id: number;
    venueId: number;
    name: string;
    type: string;
    pricePerSlot: number;
    status: string;
    createdAt: string;
  };

  // 创建场地请求
  type CreateCourtRequest = {
    name: string;
    type: string;
    pricePerSlot: number;
  };

  // Slot
  type Slot = {
    id: number;
    courtId: number;
    courtName?: string;
    slotDate: string;
    startTime: string;
    endTime: string;
    price: number;
    status: string;
    reserved?: boolean;
    createdAt: string;
  };

  // 生成 Slot 请求
  type GenerateSlotsRequest = {
    courtId: number;
    startDate: string;
    endDate: string;
  };

  // 订单
  type Order = {
    id: number;
    orderNo: string;
    userId: number;
    username?: string;
    venueId: number;
    venueName?: string;
    courtId: number;
    courtName?: string;
    slotId: number;
    slotDate?: string;
    startTime?: string;
    endTime?: string;
    amount: number;
    status: string;
    createdAt: string;
    paidAt?: string;
    completedAt?: string;
    canceledAt?: string;
  };

  // 平台仪表盘
  type AdminDashboardStats = {
    totalVenues: number;
    totalVenueAdmins: number;
    totalUsers: number;
    totalOrders: number;
    pendingPayOrders: number;
    paidOrders: number;
    completedOrders: number;
    todayOrders: number;
    totalRevenue: number;
    todayRevenue: number;
  };

  // 球馆仪表盘
  type VenueDashboardStats = {
    venueName: string;
    totalCourts: number;
    totalSlots: number;
    todaySlots: number;
    totalOrders: number;
    todayOrders: number;
    pendingPayOrders: number;
    pendingVerificationOrders: number;
    completedOrders: number;
    totalRevenue: number;
    todayRevenue: number;
  };
}
