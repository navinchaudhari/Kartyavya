import React from 'react';
import { Navigate } from 'react-router-dom';
import { clearSession, dashboardPathForRole, getSession } from '../utils/auth';

export function HomeEntry({ children }) {
  const session = getSession();
  if (session.valid) {
    return <Navigate to={dashboardPathForRole(session.role)} replace />;
  }
  if (session.token) clearSession();
  return children;
}

export function GuestOnlyRoute({ children }) {
  const session = getSession();
  if (session.valid) {
    return <Navigate to={dashboardPathForRole(session.role)} replace />;
  }
  if (session.token) clearSession();
  return children;
}

export function NotFoundRoute() {
  const session = getSession();
  return <Navigate to={session.valid ? dashboardPathForRole(session.role) : '/'} replace />;
}
