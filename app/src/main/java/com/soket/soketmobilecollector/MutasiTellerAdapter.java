package com.soket.soketmobilecollector;

import android.app.ProgressDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MutasiTellerAdapter extends RecyclerView.Adapter<MutasiTellerAdapter.MutasiTellerViewHolder>
{
    private final ArrayList<clsMutasiTeller> dataList;
    public MutasiTellerAdapter(ArrayList<clsMutasiTeller> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MutasiTellerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate( R.layout.row_laporan_kolektor, parent, false);
        return new MutasiTellerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MutasiTellerViewHolder holder, int position) {
        holder.reffNo = dataList.get(position).getReference();
        holder.jenisTransaksi = dataList.get(position).getJenisTransaksi();

        holder.txtTabID.setText(dataList.get(position).getTabID());
        holder.txtTanggal.setText(dataList.get(position).getTanggal());
        holder.txtTipe.setText(dataList.get(position).getTipeTransaksi());
        holder.txtMutation.setText(dataList.get(position).getMutasi());
        holder.txtNama.setText(dataList.get(position).getNama().concat(" (").concat(holder.jenisTransaksi).concat(")"));

        holder.mCardView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return (dataList != null) ? dataList.size() : 0;
    }

    public static class MutasiTellerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView txtTabID;
        private final TextView txtTanggal;
        private final TextView txtTipe;
        private final TextView txtMutation;
        private final TextView txtNama;
        public View mCardView;
        private String reffNo;
        private  String jenisTransaksi;

        public MutasiTellerViewHolder(View itemView) {
            super(itemView);
            txtTabID =  itemView.findViewById(R.id.textViewTabID);
            txtTanggal =  itemView.findViewById(R.id.textViewTanggal);
            txtTipe =  itemView.findViewById(R.id.textViewTipeTransaksi);
            txtMutation =  itemView.findViewById(R.id.textViewMutasi);
            txtNama = itemView.findViewById(R.id.textViewNama);

            //new
            mCardView =  itemView.findViewById(R.id.card_view);
            mCardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ProgressDialog dialog;
            dialog = ProgressDialog.show(view.getContext(), "Loading","Mohon Tunggu...", true,false);

            //int position = (int) view.getTag();
            Intent intent;
            switch (jenisTransaksi) {
                case "Pinjaman" :
                    intent = new Intent(view.getContext(), TellerPinjamanPostedActivity.class);
                    intent.putExtra("REFERENCE", reffNo);
                    intent.putExtra("RESHOW",true);
                    view.getContext().startActivity(intent);
                    break;
                default:
                    switch (txtTipe.getText().toString().toUpperCase()) {
                        case "108":
                        case "100":
                        case "01":
                            //for Setoran
                            intent = new Intent(view.getContext(), SetoranTunaiPostedActivity.class);
                            intent.putExtra("REFERENCE", reffNo);
                            intent.putExtra("RESHOW",true);
                            view.getContext().startActivity(intent);

                            break;
                        case "514":
                        case "500":
                        case "02" :
                            //for Penarikan
                            intent = new Intent(view.getContext(), PenarikanTunaiPostedActivity.class);
                            intent.putExtra("REFERENCE", reffNo);
                            intent.putExtra("RESHOW",true);
                            view.getContext().startActivity(intent);
                            break;
                        default:
                            Toast.makeText(view.getContext(),"Jurnal Transaksi tidak tersedia ...!" , Toast.LENGTH_LONG).show();
                            break;
                    }
                    break;
            }
            dialog.dismiss();
        }
    }
}
