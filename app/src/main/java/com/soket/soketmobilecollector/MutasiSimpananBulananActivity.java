package com.soket.soketmobilecollector;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MutasiSimpananBulananActivity extends AppCompatActivity {
    private String newTabID;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    //create adapter
    private MutasiSimpananAdapter adMutasiSimpanan;
    private ArrayList<clsMutasiSimpanan> arrMutasiSimpanan  ;
    private clsMutasiSimpanan MutasiSimpanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutasi_simpanan_bulanan);

        ImageButton btnMutasiTabunganBack =  findViewById(R.id.imageButtonBackMutasiSimpanan);
        TextView headerSimpananID =  findViewById((R.id.textViewTabIDMutasiTabungan));
        TextView headerSimpananNama =  findViewById((R.id.textViewNamadiMutasiTabungan));
        RecyclerView daftarMutasi =  findViewById(R.id.RecyclerViewMutasiTeller);
        TextView txtInstitutionNameMutasiSimpananBulanan =  findViewById(R.id.textViewinstitutionNameMutasiTabunganBulanan);
        ImageView logo =  findViewById(R.id.imageViewMutasiSimpananBulanan);
        TextView txtMasaKontrak =  findViewById(R.id.textViewMasaKontrakdiMutasiSimpBulanan);
        TextView txtTglMasuk =  findViewById(R.id.textViewTglMasukdiMutasiSimpBulanan);

        String nama;
        String masaKontrak;
        String tglMasuk;
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                newTabID = null;
                nama =null;
                tglMasuk =null;
                masaKontrak =null;
            } else {
                newTabID = extras.getString("TABID");
                nama = extras.getString("NAMA");
                tglMasuk = extras.getString("TGLMASUK");
                masaKontrak = extras.getString("MASAKONTRAK");
            }
        }
        else
        {
            newTabID= (String) savedInstanceState.getSerializable("TABID");
            nama = (String) savedInstanceState.getSerializable("NAMA");
            tglMasuk = (String) savedInstanceState.getSerializable("TGLMASUK");
            masaKontrak = (String) savedInstanceState.getSerializable("MASAKONTRAK");
        }

        headerSimpananID.setText(newTabID);
        headerSimpananNama.setText(nama);

        assert masaKontrak != null;
        int kontrak = Integer.parseInt(masaKontrak);
        String setText;
        if (kontrak>20)
        {
            setText="Masa Kontrak : ~";
        }
        else
        {
            setText="Masa Kontrak : ".concat(masaKontrak.concat(" tahun"));
        }
        txtMasaKontrak.setText(setText);
        setText="Tanggal Masuk : ".concat(tglMasuk);
        txtTglMasuk.setText(setText);

        //cek apa keaddan login apa tidak
        //for saved data
        clsPreference currPreference = new clsPreference();
        //String currUser = currPreference.getLoggedInUser(this);
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);


        if (currLoggedInStatus) {
            //currCapem = currPreference.getRegisteredCapem(this);
            //currKolektor =currPreference.getRegisteredKolektor(this);f
            hashKey=getString(R.string.hashKey);
            urlAPI=getString(R.string.webService );
            institutionCode= currPreference.getRegisteredInstitutionCode(this);
            txtInstitutionNameMutasiSimpananBulanan.setText(currPreference.getRegisteredInstitutionName(this) );

            //setlogo
            if (txtInstitutionNameMutasiSimpananBulanan.getText().toString().toUpperCase().contains("KOPERASI") || txtInstitutionNameMutasiSimpananBulanan.getText().toString().toUpperCase().contains("KSP") || txtInstitutionNameMutasiSimpananBulanan.getText().toString().toUpperCase().contains("KPN") || txtInstitutionNameMutasiSimpananBulanan.getText().toString().toUpperCase().contains("KSU"))
            {
                logo.setImageResource(R.drawable.mylogo_small);
            }
            else
            {
                logo.setImageResource(R.drawable.logolpd);
            }

            //-----------------bind ke recycler view
            arrMutasiSimpanan= new ArrayList<>();
            adMutasiSimpanan = new MutasiSimpananAdapter(arrMutasiSimpanan);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MutasiSimpananBulananActivity.this);
            daftarMutasi.setLayoutManager(layoutManager);
            daftarMutasi.setAdapter(adMutasiSimpanan);
            bindDataMutasiTeller();

        } else {
            currPreference.clearLoggedInUser(this);
            finish();

        }

        btnMutasiTabunganBack.setOnClickListener(view -> finish());
    }

    private void bindDataMutasiTeller()
    {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("jenis", "sipokok");
            postparams.put("txtNorek", newTabID);
            //clsGenerateSHA hash256 = new clsGenerateSHA();
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("sipokok").concat(newTabID).concat(hashKey),true));
            sendPostForBindMutasiSimpanan(urlAPI.concat("/list_transaksi") , postparams);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Toast.makeText(MutasiSimpananBulananActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForBindMutasiSimpanan(String url, JSONObject JSONBodyParam)  {
        try{

            RequestQueue requestQueue= Volley.newRequestQueue(this);
            JsonObjectRequest objectRequest=new JsonObjectRequest(Request.Method.POST, url, JSONBodyParam,
                    response -> {
                        //do something
                        String ResponseCode ;
                        String ResponseDescription;
                        try {
                            ResponseCode = response.getString("responseCode");
                            ResponseDescription = response.getString("responseDescription");
                            if (ResponseCode.equalsIgnoreCase("00"))
                            {
                                // Getting JSON Array node
                                JSONArray Mutations = response.getJSONArray("hasil");
                                JSONObject Mutation;
                                for (int k = 0; k < Mutations.length(); k++)
                                {
                                    Mutation = Mutations.getJSONObject(k);
                                    MutasiSimpanan = new clsMutasiSimpanan();
                                    MutasiSimpanan.setNoTransaksi(Mutation.getString("NoTransaksi"));
                                    MutasiSimpanan.setTanggal(Mutation.getString("Tanggal"));
                                    MutasiSimpanan.setTipeTransaksi(Mutation.getString("TipeTransaksi"));
                                    MutasiSimpanan.setDebet(formatedAmount(Mutation.getString("Debet")));
                                    MutasiSimpanan.setKredit(formatedAmount(Mutation.getString("Kredit")));
                                    MutasiSimpanan.setSaldo(formatedAmount(Mutation.getString("Saldo")));
                                    MutasiSimpanan.setUserID(Mutation.getString("UserID"));
                                    arrMutasiSimpanan.add(MutasiSimpanan);
                                }
                                adMutasiSimpanan.notifyDataSetChanged();
                            }
                            else
                            {
                                Toast.makeText(MutasiSimpananBulananActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MutasiSimpananBulananActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MutasiSimpananBulananActivity.this,error.toString() , Toast.LENGTH_LONG).show()
            )
            {
                @Override
                public Map<String, String> getHeaders()  {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(MutasiSimpananBulananActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private String formatedAmount(String amount)
    {
        //Locale localeID = new Locale("in", "ID");
        if (amount.length()>0)
        {
            NumberFormat formatRupiah = NumberFormat.getInstance();
            return formatRupiah.format( Double.parseDouble(amount));
        }
        else
        {
            return amount;
        }

    }
}