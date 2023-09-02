package com.example.slagalica.Activities.IgriceActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.slagalica.Activities.KorisnikActivities.RegistrovanKorisnik;
import com.example.slagalica.Kontroleri.AsocijacijeKontroler;
import com.example.slagalica.Kontroleri.KontrolerKonekcije;
import com.example.slagalica.R;

import java.util.ArrayList;

public class Asocijacije extends AppCompatActivity {
    private Button[][] polje = new Button[4][4];
    private EditText aOdgovor, bOdgovor, cOdgovor, dOdgovor, konacanOdgovor;
    private ImageButton Adalje, Bdalje, Cdalje, Ddalje, konacnoDalje;
    private TextView vreme;

    private AsocijacijeKontroler asocijacijeKontroler;

    private KontrolerKonekcije kontrolerKonekcije;
    private ViewGroup glavniLejaut;

    private View prekrivac;

    private Boolean nameraIzlaska = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asocijacije);

        vreme = findViewById(R.id.vreme);

        inicijalizacija();
        postaviListenere();
        asocijacijeKontroler = new AsocijacijeKontroler(getIntent().getStringExtra("IGRA_ID"), getIntent().getIntExtra("BROJ_IGRACA", 1), getIntent().getIntExtra("REDNI_BROJ_PARTIJE", 1));
        asocijacijeKontroler.kreirajAsocijacije(Asocijacije.this);
        kontrolerKonekcije = new KontrolerKonekcije();
        kontrolerKonekcije.postaviListenerZaNagliPrekidIgre(Asocijacije.this,getIntent().getStringExtra("IGRA_ID"));

    }


    @Override
    public void onBackPressed(){
        if(nameraIzlaska == true && getIntent().getIntExtra("BROJ_IGRACA", 1) ==2){
            Intent intent = new Intent(Asocijacije.this, RegistrovanKorisnik.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        if(getIntent().getIntExtra("BROJ_IGRACA", 1) == 1){
            nameraIzlaska = false;
        }

        super.onBackPressed();
    }

    @Override
    public void onPause(){
        super.onPause();
        asocijacijeKontroler.prekiniTajmer();
        asocijacijeKontroler.dobaviListenerManager().removeAllListeners();
    }



    @Override
    public void onDestroy(){
        asocijacijeKontroler.obrisiOdgovore();
        asocijacijeKontroler.prekiniTajmer();
        asocijacijeKontroler.dobaviListenerManager().removeAllListeners();
        kontrolerKonekcije.dobaviListenerManager().removeAllListeners();
        if(nameraIzlaska == true){
            kontrolerKonekcije.obrisiPodatkeIgre(getIntent().getStringExtra("IGRA_ID"));
        }
        super.onDestroy();
    }


    public void inicijalizacija(){
        polje[0][0] = findViewById(R.id.a1);
        polje[0][1] = findViewById(R.id.a2);
        polje[0][2] = findViewById(R.id.a3);
        polje[0][3] = findViewById(R.id.a4);

        polje[1][0] = findViewById(R.id.b1);
        polje[1][1] = findViewById(R.id.b2);
        polje[1][2] = findViewById(R.id.b3);
        polje[1][3] = findViewById(R.id.b4);

        polje[2][0] = findViewById(R.id.c1);
        polje[2][1] = findViewById(R.id.c2);
        polje[2][2] = findViewById(R.id.c3);
        polje[2][3] = findViewById(R.id.c4);

        polje[3][0] = findViewById(R.id.d1);
        polje[3][1] = findViewById(R.id.d2);
        polje[3][2] = findViewById(R.id.d3);
        polje[3][3] = findViewById(R.id.d4);

        aOdgovor = findViewById(R.id.aOdgovor);
        bOdgovor = findViewById(R.id.bOdgovor);
        cOdgovor = findViewById(R.id.cOdgovor);
        dOdgovor = findViewById(R.id.dOdgovor);
        konacanOdgovor = findViewById(R.id.konacanOdgovor);

        Adalje = findViewById(R.id.daljeA);
        Bdalje = findViewById(R.id.daljeB);
        Cdalje = findViewById(R.id.daljeC);
        Ddalje = findViewById(R.id.daljeD);
        konacnoDalje = findViewById(R.id.daljeKonacno);
    }

    private void postaviListenere() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                final int red = i;
                final int kolona = j;

                polje[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        asocijacijeKontroler.otvoriPolje(Asocijacije.this, red, kolona);
                    }
                });
            }
        }

        Adalje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sakrijTastaturu();
                asocijacijeKontroler.aDalje(Asocijacije.this);
            }
        });

        Bdalje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sakrijTastaturu();
                asocijacijeKontroler.bDalje(Asocijacije.this);
            }
        });

        Cdalje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sakrijTastaturu();
                asocijacijeKontroler.cDalje(Asocijacije.this);
            }
        });

        Ddalje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sakrijTastaturu();
                asocijacijeKontroler.dDalje(Asocijacije.this);
            }
        });

        konacnoDalje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sakrijTastaturu();
                asocijacijeKontroler.konacnoDalje(Asocijacije.this);
            }
        });
    }


    public void zakljucajDugmad(){
        for(int i = 0;i<polje.length;i++){
            for(int j = 0;j<polje[i].length;j++){
                polje[i][j].setClickable(false);
            }
        }
    }

    public void otkljucajDugmad(ArrayList<ArrayList<Integer>> listaListi){
        for(int i = 0;i<polje.length;i++){
            for(int j = 0;j<polje[i].length;j++){
                polje[i][j].setClickable(true);
            }
        }

        for(int i = 0;i<listaListi.size();i++){
            for(Integer broj:listaListi.get(i)){
                polje[i][broj-1].setClickable(false);
            }
        }

    }

    public void prikaziPreostaloVreme(Long preostaloVreme) {
        vreme.setText(Long.toString(preostaloVreme / 1000));
    }


    public Button dobaviPolje(int red, int kolona){
        return polje[red][kolona];
    }

    public void otkljucajUpise(){
        aOdgovor.setFocusable(true);
        aOdgovor.setFocusableInTouchMode(true);
        aOdgovor.setClickable(true);

        bOdgovor.setFocusable(true);
        bOdgovor.setFocusableInTouchMode(true);
        bOdgovor.setClickable(true);

        cOdgovor.setFocusable(true);
        cOdgovor.setFocusableInTouchMode(true);
        cOdgovor.setClickable(true);

        dOdgovor.setFocusable(true);
        dOdgovor.setFocusableInTouchMode(true);
        dOdgovor.setClickable(true);

        konacanOdgovor.setFocusable(true);
        konacanOdgovor.setFocusableInTouchMode(true);
        konacanOdgovor.setClickable(true);


        Adalje.setClickable(true);
        Bdalje.setClickable(true);
        Cdalje.setClickable(true);
        Ddalje.setClickable(true);
        konacnoDalje.setClickable(true);
    }

    public void zakljucajA(){
        aOdgovor.setFocusable(false);
        aOdgovor.setFocusableInTouchMode(false);
        aOdgovor.setClickable(false);
        Adalje.setClickable(false);
    }

    public void zakljucajB(){
        bOdgovor.setFocusable(false);
        bOdgovor.setFocusableInTouchMode(false);
        bOdgovor.setClickable(false);
        Bdalje.setClickable(false);
    }

    public void zakljucajC(){
        cOdgovor.setFocusable(false);
        cOdgovor.setFocusableInTouchMode(false);
        cOdgovor.setClickable(false);
        Cdalje.setClickable(false);
    }

    public void zakljucajD(){
        dOdgovor.setFocusable(false);
        dOdgovor.setFocusableInTouchMode(false);
        dOdgovor.setClickable(false);
        Ddalje.setClickable(false);
    }

    public void zakljucajKonacno(){
        konacanOdgovor.setFocusable(false);
        konacanOdgovor.setFocusableInTouchMode(false);
        konacanOdgovor.setClickable(false);
        konacnoDalje.setClickable(false);
    }

    public void zakljucajUpise(){
        zakljucajA();
        zakljucajB();
        zakljucajC();
        zakljucajD();
        zakljucajKonacno();
    }

    public EditText dobaviAOdgovor(){
        return aOdgovor;
    }
    public EditText dobaviBOdgovor(){
        return bOdgovor;
    }
    public EditText dobaviCOdgovor(){
        return cOdgovor;
    }
    public EditText dobaviDOdgovor(){
        return dOdgovor;
    }

    public EditText dobaviKonacanOdgovor(){
        return konacanOdgovor;
    }

    public String dobaviATekst(){
        return aOdgovor.getText().toString();
    }

    public void postaviATekst(String tekst){
        aOdgovor.setText(tekst);
    }

    public String dobaviBTekst(){
        return bOdgovor.getText().toString();
    }

    public void postaviBTekst(String tekst){
        bOdgovor.setText(tekst);
    }

    public String dobaviCTekst(){
        return cOdgovor.getText().toString();
    }

    public void postaviCTekst(String tekst){
        cOdgovor.setText(tekst);
    }

    public String dobaviDTekst(){
        return dOdgovor.getText().toString();
    }

    public void postaviDTekst(String tekst){
        dOdgovor.setText(tekst);
    }

    public String dobaviKonacnoTekst(){
        return konacanOdgovor.getText().toString();
    }

    public void postaviKonacnoTekst(String tekst){
        konacanOdgovor.setText(tekst);
    }

    public void prikaziPrekrivac(){
        glavniLejaut = findViewById(android.R.id.content);

        LayoutInflater inflater = LayoutInflater.from(this);
        prekrivac = inflater.inflate(R.layout.prekrivac, glavniLejaut, false);

        prekrivac.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        glavniLejaut.addView(prekrivac);
    }

    public void skloniPrekrivac(){
        if (prekrivac != null) {
            glavniLejaut.removeView(prekrivac);
            prekrivac = null;
        }
    }

    public void promeniBoju(Button dugme, int idBoje) {
        dugme.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Asocijacije.this, idBoje)));
        dugme.setTextColor(ContextCompat.getColor(Asocijacije.this, R.color.white));
    }

    public void promeniBojuUnosa(EditText editText, int idBoje) {
        editText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Asocijacije.this, idBoje)));
        editText.setTextColor(ContextCompat.getColor(Asocijacije.this, R.color.white));
    }


    public void sakrijTastaturu(){
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void postaviNameruIzlaska(Boolean vrednost){
        this.nameraIzlaska = vrednost;
    }


}