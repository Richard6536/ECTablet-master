package com.example.richard.ectablet.Adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.richard.ectablet.Activity.MainActivity;
import com.example.richard.ectablet.Clases.MapBoxManager;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.R;

import java.util.HashMap;
import java.util.List;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class CardviewNavigationRoutesAdapter  extends RecyclerView.Adapter<CardviewNavigationRoutesAdapter.CardViewHolder> {
    private List<SpinnerIndNavigationAdapter> indicationNavList;
    SpinnerIndNavigationAdapter indication;

    private OnItemClickListener onItemClickListener; // Global scope
    public interface OnItemClickListener {
        void onItemClicked(int position, int itemPosition, SpinnerIndNavigationAdapter indication);
    }

    private Context mCtx;

    //getting the context and product list with constructor
    public CardviewNavigationRoutesAdapter(Context mCtx, List<SpinnerIndNavigationAdapter> lista, CardviewNavigationRoutesAdapter.OnItemClickListener onItemClickListener) {
        this.mCtx = mCtx;
        this.indicationNavList = lista;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout._cardview_lista_navigation, null);

        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        indication = indicationNavList.get(position);

        holder.imgIndicationRoute.setImageResource(getApplicationContext().getResources()
                .getIdentifier(indication.geImagePath(), "drawable", getApplicationContext().getPackageName()));

        holder.txtIndicationRoute.setText(indication.getIndicationName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SpinnerIndNavigationAdapter indicationSelected = indicationNavList.get(position);
                onItemClickListener.onItemClicked(position, 0, indicationSelected);
            }
        });
    }

    @Override
    public int getItemCount() {
        return indicationNavList.size();
    }
    class CardViewHolder extends RecyclerView.ViewHolder{

        ImageView imgIndicationRoute;
        TextView txtIndicationRoute;

        public CardViewHolder(View itemView) {
            super(itemView);

            SessionManager sessionController = new SessionManager(getApplicationContext());
            imgIndicationRoute = itemView.findViewById(R.id.imgIndicationRoute);
            txtIndicationRoute = itemView.findViewById(R.id.txtIndicationRoute);

            HashMap<String, Boolean> datosTheme = sessionController.getTheme();
            boolean isNightTheme = datosTheme.get(SessionManager.KEY_THEME);

            int themeNightText = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDay);
            int themeDayText = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryBlack);

            if(isNightTheme)
            {
                txtIndicationRoute.setTextColor(themeNightText);
                imgIndicationRoute.setColorFilter(themeNightText);
            }
            else
            {
                txtIndicationRoute.setTextColor(themeDayText);
                imgIndicationRoute.setColorFilter(themeDayText);
            }

        }
    }
}
