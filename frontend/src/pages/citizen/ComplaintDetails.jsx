import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { complaintAPI, getImageUrl } from '../../services/api';

// ComplaintDetails page - detailed view of a single complaint
function ComplaintDetails() {
  const { id } = useParams();
  const [complaint, setComplaint] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadComplaint();
  }, [id]);

  const loadComplaint = async () => {
    try {
      const res = await complaintAPI.getById(id);
      setComplaint(res.data);
    } catch (error) {
      console.error('Error loading complaint:', error);
    } finally {
      setLoading(false);
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

  if (loading) {
    return (
      <div className="loading-spinner">
        <div className="spinner-border text-primary"><span className="visually-hidden">Loading...</span></div>
      </div>
    );
  }

  if (!complaint) {
    return (
      <div className="container py-5 text-center">
        <h4>Complaint not found</h4>
        <Link to="/citizen/my-complaints" className="btn btn-primary mt-3">Back to My Complaints</Link>
      </div>
    );
  }

  return (
    <>
      <div className="dashboard-header">
        <div className="container">
          <div className="d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div>
              <h2><i className="bi bi-file-text me-2"></i>Complaint Details</h2>
              <p className="mb-0">Complaint ID: #{complaint.complaintId}</p>
            </div>
            <Link to="/citizen/my-complaints" className="btn hero-btn hero-btn-outline py-2 px-4">
              <i className="bi bi-arrow-left me-2"></i>Back
            </Link>
          </div>
        </div>
      </div>

      <div className="container pb-5">
        <div className="row g-4">
          {/* Main Details */}
          <div className="col-lg-8">
            <div className="detail-card fade-in-up">
              <div className="detail-header">
                <div className="d-flex justify-content-between align-items-center">
                  <h4 className="mb-0">{complaint.complaintTitle}</h4>
                  <span className={`badge-status ${getStatusBadge(complaint.status)}`} style={{ fontSize: '0.9rem' }}>
                    {complaint.status}
                  </span>
                </div>
              </div>

              <div className="detail-body">
                <div className="detail-row">
                  <div className="detail-label">Department</div>
                  <div className="detail-value">{complaint.departmentName}</div>
                </div>
                <div className="detail-row">
                  <div className="detail-label">Area / Location</div>
                  <div className="detail-value">{complaint.areaLocation}</div>
                </div>
                <div className="detail-row">
                  <div className="detail-label">Description</div>
                  <div className="detail-value">{complaint.description}</div>
                </div>
                <div className="detail-row">
                  <div className="detail-label">Date Filed</div>
                  <div className="detail-value">
                    {new Date(complaint.complaintDate).toLocaleString()}
                  </div>
                </div>

                {complaint.complaintImage && (
                  <div className="detail-row">
                    <div className="detail-label">Complaint Image</div>
                    <div className="detail-value">
                      <img src={getImageUrl(complaint.complaintImage)} alt="Complaint"
                        className="complaint-image" />
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Side Panel */}
          <div className="col-lg-4">
            {/* Officer Info */}
            <div className="card-custom p-4 mb-4 fade-in-up fade-in-up-delay-1">
              <h6 className="fw-bold mb-3">
                <i className="bi bi-person-badge me-2 text-primary"></i>Assigned Officer
              </h6>
              {complaint.officerName ? (
                <div>
                  <p className="mb-1"><strong>Name:</strong> {complaint.officerName}</p>
                  <p className="mb-0"><strong>Mobile:</strong> {complaint.officerMobile}</p>
                </div>
              ) : (
                <p className="text-muted mb-0">Not yet assigned</p>
              )}
            </div>

            {/* Resolution Info */}
            {(complaint.status === 'In Progress' || complaint.status === 'Completed') && (
              <div className="card-custom p-4 mb-4 fade-in-up fade-in-up-delay-2">
                <h6 className="fw-bold mb-3">
                  <i className="bi bi-check-circle me-2 text-success"></i>Resolution Details
                </h6>
                {complaint.resolutionRemark && (
                  <p className="mb-2"><strong>Remark:</strong> {complaint.resolutionRemark}</p>
                )}
                {complaint.resolvedDate && (
                  <p className="mb-2"><strong>Resolved On:</strong> {new Date(complaint.resolvedDate).toLocaleString()}</p>
                )}
                {complaint.resolutionImage && (
                  <div className="mt-2">
                    <p className="mb-1"><strong>Proof Image:</strong></p>
                    <img src={getImageUrl(complaint.resolutionImage)} alt="Resolution"
                      className="complaint-image" />
                  </div>
                )}
              </div>
            )}

            {/* Status Tracker */}
            <div className="card-custom p-4 fade-in-up fade-in-up-delay-3">
              <h6 className="fw-bold mb-3">
                <i className="bi bi-clock-history me-2 text-info"></i>Status Timeline
              </h6>
              <div className="d-flex flex-column gap-3">
                <div className="d-flex align-items-center">
                  <div className="rounded-circle bg-success d-flex align-items-center justify-content-center me-3"
                    style={{ width: '32px', height: '32px', minWidth: '32px' }}>
                    <i className="bi bi-check text-white"></i>
                  </div>
                  <div>
                    <strong>Submitted</strong>
                    <p className="text-muted small mb-0">{new Date(complaint.complaintDate).toLocaleDateString()}</p>
                  </div>
                </div>

                <div className="d-flex align-items-center">
                  <div className={`rounded-circle d-flex align-items-center justify-content-center me-3 ${complaint.status !== 'Pending' ? 'bg-success' : 'bg-secondary bg-opacity-25'}`}
                    style={{ width: '32px', height: '32px', minWidth: '32px' }}>
                    <i className={`bi ${complaint.status !== 'Pending' ? 'bi-check text-white' : 'bi-hourglass text-muted'}`}></i>
                  </div>
                  <div>
                    <strong>In Progress</strong>
                    <p className="text-muted small mb-0">{complaint.status !== 'Pending' ? 'Officer is working on it' : 'Waiting'}</p>
                  </div>
                </div>

                <div className="d-flex align-items-center">
                  <div className={`rounded-circle d-flex align-items-center justify-content-center me-3 ${complaint.status === 'Completed' ? 'bg-success' : 'bg-secondary bg-opacity-25'}`}
                    style={{ width: '32px', height: '32px', minWidth: '32px' }}>
                    <i className={`bi ${complaint.status === 'Completed' ? 'bi-check text-white' : 'bi-flag text-muted'}`}></i>
                  </div>
                  <div>
                    <strong>Completed</strong>
                    <p className="text-muted small mb-0">
                      {complaint.status === 'Completed'
                        ? new Date(complaint.resolvedDate).toLocaleDateString()
                        : 'Pending resolution'}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default ComplaintDetails;
