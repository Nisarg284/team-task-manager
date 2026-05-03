import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Sidebar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <h2>⚡ TaskFlow</h2>
        <span>Team Task Manager</span>
      </div>

      <nav className="sidebar-nav">
        <NavLink to="/dashboard" id="nav-dashboard">
          <span className="nav-icon">📊</span> Dashboard
        </NavLink>
        <NavLink to="/projects" id="nav-projects">
          <span className="nav-icon">📁</span> Projects
        </NavLink>
        <NavLink to="/tasks" id="nav-tasks">
          <span className="nav-icon">✅</span> Tasks
        </NavLink>
        {isAdmin() && (
          <NavLink to="/team" id="nav-team">
            <span className="nav-icon">👥</span> Team
          </NavLink>
        )}
        <button onClick={handleLogout} id="nav-logout">
          <span className="nav-icon">🚪</span> Logout
        </button>
      </nav>

      <div className="sidebar-user">
        <div className="user-info">
          <div className="avatar">
            {user?.name?.charAt(0)?.toUpperCase()}
          </div>
          <div>
            <div className="user-name">{user?.name}</div>
            <div className="user-role">{user?.role}</div>
          </div>
        </div>
      </div>
    </aside>
  );
}
