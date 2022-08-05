package com.example.itsstudytime;

import static com.example.itsstudytime.DetailedExamList.POS;
import static com.example.itsstudytime.DetailedExamList.SERIA;

import android.app.PendingIntent;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Update;

import com.example.itsstudytime.database.Esame;
import com.example.itsstudytime.dates.DateFormatter;

import java.util.ArrayList;
import java.util.List;

public class EsameAdapter extends RecyclerView.Adapter<EsameAdapter.ViewHolder> {
    private ArrayList<Esame> esami;
    private AppCompatActivity activity;

    NotificationCompat.Builder notifBuilder;

    public EsameAdapter(AppCompatActivity activity, List<Esame> esami)  {
        this.esami = (ArrayList<Esame>) esami;
        this.activity = activity;
    }

    public ArrayList<Esame> getEsami() {
        return esami;
    }

    public void setEsami(ArrayList<Esame> esami) {
        this.esami = esami;
    }

    public void addEsame (Esame esame) {
        this.esami.add(esame);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Esame> esami) {
        this.esami=esami;
        notifyDataSetChanged();
    }

    public void removeEsame (int position) {
        this.esami.remove(position);
        notifyDataSetChanged();
    }

    public void clear() {
        this.esami.clear();
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.esame_preview, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nome.setText(esami.get(position).getNome());
        holder.data.setText(DateFormatter.formatDate(esami.get(position).getData()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), DetailedExamList.class);
                intent.putExtra(SERIA, esami.get(position));
                intent.putExtra(POS, position);
                activity.startActivity(intent);


            }
        });


    }

    @Override
    public int getItemCount() {
        return esami.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nome;
        TextView data;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.nome = itemView.findViewById(R.id.tv_nome);
            this.data = itemView.findViewById(R.id.tv_data);
        }
    }

}
