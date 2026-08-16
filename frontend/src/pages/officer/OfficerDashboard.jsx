import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { officerAPI } from '../../services/api';
import DashboardCard from '../../components/DashboardCard';

// OfficerDashboard - shows department complaints stats and recent complaints
function OfficerDashboard() {
  const [stats, setStats] = useState({});
  const [complaints, setComplaints] = useState([]);
  const [error, setError] = useState('');
  const fullName = localStorage.getItem('fullName');

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const [statsRes, complaintsRes] = await Promise.all([
        officerAPI.getDashboard(),
        officerAPI.getComplaints({})
      ]);
      setStats(statsRes.data);
      setComplaints(complaintsRes.data.slice(0, 5));
    } catch (error) {
      console.error('Error loading dashboard:', error);
      setError('Failed to load dashboard data. Please check if the backend server is running.');
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'Pending': return 'badge-pending';
      case 'In Progress': return 'badge-inprogress';
      case 'Completed': return 'badge-completed';
      default: return '';
    }
  };

  return (
    <>
      <div className="dashboard-header">
        <div className="container">
          <h2><i className="bi bi-speedometer2 me-2"></i>Officer Dashboard</h2>
          <p>Welcome, {fullName}! Here's your department's complaint overview.</p>
        </div>
      </div>

      <div className="container pb-5">
        {/* Error Alert */}
        {error && (
          <div className="alert alert-danger d-flex align-items-center mb-4">
            <i className="bi bi-exclamation-circle me-2"></i>{error}
          </div>
        )}

        {/* Stat Cards */}
        <div className="row g-4 mb-4">
          <div className="col-lg-3 col-md-6">
            <DashboardCard title="Total Complaints" value={stats.totalComplaints || 0}
              icon="bi-clipboard-data" color="primary" />
          </div>
          <div className="col-lg-3 col-md-6">
            <DashboardCard title="Pending" value={stats.pendingComplaints || 0}
              icon="bi-hourglass-split" color="warning" />
          </div>
          <div className="col-lg-3 col-md-6">
            <DashboardCard title="In Progress" value={stats.inProgressComplaints || 0}
              icon="bi-arrow-repeat" color="info" />
          </div>
          <div className="col-lg-3 col-md-6">
            <DashboardCard title="Completed" value={stats.completedComplaints || 0}
              icon="bi-check-circle" color="success" />
          </div>
        </div>

        {/* Recent Complaints */}
        <div className="card-custom">
          <div className="p-4 d-flex justify-content-between align-items-center"
            style={{ borderBottom: '1px solid var(--border-color)' }}>
            <h5 className="fw-bold mb-0">Recent Assigned Complaints</h5>
            <Link to="/officer/complaints" className="btn btn-sm btn-outline-primary">View All</Link>
          </div>

          {complaints.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox"></i>
              <h5>No Complaints</h5>
              <p>No complaints are assigned to your department yet.</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-custom mb-0">
                <thead>
                  <tr>
                    <th>Sr. No.</th>
                    <th>Title</th>
                    <th>Citizen</th>
                    <th>Location</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {complaints.map((c, index) => (
                    <tr key={c.complaintId}>
                      <td>{index + 1}</td>
                      <td className="fw-medium">{c.complaintTitle}</td>
                      <td>{c.citizenName}</td>
                      <td>{c.areaLocation}</td>
                      <td>
                        <span className={`badge-status ${getStatusBadge(c.status)}`}>{c.status}</span>
                      </td>
                      <td>{new Date(c.complaintDate).toLocaleDateString()}</td>
                      <td>
                        <Link to={`/officer/update-complaint/${c.complaintId}`}
                          className="btn btn-sm btn-outline-primary">
                          <i className="bi bi-pencil-square"></i>
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </>
  );
}

export default OfficerDashboard;
