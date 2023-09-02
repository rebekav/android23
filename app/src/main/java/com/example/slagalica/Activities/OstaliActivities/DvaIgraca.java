package com.example.slagalica.Activities.OstaliActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.slagalica.Activities.IgriceActivities.Asocijacije;
import com.example.slagalica.Activities.IgriceActivities.KorakPoKorak;
import com.example.slagalica.Activities.IgriceActivities.Spojnice;
import com.example.slagalica.Activities.KorisnikActivities.RegistrovanKorisnik;
import com.example.slagalica.Kontroleri.KontrolerDijaloga;
import com.example.slagalica.Kontroleri.KontrolerKonekcije;
import com.example.slagalica.R;

public class DvaIgraca extends AppCompatActivity {

    private ImageButton spojnice, asocijacije, korakpokorak, nazad;
    private TextView spojnicePoeni1, spojnicePoeni2, asocijacijePoeni1, asocijacijePoeni2, korakpokorakPoeni1, korakpokorakPoeni2;

    private TextView spojniceTekst, asocijacijeTekst, korakPoKorakTekst;
    private TextView poeni1, poeni2;
    private KontrolerDijaloga kontrolerDijaloga;

    private KontrolerKonekcije kontrolerKonekcije;

    private Handler handler;

    private String idIgre;

    private String narednaIgra;

    private Boolean namjeraIzlaska = true, krajIgre = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dva_igaraca);

        nazad = findViewById(R.id.nazad);
        spojnice = findViewById(R.id.spojnice);
        asocijacije = findViewById(R.id.asocijacije);
        korakpokorak = findViewById(R.id.korakpokorak);

        spojniceTekst = findViewById(R.id.spojniceText);
        asocijacijeTekst = findViewById(R.id.asocijacijeText);
        korakPoKorakTekst = findViewById(R.id.korakpokorakText);

        spojnicePoeni1 = findViewById(R.id.spojnicePoeni1);
        spojnicePoeni2 = findViewById(R.id.spojnicePoeni2);
        asocijacijePoeni1 = findViewById(R.id.asocijacijePoeni1);
        asocijacijePoeni2 = findViewById(R.id.asocijacijePoeni2);
        korakpokorakPoeni1 = findViewById(R.id.korakpokorakPoeni1);
        korakpokorakPoeni2 = findViewById(R.id.korakpokorakPoeni2);
        poeni1 = findViewById(R.id.ja);
        poeni2 = findViewById(R.id.protivnik);


        kontrolerDijaloga = new KontrolerDijaloga();
        kontrolerKonekcije = new KontrolerKonekcije();
        handler = new Handler();

        nazad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        spojnice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                narednaIgra = "Asocijacije";
                namjeraIzlaska = false;
                Intent intent = new Intent(getApplicationContext(), Spojnice.class);
                intent.putExtra("IGRA_ID",idIgre);
                intent.putExtra("BROJ_IGRACA",2);
                intent.putExtra("REDNI_BROJ_PARTIJE",1);
                startActivity(intent);
            }
        });

        asocijacije.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                narednaIgra = "KorakPoKorak";
                namjeraIzlaska = false;
                Intent intent = new Intent(getApplicationContext(), Asocijacije.class);
                intent.putExtra("IGRA_ID",idIgre);
                intent.putExtra("BROJ_IGRACA",2);
                intent.putExtra("REDNI_BROJ_PARTIJE",1);
                startActivity(intent);
            }
        });

        korakpokorak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                narednaIgra = "Kraj";
                namjeraIzlaska = false;
                Intent intent = new Intent(getApplicationContext(), KorakPoKorak.class);
                intent.putExtra("IGRA_ID",idIgre);
                intent.putExtra("BROJ_IGRACA",2);
                intent.putExtra("REDNI_BROJ_PARTIJE",1);
                startActivity(intent);
            }
        });
        inicijalizujIgruDvaIgraca();
        azurirajDugmad();
    }


    @Override
    public void onPause(){
        super.onPause();
        this.kontrolerDijaloga.iskljuciDijalog();
    }

    @Override
    public void onResume(){
        super.onResume();
        azurirajDugmad();
        namjeraIzlaska = true;
        this.kontrolerKonekcije.ucitajPoeneZaDvaIgraca(DvaIgraca.this, idIgre);
    }

    @Override
    public void onDestroy(){
        this.kontrolerKonekcije.dobaviListenerManager().removeAllListeners();
        this.handler.removeCallbacksAndMessages(null);
        if(namjeraIzlaska == true || krajIgre ==true){
            kontrolerKonekcije.obrisiPodatkeIgre(idIgre);
        }
        super.onDestroy();
    }



    public void inicijalizujIgruDvaIgraca(){
        if(getIntent().getBooleanExtra("POZVAN",false) == true){
            this.idIgre = getIntent().getStringExtra("IGRA_ID");
            postaviListenerZaNagliPrekidIgre();
            this.kontrolerKonekcije.obrisiTokenZaPocetakIgre(this.idIgre);
        } else {
            this.kontrolerDijaloga.prikaziDijalogZaUcitavanje(DvaIgraca.this, "kreiranje igre", "sačekajte...");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    kontrolerDijaloga.iskljuciDijalog();
                    kontrolerKonekcije.unistiIgruZaDvaIgraca(DvaIgraca.this, idIgre, "prošlo je vreme za učitavanje igre");
                }
            };
            this.handler.postDelayed(runnable, 10000);

            if(getIntent().getBooleanExtra("IGRA_SA_PRIJATELJEM",false) == true){
                kontrolerKonekcije.kreirajIgruZaDvaIgraca(DvaIgraca.this,getIntent().getStringExtra("ID_PRIJATELJA"));
            } else {
                kontrolerKonekcije.kreirajIgruZaDvaIgraca(DvaIgraca.this,null);
            }
        }
    }

    public void postaviIdIgre(String idIgre){
        this.idIgre = idIgre;
    }

    public void postaviListenerZaNagliPrekidIgre(){
        kontrolerKonekcije.postaviListenerZaNagliPrekidIgre(DvaIgraca.this,this.idIgre);
    }

    public void azurirajPoeneIgraca(Integer spojniceP, Integer asocijacijeP, Integer korakPoKorakP, Integer spojniceP2, Integer asocijacijeP2, Integer korakPoKorakP2){

        Integer zbir1 = 0;
        Integer zbir2 = 0;

        if(spojniceP != null){
            spojnicePoeni1.setText(Integer.toString(spojniceP));
            zbir1+=spojniceP;
        }
        if(spojniceP2 != null){
            spojnicePoeni2.setText(Integer.toString(spojniceP2));
            zbir2+=spojniceP2;
        }
        if(asocijacijeP != null){
            asocijacijePoeni1.setText(Integer.toString(asocijacijeP));
            zbir1+=asocijacijeP;
        }
        if(asocijacijeP2 != null){
            asocijacijePoeni2.setText(Integer.toString(asocijacijeP2));
            zbir2+=asocijacijeP2;
        }
        if(korakPoKorakP != null){
            korakpokorakPoeni1.setText(Integer.toString(korakPoKorakP));
            zbir1+=korakPoKorakP;
        }
        if(korakPoKorakP2 != null){
            korakpokorakPoeni2.setText(Integer.toString(korakPoKorakP2));
            zbir2+=korakPoKorakP2;
        }

        poeni1.setText(Integer.toString(zbir1));
        poeni2.setText(Integer.toString(zbir2));

    }

    public void pokreniIgruDvaIgraca(String idIgre){
        this.kontrolerDijaloga.iskljuciDijalog();
        this.handler.removeCallbacksAndMessages(null);
        this.kontrolerKonekcije.obrisiTokenZaPocetakIgre(this.idIgre);
        postaviListenerZaNagliPrekidIgre();
    }

    public void zakljucajSvaDugmad(){
        spojnice.setFocusable(false);
        spojnice.setFocusableInTouchMode(false);
        spojnice.setClickable(false);
        spojniceTekst.setTextColor(ContextCompat.getColor(DvaIgraca.this, R.color.crna));

        asocijacije.setFocusable(false);
        asocijacije.setFocusableInTouchMode(false);
        asocijacije.setClickable(false);
        asocijacijeTekst.setTextColor(ContextCompat.getColor(DvaIgraca.this, R.color.crna));

        korakpokorak.setFocusable(false);
        korakpokorak.setFocusableInTouchMode(false);
        korakpokorak.setClickable(false);
        korakPoKorakTekst.setTextColor(ContextCompat.getColor(DvaIgraca.this, R.color.crna));

    }

    public void azurirajDugmad(){
        zakljucajSvaDugmad();
        if(narednaIgra == null){
            spojnice.setFocusable(true);
            spojnice.setFocusableInTouchMode(true);
            spojnice.setClickable(true);
            spojniceTekst.setTextColor(ContextCompat.getColor(DvaIgraca.this, R.color.dvaIgracaDugmeTekst));
        } else if(narednaIgra.equals("Asocijacije")){
            asocijacije.setFocusable(true);
            asocijacije.setFocusableInTouchMode(true);
            asocijacije.setClickable(true);
            asocijacijeTekst.setTextColor(ContextCompat.getColor(DvaIgraca.this, R.color.dvaIgracaDugmeTekst));
        } else if(narednaIgra.equals("KorakPoKorak")){
            korakpokorak.setFocusable(true);
            korakpokorak.setFocusableInTouchMode(true);
            korakpokorak.setClickable(true);
            korakPoKorakTekst.setTextColor(ContextCompat.getColor(DvaIgraca.this, R.color.dvaIgracaDugmeTekst));
        } else {
            kontrolerKonekcije.dobaviListenerManager().removeAllListeners();
            kontrolerKonekcije.proglasiKrajIgreIUpisiBodove(DvaIgraca.this,this.idIgre);
            krajIgre = true;
        }
    }

    public void porukaOPobedi(){
        Toast.makeText(DvaIgraca.this, "čestitamo, pobedili ste!", Toast.LENGTH_SHORT).show();
    }

    public void porukaOPorazu(){
        Toast.makeText(DvaIgraca.this, "izgubili ste, više sreće drugi put", Toast.LENGTH_SHORT).show();
    }

}