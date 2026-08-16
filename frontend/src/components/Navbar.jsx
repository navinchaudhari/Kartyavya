import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { clearSession, dashboardPathForRole, getSession } from '../utils/auth';

function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const session = getSession();
  const homePath = session.valid ? dashboardPathForRole(session.role) : '/';

  const handleLogout = () => {
    clearSession();
    toast.success('You have been logged out securely.');
    navigate('/login', { replace: true });
  };

  const isActive = (path) => (location.pathname === path ? 'active' : '');

  return (
    <nav className="navbar navbar-expand-lg navbar-dark navbar-custom sticky-top">
      <div className="container">
        <Link className="navbar-brand" to={homePath} aria-label="Kartyavya home">
          <i className="bi bi-shield-check me-2" />
          Kartyavya
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon" />
        </button>

        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav me-auto">
            {!session.valid && (
              <>
                <li className="nav-item"><Link className={`nav-link ${isActive('/')}`} to="/">Home</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/about')}`} to="/about">About</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/services')}`} to="/services">Services</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/contact')}`} to="/contact">Contact</Link></li>
                <li className="nav-item">
                  <Link className={`nav-link ${isActive('/track')}`} to="/track">
                    <i className="bi bi-search me-1" />Track Complaint
                  </Link>
                </li>
              </>
            )}

            {session.valid && session.role === 'Citizen' && (
              <>
                <li className="nav-item"><Link className={`nav-link ${isActive('/citizen/dashboard')}`} to="/citizen/dashboard"><i className="bi bi-speedometer2 me-1" /> Dashboard</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/citizen/add-complaint')}`} to="/citizen/add-complaint"><i className="bi bi-plus-circle me-1" /> New Complaint</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/citizen/my-complaints')}`} to="/citizen/my-complaints"><i className="bi bi-list-check me-1" /> My Complaints</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/citizen/profile')}`} to="/citizen/profile"><i className="bi bi-person me-1" /> Profile</Link></li>
              </>
            )}

            {session.valid && session.role === 'Officer' && (
              <>
                <li className="nav-item"><Link className={`nav-link ${isActive('/officer/dashboard')}`} to="/officer/dashboard"><i className="bi bi-speedometer2 me-1" /> Dashboard</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/officer/complaints')}`} to="/officer/complaints"><i className="bi bi-clipboard-check me-1" /> Assigned Complaints</Link></li>
              </>
            )}

            {session.valid && session.role === 'Admin' && (
              <>
                <li className="nav-item"><Link className={`nav-link ${isActive('/admin/dashboard')}`} to="/admin/dashboard"><i className="bi bi-speedometer2 me-1" /> Dashboard</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/admin/departments')}`} to="/admin/departments"><i className="bi bi-building me-1" /> Departments</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/admin/officers')}`} to="/admin/officers"><i className="bi bi-people me-1" /> Officers</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/admin/complaints')}`} to="/admin/complaints"><i className="bi bi-exclamation-triangle me-1" /> All Complaints</Link></li>
                <li className="nav-item"><Link className={`nav-link ${isActive('/admin/routing-rules')}`} to="/admin/routing-rules"><i className="bi bi-diagram-3 me-1" /> Routing Rules</Link></li>
              </>
            )}
          </ul>

          <ul className="navbar-nav">
            {!session.valid ? (
              <>
                <li className="nav-item"><Link className="nav-link" to="/login"><i className="bi bi-box-arrow-in-right me-1" /> Login</Link></li>
                <li className="nav-item"><Link className="nav-link hero-btn hero-btn-primary py-1 px-3 ms-2" to="/register">Register</Link></li>
              </>
            ) : (
              <>
                <li className="nav-item d-flex align-items-center me-3">
                  <span className="nav-link" style={{ cursor: 'default' }}>
                    <i className="bi bi-person-circle me-1" /> {session.fullName}
                    <span className="badge bg-primary ms-2" style={{ fontSize: '0.7rem' }}>{session.role}</span>
                  </span>
                </li>
                <li className="nav-item">
                  <button className="btn btn-logout" onClick={handleLogout} type="button">
                    <i className="bi bi-box-arrow-right me-1" /> Logout
                  </button>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
