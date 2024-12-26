package com.soket.soketmobilecollector;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class PenarikanTunaiInquiryActivity extends AppCompatActivity {
    private EditText TabID;
    private AutoCompleteTextView TabID1;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;
    private String TabIDMask;

    private boolean IsUsingAutoCompleteID_Penarikan;

    //untuk isi autocompleteedittext
    private ArrayList<String> arrNasabah;
    private ArrayAdapter<String> adNasabah;

    private ProgressDialog dialog;

    private String NoTabungan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penarikan_tunai_inquiry);

        TextView headerMutasiTabungan =  findViewById(R.id.txtHeaderPenarikanTunai);
        ImageButton btnBack =  findViewById(R.id.imageButtonBackInquiryPenarikanTunai);
        ImageButton btnNext =  findViewById(R.id.imageButtonNextInquiryPenarikanTunai);
        TabID =  findViewById(R.id.editTextTabIDPenarikanTunai);
        TabID1=findViewById(R.id.autoCompleteTextViewIDPenarikanTunai) ;
        ImageButton btnScanBarcode =  findViewById(R.id.imageButton_ScanBarcode_Penarikan);

        //cek apa keaddan login apa tidak
        //for saved data
        clsPreference currPreference = new clsPreference();
        //private String currCapem;
        //private String currKolektor;
        //String currUser = currPreference.getLoggedInUser(this);
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);

        TabIDMask = currPreference.getIDMaskSimpanan(this);
        TabID.setFilters(new InputFilter[] {new InputFilter.LengthFilter(TabIDMask.length())});

        if (currLoggedInStatus)
        {
            //currCapem = currPreference.getRegisteredCapem(this);
            //currKolektor =currPreference.getRegisteredKolektor(this);f
            hashKey=getString(R.string.hashKey);
            urlAPI=getString(R.string.webService );
            institutionCode= currPreference.getRegisteredInstitutionCode(this);
            String setText="Penarikan Tunai";
            headerMutasiTabungan.setText(setText);

            IsUsingAutoCompleteID_Penarikan = currPreference.getIsUsingAutoCompleteID(this);
            if (IsUsingAutoCompleteID_Penarikan)
            {
                TabID.setVisibility(View.INVISIBLE);
                //btnScanBarcode.setVisibility(View.INVISIBLE);
                //isi autocompletetext
                arrNasabah = new ArrayList<>();
                adNasabah = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrNasabah);
                TabID1.setAdapter(adNasabah);
                getNasabah();
            }
            else
            {
                TabID1.setVisibility(View.INVISIBLE);
            }
        }
        else
        {
            currPreference.clearLoggedInUser(this);
            finish();
        }

        btnScanBarcode.setOnClickListener(view -> {
            IntentIntegrator intentIntegrator = new IntentIntegrator(PenarikanTunaiInquiryActivity.this);
            intentIntegrator.setPrompt("For flash use volume up key");
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.setOrientationLocked(true);
            intentIntegrator.setCaptureActivity(Capture.class);
            intentIntegrator.initiateScan();

        });

        btnBack.setOnClickListener(view -> finish());

        btnNext.setOnClickListener(view -> {
            dialog = ProgressDialog.show(PenarikanTunaiInquiryActivity.this, "Syncing","Please wait...", true);

            if (IsUsingAutoCompleteID_Penarikan)
            {
                //dapatkan tabid sepanjang tabid-length
                String temp="";
                String temp1=TabID1.getText().toString();
                String tmpChar;
                if (temp1.length() > TabIDMask.length())
                {
                    int j=0;
                    boolean NotFound=true;
                    while ((j <TabIDMask.length()) && NotFound)
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
                TabID.setText(temp);
            }
            NoTabungan=TabID.getText().toString();
            getAccountInformation();
        });

        TabID.addTextChangedListener(new TextWatcher() {
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
                    if (TabIDMask.length() > s.length())
                    {
                        //private String current = "";
                        String nextChar = TabIDMask.substring(s.length(), s.length() + 1);
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
            dialog = ProgressDialog.show(PenarikanTunaiInquiryActivity.this, "Syncing","Please wait...", true);
            //TabID.setText(intentResult.getContents());
            NoTabungan=intentResult.getContents();
            getAccountInformation();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"OOps.. you did not scan anything", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAccountInformation()  {
        try {

            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("jenis", "Tabungan");
            //postparams.put("txtNorek", TabID.getText().toString());
            postparams.put("txtNorek", NoTabungan);
            //clsGenerateSHA hash256 = new clsGenerateSHA();
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("Tabungan").concat(NoTabungan).concat(hashKey),true));
            sendPostForValidateTabID(urlAPI.concat("/inforekening") , postparams);
            dialog.setProgress(50);
        } catch (JSONException e) {
            Toast.makeText(PenarikanTunaiInquiryActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForValidateTabID(String url, JSONObject JSONBodyParam)  {
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

                                // Getting JSON Array nodefp
                                JSONArray Accounts = response.getJSONArray("hasil");
                                JSONObject Account;
                                if (Accounts.length()> 0)
                                {
                                    Account = Accounts.getJSONObject(0);
                                    Intent intent = new Intent(PenarikanTunaiInquiryActivity.this, PenarikanTunaiActivity.class);
                                    intent.putExtra("TABID", Account.getString("tabID"));
                                    intent.putExtra("NAMA", Account.getString("nama"));
                                    intent.putExtra("SALDO", Account.getString("saldoTersedia"));
                                    intent.putExtra("ALAMAT", Account.getString("alamat"));
                                    intent.putExtra("LASTTRANS", Account.getString("lastTrans"));
                                    startActivity(intent);
                                    TabID.setText("");
                                    TabID1.setText("");

                                }
                                else
                                {
                                    Toast.makeText(PenarikanTunaiInquiryActivity.this,"Data Tabungan tidak ada !!!", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(PenarikanTunaiInquiryActivity.this,ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                            dialog.setProgress(100);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            dialog.setProgress(100);
                            Toast.makeText(PenarikanTunaiInquiryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        dialog.dismiss();
                    },
                    error -> {
                        dialog.dismiss();
                        Toast.makeText(PenarikanTunaiInquiryActivity.this,error.toString() , Toast.LENGTH_LONG).show();
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
            Toast.makeText(PenarikanTunaiInquiryActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void getNasabah()  {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("jenis", "Tabungan");
            //clsGenerateSHA hash256 = new clsGenerateSHA();
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("Tabungan").concat(hashKey),true));
            sendPostForGetNasabah(urlAPI.concat("/nasabahAll") , postparams);
        } catch (JSONException e) {
            Toast.makeText(PenarikanTunaiInquiryActivity.this,e.toString() , Toast.LENGTH_LONG).show();
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
                                    arrNasabah.add(Account.getString("tabID").concat(" ").concat(Account.getString("nama")));
                                }
                                adNasabah.notifyDataSetChanged();
                            } else {
                                Toast.makeText(PenarikanTunaiInquiryActivity.this,ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Toast.makeText(PenarikanTunaiInquiryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(PenarikanTunaiInquiryActivity.this,error.toString() , Toast.LENGTH_LONG).show()
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
                    60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(PenarikanTunaiInquiryActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

}