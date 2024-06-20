package com.soket.soketmobilecollector;

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

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MutasiPinjamanConfirmationMainActivity extends AppCompatActivity {

    private EditText KreditID;
    private AutoCompleteTextView KreditID1;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;
    private String TabIDMask;

    //untuk isi autocompleteedittext
    private ArrayList<String> arrNasabah;
    private ArrayAdapter<String> adNasabah;
    private boolean IsUsingAutoCompleteID_Pinjaman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutasi_pinjaman_confirmation_main);

        TextView headerMutasiTabungan =  findViewById(R.id.txtHeaderMutasiSimpBulanan);
        ImageButton btnBack =  findViewById(R.id.imageButtonBackMutasiSimpanan);
        ImageButton btnNext =  findViewById(R.id.imageButtonNextSimpanan);
        KreditID =  findViewById(R.id.editTextNominalSetoranTunai);
        KreditID1=findViewById(R.id.autoCompleteTextViewMutasiPinjaman) ;

        //for saved data
        clsPreference currPreference = new clsPreference();
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);

        TabIDMask= currPreference.getIDMaskPinjaman(this) ;
        KreditID.setFilters(new InputFilter[] {new InputFilter.LengthFilter(TabIDMask.length())});

        if (currLoggedInStatus)
        {
            hashKey=getString(R.string.hashKey);
            urlAPI=getString(R.string.webService );
            institutionCode= currPreference.getRegisteredInstitutionCode(this);
            String setText = "Mutasi Pinjaman";
            headerMutasiTabungan.setText(setText);

            IsUsingAutoCompleteID_Pinjaman = currPreference.getIsUsingAutoCompleteID(this);
            if (IsUsingAutoCompleteID_Pinjaman)
            {
                KreditID.setVisibility(View.INVISIBLE);

                //isi autocompletetext
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

        btnBack.setOnClickListener(view -> finish());

        btnNext.setOnClickListener(view -> {

            if (IsUsingAutoCompleteID_Pinjaman)
            {
                //dapatkan tabid sepanjang tabid-length
                String temp="";
                String temp1=KreditID1.getText().toString();
                String tmpChar;
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

                KreditID.setText(temp);
            }
            getAccountInformation();
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
                    if (TabIDMask.length() > s.length())
                    {
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

    private void getAccountInformation()  {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("jenis", "kredit");
            postparams.put("txtNorek", KreditID.getText().toString());
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("kredit").concat(KreditID.getText().toString()).concat(hashKey),true));
            sendPostForValidateLogin(urlAPI.concat("/inforekening") , postparams);
        } catch (JSONException e) {
            Toast.makeText(MutasiPinjamanConfirmationMainActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForValidateLogin(String url, JSONObject JSONBodyParam)  {
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
                                if (Accounts.length()> 0)
                                {
                                    Account = Accounts.getJSONObject(0);
                                    Intent intent = new Intent(MutasiPinjamanConfirmationMainActivity.this, MutasiPinjamanActivity.class);
                                    intent.putExtra("KREDITID", Account.getString("kreditID"));
                                    intent.putExtra("NAMA", Account.getString("nama"));
                                    intent.putExtra("POKOK", Account.getString("pokokPinjaman"));
                                    intent.putExtra("JT", Account.getString("jt"));
                                    intent.putExtra("ANGSURAN", Account.getString("angsuran"));
                                    startActivity(intent);
                                    KreditID.setText("");
                                }
                                else
                                {
                                    Toast.makeText(MutasiPinjamanConfirmationMainActivity.this,"Data Pinjaman tidak ada !!!", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(MutasiPinjamanConfirmationMainActivity.this,ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MutasiPinjamanConfirmationMainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MutasiPinjamanConfirmationMainActivity.this,error.toString() , Toast.LENGTH_LONG).show()
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
            Toast.makeText(MutasiPinjamanConfirmationMainActivity.this,e.toString() , Toast.LENGTH_LONG).show();
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
            Toast.makeText(MutasiPinjamanConfirmationMainActivity.this,e.toString() , Toast.LENGTH_LONG).show();
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
                                    arrNasabah.add(Account.getString("kreditID").concat(" ").concat(Account.getString("nama")));
                                }
                                adNasabah.notifyDataSetChanged();
                            } else {
                                Toast.makeText(MutasiPinjamanConfirmationMainActivity.this,ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MutasiPinjamanConfirmationMainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MutasiPinjamanConfirmationMainActivity.this,error.toString() , Toast.LENGTH_LONG).show()
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
            Toast.makeText(MutasiPinjamanConfirmationMainActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

}