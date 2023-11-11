package com.soket.soketmobilecollector;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class JurnalTransaksiAdapter extends RecyclerView.Adapter<JurnalTransaksiAdapter.JurnalTransaksiViewHolder>
{
    private final ArrayList<clsItemValue> dataList;

    public JurnalTransaksiAdapter(ArrayList<clsItemValue> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public JurnalTransaksiAdapter.JurnalTransaksiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate( R.layout.row_jurnal_transaksi, parent, false);
        return new JurnalTransaksiViewHolder(view);
    }


    @Override
    public void onBindViewHolder(JurnalTransaksiAdapter.JurnalTransaksiViewHolder holder, int position) {
        holder.txtItem.setText(dataList.get(position).getItem());
        holder.txtValue.setText(dataList.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return (dataList != null) ? dataList.size() : 0;
    }

    public static class JurnalTransaksiViewHolder extends RecyclerView.ViewHolder{
        private final TextView txtItem;
        private final TextView txtValue;

        public JurnalTransaksiViewHolder(View itemView) {
            super(itemView);
            txtItem =  itemView.findViewById(R.id.textViewItem);
            txtValue =  itemView.findViewById(R.id.textViewValue);
        }
    }
}
