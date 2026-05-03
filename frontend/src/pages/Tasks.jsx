import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import API from '../api/axios';

export default function Tasks() {
  const { isAdmin } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editTask, setEditTask] = useState(null);
  const [filterStatus, setFilterStatus] = useState('');
  const [filterProject, setFilterProject] = useState('');
  const [form, setForm] = useState({ title: '', description: '', projectId: '', assignedTo: '', priority: 'MEDIUM', dueDate: '', status: 'TODO' });

  const fetchAll = async () => {
    try {
      const [t, p, u] = await Promise.all([API.get('/tasks'), API.get('/projects'), API.get('/projects/users')]);
      setTasks(t.data); setProjects(p.data); setUsers(u.data);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchAll(); }, []);

  const openCreate = () => {
    setForm({ title: '', description: '', projectId: projects[0]?.id || '', assignedTo: '', priority: 'MEDIUM', dueDate: '', status: 'TODO' });
    setEditTask(null); setShowModal(true);
  };

  const openEdit = (t) => {
    setForm({ title: t.title, description: t.description || '', projectId: t.projectId, assignedTo: t.assignedToId || '', priority: t.priority, dueDate: t.dueDate || '', status: t.status });
    setEditTask(t); setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = { ...form, projectId: Number(form.projectId), assignedTo: form.assignedTo ? Number(form.assignedTo) : null };
    try {
      if (editTask) await API.put(`/tasks/${editTask.id}`, payload);
      else await API.post('/tasks', payload);
      setShowModal(false); fetchAll();
    } catch (err) { alert(err.response?.data?.message || 'Error'); }
  };

  const updateStatus = async (id, status) => {
    try { await API.patch(`/tasks/${id}/status`, { status }); fetchAll(); }
    catch (err) { alert(err.response?.data?.message || 'Error'); }
  };

  const deleteTask = async (id) => {
    if (!window.confirm('Delete this task?')) return;
    try { await API.delete(`/tasks/${id}`); fetchAll(); } catch (err) { alert(err.response?.data?.message || 'Error'); }
  };

  const filtered = tasks.filter(t => {
    if (filterStatus && t.status !== filterStatus) return false;
    if (filterProject && t.projectId !== Number(filterProject)) return false;
    return true;
  });

  if (loading) return <div className="loading"><div className="spinner"></div></div>;

  return (
    <div>
      <div className="page-header">
        <div><h1>Tasks</h1><p>Track and manage all tasks</p></div>
        {isAdmin() && <button className="btn btn-primary" onClick={openCreate} id="create-task-btn">+ New Task</button>}
      </div>

      <div className="filter-bar">
        <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
          <option value="">All Status</option>
          <option value="TODO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="DONE">Done</option>
        </select>
        <select value={filterProject} onChange={e => setFilterProject(e.target.value)}>
          <option value="">All Projects</option>
          {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
        </select>
        <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{filtered.length} tasks</span>
      </div>

      {filtered.length > 0 ? (
        <div className="card"><div className="task-list">
          {filtered.map(t => (
            <div key={t.id} className={`task-item ${t.overdue ? 'overdue' : ''}`}>
              <div className="task-info">
                <div className="task-title">{t.title}</div>
                <div className="task-meta">
                  <span>📁 {t.projectName}</span>
                  <span>👤 {t.assignedToName || 'Unassigned'}</span>
                  {t.dueDate && <span>📅 {t.dueDate}</span>}
                  {t.overdue && <span className="badge badge-overdue">OVERDUE</span>}
                </div>
              </div>
              <div className="task-actions">
                <span className={`badge badge-${t.priority.toLowerCase()}`}>{t.priority}</span>
                <select className="status-select" value={t.status} onChange={e => updateStatus(t.id, e.target.value)}>
                  <option value="TODO">To Do</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="DONE">Done</option>
                </select>
                {isAdmin() && <>
                  <button className="btn btn-secondary btn-sm" onClick={() => openEdit(t)}>✏️</button>
                  <button className="btn btn-danger btn-sm" onClick={() => deleteTask(t.id)}>🗑️</button>
                </>}
              </div>
            </div>
          ))}
        </div></div>
      ) : (
        <div className="empty-state"><div className="empty-icon">✅</div><h3>No tasks found</h3></div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>{editTask ? 'Edit Task' : 'Create Task'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group"><label>Title</label>
                <input value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} required placeholder="Task title" /></div>
              <div className="form-group"><label>Description</label>
                <textarea rows="3" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Task description" /></div>
              <div className="form-group"><label>Project</label>
                <select value={form.projectId} onChange={e => setForm({ ...form, projectId: e.target.value })} required>
                  <option value="">Select project</option>
                  {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select></div>
              <div className="form-group"><label>Assign To</label>
                <select value={form.assignedTo} onChange={e => setForm({ ...form, assignedTo: e.target.value })}>
                  <option value="">Unassigned</option>
                  {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
                </select></div>
              <div className="form-group"><label>Priority</label>
                <select value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>
                  <option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option>
                </select></div>
              <div className="form-group"><label>Due Date</label>
                <input type="date" value={form.dueDate} onChange={e => setForm({ ...form, dueDate: e.target.value })} /></div>
              {editTask && <div className="form-group"><label>Status</label>
                <select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                  <option value="TODO">To Do</option><option value="IN_PROGRESS">In Progress</option><option value="DONE">Done</option>
                </select></div>}
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">{editTask ? 'Update' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
