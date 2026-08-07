import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authAPI } from '../services/api';
import { toast } from 'react-toastify';
import { dashboardPathForRole, saveSession } from '../utils/auth';
import { apiErrorMessage } from '../utils/notifications';

// Login page - authenticates users and redirects based on role
function Login() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const validateField = (name, value) => {
    let tempErrors = { ...errors };

    switch (name) {
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
        } else {
          delete tempErrors.password;
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

    if (!formData.email.trim()) {
      tempErrors.email = 'Email address is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      tempErrors.email = 'Invalid email address format';
    }

    if (!formData.password) {
      tempErrors.password = 'Password is required';
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
      const res = await authAPI.login(formData);
      const data = res.data;

      saveSession(data);
      toast.success(`Welcome back, ${data.fullName || 'user'}!`);
      navigate(dashboardPathForRole(data.role), { replace: true });
    } catch (err) {
      const message = apiErrorMessage(err, 'Login failed. Please try again.');
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card fade-in-up">
        {/* Logo */}
        <div className="text-center mb-4">
          <i className="bi bi-shield-check" style={{ fontSize: '2.5rem', color: 'var(--primary)' }}></i>
        </div>

        <h2 className="text-center">Welcome Back</h2>
        <p className="text-center">Sign in to your Kartyavya account</p>

        {/* Error Alert */}
        {error && (
          <div className="alert alert-danger d-flex align-items-center py-2" role="alert">
            <i className="bi bi-exclamation-circle me-2"></i>{error}
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit} noValidate>
          <div className="mb-3">
            <label className="form-label">Email Address</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-envelope"></i></span>
              <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`} name="email"
                placeholder="Enter your email"
                value={formData.email} onChange={handleChange} />
            </div>
            {errors.email && <div className="text-danger small mt-1">{errors.email}</div>}
          </div>

          <div className="mb-3">
            <label className="form-label">Password</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-lock"></i></span>
              <input type="password" className={`form-control ${errors.password ? 'is-invalid' : ''}`} name="password"
                placeholder="Enter your password"
                value={formData.password} onChange={handleChange} />
            </div>
            {errors.password && <div className="text-danger small mt-1">{errors.password}</div>}
          </div>

          <div className="d-flex justify-content-between align-items-center mb-4">
            <Link to="/forgot-password" className="text-primary text-decoration-none" style={{ fontSize: '0.9rem' }}>
              Forgot Password?
            </Link>
          </div>

          <button type="submit" className="btn btn-primary-custom" disabled={loading}>
            {loading ? (
              <><span className="spinner-border spinner-border-sm me-2"></span>Signing In...</>
            ) : (
              <><i className="bi bi-box-arrow-in-right me-2"></i>Sign In</>
            )}
          </button>
        </form>

        {/* Register Link */}
        <p className="text-center mt-4 mb-0" style={{ fontSize: '0.95rem' }}>
          Don't have an account?{' '}
          <Link to="/register" className="text-primary fw-semibold text-decoration-none">
            Register Now
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Login;
