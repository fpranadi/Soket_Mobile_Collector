package com.soket.soketmobilecollector;

import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private EditText e_intitutionCode;
    private Spinner kolektor;
    private Spinner capem;
    private ArrayList<String> arrCapem;
    private ArrayAdapter<String> adCapem;
    private ArrayList<String> arrKolektor;
    private ArrayAdapter<String> adKolektor;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    //for saved data
    private clsPreference savedData ;
    private String savedCapemID;
    private String savedKolektorID ;

    private EditText userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnSimpan =  findViewById(R.id.btnSimpan);
        Button btnCancel =  findViewById(R.id.btnCancel);
        capem= findViewById(R.id.spCapem);
        kolektor= findViewById(R.id.spKolektor);
        e_intitutionCode=findViewById(R.id.editTextInstitutionCode);
        userName = findViewById(R.id.editTextUserYangAkanLogin);
        TextInputEditText bt_Address = findViewById(R.id.txtBTAddress);
        TextInputEditText bt_Name = findViewById(R.id.txtBTName);
        Button testPrint = findViewById(R.id.btnTestPrint);

        //institutionCode=e_intitutionCode.getText().toString();
        hashKey=getString(R.string.hashKey);
        urlAPI=getString(R.string.webService );

        //getsavedData
        savedData = new clsPreference();
        savedCapemID = savedData.getRegisteredCapem(this);
        savedKolektorID =savedData.getRegisteredKolektor(this);
        institutionCode= savedData.getRegisteredInstitutionCode(this);
        e_intitutionCode.setText(institutionCode) ;
        bt_Name.setText(savedData.getBtDeviceName(this));
        bt_Address.setText(savedData.getBtDeviceAddress(this));
        userName.setText(savedData.getRegisteredUser(this));

        //isi spiner kolektor
        arrKolektor = new ArrayList<>();
        adKolektor = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrKolektor);
        kolektor.setAdapter(adKolektor);
        fillKolektor();

        //isi spiner capem
        arrCapem = new ArrayList<>();
        adCapem = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrCapem);
        capem.setAdapter(adCapem);
        fillCapem();

        btnSimpan.setOnClickListener(view -> {

            savedData.setRegisteredInstitution(SettingsActivity.this,e_intitutionCode.getText().toString(), "Koperasi Indonesia" ) ;

            if (institutionCode.equals( e_intitutionCode.getText().toString()))
            {
                boolean isCapemOk = false;
                boolean isKolektorOk=false;

                if (bt_Name.length()>0)
                {
                    savedData.setBTDevice(SettingsActivity.this,bt_Name.getText().toString(), bt_Address.getText().toString(),32);
                }
                if (capem.getSelectedItemPosition() >= 0 )
                {
                    savedData.setRegisteredCapem(SettingsActivity.this,capem.getSelectedItem().toString() );
                    isCapemOk=true;
                }
                if (kolektor.getSelectedItemPosition() >= 0)
                {
                    savedData.setRegisteredKolektor(SettingsActivity.this,kolektor.getSelectedItem().toString() );
                    isKolektorOk=true;
                }
                if (isCapemOk && isKolektorOk)
                {
                    saveIMEI();
                    savedData.clearLoggedInUser(SettingsActivity.this );
                    Toast.makeText(SettingsActivity.this, "Konfigurasi berhasil Tersimpan, buka kembali Aplikasi ini ...!!!", Toast.LENGTH_LONG).show();
                    finish();
                    MainActivity.getInstance().finish();
                }
            }
            else
            {
                institutionCode= e_intitutionCode.getText().toString();
                arrKolektor.clear();
                arrCapem.clear();
                adCapem.notifyDataSetChanged();
                adKolektor.notifyDataSetChanged();
                Toast.makeText(SettingsActivity.this, "Konfigurasi berhasil Tersimpan ... selanjutnya pastikan memilih Capem dak Kolektor !!", Toast.LENGTH_LONG).show();
                fillKolektor();
                fillCapem();
            }
        });

        btnCancel.setOnClickListener(v -> {
            savedData.clearLoggedInUser(SettingsActivity.this );
            finish();
        });

        testPrint.setOnClickListener(v -> {
            EscPosPrinter printer = null;
            try {
                printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
                printer.printFormattedText(
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logolpd, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
                                "[L]\n" +
                                "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
                                "[L]\n" +
                                "[C]================================\n" +
                                "[L]\n" +
                                "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
                                "[L]  + Size : S\n" +
                                "[L]\n" +
                                "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
                                "[L]  + Size : 57/58\n" +
                                "[L]\n" +
                                "[C]--------------------------------\n" +
                                "[R]TOTAL PRICE :[R]34.98e\n" +
                                "[R]TAX :[R]4.23e\n" +
                                "[L]\n" +
                                "[C]================================\n" +
                                "[L]\n" +
                                "[L]<font size='tall'>Customer :</font>\n" +
                                "[L]Raymond DUPONT\n" +
                                "[L]5 rue des girafes\n" +
                                "[L]31547 PERPETES\n" +
                                "[L]Tel : +33801201456\n" +
                                "[L]\n" +
                                "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
                                "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
                );
            } catch (EscPosConnectionException | EscPosEncodingException | EscPosBarcodeException | EscPosParserException e) {
                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                throw new RuntimeException(e);
            }
        });
    }

    private void fillCapem()
    {
        try {
            JSONObject Params = new JSONObject();
            Params.put("InstitutionCode", institutionCode);
            //clsGenerateSHA hash256 = new clsGenerateSHA();
            Params.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat(hashKey),true));
            sendPostForFillCapem(urlAPI.concat("/Capem") , Params);
        }
        catch (JSONException e)
        {
            //e.printStackTrace();
            Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForFillCapem(String url, JSONObject JSONBodyParam)  {
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
                                JSONArray Capems = response.getJSONArray("hasil");
                                JSONObject Capem;
                                for (int k = 0; k < Capems.length(); k++)
                                {
                                    Capem = Capems.getJSONObject(k);
                                    arrCapem.add(Capem.getString("capemID"));
                                }
                                adCapem.notifyDataSetChanged();
                                if (savedCapemID.length()>0) {capem.setSelection(((ArrayAdapter<String>)capem.getAdapter()).getPosition(savedCapemID));}
                            }
                            else
                            {
                                Toast.makeText(SettingsActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(SettingsActivity.this,error.toString() , Toast.LENGTH_LONG).show()
            )
            {
                @Override
                public Map<String, String> getHeaders()  {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer ".concat(savedData.getAccessToken(SettingsActivity.this)));
                    return headers;
                }
            };
            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(SettingsActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void fillKolektor()
    {
        try {
            JSONObject Params = new JSONObject();
            Params.put("InstitutionCode", institutionCode);
            //clsGenerateSHA hash256 = new clsGenerateSHA();
            Params.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat(hashKey),true));
            sendPostForFillKolektor(urlAPI.concat("/kolektor") , Params);
        }
        catch (JSONException e)
        {
            //e.printStackTrace();
            Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForFillKolektor(String url, JSONObject JSONBodyParam)  {
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
                                JSONArray Kolektors = response.getJSONArray("hasil");
                                JSONObject Kolektor;
                                for (int k = 0; k < Kolektors.length(); k++)
                                {
                                    Kolektor = Kolektors.getJSONObject(k);
                                    arrKolektor.add(Kolektor.getString("kolektorID"));
                                }
                                adKolektor.notifyDataSetChanged();
                                if (!savedKolektorID.isEmpty()) {kolektor.setSelection(((ArrayAdapter<String>)kolektor.getAdapter()).getPosition(savedKolektorID ));}
                            }
                            else
                            {
                                Toast.makeText(SettingsActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(SettingsActivity.this,error.toString() , Toast.LENGTH_LONG).show()
            )
            {
                @Override
                public Map<String, String> getHeaders()  {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer ".concat(savedData.getAccessToken(SettingsActivity.this)));

                    return headers;
                }
            };
            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(SettingsActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void saveIMEI()
    {
        try {
            JSONObject Params = new JSONObject();
            Params.put("institutionCode", institutionCode);
            Params.put("userName", userName.getText().toString());
            Params.put("referenceId", getAndroidId());
            Params.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat(userName.getText().toString()).concat(getAndroidId()).concat(hashKey),true));
            sendPostForSaveIMEI(urlAPI.concat("/saveuseryangakanlogin") , Params);
        }
        catch (JSONException e)
        {
            //e.printStackTrace();
            Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForSaveIMEI(String url, JSONObject JSONBodyParam)  {
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
                                savedData.setRegisteredUser(SettingsActivity.this, userName.getText().toString() );
                            }
                            else
                            {
                                Toast.makeText(SettingsActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(SettingsActivity.this,error.toString() , Toast.LENGTH_LONG).show()
            )
            {
                @Override
                public Map<String, String> getHeaders()  {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer ".concat(JWTUtils.generateToken(institutionCode, userName.getText().toString(), hashKey, getAndroidId())));
                    return headers;
                }
            };
            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(SettingsActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private String getAndroidId() {
        return Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}