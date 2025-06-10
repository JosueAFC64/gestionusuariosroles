import { useState, useEffect } from 'react';
import { useNavigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import SettingsSidebar from '../components/SettingsSidebar';
import Navbar from '../components/Navbar';

export default function Settings() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const toggleSidebar = () => setSidebarOpen(!sidebarOpen);
  const closeSidebar = () => setSidebarOpen(false);

  if (!user) {
    navigate('/login');
    return null;
  }

  return (
    <div className="h-screen w-screen flex flex-col bg-gray-50">
      <Navbar toggleSidebar={toggleSidebar} showSettingsButton={false} />
      
      <div className="flex flex-1 overflow-hidden">
        <SettingsSidebar isOpen={sidebarOpen} closeSidebar={closeSidebar} />
        
        <main className="flex-1 overflow-y-auto p-4">
          <Outlet /> {/* Esto renderizará las sub-rutas */}
        </main>
      </div>
    </div>
  );
}