import axios from "axios";
import { toast } from "react-toastify";
import { clearSession } from "../utils/auth";

export const API_ORIGIN =
  import.meta.env.VITE_API_ORIGIN || "http://localhost:8080";
const AI_REQUEST_TIMEOUT_MS = Number(
  import.meta.env.VITE_AI_REQUEST_TIMEOUT_MS || 150000,
);
const api = axios.create({
  baseURL: `${API_ORIGIN}/api`,
  timeout: 20000,
  headers: { Accept: "application/json" },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  config.headers["X-Correlation-Id"] =
    globalThis.crypto?.randomUUID?.() ||
    `${Date.now()}-${Math.random().toString(16).slice(2)}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (
      error.response?.status === 401 &&
      !window.location.pathname.includes("/login")
    ) {
      clearSession();
      toast.error("Your session expired. Please sign in again.", {
        toastId: "session-expired",
      });
      window.location.replace("/login");
    }
    return Promise.reject(error);
  },
);

export const authAPI = {
  register: (data) => api.post("/auth/register", data),
  login: (data) => api.post("/auth/login", data),
  forgotPassword: (data) => api.post("/auth/forgot-password", data),
  verifyOtp: (data) => api.post("/auth/verify-otp", data),
  resetPassword: (data) => api.post("/auth/reset-password", data),
};

export const departmentAPI = {
  getAll: () => api.get("/departments"),
  getAdminAll: () => api.get("/admin/departments"),
  add: (data) => api.post("/admin/departments", data),
  update: (id, data) => api.put(`/admin/departments/${id}`, data),
  delete: (id) => api.delete(`/admin/departments/${id}`),
};

export const complaintAPI = {
  create: (formData) => api.post("/reports", formData),
  getMyComplaints: () => api.get("/reports/my"),
  getById: (id) => api.get(`/reports/${id}`),
  update: (id, formData) => api.put(`/reports/${id}`, formData),
  delete: (id) => api.delete(`/reports/${id}`),
  nearby: (latitude, longitude, radiusKm = 1) =>
    api.get("/reports/nearby", { params: { latitude, longitude, radiusKm } }),
  track: (code) => api.get(`/reports/track/${encodeURIComponent(code)}`),
  getAll: (params = {}) =>
    api
      .get("/admin/reports", { params })
      .then((response) => ({ ...response, data: response.data.content || [] })),
  pendingDepartment: () => api.get("/admin/reports/pending-department"),
  pendingOfficer: () => api.get("/admin/reports/pending-officer"),
  assign: (id, data) => api.patch(`/admin/reports/${id}/assignment`, data),
  reprocess: (id) => api.post(`/admin/reports/${id}/reprocess-routing`),
};

export const officerAPI = {
  getComplaints: () => api.get("/officer/reports"),
  getDashboard: () => api.get("/officer/dashboard"),
  updateStatus: (id, formData) => {
    const displayStatus = formData.get("status");
    const status =
      displayStatus === "Completed"
        ? "RESOLVED"
        : displayStatus === "In Progress"
          ? "IN_PROGRESS"
          : displayStatus;
    formData.set("status", status);
    return api.put(`/officer/reports/${id}/status`, formData);
  },
};

export const citizenAPI = {
  getProfile: () => api.get("/citizen/profile"),
  updateProfile: (data) => api.put("/citizen/profile", data),
  getDashboard: () => api.get("/dashboard/citizen"),
};

export const adminAPI = {
  getDashboard: () => api.get("/dashboard/admin"),
  getOfficers: () => api.get("/admin/officers"),
  addOfficer: (data) => api.post("/admin/officers", data),
  updateOfficer: (id, data) => api.put(`/admin/officers/${id}`, data),
  deleteOfficer: (id) => api.delete(`/admin/officers/${id}`),
  getCitizens: () => api.get("/admin/users/citizens"),
  getRoutingRules: () => api.get("/admin/routing-rules"),
  addRoutingRule: (data) => api.post("/admin/routing-rules", data),
  updateRoutingRule: (id, data) => api.put(`/admin/routing-rules/${id}`, data),
};

export const analyticsAPI = {
  overview: () => api.get("/analytics/overview"),
  heatmap: () => api.get("/analytics/heatmap"),
  problemAreas: () => api.get("/analytics/problem-areas"),
  aiHotspots: () =>
    api.get("/analytics/ai-hotspots", { timeout: AI_REQUEST_TIMEOUT_MS }),
  refreshAiHotspots: () =>
    api.post("/analytics/ai-hotspots/refresh", undefined, {
      timeout: AI_REQUEST_TIMEOUT_MS,
    }),
};

export const notificationAPI = { getAll: () => api.get("/notifications") };
export const getImageUrl = (path) => (!path ? null : `${API_ORIGIN}${path}`);
export default api;
