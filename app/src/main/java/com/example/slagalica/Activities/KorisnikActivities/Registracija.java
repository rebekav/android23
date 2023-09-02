package com.example.slagalica.Activities.KorisnikActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.slagalica.Kontroleri.KontrolerKorisnika;
import com.example.slagalica.R;

public class Registracija extends AppCompatActivity {

    private ImageButton nazad, prijava, registracija;
    private EditText email, korisnickoime, lozinka, ponovljenalozinka;
    private KontrolerKorisnika kontrolerKorisnika = new KontrolerKorisnika();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registracija);

        nazad = findViewById(R.id.nazad);
        prijava = findViewById(R.id.prijava);
        registracija = findViewById(R.id.registracija);

        email = findViewById(R.id.email);
        korisnickoime = findViewById(R.id.korisnickoime);
        lozinka = findViewById(R.id.lozinka);
        ponovljenalozinka = findViewById(R.id.ponovljenalozinka);

        nazad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        prijava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Prijava.class);
                startActivity(intent);
            }
        });

        registracija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String unetEmail = email.getText().toString();
                String unetoKorisnickoIme = korisnickoime.getText().toString();
                String unetaLozinka = lozinka.getText().toString();
                String unetaLozinka2 = ponovljenalozinka.getText().toString();

                kontrolerKorisnika.registrujIgraca(unetEmail,unetaLozinka, unetaLozinka2, unetoKorisnickoIme, Registracija.this, RegistrovanKorisnik.class );
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        kontrolerKorisnika.daLiSiPrijavljen(Registracija.this, RegistrovanKorisnik.class);
    }


}