import React, { ReactNode } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from '../auth/useAuth';
import { LoadingOverlay } from '../loading/LoadingOverlay';

export interface PublicRouteProps {
  children?: ReactNode;
  restricted?: boolean;
  redirectTo?: string;
}

export const PublicRoute: React.FC<PublicRouteProps> = ({
  children,
  restricted = false,
  redirectTo = '/',
}) => {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingOverlay isGlobal message="Loading..." />;
  }

  if (isAuthenticated && restricted) {
    const from = (location.state as { from?: Location })?.from?.pathname || redirectTo;
    return <Navigate to={from} replace />;
  }

  return children ? <>{children}</> : <Outlet />;
};
