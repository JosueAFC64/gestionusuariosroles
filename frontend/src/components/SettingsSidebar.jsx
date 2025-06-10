import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { FaUser, FaLock, FaArrowLeft } from "react-icons/fa";

export default function SettingsSidebar({ isOpen, closeSidebar }) {
  const { user } = useAuth();

  return (
    <>
      {/* Overlay para móviles */}
      {isOpen && (
        <div
          className="fixed inset-0 z-20 bg-black/50 backdrop-blur-sm lg:hidden"
          onClick={closeSidebar}
        ></div>
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 bg-gradient-to-b from-gray-900 to-gray-800 text-white transform ${
          isOpen ? "translate-x-0" : "-translate-x-full"
        } transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static flex flex-col`}
      >
        <nav className="flex-1 px-4 py-6 space-y-1">
          <>
            <NavLink
              to="/settings/account"
              end
              className={({ isActive }) =>
                `flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-colors duration-200 ${
                  isActive
                    ? "bg-blue-600/20 text-blue-400"
                    : "text-gray-300 hover:bg-gray-700/50 hover:text-white"
                }`
              }
              onClick={closeSidebar}
            >
              <FaUser className="mr-3" />
              Cuenta
            </NavLink>
            <NavLink
              to="/settings/password"
              className={({ isActive }) =>
                `flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-colors duration-200 ${
                  isActive
                    ? "bg-blue-600/20 text-blue-400"
                    : "text-gray-300 hover:bg-gray-700/50 hover:text-white"
                }`
              }
              onClick={closeSidebar}
            >
              <FaLock className="mr-3" />
              Contraseña
            </NavLink>
            <NavLink
              to="/"
              className={({ isActive }) =>
                `flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-colors duration-200 ${
                  isActive
                    ? "bg-blue-600/20 text-blue-400"
                    : "text-gray-300 hover:bg-gray-700/50 hover:text-white"
                }`
              }
              onClick={closeSidebar}
            >
              <FaArrowLeft className="mr-3" />
              Volver al Inicio
            </NavLink>
          </>
        </nav>

        <div className="p-6 border-t border-gray-700/50">
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white font-medium">
              {user?.nombres.charAt(0)}
            </div>
            <div>
              <p className="text-sm font-medium text-white">{user?.nombres}</p>
              <p className="text-xs text-gray-400">{user?.rol}</p>
            </div>
          </div>
          <p className="mt-4 text-xs text-gray-400">Sistema v1.0</p>
        </div>
      </aside>
    </>
  );
}
