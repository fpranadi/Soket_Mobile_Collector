package com.soket.soketmobilecollector;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class clsPreference {
    /** Pendeklarasian key-data berupa String, untuk sebagai wadah penyimpanan data.
     * Jadi setiap data mempunyai key yang berbeda satu sama lain */
    static final String KEY_USERNAME_SEDANG_LOGIN = "Username_logged_in";
    static final String KEY_STATUS_SEDANG_LOGIN = "Status_logged_in";
    static final String KEY_KOLEKTOR_ID = "Kode Kolektor";
    static final String KEY_CAPEM_ID = "Kode Capem";
    static final String KEY_INSTITUTION_CODE="kode institusi";
    static final String KEY_INSTITUTION_NAME="Nama Institusi";

    static final String KEY_USING_SETORAN_TUNAI="Using Setoran tunai";
    static final String KEY_USING_PENARIKAN_TUNAI="Using Penarikan tunai";
    static final String KEY_USING_MUTASI_SIMPANAN="Using Mutasi Simpanan";
    static final String KEY_USING_MUTASI_PINJAMAN="Using Mutasi Pinjaman";
    static final String KEY_USING_MUTASI_SIMPANAN_BULANAN="Using Mutasi Simpanan Bulanan";
    static final String KEY_USING_MUTASI_SIMPANAN_BERJANGKA="Using Mutasi Simpanan Berjangka";

    static final String KEY_USING_TELLER_PINJAMAN="Using Teller Pinjaman";
    static final String KEY_IDMASK_SIMPANAN="ID Mask Simpanan";
    static final String KEY_IDMASK_PINJAMAN="ID Mask Pinjaman";
    static final String KEY_IDMASK_SIMPANAN_BERJANGKA="ID Mask Simpanan Berjangka";
    static final String KEY_IDMASK_SIMPANAN_BULANAN="ID Mask Simpanan Bulanan";
    static final String KEY_USING_AUTOCOMPLETE_ID="Using Auto Complete ID";
    static final String KEY_BT_DEVICE_NAME="Nama Device Bluetooth";
    static final String KEY_BT_DEVICE_ADDRESS="Address Device Bluetooth";
    static final String KEY_BT_DEVICE_MAX_CHAR_PER_LINE="Max Karakter per Line Device Bluetooth";
    static final String KEY_LAST_REQUEST="Request Terakhir";

    static final String KEY_USING_ANGSURANKOLEKTIF="Using Angsuran Kolektif";

    /** Pendlakarasian Shared Preferences yang berdasarkan paramater context */
    private static SharedPreferences getSharedPreference(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    //Institution Code================================
    public  void setRegisteredInstitution(Context context, String institutionCode, String institutionName){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_INSTITUTION_CODE, institutionCode);
        editor.putString(KEY_INSTITUTION_NAME, institutionName);
        editor.apply();
    }
    /** Mengembalikan nilai dari key KEY_KOLEKTOR_ID berupa String */
    public  String getRegisteredInstitutionCode(Context context){
        return getSharedPreference(context).getString(KEY_INSTITUTION_CODE,"000000");
    }
    /** Mengembalikan nilai dari key KEY_KOLEKTOR_ID berupa String */
    public  String getRegisteredInstitutionName(Context context){
        return getSharedPreference(context).getString(KEY_INSTITUTION_NAME,"Koperasi Indonesia");
    }
    //CApem================================
    public  void setRegisteredCapem(Context context, String capemID){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_CAPEM_ID, capemID);
        editor.apply();
    }
    /** Mengembalikan nilai dari key KEY_KOLEKTOR_ID berupa String */
    public  String getRegisteredCapem(Context context){
        return getSharedPreference(context).getString(KEY_CAPEM_ID,"");
    }

    //Kolektor=========================
    public  void setRegisteredKolektor(Context context, String kolektorID){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_KOLEKTOR_ID, kolektorID);
        editor.apply();
    }
    /** Mengembalikan nilai dari key KEY_KOLEKTOR_ID berupa String */
    public  String getRegisteredKolektor(Context context){
        return getSharedPreference(context).getString(KEY_KOLEKTOR_ID,"");
    }

    /** Deklarasi Edit Preferences dan mengubah data
     *  yang memiliki key KEY_USERNAME_SEDANG_LOGIN dengan parameter username */
    public  void setLoggedInUser(Context context, String username){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_USERNAME_SEDANG_LOGIN, username);
        editor.apply();
    }
    /** Mengembalikan nilai dari key KEY_USERNAME_SEDANG_LOGIN berupa String */
    public  String getLoggedInUser(Context context){
        return getSharedPreference(context).getString(KEY_USERNAME_SEDANG_LOGIN,"");
    }

    /** Deklarasi Edit Preferences dan mengubah data
     *  yang memiliki key KEY_STATUS_SEDANG_LOGIN dengan parameter status */
    public  void setLoggedInStatus(Context context, boolean status){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(KEY_STATUS_SEDANG_LOGIN,status);
        editor.apply();
    }
    /** Mengembalikan nilai dari key KEY_STATUS_SEDANG_LOGIN berupa boolean */
    public  boolean getLoggedInStatus(Context context){
        return getSharedPreference(context).getBoolean(KEY_STATUS_SEDANG_LOGIN,false);
    }

    /** Deklarasi Edit Preferences dan menghapus data, sehingga menjadikannya bernilai default
     *  khusus data yang memiliki key KEY_USERNAME_SEDANG_LOGIN dan KEY_STATUS_SEDANG_LOGIN */
    public  void clearLoggedInUser (Context context){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.remove(KEY_USERNAME_SEDANG_LOGIN);
        editor.remove(KEY_STATUS_SEDANG_LOGIN);
        editor.apply();
    }

    public  void setIsUsing(Context context, boolean isUsingSetoran, boolean isUsingPenarikan, boolean isUsingMutasiTab, boolean isUsingMutasiKredit, boolean isUsingMutasiSiBulan, boolean isUsingMutasiDep, boolean isUsingTellerPinjaman, boolean isUsingAngsuranKolektif){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(KEY_USING_MUTASI_SIMPANAN,isUsingMutasiTab);
        editor.putBoolean(KEY_USING_MUTASI_PINJAMAN,isUsingMutasiKredit);
        editor.putBoolean(KEY_USING_MUTASI_SIMPANAN_BULANAN,isUsingMutasiSiBulan);
        editor.putBoolean(KEY_USING_MUTASI_SIMPANAN_BERJANGKA,isUsingMutasiDep);
        editor.putBoolean(KEY_USING_SETORAN_TUNAI,isUsingSetoran);
        editor.putBoolean(KEY_USING_PENARIKAN_TUNAI,isUsingPenarikan);
        editor.putBoolean(KEY_USING_TELLER_PINJAMAN,isUsingTellerPinjaman);
        editor.putBoolean(KEY_USING_ANGSURANKOLEKTIF, isUsingAngsuranKolektif);
        editor.apply();
    }

    public  boolean getIsUsingSetoran(Context context){
        return getSharedPreference(context).getBoolean(KEY_USING_SETORAN_TUNAI,true);
    }
    public  boolean getIsUsingPenarikan(Context context){
        return getSharedPreference(context).getBoolean(KEY_USING_PENARIKAN_TUNAI,true);
    }
    public  boolean getIsUsingMutasiSimpanan(Context context){
        return getSharedPreference(context).getBoolean(KEY_USING_MUTASI_SIMPANAN,true);
    }
    public  boolean getIsUsingMutasiPinjaman(Context context){
        return getSharedPreference(context).getBoolean(KEY_USING_MUTASI_PINJAMAN,true);
    }
    public  boolean getIsUsingMutasiSimpananBulanan(Context context){
        return getSharedPreference(context).getBoolean(KEY_USING_MUTASI_SIMPANAN_BULANAN,true);
    }
    public  boolean getIsUsingMutasiSimpananBerjangka(Context context){
        return getSharedPreference(context).getBoolean(KEY_USING_MUTASI_SIMPANAN_BERJANGKA,true);
    }

    public  boolean getIsUsingTellerPinjaman(Context context){
        return getSharedPreference(context).getBoolean(KEY_USING_TELLER_PINJAMAN,true);
    }

    public  boolean getIsUsingAngsuranKolektif(Context context){
        return getSharedPreference(context).getBoolean(KEY_USING_ANGSURANKOLEKTIF,false);
    }

    public  void setIDMask(Context context, String IDMaskSimpanan, String IDMaskPinjaman, String IDMaskSimpananBulanan, String IDMaskSimpananBerjangka){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_IDMASK_SIMPANAN,IDMaskSimpanan);
        editor.putString(KEY_IDMASK_PINJAMAN,IDMaskPinjaman);
        editor.putString(KEY_IDMASK_SIMPANAN_BULANAN,IDMaskSimpananBulanan);
        editor.putString(KEY_IDMASK_SIMPANAN_BERJANGKA,IDMaskSimpananBerjangka);
        editor.apply();
    }
    public  String getIDMaskSimpanan(Context context){
        return getSharedPreference(context).getString(KEY_IDMASK_SIMPANAN,"000000");
    }
    public  String getIDMaskPinjaman(Context context){
        return getSharedPreference(context).getString(KEY_IDMASK_PINJAMAN,"000000");
    }
    public  String getIDMaskSimpananBulanan(Context context){
        return getSharedPreference(context).getString(KEY_IDMASK_SIMPANAN_BULANAN,"000000");
    }
    public  String getIDMaskSimpananBerjangka(Context context){
        return getSharedPreference(context).getString(KEY_IDMASK_SIMPANAN_BERJANGKA,"000000");
    }

    public  void setIsUsingAutoCompleteID(Context context, boolean IsUsingAutoCompleteID){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(KEY_USING_AUTOCOMPLETE_ID,IsUsingAutoCompleteID);
        editor.apply();
    }

    public  boolean getIsUsingAutoCompleteID(Context context){
        return getSharedPreference(context).getBoolean(KEY_USING_AUTOCOMPLETE_ID,false);
    }

    //bluetotth
    public  void setBTDevice(Context context, String DeviceName, String DeviceAddress, int DeviceMaxCharPerLine){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_BT_DEVICE_NAME,DeviceName);
        editor.putString(KEY_BT_DEVICE_ADDRESS,DeviceAddress);
        editor.putInt(KEY_BT_DEVICE_MAX_CHAR_PER_LINE, DeviceMaxCharPerLine);
        editor.apply();
    }

    public  String getBtDeviceName(Context context){
        return getSharedPreference(context).getString(KEY_BT_DEVICE_NAME,"");
    }

    public  String getBtDeviceAddress(Context context){
        return getSharedPreference(context).getString(KEY_BT_DEVICE_ADDRESS,"00:00:00:00:00:00");
    }

    public  int getBtDeviceMaxCharPerLine(Context context){
        return getSharedPreference(context).getInt(KEY_BT_DEVICE_MAX_CHAR_PER_LINE,32);
    }
    public  void setRecordedLastRequest(Context context, String Request){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_LAST_REQUEST,Request);
        editor.apply();
    }
    public  String getRecordedLastRequest(Context context){
        return getSharedPreference(context).getString(KEY_LAST_REQUEST,"");
    }
}
