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

public class MutasiDepositoConfirmationActivity extends AppCompatActivity {

    private EditText TabID;
    private AutoCompleteTextView TabID1;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;
    private String TabIDMask;

    //untuk isi autocompleteedittext
    private ArrayList<String> arrNasabah ;
    private ArrayAdapter<String> adNasabah;
    private boolean IsUsingAutoCompleteID_Deposito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutasi_deposito_confirmation);

        TextView headerMutasiTabungan =  findViewById(R.id.txtHeaderMutasiSimpBulanan);
        ImageButton btnBack =  findViewById(R.id.imageButtonBackMutasiSimpanan);
        ImageButton btnNext =  findViewById(R.id.imageButtonNextSimpanan);
        TabID =  findViewById(R.id.editTextNominalSetoranTunai);
        TabID1 =  findViewById(R.id.autoCompleteTextViewMutasiDeposito);

        //cek apa keaddan login apa tidak
        //for saved data
        clsPreference currPreference = new clsPreference();

        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);

        TabIDMask = currPreference.getIDMaskSimpananBerjangka(this);
        TabID.setFilters(new InputFilter[]{new InputFilter.LengthFilter(TabIDMask.length())});


        if (currLoggedInStatus) {
            //currCapem = currPreference.getRegisteredCapem(this);
            //currKolektor =currPreference.getRegisteredKolektor(this);f
            hashKey = getString(R.string.hashKey);
            urlAPI = getString(R.string.webService);
            institutionCode = currPreference.getRegisteredInstitutionCode(this);
            String setText="Mutasi Simpanan Berjangka";
            headerMutasiTabungan.setText(setText);

            IsUsingAutoCompleteID_Deposito = currPreference.getIsUsingAutoCompleteID(this);
            if (IsUsingAutoCompleteID_Deposito) {
                TabID.setVisibility(View.INVISIBLE);

                //isi autocompletetext
                arrNasabah = new ArrayList<>();
                adNasabah = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrNasabah);
                TabID1.setAdapter(adNasabah);
                getNasabah();
            } else {
                TabID1.setVisibility(View.INVISIBLE);
            }
        } else {
            currPreference.clearLoggedInUser(this);
            finish();
        }

        btnBack.setOnClickListener(view -> finish());

        btnNext.setOnClickListener(view -> {
            if (IsUsingAutoCompleteID_Deposito)
            {
                //dapatkan tabid sepanjang tabid-length
                String temp="";
                String temp1=TabID1.getText().toString();
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

                TabID.setText(temp);
            }

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
                    if (TabIDMask.length() > s.length()) {
                        String nextChar = TabIDMask.substring(s.length(), s.length() + 1);
                        if (nextChar.equals("-") || nextChar.equals("/") || nextChar.equals(".")) {
                            s.insert(s.length(), nextChar);
                        }
                    }

                }
            }
        });


    }

    private void getAccountInformation() {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("jenis", "deposito");
            postparams.put("txtNorek", TabID.getText().toString());
            //clsGenerateSHA hash256 = new clsGenerateSHA();
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("deposito").concat(TabID.getText().toString()).concat(hashKey), true));
            sendPostForValidateLogin(urlAPI.concat("/inforekening"), postparams);
        } catch (JSONException e) {
            Toast.makeText(MutasiDepositoConfirmationActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForValidateLogin(String url, JSONObject JSONBodyParam) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, JSONBodyParam,
                    response -> {
                        //do something
                        String ResponseCode;
                        String ResponseDescription;
                        try {
                            ResponseCode = response.getString("responseCode");
                            ResponseDescription = response.getString("responseDescription");
                            if (ResponseCode.equalsIgnoreCase("00")) {

                                // Getting JSON Array node
                                JSONArray Accounts = response.getJSONArray("hasil");
                                JSONObject Account;
                                if (Accounts.length() > 0) {
                                    Account = Accounts.getJSONObject(0);
                                    Intent intent = new Intent(MutasiDepositoConfirmationActivity.this, MutasiDepositoActivity.class);
                                    intent.putExtra("TABID", Account.getString("TabID"));
                                    intent.putExtra("NAMA", Account.getString("Nama"));
                                    intent.putExtra("TGLMASUK", Account.getString("TglMasuk"));
                                    intent.putExtra("JW", Account.getString("JW"));
                                    startActivity(intent);
                                    TabID.setText("");
                                } else {
                                    Toast.makeText(MutasiDepositoConfirmationActivity.this, "Data Deposito tidak ada !!!", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(MutasiDepositoConfirmationActivity.this, ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MutasiDepositoConfirmationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MutasiDepositoConfirmationActivity.this, error.toString(), Toast.LENGTH_LONG).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            requestQueue.add(objectRequest);
        } catch (Exception e) {
            Toast.makeText(MutasiDepositoConfirmationActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void getNasabah() {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("jenis", "deposito");
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat("deposito").concat(hashKey), true));
            sendPostForGetNasabah(urlAPI.concat("/nasabahAll"), postparams);
        } catch (JSONException e) {
            Toast.makeText(MutasiDepositoConfirmationActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForGetNasabah(String url, JSONObject JSONBodyParam) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, JSONBodyParam,
                    response -> {
                        //do something
                        String ResponseCode;
                        String ResponseDescription;
                        try {
                            ResponseCode = response.getString("responseCode");
                            ResponseDescription = response.getString("responseDescription");
                            if (ResponseCode.equalsIgnoreCase("00")) {

                                // Getting JSON Array node
                                JSONArray Accounts = response.getJSONArray("hasil");
                                JSONObject Account;
                                for (int k = 0; k < Accounts.length(); k++) {
                                    Account = Accounts.getJSONObject(k);
                                    arrNasabah.add(Account.getString("TabID").concat(" ").concat(Account.getString("Nama")));
                                }
                                adNasabah.notifyDataSetChanged();
                            } else {
                                Toast.makeText(MutasiDepositoConfirmationActivity.this, ResponseDescription, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MutasiDepositoConfirmationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MutasiDepositoConfirmationActivity.this, error.toString(), Toast.LENGTH_LONG).show()
            ) {
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
        } catch (Exception e) {
            Toast.makeText(MutasiDepositoConfirmationActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}