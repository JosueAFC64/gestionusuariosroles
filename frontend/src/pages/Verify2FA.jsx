import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/';

export default function Verify2FA() {
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  // Get email from location state
  const email = location.state?.email;

  if (!email) {
    navigate('/login');
    return null;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      await axios.post(
        `${BASE_URL}auth/login/2fa/verify?email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`,
        {},
        { withCredentials: true }
      );

      // After successful 2FA verification, fetch user data
      const { data } = await axios.get(`${BASE_URL}api/v1/users/user-data`, {
        withCredentials: true
      });
      
      // Update user context and redirect to dashboard
      login({ email, password: '' }, data);
      navigate('/');
    } catch (error) {
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error('Error al verificar el código 2FA');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen min-w-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-xl overflow-hidden">
        <div className="bg-gradient-to-r from-blue-600 to-indigo-700 p-6 text-center">
          <h2 className="text-2xl font-bold text-white">
            Verificación de dos factores
          </h2>
          <p className="text-blue-200 mt-1">Ingrese el código de verificación</p>
        </div>
        
        <div className="p-6">
          <form className="space-y-5" onSubmit={handleSubmit}>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Código de verificación
              </label>
              <input
                type="text"
                required
                maxLength={6}
                className="w-full text-black px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition"
                placeholder="Ingrese el código de 6 dígitos"
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/[^0-9]/g, ''))}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`w-full py-3 bg-gradient-to-r from-blue-600 to-indigo-700 text-white rounded-lg font-medium hover:from-blue-700 hover:to-indigo-800 transition shadow-md ${
                loading ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              {loading ? 'Verificando...' : 'Verificar código'}
            </button>
          </form>

          <div className="text-center mt-4">
            <button
              onClick={() => navigate('/login')}
              className="text-sm text-blue-600 hover:underline"
            >
              Volver al inicio de sesión
            </button>
          </div>
        </div>
        
        <div className="bg-gray-50 px-6 py-4 text-center text-sm text-gray-600 border-t">
          © {new Date().getFullYear()} Sistema de Gestión
        </div>
      </div>
    </div>
  );
}

