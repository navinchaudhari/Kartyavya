import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { complaintAPI } from '../../services/api';
import { toast } from 'react-toastify';
import { apiErrorMessage, confirmAction } from '../../utils/notifications';

// MyComplaints page - list all complaints of the logged-in citizen
function MyComplaints() {
  const [complaints, setComplaints] = useState([]);
  const [filter, setFilter] = useState('All');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadComplaints();
  }, []);

  const loadComplaints = async () => {
    try {
      const res = await complaintAPI.getMyComplaints();
      setComplaints(res.data);
    } catch (requestError) {
      const message = apiErrorMessage(requestError, 'Failed to load complaints.');
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  // Delete a complaint
  const handleDelete = async (id) => {
    const confirmed = await confirmAction({
      title: 'Delete complaint?',
      message: 'This action permanently removes the pending complaint.',
      confirmLabel: 'Delete complaint',
      danger: true,
    });
    if (!confirmed) return;

    try {
      await complaintAPI.delete(id);
      setComplaints((current) => current.filter((complaint) => complaint.complaintId !== id));
      toast.success('Complaint deleted successfully.');
    } catch (requestError) {
      toast.error(apiErrorMessage(requestError, 'Failed to delete complaint.'));
    }
  };

  // Filter complaints
  const filteredComplaints = filter === 'All'
    ? complaints
    : complaints.filter(c => c.status === filter);

  // Status badge
  const getStatusBadge = (status) => {
    switch (status) {
      case 'Pending': return 'badge-pending';
      case 'In Progress': return 'badge-inprogress';
      case 'Completed': return 'badge-completed';
      default: return '';
    }
  };

  if (loading) {
    return (
      <div className="loading-spinner">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="dashboard-header">
        <div className="container">
          <div className="d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div>
              <h2><i className="bi bi-list-check me-2"></i>My Complaints</h2>
              <p className="mb-0">Track and manage all your submitted complaints</p>
            </div>
            <Link to="/citizen/add-complaint" className="btn hero-btn hero-btn-primary py-2 px-4">
              <i className="bi bi-plus-circle me-2"></i>New Complaint
            </Link>
          </div>
        </div>
      </div>

      <div className="container pb-5">
        {/* Error Alert */}
        {error && (
          <div className="alert alert-danger d-flex align-items-center mb-4">
            <i className="bi bi-exclamation-circle me-2"></i>{error}
          </div>
        )}

        {/* Filter Buttons */}
        <div className="d-flex gap-2 mb-4 flex-wrap">
          {['All', 'Pending', 'In Progress', 'Completed'].map(status => (
            <button key={status}
              className={`btn btn-sm ${filter === status ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => setFilter(status)}>
              {status} ({status === 'All' ? complaints.length : complaints.filter(c => c.status === status).length})
            </button>
          ))}
        </div>

        {/* Complaints Table */}
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
                    <th>Department</th>
                    <th>Location</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredComplaints.map((c, index) => (
                    <tr key={c.complaintId}>
                      <td>{index + 1}</td>
                      <td className="fw-medium">{c.complaintTitle}</td>
                      <td>{c.departmentName}</td>
                      <td>{c.areaLocation}</td>
                      <td>
                        <span className={`badge-status ${getStatusBadge(c.status)}`}>{c.status}</span>
                      </td>
                      <td>{new Date(c.complaintDate).toLocaleDateString()}</td>
                      <td>
                        <div className="d-flex gap-1">
                          <Link to={`/citizen/complaint/${c.complaintId}`}
                            className="btn btn-sm btn-outline-primary" title="View">
                            <i className="bi bi-eye"></i>
                          </Link>
                          {c.status === 'Pending' && (
                            <>
                              <Link to={`/citizen/edit-complaint/${c.complaintId}`}
                                className="btn btn-sm btn-outline-warning" title="Edit">
                                <i className="bi bi-pencil"></i>
                              </Link>
                              <button className="btn btn-sm btn-outline-danger" title="Delete"
                                onClick={() => handleDelete(c.complaintId)}>
                                <i className="bi bi-trash"></i>
                              </button>
                            </>
                          )}
                        </div>
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

export default MyComplaints;
