import React, { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { departmentAPI } from '../../services/api';
import { apiErrorMessage, confirmAction } from '../../utils/notifications';

const EMPTY_FORM = {
  departmentName: '',
  contactEmail: '',
  description: '',
  enabled: true,
};

function ManageDepartments() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editId, setEditId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    try {
      const response = await departmentAPI.getAdminAll();
      setItems(response.data);
    } catch (requestError) {
      const message = apiErrorMessage(requestError, 'Unable to load departments.');
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const openForm = (item = null) => {
    setEditId(item?.departmentId || null);
    setForm(item
      ? {
          departmentName: item.departmentName,
          contactEmail: item.contactEmail,
          description: item.description || '',
          enabled: item.enabled,
        }
      : EMPTY_FORM);
    setShowForm(true);
    setError('');
  };

  const submit = async (event) => {
    event.preventDefault();
    setError('');

    if (form.departmentName.trim().length < 2) {
      setError('Enter a valid department name.');
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.contactEmail.trim())) {
      setError('Enter a valid department contact email.');
      return;
    }

    setSaving(true);
    try {
      const payload = {
        ...form,
        departmentName: form.departmentName.trim(),
        contactEmail: form.contactEmail.trim().toLowerCase(),
        description: form.description.trim(),
      };

      if (editId) {
        await departmentAPI.update(editId, payload);
        toast.success('Department updated successfully.');
      } else {
        await departmentAPI.add(payload);
        toast.success('Department created successfully.');
      }

      setShowForm(false);
      await load();
    } catch (requestError) {
      const message = apiErrorMessage(requestError, 'Department operation failed.');
      setError(message);
      toast.error(message);
    } finally {
      setSaving(false);
    }
  };

  const disableDepartment = async (item) => {
    const confirmed = await confirmAction({
      title: 'Disable department?',
      message: `${item.departmentName} will no longer be available for new officer assignments or routing.`,
      confirmLabel: 'Disable department',
      danger: true,
    });
    if (!confirmed) return;

    try {
      await departmentAPI.delete(item.departmentId);
      toast.success('Department disabled successfully.');
      await load();
    } catch (requestError) {
      toast.error(apiErrorMessage(requestError, 'Unable to disable department.'));
    }
  };

  return (
    <>
      <section className="dashboard-header premium-header">
        <div className="container d-flex justify-content-between align-items-center flex-wrap gap-3">
          <div>
            <span className="eyebrow">SERVICE OWNERSHIP</span>
            <h2><i className="bi bi-buildings me-2" />Manage departments</h2>
            <p className="mb-0">Departments become available immediately for officer allocation and routing rules.</p>
          </div>
          <button type="button" className="btn hero-btn hero-btn-primary py-2 px-4" onClick={() => openForm()}>
            <i className="bi bi-plus-circle me-2" />Add department
          </button>
        </div>
      </section>

      <main className="container pb-5">
        {showForm && (
          <form className="card-custom p-4 mb-4" onSubmit={submit} noValidate>
            <h5>{editId ? 'Edit department' : 'Create department'}</h5>
            {error && <div className="alert alert-danger py-2">{error}</div>}
            <div className="row g-3">
              <div className="col-md-4">
                <label className="form-label">Department name *</label>
                <input
                  className="form-control"
                  value={form.departmentName}
                  maxLength="120"
                  onChange={(event) => setForm({ ...form, departmentName: event.target.value })}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Contact email *</label>
                <input
                  type="email"
                  className="form-control"
                  value={form.contactEmail}
                  maxLength="190"
                  onChange={(event) => setForm({ ...form, contactEmail: event.target.value })}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Description</label>
                <input
                  className="form-control"
                  value={form.description}
                  maxLength="500"
                  onChange={(event) => setForm({ ...form, description: event.target.value })}
                />
              </div>
              <div className="col-12 d-flex gap-2">
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : 'Save department'}
                </button>
                <button type="button" className="btn btn-outline-secondary" onClick={() => setShowForm(false)} disabled={saving}>
                  Cancel
                </button>
              </div>
            </div>
          </form>
        )}

        {!showForm && error && <div className="alert alert-danger">{error}</div>}

        <div className="card-custom">
          <div className="table-responsive">
            <table className="table table-custom mb-0">
              <thead>
                <tr>
                  <th>Department</th>
                  <th>Contact</th>
                  <th>Description</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr><td colSpan="5" className="text-center py-4">Loading departments…</td></tr>
                ) : items.length === 0 ? (
                  <tr><td colSpan="5" className="text-center py-4 text-muted">No departments found.</td></tr>
                ) : items.map((item) => (
                  <tr key={item.departmentId}>
                    <td className="fw-semibold">{item.departmentName}</td>
                    <td>{item.contactEmail}</td>
                    <td>{item.description || '—'}</td>
                    <td>
                      <span className={`badge ${item.enabled ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary'}`}>
                        {item.enabled ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-warning me-1"
                        title="Edit department"
                        aria-label={`Edit ${item.departmentName}`}
                        onClick={() => openForm(item)}
                      >
                        <i className="bi bi-pencil" />
                      </button>
                      {item.enabled && (
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-danger"
                          title="Disable department"
                          aria-label={`Disable ${item.departmentName}`}
                          onClick={() => disableDepartment(item)}
                        >
                          <i className="bi bi-slash-circle" />
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </>
  );
}

export default ManageDepartments;
