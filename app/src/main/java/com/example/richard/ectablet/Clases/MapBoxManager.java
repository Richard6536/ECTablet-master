package com.example.richard.ectablet.Clases;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;

import android.util.Log;
import android.view.View;

import com.example.richard.ectablet.Activity.MainActivity;
import com.example.richard.ectablet.Adapters.CardviewNavigationRoutesAdapter;
import com.example.richard.ectablet.Adapters.SpinnerIndNavigationAdapter;
import com.example.richard.ectablet.Adapters.TopSheetBehavior;
import com.example.richard.ectablet.Fragments.d.MapFragment;
import com.example.richard.ectablet.R;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.trip.model.RouteProgress;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback;
import com.mapbox.navigation.core.trip.session.RouteProgressObserver;
import com.mapbox.navigation.ui.route.NavigationMapRoute;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MapBoxManager implements RouteProgressObserver {

    public MapboxNavigation mapboxNavigation;

    public static MapboxMap mapboxMap;
    public static LocationComponent locationComponent;
    public static String styleString = "cknjd6of817xe17o7vjij0cjr";

    private MapView mapView;
    public Style styleMap;

    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute = null;

    GeoJsonSource geoJsonSource;
    SymbolLayer symbolLayer;

    int PERMISSION_ALL = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION};


    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";

    private Point origin;
    private Point destination;

    public void SetMapBoxMap(MapboxMap mbMap){
        mapboxMap = mbMap;

        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(-40.5804984,-73.1153157))
                .zoom(10)
                .bearing(180)
                .build());
    }

    public Style getStyleMap(){
        return styleMap;
    }

    public void getMapBoxAccessToken(Context context){
        // Mapbox Access token
        Mapbox.getInstance(context, "pk.eyJ1IjoiemVyb2RzIiwiYSI6ImNrM2t3cG0xNzB5bzgzam12dHdwY2luMXgifQ.3qy4aurdz4Vjp4QNr1-feg");
    }

    public MapboxMap GetMapBoxMap()
    {
        if(mapboxMap == null)
        {
            Log.d("ERROR MAPBOX", "MAPBOXMAP ES NULL");
        }

        return  mapboxMap;
    }

    public MapView GetMapView(){
        return mapView;
    }

    public void InicializarMapBox(OnMapReadyCallback contexto, View view, Bundle bundle){

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(bundle);
        mapView.getMapAsync(contexto);

        NavigationOptions.Builder navigation = MapboxNavigation.defaultNavigationOptionsBuilder(getApplicationContext(), Mapbox.getAccessToken());
        mapboxNavigation = new MapboxNavigation(navigation.build());

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mapboxNavigation.startTripSession();

    }

    public void inicializarObservers(){
        mapboxNavigation.registerRouteProgressObserver(this::onRouteProgressChanged);
        mapboxNavigation.registerRoutesObserver(routesObserver);
    }

    public void destruirObservers(){
        mapboxNavigation.unregisterRouteProgressObserver(this::onRouteProgressChanged);
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
    }

    public void CambiarStyle(String uriStyle){
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/zerods/" + uriStyle), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                styleMap = style;
            }
        });
    }

    public void DefinirStyle(AutocompleteSupportFragment autoCompleteSupportFragment, Resources recursos,
                             Activity actividad, View topRouteSheet)
    {
        String a = "";
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/zerods/"+styleString), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                styleMap = style;

                // Initialize Places.
                Places.initialize(getApplicationContext(), "AIzaSyAXRgGC2NP-RPcP0YCcpuw2QMUPEO4Hqsc");
                // Create a new Places client instance.
                //PlacesClient placesClient = Places.createClient(getApplicationContext());

                /**
                 * Initialize Places. For simplicity, the API key is hard-coded. In a production
                 * environment we recommend using a secure mechanism to manage API keys.
                 */
                if (!Places.isInitialized()) {
                    Places.initialize(getApplicationContext(), "AIzaSyAXRgGC2NP-RPcP0YCcpuw2QMUPEO4Hqsc");
                }

                // Initialize the AutocompleteSupportFragment.
                AutocompleteSupportFragment autocompleteFragment = autoCompleteSupportFragment;

                autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,
                        Place.Field.LAT_LNG));

                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {

                        if (symbolLayer != null) {
                            style.removeLayer("layer-id");
                            style.removeSource("source-id");
                        }

                        HideStatusBarNavigation hideStatusBarNavigation = new HideStatusBarNavigation();
                        ActionBarActivity actionBarActivity = new ActionBarActivity();

                        hideStatusBarNavigation.hideUI(actionBarActivity.view, actionBarActivity.actionBar);

                        // Add the marker image to map
                        style.addImage("marker-icon-id",
                                BitmapFactory.decodeResource(
                                        recursos, R.drawable.mapbox_marker_icon_default));

                        geoJsonSource = new GeoJsonSource("source-id", Feature.fromGeometry(
                                Point.fromLngLat(place.getLatLng().longitude, place.getLatLng().latitude)));
                        style.addSource(geoJsonSource);

                        symbolLayer = new SymbolLayer("layer-id", "source-id");
                        symbolLayer.withProperties(
                                PropertyFactory.iconImage("marker-icon-id")
                        );
                        style.addLayer(symbolLayer);

                        CameraPosition position = new CameraPosition.Builder()
                                .target(new LatLng(place.getLatLng().latitude,
                                        place.getLatLng().longitude)) // Sets the new camera position
                                .zoom(18) // Sets the zoom
                                .bearing(180) // Rotate the camera
                                .tilt(30) // Set the camera tilt
                                .build(); // Creates a CameraPosition from the builder

                        mapboxMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(position), 8000);


                        origin = Point.fromLngLat(-73.170903, -40.567141);
                        destination = Point.fromLngLat(place.getLatLng().longitude,
                                place.getLatLng().latitude);

                        initSource(style);

                        initLayers(style, recursos);

                        // Get the directions route from the Mapbox Directions API
                        //getRoute(mapboxMap, origin, destination);
                    }

                    @Override
                    public void onError(Status status) {
                        // TODO: Handle the error.
                        Log.i(TAG, "An error occurred: " + status);
                    }
                });

                mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull LatLng point) {

                        double lat = -40.567703;
                        double longitude = -73.170281;

                        if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
                            Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                            lat = lastKnownLocation.getLatitude();
                            longitude = lastKnownLocation.getLongitude();
                        }

                        LatLng pointOr = new LatLng(lat, longitude);

                        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                        Point originPoint = Point.fromLngLat(pointOr.getLongitude(), pointOr.getLatitude());
                        //Point originPoint = Point.fromLngLat(
                        // locationComponent.getLastKnownLocation().getLongitude(),
                        // locationComponent.getLastKnownLocation().getLatitude());

                        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                        if (source != null) {
                            source.setGeoJson(Feature.fromGeometry(destinationPoint));
                        }

                        getRoute(originPoint, destinationPoint);
                        TopSheetBehavior.from(topRouteSheet).setState(TopSheetBehavior.STATE_EXPANDED);

                        return false;
                    }
                });

                verifyStoragePermissions(actividad);

                try {

                    SessionManager session = new SessionManager(getApplicationContext());
                    Map<String, String> data = session.getPoints();
                    if(data.get(SessionManager.KEY_POINTS) != null){

                        String dataPoints = data.get(SessionManager.KEY_POINTS).replaceAll("\\\\", "");

                        JSONArray jsonArray = new JSONArray(dataPoints);

                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject json_data = jsonArray.getJSONObject(i);

                            String nombre = json_data.getString("Name");
                            String latitud = json_data.getString("Latitude");
                            String longitud = json_data.getString("Longitude");
                            boolean activado = json_data.getBoolean("Activado");

                            if(activado){
                                // Add the marker image to map
                                style.addImage(nombre,
                                        BitmapFactory.decodeResource(
                                                recursos, R.drawable.mapbox_marker_icon_default));

                                geoJsonSource = new GeoJsonSource(nombre, Feature.fromGeometry(
                                        Point.fromLngLat(Double.parseDouble(longitud), Double.parseDouble(latitud))));
                                style.addSource(geoJsonSource);

                                symbolLayer = new SymbolLayer(nombre, nombre);
                                symbolLayer.withProperties(
                                        PropertyFactory.iconImage(nombre));

                                style.addLayer(symbolLayer);

                                mapboxMap.addPolygon(generatePerimeter(
                                        new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud)),
                                        0.015,
                                        200));
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                navigationMapRoute = new NavigationMapRoute.Builder(mapView, mapboxMap, getLifecycleOwner())
                        .withMapboxNavigation(mapboxNavigation)
                        .withVanishRouteLineEnabled(true)
                        .build();
            }
        });
    }

    private PolygonOptions generatePerimeter(LatLng centerCoordinates, double radiusInKilometers,
                                             int numberOfSides) {
        List<LatLng> positions = new ArrayList<>();
        double distanceX = radiusInKilometers / (111.319 * Math.cos(centerCoordinates.getLatitude()
                * Math.PI / 180));
        double distanceY = radiusInKilometers / 110.574;

        double slice = (2 * Math.PI) / numberOfSides;

        double theta;
        double x;
        double y;
        LatLng position;
        for (int i = 0; i < numberOfSides; ++i) {
            theta = i * slice;
            x = distanceX * Math.cos(theta);
            y = distanceY * Math.sin(theta);

            position = new LatLng(centerCoordinates.getLatitude() + y,
                    centerCoordinates.getLongitude() + x);
            positions.add(position);
        }
        return new PolygonOptions()
                .addAll(positions)
                .fillColor(Color.BLUE)
                .alpha(0.4f);
    }

    public LifecycleOwner getLifecycleOwner() {
        Activity context = ControllerActivity.activiyAbiertaActual;
        return (LifecycleOwner) context;
    }

    private void getRoute(Point origin, Point destination) {

        List<Point> points = new ArrayList<>();
        points.add(origin);
        points.add(destination);

        mapboxNavigation.requestRoutes(
                RouteOptions.builder()
                        .accessToken(Mapbox.getAccessToken())
                        .alternatives(true)
                        .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                        .coordinates(points)
                        .language("es")
                        .baseUrl("https://api.mapbox.com")
                        .requestUuid("pk.eyJ1IjoiemVyb2RzIiwiYSI6ImNqZHhnaWlpcTAxa3MycW5xZ2g1dDEydXkifQ.AHkuaAWcgu69eXCwJlLvpw")
                        .user("zerods")
                        .build(), new RoutesRequestCallback() {
                    @Override
                    public void onRoutesReady(@NotNull List<? extends DirectionsRoute> list) {

                        Log.d("RouteA ---- requestRoutes", "Route created");
                        navigationMapRoute.addRoutes(list);
                    }

                    @Override
                    public void onRoutesRequestFailure(@NotNull Throwable throwable, @NotNull RouteOptions routeOptions) {
                        Log.d("RouteA", "onRoutesRequestFailure");
                    }

                    @Override
                    public void onRoutesRequestCanceled(@NotNull RouteOptions routeOptions) {
                        Log.d("RouteA", "onRoutesRequestCanceled");
                    }
                }
        );
    }
    /**
     * Add the route and marker sources to the map
     */
    public void initSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID));

        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[] {
                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
        loadedMapStyle.addSource(iconGeoJsonSource);
    }

    /**
     * Add the route and marker icon layers to the map
     */
    public void initLayers(@NonNull Style loadedMapStyle, Resources recursos) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(10f),
                lineColor(ColorTemplate.getHoloBlue())
        );
        loadedMapStyle.addLayer(routeLayer);
    }
    /*
    private void getRoute(MapboxMap mapboxMap, Point origin, Point destination) {
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken("pk.eyJ1IjoiemVyb2RzIiwiYSI6ImNrM2t3cG0xNzB5bzgzam12dHdwY2luMXgifQ.3qy4aurdz4Vjp4QNr1-feg")
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
// You can get the generic HTTP info about the response
                //Timber.d("Response code: " + response.code());
                if (response.body() == null) {
                    //Timber.e("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    //Timber.e("No routes found");
                    return;
                }

// Get the directions route
                currentRoute = response.body().routes().get(0);

                String duration = String.valueOf(TimeUnit.SECONDS.toMinutes(currentRoute.duration().longValue()));
                Double distance = currentRoute.distance();

                //MapFragment.duration.setText(duration + " min.");
                //MapFragment.distance.setText(distance + " mts.");

// Make a toast which displays the route's distance
                //Toast.makeText(DirectionsActivity.this, String.format( getString(R.string.directions_activity_toast_message),currentRoute.distance()), Toast.LENGTH_SHORT).show();

                if (mapboxMap != null) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

// Retrieve and update the source designated for showing the directions route
                            GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

// Create a LineString with the directions route's geometry and
// reset the GeoJSON source for the route LineLayer source
                            if (source != null) {
                                source.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                //Timber.e("Error: " + throwable.getMessage());
                //Toast.makeText(getActivity(), "Error: " + throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
*/
    private void verifyStoragePermissions(Activity activity) {
        // Check if permissions are enabled and if not request
        int permissionLocation = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            if (Build.VERSION.SDK_INT >= 23) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ALL);
            }
            else{
                ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
            }
        }
        else
        {
            //Inicializa Mapas, posición & crea directorios si los permisos se han concedido
            enableLocationComponent(activity.getBaseContext());
        }


    }

    @SuppressLint("MissingPermission")
    public void enableLocationComponent(Context context) {

        if(styleMap == null)
        {
            Log.d("ERROR MAPBOX", "STYLEMAP ES NULL");
            return;
        }

        // Get an instance of the component
        locationComponent = mapboxMap.getLocationComponent();

        // Activate with a built LocationComponentActivationOptions object
        locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(context, styleMap).build());

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING_GPS);

        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.GPS);

        locationComponent.setLocationComponentEnabled(true);

        locationComponent.zoomWhileTracking(20, 3000);
    }

    public void findPosition(){

        locationComponent.setCameraMode(
                CameraMode.TRACKING_GPS,
                3000 /*duration*/,
                20.0 /*zoom*/,
                270.0 /*bearing, use current/determine based on the tracking mode*/,
                40.0 /*tilt*/,
                null /*transition listener*/);
        locationComponent.zoomWhileTracking(20, 3000);
    }


    private RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NotNull List<? extends DirectionsRoute> list) {
            Log.d("RouteA ---- onRoutesChanged", "Route created");
            //navigationMapRoute.addRoutes(list);
        }
    };

    @Override
    public void onRouteProgressChanged(@NotNull RouteProgress routeProgress) {
        try{
            if(routeProgress != null){
                String intr = routeProgress.getCurrentLegProgress().getCurrentStepProgress().getStep().maneuver().instruction();
                String intr2 = routeProgress.getCurrentLegProgress().getCurrentStepProgress().getStep().rotaryName();
                String intr3 = routeProgress.getCurrentLegProgress().getCurrentStepProgress().getStep().destinations();
                String intr4 = routeProgress.getCurrentLegProgress().getCurrentStepProgress().getStep().drivingSide();

                String intr5 = routeProgress.getCurrentLegProgress().getCurrentStepProgress().getStep().name();

                String intr6 = routeProgress.getCurrentLegProgress().getCurrentStepProgress().getStep().rotaryName();
                String nextStreetName = routeProgress.getCurrentLegProgress().getUpcomingStep().name();

                List<SpinnerIndNavigationAdapter> arraySteps = new ArrayList<>();

                for (RouteLeg leg : routeProgress.getRoute().legs()) {
                    for (LegStep steps : leg.steps()) {
                        if (steps.maneuver().modifier() != null) {

                            String currentType = steps.maneuver().type();
                            String currentModifier = steps.maneuver().modifier();

                            if (currentModifier.contains(" ")) {
                                String[] spl = currentModifier.split(" ");
                                currentModifier = spl[0] + "_" + spl[1];
                            }
                            String currentIconName = "direction_" + currentType + "_" + currentModifier;
                            arraySteps.add(new SpinnerIndNavigationAdapter(steps.name(), currentIconName));
                        }
                    }
                }

                //AgregarIndicacionesNavegacion(arraySteps);
                Log.d("RouteB", arraySteps.toString());

                String type = routeProgress.getCurrentLegProgress().getUpcomingStep().maneuver().type();
                String modifier = routeProgress.getCurrentLegProgress().getUpcomingStep().maneuver().modifier();

                if (modifier.contains(" ")){
                    String[] spl = modifier.split(" ");
                    modifier = spl[0] + "_" + spl[1];
                }

                String iconName = "direction_" + type + "_" + modifier;

                try {

                    int distance = (int)routeProgress.getDistanceRemaining();
                    String textDistance = "";
                    if(distance > 100000){
                        textDistance = (int)(distance / 1000) + " km.";
                    }
                    else{
                        textDistance = distance + " mts.";
                    }

                    ControllerActivity.mapFragmentIntance.imgDirectionStreet.setImageResource(getApplicationContext().getResources().getIdentifier(iconName, "drawable", getApplicationContext().getPackageName()));
                    ControllerActivity.mapFragmentIntance.txtDirectionStreet.setText(nextStreetName);
                    ControllerActivity.mapFragmentIntance.txtDuration.setText((int)(routeProgress.getDurationRemaining() / 60)+" min.");
                    ControllerActivity.mapFragmentIntance.txtDistance.setText(textDistance);
                }
                catch (Exception a){
                    Log.d("EXCEP_Street", "Error: " + a.getMessage());
                }

                //mpf.img_navigation_icon.setImageResource(getApplicationContext().getResources().getIdentifier(iconName, "drawable", getApplicationContext().getPackageName()));
                //mpf.txtRouteInfo.setText(nextStreetName);

                Log.d("RouteA", "Progress: " + intr + " --- "+ intr2 + " --- "+ intr3 + " --- "+ intr4 + " --- "+ intr5 + " --- "+ intr6 + " --- " + nextStreetName + " --- " + iconName);
            }
        }
        catch (Exception e){
            Log.d("EXCEP", "Error: " + e.getMessage());
        }
    }

    public void MostrarSiguienteDireccion(){

    }

    public void AgregarIndicacionesNavegacion(List<SpinnerIndNavigationAdapter> indicationList)
    {
        try {

            MapFragment mpf = new MapFragment();
            CardviewNavigationRoutesAdapter adapter = new CardviewNavigationRoutesAdapter(
                    getApplicationContext(), indicationList, new CardviewNavigationRoutesAdapter.OnItemClickListener() {
                @Override
                public void onItemClicked(int position, int itemPosition, SpinnerIndNavigationAdapter indication) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
