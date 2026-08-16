import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import DashboardCard from "../../components/DashboardCard";
import HeatmapPanel from "../../components/HeatmapPanel";
import { adminAPI, analyticsAPI } from "../../services/api";
import { toast } from "react-toastify";
import { apiErrorMessage } from "../../utils/notifications";

function AdminDashboard() {
  const [stats, setStats] = useState({});
  const [overview, setOverview] = useState({});
  const [points, setPoints] = useState([]);
  const [analysis, setAnalysis] = useState({
    hotspots: [],
    summary: "",
    modelVersion: "",
    sourceComplaintCount: 0,
    configured: false,
    available: false,
    status: "UNKNOWN",
  });
  const [error, setError] = useState("");
  const [refreshing, setRefreshing] = useState(false);

  const load = async () => {
    setError("");
    const messages = [];

    const [dashboardResult, analyticsResult, heatmapResult] =
      await Promise.allSettled([
        adminAPI.getDashboard(),
        analyticsAPI.overview(),
        analyticsAPI.heatmap(),
      ]);

    if (dashboardResult.status === "fulfilled")
      setStats(dashboardResult.value.data);
    else
      messages.push(
        dashboardResult.reason.response?.data?.message ||
          "Administration summary is unavailable.",
      );

    if (analyticsResult.status === "fulfilled")
      setOverview(analyticsResult.value.data);
    else
      messages.push(
        analyticsResult.reason.response?.data?.message ||
          "Analytics overview is unavailable.",
      );

    if (heatmapResult.status === "fulfilled")
      setPoints(heatmapResult.value.data);
    else
      messages.push(
        heatmapResult.reason.response?.data?.message ||
          "Complaint map data is unavailable.",
      );

    try {
      const hotspots = await analyticsAPI.aiHotspots();
      setAnalysis(hotspots.data);
      if (hotspots.data?.available === false) {
        toast.warn(
          hotspots.data.summary || "Gemini hotspot analysis is unavailable.",
          { toastId: "gemini-hotspot-degraded" },
        );
      }
    } catch (aiError) {
      const message =
        aiError.response?.data?.message ||
        "AI Analytics Service could not load Gemini hotspot analysis.";
      setAnalysis({
        hotspots: [],
        summary: message,
        modelVersion: "",
        sourceComplaintCount: 0,
        configured: false,
        available: false,
        status: "SERVICE_UNAVAILABLE",
      });
      messages.push(message);
    }

    setError([...new Set(messages)].join(" "));
  };

  useEffect(() => {
    load();
  }, []);

  const refreshHotspots = async () => {
    setRefreshing(true);
    setError("");
    try {
      const response = await analyticsAPI.refreshAiHotspots();
      setAnalysis(response.data);
      if (response.data?.available === false) {
        toast.warn(
          response.data.summary || "Gemini hotspot analysis is unavailable.",
          { toastId: "gemini-hotspot-refresh-degraded" },
        );
      } else {
        toast.success("Gemini hotspot analysis refreshed.");
      }
    } catch (requestError) {
      const message = apiErrorMessage(
        requestError,
        "Gemini hotspot refresh failed.",
      );
      setError(message);
      toast.error(message);
    } finally {
      setRefreshing(false);
    }
  };

  const geminiLabel = analysis.available
    ? analysis.modelVersion || "Gemini available"
    : analysis.configured
      ? `${analysis.modelVersion || "Gemini"} unavailable`
      : "Gemini not configured";

  const geminiSummary =
    analysis.summary ||
    (analysis.configured
      ? "Gemini is configured, but hotspot analysis is temporarily unavailable."
      : "Add KARTYAVYA_GEMINI_API_KEY to the project-root .env file and restart AI Analytics Service.");

  return (
    <>
      <section className="dashboard-header premium-header">
        <div className="container d-flex justify-content-between align-items-center flex-wrap gap-3">
          <div>
            <span className="eyebrow">MUNICIPAL COMMAND CENTRE</span>
            <h2>
              <i className="bi bi-grid-1x2 me-2" />
              Admin intelligence dashboard
            </h2>
            <p className="mb-0">
              Live routing, unresolved workloads and Gemini-assisted geographic
              hotspot analysis.
            </p>
          </div>
          <Link
            to="/admin/complaints"
            className="btn hero-btn hero-btn-primary py-2 px-4"
          >
            <i className="bi bi-inboxes me-2" />
            Open complaint queue
          </Link>
        </div>
      </section>

      <main className="container pb-5">
        {error && <div className="alert alert-danger">{error}</div>}
        <div className="row g-4 mb-4">
          <div className="col-xl-3 col-md-6">
            <DashboardCard
              title="Citizens"
              value={stats.totalCitizens || 0}
              icon="bi-people"
              color="primary"
            />
          </div>
          <div className="col-xl-3 col-md-6">
            <DashboardCard
              title="Active Officers"
              value={stats.totalOfficers || 0}
              icon="bi-person-badge"
              color="accent"
            />
          </div>
          <div className="col-xl-3 col-md-6">
            <DashboardCard
              title="Departments"
              value={stats.totalDepartments || 0}
              icon="bi-buildings"
              color="info"
            />
          </div>
          <div className="col-xl-3 col-md-6">
            <DashboardCard
              title="Complaints"
              value={stats.totalComplaints || 0}
              icon="bi-clipboard-data"
              color="danger"
            />
          </div>
        </div>

        <div className="row g-4 mb-4">
          <div className="col-lg-3 col-md-6">
            <Link
              className="queue-card queue-danger"
              to="/admin/complaints?queue=department"
            >
              <i className="bi bi-building-exclamation" />
              <span>
                <strong>{stats.pendingDepartmentSetup || 0}</strong>Pending
                department setup
              </span>
            </Link>
          </div>
          <div className="col-lg-3 col-md-6">
            <Link
              className="queue-card queue-warning"
              to="/admin/complaints?queue=officer"
            >
              <i className="bi bi-person-exclamation" />
              <span>
                <strong>{stats.pendingOfficerAssignment || 0}</strong>Pending
                officer assignment
              </span>
            </Link>
          </div>
          <div className="col-lg-3 col-md-6">
            <div className="queue-card">
              <i className="bi bi-arrow-repeat" />
              <span>
                <strong>{stats.inProgressComplaints || 0}</strong>In progress
              </span>
            </div>
          </div>
          <div className="col-lg-3 col-md-6">
            <div className="queue-card queue-success">
              <i className="bi bi-check2-circle" />
              <span>
                <strong>{stats.completedComplaints || 0}</strong>Resolved
              </span>
            </div>
          </div>
        </div>

        <div className="row g-4 mb-4">
          <div className="col-xl-8">
            <div className="card-custom p-4">
              <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                  <span className="eyebrow">GEMINI GEOSPATIAL OPERATIONS</span>
                  <h4 className="mb-0">AI-assisted complaint hotspot map</h4>
                </div>
                <div className="d-flex gap-2 align-items-center">
                  <span className="metric-chip">{points.length} mapped</span>
                  <button
                    className="btn btn-sm btn-outline-primary"
                    onClick={refreshHotspots}
                    disabled={refreshing}
                  >
                    {refreshing ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" />
                        Analyzing
                      </>
                    ) : (
                      <>
                        <i className="bi bi-stars me-2" />
                        Refresh Gemini analysis
                      </>
                    )}
                  </button>
                </div>
              </div>
              <HeatmapPanel
                points={points}
                hotspots={analysis.hotspots || []}
              />
            </div>
          </div>
          <div className="col-xl-4">
            <div className="card-custom p-4 h-100">
              <span className="eyebrow">AI QUALITY</span>
              <h4>Routing performance</h4>
              <div className="insight-metric">
                <span>Average resolution</span>
                <strong>{overview.averageResolutionHours || 0}h</strong>
              </div>
              <div className="insight-metric">
                <span>AI correction rate</span>
                <strong>{overview.aiCorrectionRate || 0}%</strong>
              </div>
              <div className="insight-metric">
                <span>High-severity open</span>
                <strong>{overview.highSeverityOpen || 0}</strong>
              </div>
              <div className="insight-metric">
                <span>AI hotspot groups</span>
                <strong>{analysis.hotspots?.length || 0}</strong>
              </div>
              <hr />
              <h6 className="fw-bold">Configuration</h6>
              <Link to="/admin/departments" className="settings-link">
                <i className="bi bi-building" />
                Departments
              </Link>
              <Link to="/admin/officers" className="settings-link">
                <i className="bi bi-person-badge" />
                Officers
              </Link>
              <Link to="/admin/routing-rules" className="settings-link">
                <i className="bi bi-diagram-3" />
                AI routing rules
              </Link>
            </div>
          </div>
        </div>

        <div className="card-custom p-4 gemini-insight-panel">
          <div className="d-flex justify-content-between align-items-start flex-wrap gap-3">
            <div>
              <span className="eyebrow">GEMINI MUNICIPAL BRIEF</span>
              <h4>Current hotspot assessment</h4>
            </div>
            <span className="metric-chip">{geminiLabel}</span>
          </div>
          <p className="lead-summary">{geminiSummary}</p>
          <div className="row g-3">
            {(analysis.hotspots || []).slice(0, 6).map((hotspot, index) => (
              <div
                className="col-lg-4 col-md-6"
                key={`${hotspot.name}-${index}`}
              >
                <div
                  className={`ai-hotspot-card priority-${(hotspot.priority || "LOW").toLowerCase()}`}
                >
                  <div className="d-flex justify-content-between gap-2">
                    <strong>{hotspot.name}</strong>
                    <span>{hotspot.priority}</span>
                  </div>
                  <p>{hotspot.explanation}</p>
                  <small>
                    {hotspot.complaintCount} complaints •{" "}
                    {hotspot.dominantCategory} • {hotspot.dominantSeverity}
                  </small>
                  <div className="recommended-action">
                    <i className="bi bi-lightbulb me-2" />
                    {hotspot.recommendedAction}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
    </>
  );
}

export default AdminDashboard;
