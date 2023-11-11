package com.soket.soketmobilecollector;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MutasiSimpananAdapter extends RecyclerView.Adapter<MutasiSimpananAdapter.MutasiSimpananViewHolder>
{
    private final ArrayList<clsMutasiSimpanan> dataList;

    public MutasiSimpananAdapter(ArrayList<clsMutasiSimpanan> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MutasiSimpananAdapter.MutasiSimpananViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate( R.layout.row_mutasi_simpanan, parent, false);
        return new MutasiSimpananViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MutasiSimpananAdapter.MutasiSimpananViewHolder holder, int position) {
        holder.txtTanggal.setText(dataList.get(position).getTanggal());
        holder.txtTipe.setText(dataList.get(position).getTipeTransaksi());
        holder.txtDebet.setText(dataList.get(position).getDebet());
        holder.txtKredit.setText(dataList.get(position).getKredit());
        holder.txtSaldo.setText(dataList.get(position).getSaldo());
        holder.txtUser.setText(dataList.get(position).getUserID());
    }

    @Override
    public int getItemCount() {
        return (dataList != null) ? dataList.size() : 0;
    }

    public static class MutasiSimpananViewHolder extends RecyclerView.ViewHolder{
        private final TextView  txtTanggal;
        private final TextView txtTipe;
        private final TextView txtDebet;
        private final TextView txtKredit;
        private final TextView txtSaldo;
        private final TextView txtUser;

        public MutasiSimpananViewHolder(View itemView) {
                super(itemView);
                txtUser =  itemView.findViewById(R.id.textViewSisa);
                txtTanggal =  itemView.findViewById(R.id.textViewValue);
                txtTipe =  itemView.findViewById(R.id.textViewPRD);
                txtDebet =  itemView.findViewById(R.id.textViewPokok);
                txtKredit =  itemView.findViewById(R.id.textViewBunga);
                txtSaldo =  itemView.findViewById(R.id.textViewDenda);
        }
    }
 }

