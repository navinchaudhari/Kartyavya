import React, { useState, useEffect } from 'react';
import { departmentAPI } from '../services/api';

// Services page - lists all municipal departments and services
function Services() {
  const [departments, setDepartments] = useState([]);

  // Department icons mapping
  const deptIcons = {
    'Water Department': 'bi-droplet-half',
    'Road Department': 'bi-cone-striped',
    'Electrical Department': 'bi-lightbulb',
    'Drainage Department': 'bi-water',
    'Sanitation Department': 'bi-trash3',
  };

  useEffect(() => {
    loadDepartments();
  }, []);

  const loadDepartments = async () => {
    try {
      const res = await departmentAPI.getAll();
      setDepartments(res.data);
    } catch (error) {
      console.error('Error loading departments:', error);
    }
  };

  return (
    <>
      {/* Page Hero */}
      <section className="page-hero">
        <div className="container">
          <h1>Our Services</h1>
          <p>Comprehensive municipal services to keep your city running smoothly</p>
        </div>
      </section>

      {/* Departments List */}
      <section className="section-padding">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="section-title">Municipal Departments</h2>
            <p className="section-subtitle">
              We handle complaints across all major municipal departments
            </p>
          </div>

          <div className="row g-4">
            {departments.map((dept) => (
              <div className="col-lg-4 col-md-6" key={dept.departmentId}>
                <div className="service-card">
                  <div className="icon-box">
                    <i className={`bi ${deptIcons[dept.departmentName] || 'bi-building'}`}></i>
                  </div>
                  <h5>{dept.departmentName}</h5>
                  <p>{dept.description || 'Handles complaints related to this department.'}</p>
                </div>
              </div>
            ))}
          </div>

          {/* What We Handle */}
          <div className="row mt-5 pt-4">
            <div className="col-12">
              <div className="card-custom p-4 p-lg-5">
                <h3 className="fw-bold mb-4">What Can You Report?</h3>
                <div className="row g-3">
                  {[
                    'Potholes & Road Damage',
                    'Water Leakage & Supply Issues',
                    'Streetlight Not Working',
                    'Blocked Drainage & Sewage',
                    'Garbage Not Collected',
                    'Illegal Construction',
                    'Broken Footpath',
                    'Water Contamination',
                    'Overflowing Dustbin',
                    'Public Nuisance',
                    'Tree Fallen on Road',
                    'Any Other Municipal Issue',
                  ].map((item, idx) => (
                    <div className="col-lg-4 col-md-6" key={idx}>
                      <div className="d-flex align-items-center p-2">
                        <i className="bi bi-check2-circle text-success me-2 fs-5"></i>
                        <span>{item}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

export default Services;
