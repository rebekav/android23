package com.example.slagalica.Activities.OstaliActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.slagalica.Activities.KorisnikActivities.Prijava;
import com.example.slagalica.Kontroleri.KontrolerKorisnika;
import com.example.slagalica.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.Toast;

import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Profil extends AppCompatActivity {
    private ImageView nazad;
    private TextView korisnickoime, email;
    private TextView brojPartijaT, koznaznaBodoviT, spojniceBodoviT, asocijacijeBodoviT, skockoBodoviT, korakpokorakBodoviT, mojbrojBodoviT;
    private TextView pobedeT, poraziT;
    private ImageView profilna;
    private ImageButton promenaSlike;

    private KontrolerKorisnika kontrolerKorisnika = new KontrolerKorisnika();
    private static final int  PICK_IMAGE_REQUEST = 1;

    FirebaseAuth mAuth;
    FirebaseUser korisnik;
    String korisnikId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        nazad = findViewById(R.id.nazad);
        korisnickoime = findViewById(R.id.korisnickoime);
        email = findViewById(R.id.email);
        brojPartijaT = findViewById(R.id.brojPartija);
        koznaznaBodoviT = findViewById(R.id.koznaznaBodovi);
        spojniceBodoviT = findViewById(R.id.spojniceBodovi);
        asocijacijeBodoviT = findViewById(R.id.asocijacijeBodovi);
        skockoBodoviT = findViewById(R.id.skockoBodovi);
        korakpokorakBodoviT = findViewById(R.id.korakpokorakBodovi);
        mojbrojBodoviT = findViewById(R.id.mojbrojBodovi);
        pobedeT = findViewById(R.id.pobede);
        poraziT = findViewById(R.id.porazi);

        profilna = findViewById(R.id.profilna);
        promenaSlike = findViewById(R.id.promenaSlike);
        promenaSlike.setOnClickListener(view -> promeniSliku());

        mAuth = FirebaseAuth.getInstance();
        korisnik = mAuth.getCurrentUser();
        korisnikId = korisnik.getUid();

        nazad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        FirebaseDatabase baza = FirebaseDatabase.getInstance();
        DatabaseReference korisniciRef = baza.getReference("koren").child("korisnici");
        DatabaseReference trenutniKorisnikRef = korisniciRef.child(korisnikId);

        trenutniKorisnikRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Integer brojPartija = snapshot.child("brojPartija").getValue(Integer.class);
                    Integer koznaznaBodovi = snapshot.child("koznaznaBodovi").getValue(Integer.class);
                    Integer spojniceBodovi = snapshot.child("spojniceBodovi").getValue(Integer.class);
                    Integer asocijacijeBodovi = snapshot.child("asocijacijeBodovi").getValue(Integer.class);
                    Integer skockoBodovi = snapshot.child("skockoBodovi").getValue(Integer.class);
                    Integer korakpokorakBodovi = snapshot.child("korakpokorakBodovi").getValue(Integer.class);
                    Integer mojbrojBodovi = snapshot.child("mojbrojBodovi").getValue(Integer.class);
                    Integer pobede = snapshot.child("pobede").getValue(Integer.class);
                    Integer porazi = snapshot.child("porazi").getValue(Integer.class);

                    if (brojPartija != null && brojPartija != 0) {
                        brojPartijaT.setText(String.valueOf(brojPartija));
                        koznaznaBodoviT.setText(String.valueOf(koznaznaBodovi / brojPartija));
                        spojniceBodoviT.setText(String.valueOf(spojniceBodovi / brojPartija));
                        asocijacijeBodoviT.setText(String.valueOf(asocijacijeBodovi / brojPartija));
                        skockoBodoviT.setText(String.valueOf(skockoBodovi / brojPartija));
                        korakpokorakBodoviT.setText(String.valueOf(korakpokorakBodovi / brojPartija));
                        mojbrojBodoviT.setText(String.valueOf(mojbrojBodovi / brojPartija));

                        double procenatPobede = (double) pobede / brojPartija * 100;
                        double procenatPoraza = (double) porazi / brojPartija * 100;

                        int zaokruzeniProcenatPobede = (int) Math.round(procenatPobede);
                        int zaokruzeniProcenatPoraza = (int) Math.round(procenatPoraza);

                        pobedeT.setText(String.valueOf(zaokruzeniProcenatPobede) + "%");
                        poraziT.setText(String.valueOf(zaokruzeniProcenatPoraza) + "%");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (korisnik == null) {
            Intent intent = new Intent(getApplicationContext(), Prijava.class);
            startActivity(intent);
            finish();
        } else {
            email.setText(korisnik.getEmail());

            trenutniKorisnikRef.child("korisnickoIme").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String korisnickoImeVrednost = dataSnapshot.getValue(String.class);
                        korisnickoime.setText(korisnickoImeVrednost);
                    } else {
                        korisnickoime.setText("greška. ne mogu da dobavim korisničko ime");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    korisnickoime.setText("greška pri povezivanju");
                }
            });
            trenutniKorisnikRef.child("profilnaSlika").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String putanjaProfilneSlike = dataSnapshot.getValue(String.class);
                        ImageView imageView = findViewById(R.id.profilna);
                        Picasso.get().load(putanjaProfilneSlike).into(imageView);
                    } else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Profil.this, "greška. ne mogu da dobavim profilnu sliku", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void promeniSliku() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==  PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri Urislike = data.getData();
            ImageView slika = findViewById(R.id.profilna);
            slika.setImageURI(Urislike);

            String korisnikId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference slikaKorisnikaRef = FirebaseStorage.getInstance()
                    .getReference("korisnici/" + korisnikId + "/profilna.jpg");

            UploadTask uploadTask = slikaKorisnikaRef.putFile(Urislike);

            AlertDialog.Builder builder = new AlertDialog.Builder(Profil.this);
            builder.setCancelable(false);
            AlertDialog dijalog = builder.create();
            dijalog.show();

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                slikaKorisnikaRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    dijalog.dismiss();
                    String Urlslike = uri.toString();
                    DatabaseReference korisniciRef = FirebaseDatabase.getInstance().getReference("koren").child("korisnici");
                    DatabaseReference trenutniKorisnikRef = korisniciRef.child(korisnikId);

                    trenutniKorisnikRef.child("profilnaSlika").setValue(Urlslike);
                });
            }).addOnFailureListener(exception -> {
                Toast.makeText(Profil.this, exception.toString(), Toast.LENGTH_SHORT).show();
            });
        }
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
}