package com.soket.soketmobilecollector;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class TellerPinjamanPostedActivity extends AppCompatActivity {

    private String reference;
    private Boolean reShow;
    private String InstitutionName_Debet;
    private String institutionCode;
    private String hashKey;
    private String urlAPI;
    private TextView txtInstitutionName;

    //create adapter
    private JurnalTransaksiAdapter adJurnalTransaksi;
    private ArrayList<clsItemValue> arrJurnalTransaksi  ;
    private clsItemValue JurnalTransaksi;

    //bluetooth print=====================
    private String textToPrint1, textToPrint2, textToPrint3, textToPrint4, textToPrint5, textToPrint6, textToPrint7, textToPrint8,textToPrint9, textToPrint10, textToPrint11;
    private int printerNbrCharacterPerLine;
    private BluetoothSocket mBluetoothSocket;
    BluetoothAdapter mBluetoothAdapter;
    private final UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    BluetoothDevice mBluetoothDevice;
    private static final int REQUEST_ENABLE_BT = 2;
    protected static final String TAG1 = "TellerPinjamanPostedActivity";
    //===============

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teller_pinjaman_posted);

        ImageButton btnJurnalTransaksiBack =  findViewById(R.id.imageButtonBackJurnalTransaksi_TellerPinjaman);
        RecyclerView daftarMutasi =  findViewById(R.id.RecyclerViewMutasiJurnalTransaksi_TellerPinjaman);
        txtInstitutionName =  findViewById(R.id.textViewinstitutionJurnalTransaksi_TellerPinjaman);
        ImageButton btnJurnalTransaksiSharePenarikan =  findViewById(R.id.imageButtonShare_TellerPinjaman);
        ImageView logo =  findViewById(R.id.imageView__TellerPinjamanPosted);
        ImageButton btnPrint = findViewById(R.id.imageButton_Print_TellerPinjamanPosted);
        ImageButton btnBack = findViewById(R.id.imageButton_Back_TellerPinjamanPosted);


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

        //for saved data
        clsPreference currPreference = new clsPreference();
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);
        InstitutionName_Debet= currPreference.getRegisteredInstitutionName(this);

        if (InstitutionName_Debet.toUpperCase().contains("KOPERASI") || InstitutionName_Debet.toUpperCase().contains("KSP") || InstitutionName_Debet.toUpperCase().contains("KPN") || InstitutionName_Debet.toUpperCase().contains("KSU") )
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
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TellerPinjamanPostedActivity.this);
            daftarMutasi.setLayoutManager(layoutManager);
            daftarMutasi.setAdapter(adJurnalTransaksi);

            bindDataJurnalTransaksi();

        } else {
            currPreference.clearLoggedInUser(this);
            finish();

        }

        btnJurnalTransaksiBack.setOnClickListener(view -> {
            //for kill setorantunaiactiviry from posted
            finish();
            if (!reShow) {
                TellerPinjamanActivity.getInstance().finish();
            }

        });

        btnBack.setOnClickListener(view -> {
            //for kill setorantunaiactiviry from posted
            finish();
            if (!reShow) {
                TellerPinjamanActivity.getInstance().finish();
            }

        });

        btnPrint.setOnClickListener(mView -> {
            //input character per line
            AlertDialog.Builder builder = new AlertDialog.Builder(TellerPinjamanPostedActivity.this);
            builder.setTitle("Siapkan Printer Bluetooth !!!");
            builder.setPositiveButton("Lanjut", (dialog, which) -> {
                dialog.dismiss();

                /* Attempt to connect to bluetooth device */
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(TellerPinjamanPostedActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 2);
                        return;
                    }
                    //return;
                }
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(TellerPinjamanPostedActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                    //return;
                }

                try
                {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null)
                    {
                        Toast.makeText(TellerPinjamanPostedActivity.this, "Bluetooth Adapter tidak tersedia ...!", Toast.LENGTH_SHORT).show();
                    } else
                    {
                        if (!mBluetoothAdapter.isEnabled())
                        {
                            try {
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                Toast.makeText(TellerPinjamanPostedActivity.this, "Bluetooth baru di-Enabled ...!", Toast.LENGTH_SHORT).show();
                            }
                            catch (SecurityException Err)
                            {
                                Toast.makeText(TellerPinjamanPostedActivity.this, Err.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else
                        {
                            try {
                                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(currPreference.getBtDeviceAddress(TellerPinjamanPostedActivity.this));
                                mBluetoothConnectProgressDialog = ProgressDialog.show(this,"Connecting...", mBluetoothDevice.getName() + " : " + mBluetoothDevice.getAddress(), true, false);
                                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
                                mBluetoothAdapter.cancelDiscovery();
                                mBluetoothSocket.connect();
                                mBluetoothConnectProgressDialog.dismiss();

                                Toast.makeText(TellerPinjamanPostedActivity.this, "Copy #1 is Printing", Toast.LENGTH_LONG).show();
                                doPrint("-Untuk Nasabah");
                                /* 5000 ms (5 Seconds) */
                                int TIME = 5000;
                                new Handler().postDelayed(() -> {
                                    /* print second copy */
                                    Toast.makeText(TellerPinjamanPostedActivity.this, "Copy #2 is Printing", Toast.LENGTH_LONG).show();
                                    doPrint("-Untuk Kolektor");
                                    closeSocket(mBluetoothSocket);
                                }, TIME);
                            } catch (IOException eConnectException) {
                                Log.d(TAG1, "Could Not Connect To Socket", eConnectException);
                                Toast.makeText(TellerPinjamanPostedActivity.this, "Could Not Connect To Socket", Toast.LENGTH_LONG).show();
                                mBluetoothConnectProgressDialog.dismiss();
                                closeSocket(mBluetoothSocket);
                            } catch (SecurityException Err1) {
                                Toast.makeText(TellerPinjamanPostedActivity.this, Err1.getMessage(), Toast.LENGTH_LONG).show();
                                Log.d(TAG1, "Could Not Connect To Socket", Err1);
                                mBluetoothConnectProgressDialog.dismiss();
                                closeSocket(mBluetoothSocket);
                            }
                        }
                    }
                } catch (Exception errp)
                {
                    Toast.makeText(TellerPinjamanPostedActivity.this, errp.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
            builder.show();
            //========
        });

        btnJurnalTransaksiSharePenarikan.setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "JURNAL TRANSAKSI - ".concat(InstitutionName_Debet) );

            String extraParams = "JURNAL TRANSAKSI - ".concat(InstitutionName_Debet).concat("\n");
            for (int i=0;i<10 ;i++)
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
            sendPostForBindJurnalTransaksi(urlAPI.concat("/JurnalTransaksiKredit") , postparams);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Toast.makeText(TellerPinjamanPostedActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                                JurnalTransaksi.setItem("No. Pinjaman");
                                JurnalTransaksi.setValue(response.getString("pinjamanNo"));
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
                                JurnalTransaksi.setValue(formatedAmount(response.getString("bayar")));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint6 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Pokok");
                                JurnalTransaksi.setValue(formatedAmount(response.getString("pokok")));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint7 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Bunga");
                                JurnalTransaksi.setValue(formatedAmount(response.getString("bunga")));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint8 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Denda");
                                JurnalTransaksi.setValue(formatedAmount(response.getString("denda")));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint9 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";


                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Status");
                                JurnalTransaksi.setValue(response.getString("status"));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint10 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                JurnalTransaksi = new clsItemValue();
                                JurnalTransaksi.setItem("Sisa Pinjaman");
                                JurnalTransaksi.setValue(formatedAmount(response.getString("sisaPinjaman")));
                                arrJurnalTransaksi.add(JurnalTransaksi);
                                textToPrint11 =  JurnalTransaksi.getItem() + addSpaceBetween(JurnalTransaksi.getItem().concat(JurnalTransaksi.getValue()), printerNbrCharacterPerLine) + JurnalTransaksi.getValue() + "\n";

                                adJurnalTransaksi.notifyDataSetChanged();
                            }
                            else
                            {
                                Toast.makeText(TellerPinjamanPostedActivity.this,"Error : ".concat(ResponseDescription), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(TellerPinjamanPostedActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(TellerPinjamanPostedActivity.this,error.toString() , Toast.LENGTH_LONG).show()
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
            Toast.makeText(TellerPinjamanPostedActivity.this,e.toString() , Toast.LENGTH_LONG).show();
        }
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
            os1.write(blank.getBytes());
            os1.write(txtInstitutionName.getText().toString().concat("\n").getBytes() );
            os1.write(he1.getBytes());
            os1.write("Jurnal Transaksi\n".getBytes());
            os1.write(header1.getBytes());
            os1.write(textToPrint1.getBytes());
            os1.write(textToPrint2.getBytes());
            os1.write(textToPrint3.getBytes());
            os1.write(textToPrint4.getBytes());
            os1.write(textToPrint5.getBytes());
            os1.write(textToPrint6.getBytes());
            os1.write(textToPrint7.getBytes());
            os1.write(textToPrint8.getBytes());
            os1.write(textToPrint9.getBytes());
            os1.write(textToPrint10.getBytes());
            os1.write(textToPrint11.getBytes());

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
            Toast.makeText(TellerPinjamanPostedActivity.this,e.getMessage() , Toast.LENGTH_LONG).show();
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