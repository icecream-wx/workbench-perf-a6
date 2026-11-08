#!/usr/bin/env node
/**
 * CRUD API smoke for the employee module. Business-specific checks can be
 * appended; this covers list/create/update/validation/duplicate/404.
 */
const BACKEND = process.env.APP_BACKEND_URL || 'http://127.0.0.1:8080';

async function call(path, options = {}) {
  const response = await fetch(BACKEND + path, {
    ...options,
    headers: { Accept: 'application/json', 'Content-Type': 'application/json', ...(options.headers || {}) },
  });
  const text = await response.text();
  let json = null;
  try { json = JSON.parse(text); } catch { /* non-JSON */ }
  return { status: response.status, json, text };
}

let failed = false;
function check(label, ok, detail = '') {
  console.log(`${ok ? 'PASS' : 'FAIL'} ${label}${detail ? `: ${detail}` : ''}`);
  if (!ok) failed = true;
}

const code = `SMOKE${Date.now()}`;
const name = '烟测员工';
let createdId = null;
try {
  const health = await call('/actuator/health');
  check('health UP', health.status === 200 && health.json?.status === 'UP');
  const openapi = await call('/v3/api-docs');
  check('openapi', openapi.status === 200);

  const list = await call('/api/employees?page=1&size=10');
  check(
    'list',
    list.status === 200
      && list.json?.code === 0
      && Array.isArray(list.json?.data?.list)
      && typeof list.json?.data?.total === 'number'
      && list.json?.data?.page === 1
      && list.json?.data?.size === 10,
    list.text.slice(0, 160),
  );

  const created = await call('/api/employees', {
    method: 'POST',
    body: JSON.stringify({ code, name, department: '烟测部', phone: '13800138000', hireDate: '2024-01-31' }),
  });
  check('create', created.status === 200 && created.json?.data?.id, created.text.slice(0, 160));
  check('create default status', created.json?.data?.status === '在职', created.text.slice(0, 160));
  createdId = created.json?.data?.id;

  const duplicate = await call('/api/employees', {
    method: 'POST',
    body: JSON.stringify({ code, name }),
  });
  check(
    'duplicate 400',
    duplicate.status === 400 && duplicate.json?.message?.includes('工号已存在'),
    duplicate.text.slice(0, 160),
  );

  const missing = await call('/api/employees', { method: 'POST', body: JSON.stringify({ name }) });
  check('validation 400', missing.status === 400 || missing.json?.code === 400, missing.text.slice(0, 160));

  const updated = await call(`/api/employees/${createdId}`, {
    method: 'PUT',
    body: JSON.stringify({ code, name: `${name}改`, status: '离职' }),
  });
  check(
    'update',
    updated.status === 200 && updated.json?.data?.name === `${name}改` && updated.json?.data?.status === '离职',
    updated.text.slice(0, 160),
  );
  const notFound = await call('/api/employees/99999999', {
    method: 'PUT',
    body: JSON.stringify({ code: `${code}X`, name }),
  });
  check('update 404', notFound.status === 404 || notFound.json?.code === 40401, notFound.text.slice(0, 160));
} catch (error) {
  console.error('CRUD API smoke failed:', error.message);
  failed = true;
}
if (failed) process.exitCode = 1;
else console.log('CRUD API smoke: PASS');
