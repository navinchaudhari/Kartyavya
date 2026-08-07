import React from 'react';

// DashboardCard component - reusable stat card for dashboards
// Props: title, value, icon, color (primary/success/warning/danger/info/accent)
function DashboardCard({ title, value, icon, color }) {
  return (
    <div className={`stat-card ${color}`}>
      <div className="d-flex justify-content-between align-items-start">
        <div>
          <p className="stat-label mb-0">{title}</p>
          <h3 className="stat-value mb-0">{value}</h3>
        </div>
        <div className="stat-icon">
          <i className={`bi ${icon}`}></i>
        </div>
      </div>
    </div>
  );
}

export default DashboardCard;
