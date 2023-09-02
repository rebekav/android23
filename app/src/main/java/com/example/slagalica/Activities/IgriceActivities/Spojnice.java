package com.example.slagalica.Activities.IgriceActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.slagalica.Activities.KorisnikActivities.RegistrovanKorisnik;
import com.example.slagalica.Activities.OstaliActivities.JedanIgrac;
import com.example.slagalica.Kontroleri.KontrolerKonekcije;
import com.example.slagalica.Kontroleri.SpojniceKontroler;
import com.example.slagalica.R;

public class Spojnice extends AppCompatActivity {

    private ImageButton[] levaKolona, desnaKolona;
    private TextView[] leviPojmovi, desniPojmovi;

    private TextView tajmer, pitanje;

    private Integer izabranLeviPojam;

    private SpojniceKontroler spojniceKontroler;

    private KontrolerKonekcije kontrolerKonekcije;

    private ViewGroup glavniLejaut;

    private View prekrivac;

    private Boolean nameraIzlaska = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spojnice);

        levaKolona = new ImageButton[] {
                findViewById(R.id.spojnica1),
                findViewById(R.id.spojnica2),
                findViewById(R.id.spojnica3),
                findViewById(R.id.spojnica4),
                findViewById(R.id.spojnica5)
        };

        desnaKolona = new ImageButton[] {
                findViewById(R.id.spojnica6),
                findViewById(R.id.spojnica7),
                findViewById(R.id.spojnica8),
                findViewById(R.id.spojnica9),
                findViewById(R.id.spojnica10)
        };

        leviPojmovi = new TextView[] {
                findViewById(R.id.spojnica1Text),
                findViewById(R.id.spojnica2Text),
                findViewById(R.id.spojnica3Text),
                findViewById(R.id.spojnica4Text),
                findViewById(R.id.spojnica5Text)
        };

        desniPojmovi = new TextView[] {
                findViewById(R.id.spojnica6Text),
                findViewById(R.id.spojnica7Text),
                findViewById(R.id.spojnica8Text),
                findViewById(R.id.spojnica9Text),
                findViewById(R.id.spojnica10Text)
        };
        pitanje = findViewById(R.id.pitanje);
        tajmer = findViewById(R.id.tajmer);

        izabranLeviPojam = -1;
        spojniceKontroler = new SpojniceKontroler(getIntent().getStringExtra("IGRA_ID"), getIntent().getIntExtra("BROJ_IGRACA", 1), getIntent().getIntExtra("REDNI_BROJ_PARTIJE", 1));
        spojniceKontroler.kreirajSpojnice(Spojnice.this);
        kontrolerKonekcije = new KontrolerKonekcije();
        kontrolerKonekcije.postaviListenerZaNagliPrekidIgre(Spojnice.this,getIntent().getStringExtra("IGRA_ID"));

        for (int i = 0; i < 5; i++) {
            final int broj = i;

            desnaKolona[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (izabranLeviPojam == -1) {
                        Toast.makeText(Spojnice.this, "morate kliknuti prvo na pojam levo", Toast.LENGTH_SHORT).show();
                    }else{
                        spojniceKontroler.proveriOdgovor(Spojnice.this, izabranLeviPojam, broj);
                        postaviIzabranLeviPojam(-1);
                    }
                }
            });
        }

        for (int i = 0; i < 5; i++) {
            final int broj = i;
            levaKolona[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postaviIzabranLeviPojam(broj);
                    spojniceKontroler.kontrolerZaLeveDugmadi(Spojnice.this,broj);
                }
            });
        }
    }

    @Override
    public void onBackPressed(){
        if(nameraIzlaska == true && getIntent().getIntExtra("BROJ_IGRACA", 1) ==2){
            Intent intent = new Intent(Spojnice.this, RegistrovanKorisnik.class);
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
        spojniceKontroler.iskljuciTajmer();
        spojniceKontroler.dobaviListenerManager().removeAllListeners();
    }

    @Override
    public void onDestroy() {
        spojniceKontroler.iskljuciTajmer();
        spojniceKontroler.dobaviListenerManager().removeAllListeners();
        kontrolerKonekcije.dobaviListenerManager().removeAllListeners();
        if(nameraIzlaska == true){
            kontrolerKonekcije.obrisiPodatkeIgre(getIntent().getStringExtra("IGRA_ID"));
        }
        super.onDestroy();
    }


    public void postaviIzabranLeviPojam(Integer vrednost){
        izabranLeviPojam = vrednost;
    }


    public void obojiLeviPojam(String boja, Integer levo){
        if(boja.equals("plava")){
            leviPojmovi[levo].setTextColor(ContextCompat.getColor(Spojnice.this, R.color.plava));
        } else if (boja.equals("crvena")) {
            leviPojmovi[levo].setTextColor(ContextCompat.getColor(Spojnice.this, R.color.crvena));
        } else if (boja.equals("crna")){
            leviPojmovi[levo].setTextColor(ContextCompat.getColor(Spojnice.this, R.color.crna));
        } else if (boja.equals("spojniceTekst")){
            leviPojmovi[levo].setTextColor(ContextCompat.getColor(Spojnice.this, R.color.spojniceTekst));
        }
    }

    public void obojiPar(String boja, Integer levo, Integer desno){
        if(boja.equals("plava")){
            levaKolona[levo].setClickable(false);
            leviPojmovi[levo].setTextColor(ContextCompat.getColor(Spojnice.this, R.color.plava));

            desnaKolona[desno].setClickable(false);
            desniPojmovi[desno].setTextColor(ContextCompat.getColor(Spojnice.this, R.color.plava));
        } else if (boja.equals("crvena")) {
            levaKolona[levo].setClickable(false);
            leviPojmovi[levo].setTextColor(ContextCompat.getColor(Spojnice.this, R.color.crvena));

            desnaKolona[desno].setClickable(false);
            desniPojmovi[desno].setTextColor(ContextCompat.getColor(Spojnice.this, R.color.crvena));
        }
    }

    public void otkljucajLevoDugme(Integer broj){
        levaKolona[broj].setClickable(true);
    }

    public void zakljucajLevoDugme(Integer broj){
        levaKolona[broj].setClickable(false);
    }

    public void postaviTekstZaLeviPojam(Integer broj, String tekst){
        leviPojmovi[broj].setText(tekst);
    }


    public void postaviTekstZaDesniPojam(Integer broj, String tekst){
        desniPojmovi[broj].setText(tekst);
    }

    public void postaviTekstZaPitanje(String tekst){
        pitanje.setText(tekst);
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

    public void postaviNameruIzlaska(Boolean vrednost){
        this.nameraIzlaska = vrednost;
    }

}