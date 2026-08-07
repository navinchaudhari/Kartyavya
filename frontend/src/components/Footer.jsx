import React from 'react';
import { Link } from 'react-router-dom';

// Footer component - displayed on all public pages
function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div className="row">
          {/* About Section */}
          <div className="col-lg-4 col-md-6 mb-4">
            <h5>
              <i className="bi bi-shield-check me-2"></i>
              Kartyavya
            </h5>
            <p style={{ fontSize: '0.95rem', lineHeight: '1.8' }}>
              Smart Complaint Management System for Municipal Corporations. 
              Empowering citizens to report and track municipal issues efficiently.
            </p>
          </div>

          {/* Quick Links */}
          <div className="col-lg-2 col-md-6 mb-4">
            <h5>Quick Links</h5>
            <ul className="list-unstyled">
              <li className="mb-2"><Link to="/">Home</Link></li>
              <li className="mb-2"><Link to="/about">About</Link></li>
              <li className="mb-2"><Link to="/services">Services</Link></li>
              <li className="mb-2"><Link to="/contact">Contact</Link></li>
            </ul>
          </div>

          {/* Services */}
          <div className="col-lg-3 col-md-6 mb-4">
            <h5>Services</h5>
            <ul className="list-unstyled">
              <li className="mb-2"><Link to="/services">Water Supply Issues</Link></li>
              <li className="mb-2"><Link to="/services">Road & Pothole</Link></li>
              <li className="mb-2"><Link to="/services">Streetlight Failures</Link></li>
              <li className="mb-2"><Link to="/services">Drainage & Sewage</Link></li>
              <li className="mb-2"><Link to="/services">Garbage Collection</Link></li>
            </ul>
          </div>

          {/* Contact Info */}
          <div className="col-lg-3 col-md-6 mb-4">
            <h5>Contact Us</h5>
            <ul className="list-unstyled">
              <li className="mb-2">
                <i className="bi bi-geo-alt me-2"></i>
                Municipal Corporation Office, Bhusawal
              </li>
              <li className="mb-2">
                <i className="bi bi-telephone me-2"></i>
                +91 9999999999
              </li>
              <li className="mb-2">
                <i className="bi bi-envelope me-2"></i>
                support@kartyavya.com
              </li>
            </ul>
          </div>
        </div>

        {/* Footer Bottom */}
        <div className="footer-bottom">
          <p>&copy; {new Date().getFullYear()} Kartyavya. All rights reserved. | CDAC Mini Project</p>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
