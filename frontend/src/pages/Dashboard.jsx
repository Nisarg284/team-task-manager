import { useState, useEffect } from 'react';
import API from '../api/axios';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    API.get('/dashboard').then(res => { setData(res.data); setLoading(false); })
      .catch((err) => { setLoading(false); console.error('Dashboard API error:', err); });
  }, []);

  if (loading) return <div className="loading"><div className="spinner"></div></div>;
  if (!data) return <div className="empty-state"><h3>Failed to load dashboard</h3></div>;

  return (
    <div>
      <div className="page-header"><div><h1>Dashboard</h1><p>Overview of your projects and tasks</p></div></div>

      <div className="stats-grid">
        <div className="stat-card accent">
          <div className="stat-icon">📁</div>
          <div className="stat-value">{data.totalProjects}</div>
          <div className="stat-label">Total Projects</div>
        </div>
        <div className="stat-card info">
          <div className="stat-icon">📋</div>
          <div className="stat-value">{data.totalTasks}</div>
          <div className="stat-label">Total Tasks</div>
        </div>
        <div className="stat-card warning">
          <div className="stat-icon">🔄</div>
          <div className="stat-value">{data.inProgressTasks}</div>
          <div className="stat-label">In Progress</div>
        </div>
        <div className="stat-card success">
          <div className="stat-icon">✅</div>
          <div className="stat-value">{data.doneTasks}</div>
          <div className="stat-label">Completed</div>
        </div>
        <div className="stat-card danger">
          <div className="stat-icon">⚠️</div>
          <div className="stat-value">{data.overdueTasks}</div>
          <div className="stat-label">Overdue</div>
        </div>
        <div className="stat-card accent">
          <div className="stat-icon">👥</div>
          <div className="stat-value">{data.totalMembers}</div>
          <div className="stat-label">Team Members</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        <div className="card">
          <div className="card-header"><h2>📌 Recent Tasks</h2></div>
          {data.recentTasks?.length > 0 ? (
            <div className="task-list">
              {data.recentTasks.map(task => (
                <div key={task.id} className={`task-item ${task.overdue ? 'overdue' : ''}`}>
                  <div className="task-info">
                    <div className="task-title">{task.title}</div>
                    <div className="task-meta">
                      <span>{task.projectName}</span>
                      <span>{task.assignedToName || 'Unassigned'}</span>
                      {task.dueDate && <span>Due: {task.dueDate}</span>}
                    </div>
                  </div>
                  <span className={`badge badge-${task.status.toLowerCase().replace('_', '-')}`}>{task.status.replace('_', ' ')}</span>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state"><div className="empty-icon">📋</div><h3>No tasks yet</h3></div>
          )}
        </div>

        <div className="card">
          <div className="card-header"><h2>⚠️ Overdue Tasks</h2></div>
          {data.overdueTaksList?.length > 0 ? (
            <div className="task-list">
              {data.overdueTaksList.map(task => (
                <div key={task.id} className="task-item overdue">
                  <div className="task-info">
                    <div className="task-title">{task.title}</div>
                    <div className="task-meta">
                      <span>{task.projectName}</span>
                      <span>Due: {task.dueDate}</span>
                      <span className="badge badge-overdue">OVERDUE</span>
                    </div>
                  </div>
                  <span className={`badge badge-${task.priority.toLowerCase()}`}>{task.priority}</span>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state"><div className="empty-icon">🎉</div><h3>No overdue tasks!</h3><p>Great job keeping up</p></div>
          )}
        </div>
      </div>
    </div>
  );
}
