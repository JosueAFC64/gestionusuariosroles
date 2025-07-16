import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaCalendarAlt } from "react-icons/fa";
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';

const BASE_URL = 'http://localhost:8080/';

export default function EditUser() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [editUser, setEditUser] = useState(null);
  const {user, setUser} = useAuth();
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    nombres: '',
    apellidos: '',
    email: '',
    rol: '',
    fechaNacimiento: '',
    telefono: '',
    dni: ''
  });

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const { data } = await axios.get(`${BASE_URL}api/v1/users/${id}`, {
          withCredentials: true
        });
        setEditUser(data);
        setFormData({
          nombres: data.nombres,
          apellidos: data.apellidos,
          email: data.email,
          rol: data.rol,
          fechaNacimiento: data.fechaNacimiento,
          estado: data.estado,
          telefono: data.telefono,
          dni: data.dni
        });
        setLoading(false);
      } catch (error) {
        console.error('Error al cargar el usuario:', error);
        setLoading(false);
      }
    };
    console.log(user);
    fetchUser();
  }, [id]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.patch(`${BASE_URL}api/v1/users/${id}`, formData, {
        withCredentials: true
      });

      console.log(typeof id);

      if (String(user.id) === String(id)) {
        setUser(response.data);
      }

      toast.success('Usuario actualizado correctamente');
      navigate('/'); // Volver al dashboard después de editar
    } catch (error) {
      if(error.response && error.response.data) {
        const {status, message} = error.response.data;
        toast.error(`Error ${status}: ${message}`);
      }
      console.error('Error al actualizar el usuario:', error);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <h1 className="text-3xl text-black font-bold mb-6">Editar Usuario</h1>
      
      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-md p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nombres</label>
            <input
              type="text"
              name="nombres"
              value={formData.nombres}
              onChange={handleChange}
              className="w-full text-black px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              required
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Apellidos</label>
            <input
              type="text"
              name="apellidos"
              value={formData.apellidos}
              onChange={handleChange}
              className="w-full text-black px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              required
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className="w-full text-black px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              required
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
            <input
              type="text"
              name="telefono"
              value={formData.telefono}
              onChange={handleChange}
              className="w-full text-black px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">DNI</label>
            <input
              type="text"
              name="dni"
              value={formData.dni}
              onChange={handleChange}
              className="w-full text-black px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div className="relative">
            <label className="block text-sm font-medium text-gray-700 mb-1">Fecha de Nacimiento</label>
            <div className="relative">
              <input
                type="date"
                name="fechaNacimiento"
                value={formData.fechaNacimiento}
                onChange={handleChange}
                className="w-full text-black px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 pr-10" // Añade padding-right (pr-10)
              />
              <FaCalendarAlt className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none" />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Rol</label>
            <select
              name="rol"
              value={formData.rol}
              onChange={handleChange}
              className={`w-full text-black px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                editUser.id === user.id ? 'bg-gray-100 cursor-not-allowed' : ''
              }`}
              disabled={editUser.id === user.id}
              required
            >
              <option value="">Seleccionar rol</option>
              <option value="ADMINISTRADOR">Administrador</option>
              <option value="SUPERVISOR">Supervisor</option>
              <option value="EGRESADO">Egresado</option>
            </select>
          </div>       
        </div>
        
        <div className="mt-8 flex justify-end space-x-3">
          <button
            type="button"
            onClick={() => navigate('/')}
            className="px-4 py-2 bg-[#1a1a1a] border border-gray-300 rounded-md text-sm font-medium text-red-300 hover:text-red-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="px-4 py-2 bg-[#1a1a1a] border border-transparent rounded-md text-sm font-medium text-green-300 hover:text-green-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
          >
            Guardar Cambios
          </button>
        </div>
      </form>
    </div>
  );
}