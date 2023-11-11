package com.soket.soketmobilecollector;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MutasiPinjamanAdapter extends RecyclerView.Adapter<MutasiPinjamanAdapter.MutasiPinjamanViewHolder>
{
    private final ArrayList<clsMutasiPinjaman> dataList;

    public MutasiPinjamanAdapter(ArrayList<clsMutasiPinjaman> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MutasiPinjamanAdapter.MutasiPinjamanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate( R.layout.row_mutasi_pinjaman, parent, false);
        return new MutasiPinjamanViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MutasiPinjamanAdapter.MutasiPinjamanViewHolder holder, int position) {
        holder.txtTanggal.setText(dataList.get(position).getTanggal());
        holder.txtPRDNo.setText(dataList.get(position).getPRDNo());
        holder.txtPokok.setText(dataList.get(position).getAngsPokok());
        holder.txtBunga.setText(dataList.get(position).getAngsBunga());
        holder.txtDenda.setText(dataList.get(position).getDenda());
        holder.txtSisa.setText(dataList.get(position).getSisaPinjaman());
    }

    @Override
    public int getItemCount() {
        return (dataList != null) ? dataList.size() : 0;
    }

    public static class MutasiPinjamanViewHolder extends RecyclerView.ViewHolder{
        private final TextView txtTanggal;
        private final TextView txtPRDNo;
        private final TextView txtPokok;
        private final TextView txtBunga;
        private final TextView txtDenda;
        private final TextView txtSisa;

        public MutasiPinjamanViewHolder(View itemView) {
            super(itemView);
            txtSisa =  itemView.findViewById(R.id.textViewSisa);
            txtTanggal =  itemView.findViewById(R.id.textViewValue);
            txtPRDNo =  itemView.findViewById(R.id.textViewPRD);
            txtPokok =  itemView.findViewById(R.id.textViewPokok);
            txtBunga =  itemView.findViewById(R.id.textViewBunga);
            txtDenda =  itemView.findViewById(R.id.textViewDenda);
        }
    }
}

