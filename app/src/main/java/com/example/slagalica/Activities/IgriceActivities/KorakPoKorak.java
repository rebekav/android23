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
import android.widget.TextView;

import com.example.slagalica.Activities.KorisnikActivities.RegistrovanKorisnik;
import com.example.slagalica.Kontroleri.KontrolerKonekcije;
import com.example.slagalica.Kontroleri.KorakPoKorakKontroler;
import com.example.slagalica.R;

public class KorakPoKorak extends AppCompatActivity {

    private KorakPoKorakKontroler korakPoKorakKontroler;
    private TextView koraci[], tajmer;
    private EditText odgovor;
    private Button provera;

    private ViewGroup glavniLejaut;

    private View prekrivac;

    private Boolean nameraIzlaska = true;

    private KontrolerKonekcije kontrolerKonekcije;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_korak_po_korak);

        koraci = new TextView[] {
                findViewById(R.id.korak1),
                findViewById(R.id.korak2),
                findViewById(R.id.korak3),
                findViewById(R.id.korak4),
                findViewById(R.id.korak5),
                findViewById(R.id.korak6),
                findViewById(R.id.korak7),
        };

        tajmer = findViewById(R.id.tajmer);

        odgovor = findViewById(R.id.odgovor);
        provera = findViewById(R.id.dugmeProvera);

        korakPoKorakKontroler = new KorakPoKorakKontroler(getIntent().getStringExtra("IGRA_ID"), getIntent().getIntExtra("BROJ_IGRACA", 1), getIntent().getIntExtra("REDNI_BROJ_PARTIJE", 1));
        korakPoKorakKontroler.kreirajKorakPoKorak(KorakPoKorak.this);
        kontrolerKonekcije = new KontrolerKonekcije();
        kontrolerKonekcije.postaviListenerZaNagliPrekidIgre(KorakPoKorak.this,getIntent().getStringExtra("IGRA_ID"));

        provera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                korakPoKorakKontroler.proveriOdgovor(KorakPoKorak.this, odgovor.getText().toString());
            }
        });
    }


    @Override
    public void onBackPressed(){
        if(nameraIzlaska == true && getIntent().getIntExtra("BROJ_IGRACA", 1) ==2){
            Intent intent = new Intent(KorakPoKorak.this, RegistrovanKorisnik.class);
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
        korakPoKorakKontroler.iskljuciTajmere();
        korakPoKorakKontroler.dobaviListenerManager().removeAllListeners();
    }


    @Override
    public void onDestroy() {
        korakPoKorakKontroler.iskljuciTajmere();
        korakPoKorakKontroler.dobaviListenerManager().removeAllListeners();
        kontrolerKonekcije.dobaviListenerManager().removeAllListeners();
        if(nameraIzlaska == true){
            kontrolerKonekcije.obrisiPodatkeIgre(getIntent().getStringExtra("IGRA_ID"));
        }
        super.onDestroy();
    }



    public void obrisiUnos(){
        odgovor.setText("");
    }

    public void postaviUnos(String tekst){
        odgovor.setText(tekst);
    }

    public void zabraniUnos(){
        provera.setClickable(false);
        odgovor.setFocusable(false);
        odgovor.setFocusableInTouchMode(false);
        odgovor.setClickable(false);
    }

    public void obojiUnos(String boja){
        if(boja.equals("plava")){
            provera.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(KorakPoKorak.this, R.color.plava)));
            odgovor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(KorakPoKorak.this, R.color.plava)));
            odgovor.setTextColor(ContextCompat.getColor(KorakPoKorak.this, R.color.bela));
        } else if(boja.equals("crvena")){
            provera.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(KorakPoKorak.this, R.color.crvena)));
            odgovor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(KorakPoKorak.this, R.color.crvena)));
            odgovor.setTextColor(ContextCompat.getColor(KorakPoKorak.this, R.color.bela));
        }
    }

    public void otkrijKorak(Integer redniBrojKoraka, String tekst){
        koraci[redniBrojKoraka].setText(tekst);
    }

    public void azurirajVreme(String vreme){
        tajmer.setText(vreme);
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