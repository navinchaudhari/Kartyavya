import React, { useEffect, useState } from 'react';
import { citizenAPI } from '../../services/api';
import { toast } from 'react-toastify';
import { apiErrorMessage } from '../../utils/notifications';

function CitizenProfile() {
  const [profile, setProfile] = useState({ fullName: '', mobileNumber: '', address: '' });
  const [profileErrors, setProfileErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    citizenAPI.getProfile()
      .then((response) => {
        setProfile({
          fullName: response.data.fullName || '',
          mobileNumber: response.data.mobileNumber || '',
          address: response.data.address || '',
        });
      })
      .catch((error) => {
        const errorMessage = apiErrorMessage(error, 'Unable to load your profile.');
        setMessage(errorMessage);
        toast.error(errorMessage);
      })
      .finally(() => setLoading(false));
  }, []);

  const validate = (values) => {
    const errors = {};
    if (!values.fullName.trim()) errors.fullName = 'Full name is required';
    else if (values.fullName.trim().length < 3) errors.fullName = 'Full name must be at least 3 characters';
    else if (!/^[A-Za-z\s]+$/.test(values.fullName.trim())) errors.fullName = 'Full name must contain only letters and spaces';

    if (!values.mobileNumber.trim()) errors.mobileNumber = 'Mobile number is required';
    else if (!/^[6-9]\d{9}$/.test(values.mobileNumber.trim())) errors.mobileNumber = 'Enter a valid 10-digit Indian mobile number';

    if (!values.address.trim()) errors.address = 'Address is required';
    else if (values.address.trim().length < 5) errors.address = 'Address must be at least 5 characters';
    return errors;
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    const next = { ...profile, [name]: value };
    setProfile(next);
    setProfileErrors(validate(next));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage('');
    const errors = validate(profile);
    setProfileErrors(errors);
    if (Object.keys(errors).length) return;

    setSaving(true);
    try {
      await citizenAPI.updateProfile({
        fullName: profile.fullName.trim(),
        mobileNumber: profile.mobileNumber.trim(),
        address: profile.address.trim(),
      });
      localStorage.setItem('fullName', profile.fullName.trim());
      setMessage('Profile updated successfully!');
      toast.success('Profile updated successfully.');
    } catch (error) {
      const errorMessage = apiErrorMessage(error, 'Failed to update profile.');
      setMessage(errorMessage);
      toast.error(errorMessage);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="loading-spinner"><div className="spinner-border text-primary"><span className="visually-hidden">Loading...</span></div></div>;
  }

  return (
    <>
      <section className="dashboard-header premium-header">
        <div className="container">
          <span className="eyebrow">CITIZEN ACCOUNT</span>
          <h2><i className="bi bi-person-circle me-2" />My profile</h2>
          <p className="mb-0">Keep your contact and address information accurate for complaint communication.</p>
        </div>
      </section>

      <main className="container pb-5">
        <div className="row g-4 justify-content-center">
          <div className="col-xl-8 col-lg-9">
            <div className="card-custom p-4 p-lg-5 fade-in-up">
              <div className="d-flex align-items-center gap-3 mb-4">
                <div className="profile-avatar"><i className="bi bi-person" /></div>
                <div>
                  <span className="eyebrow">PERSONAL INFORMATION</span>
                  <h4 className="mb-0">Update profile details</h4>
                </div>
              </div>

              {message && (
                <div className={`alert ${message.includes('success') ? 'alert-success' : 'alert-danger'}`}>
                  {message}
                </div>
              )}

              <form onSubmit={handleSubmit} noValidate>
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Full name</label>
                    <input
                      type="text"
                      name="fullName"
                      className={`form-control ${profileErrors.fullName ? 'is-invalid' : ''}`}
                      value={profile.fullName}
                      onChange={handleChange}
                      maxLength="100"
                    />
                    {profileErrors.fullName && <div className="invalid-feedback">{profileErrors.fullName}</div>}
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Mobile number</label>
                    <input
                      type="tel"
                      name="mobileNumber"
                      className={`form-control ${profileErrors.mobileNumber ? 'is-invalid' : ''}`}
                      value={profile.mobileNumber}
                      onChange={handleChange}
                      inputMode="numeric"
                      maxLength="10"
                    />
                    {profileErrors.mobileNumber && <div className="invalid-feedback">{profileErrors.mobileNumber}</div>}
                  </div>
                  <div className="col-12">
                    <label className="form-label">Residential address</label>
                    <textarea
                      name="address"
                      className={`form-control ${profileErrors.address ? 'is-invalid' : ''}`}
                      value={profile.address}
                      onChange={handleChange}
                      rows="4"
                      maxLength="255"
                    />
                    {profileErrors.address && <div className="invalid-feedback">{profileErrors.address}</div>}
                  </div>
                </div>

                <div className="profile-security-note mt-4">
                  <i className="bi bi-shield-check" />
                  <div>
                    <strong>Password management is handled through secure email OTP recovery.</strong>
                    <p className="mb-0">Use “Forgot Password” on the login page when a password reset is required.</p>
                  </div>
                </div>

                <button type="submit" className="btn btn-primary-custom mt-4" disabled={saving} style={{ width: 'auto' }}>
                  {saving ? <><span className="spinner-border spinner-border-sm me-2" />Saving</> : <><i className="bi bi-check2-circle me-2" />Save profile</>}
                </button>
              </form>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}

export default CitizenProfile;
