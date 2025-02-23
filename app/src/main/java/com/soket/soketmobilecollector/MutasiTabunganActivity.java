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

public class MutasiTabunganActivity extends AppCompatActivity {
    private String newTabID;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    //create adapter
    private MutasiSimpananAdapter adMutasiSimpanan;
    private ArrayList<clsMutasiSimpanan> arrMutasiSimpanan  ;
    private clsMutasiSimpanan MutasiSimpanan;

    private clsPreference currPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutasi_tabungan);

        ImageButton btnMutasiTabunganBack =  findViewById(R.id.imageButtonBackMutasiSimpanan);
        TextView headerSimpananID =  findViewById((R.id.textViewTabIDMutasiTabungan));
        TextView headerSimpananNama =  findViewById((R.id.textViewNamadiMutasiTabungan));
        RecyclerView daftarMutasi =  findViewById(R.id.RecyclerViewMutasiTeller);
        TextView txtInstitutionNameMutasiTabungan =  findViewById(R.id.textViewinstitutionNameMutasiTabunganBulanan);
        ImageView logo =  findViewById(R.id.imageViewTabungan);

        TextView alamat =  findViewById(R.id.textViewAlamatdiMutasiTabungan);
        TextView saldo =  findViewById(R.id.textViewSaldodiMutasiTabungan);

        String nama;
        String vAlamat;
        String vSaldo;
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                newTabID = null;
                nama =null;
                vAlamat =null;
                vSaldo ="";
            } else {
                newTabID = extras.getString("TABID");
                nama = extras.getString("NAMA");
                vAlamat =extras.getString("ALAMAT");
                vSaldo =extras.getString("SALDO");
            }
        }
        else
            {
                newTabID= (String) savedInstanceState.getSerializable("TABID");
                nama = (String) savedInstanceState.getSerializable("NAMA");
                vAlamat = (String) savedInstanceState.getSerializable("ALAMAT");
                vSaldo = (String) savedInstanceState.getSerializable("SALDO");
            }

        headerSimpananID.setText(newTabID);
        headerSimpananNama.setText(nama);
        alamat.setText(vAlamat);
        String setText="Saldo : ".concat(formatedAmount(vSaldo));
        saldo.setText(setText);

        //cek apa keaddan login apa tidak
        //for saved data
        currPreference = new clsPreference();
        //String currUser = currPreference.getLoggedInUser(this);
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);


        if (currLoggedInStatus) {
            //currCapem = currPreference.getRegisteredCapem(this);
            //currKolektor =currPreference.getRegisteredKolektor(this);f
            hashKey=getString(R.string.hashKey);
            urlAPI=getString(R.string.webService );
            institutionCode= currPreference.getRegisteredInstitutionCode(this);
            txtInstitutionNameMutasiTabungan.setText(currPreference.getRegisteredInstitutionName(this) );

            //setlogo
            if (txtInstitutionNameMutasiTabungan.getText().toString().toUpperCase().contains("KOPERASI") || txtInstitutionNameMutasiTabungan.getText().toString().toUpperCase().contains("KSP")  || txtInstitutionNameMutasiTabungan.getText().toString().toUpperCase().contains("KSU") || txtInstitutionNameMutasiTabungan.getText().toString().toUpperCase().contains("KPN"))
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
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MutasiTabunganActivity.this);
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
            postparams.put("jenis", "Tabungan");
            postparams.put("txtNorek", newTabID);
            //clsGenerateSHA hash256 = new clsGenerateSHA();
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("Tabungan").concat(newTabID).concat(hashKey),true));
            sendPostForBindMutasiSimpanan(urlAPI.concat("/list_transaksi") , postparams);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Toast.makeText(MutasiTabunganActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                                    MutasiSimpanan.setNoTransaksi(Mutation.getString("noTransaksi"));
                                    MutasiSimpanan.setTanggal(Mutation.getString("tanggal"));
                                    MutasiSimpanan.setTipeTransaksi(Mutation.getString("tipeTransaksi"));
                                    MutasiSimpanan.setDebet(formatedAmount(Mutation.getString("debet")));
                                    MutasiSimpanan.setKredit(formatedAmount(Mutation.getString("kredit")));
                                    MutasiSimpanan.setSaldo(formatedAmount(Mutation.getString("saldo")));
                                    MutasiSimpanan.setUserID(Mutation.getString("userID"));
                                    arrMutasiSimpanan.add(MutasiSimpanan);
                                }
                                adMutasiSimpanan.notifyDataSetChanged();
                            }
                            else
                            {
                                Toast.makeText(MutasiTabunganActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException  e) {
                            e.printStackTrace();
                            Toast.makeText(MutasiTabunganActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MutasiTabunganActivity.this,error.toString() , Toast.LENGTH_LONG).show()
            )
            {
                @Override
                public Map<String, String> getHeaders()  {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer ".concat(currPreference.getAccessToken(MutasiTabunganActivity.this)));

                    return headers;
                }
            };
            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(MutasiTabunganActivity.this,e.toString() , Toast.LENGTH_LONG).show();
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

    //private String formatedDate(String Tanggal) throws ParseException {
    //    Date date1=new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(Tanggal);
    //    return DateFormat.getDateTimeInstance().format(date1);
    //}
}
