/**
 * 权限定义
 * @see https://umijs.org/docs/max/access
 */
export default function access(initialState: { currentUser?: API.CurrentUser } | undefined) {
  const { currentUser } = initialState ?? {};

  return {
    // 用户权限
    isUser: currentUser && currentUser.role === 'USER',
    // 球馆管理员权限
    isVenueAdmin: currentUser && currentUser.role === 'VENUE_ADMIN',
    // 超级管理员权限
    isSuperAdmin: currentUser && currentUser.role === 'SUPER_ADMIN',
  };
}
