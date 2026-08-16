import React, { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { adminAPI, departmentAPI } from '../../services/api';
import { apiErrorMessage } from '../../utils/notifications';

const CATEGORIES = ['POTHOLE', 'GARBAGE', 'STREETLIGHT', 'WATER_LEAKAGE', 'OTHER'];

function RoutingRules() {
  const [rules, setRules] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [form, setForm] = useState({ category: 'WATER_LEAKAGE', departmentId: '' });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    try {
      const [ruleResponse, departmentResponse] = await Promise.all([
        adminAPI.getRoutingRules(),
        departmentAPI.getAll(),
      ]);
      setRules(ruleResponse.data);
      setDepartments(departmentResponse.data.filter((department) => department.enabled !== false));
    } catch (requestError) {
      const message = apiErrorMessage(requestError, 'Unable to load routing rules.');
      setError(message);
      toast.error(message);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const save = async (event) => {
    event.preventDefault();
    setError('');
    if (!form.departmentId) {
      setError('Select the department that owns this AI category.');
      return;
    }

    setSaving(true);
    try {
      const existing = rules.find((rule) => rule.category === form.category);
      const payload = {
        category: form.category,
        departmentId: Number(form.departmentId),
        active: true,
      };

      if (existing) {
        await adminAPI.updateRoutingRule(existing.id, payload);
        toast.success(`${form.category} routing rule updated.`);
      } else {
        await adminAPI.addRoutingRule(payload);
        toast.success(`${form.category} routing rule created.`);
      }
      await load();
    } catch (requestError) {
      const message = apiErrorMessage(requestError, 'Routing rule could not be saved.');
      setError(message);
      toast.error(message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <section className="dashboard-header premium-header">
        <div className="container">
          <span className="eyebrow">AI TO OPERATIONS</span>
          <h2><i className="bi bi-diagram-3 me-2" />Routing rules</h2>
          <p className="mb-0">Map every AI category to an active municipal department.</p>
        </div>
      </section>

      <main className="container pb-5">
        <div className="row g-4">
          <div className="col-lg-5">
            <form className="card-custom p-4" onSubmit={save}>
              <h5>Configure category ownership</h5>
              <p className="text-muted">Create the missing department first, then map the category here. Pending complaints can then be reprocessed.</p>
              {error && <div className="alert alert-danger py-2">{error}</div>}
              <label className="form-label">AI category</label>
              <select className="form-select mb-3" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })}>
                {CATEGORIES.map((category) => <option key={category}>{category}</option>)}
              </select>
              <label className="form-label">Responsible department</label>
              <select className="form-select mb-4" value={form.departmentId} onChange={(event) => setForm({ ...form, departmentId: event.target.value })}>
                <option value="">Select department</option>
                {departments.map((department) => (
                  <option key={department.departmentId} value={department.departmentId}>{department.departmentName}</option>
                ))}
              </select>
              <button className="btn btn-primary-custom" disabled={saving}>
                {saving ? 'Saving…' : <><i className="bi bi-link-45deg me-2" />Save routing rule</>}
              </button>
            </form>
          </div>

          <div className="col-lg-7">
            <div className="card-custom">
              <div className="table-responsive">
                <table className="table table-custom mb-0">
                  <thead><tr><th>AI category</th><th>Department</th><th>Status</th></tr></thead>
                  <tbody>
                    {CATEGORIES.map((category) => {
                      const rule = rules.find((item) => item.category === category);
                      return (
                        <tr key={category}>
                          <td><span className="category-chip">{category}</span></td>
                          <td>{rule?.departmentName || <span className="text-danger">Not mapped</span>}</td>
                          <td>
                            <span className={`badge ${rule?.active ? 'bg-success-subtle text-success' : 'bg-warning-subtle text-warning'}`}>
                              {rule?.active ? 'Active' : 'Setup required'}
                            </span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}

export default RoutingRules;
