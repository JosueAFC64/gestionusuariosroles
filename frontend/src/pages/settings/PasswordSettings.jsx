import { useState, useEffect } from "react";
import { toast } from "react-toastify";
import { FaEye } from "react-icons/fa";
import axios from "axios";

const BASE_URL = "http://localhost:8080/";

export default function PasswordSettings() {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [isDirty, setIsDirty] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.patch(
        `${BASE_URL}api/v1/users/password`,
        { currentPassword, newPassword },
        { withCredentials: true }
      );

      if (response.data.status) {
        toast.success(response.data.message);
        setIsDirty(false);
      }
    } catch (error) {
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error("Error al actualizar la contraseña");
      }
    }
  };

  return (
    <div className="max-w-screen mx-auto bg-gradient-to-b from-gray-900 to-gray-800 rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-6 text-[#646cff]">
        Actualizar Contraseña
      </h2>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="min-w-0">
            <label className="block text-sm font-medium text-[#646cff] mb-1">
              Contraseña actual
            </label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-white"
                placeholder="Ingrese su contraseña actual"
                value={currentPassword}
                onChange={(e) => {setCurrentPassword(e.target.value); setIsDirty(true);}}
              />
              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-300 hover:text-white focus:outline-none no-bc"
              >
                <FaEye />
              </button>
            </div>
          </div>

          <div className="min-w-0">
            <label className="block text-sm font-medium text-[#646cff] mb-1">
              Nueva contraseña
            </label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-white"
                placeholder="Ingrese su nueva contraseña"
                value={newPassword}
                onChange={(e) => {setNewPassword(e.target.value); setIsDirty(true);}}
              />
              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-300 hover:text-white focus:outline-none no-bc"
              >
                <FaEye />
              </button>
            </div>
          </div>
        </div>

        <div className="pt-4">
          <button
            type="submit"
            disabled={!isDirty}
            className={`px-4 py-2 rounded-md text-white ${
              isDirty
                ? "bg-[#1a1a1a] hover:text-red-500"
                : "bg-gray-400 cursor-not-allowed"
            }`}
          >
            Guardar Cambios
          </button>
        </div>
      </form>
    </div>
  );
}
