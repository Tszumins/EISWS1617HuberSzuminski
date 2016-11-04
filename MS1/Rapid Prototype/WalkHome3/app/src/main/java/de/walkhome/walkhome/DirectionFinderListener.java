package de.walkhome.walkhome;

/**
 * Created by yannikhuber on 02.11.16.
 */

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import de.walkhome.walkhome.Route;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}