import { useState, useEffect } from "react";
import { FaFileExport } from "react-icons/fa";
import { Menu, Transition } from "@headlessui/react";
import { FaFilter } from "react-icons/fa";
import axios from "axios";
import { useAuth } from "../context/AuthContext";

const BASE_URL = "http://localhost:8080/";

export default function ActivityLogs() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 5,
    totalPages: 0,
    totalElements: 0,
  });
  const { user } = useAuth();

  useEffect(() => {
    fetchLogs();
  }, [pagination.page, pagination.size]);

  const fetchLogs = async () => {
    try {
      const { data } = await axios.get(`${BASE_URL}api/v1/activity-logs`, {
        params: {
          page: pagination.page,
          size: pagination.size,
        },
        withCredentials: true,
      });

      setLogs(data.content);
      setPagination((prev) => ({
        ...prev,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
      }));
    } catch (error) {
      console.error("Error fetching logs:", error);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      setPagination((prev) => ({ ...prev, page: newPage }));
    }
  };

  const handleSizeChange = (e) => {
    const newSize = parseInt(e.target.value);
    setPagination((prev) => ({ ...prev, size: newSize, page: 0 }));
  };

  const formatDateTime = (dateTime) => {
    return new Date(dateTime).toLocaleString();
  };

  const exportToPDF = () => {
    // Crear un enlace temporal
    const link = document.createElement("a");

    // Configurar el enlace
    link.href = `${BASE_URL}api/v1/activity-logs/export/pdf?t=${Date.now()}`;
    link.setAttribute("download", "Lista_Logs.pdf"); // Fuerza la descarga con el nombre específico

    // Añadir el enlace al DOM y hacer click
    document.body.appendChild(link);
    link.click();

    // Limpiar
    setTimeout(() => {
      document.body.removeChild(link);
      window.URL.revokeObjectURL(link.href);
    }, 100);
  };

  const exportToExcel = () => {
    // Crear un enlace temporal
    const link = document.createElement("a");

    // Configurar el enlace
    link.href = `${BASE_URL}api/v1/activity-logs/export/excel?t=${Date.now()}`;
    link.setAttribute("download", "Lista_Logs.xlsx"); // Fuerza la descarga con el nombre específico

    // Añadir el enlace al DOM y hacer click
    document.body.appendChild(link);
    link.click();

    // Limpiar
    setTimeout(() => {
      document.body.removeChild(link);
      window.URL.revokeObjectURL(link.href);
    }, 100);
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
      <h1 className="text-3xl text-black font-bold mb-6">Registro de Actividades</h1>

      <div className="flex flex-col sm:flex-row justify-end items-center mb-6 gap-4">
        <div className="flex items-center gap-3 w-full sm:w-auto">
          <Menu as="div" className="relative">
            <Menu.Button className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500">
              <FaFileExport className="text-gray-600" />
            </Menu.Button>

            <Transition
              enter="transition ease-out duration-100"
              enterFrom="transform opacity-0 scale-95"
              enterTo="transform opacity-100 scale-100"
              leave="transition ease-in duration-75"
              leaveFrom="transform opacity-100 scale-100"
              leaveTo="transform opacity-0 scale-95"
            >
              <Menu.Items className="absolute right-0 mt-2 w-40 origin-top-right rounded-lg bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none z-10">
                <div>
                  <Menu.Item>
                    {({ active }) => (
                      <button
                        onClick={exportToPDF}
                        className={`${
                          active ? "bg-gray-100 text-gray-900" : "text-gray-700"
                        } w-full px-4 py-2 text-left text-sm flex items-center rounded-t-lg`}
                      >
                        <svg
                          className="h-4 w-4 mr-2 text-red-500"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10"
                          />
                        </svg>
                        Exportar a PDF
                      </button>
                    )}
                  </Menu.Item>
                  <Menu.Item>
                    {({ active }) => (
                      <button
                        onClick={exportToExcel}
                        className={`${
                          active ? "bg-gray-100 text-gray-900" : "text-gray-700"
                        } w-full px-4 py-2 text-left text-sm flex items-center rounded-b-lg`}
                      >
                        <svg
                          className="h-4 w-4 mr-2 text-green-500"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                          />
                        </svg>
                        Exportar a Excel
                      </button>
                    )}
                  </Menu.Item>
                </div>
              </Menu.Items>
            </Transition>
          </Menu>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Usuario
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acción
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Descripción
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fecha/Hora
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {logs.map((log, index) => (
              <tr key={index}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {log.username}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {log.action}
                </td>
                <td className="px-6 py-4 text-sm text-gray-900">
                  {log.description}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {formatDateTime(log.actionDateTime)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Controles de paginación */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="text-sm text-gray-600">
          Mostrando {pagination.page * pagination.size + 1} -
          {Math.min(
            (pagination.page + 1) * pagination.size,
            pagination.totalElements
          )}{" "}
          de {pagination.totalElements} registros
        </div>

        <div className="flex items-center gap-4 mt-6">
          <select
            value={pagination.size}
            onChange={handleSizeChange}
            className="px-2 py-1 border border-gray-300 rounded-md text-sm text-gray-600"
          >
            <option value="5">5 por página</option>
            <option value="10">10 por página</option>
            <option value="20">20 por página</option>
            <option value="50">50 por página</option>
          </select>

          <div className="flex items-center gap-2">
            <button
              onClick={() => handlePageChange(pagination.page - 1)}
              disabled={pagination.page === 0}
              className={`px-3 py-1 rounded-md ${
                pagination.page === 0 ? "bg-gray-200 cursor-na no-bc" : "bg-[#1a1a1a]"
              }`}
            >
              &lt;
            </button>

            <span className="text-sm text-gray-600">
              Página {pagination.page + 1} de {pagination.totalPages}
            </span>

            <button
              onClick={() => handlePageChange(pagination.page + 1)}
              disabled={pagination.page >= pagination.totalPages - 1}
              className={`px-3 py-1 rounded-md ${
                pagination.page >= pagination.totalPages - 1
                  ? "bg-gray-200 cursor-na no-bc"
                  : "bg-[#1a1a1a]"
              }`}
            >
              &gt;
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
