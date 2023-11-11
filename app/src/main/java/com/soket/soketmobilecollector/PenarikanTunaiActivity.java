package com.soket.soketmobilecollector;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PenarikanTunaiActivity extends AppCompatActivity {

    private TextView TabID;
    private TextInputEditText Nominal;
    private TextView txtNama;
    private TextView txtSaldo;
    private TextView txtLastTrans;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    private String currCapem;
    private String currKolektor;
    private String currUser;

    //for killed from posted
    public static PenarikanTunaiActivity penarikanTunaiActivity;

    private ProgressDialog dialog;

    private String setText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penarikan_tunai);

        //for killed from ouside
        penarikanTunaiActivity = this;

        TextView headerSetoranTunai =  findViewById(R.id.txtHeaderPenarikanTunai);
        ImageButton btnBack =  findViewById(R.id.imageButtonBackPenarikanTunai);
        ImageButton btnNext =  findViewById(R.id.imageButtonNextPenarikanTunai);

        TabID =  findViewById(R.id.textViewTabIDPenarikanTunai);
        txtNama = findViewById(R.id.textViewNamadiPenarikanTunai) ;
        txtSaldo = findViewById(R.id.textViewSaldodiPenarikanTunai) ;
        txtLastTrans = findViewById(R.id.textViewAlamatdiPenarikanTunai) ;
        ImageView logo =  findViewById(R.id.imageViewPenarikanTunai);

        Nominal =  findViewById(R.id.TextInputEditTextPenarikanTunai);

        Button btn5000 =  findViewById(R.id.button5000_PenarikanTunai);
        Button btn10000 =  findViewById(R.id.button10000_PenarikanTunai);
        Button btn20000 =  findViewById(R.id.button20000_PenarikanTunai);
        Button btn50000 =  findViewById(R.id.button50000_PenarikanTunai);
        Button btn75000 =  findViewById(R.id.button75000_PenarikanTunai);
        Button btn100000 =  findViewById(R.id.button100000_PenarikanTunai);
        Button btn200000 =  findViewById(R.id.button200000_PenarikanTunai);
        Button btn500000 =  findViewById(R.id.button500000_PenarikanTunai);
        Button btn750000 =  findViewById(R.id.button750000_PenarikanTunai);
        Button btn1000000 =  findViewById(R.id.button1000000_PenarikanTunai);

        TextView txtInstitutionNameSetoranTunai =  findViewById(R.id.textViewinstitutionNamePenarikanTunai);

        //cek apa keaddan login apa tidak
        //for saved data
        clsPreference currPreference = new clsPreference();
        currUser= currPreference.getLoggedInUser(this);
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);

        String newTabID;
        String nama;
        String saldo;
        String lastTrans;
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                newTabID = null;
                nama =null;
                saldo =null;
                lastTrans =null;
            } else {
                newTabID = extras.getString("TABID");
                nama = extras.getString("NAMA");
                saldo = extras.getString("SALDO");
                lastTrans = extras.getString("LASTTRANS");
            }
        }
        else
        {
            newTabID = (String) savedInstanceState.getSerializable("TABID");
            nama = (String) savedInstanceState.getSerializable("NAMA");
            saldo = (String) savedInstanceState.getSerializable("SALDO");
            lastTrans = (String) savedInstanceState.getSerializable("LASTTRANS");
        }

        if (currLoggedInStatus)
        {
            currCapem = currPreference.getRegisteredCapem(this);
            currKolektor = currPreference.getRegisteredKolektor(this);
            hashKey=getString(R.string.hashKey);
            urlAPI=getString(R.string.webService );
            institutionCode= currPreference.getRegisteredInstitutionCode(this);
            setText = "Asal Penarikan Tunai";
            headerSetoranTunai.setText(setText);
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

        }
        else
        {
            currPreference.clearLoggedInUser(this);
            finish();
        }

        btnBack.setOnClickListener(view -> finish());

        btnNext.setOnClickListener(view -> {
            dialog = ProgressDialog.show(PenarikanTunaiActivity.this, "Posting","Please wait...", true);
            postingSetoranTunai();
        });

        //--------------
        btn5000.setOnClickListener(view -> {

            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "5000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn10000.setOnClickListener(view -> {
            //Nominal.setText(formatedAmount("10000"));
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "10000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn20000.setOnClickListener(view -> {
            // Nominal.setText(formatedAmount("20000"));

            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "20000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn50000.setOnClickListener(view -> {
            //Nominal.setText(formatedAmount("50000"));
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "50000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn75000.setOnClickListener(view -> {
            //Nominal.setText(formatedAmount("75000"));
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "75000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn100000.setOnClickListener(view -> {
            //Nominal.setText("100000");
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "100000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn200000.setOnClickListener(view -> {
            //Nominal.setText(formatedAmount("200000"));
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "200000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });
        btn500000.setOnClickListener(view -> {
            //Nominal.setText(formatedAmount("500000"));
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "500000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });
        btn750000.setOnClickListener(view -> {
            //Nominal.setText(formatedAmount("750000"));
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "750000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });
        btn1000000.setOnClickListener(view -> {
            //Nominal.setText(formatedAmount("1000000"));
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "1000000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });
        //---------

        Nominal.addTextChangedListener(new TextWatcher() {
            int length_before = 0;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                length_before = s.length();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            private String current = "";
            @Override
            public void afterTextChanged(Editable s) {
                if (length_before < s.length()) {
                    if (!s.toString().equals(current)) {
                        Nominal.removeTextChangedListener(this);
                        String cleanString = s.toString().replaceAll("[$,.]", "");
                        double parsed = Double.parseDouble(cleanString);
                        String formatted = NumberFormat.getInstance().format((parsed ));
                        current = formatted;
                        Nominal.setText(formatted);
                        Nominal.setSelection(formatted.length());
                        Nominal.addTextChangedListener(this);
                    }
                }
            }
        });

    }

    private void postingSetoranTunai()  {
        try {
            if (TabID.getText().toString().length()>0)
            {
                JSONObject postparams = new JSONObject();
                postparams.put("InstitutionCode", institutionCode);
                postparams.put("TabID", TabID.getText().toString());
                postparams.put("debet", Objects.requireNonNull(Nominal.getText()).toString().replaceAll("[$,.]", ""));
                postparams.put("UserID", currUser);
                postparams.put("KolektorID", currKolektor);
                postparams.put("CapemID", currCapem);

                //clsGenerateSHA hash256 = new clsGenerateSHA();
                String GeneratedHashCode = clsGenerateSHA.hex256(institutionCode.concat(TabID.getText().toString()).concat(Nominal.getText().toString().replaceAll("[$,.]", "")).concat(currUser).concat(currKolektor).concat(currCapem).concat(hashKey),true);
                postparams.put("hashCode", GeneratedHashCode);

                sendPostForPostingTarikanTunai(urlAPI.concat("/tarikan") , postparams);

                dialog.setProgress(40);
            }
            else
            {
                Toast.makeText(PenarikanTunaiActivity.this,"Nomor Rekening Kosong, Mohon tekan Back untuk memasukan No rekening Tujuan !!!" , Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            Toast.makeText(PenarikanTunaiActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForPostingTarikanTunai(String url, JSONObject JSONBodyParam)  {
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
                            if (ResponseCode.equalsIgnoreCase("00")) {
                                Intent intent = new Intent(PenarikanTunaiActivity.this, PenarikanTunaiPostedActivity.class);
                                intent.putExtra("REFERENCE", response.getString("reference"));
                                intent.putExtra("RESHOW",false);
                                startActivity(intent);

                                TabID.setText("");
                                txtNama.setText("");
                                setText="Saldo : 0";
                                txtSaldo.setText(setText);
                                txtLastTrans.setText("");

                            } else {
                                Toast.makeText(PenarikanTunaiActivity.this,ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                            dialog.setProgress(100);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PenarikanTunaiActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            dialog.setProgress(100);
                        }
                        dialog.dismiss();
                    },
                    error -> {
                        Toast.makeText(PenarikanTunaiActivity.this,error.toString() , Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
            )
            {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    90000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(PenarikanTunaiActivity.this,e.toString() , Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
    }

    //for killed from other activity
    public static PenarikanTunaiActivity getInstance(){
        return   penarikanTunaiActivity;
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

    private String formatedAmount_From_Double(double amount)
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

    private String setNominal(String currentNominal, String addWithValue)
    {
        String cleanString = currentNominal.replaceAll("[$,.]", "");
        if (cleanString.length()<1) {cleanString="0";}
        double parsed = Double.parseDouble(cleanString) + Double.parseDouble(addWithValue);

        return formatedAmount_From_Double (parsed);
    }
}