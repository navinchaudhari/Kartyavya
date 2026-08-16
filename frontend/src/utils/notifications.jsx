import React from 'react';
import { toast } from 'react-toastify';

export function apiErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const payload = error?.response?.data;
  if (payload?.fieldErrors && typeof payload.fieldErrors === 'object') {
    return Object.entries(payload.fieldErrors)
      .map(([field, message]) => `${field}: ${message}`)
      .join(' • ');
  }
  return payload?.message || error?.message || fallback;
}

export function notifyError(error, fallback) {
  toast.error(apiErrorMessage(error, fallback));
}

export function confirmAction({
  title = 'Please confirm',
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  danger = false,
}) {
  return new Promise((resolve) => {
    let toastId;
    const finish = (result) => {
      toast.dismiss(toastId);
      resolve(result);
    };

    toastId = toast(
      <div className="toast-confirmation">
        <div className="toast-confirmation-icon">
          <i className={`bi ${danger ? 'bi-exclamation-triangle-fill' : 'bi-question-circle-fill'}`} />
        </div>
        <div className="toast-confirmation-content">
          <strong>{title}</strong>
          <span>{message}</span>
          <div className="toast-confirmation-actions">
            <button type="button" className="btn btn-sm btn-light" onClick={() => finish(false)}>
              {cancelLabel}
            </button>
            <button
              type="button"
              className={`btn btn-sm ${danger ? 'btn-danger' : 'btn-primary'}`}
              onClick={() => finish(true)}
            >
              {confirmLabel}
            </button>
          </div>
        </div>
      </div>,
      {
        autoClose: false,
        closeButton: false,
        closeOnClick: false,
        draggable: false,
        position: 'top-center',
      },
    );
  });
}
