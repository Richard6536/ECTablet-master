package com.example.richard.ectablet.Fragments.d;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.richard.ectablet.Adapters.CardviewAutosAdapter;
import com.example.richard.ectablet.Adapters.CardviewNavigationRoutesAdapter;
import com.example.richard.ectablet.Adapters.SpinnerAdapter;
import com.example.richard.ectablet.Adapters.SpinnerIndNavigationAdapter;
import com.example.richard.ectablet.Adapters.TopSheetBehavior;
import com.example.richard.ectablet.Clases.MapBoxManager;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.R;

import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.suke.widget.SwitchButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private View view;
    private MapBoxManager mapBoxBManager = new MapBoxManager();
    public static View topRouteSheet;
    public static RelativeLayout relativeDrawableSearch;
    public static RecyclerView recyclerView_direction_nav;

    public static ImageView imgIndicationRoute;
    public static TextView txtIndicationRoute;

    BottomSheetBehavior sheetBehavior;
    private LinearLayout bottom_sheet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);

        mapBoxBManager.InicializarMapBox(this, view, savedInstanceState);

        relativeDrawableSearch = view.findViewById(R.id.relativeDrawableSearch);

        topRouteSheet = view.findViewById(R.id.layoutSheetTop);
        TopSheetBehavior.from(topRouteSheet).setState(TopSheetBehavior.STATE_HIDDEN);

        try{

            recyclerView_direction_nav = (RecyclerView)view.findViewById(R.id.recyclerView_direction_nav);
            recyclerView_direction_nav.setHasFixedSize(true);
            GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 1);
            recyclerView_direction_nav.setLayoutManager(mGridLayoutManager);

        }
        catch (Exception e){
            e.printStackTrace();
        }
        /**
        duration = view.findViewById(R.id.txtDuration);
        distance = view.findViewById(R.id.txtDistance);

        bottom_sheet = view.findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);

         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         *

        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
         */

        return view;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        //this.mapboxMap = mapboxMap;
        mapBoxBManager.SetMapBoxMap(mapboxMap);

        AutocompleteSupportFragment autocompleteSupport = (AutocompleteSupportFragment)getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        Resources recursosFragmento = MapFragment.this.getResources();

        mapBoxBManager.DefinirStyle(autocompleteSupport, recursosFragmento, getActivity(), topRouteSheet);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            mapBoxBManager.enableLocationComponent(getContext());
        } else {
            getActivity().finish();
        }
    }

    public void putArguments(Bundle args){
        String value = args.getString("val");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapBoxBManager.GetMapView().onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapBoxBManager.GetMapView().onStop();
    }
    @Override
    public void onPause() {
        super.onPause();
        mapBoxBManager.GetMapView().onPause();

    }

    @Override
    public void onStart() {
        super.onStart();
        mapBoxBManager.GetMapView().onStart();
        mapBoxBManager.inicializarObservers();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapBoxBManager.destruirObservers();
        mapBoxBManager.GetMapView().onDestroy();
    }
}
