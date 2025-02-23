package com.soket.soketmobilecollector;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

public class TellerPinjamanActivity extends AppCompatActivity {

    private String kreditNo;
    private int pRD;
    private int tgk;
    private int tgkDenda;
    private Double tPokok;
    private Double tBunga;
    private Double denda;

    private TextView KreditID;
    private TextInputEditText Nominal;
    private TextView txtNama;
    private TextView txtPokokPinjaman;
    private TextView txtJT;
    private TextView txtAngsuran;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    private String currCapem;
    private String currKolektor;
    private String currUser;

    //for killed from posted
    public static TellerPinjamanActivity tellerPinjamanActivity;

    private ProgressDialog dialog;

    private clsPreference currPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_pinjaman);

        //for killed from ouside
        tellerPinjamanActivity = this;

        ImageButton btnBack =  findViewById(R.id.imageButtonBack_TellerPinjaman);
        ImageButton btnNext =  findViewById(R.id.imageButtonNext_TellerPinjaman);

        KreditID =  findViewById(R.id.textViewKreditID_TellerPinjaman);
        txtNama = findViewById(R.id.textViewNamadi_TellerPinjaman) ;
        txtPokokPinjaman = findViewById(R.id.textViewPokokPinjaman_TellerPinjaman) ;
        txtJT = findViewById(R.id.textViewJT_TellerPinjaman) ;
        txtAngsuran = findViewById(R.id.textViewAngsuran_TellerPinjaman);
        ImageView logo =  findViewById(R.id.imageView_TellerPinjaman);
        TextView txtTgkPokok = findViewById(R.id.textView_Pokok_TellerPinjaman);
        TextView txtTgkBunga = findViewById(R.id.textView_Bunga_TellerPinjaman);
        TextView txtTgkDenda = findViewById(R.id.textView_Denda_TellerPinjaman);
        TextView txtTgkTotal = findViewById(R.id.textView_TotalTunggakan_TellerPinjaman);
        TextView txtSisaPinjaman = findViewById(R.id.textViewSisaPinjaman_TellerPinjaman);

        Nominal =  findViewById(R.id.TextInputEditText_TellerPinjaman);

        Button btn5000 =  findViewById(R.id.button5000_TellerPinjaman);
        Button btn10000 =  findViewById(R.id.button10000_TellerPinjaman);
        Button btn20000 =  findViewById(R.id.button20000_TellerPinjaman);
        Button btn50000 =  findViewById(R.id.button50000_TellerPinjaman);
        Button btn75000 =  findViewById(R.id.button75000_TellerPinjaman);
        Button btn100000 =  findViewById(R.id.button100000_TellerPinjaman);
        Button btn200000 =  findViewById(R.id.button200000_TellerPinjaman);
        Button btn500000 =  findViewById(R.id.button500000_TellerPinjaman);
        Button btn750000 =  findViewById(R.id.button750000_TellerPinjaman);
        Button btn1000000 =  findViewById(R.id.button1000000_TellerPinjaman);

        TextView txtInstitutionNameSetoranTunai =  findViewById(R.id.textViewinstitutionName_TellerPinjaman);

        currPreference = new clsPreference();
        currUser= currPreference.getLoggedInUser(this);
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);

        String nama;
        Double pokokPinjaman;
        //String alamat;
        //String realisasi;
        //int jangkaWaktu;
        //String jenis;
        String jatuhTempo;
        Double angsuran;
        Double sisaPinjaman;
        try {
            if (savedInstanceState == null)
            {
                Bundle extras = getIntent().getExtras();
                if (extras == null) {
                    kreditNo=null;
                    nama =null;
                    pokokPinjaman =0.0;
                    //alamat =null;
                    //realisasi =null;
                    //jangkaWaktu =0;
                    //jenis =null;
                    jatuhTempo =null;
                    angsuran =0.0;
                    sisaPinjaman =0.0;
                    pRD=0;
                    tgk=0;
                    tgkDenda=0;
                    tPokok=0.0;
                    tBunga=0.0;
                    denda=0.0;
                } else {
                    kreditNo= extras.getString("KreditNo");
                    nama = extras.getString("Nama");
                    pokokPinjaman = extras.getDouble("PokokPinjaman");
                    //alamat = extras.getString("Alamat");
                    //realisasi = extras.getString("Realisasi");
                    //jangkaWaktu = extras.getInt("JangkaWaktu");
                    //jenis = extras.getString("Jenis");
                    jatuhTempo = extras.getString("JatuhTempo");
                    angsuran = extras.getDouble("Angsuran");
                    sisaPinjaman = extras.getDouble("SisaPinjaman");
                    pRD= extras.getInt("PRD");
                    tgk= extras.getInt("Tgk");
                    tgkDenda= extras.getInt("TgkDenda");
                    tPokok= extras.getDouble("TPokok");
                    tBunga= extras.getDouble("TBunga");
                    denda= extras.getDouble("Denda");

                }
            }
            else
            {
                kreditNo= (String) savedInstanceState.getSerializable("KreditNo");
                nama = (String) savedInstanceState.getSerializable("Nama");
                pokokPinjaman = (Double) savedInstanceState.getSerializable("PokokPinjaman");
                //alamat = (String) savedInstanceState.getSerializable("Alamat");
                //realisasi = (String) savedInstanceState.getSerializable("Realisasi");
                //jangkaWaktu = (int) savedInstanceState.getSerializable("JangkaWaktu");
                //jenis = (String) savedInstanceState.getSerializable("Jenis");
                jatuhTempo = (String) savedInstanceState.getSerializable("JatuhTempo");
                angsuran = (Double) savedInstanceState.getSerializable("Angsuran");
                sisaPinjaman = (Double) savedInstanceState.getSerializable("SisaPinjaman");
                pRD= (int) savedInstanceState.getSerializable("PRD");
                tgk= (int) savedInstanceState.getSerializable("Tgk");
                tgkDenda= (int) savedInstanceState.getSerializable("TgkDenda");
                tPokok= (Double) savedInstanceState.getSerializable("TPokok");
                tBunga= (Double) savedInstanceState.getSerializable("TBunga");
                denda= (Double) savedInstanceState.getSerializable("Denda");
            }
        }
        catch (Exception err)
        {
            kreditNo=null;
            nama =null;
            pokokPinjaman =0.0;
            //alamat =null;
            //realisasi =null;
            //jangkaWaktu =0;
            //jenis =null;
            jatuhTempo =null;
            angsuran =0.0;
            sisaPinjaman =0.0;
            pRD=0;
            tgk=0;
            tgkDenda=0;
            tPokok=0.0;
            tBunga=0.0;
            denda=0.0;
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

            //masih salah
            KreditID.setText(kreditNo);
            txtNama.setText(nama);

            txtPokokPinjaman.setText(getString(R.string.PokokPinjaman).concat(formatedAmount_From_Double(pokokPinjaman)));
            txtJT.setText(getString(R.string.JatuhTempo).concat(jatuhTempo));
            txtAngsuran.setText(getString(R.string.Angsuran).concat(formatedAmount_From_Double(angsuran)));
            txtTgkPokok.setText((formatedAmount_From_Double(tPokok)));
            txtTgkBunga.setText((formatedAmount_From_Double(tBunga)));
            txtTgkDenda.setText((formatedAmount_From_Double(denda)));
            txtTgkTotal.setText(getString(R.string.TotalTunggakan).concat(formatedAmount_From_Double(tPokok+tBunga+denda)));
            txtSisaPinjaman.setText(getString(R.string.SisaPinjaman).concat(formatedAmount_From_Double(sisaPinjaman)));
        }
        else
        {
            currPreference.clearLoggedInUser(this);
            finish();
        }

        btnBack.setOnClickListener(view -> finish());

        btnNext.setOnClickListener(view -> {
            //hitung rincian tunggakan terlebih darhulu
            double bayar = Double.parseDouble(Objects.requireNonNull(Nominal.getText()).toString().replaceAll("[$,.]", ""));
            double by_Pokok=0;
            double by_Bunga=0;
            double by_denda=0;
            int by_Tgk=0;
            if (bayar >= tBunga+denda)
            {
                by_denda=denda;
                by_Bunga=tBunga;
                by_Pokok= bayar - by_Bunga - by_denda;
                by_Tgk=tgk;
            }
            else
            {
                double bunga1x = tPokok / tgk;
                double denda1x = denda / tgkDenda;
                if (bayar >= bunga1x)
                {
                    while ((bayar >= bunga1x ) && (bayar >=0))
                    {
                        by_Bunga = by_Bunga + bunga1x;
                        by_Tgk = by_Tgk + 1;
                        bayar = bayar - bunga1x;

                        if (bayar >= denda1x)
                        {
                            by_denda = by_denda + denda1x;
                            bayar = bayar - denda1x;
                        }
                        else
                        {
                            by_denda = by_denda + bayar;
                            bayar = 0;
                        }
                    }
                }
                else
                {
                    by_Tgk = 0;
                    by_Bunga=0;
                    by_denda=0;
                    Toast.makeText(TellerPinjamanActivity.this,"Pembayaran tunggakan, tidak mencukupi ..!!" , Toast.LENGTH_LONG).show();
                    bayar=0;
                }
                if (bayar > 0) {by_Pokok = bayar;}
            }
            if ((by_Pokok + by_Bunga + by_denda) > 0 )
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(TellerPinjamanActivity.this);
                builder.setTitle("Konfirmasi Rincian Pembayaran ");
                builder.setMessage("Tunggakan terbayar: " + formatedAmount_From_Double(by_Tgk) + "\n Pokok: " + formatedAmount_From_Double(by_Pokok) + "\n Bunga: " + formatedAmount_From_Double(by_Bunga) + "\n Denda: " + formatedAmount_From_Double(by_denda) + "\n Jika benar, lakukan Posting !...");
                double finalBy_Pokok = by_Pokok;
                int finalBy_Tgk = by_Tgk;
                double finalBy_Bunga = by_Bunga;
                double finalBy_denda = by_denda;
                builder.setPositiveButton("Posting", (dialog, which) -> {
                    dialog.dismiss();
                    postingSetoranKredit(finalBy_Tgk, finalBy_Pokok, finalBy_Bunga, finalBy_denda);
                });
                builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
                builder.show();
            }
            else
            {
                Toast.makeText(TellerPinjamanActivity.this,"Pembayaran tunggakan, tidak mencukupi ..!!" , Toast.LENGTH_LONG).show();
            }
        });

        //--------------
        btn5000.setOnClickListener(view -> {
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "5000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn10000.setOnClickListener(view -> {
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "10000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn20000.setOnClickListener(view -> {
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "20000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn50000.setOnClickListener(view -> {
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "50000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn75000.setOnClickListener(view -> {
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "75000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn100000.setOnClickListener(view -> {
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "100000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });

        btn200000.setOnClickListener(view -> {
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "200000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });
        btn500000.setOnClickListener(view -> {
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "500000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });
        btn750000.setOnClickListener(view -> {
            Nominal.setText(setNominal(Objects.requireNonNull(Nominal.getText()).toString(), "750000"));
            Nominal.setSelection(Nominal.getText().toString().length());
        });
        btn1000000.setOnClickListener(view -> {
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

    //for killed from other activity
    public static TellerPinjamanActivity getInstance(){
        return   tellerPinjamanActivity;
    }

    private void postingSetoranKredit(int byTgk, double byPokok, double byBunga, double denda)  {
        try {
            if (KreditID.getText().toString().length()>0)
            {
                dialog = ProgressDialog.show(TellerPinjamanActivity.this, "Posting","Mohon Tunggu...", true);

                JSONObject postparams = new JSONObject();
                postparams.put("institutionCode", institutionCode);
                postparams.put("KreditID", kreditNo);
                postparams.put("Pokok", Math.round(byPokok));
                postparams.put("Bunga", Math.round(byBunga));
                postparams.put("Denda", Math.round(denda));
                postparams.put("PRD", pRD + byTgk);
                postparams.put("UserID", currUser);
                postparams.put("KolektorID", currKolektor);
                postparams.put("CapemID", currCapem);
                postparams.put("Reference", "00");

                String hCode = institutionCode.concat(kreditNo);
                hCode = hCode +  Math.round(byPokok) + Math.round(byBunga) + Math.round(denda)  + Math.round(pRD + byTgk);
                hCode = hCode.concat(currUser).concat(currKolektor).concat(currCapem).concat("00");
                hCode = hCode.concat(hashKey);
                String GeneratedHashCode = clsGenerateSHA.hex256(hCode,true);
                postparams.put("hashCode", GeneratedHashCode);

                sendPostForPostingBayarAngsuran(urlAPI.concat("/setorankredit") , postparams);

                dialog.setProgress(40);
            }
            else
            {
                Toast.makeText(TellerPinjamanActivity.this,"Nomor Pinjaman Kosong, Mohon tekan Back untuk memasukan No rekening Pinjaman tujuan !!!" , Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            Toast.makeText(TellerPinjamanActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForPostingBayarAngsuran(String url, JSONObject JSONBodyParam)  {
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
                                Intent intent = new Intent(TellerPinjamanActivity.this, TellerPinjamanPostedActivity.class);
                                intent.putExtra("REFERENCE", response.getString("reference"));
                                intent.putExtra("RESHOW",false);
                                startActivity(intent);

                                KreditID.setText("");
                                txtNama.setText("");
                                txtPokokPinjaman.setText("");
                                txtJT.setText("");
                                txtAngsuran.setText("");
                            } else {
                                Toast.makeText(TellerPinjamanActivity.this,ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                            dialog.setProgress(100);
                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Toast.makeText(TellerPinjamanActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            dialog.setProgress(100);
                        }
                        dialog.dismiss();
                    },
                    error -> {
                        Toast.makeText(TellerPinjamanActivity.this,error.toString() , Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
            )
            {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer ".concat(currPreference.getAccessToken(TellerPinjamanActivity.this)));

                    return headers;
                }
            };
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    90000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(TellerPinjamanActivity.this,e.toString() , Toast.LENGTH_LONG).show();
            dialog.dismiss();
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