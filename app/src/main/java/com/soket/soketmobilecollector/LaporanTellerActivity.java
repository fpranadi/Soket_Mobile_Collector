package com.soket.soketmobilecollector;

import android.os.Bundle;
import android.widget.Button;
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

public class LaporanTellerActivity extends AppCompatActivity {
    private TextView txtTotalMutation;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    private String currCapem;
    private String currKolektor;
    private String currUser;

    //create adapter
    private MutasiTellerAdapter adMutasiTeller;
    private ArrayList<clsMutasiTeller> arrMutasiTeller;
    private clsMutasiTeller Mutasiteller;
    private double TotalMutasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_teller);

        //assigmn control
        Button btnKeluarLapTeller =  findViewById(R.id.btnKeluarMutasiTeller);
        TextView txtHeader =  findViewById(R.id.txtHeaderLaporanTeller);
        RecyclerView daftarMutasi =  findViewById(R.id.RecyclerViewMutasiTeller);
        txtTotalMutation =  findViewById(R.id.textViewTotalMutasi) ;

        //cek apa keaddan login apa tidak
        //for saved data
        clsPreference currPreference = new clsPreference();
        currUser= currPreference.getLoggedInUser(this);
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);

        if (currLoggedInStatus)
        {
            currCapem = currPreference.getRegisteredCapem(this);
            currKolektor = currPreference.getRegisteredKolektor(this);
            String setText="Mutasi Teller ".concat(currUser);
            txtHeader.setText(setText);
            hashKey=getString(R.string.hashKey);
            urlAPI=getString(R.string.webService );
            institutionCode= currPreference.getRegisteredInstitutionCode(this);

            //set adapter dan cardview nya dulu
            arrMutasiTeller= new ArrayList<>();
            adMutasiTeller = new MutasiTellerAdapter(arrMutasiTeller);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(LaporanTellerActivity.this);
            daftarMutasi.setLayoutManager(layoutManager);
            daftarMutasi.setAdapter(adMutasiTeller);

            bindDataMutasiTeller();
        }
        else
        {
            currPreference.clearLoggedInUser(this);
            finish();
        }

        btnKeluarLapTeller.setOnClickListener(view -> finish());
    }

    private void bindDataMutasiTeller()
    {
        try {
            JSONObject Params = new JSONObject();
            Params.put("InstitutionCode", institutionCode);
            Params.put("idKolektor", currKolektor );
            Params.put("idCapem", currCapem );
            Params.put("idUser", currUser);
            clsGenerateSHA hash256 = new clsGenerateSHA();
            Params.put("hashCode", hash256.hex256(institutionCode.concat(currKolektor).concat(currCapem).concat(currUser).concat(hashKey),true));
            sendPostForBindMutasiTeller(urlAPI.concat("/list_transaksi_kolektor") , Params);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Toast.makeText(LaporanTellerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForBindMutasiTeller(String url, JSONObject JSONBodyParam)  {
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
                                TotalMutasi=0;
                                for (int k = 0; k < Mutations.length(); k++)
                                {
                                    Mutation = Mutations.getJSONObject(k);
                                    Mutasiteller = new clsMutasiTeller();
                                    Mutasiteller.setTabID( Mutation.getString("tabID"));
                                    Mutasiteller.setTipeTransaksi( Mutation.getString("alias"));
                                    Mutasiteller.setTanggal(Mutation.getString("tanggal"));
                                    Mutasiteller.setMutasi(formatedAmount(Mutation.getString("saldo")));
                                    Mutasiteller.setNama(Mutation.getString("nama"));
                                    Mutasiteller.setReference(Mutation.getString("reference"));
                                    Mutasiteller.setJenisTransaksi(Mutation.getString("jenisTransaksi"));
                                    arrMutasiTeller.add(Mutasiteller);
                                    TotalMutasi=TotalMutasi + Double.parseDouble(Mutation.getString("saldo"));
                                }
                                adMutasiTeller.notifyDataSetChanged();
                                txtTotalMutation.setText(formatedAmount(String.valueOf(TotalMutasi)));
                            }
                            else
                            {
                                Toast.makeText(LaporanTellerActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LaporanTellerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(LaporanTellerActivity.this,error.toString() , Toast.LENGTH_LONG).show()
            )
            {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(LaporanTellerActivity.this,e.toString() , Toast.LENGTH_LONG).show();
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