package com.example.slagalica.Activities.OstaliActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.example.slagalica.Kontroleri.KontrolerKonekcije;
import com.example.slagalica.Kontroleri.KontrolerKorisnika;
import com.example.slagalica.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class Prijatelji extends AppCompatActivity {

    private KontrolerKonekcije kontrolerKonekcije;
    private ImageButton nazad;
    private EditText pretragaEditText;
    private Button nadjiButton;
    LinearLayout listaPrijatelja;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    FirebaseUser korisnik = mAuth.getCurrentUser();;
    String korisnikovId = korisnik.getUid();
    DatabaseReference korisniciRef = FirebaseDatabase.getInstance().getReference("koren").child("korisnici");
    DatabaseReference trenutniKorisnikRef = korisniciRef.child(korisnikovId);

    private KontrolerKorisnika kontrolerKorisnika = new KontrolerKorisnika();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prijatelji);

        kontrolerKonekcije = new KontrolerKonekcije();

        nazad = findViewById(R.id.nazad);
        listaPrijatelja = findViewById(R.id.listaPrijatelja);
        pretragaEditText = findViewById(R.id.pretraga);
        nadjiButton = findViewById(R.id.nadji);

        DatabaseReference prijateljRef = trenutniKorisnikRef.child("prijatelji");

        prijateljRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot prijateljSnapshot : snapshot.getChildren()){
                    String kljuc = prijateljSnapshot.getKey();
                    String prijateljId = prijateljSnapshot.getValue(String.class);
                    dodajKorisnika(prijateljId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        nazad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        nadjiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pretraga = pretragaEditText.getText().toString();
                if (!pretraga.isEmpty()) {
                    pretragaPoKorisnickomImenu(pretraga);
                } else {
                    Toast.makeText(Prijatelji.this, "pretražite korisnike po imenu", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pretragaEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String query = pretragaEditText.getText().toString();
                    pretragaIDodavanjePrijatelja(query);
                    return true;
                }
                return false;
            }
        });
    }

    private void dodajPrijatelja(String idPrijatelja) {
        DatabaseReference referencaPrijateljaTrenutnogKorisnika = FirebaseDatabase.getInstance().getReference("koren")
                .child("korisnici")
                .child(korisnik.getUid())
                .child("prijatelji");
        referencaPrijateljaTrenutnogKorisnika.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long brojPrijatelja = dataSnapshot.getChildrenCount();
                final long sledeciBrojPrijatelja = brojPrijatelja + 1;

                for(DataSnapshot trenutanPrijatelj:dataSnapshot.getChildren()){
                    if(trenutanPrijatelj.getValue(String.class).equals(idPrijatelja)){
                        Toast.makeText(Prijatelji.this, "ovaj igrac se trenutno nalazi u listi vasih prijatelja", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                DatabaseReference referencaPrijatelja = FirebaseDatabase.getInstance().getReference("koren").child("korisnici").child(idPrijatelja).child("prijatelji");
                referencaPrijatelja.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DatabaseReference novaReferencaPrijatelja = referencaPrijateljaTrenutnogKorisnika.child(String.valueOf(sledeciBrojPrijatelja));
                        novaReferencaPrijatelja.setValue(idPrijatelja);
                        long brojPrijateljaOdPrijatelja = snapshot.getChildrenCount();
                        DatabaseReference referencaZaTebeKodPrijatelja = referencaPrijatelja.child(String.valueOf(brojPrijateljaOdPrijatelja+1));
                        referencaZaTebeKodPrijatelja.setValue(korisnikovId);
                        Toast.makeText(Prijatelji.this, "dodali ste novog prijatelja", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void dodajKorisnika(String korisnikId) {
        View prikazPrijateljaView = LayoutInflater.from(this).inflate(R.layout.prikaz_prijatelja, null);
        ImageView profilnaSlika = prikazPrijateljaView.findViewById(R.id.profilnaSlika);
        TextView korisnickoIme = prikazPrijateljaView.findViewById(R.id.korisnickoime);
        Button igrajDugme = prikazPrijateljaView.findViewById(R.id.pozoviUIgru);

        DatabaseReference posmatraniKorisnik = FirebaseDatabase.getInstance().getReference("koren").child("korisnici").child(korisnikId);
        posmatraniKorisnik.child("profilnaSlika").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Object refSlikeObj = dataSnapshot.getValue();

                    if (refSlikeObj instanceof String) {
                        String refSlike = (String) refSlikeObj;
                        Picasso.get().load(refSlike).into(profilnaSlika);
                    } else {
                        Picasso.get().load(R.drawable.dugme33).into(profilnaSlika);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        posmatraniKorisnik.child("korisnickoIme").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String korisnickoImeVrednost = dataSnapshot.getValue(String.class);
                    korisnickoIme.setText(korisnickoImeVrednost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        igrajDugme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DvaIgraca.class);
                intent.putExtra("IGRA_SA_PRIJATELJEM",true);
                intent.putExtra("ID_PRIJATELJA",korisnikId);
                startActivity(intent);
                finish();
            }
        });

        listaPrijatelja.addView(prikazPrijateljaView);
    }

    private void pretragaPoKorisnickomImenu(String korisnickoIme) {
        Query query = korisniciRef.orderByChild("korisnickoIme").equalTo(korisnickoIme);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaPrijatelja.removeAllViews();

                for (DataSnapshot korisnikSnapshot : snapshot.getChildren()) {
                    String pronadjeniKorisnikId = korisnikSnapshot.getKey();
                    if (!pronadjeniKorisnikId.equals(korisnik.getUid())) {
                        dodajPronadjenogIgraca(pronadjeniKorisnikId);
                    } else {
                        Toast.makeText(Prijatelji.this, "Ne možete dodati sebe za prijatelja", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void pretragaIDodavanjePrijatelja(String korisnickoIme) {
        Query upit = korisniciRef.orderByChild("korisnickoIme").equalTo(korisnickoIme);
        upit.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot korisnikSnapshot : snapshot.getChildren()) {
                    String pronadjeniKorisnikId = korisnikSnapshot.getKey();
                    if (!pronadjeniKorisnikId.equals(korisnik.getUid())) {
                        dodajPrijatelja(pronadjeniKorisnikId);
                    } else {
                        Toast.makeText(Prijatelji.this, "ne možete dodati sami sebe za prijatelja", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void dodajPronadjenogIgraca(String korisnikId) {
        View prikazIgracaView = LayoutInflater.from(this).inflate(R.layout.prijatelj, null);
        ImageView profilnaSlika = prikazIgracaView.findViewById(R.id.profilnaSlika);
        TextView korisnickoIme = prikazIgracaView.findViewById(R.id.korisnickoime);
        Button dodajDugme = prikazIgracaView.findViewById(R.id.dodaj);

        DatabaseReference posmatraniKorisnik = FirebaseDatabase.getInstance().getReference("koren").child("korisnici").child(korisnikId);
        posmatraniKorisnik.child("profilnaSlika").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String refSlike = dataSnapshot.getValue(String.class);
                    Picasso.get().load(refSlike).into(profilnaSlika);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        posmatraniKorisnik.child("korisnickoIme").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String korisnickoImeVrednost = dataSnapshot.getValue(String.class);
                    korisnickoIme.setText(korisnickoImeVrednost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dodajDugme.setOnClickListener(v -> dodajPrijatelja(korisnikId));

        listaPrijatelja.addView(prikazIgracaView);
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