package com.example.slagalica.Activities.OstaliActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import com.example.slagalica.Kontroleri.KontrolerKorisnika;
import com.example.slagalica.R;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RangLista extends AppCompatActivity {
    private ImageButton nazad;
    private LinearLayout rangListaLayout;
    DatabaseReference korisniciRef = FirebaseDatabase.getInstance().getReference("koren").child("korisnici");
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final long AZURIRANJE_INTERVAL = 2 * 60 * 1000;

    private KontrolerKorisnika kontrolerKorisnika;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rang_lista);

        kontrolerKorisnika = new KontrolerKorisnika();
        nazad = findViewById(R.id.nazad);
        rangListaLayout = findViewById(R.id.rangLista);


        nazad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        korisniciRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> rangMapa = new HashMap<>();
                for(DataSnapshot korisnikSnapshot : snapshot.getChildren()){
                    String korisnikId = korisnikSnapshot.getKey();
                    Integer brzvezda = korisnikSnapshot.child("zvezde").getValue(Integer.class);
                    rangMapa.put(korisnikId, brzvezda);
                }
                List<Map.Entry<String, Integer>> listaZaSortiranje = new ArrayList<>(rangMapa.entrySet());
                Collections.sort(listaZaSortiranje, new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });
                for (Map.Entry<String, Integer> entry : listaZaSortiranje) {
                    String korisnikId = entry.getKey();
                    Integer brzvezda = entry.getValue();

                    dodajURangListu(korisnikId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        azurirajRangListu();
        pokreniPeriodicnoAzuriranje();
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

    private void dodajURangListu(String korisnikId) {
        View prikazRangaView = LayoutInflater.from(this).inflate(R.layout.prikaz_rang, null);
        ImageView profilnaSlika = prikazRangaView.findViewById(R.id.profilnaSlika);
        TextView korisnickoIme = prikazRangaView.findViewById(R.id.korisnickoime);
        TextView brojZvezda = prikazRangaView.findViewById(R.id.brojZvezda);

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

        posmatraniKorisnik.child("zvezde").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer zvezdeVrednost = dataSnapshot.getValue(Integer.class);
                    String zvezdestr = zvezdeVrednost.toString();
                    brojZvezda.setText(zvezdestr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rangListaLayout.addView(prikazRangaView);
    }

    private void pokreniPeriodicnoAzuriranje() {
        runnable = new Runnable() {
            @Override
            public void run() {
                azurirajRangListu();
                handler.postDelayed(this, AZURIRANJE_INTERVAL);
            }
        };
        handler.postDelayed(runnable, AZURIRANJE_INTERVAL);
    }

    private void azurirajRangListu() {
        DatabaseReference korisniciRef = FirebaseDatabase.getInstance().getReference("koren").child("korisnici");

        korisniciRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> rangMapa = new HashMap<>();
                for(DataSnapshot korisnikSnapshot : snapshot.getChildren()){
                    String korisnikId = korisnikSnapshot.getKey();
                    Integer brzvezda = korisnikSnapshot.child("zvezde").getValue(Integer.class);
                    rangMapa.put(korisnikId, brzvezda);
                }
                List<Map.Entry<String, Integer>> listaZaSortiranje = new ArrayList<>(rangMapa.entrySet());

                Collections.sort(listaZaSortiranje, new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });
                rangListaLayout.removeAllViews();

                for (Map.Entry<String, Integer> entry : listaZaSortiranje) {
                    String korisnikId = entry.getKey();
                    dodajURangListu(korisnikId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}