package com.soket.soketmobilecollector;

public class clsMutasiTeller {
    private String tanggal;
    private String tabID;
    private String tipeTransaksi;
    private String mutasi;
    private String nama;
    private String reference;
    private String jenisTransaksi;

    public void setTabID(String tabID) {
        this.tabID = tabID;
    }

    public String getTabID() {
        return tabID;
    }

    public void setTipeTransaksi(String tipeTransaksi) {
        this.tipeTransaksi = tipeTransaksi;
    }

    public String getTipeTransaksi() {
        return tipeTransaksi;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setMutasi(String mutasi) {
        this.mutasi = mutasi;
    }

    public String getMutasi() {
        return mutasi;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {this.nama=nama;}

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {this.reference=reference;}

    public String getJenisTransaksi() {
        return jenisTransaksi;
    }

    public void setJenisTransaksi(String jenisTransaksi) {this.jenisTransaksi=jenisTransaksi;}

}
