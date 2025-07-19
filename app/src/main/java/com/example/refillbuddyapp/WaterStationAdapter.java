package com.example.refillbuddyapp;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// adapter für das recyclerview (adapter pattern wie in der vorlesung)
public class WaterStationAdapter extends RecyclerView.Adapter<WaterStationAdapter.ViewHolder> {
    
    // liste der wasserstellen
    private List<WaterStation> waterStations;
    // map für berechnete entfernungen (position -> entfernung)
    private Map<Integer, String> distanceMap;
    
    // konstruktor
    public WaterStationAdapter(List<WaterStation> waterStations) {
        this.waterStations = waterStations;
        this.distanceMap = new HashMap<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // layout für ein item inflaten
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_water_station, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // daten für ein item setzen
        WaterStation station = waterStations.get(position);
        holder.titleTextView.setText(station.getName());
        holder.descriptionTextView.setText(station.getDescription());
        
        // entfernung anzeigen falls berechnet, sonst fallback
        String distance = distanceMap.get(position);
        if (distance != null) {
            holder.distanceTextView.setText(distance);
        } else {
            holder.distanceTextView.setText("in der Nähe"); // fallback
        }
    }
    
    @Override
    public int getItemCount() {
        // anzahl der items
        return waterStations.size();
    }
    
    // entfernungen für alle wasserstellen aktualisieren
    public void updateDistances(Location userLocation) {
        if (userLocation == null) return;
        
        // für alle wasserstellen entfernung berechnen
        for (int i = 0; i < waterStations.size(); i++) {
            WaterStation station = waterStations.get(i);
            double distance = MainActivity.LocationHelper.calculateDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    station.getLat(), station.getLng()
            );
            // formatiert speichern
            distanceMap.put(i, MainActivity.LocationHelper.formatDistance(distance));
        }
        
        // recyclerview aktualisieren
        notifyDataSetChanged();
    }
    
    // viewholder klasse (viewholder pattern)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView distanceTextView;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // views finden
            titleTextView = itemView.findViewById(R.id.tv_title);
            descriptionTextView = itemView.findViewById(R.id.tv_description);
            distanceTextView = itemView.findViewById(R.id.tv_distance);
        }
    }
    
    // einfache waterstation klasse für die daten
    public static class WaterStation {
        private String name;
        private String description;
        private double lat;
        private double lng;
        
        // konstruktor
        public WaterStation(String name, String description, double lat, double lng) {
            this.name = name;
            this.description = description;
            this.lat = lat;
            this.lng = lng;
        }
        
        // getter methoden
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getLat() { return lat; }
        public double getLng() { return lng; }
    }
} 