import React from 'react';
import { Navigate } from 'react-router-dom';
import { clearSession, dashboardPathForRole, getSession, normalizeRole } from '../utils/auth';

function ProtectedRoute({ children, role }) {
  const session = getSession();

  if (!session.valid) {
    clearSession();
    return <Navigate to="/login" replace />;
  }

  if (role && session.role !== normalizeRole(role)) {
    return <Navigate to={dashboardPathForRole(session.role)} replace />;
  }

  return children;
}

export default ProtectedRoute;
