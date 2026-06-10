import { expect, Page, test } from '@playwright/test';

type OrderStatus = 'PENDING_PAY' | 'PAID' | 'COMPLETED' | 'CANCELED';

type MockOrder = {
  id: number;
  orderNo: string;
  userId: number;
  venueId: number;
  courtId: number;
  slotId: number;
  amount: number;
  status: OrderStatus;
  createdAt: string;
  paidAt?: string;
  completedAt?: string;
  canceledAt?: string;
};

const venue = {
  id: 1,
  name: '阳光羽毛球馆',
  address: '北京市朝阳区建国路88号',
  phone: '010-12345678',
  openTime: '09:00',
  closeTime: '22:00',
  status: 'enabled',
};

const court = {
  id: 11,
  venueId: 1,
  name: '1号场地',
  type: '羽毛球',
  pricePerSlot: 88,
  status: 'enabled',
  createdAt: '2026-02-07T08:00:00',
};

const slot = {
  id: 101,
  courtId: 11,
  courtName: '1号场地',
  slotDate: '2099-01-01',
  startTime: '10:00',
  endTime: '11:00',
  price: 88,
  status: 'available',
  createdAt: '2026-02-07T08:00:00',
};

function apiSuccess<T>(data: T) {
  return {
    code: 200,
    message: 'success',
    data,
  };
}

async function setupLifecycleApi(page: Page, seedOrders: MockOrder[] = []) {
  const orders: MockOrder[] = [...seedOrders];
  let nextOrderId = seedOrders.reduce((max, item) => Math.max(max, item.id), 899) + 1;

  const toOrderView = (order: MockOrder) => ({
    ...order,
    username: 'user1',
    venueName: venue.name,
    courtName: court.name,
    slotDate: slot.slotDate,
    startTime: slot.startTime,
    endTime: slot.endTime,
  });

  const isSlotReserved = () => orders.some((item) => item.slotId === slot.id && item.status !== 'CANCELED');

  await page.route('**/auth/me', async (route) => {
    const authHeader = route.request().headers().authorization || '';
    if (authHeader.includes('token-user')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess({
          userId: 5,
          username: 'user1',
          role: 'USER',
        })),
      });
      return;
    }

    if (authHeader.includes('token-venue')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess({
          userId: 2,
          username: 'venue_admin1',
          role: 'VENUE_ADMIN',
        })),
      });
      return;
    }

    await route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 401,
        message: 'unauthorized',
        data: null,
      }),
    });
  });

  await page.route('**/app/**', async (route) => {
    if (!['xhr', 'fetch'].includes(route.request().resourceType())) {
      await route.continue();
      return;
    }

    const method = route.request().method();
    const url = new URL(route.request().url());
    const path = url.pathname.replace(/^\/api/, '');

    if (method === 'GET' && path === '/app/venues') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess([venue])),
      });
      return;
    }

    if (method === 'GET' && path === `/app/venues/${venue.id}`) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess(venue)),
      });
      return;
    }

    if (method === 'GET' && path === `/app/venues/${venue.id}/courts`) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess([court])),
      });
      return;
    }

    if (method === 'GET' && path === `/app/courts/${court.id}/slots`) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess([
          {
            ...slot,
            reserved: isSlotReserved(),
          },
        ])),
      });
      return;
    }

    if (method === 'POST' && path === '/app/orders') {
      const payload = JSON.parse(route.request().postData() || '{}');
      if (payload.slotId !== slot.id || isSlotReserved()) {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            code: 3002,
            message: 'Slot已被预约',
            data: null,
          }),
        });
        return;
      }

      const order: MockOrder = {
        id: nextOrderId,
        orderNo: `ORDMOCK${nextOrderId}`,
        userId: 5,
        venueId: venue.id,
        courtId: court.id,
        slotId: slot.id,
        amount: slot.price,
        status: 'PENDING_PAY',
        createdAt: '2026-02-07T08:30:00',
      };
      orders.unshift(order);
      nextOrderId += 1;

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess(order)),
      });
      return;
    }

    if (method === 'GET' && path === '/app/orders') {
      const status = url.searchParams.get('status');
      const filtered = status ? orders.filter((item) => item.status === status) : orders;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess(filtered.map(toOrderView))),
      });
      return;
    }

    const orderDetailMatch = path.match(/^\/app\/orders\/(\d+)$/);
    if (method === 'GET' && orderDetailMatch) {
      const orderId = Number(orderDetailMatch[1]);
      const order = orders.find((item) => item.id === orderId);
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(order
          ? apiSuccess(toOrderView(order))
          : { code: 3003, message: '订单不存在', data: null }),
      });
      return;
    }

    const orderActionMatch = path.match(/^\/app\/orders\/(\d+)\/(pay|cancel)$/);
    if (method === 'POST' && orderActionMatch) {
      const orderId = Number(orderActionMatch[1]);
      const action = orderActionMatch[2];
      const order = orders.find((item) => item.id === orderId);

      if (!order) {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ code: 3003, message: '订单不存在', data: null }),
        });
        return;
      }

      if (action === 'pay') {
        order.status = 'PAID';
        order.paidAt = '2026-02-07T08:35:00';
      } else {
        order.status = 'CANCELED';
        order.canceledAt = '2026-02-07T08:36:00';
      }

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess(null)),
      });
      return;
    }

    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 500,
        message: `Unexpected app route: ${method} ${path}`,
        data: null,
      }),
    });
  });

  await page.route('**/venue/**', async (route) => {
    if (!['xhr', 'fetch'].includes(route.request().resourceType())) {
      await route.continue();
      return;
    }

    const method = route.request().method();
    const url = new URL(route.request().url());
    const path = url.pathname.replace(/^\/api/, '');

    if (method === 'GET' && path === '/venue/orders') {
      const status = url.searchParams.get('status');
      const filtered = status ? orders.filter((item) => item.status === status) : orders;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess(filtered.map(toOrderView))),
      });
      return;
    }

    const completeMatch = path.match(/^\/venue\/orders\/(\d+)\/complete$/);
    if (method === 'POST' && completeMatch) {
      const orderId = Number(completeMatch[1]);
      const order = orders.find((item) => item.id === orderId);
      if (!order) {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ code: 3003, message: '订单不存在', data: null }),
        });
        return;
      }
      order.status = 'COMPLETED';
      order.completedAt = '2026-02-07T08:40:00';
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiSuccess(null)),
      });
      return;
    }

    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 500,
        message: `Unexpected venue route: ${method} ${path}`,
        data: null,
      }),
    });
  });
}

test('user books slot and pays order successfully', async ({ page }) => {
  await setupLifecycleApi(page);

  await page.addInitScript(() => {
    window.localStorage.setItem('token', 'token-user');
  });

  await page.goto('/app/home');
  await expect(page.getByTestId('venue-card-1')).toBeVisible();
  await page.getByTestId('venue-card-1').click();
  await expect(page).toHaveURL(/\/app\/venues\/1$/);

  await page.getByTestId('court-card-11').click();
  await expect(page).toHaveURL(/\/app\/courts\/11$/);

  await page.getByTestId('book-slot-101').click();
  await page.locator('.ant-modal-confirm .ant-btn-primary').click();

  await expect(page).toHaveURL(/\/app\/orders\/900$/);
  await expect(page.getByTestId('order-status')).toContainText('待支付');

  await page.getByTestId('order-pay-btn').click();
  await page.locator('.ant-modal-confirm .ant-btn-primary').click();
  await expect(page.getByTestId('order-status')).toContainText('已支付');
});

test('venue admin completes paid order', async ({ page }) => {
  await setupLifecycleApi(page, [
    {
      id: 910,
      orderNo: 'ORDMOCK910',
      userId: 5,
      venueId: venue.id,
      courtId: court.id,
      slotId: slot.id,
      amount: slot.price,
      status: 'PAID',
      createdAt: '2026-02-07T08:31:00',
      paidAt: '2026-02-07T08:35:00',
    },
  ]);

  await page.addInitScript(() => {
    window.localStorage.setItem('token', 'token-venue');
  });

  await page.goto('/venue/orders');
  await expect(page).toHaveURL(/\/venue\/orders$/);
  await expect(page.getByTestId('venue-complete-order-910')).toBeVisible();
  await page.getByTestId('venue-complete-order-910').click();
  await page.locator('.ant-modal-confirm .ant-btn-primary').click();
  await expect(page.locator('.ant-table-row', { hasText: 'ORDMOCK910' })).toContainText('已完成');
});

test('cancels pending order from user order list', async ({ page }) => {
  await setupLifecycleApi(page, [
    {
      id: 901,
      orderNo: 'ORDMOCK901',
      userId: 5,
      venueId: venue.id,
      courtId: court.id,
      slotId: slot.id,
      amount: slot.price,
      status: 'PENDING_PAY',
      createdAt: '2026-02-07T08:31:00',
    },
  ]);

  await page.addInitScript(() => {
    window.localStorage.setItem('token', 'token-user');
  });

  await page.goto('/app/orders');
  await expect(page.getByTestId('app-order-cancel-901')).toBeVisible();

  await page.getByTestId('app-order-cancel-901').click();
  await page.locator('.ant-modal-confirm .ant-btn-primary').click();

  await expect(page.locator('.ant-table-row', { hasText: 'ORDMOCK901' })).toContainText('已取消');
  await expect(page.getByTestId('app-order-cancel-901')).toHaveCount(0);
});
