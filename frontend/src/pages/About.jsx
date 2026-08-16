import React from 'react';

// About page - information about the system
function About() {
  return (
    <>
      {/* Page Hero */}
      <section className="page-hero">
        <div className="container">
          <h1>About Kartyavya</h1>
          <p>Empowering citizens and municipalities for a cleaner, safer city</p>
        </div>
      </section>

      {/* About Content */}
      <section className="section-padding">
        <div className="container">
          <div className="row align-items-center g-5">
            <div className="col-lg-6">
              <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-2 mb-3 rounded-pill">
                Our Mission
              </span>
              <h2 className="section-title">Making Municipal Governance Transparent</h2>
              <p className="text-muted mb-4" style={{ lineHeight: '1.9' }}>
                <strong>Kartyavya</strong> (कर्तव्य — "Duty") is a Smart Complaint Management System 
                designed to bridge the gap between citizens and municipal corporations. We believe 
                every citizen deserves a clean, safe, and well-maintained city.
              </p>
              <p className="text-muted mb-4" style={{ lineHeight: '1.9' }}>
                Our platform makes it simple to report issues like potholes, water leakage, 
                drainage blockage, garbage overflow, and streetlight failures. Every complaint 
                is automatically routed to the right department for quick resolution.
              </p>
              <div className="row g-3 mt-2">
                <div className="col-6">
                  <div className="d-flex align-items-center">
                    <i className="bi bi-check-circle-fill text-success me-2 fs-5"></i>
                    <span className="fw-medium">Easy to Use</span>
                  </div>
                </div>
                <div className="col-6">
                  <div className="d-flex align-items-center">
                    <i className="bi bi-check-circle-fill text-success me-2 fs-5"></i>
                    <span className="fw-medium">Real-time Tracking</span>
                  </div>
                </div>
                <div className="col-6">
                  <div className="d-flex align-items-center">
                    <i className="bi bi-check-circle-fill text-success me-2 fs-5"></i>
                    <span className="fw-medium">Transparent Process</span>
                  </div>
                </div>
                <div className="col-6">
                  <div className="d-flex align-items-center">
                    <i className="bi bi-check-circle-fill text-success me-2 fs-5"></i>
                    <span className="fw-medium">Quick Resolution</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="col-lg-6">
              <div className="p-5 rounded-4" style={{ background: 'var(--gradient-hero)' }}>
                <div className="text-center text-white">
                  <i className="bi bi-shield-check" style={{ fontSize: '5rem', opacity: 0.3 }}></i>
                  <h3 className="fw-bold mt-3">Kartyavya</h3>
                  <p className="mb-0 opacity-75">Your Duty, Our Commitment</p>
                  <hr className="my-4 opacity-25" />
                  <div className="row g-3">
                    <div className="col-4">
                      <h4 className="fw-bold">500+</h4>
                      <small className="opacity-75">Complaints Resolved</small>
                    </div>
                    <div className="col-4">
                      <h4 className="fw-bold">5</h4>
                      <small className="opacity-75">Departments</small>
                    </div>
                    <div className="col-4">
                      <h4 className="fw-bold">24/7</h4>
                      <small className="opacity-75">Support</small>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Team / Technology */}
      <section className="section-padding" style={{ background: '#fff' }}>
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="section-title">Built With Modern Technology</h2>
            <p className="section-subtitle">
              Powered by industry-standard technologies for reliability and performance
            </p>
          </div>
          <div className="row g-4 justify-content-center">
            {[
              { name: 'React.js', icon: 'bi-filetype-jsx', desc: 'Frontend UI' },
              { name: 'ASP.NET Core', icon: 'bi-gear', desc: 'Backend API' },
              { name: 'MySQL', icon: 'bi-database', desc: 'Database' },
              { name: 'Bootstrap 5', icon: 'bi-phone', desc: 'Responsive UI' },
            ].map((tech, idx) => (
              <div className="col-lg-3 col-md-6" key={idx}>
                <div className="service-card">
                  <div className="icon-box">
                    <i className={`bi ${tech.icon}`}></i>
                  </div>
                  <h5>{tech.name}</h5>
                  <p>{tech.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </>
  );
}

export default About;
