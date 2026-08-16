import React, { useState } from 'react';

// Contact page - contact form and information
function Contact() {
  const [formData, setFormData] = useState({ name: '', email: '', subject: '', message: '' });
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    // In a real app, this would send to an API
    setSubmitted(true);
    setFormData({ name: '', email: '', subject: '', message: '' });
  };

  return (
    <>
      {/* Page Hero */}
      <section className="page-hero">
        <div className="container">
          <h1>Contact Us</h1>
          <p>Have questions? We're here to help you</p>
        </div>
      </section>

      {/* Contact Content */}
      <section className="section-padding">
        <div className="container">
          <div className="row g-5">
            {/* Contact Form */}
            <div className="col-lg-7">
              <div className="card-custom p-4 p-lg-5">
                <h3 className="fw-bold mb-4">Send Us a Message</h3>

                {submitted && (
                  <div className="alert alert-success d-flex align-items-center" role="alert">
                    <i className="bi bi-check-circle-fill me-2"></i>
                    Thank you! Your message has been received.
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  <div className="row g-3">
                    <div className="col-md-6">
                      <label className="form-label">Full Name</label>
                      <input type="text" className="form-control" required
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })} />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label">Email</label>
                      <input type="email" className="form-control" required
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })} />
                    </div>
                    <div className="col-12">
                      <label className="form-label">Subject</label>
                      <input type="text" className="form-control" required
                        value={formData.subject}
                        onChange={(e) => setFormData({ ...formData, subject: e.target.value })} />
                    </div>
                    <div className="col-12">
                      <label className="form-label">Message</label>
                      <textarea className="form-control" rows="5" required
                        value={formData.message}
                        onChange={(e) => setFormData({ ...formData, message: e.target.value })} />
                    </div>
                    <div className="col-12">
                      <button type="submit" className="btn btn-primary-custom">
                        <i className="bi bi-send me-2"></i>Send Message
                      </button>
                    </div>
                  </div>
                </form>
              </div>
            </div>

            {/* Contact Info */}
            <div className="col-lg-5">
              <div className="card-custom p-4 mb-4">
                <div className="d-flex align-items-start">
                  <div className="stat-icon me-3" style={{ background: 'rgba(37, 99, 235, 0.1)', color: 'var(--primary)', width: '48px', height: '48px', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <i className="bi bi-geo-alt fs-5"></i>
                  </div>
                  <div>
                    <h6 className="fw-bold mb-1">Address</h6>
                    <p className="text-muted mb-0">Municipal Corporation Office,<br />Bhusawal, Maharashtra - 425201</p>
                  </div>
                </div>
              </div>

              <div className="card-custom p-4 mb-4">
                <div className="d-flex align-items-start">
                  <div className="stat-icon me-3" style={{ background: 'rgba(16, 185, 129, 0.1)', color: 'var(--success)', width: '48px', height: '48px', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <i className="bi bi-telephone fs-5"></i>
                  </div>
                  <div>
                    <h6 className="fw-bold mb-1">Phone</h6>
                    <p className="text-muted mb-0">+91 9999999999<br />+91 020-12345678</p>
                  </div>
                </div>
              </div>

              <div className="card-custom p-4 mb-4">
                <div className="d-flex align-items-start">
                  <div className="stat-icon me-3" style={{ background: 'rgba(245, 158, 11, 0.1)', color: 'var(--warning)', width: '48px', height: '48px', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <i className="bi bi-envelope fs-5"></i>
                  </div>
                  <div>
                    <h6 className="fw-bold mb-1">Email</h6>
                    <p className="text-muted mb-0">support@kartyavya.com<br />info@kartyavya.com</p>
                  </div>
                </div>
              </div>

              <div className="card-custom p-4">
                <div className="d-flex align-items-start">
                  <div className="stat-icon me-3" style={{ background: 'rgba(99, 102, 241, 0.1)', color: 'var(--info)', width: '48px', height: '48px', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <i className="bi bi-clock fs-5"></i>
                  </div>
                  <div>
                    <h6 className="fw-bold mb-1">Working Hours</h6>
                    <p className="text-muted mb-0">Mon - Fri: 9:00 AM - 6:00 PM<br />Sat: 9:00 AM - 1:00 PM</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

export default Contact;
