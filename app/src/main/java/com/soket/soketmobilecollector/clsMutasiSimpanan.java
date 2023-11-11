package com.soket.soketmobilecollector;

public class clsMutasiSimpanan {
    private String NoTransaksi="_";
    private String Tanggal ="_";
    private String TipeTransaksi="_";
    private String Debet="_";
    private String Kredit="_";
    private String Saldo="_";
    private String UserID="_";

    public String getNoTransaksi() {
        return NoTransaksi;
    }

    public void setNoTransaksi(String noTransaksi) {
        NoTransaksi = noTransaksi;
    }

    public String getTanggal() {
        return Tanggal;
    }

    public void setTanggal(String tanggal) {
        Tanggal = tanggal;
    }

    public String getTipeTransaksi() {
        return TipeTransaksi;
    }

    public void setTipeTransaksi(String tipeTransaksi) {
        TipeTransaksi = tipeTransaksi;
    }

    public String getDebet() {
        return Debet;
    }

    public void setDebet(String debet   ) {
            Debet = debet;

    }

    public String getKredit() {
        return Kredit;
    }

    public void setKredit(String kredit) {
            Kredit = kredit;

    }

    public void setSaldo(String saldo) {
        Saldo = saldo;
    }

    public String getSaldo() {
        return Saldo;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getUserID() {
        return UserID;
    }
}
