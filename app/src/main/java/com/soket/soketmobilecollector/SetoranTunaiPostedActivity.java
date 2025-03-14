package com.soket.soketmobilecollector;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetoranTunaiPostedActivity extends AppCompatActivity {
    private String reference;
    private Boolean reShow;
    private String  InstitutionName;
    private String institutionCode;
    private String hashKey;
    private String urlAPI;

    //create adapter
    private JurnalTransaksiAdapter adJurnalTransaksi;
    private ArrayList<clsItemValue> arrJurnalTransaksi  ;
    private clsItemValue JurnalTransaksi;

    //bluetooth print=====================
    private String textToPrint1, textToPrint2, textToPrint3, textToPrint4, textToPrint5, textToPrint6, textToPrint7, textToPrint8;
    private int printerNbrCharacterPerLine;
    private BluetoothSocket mBluetoothSocket;
    BluetoothAdapter mBluetoothAdapter;
    private final UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    BluetoothDevice mBluetoothDevice;
    private static final int REQUEST_ENABLE_BT = 2;
    protected static final String TAG1 = "SetoranTunaiPostedActivity";
    //===============

    private TextView txtInstitutionName;

    private  clsPreference currPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setoran_tunai_posted);
        ImageButton btnJurnalTransaksiBack =  findViewById(R.id.imageButtonBackJurnalTransaksi);
        RecyclerView daftarMutasi =  findViewById(R.id.RecyclerViewMutasiJurnalTransaksi);
        txtInstitutionName =  findViewById(R.id.textViewinstitutionJurnalTransaksi);
        ImageButton btnJurnalTransaksiShare =  findViewById(R.id.imageButtonShare);
        ImageView logo =  findViewById(R.id.imageViewSetoranTunaiPosted);
        ImageButton btnPrint = findViewById(R.id.imageButton_Print_SetoranTunaiPosted);
        ImageButton btnBack = findViewById(R.id.imageButton_Back_SetoranTunaiPosted);

        try {
            if (savedInstanceState == null)
            {
                Bundle extras = getIntent().getExtras();

                if (extras == null) {
                    reference = null;
                    reShow=false;
                } else {
                    reference = extras.getString("REFERENCE");
                    reShow = extras.getBoolean("RESHOW");
                }
            }
            else
            {
                reference= (String) savedInstanceState.getSerializable("REFERENCE");
                reShow= (Boolean) savedInstanceState.getSerializable("RESHOW");
            }
        }
        catch (Exception err) {
            reference = null;
            reShow = false;
        }

        //for saved data
        currPreference = new clsPreference();
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);
        InstitutionName = currPreference.getRegisteredInstitutionName(this);

        if (InstitutionName.toUpperCase().contains("KOPERASI") || InstitutionName.toUpperCase().contains("KSP") || InstitutionName.toUpperCase().contains("KPN") || InstitutionName.toUpperCase().contains("KSU"))
        {
            logo.setImageResource(R.drawable.mylogo_small);
        }
        else
        {
            logo.setImageResource(R.drawable.logolpd);
        }

        if (currLoggedInStatus) {
            //bluetooth print
            printerNbrCharacterPerLine= currPreference.getBtDeviceMaxCharPerLine(this);
            //===============

            hashKey=getString(R.string.hashKey);
            urlAPI=getString(R.string.webService );
            institutionCode= currPreference.getRegisteredInstitutionCode(this);
            txtInstitutionName.setText(currPreference.getRegisteredInstitutionName(this) );

            //-----------------bind ke recycler view
            arrJurnalTransaksi= new ArrayList<>();
            adJurnalTransaksi = new JurnalTransaksiAdapter(arrJurnalTransaksi);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SetoranTunaiPostedActivity.this);
            daftarMutasi.setLayoutManager(layoutManager);
            daftarMutasi.setAdapter(adJurnalTransaksi);

            bindDataJurnalTransaksi();
        } else {
            currPreference.clearLoggedInUser(this);
            finish();

        }

        btnJurnalTransaksiBack.setOnClickListener(view -> {
            finish();
            if (!reShow) {
                SetoranTunaiActivity.getInstance().finish();
            }
        });

        btnBack.setOnClickListener(view -> {
            finish();
            if (!reShow) {
                SetoranTunaiActivity.getInstance().finish();
            }
        });

        btnPrint.setOnClickListener(mView -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SetoranTunaiPostedActivity.this);
            builder.setTitle("Siapkan Printer Bluetooth !!!");
            builder.setPositiveButton("Lanjut", (dialog, which) -> {
                dialog.dismiss();

                /* Attempt to connect to bluetooth device */
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(SetoranTunaiPostedActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 2);
                        return;
                    }
                }
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(SetoranTunaiPostedActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }

                try
                {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null)
                    {
                        Toast.makeText(SetoranTunaiPostedActivity.this, "Bluetooth Adapter tidak tersedia ...!", Toast.LENGTH_SHORT).show();
                    } else
                    {
                        if (!mBluetoothAdapter.isEnabled())
                        {
                            try {
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                Toast.makeText(SetoranTunaiPostedActivity.this, "Bluetooth baru di-Enabled ...!", Toast.LENGTH_SHORT).show();
                            }
                            catch (SecurityException Err)
                            {
                                Toast.makeText(SetoranTunaiPostedActivity.this, Err.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else
                        {
                            try {
                                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(currPreference.getBtDeviceAddress(SetoranTunaiPostedActivity.this));
                                mBluetoothConnectProgressDialog = ProgressDialog.show(this,"Connecting...", mBluetoothDevice.getName() + " : " + mBluetoothDevice.getAddress(), true, false);
                                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
                                mBluetoothAdapter.cancelDiscovery();
                                mBluetoothSocket.connect();
                                mBluetoothConnectProgressDialog.dismiss();

                                Toast.makeText(SetoranTunaiPostedActivity.this, "Copy #1 is Printing", Toast.LENGTH_LONG).show();
                                doPrint("-Untuk Nasabah");
                                /* 5000 ms (5 Seconds) */
                                int TIME = 5000;
                                new Handler().postDelayed(() -> {
                                    /* print second copy */
                                    Toast.makeText(SetoranTunaiPostedActivity.this, "Copy #2 is Printing", Toast.LENGTH_LONG).show();
                                    doPrint("-Untuk Kolektor");
                                    closeSocket(mBluetoothSocket);
                                }, TIME);
                            } catch (IOException eConnectException) {
                                Log.d(TAG1, "Could Not Connect To Socket", eConnectException);
                                Toast.makeText(SetoranTunaiPostedActivity.this, "Could Not Connect To Socket", Toast.LENGTH_LONG).show();
                                mBluetoothConnectProgressDialog.dismiss();
                                closeSocket(mBluetoothSocket);
                            } catch (SecurityException Err1) {
                                Log.d(TAG1, Err1.getMessage(), Err1);
                                Toast.makeText(SetoranTunaiPostedActivity.this, Err1.getMessage(), Toast.LENGTH_LONG).show();

                                //AlertDialog.Builder builder1 = new AlertDialog.Builder(SetoranTunaiPostedActivity.this);
                                //builder1.setTitle("Siapkan Printer Bluetooth !!!");
                                //builder1.setPositiveButton("Lanjut", (dialog1, which) -> {
                                //            dialog1.dismiss();
                                //        });

                                mBluetoothConnectProgressDialog.dismiss();
                                closeSocket(mBluetoothSocket);
                            }
                        }
                    }
                } catch (Exception errp)
                {
                    Toast.makeText(SetoranTunaiPostedActivity.this, errp.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        btnJurnalTransaksiShare.setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "BUKTI KAS MASUK - ".concat(InstitutionName) );

            String extraParams = "BUKTI KAS MASUK - ".concat(InstitutionName).concat("\n");
            for (int i=0;i<8 ;i++)
            {
                extraParams = extraParams.concat(arrJurnalTransaksi.get(i).getItem()).concat(":\n");
                extraParams = extraParams.concat("    ").concat(arrJurnalTransaksi.get(i).getValue()).concat("\n");
            }
            sendIntent.putExtra(Intent.EXTRA_TEXT, extraParams);

            Intent shareIntent = Intent.createChooser(sendIntent, "Share Via");
            startActivity(shareIntent);
        });
    }

    private void bindDataJurnalTransaksi()
    {
        try {
            JSONObject postparams = new JSONObject();
            postparams.put("InstitutionCode", institutionCode);
            postparams.put("Reference", reference);
            postparams.put("hashCode", clsGenerateSHA.hex256(institutionCode.concat(reference).concat(hashKey),true));
            sendPostForBindJurnalTransaksi(urlAPI.concat("/JurnalTransaksi") , postparams);
        }
        catch (JSONException e)
        {
            //e.printStackTrace();
            Toast.makeText(SetoranTunaiPostedActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPostForBindJurnalTransaksi(String url, JSONObject JSONBodyParam)  {
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
                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Tanggal");
                                JurnalTransaksi.setValue(response.getString("tanggal"));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint1 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Transaksi");
                                JurnalTransaksi.setValue(response.getString("transaksi"));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint2 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Rekening Tujuan");
                                JurnalTransaksi.setValue(response.getString("rekTujuan"));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint3 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Nama");
                                JurnalTransaksi.setValue(response.getString("nama"));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint4 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Referensi");
                                JurnalTransaksi.setValue(response.getString("noRef"));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint5 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Nominal");
                                JurnalTransaksi.setValue(formatedAmount(response.getString("nominal")));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint6 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Status");
                                JurnalTransaksi.setValue(response.getString("status"));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint7 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Saldo Akhir");
                                JurnalTransaksi.setValue(formatedAmount(response.getString("saldoakhir")));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint8 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                adJurnalTransaksi.notifyDataSetChanged();
                            }
                            else
                            {
                                Toast.makeText(SetoranTunaiPostedActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Toast.makeText(SetoranTunaiPostedActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        Toast.makeText(SetoranTunaiPostedActivity.this,"Session telah habis, Mohon Login Kembali !!!" , Toast.LENGTH_LONG).show();
                        finish();

                    }

            )
            {
                @Override
                public Map<String, String> getHeaders()  {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer ".concat(currPreference.getAccessToken(SetoranTunaiPostedActivity.this)));

                    return headers;
                }
            };
            requestQueue.add(objectRequest);
        }catch (Exception e){
            Toast.makeText(SetoranTunaiPostedActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
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

    private String addSpaceBetween(String Phrase, int MaxCharacter)
    {
        String spaceString="";
        if (Phrase.length() < MaxCharacter)
        {
            for (int i=Phrase.length(); i < MaxCharacter; i++)
            {
                spaceString=spaceString.concat(" ");
            }
        }
        return spaceString;
    }

    public void doPrint( String CopySName)
    {
        try
        {
            OutputStream os1 = mBluetoothSocket.getOutputStream();
            String he1="";
            String header1="" ;
            String blank = "\n";
            String footer = "Terima kasih telah bertransaksi pada Aplikasi Mobile ".concat(txtInstitutionName.getText().toString()).concat("\n\n\n\n\n");
            for (int i=0; i < printerNbrCharacterPerLine; i++)
            {
                he1=he1.concat("*");
                header1=header1.concat("=");
            }
            he1=he1.concat("\n");
            header1=header1.concat("\n");

            String copy1 = "-".concat(CopySName).concat("\n\n");
            //os1.write(centring(txtInstitutionName.getText().toString(),printerNbrCharacterPerLine).concat("\n").getBytes() );
            os1.write(blank.getBytes());
            os1.write(txtInstitutionName.getText().toString().concat("\n").getBytes() );
            os1.write(he1.getBytes());
            os1.write("Bukti Kas Masuk\n".getBytes());
            os1.write(header1.getBytes());
            os1.write(textToPrint1.getBytes());
            os1.write(textToPrint2.getBytes());
            os1.write(textToPrint3.getBytes());
            os1.write(textToPrint4.getBytes());
            os1.write(textToPrint5.getBytes());
            os1.write(textToPrint6.getBytes());
            os1.write(textToPrint7.getBytes());
            os1.write(textToPrint8.getBytes());
            os1.write(header1.getBytes());
            os1.write(copy1.getBytes());
            os1.write(footer.getBytes());

            // Setting height
            int gs = 29;
            os1.write(intToByteArray(gs));
            int h = 150;
            os1.write(intToByteArray(h));
            int n = 170;
            os1.write(intToByteArray(n));

            // Setting Width
            int gs_width = 29;
            os1.write(intToByteArray(gs_width));
            int w = 119;
            os1.write(intToByteArray(w));
            int n_width = 2;
            os1.write(intToByteArray(n_width));
        }
        catch (Exception e)
        {
            Log.e("PrintActivity", "Exe ", e);
            Toast.makeText(SetoranTunaiPostedActivity.this,e.getMessage() , Toast.LENGTH_LONG).show();
        }
    }

    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();
        return b[3];
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d(TAG1, "Socket Closed");
        } catch (IOException ex) {
            Log.d(TAG1, "Could Not Close Socket");
        }
    }
}
