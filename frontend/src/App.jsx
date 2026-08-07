import React from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';

// Components
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';
import { GuestOnlyRoute, HomeEntry, NotFoundRoute } from './components/RouteGuards';

// Public Pages
import Home from './pages/Home';
import About from './pages/About';
import Services from './pages/Services';
import Contact from './pages/Contact';
import TrackComplaint from './pages/public/TrackComplaint';

// Auth Pages
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';

// Citizen Pages
import CitizenDashboard from './pages/citizen/CitizenDashboard';
import AddComplaint from './pages/citizen/AddComplaint';
import EditComplaint from './pages/citizen/EditComplaint';
import MyComplaints from './pages/citizen/MyComplaints';
import ComplaintDetails from './pages/citizen/ComplaintDetails';
import CitizenProfile from './pages/citizen/CitizenProfile';

// Officer Pages
import OfficerDashboard from './pages/officer/OfficerDashboard';
import AssignedComplaints from './pages/officer/AssignedComplaints';
import UpdateComplaint from './pages/officer/UpdateComplaint';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import ManageDepartments from './pages/admin/ManageDepartments';
import ManageOfficers from './pages/admin/ManageOfficers';
import AllComplaints from './pages/admin/AllComplaints';
import AdminComplaintDetails from './pages/admin/AdminComplaintDetails';
import RoutingRules from './pages/admin/RoutingRules';

import './App.css';

function App() {
  const location = useLocation();

  // Hide footer on auth pages and dashboard pages
  const hideFooter = ['/login', '/register', '/forgot-password'].includes(location.pathname)
    || location.pathname.startsWith('/citizen/')
    || location.pathname.startsWith('/officer/')
    || location.pathname.startsWith('/admin/');

  // Hide navbar on auth pages
  const hideNavbar = ['/login', '/register', '/forgot-password'].includes(location.pathname);

  return (
    <div className="App">
      {/* Navbar - hidden on auth pages */}
      {!hideNavbar && <Navbar />}

      {/* Routes */}
      <Routes>
        {/* ===== Public Routes ===== */}
        <Route path="/" element={<HomeEntry><Home /></HomeEntry>} />
        <Route path="/about" element={<About />} />
        <Route path="/services" element={<Services />} />
        <Route path="/contact" element={<Contact />} />
        <Route path="/track" element={<TrackComplaint />} />

        {/* ===== Auth Routes ===== */}
        <Route path="/login" element={<GuestOnlyRoute><Login /></GuestOnlyRoute>} />
        <Route path="/register" element={<GuestOnlyRoute><Register /></GuestOnlyRoute>} />
        <Route path="/forgot-password" element={<GuestOnlyRoute><ForgotPassword /></GuestOnlyRoute>} />

        {/* ===== Citizen Routes (Protected) ===== */}
        <Route path="/citizen/dashboard" element={
          <ProtectedRoute role="Citizen"><CitizenDashboard /></ProtectedRoute>
        } />
        <Route path="/citizen/add-complaint" element={
          <ProtectedRoute role="Citizen"><AddComplaint /></ProtectedRoute>
        } />
        <Route path="/citizen/edit-complaint/:id" element={
          <ProtectedRoute role="Citizen"><EditComplaint /></ProtectedRoute>
        } />
        <Route path="/citizen/my-complaints" element={
          <ProtectedRoute role="Citizen"><MyComplaints /></ProtectedRoute>
        } />
        <Route path="/citizen/complaint/:id" element={
          <ProtectedRoute role="Citizen"><ComplaintDetails /></ProtectedRoute>
        } />
        <Route path="/citizen/profile" element={
          <ProtectedRoute role="Citizen"><CitizenProfile /></ProtectedRoute>
        } />

        {/* ===== Officer Routes (Protected) ===== */}
        <Route path="/officer/dashboard" element={
          <ProtectedRoute role="Officer"><OfficerDashboard /></ProtectedRoute>
        } />
        <Route path="/officer/complaints" element={
          <ProtectedRoute role="Officer"><AssignedComplaints /></ProtectedRoute>
        } />
        <Route path="/officer/update-complaint/:id" element={
          <ProtectedRoute role="Officer"><UpdateComplaint /></ProtectedRoute>
        } />

        {/* ===== Admin Routes (Protected) ===== */}
        <Route path="/admin/dashboard" element={
          <ProtectedRoute role="Admin"><AdminDashboard /></ProtectedRoute>
        } />
        <Route path="/admin/departments" element={
          <ProtectedRoute role="Admin"><ManageDepartments /></ProtectedRoute>
        } />
        <Route path="/admin/officers" element={
          <ProtectedRoute role="Admin"><ManageOfficers /></ProtectedRoute>
        } />
        <Route path="/admin/complaints" element={
          <ProtectedRoute role="Admin"><AllComplaints /></ProtectedRoute>
        } />
        <Route path="/admin/complaint/:id" element={
          <ProtectedRoute role="Admin"><AdminComplaintDetails /></ProtectedRoute>
        } />
        <Route path="/admin/routing-rules" element={
          <ProtectedRoute role="Admin"><RoutingRules /></ProtectedRoute>
        } />
        <Route path="*" element={<NotFoundRoute />} />
      </Routes>

      {/* Footer - shown only on public pages */}
      {!hideFooter && <Footer />}
    </div>
  );
}

export default App;
