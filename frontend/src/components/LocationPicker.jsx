import React, { useEffect, useRef } from 'react';
import L from '../utils/leaflet';

function LocationPicker({ latitude, longitude, onChange }) {
  const elementRef = useRef(null);
  const mapRef = useRef(null);
  const markerRef = useRef(null);

  useEffect(() => {
    if (!elementRef.current || !L || mapRef.current) return;
    const initial = [latitude || 21.05, longitude || 75.77];
    const map = L.map(elementRef.current, { zoomControl: true }).setView(initial, latitude ? 16 : 11);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(map);
    const marker = L.marker(initial, { draggable: true }).addTo(map);
    const update = ({ lat, lng }) => onChange(Number(lat.toFixed(7)), Number(lng.toFixed(7)));
    marker.on('dragend', () => update(marker.getLatLng()));
    map.on('click', (event) => { marker.setLatLng(event.latlng); update(event.latlng); });
    mapRef.current = map;
    markerRef.current = marker;
    setTimeout(() => map.invalidateSize(), 100);
    return () => { map.remove(); mapRef.current = null; markerRef.current = null; };
  }, []);

  useEffect(() => {
    if (!mapRef.current || !markerRef.current || !latitude || !longitude) return;
    const point = [latitude, longitude];
    markerRef.current.setLatLng(point);
    mapRef.current.setView(point, 16);
  }, [latitude, longitude]);

  return <div ref={elementRef} className="location-map" aria-label="Select complaint location on map" />;
}

export default LocationPicker;
