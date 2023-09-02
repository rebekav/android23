package com.example.slagalica.Activities.KorisnikActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.slagalica.Activities.OstaliActivities.JedanIgrac;
import com.example.slagalica.Kontroleri.KontrolerDijaloga;
import com.example.slagalica.Kontroleri.KontrolerKorisnika;
import com.example.slagalica.R;


public class MainActivity extends AppCompatActivity {

    private ImageButton igraj, prijava, registracija;

    private KontrolerKorisnika kontrolerKorisnika = new KontrolerKorisnika();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        igraj = findViewById(R.id.igraj);
        prijava = findViewById(R.id.prijava);
        registracija = findViewById(R.id.registracija);

        igraj.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), JedanIgrac.class);
                startActivity(intent);
            }
        });

        prijava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(getApplicationContext(), Prijava.class);
                startActivity(intent);
            }
        });

        registracija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Registracija.class);
                startActivity(intent);
            }
        });

    }


    @Override
    public void onStart(){
        super.onStart();
        kontrolerKorisnika.daLiSiPrijavljen(MainActivity.this, RegistrovanKorisnik.class);
    }

    @Override
    public void onBackPressed() {
        KontrolerDijaloga kontrolerDijaloga = new KontrolerDijaloga();
        kontrolerDijaloga.prikaziDijalogZaZatvaranje(MainActivity.this);
    }
}