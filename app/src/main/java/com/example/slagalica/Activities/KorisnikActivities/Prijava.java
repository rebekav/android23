package com.example.slagalica.Activities.KorisnikActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.slagalica.Kontroleri.KontrolerKorisnika;
import com.example.slagalica.R;

public class Prijava extends AppCompatActivity {
    private ImageButton nazad, prijava, registracija;
    private EditText email, lozinka;
    private KontrolerKorisnika kontrolerKorisnika = new KontrolerKorisnika();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prijava);

        nazad = findViewById(R.id.nazad);
        prijava = findViewById(R.id.prijava);
        registracija = findViewById(R.id.registracija);

        email = findViewById(R.id.emaililiime);
        lozinka = findViewById(R.id.lozinka);

        nazad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        registracija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Registracija.class);
                startActivity(intent);
            }
        });

        prijava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String unetEmail = email.getText().toString().trim();
                String unetaLozinka = lozinka.getText().toString();
                kontrolerKorisnika.prijaviIgraca(unetEmail,unetaLozinka,Prijava.this, RegistrovanKorisnik.class);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        kontrolerKorisnika.daLiSiPrijavljen(Prijava.this, RegistrovanKorisnik.class);
    }
}