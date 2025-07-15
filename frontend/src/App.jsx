import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import Login from "./pages/Login";
import ForgotPassword from "./pages/ForgotPassword";
import Dashboard from "./pages/Dashboard";
import EditUser from "./pages/EditUser";
import UserTable from "./components/UserTable";
import NewUser from "./components/NewUser";
import RoleDistributionReport from "./pages/RoleDistributionReport";
import ResetPassword from "./pages/ResetPassword";
import Settings from "./pages/Settings";
import PasswordSettings from "./pages/settings/PasswordSettings";
import AccountSettings from "./pages/settings/AccountSettings";
import Verify2FA from "./pages/Verify2FA";
import ActivityLogs from "./pages/ActivityLogs";
import NotFound from "./pages/NotFound";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

// Componente para rutas protegidas con control de roles
function ProtectedRoute({ children, allowedRoles }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.rol)) {
    // Redirigir a una página de acceso denegado o al dashboard
    return <Navigate to="/" replace />;
  }

  return children;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/verify-2fa" element={<Verify2FA />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route 
          path="/settings" 
          element={
            <ProtectedRoute 
              allowedRoles={["ADMINISTRADOR", "SUPERVISOR", "EGRESADO"]}
            >
              <Settings />
            </ProtectedRoute>}>
          <Route 
            path="account" 
            element={
              <ProtectedRoute 
                allowedRoles={["ADMINISTRADOR", "SUPERVISOR", "EGRESADO"]}
              >
                <AccountSettings />
              </ProtectedRoute>} 
          />
          <Route
            path="password" 
            element={
              <ProtectedRoute 
                allowedRoles={["ADMINISTRADOR", "SUPERVISOR", "EGRESADO"]}
              >
                <PasswordSettings />
              </ProtectedRoute>} 
          />
          <Route 
            index 
            element={
              <Navigate 
                to="account" 
                replace 
              />} 
          />
        </Route>
        <Route
          path="/"
          element={
            <ProtectedRoute
              allowedRoles={["ADMINISTRADOR", "SUPERVISOR", "EGRESADO"]}
            >
              <Dashboard />
            </ProtectedRoute>
          }
        >
          <Route
            index
            element={
              <ProtectedRoute allowedRoles={["ADMINISTRADOR", "SUPERVISOR"]}>
                <UserTable />
              </ProtectedRoute>
            }
          />
          <Route
            path="edit-user/:id"
            element={
              <ProtectedRoute allowedRoles={["ADMINISTRADOR", "SUPERVISOR"]}>
                <EditUser />
              </ProtectedRoute>
            }
          />
          <Route
            path="new-user"
            element={
              <ProtectedRoute allowedRoles={["ADMINISTRADOR", "SUPERVISOR"]}>
                <NewUser />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reportes/rol-distribution"
            element={
              <ProtectedRoute allowedRoles={["ADMINISTRADOR", "SUPERVISOR"]}>
                <RoleDistributionReport />
              </ProtectedRoute>
            }
          />
          <Route
            path="/activity-logs"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <ActivityLogs />
              </ProtectedRoute>
            }
          />
        </Route>
        <Route path="*" element={<NotFound />} />
      </Routes>
      <ToastContainer
        position="top-right"
        autoClose={5000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="colored"
      />
    </BrowserRouter>
  );
}

export default App;
