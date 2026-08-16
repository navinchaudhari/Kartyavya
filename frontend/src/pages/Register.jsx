import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authAPI } from '../services/api';
import { toast } from 'react-toastify';
import { apiErrorMessage } from '../utils/notifications';

// Register page - citizen registration form
function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    fullName: '', email: '', password: '', mobileNumber: '', address: ''
  });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

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
        if (!value) {
          tempErrors.password = 'Password is required';
        } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/.test(value)) {
          tempErrors.password = 'Password must be 8–72 characters with uppercase, lowercase, number and special character';
        } else {
          delete tempErrors.password;
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

      default:
        break;
    }

    setErrors(tempErrors);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    validateField(name, value);
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

    if (!formData.mobileNumber.trim()) {
      tempErrors.mobileNumber = 'Mobile number is required';
    } else if (!/^[6-9]\d{9}$/.test(formData.mobileNumber.trim())) {
      tempErrors.mobileNumber = 'Enter a valid 10-digit Indian mobile number';
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

    setLoading(true);

    try {
      await authAPI.register(formData);
      toast.success('Registration successful. Sign in with your new account.');
      navigate('/login', { replace: true });
    } catch (err) {
      const message = apiErrorMessage(err, 'Registration failed. Please try again.');
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card fade-in-up" style={{ maxWidth: '520px' }}>
        {/* Logo */}
        <div className="text-center mb-4">
          <i className="bi bi-shield-check" style={{ fontSize: '2.5rem', color: 'var(--primary)' }}></i>
        </div>

        <h2 className="text-center">Create Account</h2>
        <p className="text-center">Register as a citizen to start filing complaints</p>

        {/* Error Alert */}
        {error && (
          <div className="alert alert-danger d-flex align-items-center py-2">
            <i className="bi bi-exclamation-circle me-2"></i>{error}
          </div>
        )}

        {/* Registration Form */}
        <form onSubmit={handleSubmit} noValidate>
          <div className="mb-3">
            <label className="form-label">Full Name *</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-person"></i></span>
              <input type="text" className={`form-control ${errors.fullName ? 'is-invalid' : ''}`} name="fullName"
                placeholder="Enter your full name"
                value={formData.fullName} onChange={handleChange} />
            </div>
            {errors.fullName && <div className="text-danger small mt-1">{errors.fullName}</div>}
          </div>

          <div className="mb-3">
            <label className="form-label">Email Address *</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-envelope"></i></span>
              <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`} name="email"
                placeholder="Enter your email"
                value={formData.email} onChange={handleChange} />
            </div>
            {errors.email && <div className="text-danger small mt-1">{errors.email}</div>}
          </div>

          <div className="mb-3">
            <label className="form-label">Password *</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-lock"></i></span>
              <input type="password" className={`form-control ${errors.password ? 'is-invalid' : ''}`} name="password"
                placeholder="8+ chars: upper, lower, number, special"
                value={formData.password} onChange={handleChange} />
            </div>
            {errors.password && <div className="text-danger small mt-1">{errors.password}</div>}
          </div>

          <div className="mb-3">
            <label className="form-label">Mobile Number *</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-phone"></i></span>
              <input type="text" className={`form-control ${errors.mobileNumber ? 'is-invalid' : ''}`} name="mobileNumber"
                placeholder="Enter your mobile number"
                value={formData.mobileNumber} onChange={handleChange} />
            </div>
            {errors.mobileNumber && <div className="text-danger small mt-1">{errors.mobileNumber}</div>}
          </div>

          <div className="mb-4">
            <label className="form-label">Address</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-geo-alt"></i></span>
              <input type="text" className="form-control" name="address"
                placeholder="Enter your address"
                value={formData.address} onChange={handleChange} />
            </div>
          </div>

          <button type="submit" className="btn btn-primary-custom" disabled={loading}>
            {loading ? (
              <><span className="spinner-border spinner-border-sm me-2"></span>Registering...</>
            ) : (
              <><i className="bi bi-person-plus me-2"></i>Register</>
            )}
          </button>
        </form>

        {/* Login Link */}
        <p className="text-center mt-4 mb-0" style={{ fontSize: '0.95rem' }}>
          Already have an account?{' '}
          <Link to="/login" className="text-primary fw-semibold text-decoration-none">
            Sign In
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Register;
