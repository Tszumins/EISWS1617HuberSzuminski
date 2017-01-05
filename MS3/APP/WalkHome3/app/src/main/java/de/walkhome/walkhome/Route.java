package de.walkhome.walkhome;

/**
 * Created by yannikhuber on 02.11.16.
 */

import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}