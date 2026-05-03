import { useState, useEffect } from 'react';
import API from '../api/axios';

export default function Team() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    API.get('/projects/users')
      .then(res => { setUsers(res.data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading"><div className="spinner"></div></div>;

  const admins = users.filter(u => u.role === 'ADMIN');
  const members = users.filter(u => u.role === 'MEMBER');

  return (
    <div>
      <div className="page-header">
        <div><h1>Team</h1><p>All registered team members</p></div>
      </div>

      <div className="stats-grid" style={{ marginBottom: '32px' }}>
        <div className="stat-card accent">
          <div className="stat-icon">👥</div>
          <div className="stat-value">{users.length}</div>
          <div className="stat-label">Total Members</div>
        </div>
        <div className="stat-card info">
          <div className="stat-icon">🛡️</div>
          <div className="stat-value">{admins.length}</div>
          <div className="stat-label">Admins</div>
        </div>
        <div className="stat-card success">
          <div className="stat-icon">👤</div>
          <div className="stat-value">{members.length}</div>
          <div className="stat-label">Members</div>
        </div>
      </div>

      {admins.length > 0 && (
        <div className="section">
          <div className="section-title">🛡️ Admins</div>
          <div className="card">
            <div className="members-list">
              {admins.map(u => (
                <div key={u.id} className="member-item">
                  <div className="member-info">
                    <div className="member-avatar">{u.name?.charAt(0)}</div>
                    <div>
                      <div className="member-name">{u.name}</div>
                      <div className="member-email">{u.email}</div>
                    </div>
                  </div>
                  <span className="badge badge-admin">ADMIN</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {members.length > 0 && (
        <div className="section">
          <div className="section-title">👤 Members</div>
          <div className="card">
            <div className="members-list">
              {members.map(u => (
                <div key={u.id} className="member-item">
                  <div className="member-info">
                    <div className="member-avatar">{u.name?.charAt(0)}</div>
                    <div>
                      <div className="member-name">{u.name}</div>
                      <div className="member-email">{u.email}</div>
                    </div>
                  </div>
                  <span className="badge badge-member">MEMBER</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {users.length === 0 && (
        <div className="empty-state">
          <div className="empty-icon">👥</div>
          <h3>No team members</h3>
          <p>Users will appear here after signing up</p>
        </div>
      )}
    </div>
  );
}
