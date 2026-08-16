import React, { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { complaintAPI } from "../../services/api";

function AllComplaints() {
  const location = useLocation();
  const navigate = useNavigate();
  const queue = new URLSearchParams(location.search).get("queue") || "all";
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState({
    search: "",
    status: "",
    severity: "",
  });

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const response =
        queue === "department"
          ? await complaintAPI.pendingDepartment()
          : queue === "officer"
            ? await complaintAPI.pendingOfficer()
            : await complaintAPI.getAll({ size: 100 });
      setComplaints(response.data);
    } catch (requestError) {
      setError(
        requestError.response?.data?.message || "Unable to load complaints.",
      );
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    load();
  }, [queue]);

  const filtered = useMemo(
    () =>
      complaints.filter((item) => {
        const needle = filters.search.trim().toLowerCase();
        const matchesSearch =
          !needle ||
          [
            item.complaintTitle,
            item.trackingCode,
            item.citizenName,
            item.areaLocation,
            item.departmentName,
            item.officerName,
          ]
            .filter(Boolean)
            .some((value) => value.toLowerCase().includes(needle));
        return (
          matchesSearch &&
          (!filters.status || item.statusCode === filters.status) &&
          (!filters.severity || item.aiSeverity === filters.severity)
        );
      }),
    [complaints, filters],
  );

  const setQueue = (value) =>
    navigate(
      value === "all"
        ? "/admin/complaints"
        : `/admin/complaints?queue=${value}`,
    );
  const statusBadge = (statusCode) => {
    switch (statusCode) {
      case "ASSIGNED":
        return "status-assigned";

      case "IN_PROGRESS":
        return "status-in-progress";

      case "RESOLVED":
        return "status-resolved";

      case "REJECTED":
        return "status-rejected";

      case "PENDING_DEPARTMENT_SETUP":
        return "status-pending-department";

      case "PENDING_OFFICER_ASSIGNMENT":
        return "status-pending-officer";

      default:
        return "status-pending";
    }
  };

  const getStatusIcon = (statusCode) => {
    switch (statusCode) {
      case "ASSIGNED":
        return "bi bi-person-check-fill";

      case "IN_PROGRESS":
        return "bi bi-arrow-repeat";

      case "RESOLVED":
        return "bi bi-check-circle-fill";

      case "REJECTED":
        return "bi bi-x-circle-fill";

      case "PENDING_DEPARTMENT_SETUP":
        return "bi bi-building-exclamation";

      case "PENDING_OFFICER_ASSIGNMENT":
        return "bi bi-person-exclamation";

      default:
        return "bi bi-clock-fill";
    }
  };

  const formatStatus = (statusCode) => {
    if (!statusCode) {
      return "Pending";
    }

    return statusCode
      .replaceAll("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (character) => character.toUpperCase());
  };

  return (
    <>
      <section className="dashboard-header premium-header">
        <div className="container d-flex justify-content-between align-items-center flex-wrap gap-3">
          <div>
            <span className="eyebrow">OPERATIONS QUEUE</span>
            <h2>
              <i className="bi bi-inboxes me-2" />
              Complaint control centre
            </h2>
            <p className="mb-0">
              Prioritize unmapped complaints and assign newly allocated
              officers.
            </p>
          </div>
          <Link
            to="/admin/routing-rules"
            className="btn hero-btn hero-btn-primary py-2 px-4"
          >
            <i className="bi bi-diagram-3 me-2" />
            Routing rules
          </Link>
        </div>
      </section>
      <main className="container pb-5">
        {error && <div className="alert alert-danger mt-4">{error}</div>}
        <div className="queue-tabs mt-4">
          <button
            className={queue === "all" ? "active" : ""}
            onClick={() => setQueue("all")}
          >
            All complaints
          </button>
          <button
            className={queue === "department" ? "active" : ""}
            onClick={() => setQueue("department")}
          >
            <i className="bi bi-building-exclamation me-2" />
            Department setup
          </button>
          <button
            className={queue === "officer" ? "active" : ""}
            onClick={() => setQueue("officer")}
          >
            <i className="bi bi-person-exclamation me-2" />
            Officer assignment
          </button>
        </div>
        <section className="card-custom p-4 mt-3 mb-4">
          <div className="row g-3">
            <div className="col-lg-6">
              <label className="form-label">Search complaint</label>
              <input
                className="form-control"
                value={filters.search}
                onChange={(event) =>
                  setFilters({ ...filters, search: event.target.value })
                }
                placeholder="Title, tracking code, citizen, area or ownership"
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">Exact workflow</label>
              <select
                className="form-select"
                value={filters.status}
                onChange={(event) =>
                  setFilters({ ...filters, status: event.target.value })
                }
              >
                <option value="">All statuses</option>
                <option value="PENDING_DEPARTMENT_SETUP">
                  Pending department
                </option>
                <option value="PENDING_OFFICER_ASSIGNMENT">
                  Pending officer
                </option>
                <option value="ASSIGNED">Assigned</option>
                <option value="IN_PROGRESS">In progress</option>
                <option value="RESOLVED">Resolved</option>
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">AI severity</label>
              <select
                className="form-select"
                value={filters.severity}
                onChange={(event) =>
                  setFilters({ ...filters, severity: event.target.value })
                }
              >
                <option value="">All severities</option>
                <option>HIGH</option>
                <option>MEDIUM</option>
                <option>LOW</option>
              </select>
            </div>
          </div>
        </section>
        <div className="d-flex justify-content-between align-items-center mb-3">
          <p className="text-muted mb-0">
            <strong>{filtered.length}</strong> complaint
            {filtered.length === 1 ? "" : "s"} in this view
          </p>
          <button className="btn btn-sm btn-outline-secondary" onClick={load}>
            <i className="bi bi-arrow-clockwise me-1" />
            Refresh
          </button>
        </div>
        {loading ? (
          <div className="loading-spinner">
            <div className="spinner-border text-primary" />
          </div>
        ) : filtered.length === 0 ? (
          <div className="card-custom">
            <div className="empty-state">
              <i className="bi bi-inbox" />
              <h5>Queue is clear</h5>
              <p>No complaints match this operational view.</p>
            </div>
          </div>
        ) : (
          <div className="row g-4">
            {filtered.map((item) => (
              <div className="col-xl-6" key={item.complaintId}>
                <article
                  className={`complaint-ops-card ${
                    item.statusCode === "PENDING_DEPARTMENT_SETUP"
                      ? "needs-department"
                      : item.statusCode === "PENDING_OFFICER_ASSIGNMENT"
                        ? "needs-officer"
                        : ""
                  }`}
                >
                  <div className="complaint-card-heading">
                    <div className="complaint-card-title">
                      <span className="eyebrow">{item.trackingCode}</span>

                      <h4>{item.complaintTitle}</h4>
                    </div>

                    <span
                      className={`complaint-status-badge ${statusBadge(
                        item.statusCode,
                      )}`}
                    >
                      <i className={getStatusIcon(item.statusCode)} />
                      {formatStatus(item.statusCode)}
                    </span>
                  </div>

                  <p className="text-muted line-clamp-2">{item.description}</p>

                  {item.pendingReason && (
                    <div className="pending-reason">
                      <i className="bi bi-exclamation-circle me-2" />
                      {item.pendingReason}
                    </div>
                  )}

                  <div className="ops-meta">
                    <span>
                      <i className="bi bi-person" />
                      {item.citizenName}
                    </span>

                    <span>
                      <i className="bi bi-geo-alt" />
                      {item.areaLocation}
                    </span>

                    <span>
                      <i className="bi bi-cpu" />
                      {item.aiCategory} · {item.aiSeverity}
                    </span>

                    <span>
                      <i className="bi bi-building" />
                      {item.departmentName || "Department not mapped"}
                    </span>

                    <span>
                      <i className="bi bi-person-badge" />
                      {item.officerName || "Officer not assigned"}
                    </span>
                  </div>

                  <div className="d-flex justify-content-between align-items-center mt-3">
                    <small className="text-muted">
                      Filed {new Date(item.complaintDate).toLocaleString()}
                    </small>

                    <Link
                      to={`/admin/complaint/${item.complaintId}`}
                      className="btn btn-primary btn-sm"
                    >
                      <i className="bi bi-sliders me-1" />
                      Open assignment
                    </Link>
                  </div>
                </article>
              </div>
            ))}
          </div>
        )}
      </main>
    </>
  );
}
export default AllComplaints;
