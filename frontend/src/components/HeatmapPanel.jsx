import React, { useEffect, useRef } from "react";
import L from "../utils/leaflet";

function addTextLine(container, label, value, emphasis = false) {
  const line = document.createElement("div");
  if (label) {
    const labelNode = document.createElement("span");
    labelNode.textContent = `${label}: `;
    line.appendChild(labelNode);
  }
  const valueNode = document.createElement(emphasis ? "strong" : "span");
  valueNode.textContent = value == null || value === "" ? "—" : String(value);
  line.appendChild(valueNode);
  container.appendChild(line);
}

function complaintPopup(point) {
  const container = document.createElement("div");
  container.className = "map-popup-content";
  addTextLine(container, "", point.complaintTitle, true);
  addTextLine(container, "Area", point.areaLocation);
  addTextLine(container, "Category", point.aiCategory);
  addTextLine(container, "Severity", point.aiSeverity);
  addTextLine(
    container,
    "Gemini confidence",
    `${Math.round(point.aiConfidence || 0)}%`,
  );
  addTextLine(container, "Status", point.status);
  return container;
}

function hotspotPopup(hotspot) {
  const container = document.createElement("div");
  container.className = "map-popup-content";
  addTextLine(container, "", `Gemini hotspot: ${hotspot.name}`, true);
  addTextLine(container, "Priority", hotspot.priority);
  addTextLine(container, "Complaints", hotspot.complaintCount);
  addTextLine(container, "Analysis", hotspot.explanation);
  addTextLine(container, "Recommended action", hotspot.recommendedAction);
  return container;
}

function HeatmapPanel({ points = [], hotspots = [] }) {
  const ref = useRef(null);
  const mapRef = useRef(null);
  const pointLayerRef = useRef(null);
  const hotspotLayerRef = useRef(null);

  useEffect(() => {
    if (!ref.current || mapRef.current) return undefined;
    const map = L.map(ref.current).setView([21.05, 75.77], 11);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "&copy; OpenStreetMap contributors",
    }).addTo(map);
    pointLayerRef.current = L.layerGroup().addTo(map);
    hotspotLayerRef.current = L.layerGroup().addTo(map);
    mapRef.current = map;
    const resizeTimer = window.setTimeout(() => map.invalidateSize(), 100);
    return () => {
      window.clearTimeout(resizeTimer);
      map.remove();
      mapRef.current = null;
      pointLayerRef.current = null;
      hotspotLayerRef.current = null;
    };
  }, []);

  useEffect(() => {
    if (!pointLayerRef.current) return;
    pointLayerRef.current.clearLayers();
    hotspotLayerRef.current?.clearLayers();
    const bounds = [];

    points.forEach((point) => {
      const severity = point.aiSeverity;
      const fillColor =
        severity === "HIGH"
          ? "#ef4444"
          : severity === "MEDIUM"
            ? "#f59e0b"
            : "#2563eb";
      const marker = L.circleMarker([point.latitude, point.longitude], {
        radius: severity === "HIGH" ? 12 : severity === "MEDIUM" ? 9 : 7,
        color: "#fff",
        weight: 2,
        fillColor,
        fillOpacity: 0.78,
      });
      marker.bindPopup(complaintPopup(point));
      marker.addTo(pointLayerRef.current);
      bounds.push([point.latitude, point.longitude]);
    });

    hotspots.forEach((hotspot) => {
      const priorityColor =
        hotspot.priority === "CRITICAL"
          ? "#7f1d1d"
          : hotspot.priority === "HIGH"
            ? "#dc2626"
            : hotspot.priority === "MEDIUM"
              ? "#d97706"
              : "#2563eb";
      const circle = L.circle(
        [hotspot.centerLatitude, hotspot.centerLongitude],
        {
          radius: Math.max(150, (hotspot.radiusKm || 1) * 1000),
          color: priorityColor,
          weight: 3,
          dashArray: "8 6",
          fillColor: priorityColor,
          fillOpacity: 0.12,
        },
      );
      circle.bindPopup(hotspotPopup(hotspot));
      circle.addTo(hotspotLayerRef.current);
      bounds.push([hotspot.centerLatitude, hotspot.centerLongitude]);
    });

    if (bounds.length && mapRef.current) {
      mapRef.current.fitBounds(bounds, { padding: [28, 28], maxZoom: 15 });
    }
  }, [points, hotspots]);

  return <div ref={ref} className="analytics-map" />;
}

export default HeatmapPanel;
