import React, { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminAPI, complaintAPI, departmentAPI, getImageUrl } from '../../services/api';
import { toast } from 'react-toastify';
import { apiErrorMessage } from '../../utils/notifications';

const STATUS_OPTIONS = [
  ['CLASSIFIED', 'Classified'],
  ['PENDING_DEPARTMENT_SETUP', 'Pending department setup'],
  ['PENDING_OFFICER_ASSIGNMENT', 'Pending officer assignment'],
  ['ASSIGNED', 'Assigned'],
  ['IN_PROGRESS', 'In progress'],
  ['RESOLVED', 'Resolved'],
  ['REJECTED', 'Rejected'],
];

function AdminComplaintDetails() {
  const { id } = useParams();
  const [complaint, setComplaint] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [officers, setOfficers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [reprocessing, setReprocessing] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [assignment, setAssignment] = useState({ departmentId: '', officerId: '', remarks: '' });

  const populate = (item) => {
    setComplaint(item);
    setAssignment((current) => ({
      ...current,
      departmentId: item.departmentId ? String(item.departmentId) : '',
      officerId: item.officerId ? String(item.officerId) : '',
    }));
  };

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [reportResponse, departmentResponse, officerResponse] = await Promise.all([
        complaintAPI.getById(id),
        departmentAPI.getAll(),
        adminAPI.getOfficers(),
      ]);
      populate(reportResponse.data);
      setDepartments(departmentResponse.data.filter((department) => department.enabled !== false));
      setOfficers(officerResponse.data.filter((officer) => officer.enabled !== false));
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Unable to load the complaint assignment workspace.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [id]);

  const eligibleOfficers = useMemo(
    () => officers.filter((officer) => String(officer.departmentId) === String(assignment.departmentId)),
    [officers, assignment.departmentId],
  );

  const isTerminal = ['RESOLVED', 'REJECTED'].includes(complaint?.statusCode);
  const assignmentRequired = ['PENDING_DEPARTMENT_SETUP', 'PENDING_OFFICER_ASSIGNMENT', 'CLASSIFIED'].includes(complaint?.statusCode);

  const assignOfficer = async (event) => {
    event.preventDefault();
    setError(''); setMessage('');
    if (!assignment.departmentId) return setError('Select the department that owns this complaint.');
    if (assignment.remarks.trim().length < 5) return setError('Enter an audit remark of at least 5 characters.');
    setSaving(true);
    try {
      const response = await complaintAPI.assign(id, {
        departmentId: Number(assignment.departmentId),
        officerId: assignment.officerId ? Number(assignment.officerId) : null,
        remarks: assignment.remarks.trim(),
      });
      populate(response.data);
      setAssignment((current) => ({ ...current, remarks: '' }));
      const successMessage = assignment.officerId
        ? 'Officer assigned successfully. The citizen and officer notification events were published.'
        : 'Department assigned. This complaint remains in the pending-officer queue until an officer is selected.';
      setMessage(successMessage);
      toast.success(successMessage);
    } catch (requestError) {
      const failureMessage = apiErrorMessage(
        requestError,
        'Assignment failed. Verify that the selected officer belongs to the selected department.',
      );
      setError(failureMessage);
      toast.error(failureMessage);
    } finally { setSaving(false); }
  };

  const reprocess = async () => {
    setReprocessing(true); setError(''); setMessage('');
    try {
      const response = await complaintAPI.reprocess(id);
      populate(response.data);
      const successMessage = 'Routing was reprocessed using the latest department, officer and routing-rule configuration.';
      setMessage(successMessage);
      toast.success(successMessage);
    } catch (requestError) {
      const failureMessage = apiErrorMessage(requestError, 'Routing could not be reprocessed.');
      setError(failureMessage);
      toast.error(failureMessage);
    } finally { setReprocessing(false); }
  };

  const statusClass = (statusCode) => {
    if (statusCode === 'RESOLVED') return 'badge-completed';
    if (statusCode === 'IN_PROGRESS') return 'badge-inprogress';
    if (statusCode === 'REJECTED') return 'badge-danger';
    return 'badge-pending';
  };

  if (loading) return <div className="loading-spinner"><div className="spinner-border text-primary"><span className="visually-hidden">Loading...</span></div></div>;
  if (!complaint) return <div className="container py-5 text-center"><h4>Complaint not found</h4><Link to="/admin/complaints" className="btn btn-primary mt-3">Back to complaints</Link></div>;

  return <>
    <section className="dashboard-header premium-header">
      <div className="container d-flex justify-content-between align-items-center flex-wrap gap-3">
        <div><span className="eyebrow">CONTROLLED ASSIGNMENT WORKSPACE</span><h2>{complaint.complaintTitle}</h2><p className="mb-0">{complaint.trackingCode} · Complaint #{complaint.complaintId}</p></div>
        <Link to="/admin/complaints" className="btn hero-btn hero-btn-outline py-2 px-4"><i className="bi bi-arrow-left me-2" />Back</Link>
      </div>
    </section>

    <main className="container pb-5">
      {error && <div className="alert alert-danger mt-4"><i className="bi bi-exclamation-triangle me-2" />{error}</div>}
      {message && <div className="alert alert-success mt-4"><i className="bi bi-check-circle me-2" />{message}</div>}

      {complaint.pendingReason && <div className="pending-assignment-banner mt-4">
        <div className="pending-icon"><i className="bi bi-hourglass-split" /></div>
        <div><span className="eyebrow">ACTION REQUIRED</span><h5>{STATUS_OPTIONS.find(([code]) => code === complaint.statusCode)?.[1] || complaint.status}</h5><p className="mb-0">{complaint.pendingReason}</p></div>
        <div className="ms-lg-auto d-flex gap-2 flex-wrap"><Link to="/admin/departments" className="btn btn-sm btn-light">Departments</Link><Link to="/admin/officers" className="btn btn-sm btn-light">Officers</Link><Link to="/admin/routing-rules" className="btn btn-sm btn-light">Routing rules</Link></div>
      </div>}

      <div className="row g-4 mt-1">
        <div className="col-xl-8">
          <article className="detail-card fade-in-up">
            <header className="detail-header d-flex justify-content-between align-items-center gap-3 flex-wrap">
              <div><h4 className="mb-1">Complaint evidence</h4><small className="text-muted">Submitted {new Date(complaint.complaintDate).toLocaleString()}</small></div>
              <span className={`badge-status ${statusClass(complaint.statusCode)}`}>{complaint.statusCode.replaceAll('_', ' ')}</span>
            </header>
            <div className="detail-body">
              <div className="row g-3 mb-4">
                <div className="col-md-4"><div className="metric-glass"><span>AI category</span><strong>{complaint.aiCategory}</strong></div></div>
                <div className="col-md-4"><div className="metric-glass"><span>AI severity</span><strong>{complaint.aiSeverity}</strong></div></div>
                <div className="col-md-4"><div className="metric-glass"><span>Confidence</span><strong>{Number(complaint.aiConfidence || 0).toFixed(1)}%</strong></div></div>
              </div>
              <div className="detail-row"><div className="detail-label">Description</div><div className="detail-value">{complaint.description}</div></div>
              <div className="detail-row"><div className="detail-label">Area / landmark</div><div className="detail-value">{complaint.areaLocation}</div></div>
              <div className="detail-row"><div className="detail-label">Coordinates</div><div className="detail-value">{complaint.latitude}, {complaint.longitude} <a className="ms-2" href={`https://www.openstreetmap.org/?mlat=${complaint.latitude}&mlon=${complaint.longitude}#map=17/${complaint.latitude}/${complaint.longitude}`} target="_blank" rel="noreferrer"><i className="bi bi-box-arrow-up-right me-1" />Open map</a></div></div>
              <div className="detail-row"><div className="detail-label">Suggested routing code</div><div className="detail-value"><span className="category-chip">{complaint.suggestedDepartmentCode || 'UNMAPPED'}</span></div></div>
              {complaint.complaintImage && <div className="detail-row"><div className="detail-label">Citizen image</div><div className="detail-value"><img src={getImageUrl(complaint.complaintImage)} alt="Complaint evidence" className="complaint-image" /></div></div>}
              {complaint.resolutionRemark && <div className="detail-row"><div className="detail-label">Resolution remark</div><div className="detail-value">{complaint.resolutionRemark}</div></div>}
              {complaint.resolutionImage && <div className="detail-row"><div className="detail-label">Resolution proof</div><div className="detail-value"><img src={getImageUrl(complaint.resolutionImage)} alt="Resolution proof" className="complaint-image" /></div></div>}
            </div>
          </article>

          <section className="card-custom p-4 mt-4">
            <div className="d-flex justify-content-between align-items-center"><div><span className="eyebrow">IMMUTABLE AUDIT VIEW</span><h4>Status and assignment history</h4></div><span className="metric-chip">{complaint.statusHistory?.length || 0} entries</span></div>
            <div className="timeline-list mt-4">
              {(complaint.statusHistory || []).length === 0 && <p className="text-muted">No history entries have been recorded yet.</p>}
              {(complaint.statusHistory || []).map((entry, index) => <div className="workflow-item" key={`${entry.changedAt}-${index}`}><span>{index + 1}</span><div><strong>{entry.fromStatus?.replaceAll('_', ' ')} → {entry.toStatus?.replaceAll('_', ' ')}</strong><p>{entry.remarks || 'Workflow updated'} · {entry.changedByRole} · {new Date(entry.changedAt).toLocaleString()}</p></div></div>)}
            </div>
          </section>
        </div>

        <aside className="col-xl-4">
          <div className="card-custom p-4 mb-4"><span className="eyebrow">CITIZEN</span><h5>{complaint.citizenName}</h5><p className="mb-1">{complaint.citizenEmail}</p><p className="mb-0">{complaint.citizenMobile}</p></div>
          <div className="card-custom p-4 mb-4"><span className="eyebrow">CURRENT OWNERSHIP</span><h5>{complaint.departmentName || 'No department assigned'}</h5><p className="mb-1">{complaint.officerName || 'No officer assigned'}</p>{complaint.officerEmail && <p className="mb-0 text-muted">{complaint.officerEmail}</p>}</div>

          <form className="card-custom p-4 assignment-console" onSubmit={assignOfficer}>
            <span className="eyebrow">ADMIN ASSIGNMENT</span><h4>Complete complaint ownership</h4>
            <p className="text-muted small">An officer is selectable only when currently allocated to the selected department.</p>
            <label className="form-label">Department *</label>
            <select className="form-select mb-3" value={assignment.departmentId} disabled={isTerminal} onChange={(event) => setAssignment({ ...assignment, departmentId: event.target.value, officerId: '' })}>
              <option value="">Select department</option>{departments.map((department) => <option key={department.departmentId} value={department.departmentId}>{department.departmentName}</option>)}
            </select>
            <label className="form-label">Officer</label>
            <select className="form-select mb-3" value={assignment.officerId} disabled={isTerminal || !assignment.departmentId} onChange={(event) => setAssignment({ ...assignment, officerId: event.target.value })}>
              <option value="">Leave in pending-officer queue</option>{eligibleOfficers.map((officer) => <option key={officer.userId} value={officer.userId}>{officer.fullName}</option>)}
            </select>
            {assignment.departmentId && eligibleOfficers.length === 0 && <div className="alert alert-warning py-2 small">No active officer is allocated to this department. <Link to="/admin/officers">Add or allocate an officer</Link>, then return here.</div>}
            <label className="form-label">Audit remark *</label>
            <textarea className="form-control mb-3" rows="3" maxLength="1000" disabled={isTerminal} value={assignment.remarks} onChange={(event) => setAssignment({ ...assignment, remarks: event.target.value })} placeholder="Example: Officer added to Water & Drainage and assigned after department setup" />
            <button className="btn btn-primary-custom" disabled={saving || isTerminal}>{saving ? <><span className="spinner-border spinner-border-sm me-2" />Assigning</> : <><i className="bi bi-person-check me-2" />{assignment.officerId ? 'Assign selected officer' : 'Save department ownership'}</>}</button>
            {assignmentRequired && <button type="button" className="btn btn-outline-primary w-100 mt-2" disabled={reprocessing || isTerminal} onClick={reprocess}>{reprocessing ? 'Reprocessing...' : <><i className="bi bi-arrow-repeat me-2" />Reprocess latest routing</>}</button>}
          </form>
        </aside>
      </div>
    </main>
  </>;
}
export default AdminComplaintDetails;
