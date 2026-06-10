import { expect, Page, test } from '@playwright/test';

function apiSuccess<T>(data: T) {
  return {
    code: 0,
    message: 'ok',
    data,
  };
}

async function fulfillJson(page: Page, url: string | RegExp, data: unknown) {
  await page.route(url, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(data),
    });
  });
}

test('redirects unauthenticated visitor to login page', async ({ page }) => {
  await page.goto('/app/home');
  await expect(page).toHaveURL(/\/user\/login$/);
});

test('logs in as USER and redirects to app home', async ({ page }) => {
  await fulfillJson(page, '**/auth/login', apiSuccess({ token: 'token-user', role: 'USER', userId: 5 }));
  await fulfillJson(page, '**/auth/me', apiSuccess({
    userId: 5,
    username: 'user1',
    role: 'USER',
  }));
  await fulfillJson(page, '**/app/venues**', apiSuccess([
    {
      id: 1,
      name: '阳光羽毛球馆',
      address: '北京市朝阳区建国路88号',
      phone: '010-12345678',
      openTime: '09:00',
      closeTime: '22:00',
      status: 'enabled',
    },
  ]));

  await page.goto('/user/login');
  await page.getByTestId('login-username').fill('user1');
  await page.getByTestId('login-password').fill('123456');
  await page.getByTestId('login-submit').click();

  await expect(page).toHaveURL(/\/app\/home$/);
  await expect(page.getByTestId('venue-search-input')).toBeVisible();
});

test('shows error message when login fails', async ({ page }) => {
  await fulfillJson(page, '**/auth/login', {
    code: 1001,
    message: '用户名或密码错误',
    data: null,
  });

  await page.goto('/user/login');
  await page.getByTestId('login-username').fill('user1');
  await page.getByTestId('login-password').fill('wrong-password');
  await page.getByTestId('login-submit').click();

  await expect(page).toHaveURL(/\/user\/login$/);
  await expect(
    page.locator('.ant-message-notice-content').filter({ hasText: '用户名或密码错误' }).first(),
  ).toBeVisible();
});

test('supports searching venues and shows empty state', async ({ page }) => {
  await page.addInitScript(() => {
    window.localStorage.setItem('token', 'token-user');
  });

  await fulfillJson(page, '**/auth/me', apiSuccess({
    userId: 5,
    username: 'user1',
    role: 'USER',
  }));

  await page.route('**/app/venues**', async (route) => {
    const url = new URL(route.request().url());
    const keyword = url.searchParams.get('keyword') ?? '';

    const venues = keyword === '不存在' ? [] : [
      {
        id: 1,
        name: '阳光羽毛球馆',
        address: '北京市朝阳区建国路88号',
        phone: '010-12345678',
        openTime: '09:00',
        closeTime: '22:00',
        status: 'enabled',
      },
    ];

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(apiSuccess(venues)),
    });
  });

  await page.goto('/app/home');
  await expect(page.getByTestId('venue-search-input')).toBeVisible();
  await expect(page.getByText('阳光羽毛球馆')).toBeVisible();

  await page.getByTestId('venue-search-input').fill('不存在');
  await page.getByTestId('venue-search-input').press('Enter');

  await expect(page.getByTestId('venue-empty')).toBeVisible();
});
