import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import AppLayout from "./layouts/AppLayout";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import AnimalsPage from "./pages/AnimalsPage";
import MilkPage from "./pages/MilkPage";
import HealthPage from "./pages/HealthPage";
import QualityPage from "./pages/QualityPage";
import FeedPage from "./pages/FeedPage";
import TasksPage from "./pages/TasksPage";
import UsersPage from "./pages/UsersPage";
import DevicesPage from "./pages/DevicesPage";
import ExpensesPage from "./pages/ExpensesPage";
import ReproductivePage from "./pages/ReproductivePage";
import CalvesPage from "./pages/CalvesPage";
import VaccinationsPage from "./pages/VaccinationsPage";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          {/* Auth guard layout — renders AppLayout (which contains <Outlet>) */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route index element={<DashboardPage />} />
              <Route path="/animals" element={<AnimalsPage />} />
              <Route path="/milk" element={<MilkPage />} />
              <Route path="/health" element={<HealthPage />} />
              <Route path="/quality" element={<QualityPage />} />
              <Route path="/feed" element={<FeedPage />} />
              <Route path="/tasks" element={<TasksPage />} />
              <Route path="/expenses" element={<ExpensesPage />} />
              <Route path="/reproductive" element={<ReproductivePage />} />
              <Route path="/calves" element={<CalvesPage />} />
              <Route path="/vaccinations" element={<VaccinationsPage />} />
              <Route element={<ProtectedRoute allowedRoles={["admin"]} />}>
                <Route path="/users" element={<UsersPage />} />
                <Route path="/devices" element={<DevicesPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
