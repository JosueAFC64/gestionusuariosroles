import { useState, useEffect } from "react";
import { useAuth } from "../../context/AuthContext";
import { toast } from "react-toastify";
import axios from "axios";

const BASE_URL = "http://localhost:8080/";

export default function AccountSettings() {
  const { user, setUser } = useAuth();
  const [formData, setFormData] = useState({
    nombres: "",
    apellidos: "",
    email: "",
    fechaNacimiento: "",
    telefono: "",
    dni: "",
  });
  const [isDirty, setIsDirty] = useState(false);
  const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);
  const [deleteEmail, setDeleteEmail] = useState("");

  // Inicializar formulario con datos del usuario
  useEffect(() => {
    if (user) {
      setFormData({
        nombres: user.nombres || "",
        apellidos: user.apellidos || "",
        email: user.email || "",
        fechaNacimiento: user.fechaNacimiento?.split("T")[0] || "",
        telefono: user.telefono || "",
        dni: user.dni || "",
      });
    }
  }, [user]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setIsDirty(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.patch(
        `${BASE_URL}api/v1/users/session/${user.id}`,
        formData,
        { withCredentials: true }
      );

      // Actualizar el estado del usuario con la respuesta del backend
      setUser(response.data);
      setIsDirty(false);
      toast.success("Datos actualizados correctamente");

      // Actualizar el formData con los nuevos valores
      setFormData({
        nombres: response.data.nombres || "",
        apellidos: response.data.apellidos || "",
        email: response.data.email || "",
        fechaNacimiento: response.data.fechaNacimiento?.split("T")[0] || "",
        telefono: response.data.telefono || "",
        dni: response.data.dni || "",
      });
    } catch (error) {
      console.error("Error:", error);
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error("Error al actualizar los datos");
      }
    }
  };

  const handleDeleteAccount = async () => {
    if (deleteEmail !== user.email) {
      toast.error("El email no coincide con tu cuenta");
      return;
    }

    try {
      await axios.delete(`${BASE_URL}api/v1/users/delete/${user.id}`, {
        withCredentials: true,
      });

      toast.success("Cuenta eliminada correctamente");
      setUser(null);
      navigate("/login");
    } catch (error) {
      console.error("Error al eliminar cuenta:", error);
      toast.error(
        error.response?.data?.message || "Error al eliminar la cuenta"
      );
    }
  };

  const handleToggle2FA = async () => {
    try {
      const response = await axios.patch(
        `${BASE_URL}auth/user/2fa/change/${user.id}`,
        {
          withCredentials: true,
        }
      );

      const newState2fa = !user.is2faEnabled;
      setUser({ ...user, is2faEnabled: newState2fa });

      toast.success(
        response.data ||
          (newState2fa
            ? "2FA activado correctamente"
            : "2FA desactivado correctamente")
      );
    } catch (error) {
      console.error("Error al cambiar 2FA:", error);
      toast.error(error.response?.data?.message || "Error al actualizar 2FA");
    }
  };

  return (
    <div className="w-scren mx-auto bg-gradient-to-b shadow-md">
      <div className="mb-3 bg-gradient-to-b from-gray-900 to-gray-800 rounded-lg p-6">
        <h2 className="text-2xl font-bold mb-6 text-[#646cff]">
          Configuración de Cuenta
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-[#646cff] mb-1">
                Nombres
              </label>
              <input
                type="text"
                name="nombres"
                value={formData.nombres}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-white"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#646cff] mb-1">
                Apellidos
              </label>
              <input
                type="text"
                name="apellidos"
                value={formData.apellidos}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-white"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#646cff] mb-1">
                Email
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-white"
                disabled
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#646cff] mb-1">
                Fecha de Nacimiento
              </label>
              <input
                type="date"
                name="fechaNacimiento"
                value={formData.fechaNacimiento}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-white"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#646cff] mb-1">
                Teléfono
              </label>
              <input
                type="text"
                name="telefono"
                value={formData.telefono}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-white"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#646cff] mb-1">
                DNI
              </label>
              <input
                type="text"
                name="dni"
                value={formData.dni}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-white"
              />
            </div>
          </div>

          <div className="pt-4">
            <button
              type="submit"
              disabled={!isDirty}
              className={`px-4 py-2 rounded-md text-white ${
                isDirty
                  ? "bg-[#1a1a1a] hover:text-green-500"
                  : "bg-gray-400 cursor-not-allowed"
              }`}
            >
              Guardar Cambios
            </button>
          </div>
        </form>
      </div>

      <div className="bg-gradient-to-b from-gray-900 to-gray-800 rounded-lg shadow-md p-6 mb-3 border border-blue-200">
        <h2 className="text-2xl font-bold mb-4 text-[#646cff]">
          Autenticación en Dos Pasos (2FA)
        </h2>

        <div className="flex items-center justify-between">
          <div>
            <p className="text-gray-400">
              {user.is2faE
                ? "La autenticación en dos pasos está actualmente activada."
                : "La autenticación en dos pasos está desactivada."}
            </p>
            <p className=" text-gray-400 mt-1">
              {user.is2faEnabled
                ? "Recibirás un código por correo cada vez que inicies sesión."
                : "Actívala para mayor seguridad en tu cuenta."}
            </p>
          </div>

          <label className="relative inline-flex items-center cursor-pointer">
            <input
              type="checkbox"
              checked={user.is2faEnabled || false}
              onChange={handleToggle2FA}
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
          </label>
        </div>
      </div>

      <div className="bg-gradient-to-b from-gray-900 to-gray-800 rounded-lg p-6 border border-red-200 mb-3">
        <h2 className="text-2xl font-bold mb-4 text-red-400">
          Eliminar Cuenta
        </h2>

        <p className="mb-4 text-gray-400">
          Esta acción eliminará permanentemente tu cuenta y todos tus datos.
          <span className="font-semibold">
            {" "}
            ¡Esta operación no se puede deshacer!
          </span>
        </p>

        {!showDeleteConfirmation ? (
          <button
            onClick={() => setShowDeleteConfirmation(true)}
            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
          >
            Eliminar Mi Cuenta
          </button>
        ) : (
          <div className="space-y-4">
            <p className="text-gray-400">
              Para confirmar, ingresa tu email{" "}
              <span className="font-semibold">{user.email}</span>:
            </p>

            <input
              type="email"
              value={deleteEmail}
              onChange={(e) => setDeleteEmail(e.target.value)}
              placeholder="Ingresa tu email"
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-white"
            />

            <div className="flex space-x-3">
              <button
                onClick={handleDeleteAccount}
                disabled={deleteEmail !== user.email}
                className={`px-4 py-2 rounded-md text-white ${
                  deleteEmail === user.email
                    ? "bg-red-600 hover:bg-red-700"
                    : "bg-red-400 cursor-not-allowed"
                }`}
              >
                Confirmar Eliminación
              </button>

              <button
                onClick={() => {
                  setShowDeleteConfirmation(false);
                  setDeleteEmail("");
                }}
                className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300"
              >
                Cancelar
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
