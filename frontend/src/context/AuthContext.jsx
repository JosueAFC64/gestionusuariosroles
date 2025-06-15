import { createContext, useState, useContext, useEffect } from "react";
import axios from "axios";

const BASE_URL = "http://localhost:8080/";

const AuthContext = createContext();

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const login = async (credentials, userData = null) => {
    try {
      if (userData) {
        // si userData ya está disponible, es porque se ha verificado 2FA
        setUser(userData);
        return true;
      }

      // Initial login attempt
      const response = await axios.post(`${BASE_URL}auth/login`, credentials, {
        withCredentials: true,
      });

      const { requires2fa, message } = response.data;

      if (requires2fa) {
        // If 2FA is required, return the message to be shown in the toast
        return { requires2fa: true, message };
      }

      // If no 2FA required, fetch user data and set user
      if (response.status === 200) {
        await fetchUserData();
        return true;
      }
    } catch (error) {
      console.error("Login failed:", error);
      return false;
    }
  };

  const logout = async () => {
    try {
      await axios.post(`${BASE_URL}auth/logout`, {}, { withCredentials: true });
      setUser(null);
    } catch (error) {
      console.error("Logout failed:", error);
    }
  };

  const fetchUserData = async () => {
    try {
      const { data } = await axios.get(
        `${BASE_URL}api/v1/users/session/user-data`,
        {
          withCredentials: true,
        }
      );
      setUser(data);
    } catch (error) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const publicRoutes = ["/login", "/forgot-password", "/reset-password", "/verify-2fa"];
    const currentPath = window.location.pathname;

    if (!publicRoutes.includes(currentPath)) {
      fetchUserData();
    } else {
      setLoading(false);
    }
  }, []);

  const value = {
    user,
    setUser,
    loading,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading ? (
        children
      ) : (
        <div className="min-h-screen min-w-screen flex items-center justify-center bg-gray-50">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
        </div>
      )}
    </AuthContext.Provider>
  );
}
