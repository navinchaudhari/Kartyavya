import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { complaintAPI, officerAPI, getImageUrl } from '../../services/api';
import { toast } from 'react-toastify';
import { apiErrorMessage } from '../../utils/notifications';

// UpdateComplaint - officer can update status, add remark, upload resolution image
function UpdateComplaint() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [complaint, setComplaint] = useState(null);
  const [status, setStatus] = useState('');
  const [remark, setRemark] = useState('');
  const [resolutionImage, setResolutionImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadComplaint();
  }, [id]);

  const loadComplaint = async () => {
    try {
      const res = await complaintAPI.getById(id);
      setComplaint(res.data);
      setStatus(res.data.status);
      setRemark(res.data.resolutionRemark || '');
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  const validateField = (name, value) => {
    let tempErrors = { ...errors };

    switch (name) {
      case 'status':
        if (!value) {
          tempErrors.status = 'Status is required';
        } else {
          delete tempErrors.status;
        }
        // Recalculate remark validation on status change
        if (value === 'Completed' && !remark.trim()) {
          tempErrors.remark = 'Resolution remark is required when marking as Completed';
        } else if (value === 'Completed' && remark.trim().length < 5) {
          tempErrors.remark = 'Resolution remark must be at least 5 characters';
        } else {
          delete tempErrors.remark;
        }
        break;

      case 'remark':
        if (status === 'Completed' && !value.trim()) {
          tempErrors.remark = 'Resolution remark is required when marking as Completed';
        } else if (status === 'Completed' && value.trim().length < 5) {
          tempErrors.remark = 'Resolution remark must be at least 5 characters';
        } else {
          delete tempErrors.remark;
        }
        break;

      default:
        break;
    }

    setErrors(tempErrors);
  };

  const handleStatusChange = (e) => {
    const val = e.target.value;
    setStatus(val);
    validateField('status', val);
  };

  const handleRemarkChange = (e) => {
    const val = e.target.value;
    setRemark(val);
    validateField('remark', val);
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setResolutionImage(file);
      setImagePreview(URL.createObjectURL(file));
      
      let tempErrors = { ...errors };
      delete tempErrors.resolutionImage;
      setErrors(tempErrors);
    }
  };

  const validateAll = () => {
    let tempErrors = {};

    if (!status) {
      tempErrors.status = 'Status is required';
    }

    if (status === 'Completed') {
      if (!remark.trim()) {
        tempErrors.remark = 'Resolution remark is required when marking as Completed';
      } else if (remark.trim().length < 5) {
        tempErrors.remark = 'Resolution remark must be at least 5 characters';
      }

      if (!resolutionImage && !complaint.resolutionImage) {
        tempErrors.resolutionImage = 'Resolution proof image is required to complete the complaint';
      }
    }

    setErrors(tempErrors);
    return Object.keys(tempErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!validateAll()) {
      return;
    }

    setUpdating(true);

    try {
      const data = new FormData();
      data.append('status', status);
      data.append('resolutionRemark', remark);
      if (resolutionImage) {
        data.append('resolutionImage', resolutionImage);
      }

      await officerAPI.updateStatus(id, data);
      toast.success(`Complaint status updated to "${status}".`);
      navigate('/officer/complaints');
    } catch (err) {
      const message = apiErrorMessage(err, 'Failed to update complaint.');
      setError(message);
      toast.error(message);
    } finally {
      setUpdating(false);
    }
  };

  const getStatusBadge = (s) => {
    switch (s) {
      case 'Pending': return 'badge-pending';
      case 'In Progress': return 'badge-inprogress';
      case 'Completed': return 'badge-completed';
      default: return '';
    }
  };

  if (loading) return <div className="loading-spinner"><div className="spinner-border text-primary"></div></div>;
  if (!complaint) return <div className="container py-5 text-center"><h4>Complaint not found</h4></div>;

  return (
    <>
      <div className="dashboard-header">
        <div className="container">
          <h2><i className="bi bi-pencil-square me-2"></i>Update Complaint</h2>
          <p>Complaint ID: #{complaint.complaintId} | Current Status: {complaint.status}</p>
        </div>
      </div>

      <div className="container pb-5">
        <div className="row g-4">
          {/* Complaint Details */}
          <div className="col-lg-7">
            <div className="detail-card fade-in-up">
              <div className="detail-header">
                <h4 className="mb-0">{complaint.complaintTitle}</h4>
              </div>
              <div className="detail-body">
                <div className="detail-row">
                  <div className="detail-label">Citizen Name</div>
                  <div className="detail-value">{complaint.citizenName}</div>
                </div>
                <div className="detail-row">
                  <div className="detail-label">Citizen Mobile</div>
                  <div className="detail-value">{complaint.citizenMobile}</div>
                </div>
                <div className="detail-row">
                  <div className="detail-label">Department</div>
                  <div className="detail-value">{complaint.departmentName}</div>
                </div>
                <div className="detail-row">
                  <div className="detail-label">Location</div>
                  <div className="detail-value">{complaint.areaLocation}</div>
                </div>
                <div className="detail-row">
                  <div className="detail-label">Description</div>
                  <div className="detail-value">{complaint.description}</div>
                </div>
                <div className="detail-row">
                  <div className="detail-label">Date Filed</div>
                  <div className="detail-value">{new Date(complaint.complaintDate).toLocaleString()}</div>
                </div>
                {complaint.complaintImage && (
                  <div className="detail-row">
                    <div className="detail-label">Complaint Image</div>
                    <div className="detail-value">
                      <img src={getImageUrl(complaint.complaintImage)} alt="Complaint" className="complaint-image" />
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Update Form */}
          <div className="col-lg-5">
            <div className="card-custom p-4 fade-in-up fade-in-up-delay-1">
              <h5 className="fw-bold mb-4">
                <i className="bi bi-gear me-2"></i>Update Status
              </h5>

              {error && (
                <div className="alert alert-danger py-2">
                  <i className="bi bi-exclamation-circle me-2"></i>{error}
                </div>
              )}

              {complaint.status === 'Completed' ? (
                <div className="alert alert-success">
                  <i className="bi bi-check-circle me-2"></i>
                  This complaint has been resolved on {new Date(complaint.resolvedDate).toLocaleDateString()}.
                  {complaint.resolutionRemark && (
                    <p className="mt-2 mb-0"><strong>Remark:</strong> {complaint.resolutionRemark}</p>
                  )}
                </div>
              ) : (
                <form onSubmit={handleSubmit} noValidate>
                  {/* Status Selection */}
                  <div className="mb-3">
                    <label className="form-label">Change Status *</label>
                    <select className={`form-select ${errors.status ? 'is-invalid' : ''}`} value={status}
                      onChange={handleStatusChange}>
                      <option value="Pending" disabled={complaint.status !== 'Pending'}>Pending</option>
                      <option value="In Progress">In Progress</option>
                      <option value="Completed" disabled={complaint.status === 'Pending'}>Completed</option>
                    </select>
                    {errors.status && <div className="text-danger small mt-1">{errors.status}</div>}
                    <small className="text-muted d-block mt-1">
                      Flow: Pending → In Progress → Completed
                    </small>
                  </div>

                  {/* Resolution Remark */}
                  <div className="mb-3">
                    <label className="form-label">Resolution Remark {status === 'Completed' && '*'}</label>
                    <textarea className={`form-control ${errors.remark ? 'is-invalid' : ''}`} rows="3"
                      placeholder="Add a remark about the resolution..."
                      value={remark} onChange={handleRemarkChange} />
                    {errors.remark && <div className="text-danger small mt-1">{errors.remark}</div>}
                  </div>

                  {/* Resolution Image */}
                  <div className="mb-4">
                    <label className="form-label">Upload Resolution Proof (Image) {status === 'Completed' && '*'}</label>
                    <input type="file" className={`form-control ${errors.resolutionImage ? 'is-invalid' : ''}`} accept="image/*"
                      onChange={handleImageChange} />
                    {errors.resolutionImage && <div className="text-danger small mt-1">{errors.resolutionImage}</div>}
                    {imagePreview && (
                      <img src={imagePreview} alt="Resolution Preview" className="complaint-image mt-2" />
                    )}
                  </div>

                  <div className="d-flex gap-2">
                    <button type="submit" className="btn btn-primary-custom" style={{ width: 'auto' }}
                      disabled={updating}>
                      {updating ? 'Updating...' : <><i className="bi bi-check-lg me-1"></i>Update Status</>}
                    </button>
                    <button type="button" className="btn btn-outline-secondary"
                      onClick={() => navigate('/officer/complaints')}>
                      Back
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default UpdateComplaint;
