import React, { ReactNode } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from '../auth/useAuth';
import { LoadingOverlay } from '../loading/LoadingOverlay';

export interface ProtectedRouteProps {
  children?: ReactNode;
  redirectTo?: string;
  allowedRoles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  redirectTo = '/login',
  allowedRoles,
}) => {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingOverlay isGlobal message="Restoring session..." />;
  }

  if (!isAuthenticated) {
    // Preserve current location in route state for redirect-after-login
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0 && user?.role) {
    const hasRole = allowedRoles.includes(user.role as string);
    if (!hasRole) {
      return <Navigate to="/unauthorized" replace />;
    }
  }

  return children ? <>{children}</> : <Outlet />;
};
