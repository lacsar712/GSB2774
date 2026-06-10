import { defineConfig } from '@umijs/max';

export default defineConfig({
  antd: {},
  access: {},
  model: {},
  initialState: {},
  request: {},
  layout: {
    title: '球馆预约管理系统',
  },
  routes: [
    {
      path: '/user',
      layout: false,
      routes: [
        { path: '/user/login', component: './User/Login' },
        { path: '/user/register', component: './User/Register' },
      ],
    },
    {
      path: '/app',
      name: '前台',
      icon: 'home',
      access: 'isUser',
      routes: [
        { path: '/app/home', name: '球馆列表', component: './App/Home' },
        { path: '/app/venues/:id', name: '球馆详情', component: './App/VenueDetail', hideInMenu: true },
        { path: '/app/courts/:id', name: '场地预约', component: './App/CourtBooking', hideInMenu: true },
        { path: '/app/orders', name: '我的订单', component: './App/Orders' },
        { path: '/app/orders/:id', name: '订单详情', component: './App/OrderDetail', hideInMenu: true },
      ],
    },
    {
      path: '/venue',
      name: '球馆管理',
      icon: 'shop',
      access: 'isVenueAdmin',
      routes: [
        { path: '/venue/dashboard', name: '仪表盘', component: './Venue/Dashboard' },
        { path: '/venue/profile', name: '球馆信息', component: './Venue/Profile' },
        { path: '/venue/courts', name: '场地管理', component: './Venue/Courts' },
        { path: '/venue/slots', name: '时段管理', component: './Venue/Slots' },
        { path: '/venue/orders', name: '订单管理', component: './Venue/Orders' },
      ],
    },
    {
      path: '/admin',
      name: '系统管理',
      icon: 'crown',
      access: 'isSuperAdmin',
      routes: [
        { path: '/admin/dashboard', name: '仪表盘', component: './Admin/Dashboard' },
        { path: '/admin/venue-admins', name: '管理员账号', component: './Admin/VenueAdmins' },
        { path: '/admin/venues', name: '球馆列表', component: './Admin/Venues' },
      ],
    },
    { path: '/', redirect: '/user/login' },
  ],
  npmClient: 'pnpm',
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
});
