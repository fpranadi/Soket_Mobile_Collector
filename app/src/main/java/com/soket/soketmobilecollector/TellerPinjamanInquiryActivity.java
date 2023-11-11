package com.soket.soketmobilecollector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TellerPinjamanInquiryActivity extends AppCompatActivity {

    private EditText KreditID;
    private AutoCompleteTextView KreditID1;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;
    private String KreditIDMask;

    private boolean IsUsingAutoCompleteID_Pinjaman;

    private ArrayList<String> arrNasabah;
    private ArrayAdapter<String> adNasabah;

    private ProgressDialog dialog;

    private String NoPinjaman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_pinjaman_inquiry);

        TextView headerTellerPinjaman =  findViewById(R.id.txtHeader_TellerPinjaman);
        ImageButton btnBack =  findViewById(R.id.imageButtonBackInquiry_TellerPinjaman);
        ImageButton btnNext =  findViewById(R.id.imageButtonNextInquiry_TellerPinjaman);
        KreditID =  findViewById(R.id.editTextKreditID_TellerPinjaman);
        KreditID1 =findViewById(R.id.autoCompleteTextViewID_TellerPinjaman) ;
        ImageButton btnScanBarcode =  findViewById(R.id.imageButton_ScanBarcode_TellerPinjaman);

        //for saved data
        clsPreference currPreference = new clsPreference();
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);

        KreditIDMask= currPreference.getIDMaskPinjaman(this);
        KreditID.setFilters(new InputFilter[] {new InputFilter.LengthFilter(KreditIDMask.length())});

        if (currLoggedInStatus)
        {
            hashKey=getString(R.string.hashKey);
            urlAPI=getString(R.string.webService );
            institutionCode= currPreference.getRegisteredInstitutionCode(this);
            String setText=getString(R.string.TellerPinjaman);
            headerTellerPinjaman.setText(setText);

            IsUsingAutoCompleteID_Pinjaman = currPreference.getIsUsingAutoCompleteID(this);
            if (IsUsingAutoCompleteID_Pinjaman)
            {
                KreditID.setVisibility(View.INVISIBLE);
                arrNasabah = new ArrayList<>();
                adNasabah = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrNasabah);
                KreditID1.setAdapter(adNasabah);
                getNasabah();
            }
            else
            {
                KreditID1.setVisibility(View.INVISIBLE);
            }
        }
        else
        {
            currPreference.clearLoggedInUser(this);
            finish();
        }

        btnScanBarcode.setOnClickListener(view -> {
            IntentIntegrator intentIntegrator = new IntentIntegrator(TellerPinjamanInquiryActivity.this);
            intentIntegrator.setPrompt("For flash use volume up key");
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.setOrientationLocked(true);
            intentIntegrator.setCaptureActivity(Capture.class);
            intentIntegrator.initiateScan();

        });

        btnBack.setOnClickListener(view -> finish());

        btnNext.setOnClickListener(view -> {
            dialog = ProgressDialog.show(TellerPinjamanInquiryActivity.this, "Syncing","Please wait...", true, false);

            if (IsUsingAutoCompleteID_Pinjaman)
            {
                //dapatkan tabid sepanjang tabid-length
                String temp="";
                String temp1=KreditID1.getText().toString();
                String tmpChar;

                if (temp1.length() > KreditIDMask.length())
                {
                    int j=0;
                    boolean NotFound=true;
                    while ((j <KreditIDMask.length()) && NotFound)
                    {
                        tmpChar=temp1.substring(j,j+1);
                        if (!tmpChar.equals(" "))
                        {
                            temp=temp.concat(tmpChar);
                        }
                        else
                        {
                            NotFound=false;
                        }
                        j++;
                    }
                }
                else
                {
                    temp=temp1;
                }

                KreditID.setText(temp);
            }
            NoPinjaman=KreditID.getText().toString();
            getAngsuran();
        });

        KreditID.addTextChangedListener(new TextWatcher() {
            int length_before = 0;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                length_before = s.length();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (length_before < s.length()) {
                    if (KreditIDMask.length() > s.length())
                    {
                        //private String current = "";
                        String nextChar = KreditIDMask.substring(s.length(), s.length() + 1);
                        if (nextChar.equals("-") || nextChar.equals("/") || nextChar.equals(".") )
                        {
                            s.insert(s.length(), nextChar);
                        }
                    }

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult=  IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (intentResult.getContents()!=null)
        {
            dialog = ProgressDialog.show(TellerPinjamanInquiryActivity.this, "Syncing","Please wait...", true);
            NoPinjaman=intentResult.getContents();
            getAngsuran();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"OOps.. you did not scan anything", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAngsuran()  {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("jenis", "Kredit");
            postparams.put("txtNorek", NoPinjaman);
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("Kredit").concat(NoPinjaman).concat(hashKey),true));
            sendPostForGetAngsuran(urlAPI.concat("/angsuran") , postparams);
            dialog.setProgress(50);
        } catch (JSONException e) {
            Toast.makeText(TellerPinjamanInquiryActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForGetAngsuran(String url, JSONObject JSONBodyParam)  {
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
                                Intent intent = new Intent(TellerPinjamanInquiryActivity.this, TellerPinjamanActivity.class);
                                intent.putExtra("KreditNo", response.getString("KreditNo"));
                                intent.putExtra("Nama", response.getString("Nama"));
                                intent.putExtra("PokokPinjaman", response.getDouble("PokokPinjaman"));
                                intent.putExtra("Alamat", response.getString("Alamat"));
                                intent.putExtra("Realisasi", response.getString("Realisasi"));
                                intent.putExtra("JangkaWaktu", response.getInt("JangkaWaktu"));
                                intent.putExtra("Jenis", response.getString("Jenis"));
                                intent.putExtra("JatuhTempo", response.getString("JatuhTempo"));
                                intent.putExtra("Angsuran", response.getDouble("Angsuran"));
                                intent.putExtra("SisaPinjaman", response.getDouble("SisaPinjaman"));
                                intent.putExtra("PRD", response.getInt("PRD"));
                                intent.putExtra("Tgk", response.getInt("Tgk"));
                                intent.putExtra("TgkDenda", response.getInt("TgkDenda"));
                                intent.putExtra("TPokok", response.getDouble("TPokok"));
                                intent.putExtra("TBunga", response.getDouble("TBunga"));
                                intent.putExtra("Denda", response.getDouble("Denda"));
                                startActivity(intent);
                                KreditID.setText("");
                                KreditID1.setText("");
                            } else {
                                Toast.makeText(TellerPinjamanInquiryActivity.this,ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                            dialog.setProgress(100);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            dialog.setProgress(100);
                            Toast.makeText(TellerPinjamanInquiryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        dialog.dismiss();
                    },
                    error -> {
                        dialog.dismiss();
                        Toast.makeText(TellerPinjamanInquiryActivity.this,error.toString() , Toast.LENGTH_LONG).show();
                    }
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
                    90000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(objectRequest);
        }catch (Exception e){
            dialog.dismiss();
            Toast.makeText(TellerPinjamanInquiryActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void getNasabah()  {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("jenis", "kredit");
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("kredit").concat(hashKey),true));
            sendPostForGetNasabah(urlAPI.concat("/nasabahAll") , postparams);
        } catch (JSONException e) {
            Toast.makeText(TellerPinjamanInquiryActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForGetNasabah(String url, JSONObject JSONBodyParam)  {
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
                                // Getting JSON Array node
                                JSONArray Accounts = response.getJSONArray("hasil");
                                JSONObject Account;
                                for (int k = 0; k < Accounts.length(); k++)
                                {
                                    Account = Accounts.getJSONObject(k);
                                    arrNasabah.add(Account.getString("KreditID").concat(" ").concat(Account.getString("Nama")));
                                }
                                adNasabah.notifyDataSetChanged();
                            } else {
                                Toast.makeText(TellerPinjamanInquiryActivity.this,ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(TellerPinjamanInquiryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(TellerPinjamanInquiryActivity.this,error.toString() , Toast.LENGTH_LONG).show()
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
                    60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(TellerPinjamanInquiryActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }
}