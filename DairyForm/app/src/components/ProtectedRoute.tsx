import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

interface Props {
  children?: React.ReactNode;
  allowedRoles?: string[];
}

export default function ProtectedRoute({ children, allowedRoles }: Props) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#f5f7f6]">
        <div className="text-center">
          <div className="mx-auto mb-4 flex size-12 animate-spin items-center justify-center rounded-full border-4 border-[#e4e9e6] border-t-[#1d7352]" />
          <p className="text-sm text-[#7d8983]">Loading...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#f5f7f6]">
        <div className="text-center">
          <h2 className="text-xl font-bold text-[#17221d]">Access Denied</h2>
          <p className="mt-2 text-sm text-[#7d8983]">You don&apos;t have permission to view this page.</p>
        </div>
      </div>
    );
  }

  return children ? <>{children}</> : <Outlet />;
}
