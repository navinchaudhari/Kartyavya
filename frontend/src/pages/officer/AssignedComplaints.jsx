import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { officerAPI } from '../../services/api';

// AssignedComplaints - list all complaints in officer's department
function AssignedComplaints() {
  const [complaints, setComplaints] = useState([]);
  const [filter, setFilter] = useState('All');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadComplaints();
  }, []);

  const loadComplaints = async () => {
    try {
      const res = await officerAPI.getComplaints({});
      setComplaints(res.data);
    } catch (error) {
      console.error('Error loading complaints:', error);
      setError('Failed to load complaints. Please check if the backend server is running.');
    } finally {
      setLoading(false);
    }
  };

  const filteredComplaints = filter === 'All'
    ? complaints
    : complaints.filter(c => c.status === filter);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'Pending': return 'badge-pending';
      case 'In Progress': return 'badge-inprogress';
      case 'Completed': return 'badge-completed';
      default: return '';
    }
  };

  if (loading) {
    return <div className="loading-spinner"><div className="spinner-border text-primary"></div></div>;
  }

  return (
    <>
      <div className="dashboard-header">
        <div className="container">
          <h2><i className="bi bi-clipboard-check me-2"></i>Assigned Complaints</h2>
          <p>View and manage complaints assigned to your department</p>
        </div>
      </div>

      <div className="container pb-5">
        {/* Error Alert */}
        {error && (
          <div className="alert alert-danger d-flex align-items-center mb-4">
            <i className="bi bi-exclamation-circle me-2"></i>{error}
          </div>
        )}

        {/* Filters */}
        <div className="d-flex gap-2 mb-4 flex-wrap">
          {['All', 'Pending', 'In Progress', 'Completed'].map(status => (
            <button key={status}
              className={`btn btn-sm ${filter === status ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => setFilter(status)}>
              {status} ({status === 'All' ? complaints.length : complaints.filter(c => c.status === status).length})
            </button>
          ))}
        </div>

        {filteredComplaints.length === 0 ? (
          <div className="card-custom">
            <div className="empty-state">
              <i className="bi bi-inbox"></i>
              <h5>No Complaints Found</h5>
              <p>No complaints match the selected filter.</p>
            </div>
          </div>
        ) : (
          <div className="card-custom">
            <div className="table-responsive">
              <table className="table table-custom mb-0">
                <thead>
                  <tr>
                    <th>Sr. No.</th>
                    <th>Title</th>
                    <th>Citizen</th>
                    <th>Mobile</th>
                    <th>Location</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredComplaints.map((c, index) => (
                    <tr key={c.complaintId}>
                      <td>{index + 1}</td>
                      <td className="fw-medium">{c.complaintTitle}</td>
                      <td>{c.citizenName}</td>
                      <td>{c.citizenMobile}</td>
                      <td>{c.areaLocation}</td>
                      <td>
                        <span className={`badge-status ${getStatusBadge(c.status)}`}>{c.status}</span>
                      </td>
                      <td>{new Date(c.complaintDate).toLocaleDateString()}</td>
                      <td>
                        <Link to={`/officer/update-complaint/${c.complaintId}`}
                          className="btn btn-sm btn-outline-primary">
                          {c.status === 'Completed' ? <i className="bi bi-eye"></i> : <i className="bi bi-pencil-square"></i>}
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

export default AssignedComplaints;
