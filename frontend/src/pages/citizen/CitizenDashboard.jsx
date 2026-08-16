import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { citizenAPI, complaintAPI } from '../../services/api';
import DashboardCard from '../../components/DashboardCard';

// CitizenDashboard - shows stats and recent complaints
function CitizenDashboard() {
  const [stats, setStats] = useState({});
  const [recentComplaints, setRecentComplaints] = useState([]);
  const [error, setError] = useState('');
  const fullName = localStorage.getItem('fullName');

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const [statsRes, complaintsRes] = await Promise.all([
        citizenAPI.getDashboard(),
        complaintAPI.getMyComplaints()
      ]);
      setStats(statsRes.data);
      setRecentComplaints(complaintsRes.data.slice(0, 5)); // Show latest 5
    } catch (error) {
      console.error('Error loading dashboard:', error);
      setError('Failed to load dashboard data. Please check if the backend server is running.');
    }
  };

  // Get badge class based on status
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
      {/* Dashboard Header */}
      <div className="dashboard-header">
        <div className="container">
          <h2><i className="bi bi-speedometer2 me-2"></i>Citizen Dashboard</h2>
          <p>Welcome back, {fullName}! Here's your complaint overview.</p>
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

        {/* Quick Actions */}
        <div className="row g-4 mb-4">
          <div className="col-md-6">
            <Link to="/citizen/add-complaint" className="card-custom p-4 d-block text-decoration-none"
              style={{ borderLeft: '4px solid var(--primary)' }}>
              <div className="d-flex align-items-center">
                <div className="stat-icon me-3" style={{ background: 'rgba(37, 99, 235, 0.1)', color: 'var(--primary)' }}>
                  <i className="bi bi-plus-circle"></i>
                </div>
                <div>
                  <h6 className="fw-bold mb-0 text-dark">Submit New Complaint</h6>
                  <small className="text-muted">Report a new municipal issue</small>
                </div>
                <i className="bi bi-chevron-right ms-auto text-muted"></i>
              </div>
            </Link>
          </div>
          <div className="col-md-6">
            <Link to="/citizen/my-complaints" className="card-custom p-4 d-block text-decoration-none"
              style={{ borderLeft: '4px solid var(--success)' }}>
              <div className="d-flex align-items-center">
                <div className="stat-icon me-3" style={{ background: 'rgba(16, 185, 129, 0.1)', color: 'var(--success)' }}>
                  <i className="bi bi-list-check"></i>
                </div>
                <div>
                  <h6 className="fw-bold mb-0 text-dark">View All Complaints</h6>
                  <small className="text-muted">Track your complaint status</small>
                </div>
                <i className="bi bi-chevron-right ms-auto text-muted"></i>
              </div>
            </Link>
          </div>
        </div>

        {/* Recent Complaints Table */}
        <div className="card-custom">
          <div className="p-4 d-flex justify-content-between align-items-center" style={{ borderBottom: '1px solid var(--border-color)' }}>
            <h5 className="fw-bold mb-0">Recent Complaints</h5>
            <Link to="/citizen/my-complaints" className="btn btn-sm btn-outline-primary">View All</Link>
          </div>

          {recentComplaints.length === 0 ? (
            <div className="empty-state">
              <i className="bi bi-inbox"></i>
              <h5>No Complaints Yet</h5>
              <p>You haven't submitted any complaints yet.</p>
              <Link to="/citizen/add-complaint" className="btn btn-primary-custom" style={{ width: 'auto', display: 'inline-block' }}>
                Submit Your First Complaint
              </Link>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-custom mb-0">
                <thead>
                  <tr>
                    <th>Sr. No.</th>
                    <th>Title</th>
                    <th>Department</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {recentComplaints.map((c, index) => (
                    <tr key={c.complaintId}>
                      <td>{index + 1}</td>
                      <td className="fw-medium">{c.complaintTitle}</td>
                      <td>{c.departmentName}</td>
                      <td>
                        <span className={`badge-status ${getStatusBadge(c.status)}`}>{c.status}</span>
                      </td>
                      <td>{new Date(c.complaintDate).toLocaleDateString()}</td>
                      <td>
                        <Link to={`/citizen/complaint/${c.complaintId}`} className="btn btn-sm btn-outline-primary">
                          <i className="bi bi-eye"></i>
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

export default CitizenDashboard;
