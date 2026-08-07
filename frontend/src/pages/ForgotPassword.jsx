import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authAPI } from '../services/api';
import { toast } from 'react-toastify';
import { apiErrorMessage } from '../utils/notifications';

function ForgotPassword() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({ email: '', otp: '', newPassword: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const update = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const requestOtp = async (event) => {
    event.preventDefault(); setError(''); setMessage('');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return setError('Enter a valid registered email address.');
    setLoading(true);
    try {
      await authAPI.forgotPassword({ email: form.email.trim().toLowerCase() });
      setStep(2);
      setMessage('A six-digit OTP was sent to your registered email. It remains valid for 10 minutes.');
      toast.info('Secure OTP sent to your registered email.');
    } catch (requestError) {
      const errorMessage = apiErrorMessage(requestError, 'Unable to send OTP.');
      setError(errorMessage);
      toast.error(errorMessage);
    }
    finally { setLoading(false); }
  };

  const verifyOtp = async (event) => {
    event.preventDefault(); setError('');
    if (!/^\d{6}$/.test(form.otp)) return setError('Enter the six-digit OTP.');
    setLoading(true);
    try {
      await authAPI.verifyOtp({ email: form.email.trim().toLowerCase(), otp: form.otp });
      setStep(3);
      setMessage('OTP verified. Create a strong new password.');
      toast.success('OTP verified successfully.');
    } catch (requestError) {
      const errorMessage = apiErrorMessage(requestError, 'OTP verification failed.');
      setError(errorMessage);
      toast.error(errorMessage);
    }
    finally { setLoading(false); }
  };

  const resetPassword = async (event) => {
    event.preventDefault(); setError('');
    const strong = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/;
    if (!strong.test(form.newPassword)) return setError('Password must contain uppercase, lowercase, number and special character.');
    if (form.newPassword !== form.confirmPassword) return setError('Passwords do not match.');
    setLoading(true);
    try {
      await authAPI.resetPassword({
        email: form.email.trim().toLowerCase(),
        otp: form.otp,
        newPassword: form.newPassword,
      });
      toast.success('Password reset successful. You can now sign in.');
      navigate('/login', { replace: true });
    } catch (requestError) {
      const errorMessage = apiErrorMessage(requestError, 'Password reset failed.');
      setError(errorMessage);
      toast.error(errorMessage);
    }
    finally { setLoading(false); }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card fade-in-up">
        <div className="text-center mb-4"><div className="auth-icon"><i className="bi bi-shield-lock" /></div></div>
        <div className="step-pills mb-4"><span className={step >= 1 ? 'active' : ''}>1</span><i /><span className={step >= 2 ? 'active' : ''}>2</span><i /><span className={step >= 3 ? 'active' : ''}>3</span></div>
        <h2 className="text-center">{step === 1 ? 'Recover account' : step === 2 ? 'Verify email OTP' : 'Create new password'}</h2>
        <p className="text-center">Secure email verification protects your account.</p>
        {message && <div className="alert alert-info py-2"><i className="bi bi-envelope-check me-2" />{message}</div>}
        {error && <div className="alert alert-danger py-2"><i className="bi bi-exclamation-circle me-2" />{error}</div>}
        {step === 1 && <form onSubmit={requestOtp}><label className="form-label">Registered email</label><input name="email" type="email" className="form-control mb-4" value={form.email} onChange={update} placeholder="citizen@example.com" /><button className="btn btn-primary-custom" disabled={loading}>{loading ? 'Sending…' : 'Send secure OTP'}</button></form>}
        {step === 2 && <form onSubmit={verifyOtp}><label className="form-label">Six-digit OTP</label><input name="otp" inputMode="numeric" maxLength="6" className="form-control otp-input mb-3" value={form.otp} onChange={update} placeholder="000000" /><button className="btn btn-primary-custom" disabled={loading}>{loading ? 'Verifying…' : 'Verify OTP'}</button><button type="button" className="btn btn-link w-100 mt-2" onClick={() => setStep(1)}>Use another email</button></form>}
        {step === 3 && <form onSubmit={resetPassword}><label className="form-label">New password</label><input name="newPassword" type="password" className="form-control mb-3" value={form.newPassword} onChange={update} /><label className="form-label">Confirm password</label><input name="confirmPassword" type="password" className="form-control mb-4" value={form.confirmPassword} onChange={update} /><button className="btn btn-primary-custom" disabled={loading}>{loading ? 'Resetting…' : 'Reset password'}</button></form>}
        <p className="text-center mt-4 mb-0"><Link to="/login"><i className="bi bi-arrow-left me-1" />Back to login</Link></p>
      </div>
    </div>
  );
}
export default ForgotPassword;
