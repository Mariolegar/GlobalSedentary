    package com.example.globalsedentary.ui.slideshow;

    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.preference.PreferenceManager;
    import android.os.Looper;
    import android.location.Location;
    import android.location.LocationManager;
    import android.content.Context;
    import android.view.GestureDetector;
    import android.view.MotionEvent;

    import com.google.android.gms.location.FusedLocationProviderClient;
    import com.google.android.gms.location.LocationServices;
    import com.google.android.gms.location.LocationRequest;
    import com.google.android.gms.location.LocationResult;
    import com.google.android.gms.location.LocationCallback;

    import org.osmdroid.api.IMapController;
    import org.osmdroid.config.Configuration;
    import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
    import org.osmdroid.util.GeoPoint;
    import org.osmdroid.views.MapView;
    import org.osmdroid.views.overlay.Marker;

    import androidx.annotation.NonNull;
    import androidx.fragment.app.Fragment;

    import com.example.globalsedentary.databinding.FragmentSlideshowBinding;

    import java.util.Locale;

    public class SlideshowFragment extends Fragment {
        private FragmentSlideshowBinding binding;
        private IMapController mapController;
        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState
        ) {
            binding = FragmentSlideshowBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            MapView map = binding.map;
            mapController = map.getController();

            map.setOnTouchListener(new View.OnTouchListener() {
                private final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        // Handle single tap (if needed)
                        return false; // Allow events to propagate
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        GeoPoint position = (GeoPoint) map.getProjection().fromPixels((int) e.getX(), (int) e.getY());

                        double latitude = position.getLatitude();
                        double longitude = position.getLongitude();

                        String markerTitle = String.format(
                                Locale.getDefault(),
                                "Latitud:%.6f, Longitud:%.6f",
                                latitude,
                                longitude
                        );

                        Marker marker = new Marker(map);
                        marker.setPosition(position);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setTitle(markerTitle);
                        map.getOverlays().add(marker);
                        map.invalidate();
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });


            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            double latitude = 0.0;
            double longitude = 0.0;

            Looper mainLooper = getActivity().getMainLooper();
            fusedLocationClient.requestLocationUpdates(
                    LocationRequest.create(),
                    new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult ) {
                            super.onLocationResult(locationResult);
                            String markerTitle = String.format(
                                    Locale.getDefault(),
                                    "Latitud:%.6f, Longitud:%.6f",
                                    latitude,
                                    longitude
                            );
                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                final double latitude = location.getLatitude();
                                final double longitude = location.getLongitude();
                                mapController.setCenter(new GeoPoint(latitude, longitude));

                                Marker deviceMarker = new Marker(map);
                                deviceMarker.setPosition(new GeoPoint(latitude, longitude));
                                deviceMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                deviceMarker.setTitle(markerTitle);
                                map.getOverlays().add(deviceMarker);
                            }
                        }
                    }, mainLooper
            );

            Configuration.getInstance().load(
                    getActivity(),
                    PreferenceManager.getDefaultSharedPreferences(getActivity())
            );
            map.setTileSource(TileSourceFactory.MAPNIK);
            IMapController mapControllerObject = map.getController();
            mapControllerObject.setZoom(15);
            mapControllerObject.setCenter(new GeoPoint(latitude, longitude));

            return root;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }
    }