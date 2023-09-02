package com.example.slagalica.Activities.KorisnikActivities;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.slagalica.Activities.OstaliActivities.DvaIgraca;
import com.example.slagalica.Activities.OstaliActivities.JedanIgrac;
import com.example.slagalica.Activities.OstaliActivities.Prijatelji;
import com.example.slagalica.Activities.OstaliActivities.Profil;
import com.example.slagalica.Activities.OstaliActivities.RangLista;
import com.example.slagalica.Kontroleri.KontrolerDijaloga;
import com.example.slagalica.Kontroleri.KontrolerKonekcije;
import com.example.slagalica.Kontroleri.KontrolerKorisnika;
import com.example.slagalica.R;


public class RegistrovanKorisnik extends AppCompatActivity {

    private ImageButton jedanIgrac, dvaIgraca, prijatelji, profil, rangLista;
    private Button odjava;
    private TextView brojZvezda, brojTokena, brojRanga;

    private KontrolerKorisnika kontrolerKorisnika = new KontrolerKorisnika();

    private KontrolerKonekcije kontrolerKonekcije = new KontrolerKonekcije();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrovan_korisnik);

        odjava = findViewById(R.id.odjava);
        jedanIgrac = findViewById(R.id.jedanIgrac);
        dvaIgraca = findViewById(R.id.dvaIgraca);
        prijatelji = findViewById(R.id.prijatelji);
        profil = findViewById(R.id.profil);
        rangLista = findViewById(R.id.rangLista);

        brojZvezda = findViewById(R.id.brojZvezda);
        brojTokena = findViewById(R.id.brojTokena);
        brojRanga = findViewById(R.id.brojRanga);

        odjava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kontrolerKorisnika.odjaviKorisnika(RegistrovanKorisnik.this);
            }
        });

        jedanIgrac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), JedanIgrac.class);
                startActivity(intent);
            }
        });

        dvaIgraca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DvaIgraca.class);
                startActivity(intent);
            }
        });

        prijatelji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Prijatelji.class);
                startActivity(intent);
            }
        });

        profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Profil.class);
                startActivity(intent);
            }
        });

        rangLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RangLista.class);
                startActivity(intent);
            }
        });

        kontrolerKorisnika.daLiSiPrijavljen(RegistrovanKorisnik.this, Prijava.class);
        kontrolerKorisnika.staviKorisnikaNaListuAktivnih();
        kontrolerKorisnika.zvezdeListener(RegistrovanKorisnik.this);
        kontrolerKorisnika.tokeniListener(RegistrovanKorisnik.this);
        kontrolerKorisnika.rangListener(RegistrovanKorisnik.this);

        if(getIntent().getBooleanExtra("NOVI_KORISNIK",false) == true){
            KontrolerDijaloga kontrolerDijaloga = new KontrolerDijaloga();
            kontrolerDijaloga.tokeniPriRegistraciji(RegistrovanKorisnik.this);
        } else {
            kontrolerKonekcije.dodajTokeneNaDan(RegistrovanKorisnik.this);
        }

        kontrolerKonekcije.ukljuciListenerZaAktivneIgre(RegistrovanKorisnik.this);
        kontrolerKonekcije.ocistiSvePreostaleIgre();
    }


    @Override
    public void onBackPressed() {
        KontrolerDijaloga kontrolerDijaloga = new KontrolerDijaloga();
        kontrolerDijaloga.prikaziDijalogZaZatvaranje(RegistrovanKorisnik.this);
    }

    @Override
    public void onResume(){
        super.onResume();
        kontrolerKorisnika.staviKorisnikaNaListuAktivnih();
    }

    @Override
    public void onPause(){
        super.onPause();
        kontrolerKorisnika.obrisiKorisnikaSaListeAktivnih();
    }

    @Override
    public void onDestroy(){
        kontrolerKonekcije.dobaviListenerManager().removeAllListeners();
        kontrolerKorisnika.dobaviListenerManager().removeAllListeners();
        super.onDestroy();
    }

    public void postaviZvezde(String zvezde){
        brojZvezda.setText(zvezde);
    }

    public void postaviTokene(String tokeni){
        brojTokena.setText(tokeni);
    }
    public void postaviRang(String mesto){ brojRanga.setText(mesto); }

    public void prikaziPozivZaIgru(String idIgre){
        KontrolerDijaloga kontrolerDijaloga = new KontrolerDijaloga();
        kontrolerDijaloga.prikaziPozivZaIgru(RegistrovanKorisnik.this, idIgre);
    }

}