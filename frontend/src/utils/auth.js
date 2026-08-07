const AUTH_KEYS = [
  'token',
  'userId',
  'fullName',
  'email',
  'role',
  'departmentId',
];

const DASHBOARDS = {
  Admin: '/admin/dashboard',
  Officer: '/officer/dashboard',
  Citizen: '/citizen/dashboard',
};

export function normalizeRole(role) {
  if (!role) return '';
  const value = String(role).trim().toLowerCase();
  if (value === 'admin') return 'Admin';
  if (value === 'officer') return 'Officer';
  if (value === 'citizen') return 'Citizen';
  return '';
}

export function dashboardPathForRole(role) {
  return DASHBOARDS[normalizeRole(role)] || '/login';
}

export function decodeJwtPayload(token) {
  try {
    const [, payload] = String(token).split('.');
    if (!payload) return null;
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    return JSON.parse(window.atob(padded));
  } catch {
    return null;
  }
}

export function isTokenExpired(token) {
  const payload = decodeJwtPayload(token);
  if (!payload?.exp) return true;
  return payload.exp * 1000 <= Date.now();
}

export function getSession() {
  const token = localStorage.getItem('token');
  const role = normalizeRole(localStorage.getItem('role'));
  const valid = Boolean(token && role && !isTokenExpired(token));
  return {
    token,
    role,
    fullName: localStorage.getItem('fullName') || '',
    valid,
  };
}

export function saveSession(data) {
  clearSession();
  localStorage.setItem('token', data.token);
  localStorage.setItem('userId', String(data.userId));
  localStorage.setItem('fullName', data.fullName || '');
  localStorage.setItem('email', data.email || '');
  localStorage.setItem('role', normalizeRole(data.role));
  if (data.departmentId != null) {
    localStorage.setItem('departmentId', String(data.departmentId));
  }
}

export function clearSession() {
  AUTH_KEYS.forEach((key) => localStorage.removeItem(key));
}
