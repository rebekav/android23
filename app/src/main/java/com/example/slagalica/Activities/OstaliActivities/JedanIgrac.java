package com.example.slagalica.Activities.OstaliActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.slagalica.Activities.IgriceActivities.Asocijacije;
import com.example.slagalica.Activities.IgriceActivities.KorakPoKorak;
import com.example.slagalica.Activities.IgriceActivities.Spojnice;
import com.example.slagalica.Activities.KorisnikActivities.MainActivity;
import com.example.slagalica.Activities.KorisnikActivities.RegistrovanKorisnik;
import com.example.slagalica.Kontroleri.KontrolerKonekcije;
import com.example.slagalica.Kontroleri.KontrolerKorisnika;
import com.example.slagalica.R;

public class JedanIgrac extends AppCompatActivity {

    private ImageButton spojnice, asocijacije, korakpokorak, nazad;
    private TextView spojnicePoeni, asocijacijePoeni, korakpokorakPoeni;

    private TextView ukupnoPoena;
    private KontrolerKonekcije kontrolerKonekcije;

    private KontrolerKorisnika kontrolerKorisnika;
    private String idIgre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jedan_igrac);

        nazad = findViewById(R.id.nazad);
        spojnice = findViewById(R.id.spojnice);
        asocijacije = findViewById(R.id.asocijacije);
        korakpokorak = findViewById(R.id.korakpokorak);

        spojnicePoeni = findViewById(R.id.spojnicePoeni);
        asocijacijePoeni = findViewById(R.id.asocijacijePoeni);
        korakpokorakPoeni = findViewById(R.id.korakpokorakPoeni);

        ukupnoPoena = findViewById(R.id.ja);

        kontrolerKonekcije = new KontrolerKonekcije();
        kontrolerKorisnika = new KontrolerKorisnika();

        nazad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        spojnice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Spojnice.class);
                intent.putExtra("IGRA_ID",idIgre);
                startActivity(intent);
            }
        });

        asocijacije.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Asocijacije.class);
                intent.putExtra("IGRA_ID",idIgre);
                startActivity(intent);
            }
        });

        korakpokorak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), KorakPoKorak.class);
                intent.putExtra("IGRA_ID",idIgre);
                startActivity(intent);
            }
        });
        this.kontrolerKonekcije.kreirajIgruZaJednogIgraca(JedanIgrac.this);
    }

    @Override
    public void onBackPressed(){
        if(this.kontrolerKorisnika.dobaviMAuth().getCurrentUser() != null){
            Intent intent = new Intent(JedanIgrac.this, RegistrovanKorisnik.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(JedanIgrac.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        super.onBackPressed();
    }

    @Override
    public void onResume(){
        super.onResume();
        this.kontrolerKonekcije.ucitajPoeneZaJednogIgraca(JedanIgrac.this, idIgre);
    }

    @Override
    public void onDestroy(){
        this.kontrolerKonekcije.obrisiPodatkeIgre(this.idIgre);
        this.kontrolerKonekcije.dobaviListenerManager().removeAllListeners();
        super.onDestroy();
    }

    public void azurirajPoeneIgraca(Integer spojniceP, Integer asocijacijeP, Integer korakPoKorakP){
        Integer ukupno  = 0;

        if(spojniceP != null){
            spojnicePoeni.setText(Integer.toString(spojniceP));
            ukupno += spojniceP;
        }
        if(asocijacijeP != null){
            asocijacijePoeni.setText(Integer.toString(asocijacijeP));
            ukupno += asocijacijeP;
        }
        if(korakPoKorakP != null){
            korakpokorakPoeni.setText(Integer.toString(korakPoKorakP));
            ukupno += korakPoKorakP;
        }

        ukupnoPoena.setText(Integer.toString(ukupno));
    }
    public void postaviIdIgre(String idIgre){
        this.idIgre = idIgre;
    }


}