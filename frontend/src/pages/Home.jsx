import React from 'react';
import { Link } from 'react-router-dom';

// Home page - public landing page with hero, services, and how it works sections
function Home() {
  return (
    <>
      {/* ============================================ */}
      {/* Hero Section */}
      {/* ============================================ */}
      <section className="hero-section">
        <div className="container" style={{ position: 'relative', zIndex: 1 }}>
          <div className="row align-items-center">
            <div className="col-lg-7">
              <div className="fade-in-up">
                <span className="badge bg-primary bg-opacity-25 text-primary px-3 py-2 mb-3 rounded-pill"
                  style={{ fontSize: '0.85rem' }}>
                  <i className="bi bi-stars me-1"></i> Smart Municipal Solution
                </span>
                <h1 className="hero-title">
                  Your Voice,<br />
                  Our <span>Responsibility</span>
                </h1>
                <p className="hero-subtitle">
                  Report municipal issues like potholes, water leakage, garbage collection, 
                  and more. Track your complaints in real-time and get them resolved faster.
                </p>
                <div className="d-flex gap-3 mt-4">
                  <Link to="/register" className="btn hero-btn hero-btn-primary">
                    <i className="bi bi-pencil-square me-2"></i>Register Complaint
                  </Link>
                  <Link to="/about" className="btn hero-btn hero-btn-outline">
                    Learn More <i className="bi bi-arrow-right ms-1"></i>
                  </Link>
                </div>
              </div>

              {/* Hero Stats */}
              <div className="hero-stats fade-in-up fade-in-up-delay-2">
                <div className="hero-stat">
                  <h3>500+</h3>
                  <p>Complaints Resolved</p>
                </div>
                <div className="hero-stat">
                  <h3>5</h3>
                  <p>Departments</p>
                </div>
                <div className="hero-stat">
                  <h3>98%</h3>
                  <p>Satisfaction Rate</p>
                </div>
              </div>
            </div>

            <div className="col-lg-5 d-none d-lg-block text-center">
              <div className="fade-in-up fade-in-up-delay-1" style={{ fontSize: '12rem', opacity: 0.15, color: '#fff' }}>
                <i className="bi bi-shield-check"></i>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================ */}
      {/* Services Section */}
      {/* ============================================ */}
      <section className="section-padding">
        <div className="container">
          <div className="text-center mb-5">
            <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-2 mb-3 rounded-pill">
              Our Services
            </span>
            <h2 className="section-title">Municipal Services We Cover</h2>
            <p className="section-subtitle">
              Report any municipal issue and we'll make sure it reaches the right department
            </p>
          </div>

          <div className="row g-4">
            {/* Water Supply */}
            <div className="col-lg-4 col-md-6">
              <div className="service-card fade-in-up">
                <div className="icon-box">
                  <i className="bi bi-droplet-half"></i>
                </div>
                <h5>Water Supply</h5>
                <p>Report water leakage, contamination, low pressure, or supply disruption issues.</p>
              </div>
            </div>

            {/* Road & Pothole */}
            <div className="col-lg-4 col-md-6">
              <div className="service-card fade-in-up fade-in-up-delay-1">
                <div className="icon-box">
                  <i className="bi bi-cone-striped"></i>
                </div>
                <h5>Road & Pothole</h5>
                <p>Report potholes, road damage, broken footpaths, and construction debris.</p>
              </div>
            </div>

            {/* Streetlight */}
            <div className="col-lg-4 col-md-6">
              <div className="service-card fade-in-up fade-in-up-delay-2">
                <div className="icon-box">
                  <i className="bi bi-lightbulb"></i>
                </div>
                <h5>Streetlight Failures</h5>
                <p>Report broken or non-functional streetlights, electrical hazards in public areas.</p>
              </div>
            </div>

            {/* Drainage */}
            <div className="col-lg-4 col-md-6">
              <div className="service-card fade-in-up">
                <div className="icon-box">
                  <i className="bi bi-water"></i>
                </div>
                <h5>Drainage & Sewage</h5>
                <p>Report blocked drains, sewage overflow, and waterlogging in your area.</p>
              </div>
            </div>

            {/* Garbage */}
            <div className="col-lg-4 col-md-6">
              <div className="service-card fade-in-up fade-in-up-delay-1">
                <div className="icon-box">
                  <i className="bi bi-trash3"></i>
                </div>
                <h5>Garbage Collection</h5>
                <p>Report missed garbage pickups, overflowing bins, and illegal dumping.</p>
              </div>
            </div>

            {/* General */}
            <div className="col-lg-4 col-md-6">
              <div className="service-card fade-in-up fade-in-up-delay-2">
                <div className="icon-box">
                  <i className="bi bi-megaphone"></i>
                </div>
                <h5>General Complaints</h5>
                <p>Report any other municipal issue that needs attention in your locality.</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================ */}
      {/* How It Works Section */}
      {/* ============================================ */}
      <section className="section-padding" style={{ background: '#fff' }}>
        <div className="container">
          <div className="text-center mb-5">
            <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-2 mb-3 rounded-pill">
              How It Works
            </span>
            <h2 className="section-title">Three Simple Steps</h2>
            <p className="section-subtitle">
              Filing a complaint has never been easier
            </p>
          </div>

          <div className="row g-4">
            <div className="col-md-4">
              <div className="text-center p-4">
                <div className="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
                  style={{ width: '80px', height: '80px', background: 'rgba(37, 99, 235, 0.1)', fontSize: '2rem', color: 'var(--primary)' }}>
                  <i className="bi bi-person-plus"></i>
                </div>
                <h5 className="fw-bold mt-2">1. Register</h5>
                <p className="text-muted">Create your free account as a citizen in just a few seconds.</p>
              </div>
            </div>
            <div className="col-md-4">
              <div className="text-center p-4">
                <div className="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
                  style={{ width: '80px', height: '80px', background: 'rgba(16, 185, 129, 0.1)', fontSize: '2rem', color: 'var(--success)' }}>
                  <i className="bi bi-pencil-square"></i>
                </div>
                <h5 className="fw-bold mt-2">2. Submit Complaint</h5>
                <p className="text-muted">Describe the issue, select department, and upload a photo.</p>
              </div>
            </div>
            <div className="col-md-4">
              <div className="text-center p-4">
                <div className="d-inline-flex align-items-center justify-content-center rounded-circle mb-3"
                  style={{ width: '80px', height: '80px', background: 'rgba(245, 158, 11, 0.1)', fontSize: '2rem', color: 'var(--warning)' }}>
                  <i className="bi bi-check-circle"></i>
                </div>
                <h5 className="fw-bold mt-2">3. Track & Resolve</h5>
                <p className="text-muted">Track your complaint status as the officer resolves it.</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================ */}
      {/* CTA Section */}
      {/* ============================================ */}
      <section className="section-padding" style={{ background: 'var(--gradient-hero)' }}>
        <div className="container text-center">
          <h2 className="text-white fw-bold mb-3" style={{ fontSize: '2.2rem' }}>
            Ready to Make Your City Better?
          </h2>
          <p className="text-white-50 mb-4" style={{ fontSize: '1.1rem' }}>
            Join thousands of citizens who are actively improving their neighborhoods.
          </p>
          <Link to="/register" className="btn hero-btn hero-btn-primary me-3">
            Get Started Free
          </Link>
          <Link to="/contact" className="btn hero-btn hero-btn-outline">
            Contact Us
          </Link>
        </div>
      </section>
    </>
  );
}

export default Home;
