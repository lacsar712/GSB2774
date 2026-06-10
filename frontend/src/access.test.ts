import { describe, expect, it } from 'vitest';
import access from './access';

describe('access permission mapping', () => {
  it('returns falsy permissions for unauthenticated user', () => {
    const result = access(undefined);

    expect(Boolean(result.isUser)).toBe(false);
    expect(Boolean(result.isVenueAdmin)).toBe(false);
    expect(Boolean(result.isSuperAdmin)).toBe(false);
  });

  it('returns only USER permission for USER role', () => {
    const result = access({
      currentUser: {
        userId: 1,
        username: 'u1',
        role: 'USER',
      },
    });

    expect(result.isUser).toBe(true);
    expect(result.isVenueAdmin).toBe(false);
    expect(result.isSuperAdmin).toBe(false);
  });

  it('returns only VENUE_ADMIN permission for VENUE_ADMIN role', () => {
    const result = access({
      currentUser: {
        userId: 2,
        username: 'v1',
        role: 'VENUE_ADMIN',
      },
    });

    expect(result.isUser).toBe(false);
    expect(result.isVenueAdmin).toBe(true);
    expect(result.isSuperAdmin).toBe(false);
  });

  it('returns only SUPER_ADMIN permission for SUPER_ADMIN role', () => {
    const result = access({
      currentUser: {
        userId: 3,
        username: 'a1',
        role: 'SUPER_ADMIN',
      },
    });

    expect(result.isUser).toBe(false);
    expect(result.isVenueAdmin).toBe(false);
    expect(result.isSuperAdmin).toBe(true);
  });
});
