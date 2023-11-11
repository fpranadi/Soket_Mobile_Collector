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

public class MutasiPinjamanActivity extends AppCompatActivity {
    private String newTabID;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    //create adapter
    private MutasiPinjamanAdapter adMutasiPinjaman;
    private ArrayList<clsMutasiPinjaman> arrMutasiPinjaman  ;
    private clsMutasiPinjaman MutasiPinjaman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutasi_pinjaman);

        ImageButton btnMutasiTabunganBack =  findViewById(R.id.imageButtonBackMutasiSimpanan);
        TextView headerSimpananID =  findViewById((R.id.textViewTabIDMutasiTabungan));
        TextView headerSimpananNama =  findViewById((R.id.textViewNamadiMutasiTabungan));
        RecyclerView daftarMutasi =  findViewById(R.id.RecyclerViewMutasiTeller);
        TextView txtInstitutionNameMutasiSimpananBulanan =  findViewById(R.id.textViewinstitutionNameMutasiTabunganBulanan);
        ImageView logo =  findViewById(R.id.imageViewMutasiPinjaman);
        TextView txtAngsuran =  findViewById(R.id.textViewAngsurandiMutasiPinjaman);
        TextView txtJT =  findViewById(R.id.textViewJTdiMutasiPinjaman);
        TextView txtPokokPinjaman =  findViewById(R.id.textViewPokokPinjamandiMutasiPinjaman);


        String nama;
        String pokokPinjaman;
        String jT;
        String angsuran;
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                newTabID = null;
                nama =null;
                pokokPinjaman =null;
                jT =null;
                angsuran =null;
            } else {
                newTabID = extras.getString("KREDITID");
                nama = extras.getString("NAMA");
                pokokPinjaman = extras.getString("POKOK");
                jT = extras.getString("JT");
                angsuran = extras.getString("ANGSURAN");
            }
        }
        else
        {
            newTabID= (String) savedInstanceState.getSerializable("KREDITID");
            nama = (String) savedInstanceState.getSerializable("NAMA");
            pokokPinjaman = (String) savedInstanceState.getSerializable("POKOK");
            jT = (String) savedInstanceState.getSerializable("JT");
            angsuran = (String) savedInstanceState.getSerializable("ANGSURAN");
        }

        headerSimpananID.setText(newTabID);
        headerSimpananNama.setText(nama);
        assert angsuran != null;
        String setText="Angsuran : ".concat(formatedAmount(angsuran));
        txtAngsuran.setText(setText);
        setText="Jatuh Tempo : " .concat(jT);
        txtJT.setText(setText);
        setText="Pokok Pinjaman : ".concat(formatedAmount(pokokPinjaman));
        txtPokokPinjaman.setText(setText);

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
            arrMutasiPinjaman= new ArrayList<>();
            adMutasiPinjaman = new MutasiPinjamanAdapter(arrMutasiPinjaman);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MutasiPinjamanActivity.this);
            daftarMutasi.setLayoutManager(layoutManager);
            daftarMutasi.setAdapter(adMutasiPinjaman);
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
            postparams.put("jenis", "kredit");
            postparams.put("txtNorek", newTabID);
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("kredit").concat(newTabID).concat(hashKey),true));
            sendPostForBindMutasiSimpanan(urlAPI.concat("/list_transaksi") , postparams);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Toast.makeText(MutasiPinjamanActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                                    MutasiPinjaman = new clsMutasiPinjaman();
                                    MutasiPinjaman.setPRDNo(Mutation.getString("PRDNo"));
                                    MutasiPinjaman.setTanggal(Mutation.getString("Tanggal"));
                                    MutasiPinjaman.setAngsPokok(formatedAmount(Mutation.getString("AngsPokok")));
                                    MutasiPinjaman.setAngsBunga(formatedAmount(Mutation.getString("AngsBunga")));
                                    MutasiPinjaman.setDenda(formatedAmount(Mutation.getString("Denda")));
                                    MutasiPinjaman.setSisaPinjaman(formatedAmount(Mutation.getString("SisaPinjaman")));
                                    arrMutasiPinjaman.add(MutasiPinjaman);
                                }
                                adMutasiPinjaman.notifyDataSetChanged();
                            }
                            else
                            {
                                Toast.makeText(MutasiPinjamanActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MutasiPinjamanActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MutasiPinjamanActivity.this,error.toString() , Toast.LENGTH_LONG).show()
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
            Toast.makeText(MutasiPinjamanActivity.this,e.toString() , Toast.LENGTH_LONG).show();
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