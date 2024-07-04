package com.example.globalsedentary.activities;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.globalsedentary.R;
import com.example.globalsedentary.services.Registro;

import java.util.List;

public class adapter extends RecyclerView.Adapter<adapter.ViewHolder> {

    private List<Registro> registrosList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textNumber;
        public TextView textName;
        public TextView textTemp;
        public TextView textHumedad;
        public TextView textDate;
        public TextView textStatus;

        public ViewHolder(View view) {
            super(view);
            textName = view.findViewById(R.id.text_name);
            textStatus = view.findViewById(R.id.text_date);
            textTemp = view.findViewById(R.id.text_temp);
            textHumedad = view.findViewById(R.id.text_humidity);
        }
    }

    public adapter(List<Registro> registrosList) {
        this.registrosList = registrosList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Registro registro = registrosList.get(position);
        holder.textName.setText(String.valueOf(registro.getDispositivo()));
        holder.textTemp.setText(registro.getTemperatura());
        holder.textHumedad.setText(registro.getHumedad());
        String aux;
        if (registro.getIsUploaded() == "true") {
            aux="Sincronizado" ;
        } else {
            aux="Pendiente";
        }
        holder.textStatus.setText(aux);
    }

    @Override
    public int getItemCount() {
        return registrosList.size();
    }
}
