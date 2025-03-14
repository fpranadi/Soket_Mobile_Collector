package com.soket.soketmobilecollector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements Runnable {

    private boolean IsUsingSetoranTunai;
    private boolean IsUsingPenarikanTunai;
    private boolean IsUsingMutasiSimpanan;
    private boolean IsUsingMutasiPinjaman;
    private boolean IsUsingMutasiSimpananBulanan;
    private boolean IsUsingMutasiSimpananBerjangka;
    private boolean IsUsingTellerPinjaman;
    private boolean IsUsingAngsuranKolektif;

    //for saved data
    private clsPreference currPreference;

    //Bluetooth Print
    BluetoothAdapter mBluetoothAdapter;
    private final UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    protected static final String TAG = "HomeActivity";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private TextView stat;
    private TextView mScan;
    private int printerNbrCharacterPerLine;
    //========

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        //assigmn control
        ImageView btnKeluar = findViewById(R.id.Keluar);
        ImageView btnPenarikanTunai = findViewById(R.id.PenarikanTunai);
        ImageView btnSetoranTunai = findViewById(R.id.SetoranTunai);
        ImageView btnSimpanan = findViewById(R.id.MutasiSimpanan);
        ImageView btnSimpananBerjangka = findViewById(R.id.MutasiSimpananBerjangka);
        ImageView btnSimpananBulanan = findViewById(R.id.MutasiSimpananBulanan);
        ImageView btnPinjaman = findViewById(R.id.MutasiPinjaman);
        ImageView btnLaporanTeller = findViewById(R.id.MutasiKolektor);
        ImageView btnTellerPinjaman = findViewById(R.id.TellerPinjaman_Main);
        TextView txtinstitutionName = findViewById(R.id.txtInstitutionName);
        ImageView btnBtConnect = findViewById(R.id.Bluetooth_Connect);
        ImageView btnAngsuranKolektif = findViewById(R.id.imageView_AngsuranKolektif);
        stat = findViewById(R.id.textView_BT_Status);
        mScan = findViewById(R.id.textView_BT_Scan);

        //cek apa keaddan login apa tidak
        currPreference = new clsPreference();
        boolean currLoggedInStatus = currPreference.getLoggedInStatus(this);

        if (currLoggedInStatus) {
            IsUsingSetoranTunai = currPreference.getIsUsingSetoran(this);
            IsUsingPenarikanTunai = currPreference.getIsUsingPenarikan(this);
            IsUsingMutasiSimpanan = currPreference.getIsUsingMutasiSimpanan(this);
            IsUsingMutasiSimpananBulanan = currPreference.getIsUsingMutasiSimpananBulanan(this);
            IsUsingMutasiPinjaman = currPreference.getIsUsingMutasiPinjaman(this);
            IsUsingMutasiSimpananBerjangka = currPreference.getIsUsingMutasiSimpananBerjangka(this);
            IsUsingTellerPinjaman = currPreference.getIsUsingTellerPinjaman(this);
            txtinstitutionName.setText(currPreference.getRegisteredInstitutionName(this));
            printerNbrCharacterPerLine = currPreference.getBtDeviceMaxCharPerLine(this);
            IsUsingAngsuranKolektif = currPreference.getIsUsingAngsuranKolektif(this);
        } else {
            currPreference.clearLoggedInUser(this);
            finish();
        }

        btnKeluar.setOnClickListener(view -> {
            currPreference.clearLoggedInUser(HomeActivity.this);
            finish();
        });

        btnLaporanTeller.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, LaporanTellerActivity.class);
            startActivity(intent);
        });

        btnSimpanan.setOnClickListener(view -> {
            if (IsUsingMutasiSimpanan) {
                Intent intent = new Intent(HomeActivity.this, MutasiTabunganConfirmationActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Not Available", Toast.LENGTH_LONG).show();
            }
        });

        btnSimpananBulanan.setOnClickListener(view -> {
            if (IsUsingMutasiSimpananBulanan) {
                Intent intent = new Intent(HomeActivity.this, MutasiSimpananBulananConfirmationActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Not Available", Toast.LENGTH_LONG).show();
            }
        });

        btnSimpananBerjangka.setOnClickListener(view -> {
            if (IsUsingMutasiSimpananBerjangka) {
                Intent intent = new Intent(HomeActivity.this, MutasiDepositoConfirmationActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Not Available", Toast.LENGTH_LONG).show();
            }
        });

        btnPinjaman.setOnClickListener(view -> {
            if (IsUsingMutasiPinjaman) {
                Intent intent = new Intent(HomeActivity.this, MutasiPinjamanConfirmationMainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Not Available", Toast.LENGTH_LONG).show();
            }
        });

        btnSetoranTunai.setOnClickListener(view -> {
            if (IsUsingSetoranTunai) {
                Intent intent = new Intent(HomeActivity.this, SetoranTunaiInquiryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Not Available", Toast.LENGTH_LONG).show();
            }
        });

        btnPenarikanTunai.setOnClickListener(view -> {
            if (IsUsingPenarikanTunai) {
                Intent intent = new Intent(HomeActivity.this, PenarikanTunaiInquiryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Not Available", Toast.LENGTH_LONG).show();
            }
        });

        btnTellerPinjaman.setOnClickListener(view -> {
            if (IsUsingTellerPinjaman) {
                Intent intent = new Intent(HomeActivity.this, TellerPinjamanInquiryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Not Available", Toast.LENGTH_LONG).show();
            }
        });

        btnAngsuranKolektif.setOnClickListener(view -> {
            if (IsUsingAngsuranKolektif) {
                Intent intent = new Intent(HomeActivity.this, AngsuranKolektifInquiryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Not Available", Toast.LENGTH_LONG).show();
            }
        });

        btnBtConnect.setOnClickListener(mView -> {
            if (mScan.getText().toString().equalsIgnoreCase("Connect")) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    Toast.makeText(HomeActivity.this, "Bluetooth Adapter tidak tersedia ...!", Toast.LENGTH_SHORT).show();
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                                return;
                            }
                            //return;
                        }
                        try {
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            Toast.makeText(HomeActivity.this, "Bluetooth baru di-Enabled ...!", Toast.LENGTH_SHORT).show();
                        } catch (SecurityException ee) {
                            Toast.makeText(HomeActivity.this, "Error on bluetooth security !", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        ListPairedDevices();
                        Intent connectIntent = new Intent(HomeActivity.this, DeviceListActivity.class);
                        startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                    }
                }

            } else {
                try {
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.disable();
                    }
                } catch (SecurityException ee1) {
                    Toast.makeText(HomeActivity.this, "Error On bluettooth Security", Toast.LENGTH_SHORT).show();
                }

                stat.setText("");
                stat.setText(R.string.disconnected);
                stat.setTextColor(Color.rgb(199, 59, 59));
                btnBtConnect.setEnabled(true);
                mScan.setText(R.string.connect);
            }
        });
    }

    private void ListPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
            //return;
        }
        try {
            Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
            if (!mPairedDevices.isEmpty()) {
                for (BluetoothDevice mDevice : mPairedDevices) {
                    Log.v(TAG, "PairedDevices: " + mDevice.getName() + "  " + mDevice.getAddress());
                }
            }
        } catch (SecurityException Err) {
            Toast.makeText(HomeActivity.this, "Permission Bluetooth bermasalah !", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* Terminate bluetooth connection and close all sockets opened */
        try {
            if (mBluetoothSocket != null) mBluetoothSocket.close();
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
    }

    public void onActivityResult(int mRequestCode, int mResultCode, Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);
        switch (mRequestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (mResultCode == Activity.RESULT_OK) {
                    Bundle mExtra = mDataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    String mDeviceName = mExtra.getString("DeviceName");
                    Log.v(TAG, "Coming incoming address " + mDeviceAddress);

                    //input character per line
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("Jumlah Karakter per Baris ?");
                    final EditText input = new EditText(HomeActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setText(String.valueOf(printerNbrCharacterPerLine));
                    builder.setView(input);
                    builder.setPositiveButton("Apply", (dialog, which) -> {
                        dialog.dismiss();
                        printerNbrCharacterPerLine = Integer.parseInt(input.getText().toString());
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                    builder.show();
                    //========

                    currPreference.setBTDevice(HomeActivity.this, mDeviceName, mDeviceAddress, printerNbrCharacterPerLine);

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                            return;
                        }
                        //return;
                    }

                    try {
                        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                        mBluetoothConnectProgressDialog = ProgressDialog.show(this, "Connecting...", mBluetoothDevice.getName() + " : " + mBluetoothDevice.getAddress(), true, false);

                        Thread mBlutoothConnectThread = new Thread(this);
                        mBlutoothConnectThread.start();
                    } catch (SecurityException err) {
                        Toast.makeText(HomeActivity.this, "Error : Security Bluetooth ...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                if (mResultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(HomeActivity.this, DeviceListActivity.class);
                    startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(HomeActivity.this, "Not connected to any device", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            stat.setText("");
            stat.setText(R.string.connected);
            stat.setTextColor(Color.rgb(97, 170, 74));
            mScan.setText(R.string.disconnect);
        }
    };

    @Override
    public void run() {
        try {
            /* Attempt to connect to bluetooth device */
            if (ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 2);
                    return;
                }
            }
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return;
                }
            }
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);
        } catch (IOException eConnectException) {
            Log.d(TAG, "CouldNotConnectToSocket", eConnectException);
            Toast.makeText(HomeActivity.this, "CouldNotConnectToSocket", Toast.LENGTH_SHORT).show();
        } catch (SecurityException Err1)  {
            Toast.makeText(HomeActivity.this, "Error on Security Bluetooth ..", Toast.LENGTH_SHORT).show();
        }
        finally {
            //langsung di close aja... yang disini buat tes aja
            closeSocket(mBluetoothSocket);
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d(TAG, "Socket Closed");
        } catch (IOException ex) {
            Toast.makeText(HomeActivity.this, "Could Not Close Socket", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Could Not Close Socket");
        }
    }
}