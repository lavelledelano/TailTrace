const request = require('supertest');
const app = require('../app');

test('health check responds', async () => {
  const res = await request(app).get('/');
  expect(res.statusCode).toBe(200);
  expect(res.body.status).toBe('TailTrace API running');
});

test('register rejects a short password', async () => {
  const res = await request(app).post('/api/auth/register')
    .send({ fullName: 'Test', email: 'a@b.com', password: '123' });
  expect(res.statusCode).toBe(400);
});

test('login requires email and password', async () => {
  const res = await request(app).post('/api/auth/login').send({});
  expect(res.statusCode).toBe(400);
});

test('protected routes reject requests without a token', async () => {
  const res = await request(app).get('/api/pets/nearby?lat=1&lng=1');
  expect(res.statusCode).toBe(401);
});