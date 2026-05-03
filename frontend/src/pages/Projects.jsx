import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import API from '../api/axios';

export default function Projects() {
  const { isAdmin } = useAuth();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editProject, setEditProject] = useState(null);
  const [form, setForm] = useState({ name: '', description: '' });
  const [showMembers, setShowMembers] = useState(null);
  const [users, setUsers] = useState([]);
  const [addUserId, setAddUserId] = useState('');

  const fetchProjects = () => {
    API.get('/projects').then(res => { setProjects(res.data); setLoading(false); }).catch(() => setLoading(false));
  };

  useEffect(() => { fetchProjects(); }, []);

  const openCreate = () => { setForm({ name: '', description: '' }); setEditProject(null); setShowModal(true); };
  const openEdit = (p) => { setForm({ name: p.name, description: p.description || '' }); setEditProject(p); setShowModal(true); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editProject) { await API.put(`/projects/${editProject.id}`, form); }
      else { await API.post('/projects', form); }
      setShowModal(false);
      fetchProjects();
    } catch (err) { alert(err.response?.data?.message || 'Error'); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this project and all its tasks?')) return;
    try { await API.delete(`/projects/${id}`); fetchProjects(); }
    catch (err) { alert(err.response?.data?.message || 'Error'); }
  };

  const openMembers = async (project) => {
    setShowMembers(project);
    try { const res = await API.get('/projects/users'); setUsers(res.data); }
    catch { /* ignore */ }
  };

  const addMember = async () => {
    if (!addUserId) return;
    try {
      await API.post(`/projects/${showMembers.id}/members`, { userId: Number(addUserId), role: 'MEMBER' });
      const res = await API.get(`/projects/${showMembers.id}`);
      setShowMembers(res.data);
      setAddUserId('');
      fetchProjects();
    } catch (err) { alert(err.response?.data?.message || 'Error'); }
  };

  const removeMember = async (userId) => {
    try {
      await API.delete(`/projects/${showMembers.id}/members/${userId}`);
      const res = await API.get(`/projects/${showMembers.id}`);
      setShowMembers(res.data);
      fetchProjects();
    } catch (err) { alert(err.response?.data?.message || 'Error'); }
  };

  if (loading) return <div className="loading"><div className="spinner"></div></div>;

  return (
    <div>
      <div className="page-header">
        <div><h1>Projects</h1><p>Manage your team projects</p></div>
        {isAdmin() && <button className="btn btn-primary" onClick={openCreate} id="create-project-btn">+ New Project</button>}
      </div>

      {projects.length > 0 ? (
        <div className="projects-grid">
          {projects.map(p => (
            <div key={p.id} className="project-card">
              <h3>{p.name}</h3>
              <p>{p.description || 'No description'}</p>
              <div className="project-meta">
                <span>📋 {p.taskCount} tasks</span>
                <span>👥 {p.memberCount} members</span>
              </div>
              <div className="project-actions">
                <button className="btn btn-secondary btn-sm" onClick={() => openMembers(p)}>Members</button>
                {isAdmin() && <>
                  <button className="btn btn-secondary btn-sm" onClick={() => openEdit(p)}>Edit</button>
                  <button className="btn btn-danger btn-sm" onClick={() => handleDelete(p.id)}>Delete</button>
                </>}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="empty-state"><div className="empty-icon">📁</div><h3>No projects yet</h3>
          <p>{isAdmin() ? 'Create your first project' : 'No projects assigned to you'}</p></div>
      )}

      {/* Create/Edit Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>{editProject ? 'Edit Project' : 'Create Project'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Project Name</label>
                <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required placeholder="Enter project name" />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea rows="3" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Enter description" />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">{editProject ? 'Update' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Members Modal */}
      {showMembers && (
        <div className="modal-overlay" onClick={() => setShowMembers(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>Members — {showMembers.name}</h2>
            <div className="members-list">
              {showMembers.members?.map(m => (
                <div key={m.userId} className="member-item">
                  <div className="member-info">
                    <div className="member-avatar">{m.name?.charAt(0)}</div>
                    <div><div className="member-name">{m.name}</div><div className="member-email">{m.email}</div></div>
                  </div>
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                    <span className={`badge badge-${m.role.toLowerCase()}`}>{m.role}</span>
                    {isAdmin() && <button className="btn btn-danger btn-sm" onClick={() => removeMember(m.userId)}>✕</button>}
                  </div>
                </div>
              ))}
            </div>
            {isAdmin() && (
              <div style={{ marginTop: '20px', display: 'flex', gap: '8px' }}>
                <select value={addUserId} onChange={e => setAddUserId(e.target.value)} style={{ flex: 1, padding: '10px', background: 'var(--bg-input)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)', fontFamily: 'var(--font)' }}>
                  <option value="">Select user to add...</option>
                  {users.filter(u => !showMembers.members?.find(m => m.userId === u.id)).map(u => (
                    <option key={u.id} value={u.id}>{u.name} ({u.email})</option>
                  ))}
                </select>
                <button className="btn btn-primary btn-sm" onClick={addMember}>Add</button>
              </div>
            )}
            <div className="modal-actions"><button className="btn btn-secondary" onClick={() => setShowMembers(null)}>Close</button></div>
          </div>
        </div>
      )}
    </div>
  );
}
