import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { complaintAPI } from "../../services/api";
import LocationPicker from "../../components/LocationPicker";
import { toast } from "react-toastify";
import { apiErrorMessage } from "../../utils/notifications";

function AddComplaint() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    complaintTitle: "",
    description: "",
    areaLocation: "",
    latitude: "",
    longitude: "",
  });
  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState("");
  const [nearby, setNearby] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [locating, setLocating] = useState(false);
  const progress = useMemo(
    () =>
      [
        form.complaintTitle,
        form.description,
        form.areaLocation,
        form.latitude,
        form.longitude,
        image,
      ].filter(Boolean).length,
    [form, image],
  );
  useEffect(
    () => () => {
      if (preview) URL.revokeObjectURL(preview);
    },
    [preview],
  );
  const setLocation = (latitude, longitude) =>
    setForm((current) => ({ ...current, latitude, longitude }));
  const detectLocation = () => {
    setError("");
    setLocating(true);
    if (!navigator.geolocation) {
      setLocating(false);
      return setError("Geolocation is not supported by this browser.");
    }
    navigator.geolocation.getCurrentPosition(
      async ({ coords }) => {
        const lat = Number(coords.latitude.toFixed(7));
        const lng = Number(coords.longitude.toFixed(7));
        setLocation(lat, lng);
        setLocating(false);
        try {
          const response = await complaintAPI.nearby(lat, lng, 1);
          setNearby(response.data);
        } catch {
          setNearby([]);
        }
      },
      () => {
        setLocating(false);
        setError(
          "Location permission was denied. Select the point manually on the map.",
        );
      },
      { enableHighAccuracy: true, timeout: 10000 },
    );
  };
  const chooseImage = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type))
      return setError("Only JPG, PNG and WEBP images are allowed.");
    if (file.size > 5 * 1024 * 1024)
      return setError("Image size cannot exceed 5 MB.");
    if (preview) URL.revokeObjectURL(preview);
    setImage(file);
    setPreview(URL.createObjectURL(file));
    setError("");
  };
  const submit = async (event) => {
    event.preventDefault();
    setError("");
    if (form.complaintTitle.trim().length < 5)
      return setError("Complaint title must contain at least 5 characters.");
    if (form.description.trim().length < 20)
      return setError("Describe the issue in at least 20 characters.");
    if (!form.areaLocation.trim())
      return setError("Enter a recognizable area or landmark.");
    if (!form.latitude || !form.longitude)
      return setError("Capture or select the complaint location.");
    if (!image) return setError("Upload a clear complaint image.");
    const data = new FormData();
    Object.entries(form).forEach(([key, value]) => data.append(key, value));
    data.append("complaintImage", image);
    setLoading(true);
    try {
      const response = await complaintAPI.create(data);
      toast.success(
        `Complaint registered. Tracking code: ${response.data.trackingCode}`,
        {
          autoClose: 6500,
        },
      );
      navigate(`/citizen/complaint/${response.data.complaintId}`);
    } catch (requestError) {
      const message = apiErrorMessage(
        requestError,
        "Complaint submission failed.",
      );
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };
  return (
    <>
      <section className="dashboard-header premium-header">
        <div className="container d-flex justify-content-between align-items-center flex-wrap gap-3">
          <div>
            <span className="eyebrow">AI-ASSISTED CIVIC REPORTING</span>
            <h2>
              <i className="bi bi-geo-alt me-2" />
              Report a verified issue
            </h2>
            <p className="mb-0">
              Upload evidence, pin the exact location and let Kartyavya route
              the complaint.
            </p>
          </div>
          <div className="completion-ring">
            <strong>{progress}/6</strong>
            <span>ready</span>
          </div>
        </div>
      </section>
      <main className="container pb-5">
        <div className="row g-4">
          <div className="col-lg-8">
            <form className="card-custom p-4 p-lg-5" onSubmit={submit}>
              {error && <div className="alert alert-danger">{error}</div>}
              <div className="form-section-title">
                <span>01</span>
                <div>
                  <h5>Issue details</h5>
                  <p>
                    Clear descriptions help Gemini classify the issue
                    accurately.
                  </p>
                </div>
              </div>
              <label className="form-label">Complaint title *</label>
              <input
                className="form-control mb-3"
                value={form.complaintTitle}
                onChange={(e) =>
                  setForm({ ...form, complaintTitle: e.target.value })
                }
                placeholder="Large pothole near the school entrance"
                maxLength="200"
              />
              <label className="form-label">Detailed description *</label>
              <textarea
                className="form-control mb-3"
                rows="5"
                value={form.description}
                onChange={(e) =>
                  setForm({ ...form, description: e.target.value })
                }
                placeholder="Describe urgency, size, risks and nearby landmarks…"
                maxLength="3000"
              />
              <label className="form-label">Area / landmark *</label>
              <input
                className="form-control mb-4"
                value={form.areaLocation}
                onChange={(e) =>
                  setForm({ ...form, areaLocation: e.target.value })
                }
                placeholder="Shanti Nagar, opposite municipal school"
              />
              <div className="form-section-title">
                <span>02</span>
                <div>
                  <h5>Photo evidence</h5>
                  <p>
                    The citizen uploads the image; no image URL is accepted.
                  </p>
                </div>
              </div>
              <label
                className={`upload-dropzone ${preview ? "has-image" : ""}`}
              >
                <input
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  capture="environment"
                  onChange={chooseImage}
                  hidden
                />
                {preview ? (
                  <>
                    <img src={preview} alt="Complaint preview" />
                    <div>
                      <strong>Replace evidence image</strong>
                      <small>{image?.name}</small>
                    </div>
                  </>
                ) : (
                  <>
                    <i className="bi bi-camera" />
                    <div>
                      <strong>Choose or capture image</strong>
                      <small>JPG, PNG or WEBP • maximum 5 MB</small>
                    </div>
                  </>
                )}
              </label>
              <div className="form-section-title mt-4">
                <span>03</span>
                <div>
                  <h5>Verified location</h5>
                  <p>Use browser GPS or click the map to place the pin.</p>
                </div>
              </div>
              <div className="d-flex gap-2 flex-wrap mb-3">
                <button
                  type="button"
                  className="btn btn-outline-primary"
                  onClick={detectLocation}
                  disabled={locating}
                >
                  <i className="bi bi-crosshair me-2" />
                  {locating ? "Locating…" : "Use my current location"}
                </button>
                {form.latitude && (
                  <span className="coordinate-chip">
                    <i className="bi bi-check-circle-fill" /> {form.latitude},{" "}
                    {form.longitude}
                  </span>
                )}
              </div>
              <LocationPicker
                latitude={Number(form.latitude)}
                longitude={Number(form.longitude)}
                onChange={setLocation}
              />
              <button
                className="btn btn-primary-custom mt-4"
                disabled={loading}
              >
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" />
                    Submitting…
                  </>
                ) : (
                  <>
                    <i className="bi bi-stars me-2" />
                    Classify and submit complaint
                  </>
                )}
              </button>
            </form>
          </div>
          {/* <aside className="col-lg-4">
            <div className="intelligence-card sticky-lg-top">
              <div className="ai-orb">
                <i className="bi bi-cpu" />
              </div>
              <h4>What happens next?</h4>
              <div className="workflow-item">
                <span>1</span>
                <div>
                  <strong>Gemini classification</strong>
                  <p>
                    Gemini determines category, severity, confidence and the
                    suggested department code.
                  </p>
                </div>
              </div>
              <div className="workflow-item">
                <span>2</span>
                <div>
                  <strong>Department routing</strong>
                  <p>
                    If no department is configured, the complaint remains
                    pending for Admin setup.
                  </p>
                </div>
              </div>
              <div className="workflow-item">
                <span>3</span>
                <div>
                  <strong>Officer allocation</strong>
                  <p>
                    If no officer exists, Admin can assign one after creating or
                    allocating the officer.
                  </p>
                </div>
              </div>
              {nearby.length > 0 && (
                <div className="nearby-warning">
                  <i className="bi bi-exclamation-triangle" />
                  <div>
                    <strong>{nearby.length} nearby open report(s)</strong>
                    <p>
                      Review possible duplicates before submitting another
                      complaint.
                    </p>
                  </div>
                </div>
              )}
            </div>
          </aside> */}
        </div>
      </main>
    </>
  );
}
export default AddComplaint;
