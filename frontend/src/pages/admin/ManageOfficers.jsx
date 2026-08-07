import React, { useState, useEffect } from 'react';
import { adminAPI, departmentAPI } from '../../services/api';
import { toast } from 'react-toastify';
import { apiErrorMessage, confirmAction } from '../../utils/notifications';

// ManageOfficers - CRUD for officers with department assignment
function ManageOfficers() {
  const [officers, setOfficers] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState(null);
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');

  // Form data
  const [formData, setFormData] = useState({
    fullName: '', email: '', password: '', mobileNumber: '', address: '', departmentId: ''
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [officersRes, deptRes] = await Promise.all([
        adminAPI.getOfficers(),
        departmentAPI.getAll()
      ]);
      setOfficers(officersRes.data);
      setDepartments(deptRes.data);
    } catch (error) {
      console.error('Error:', error);
      setError('Failed to load officers. Please check if the backend server is running.');
    }
  };

  const validateField = (name, value) => {
    let tempErrors = { ...errors };

    switch (name) {
      case 'fullName':
        if (!value.trim()) {
          tempErrors.fullName = 'Full name is required';
        } else if (value.trim().length < 3) {
          tempErrors.fullName = 'Full name must be at least 3 characters';
        } else if (!/^[A-Za-z\s]+$/.test(value)) {
          tempErrors.fullName = 'Full name must contain only letters and spaces';
        } else {
          delete tempErrors.fullName;
        }
        break;

      case 'email':
        if (!value.trim()) {
          tempErrors.email = 'Email address is required';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          tempErrors.email = 'Invalid email address format';
        } else {
          delete tempErrors.email;
        }
        break;

      case 'password':
        if (!editId) {
          if (!value) {
            tempErrors.password = 'Password is required';
          } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/.test(value)) {
            tempErrors.password = 'Password must be 8–72 characters with uppercase, lowercase, number and special character';
          } else {
            delete tempErrors.password;
          }
        }
        break;

      case 'mobileNumber':
        if (!value.trim()) {
          tempErrors.mobileNumber = 'Mobile number is required';
        } else if (!/^[6-9]\d{9}$/.test(value.trim())) {
          tempErrors.mobileNumber = 'Enter a valid 10-digit Indian mobile number';
        } else {
          delete tempErrors.mobileNumber;
        }
        break;

      case 'departmentId':
        if (!value) {
          tempErrors.departmentId = 'Department assignment is required';
        } else {
          delete tempErrors.departmentId;
        }
        break;

      default:
        break;
    }

    setErrors(tempErrors);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    validateField(name, value);
  };

  const handleAdd = () => {
    setFormData({ fullName: '', email: '', password: '', mobileNumber: '', address: '', departmentId: '' });
    setEditId(null);
    setErrors({});
    setShowForm(true);
    setError('');
  };

  const handleEdit = (officer) => {
    setFormData({
      fullName: officer.fullName,
      email: officer.email,
      password: '',
      mobileNumber: officer.mobileNumber,
      address: officer.address || '',
      departmentId: officer.departmentId || ''
    });
    setEditId(officer.userId);
    setErrors({});
    setShowForm(true);
    setError('');
  };

  const validateAll = () => {
    let tempErrors = {};

    if (!formData.fullName.trim()) {
      tempErrors.fullName = 'Full name is required';
    } else if (formData.fullName.trim().length < 3) {
      tempErrors.fullName = 'Full name must be at least 3 characters';
    } else if (!/^[A-Za-z\s]+$/.test(formData.fullName)) {
      tempErrors.fullName = 'Full name must contain only letters and spaces';
    }

    if (!editId) {
      if (!formData.email.trim()) {
        tempErrors.email = 'Email address is required';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
        tempErrors.email = 'Invalid email address format';
      }

      if (!formData.password) {
        tempErrors.password = 'Password is required';
      } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/.test(formData.password)) {
        tempErrors.password = 'Password must be 8–72 characters with uppercase, lowercase, number and special character';
      }
    }

    if (!formData.mobileNumber.trim()) {
      tempErrors.mobileNumber = 'Mobile number is required';
    } else if (!/^[6-9]\d{9}$/.test(formData.mobileNumber.trim())) {
      tempErrors.mobileNumber = 'Enter a valid 10-digit Indian mobile number';
    }

    if (!formData.departmentId) {
      tempErrors.departmentId = 'Department assignment is required';
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

    try {
      if (editId) {
        // Update officer (no password change)
        await adminAPI.updateOfficer(editId, {
          fullName: formData.fullName,
          mobileNumber: formData.mobileNumber,
          address: formData.address,
          departmentId: parseInt(formData.departmentId)
        });
        toast.success('Officer updated successfully.');
      } else {
        // Add new officer
        await adminAPI.addOfficer({
          ...formData,
          departmentId: parseInt(formData.departmentId)
        });
        toast.success('Officer added successfully.');
      }
      setShowForm(false);
      loadData();
    } catch (err) {
      const message = apiErrorMessage(err, 'Officer operation failed.');
      setError(message);
      toast.error(message);
    }
  };

  const handleDelete = async (id) => {
    const confirmed = await confirmAction({
      title: 'Delete officer?',
      message: 'The officer account and its department assignment will be removed.',
      confirmLabel: 'Delete officer',
      danger: true,
    });
    if (!confirmed) return;

    try {
      await adminAPI.deleteOfficer(id);
      toast.success('Officer deleted successfully.');
      await loadData();
    } catch (err) {
      toast.error(apiErrorMessage(err, 'Failed to delete officer.'));
    }
  };

  return (
    <>
      <div className="dashboard-header">
        <div className="container">
          <div className="d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div>
              <h2><i className="bi bi-people me-2"></i>Manage Officers</h2>
              <p className="mb-0">Add, edit, and delete officers and assign departments</p>
            </div>
            <button className="btn hero-btn hero-btn-primary py-2 px-4" onClick={handleAdd}>
              <i className="bi bi-person-plus me-2"></i>Add Officer
            </button>
          </div>
        </div>
      </div>

      <div className="container pb-5">
        {/* Error Alert */}
        {error && !showForm && (
          <div className="alert alert-danger d-flex align-items-center mb-4">
            <i className="bi bi-exclamation-circle me-2"></i>{error}
          </div>
        )}

        {/* Add/Edit Form */}
        {showForm && (
          <div className="card-custom p-4 mb-4 fade-in-up">
            <h5 className="fw-bold mb-3">{editId ? 'Edit Officer' : 'Add New Officer'}</h5>

            {error && <div className="alert alert-danger py-2">{error}</div>}

            <form onSubmit={handleSubmit} noValidate>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">Full Name *</label>
                  <input type="text" className={`form-control ${errors.fullName ? 'is-invalid' : ''}`} name="fullName"
                    value={formData.fullName}
                    onChange={handleFormChange} />
                  {errors.fullName && <div className="text-danger small mt-1">{errors.fullName}</div>}
                </div>
                <div className="col-md-6">
                  <label className="form-label">Email *</label>
                  <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`} name="email" disabled={!!editId}
                    value={formData.email}
                    onChange={handleFormChange} />
                  {errors.email && <div className="text-danger small mt-1">{errors.email}</div>}
                </div>
                {!editId && (
                  <div className="col-md-6">
                    <label className="form-label">Password *</label>
                    <input type="password" className={`form-control ${errors.password ? 'is-invalid' : ''}`} name="password"
                      value={formData.password}
                      onChange={handleFormChange} />
                    {errors.password && <div className="text-danger small mt-1">{errors.password}</div>}
                  </div>
                )}
                <div className="col-md-6">
                  <label className="form-label">Mobile Number *</label>
                  <input type="text" className={`form-control ${errors.mobileNumber ? 'is-invalid' : ''}`} name="mobileNumber"
                    value={formData.mobileNumber}
                    onChange={handleFormChange} />
                  {errors.mobileNumber && <div className="text-danger small mt-1">{errors.mobileNumber}</div>}
                </div>
                <div className="col-md-6">
                  <label className="form-label">Department *</label>
                  <select className={`form-select ${errors.departmentId ? 'is-invalid' : ''}`} name="departmentId"
                    value={formData.departmentId}
                    onChange={handleFormChange}>
                    <option value="">-- Select Department --</option>
                    {departments.map(dept => (
                      <option key={dept.departmentId} value={dept.departmentId}>
                        {dept.departmentName}
                      </option>
                    ))}
                  </select>
                  {errors.departmentId && <div className="text-danger small mt-1">{errors.departmentId}</div>}
                </div>
                <div className="col-md-6">
                  <label className="form-label">Address</label>
                  <input type="text" className="form-control" name="address"
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })} />
                </div>
                <div className="col-12 mt-3">
                  <button type="submit" className="btn btn-primary me-2">
                    {editId ? 'Update Officer' : 'Add Officer'}
                  </button>
                  <button type="button" className="btn btn-outline-secondary"
                    onClick={() => setShowForm(false)}>Cancel</button>
                </div>
              </div>
            </form>
          </div>
        )}

        {/* Officers Table */}
        <div className="card-custom">
          <div className="table-responsive">
            <table className="table table-custom mb-0">
              <thead>
                <tr>
                  <th>Sr. No.</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Mobile</th>
                  <th>Department</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {officers.length === 0 ? (
                  <tr><td colSpan="6" className="text-center py-4 text-muted">No officers found.</td></tr>
                ) : (
                  officers.map((officer, index) => (
                    <tr key={officer.userId}>
                      <td>{index + 1}</td>
                      <td className="fw-medium">{officer.fullName}</td>
                      <td>{officer.email}</td>
                      <td>{officer.mobileNumber}</td>
                      <td>
                        <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-1">
                          {officer.departmentName || 'Not Assigned'}
                        </span>
                      </td>
                      <td>
                        <div className="d-flex gap-1">
                          <button className="btn btn-sm btn-outline-warning" onClick={() => handleEdit(officer)}>
                            <i className="bi bi-pencil"></i>
                          </button>
                          <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(officer.userId)}>
                            <i className="bi bi-trash"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </>
  );
}

export default ManageOfficers;
