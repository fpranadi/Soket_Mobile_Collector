package com.soket.soketmobilecollector;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity {
    private EditText userName;
    private EditText userPassword;
    private Button btnLogin;
    private TextView institutionName;
    private ImageView logo;


    private int counter;
    private Spinner kolektor;
    private Spinner capem;
    private ArrayList<String> arrCapem ;
    private ArrayAdapter<String> adCapem;
    private ArrayList<String> arrKolektor;
    private ArrayAdapter<String> adKolektor;

    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    private Boolean IsUsingSetoranTunai ;
    private Boolean IsUsingPenarikanTunai ;
    private Boolean IsUsingMutasiSimpanan ;
    private Boolean IsUsingMutasiPinjaman ;
    private Boolean IsUsingMutasiSimpananBulanan;
    private Boolean IsUsingMutasiSimpananBerjangka;
    private Boolean IsUsingTellerPinjaman;

    public Boolean IsUsingAutoCompleteID;

    private String IDMaskSimpanan ;
    private String IDMaskPinjaman ;
    private String IDMaskSimpananBulanan ;
    private String IDMaskSimpananBerjangka ;

    //for saved data
    private clsPreference savedData ;
    private String savedCapemID;
    private String savedKolektorID ;

    //for killed from posted
    public static MainActivity mainActivity;

    //inputdialog
    private String m_Text ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //for killed from ouside
        mainActivity = this;

        counter=5;

        userName =findViewById(R.id.editTextTextPersonName );
        userPassword=findViewById(R.id.editTextTextPassword );
        btnLogin=findViewById(R.id.btnLogin);
        capem= findViewById(R.id.spCapem);
        kolektor= findViewById(R.id.spKolektor);
        ImageButton imgSetting =  findViewById(R.id.imageButtonSetting);
        institutionName= findViewById(R.id.textViewInstitutionName);
        logo=findViewById(R.id.imageViewLogo);

        //getsavedData
        savedData = new clsPreference();
        savedCapemID = savedData.getRegisteredCapem(this);
        savedKolektorID =savedData.getRegisteredKolektor(this);
        savedData.clearLoggedInUser(this);

        institutionCode= savedData.getRegisteredInstitutionCode(this);

        hashKey=getString(R.string.hashKey);
        urlAPI=getString(R.string.webService );

        //get institution name
        GetInstitutionName();

        //isi spiner kolekto
        arrKolektor = new ArrayList<>();
        adKolektor = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrKolektor);
        kolektor.setAdapter(adKolektor);
        fillKolektor();

        //isi spiner capem
        arrCapem = new ArrayList<>();
        adCapem = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrCapem);
        capem.setAdapter(adCapem);
        fillCapem();

        //setfocus di login
        userName.requestFocus();

        btnLogin.setOnClickListener(view -> validateLogin(userName.getText().toString(), userPassword.getText().toString()));

        imgSetting.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Input PIN");
            // Set up the input
            final EditText input = new EditText(MainActivity.this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            builder.setView(input);
            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
                m_Text = input.getText().toString();
                if (m_Text.equals("1976"))
                {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Invalid PIN" , Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void validateLogin(String pUserName, String pPassword)  {
        try {
            boolean isDoingLogin=true;

            // ini khusus untuk baru install langsung set ini kalau usernya fpranadi dan password : cemapanj << biar tidak di reject di google play
            if (!institutionCode.equals("000000"))
            {
                savedData.setRegisteredCapem(this,capem.getSelectedItem().toString() );
                savedData.setRegisteredKolektor(this,kolektor.getSelectedItem().toString() );
            }
            else
            {
                if( pUserName.equals("fpranadi") && pPassword.equals("cemapanj"))
                {
                    institutionCode="989006";
                    savedData.setRegisteredInstitution(this, institutionCode,"KSP Saduarsa");
                    savedData.setRegisteredCapem(this,"KANTOR" );
                    savedData.setRegisteredKolektor(this,"FP" );

                    savedCapemID = savedData.getRegisteredCapem(this);
                    savedKolektorID =savedData.getRegisteredKolektor(this);

                    savedData.clearLoggedInUser(this);

                    GetInstitutionName();
                }
                else
                {
                    isDoingLogin=false;
                }
            }

            if (isDoingLogin)
            {
                JSONObject postparams = new JSONObject();
                postparams.put("InstitutionCode", institutionCode);
                postparams.put("txtUserName", pUserName);
                postparams.put("txtPassword", pPassword);
                postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat(pUserName).concat(pPassword).concat(hashKey),true));
                sendPostForValidateLogin(urlAPI.concat("/login") , postparams);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Login Failed ..." , Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(MainActivity.this,e.toString() , Toast.LENGTH_LONG).show();
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
                                savedData.setLoggedInUser(MainActivity.this,userName.getText().toString() );
                                savedData.setLoggedInStatus(MainActivity.this,true );
                                userName.setText("");
                                userPassword.setText("");

                                savedData.setIsUsing(MainActivity.this,IsUsingSetoranTunai, IsUsingPenarikanTunai, IsUsingMutasiSimpanan, IsUsingMutasiPinjaman, IsUsingMutasiSimpananBulanan, IsUsingMutasiSimpananBerjangka, IsUsingTellerPinjaman);
                                savedData.setIDMask(MainActivity.this,IDMaskSimpanan, IDMaskPinjaman, IDMaskSimpananBulanan,IDMaskSimpananBerjangka );
                                savedData.setIsUsingAutoCompleteID(MainActivity.this,IsUsingAutoCompleteID);

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                            } else {
                                counter--;
                                if (counter == 0) {
                                    Toast.makeText(MainActivity.this,"No of Attempts remaining: ".concat(Integer.toString(counter )).concat(ResponseDescription), Toast.LENGTH_LONG).show();
                                    btnLogin.setEnabled(false);
                                    savedData.clearLoggedInUser(MainActivity.this);
                                }
                                else
                                {
                                    savedData.setLoggedInStatus(MainActivity.this,false);
                                    Toast.makeText(MainActivity.this,ResponseDescription, Toast.LENGTH_LONG).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            savedData.clearLoggedInUser(MainActivity.this);
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        Toast.makeText(MainActivity.this,error.toString() , Toast.LENGTH_LONG).show();
                        savedData.clearLoggedInUser(MainActivity.this);
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
            requestQueue.add(objectRequest);
        }catch (Exception e){
            savedData.clearLoggedInUser(MainActivity.this);
            Toast.makeText(MainActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void fillCapem()
    {
        try {
            JSONObject Params = new JSONObject();
            Params.put("InstitutionCode", institutionCode);
            Params.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat(hashKey),true));
            sendPostForFillCapem(urlAPI.concat("/Capem") , Params);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                                capem.setSelection(((ArrayAdapter<String>)capem.getAdapter()).getPosition(savedCapemID));
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MainActivity.this,error.toString() , Toast.LENGTH_LONG).show()
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
            Toast.makeText(MainActivity.this,e.toString() , Toast.LENGTH_LONG).show();
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
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                                kolektor.setSelection(((ArrayAdapter<String>)kolektor.getAdapter()).getPosition(savedKolektorID ));
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MainActivity.this,error.toString() , Toast.LENGTH_LONG).show()
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
            Toast.makeText(MainActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    private void GetInstitutionName()
    {
        try {
            JSONObject Params = new JSONObject();
            Params.put("InstitutionCode", institutionCode);
            Params.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat(hashKey),true));
            sendPostForGetInstitutionName(urlAPI.concat("/institution") , Params);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForGetInstitutionName(String url, JSONObject JSONBodyParam)  {
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
                                institutionName.setText( response.getString("institutionName"));
                                if (institutionName.getText().toString().toUpperCase().contains("KOPERASI") || institutionName.getText().toString().toUpperCase().contains("KSP") || institutionName.getText().toString().toUpperCase().contains("KPN") || institutionName.getText().toString().toUpperCase().contains("KSU"))
                                {
                                    logo.setImageResource(R.drawable.mylogo_small);
                                }
                                else
                                {
                                    logo.setImageResource(R.drawable.logolpd);
                                }

                                IsUsingSetoranTunai = Boolean.parseBoolean(response.getString("isUsingSetoranTunai"));
                                IsUsingPenarikanTunai = Boolean.parseBoolean(response.getString("isUsingPenarikanTunai"));
                                IsUsingMutasiSimpanan = Boolean.parseBoolean(response.getString("isUsingMutasiSimpanan"));
                                IsUsingMutasiPinjaman = Boolean.parseBoolean(response.getString("isUsingMutasiPinjaman"));
                                IsUsingMutasiSimpananBulanan = Boolean.parseBoolean(response.getString("isUsingMutasiSimpananBulanan"));
                                IsUsingMutasiSimpananBerjangka = Boolean.parseBoolean(response.getString("isUsingMutasiSimpananBerjangka"));
                                IsUsingTellerPinjaman = Boolean.parseBoolean(response.getString("isUsingTellerPinjaman"));

                                IDMaskSimpanan= response.getString("idMaskSimpanan");
                                IDMaskPinjaman= response.getString("idMaskPinjaman");
                                IDMaskSimpananBulanan= response.getString("idMaskSimpananBulanan");
                                IDMaskSimpananBerjangka= response.getString("idMaskSimpananBerjangka");

                                IsUsingAutoCompleteID=Boolean.parseBoolean(response.getString("isInputIDAutoComplete"));

                            }
                            else
                            {
                                String setText = "Soket";
                                institutionName.setText( setText);
                                Toast.makeText(MainActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                            savedData.setRegisteredInstitution(MainActivity.this,institutionCode, institutionName.getText().toString() );
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (!institutionCode.equals("000000"))
                            {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    error -> Toast.makeText(MainActivity.this,error.toString() , Toast.LENGTH_LONG).show()
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
            Toast.makeText(MainActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
    }

    //for killed from other activity
    public static MainActivity getInstance(){
        return   mainActivity;
    }

}