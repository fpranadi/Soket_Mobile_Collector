package com.soket.soketmobilecollector;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AngsuranKolektifActivity extends AppCompatActivity {

    private TextView TabID;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    private String currCapem;
    private String currKolektor;
    private String currUser;

    //for killed from posted
    public static AngsuranKolektifActivity  angsuranKolektifActivity;

    private ProgressDialog dialog;
    private String setText;

    clsPreference currPreference ;

    private ArrayList<String> arrKodeAutoDebet ;
    private ArrayAdapter<String> adKodeAutoDebet;
    private JSONArray daftarAutoDebet;

    private EditText NoRekeningTujuan;
    private EditText amount;
    private EditText angsuran;
    private EditText autoDebetId;

    private double amountD;
    private double angsuranD;
    private double saldoD;

    private String nama;
    private String dateTimePRM;
    private Spinner kodeAutoDebet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_angsuran_kolektif);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //for killed from ouside
        angsuranKolektifActivity = this;

        ImageButton btnBack =  findViewById(R.id.imageButtonBackAngsuranKolektifPosting);
        ImageButton btnNext =  findViewById(R.id.imageButtonNextAngsuranKolektifPosting);

        TabID =  findViewById(R.id.textViewTabIDAngsuranKolektifPosting);
        TextView txtNama = findViewById(R.id.textViewNamadiAngsuranKolektifPosting);
        TextView txtSaldo = findViewById(R.id.textViewSaldodiAngsuranKolektifPosting);
        TextView txtLastTrans = findViewById(R.id.textViewAlamatdiAngsuranKolektifPosting);
        ImageView logo =  findViewById(R.id.imageViewAngsuranKolektifPosting);

        TextView txtInstitutionNameSetoranTunai =  findViewById(R.id.textViewinstitutionNameAngsuranKolektifPosting);

        kodeAutoDebet = findViewById(R.id.spDaftarKodeAutoDebet_AngsuranKolektifPosting);
        NoRekeningTujuan=findViewById(R.id.editTextNoRekening_AngsuranKolektifPosting) ;
        amount=findViewById(R.id.editTextAmount_AngsuranKolektifPosting) ;
        angsuran = findViewById(R.id.editTextAngsuran_AngsuranKolektifPosting);
        autoDebetId=findViewById(R.id.editTextAutoDebetID_AngsuranKolektifPosting);

        ImageButton addAmount = findViewById(R.id.imageButtonAddAmount_AngsuranKolektifPosting);
        ImageButton minAmount = findViewById(R.id.imageButtonSubstractAmount_AngsuranKolektifPosting);

        currPreference = new clsPreference();
        currUser= currPreference.getLoggedInUser(this);
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);

        String newTabID;
        String saldo;
        String lastTrans;
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                newTabID = null;
                nama =null;
                saldo =null;
                saldoD=0;
                lastTrans =null;
            } else {
                newTabID = extras.getString("TABID");
                nama = extras.getString("NAMA");
                saldo = extras.getString("SALDO");
                saldoD = Double.parseDouble(saldo);
                lastTrans = extras.getString("LASTTRANS");
            }
        }
        else
        {
            newTabID = (String) savedInstanceState.getSerializable("TABID");
            nama = (String) savedInstanceState.getSerializable("NAMA");
            saldo = (String) savedInstanceState.getSerializable("SALDO");
            saldoD = Double.parseDouble(saldo);
            lastTrans = (String) savedInstanceState.getSerializable("LASTTRANS");
        }

        if (currLoggedInStatus)
        {
            currCapem = currPreference.getRegisteredCapem(this);
            currKolektor = currPreference.getRegisteredKolektor(this);
            hashKey=getString(R.string.hashKey);
            urlAPI=getString(R.string.webService );
            institutionCode= currPreference.getRegisteredInstitutionCode(this);
            txtInstitutionNameSetoranTunai.setText(currPreference.getRegisteredInstitutionName(this) );

            //setlogo
            if (txtInstitutionNameSetoranTunai.getText().toString().toUpperCase().contains("KOPERASI") || txtInstitutionNameSetoranTunai.getText().toString().toUpperCase().contains("KSP") || txtInstitutionNameSetoranTunai.getText().toString().toUpperCase().contains("KPN") || txtInstitutionNameSetoranTunai.getText().toString().toUpperCase().contains("KSU"))
            {
                logo.setImageResource(R.drawable.mylogo_small);
            }
            else
            {
                logo.setImageResource(R.drawable.logolpd);
            }

            TabID.setText(newTabID);
            txtNama.setText(nama);
            assert saldo != null;
            setText ="Saldo : ".concat(formatedAmount(saldo));
            txtSaldo.setText(setText);
            setText ="Transaksi terakhir :".concat(lastTrans);
            txtLastTrans.setText(setText);

            //isi spiner daftar autodebet id tabungan
            arrKodeAutoDebet = new ArrayList<>();
            adKodeAutoDebet = new ArrayAdapter<>(this, R.layout.spinner_item, arrKodeAutoDebet);
            kodeAutoDebet.setAdapter(adKodeAutoDebet);
            kodeAutoDebet.setSelection(0,true);

            //sent post to get daftar transfer and kode bank
            fillDaftarAutoDebet();

        }
        else
        {
            currPreference.clearLoggedInUser(this);
            finish();
        }

        btnBack.setOnClickListener(view -> finish());


        addAmount.setOnClickListener(v -> {

            if ((amountD + angsuranD)<saldoD)
            {
                amountD = amountD + angsuranD;
            }
            amount.setText(formatedAmount(amountD));
        });

        minAmount.setOnClickListener(v -> {
            if (amountD > angsuranD)
            {
                amountD = amountD - angsuranD;
            }
            amount.setText(formatedAmount(amountD));
        });

        kodeAutoDebet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                try {
                    JSONObject autoDebet;
                    autoDebet = daftarAutoDebet.getJSONObject(position);
                    NoRekeningTujuan.setText(autoDebet.getString("tabunganDest"));
                    amountD = autoDebet.getDouble("amount");
                    angsuranD = amountD;
                    if (amountD > saldoD)
                    {
                        amountD = 0;
                    }
                    amount.setText(formatedAmount(amountD));
                    angsuran.setText(amount.getText().toString());
                    autoDebetId.setText(autoDebet.getString("autoDebetId"));
                } catch (JSONException e) {
                    Toast.makeText(AngsuranKolektifActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
                Toast.makeText(AngsuranKolektifActivity.this, "Pilih salah satu Program Pinjaman !!!", Toast.LENGTH_LONG).show();

            }

        });

        btnNext.setOnClickListener(view -> {
            dialog = ProgressDialog.show(AngsuranKolektifActivity.this, "Posting","Please wait...", true);
            PostingAngsuranKolektif();
        });

    }

    //for killed from other activity
    public static AngsuranKolektifActivity getInstance(){
        return   angsuranKolektifActivity;
    }

    private String formatedAmount(String amount)
    {
        //Locale localeID = new Locale("in", "ID");
        if (!amount.isEmpty())
        {
            NumberFormat formatRupiah = NumberFormat.getInstance();
            return formatRupiah.format( Double.parseDouble(amount));
        }
        else
        {
            return amount;
        }

    }

    private String formatedAmount(double amount)
    {
        //Locale localeID = new Locale("in", "ID");
        if (amount>0)
        {
            NumberFormat formatRupiah = NumberFormat.getInstance();
            return formatRupiah.format( Math.round(amount));
        }
        else
        {
            return  String.valueOf( Math.round(amount));
        }

    }

    private void fillDaftarAutoDebet()
    {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("TabID", TabID.getText().toString());
            postparams.put("HashCode", clsGenerateSHA.hex256(institutionCode.concat(TabID.getText().toString()).concat(hashKey),true));
            sendPostForFillDaftarAutoDebet(urlAPI.concat("/list_data_autodebet_anggota") , postparams);
        }
        catch (JSONException e)
        {
            Toast.makeText(AngsuranKolektifActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForFillDaftarAutoDebet(String url, JSONObject JSONBodyParam)  {
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
                                daftarAutoDebet = response.getJSONArray("details");
                                JSONObject AutoDebet;
                                if (daftarAutoDebet.length()>0)
                                {
                                    for (int k = 0; k < daftarAutoDebet.length(); k++) {
                                        AutoDebet = daftarAutoDebet.getJSONObject(k);
                                        arrKodeAutoDebet.add(AutoDebet.getString("autoDebetId").concat("-").concat(AutoDebet.getString("description")));
                                    }
                                    adKodeAutoDebet.notifyDataSetChanged();
                                }
                            }
                            else
                            {
                                Toast.makeText(AngsuranKolektifActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(AngsuranKolektifActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        } catch (Exception e){
                            Toast.makeText(AngsuranKolektifActivity.this,e.toString() , Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(AngsuranKolektifActivity.this,"Session telah Habis, Mohon Login Kembali !!!" , Toast.LENGTH_LONG).show()
            )
            {
                @Override
                public Map<String, String> getHeaders()  {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(AngsuranKolektifActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    //posting
    private void PostingAngsuranKolektif()
    {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("institutionCode", institutionCode);
            postparams.put("accountNumber", TabID.getText().toString());
            String totalAmount = amount.getText().toString().replaceAll("[$,.]", "");
            postparams.put("amount", totalAmount);

            Date date= Calendar.getInstance().getTime();
            DateFormat dateFormat=new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            dateTimePRM = dateFormat.format(date);

            postparams.put("dateTime", dateTimePRM);
            postparams.put("referenceNumber", dateTimePRM);
            postparams.put("terminalType", "MT");
            postparams.put("terminalId", currUser);
            postparams.put("autoDebetId", autoDebetId.getText().toString());
            postparams.put("destinationAccountNumber", NoRekeningTujuan.getText().toString());
            postparams.put("destinationAccountName", nama);
            postparams.put("userId", currUser);
            postparams.put("kolektorId", currKolektor);
            postparams.put("capemId", currCapem);
            postparams.put("HashCode", clsGenerateSHA.hex256(institutionCode.concat(TabID.getText().toString()).concat(totalAmount).concat(dateTimePRM).concat(dateTimePRM).concat("MT").concat(currUser).concat(autoDebetId.getText().toString()).concat(NoRekeningTujuan.getText().toString()).concat(nama).concat(currUser).concat(currKolektor).concat(currCapem).concat(hashKey),true));
            sendPostForAngsuranKolektif(urlAPI.concat("/postingAngsuranKolektif") , postparams);
        }
        catch (JSONException e)
        {
            dialog.dismiss();
            Toast.makeText(AngsuranKolektifActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForAngsuranKolektif(String url, JSONObject JSONBodyParam)  {
        try{

            RequestQueue requestQueue= Volley.newRequestQueue(this);
            JsonObjectRequest objectRequest=new JsonObjectRequest(Request.Method.POST, url, JSONBodyParam,
                    response -> {
                        //do something
                        String ResponseCode ;
                        String ResponseDescription;
                        String ReffNo;
                        try {
                            ResponseCode = response.getString("responseCode");
                            ResponseDescription = response.getString("responseDescription");
                            ReffNo=response.getString("referenceNumber");
                            if (ResponseCode.equalsIgnoreCase("00"))
                            {
                                //pindah ke jurnal transaksi
                                Intent intent;
                                intent = new Intent(AngsuranKolektifActivity.this, AngsuranKolektifPostedActivity.class);
                                intent.putExtra("INSTITUTIONCODE", institutionCode );
                                intent.putExtra("ACCOUNTNUMBER", TabID.getText().toString() );
                                intent.putExtra("TANGGAL", dateTimePRM );
                                intent.putExtra("REFERENCE", ReffNo );
                                intent.putExtra("FROM", "Angsuran Kolektif");
                                startActivity(intent);

                                kodeAutoDebet.setSelection(0,true);

                                dialog.dismiss();
                            }
                            else
                            {
                                dialog.dismiss();
                                new AlertDialog.Builder(AngsuranKolektifActivity.this)
                                        .setTitle("Status Pembayaran Angsuran Kolektif")
                                        .setMessage(ResponseDescription)
                                        .setCancelable(false)
                                        .setPositiveButton("Kembali", (dialog, which) -> finish())
                                        .show();
                            }

                        } catch (JSONException e) {
                            dialog.dismiss();
                            Toast.makeText(AngsuranKolektifActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            finish();
                        } catch (Exception e){
                            dialog.dismiss();
                            Toast.makeText(AngsuranKolektifActivity.this,e.toString() , Toast.LENGTH_LONG).show();
                            finish();
                        }
                    },
                    error -> {
                        dialog.dismiss();
                        new AlertDialog.Builder(AngsuranKolektifActivity.this)
                                .setTitle("Status Pembayaran Angsuran kolektif")
                                .setMessage("Session telah Habis, Mohon Login Kembali !!!")
                                .setCancelable(false)
                                .setPositiveButton("Kembali", (dialog, which) -> finish())
                                .show();
                        finish();

                    }
            )
            {
                @Override
                public Map<String, String> getHeaders()  {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    //headers.put("Authorization", "Bearer ".concat(savedData.getAccessToken(PostingAngsuranKolektifActivity.this)));

                    return headers;
                }
            };
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(AngsuranKolektifActivity.this,e.toString() , Toast.LENGTH_LONG).show();
            finish();
        }
    }


}