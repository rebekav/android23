package com.example.slagalica.Kontroleri;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.slagalica.Activities.KorisnikActivities.MainActivity;
import com.example.slagalica.Activities.KorisnikActivities.Prijava;
import com.example.slagalica.Activities.KorisnikActivities.Registracija;
import com.example.slagalica.Activities.KorisnikActivities.RegistrovanKorisnik;
import com.example.slagalica.PomocneKlase.FirebaseListenerManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KontrolerKorisnika {
    private FirebaseAuth mAuth;
    private DatabaseReference koren;

    private FirebaseListenerManager listenerManager;

    public KontrolerKorisnika(){
        this.listenerManager = new FirebaseListenerManager();
        this.mAuth = FirebaseAuth.getInstance();
        this.koren = FirebaseDatabase.getInstance().getReference("koren");
    }

    public FirebaseListenerManager dobaviListenerManager(){
        return listenerManager;
    }

    public FirebaseAuth dobaviMAuth(){
        return mAuth;
    }

    public void prijaviIgraca(String email, String lozinka, final Activity trenutniActivity, final Class<?> naredniActivity){
        this.mAuth.signInWithEmailAndPassword(email, lozinka).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(trenutniActivity, "uspešno ste se prijavili", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(trenutniActivity,naredniActivity);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    trenutniActivity.startActivity(intent);
                    trenutniActivity.finish();
                }else{
                    Toast.makeText(trenutniActivity, "proverite email ili lozinku", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void odjaviKorisnika(final Activity trenutniActivity){
        obrisiKorisnikaSaListeAktivnih();
        this.mAuth.signOut();
        Toast.makeText(trenutniActivity, "uspešno ste se odjavili", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(trenutniActivity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        trenutniActivity.startActivity(intent);
        trenutniActivity.finish();
    }

    private boolean proveraIspravnostiRegistracije(String unetEmail, String unetoKorisnickoIme, String unetaLozinka, String unetaLozinka2) {
        return !unetEmail.isEmpty() && !unetoKorisnickoIme.isEmpty() && !unetaLozinka.isEmpty() && unetaLozinka.equals(unetaLozinka2);
    }




    public void registrujIgraca(String email, String lozinka, String lozinka2, String korisnickoIme, final Activity trenutniActivity, final Class<?> naredniActivity){
        if(proveraIspravnostiRegistracije(email, korisnickoIme, lozinka, lozinka2) == true){
            this.mAuth.createUserWithEmailAndPassword(email,lozinka).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        DatabaseReference korisnickaReferenca = koren.child("korisnici").child(mAuth.getUid());
                        korisnickaReferenca.child("korisnickoIme").setValue(korisnickoIme);
                        korisnickaReferenca.child("zvezde").setValue(0);
                        korisnickaReferenca.child("tokeni").setValue(5);
                        korisnickaReferenca.child("koznaznaBodovi").setValue(0);
                        korisnickaReferenca.child("spojniceBodovi").setValue(0);
                        korisnickaReferenca.child("asocijacijeBodovi").setValue(0);
                        korisnickaReferenca.child("skockoBodovi").setValue(0);
                        korisnickaReferenca.child("korakpokorakBodovi").setValue(0);
                        korisnickaReferenca.child("mojbrojBodovi").setValue(0);
                        korisnickaReferenca.child("brojPartija").setValue(0);
                        korisnickaReferenca.child("pobede").setValue(0);
                        korisnickaReferenca.child("porazi").setValue(0);
                        Toast.makeText(trenutniActivity, "uspešno ste se registrovali", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(trenutniActivity,naredniActivity);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("NOVI_KORISNIK", true);
                        trenutniActivity.startActivity(intent);
                        trenutniActivity.finish();
                    } else {
                        Toast.makeText(trenutniActivity, "neuspešna registracija", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(trenutniActivity, "greška. proverite podatke", Toast.LENGTH_SHORT).show();
        }

    }

    public void daLiSiPrijavljen(final Activity trenutniActivity, final Class<?> naredniActivity){
        if(this.mAuth.getCurrentUser() != null && !(trenutniActivity instanceof RegistrovanKorisnik)){
            if(trenutniActivity instanceof Prijava || trenutniActivity instanceof Registracija){
                Toast.makeText(trenutniActivity, "već ste prijavljeni", Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(trenutniActivity,naredniActivity);
            trenutniActivity.startActivity(intent);
            trenutniActivity.finish();
        } else if (this.mAuth.getCurrentUser() == null && trenutniActivity instanceof RegistrovanKorisnik){
            Intent intent = new Intent(trenutniActivity,MainActivity.class);
            trenutniActivity.startActivity(intent);
            trenutniActivity.finish();
        }
    }

    public void staviKorisnikaNaListuAktivnih(){
        DatabaseReference referencaNaAktivneKorisnike = this.koren.child("aktivniKorisnici");
        if(this.mAuth.getCurrentUser() != null){
            referencaNaAktivneKorisnike.child(this.mAuth.getCurrentUser().getUid()).setValue(true);
        }
    }

    public void obrisiKorisnikaSaListeAktivnih(){
        DatabaseReference referencaNaAktivneKorisnike =this.koren.child("aktivniKorisnici");
        if(this.mAuth.getCurrentUser() != null) {
            referencaNaAktivneKorisnike.child(this.mAuth.getCurrentUser().getUid()).setValue(false);
        }
    }
    public void zvezdeListener(final Activity trenutniActivity){
        if(!(trenutniActivity instanceof RegistrovanKorisnik)){
            return;
        }
        ValueEventListener zvezdeEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long vrednost = dataSnapshot.getValue(Long.class);
                    ((RegistrovanKorisnik)trenutniActivity).postaviZvezde(vrednost.toString());
                } else {
                    ((RegistrovanKorisnik)trenutniActivity).postaviZvezde("-");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        listenerManager.addValueEventListener(this.koren.child("korisnici").child(this.mAuth.getCurrentUser().getUid()).child("zvezde"),zvezdeEventListener);
    }

    public void tokeniListener(final Activity trenutniActivity){
        if(!(trenutniActivity instanceof RegistrovanKorisnik)){
            return;
        }
        ValueEventListener tokeniEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long vrednost = dataSnapshot.getValue(Long.class);
                    ((RegistrovanKorisnik)trenutniActivity).postaviTokene(vrednost.toString());
                } else {
                    ((RegistrovanKorisnik)trenutniActivity).postaviTokene("-");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        listenerManager.addValueEventListener(this.koren.child("korisnici").child(this.mAuth.getCurrentUser().getUid()).child("tokeni"),tokeniEventListener);
    }

    public void rangListener(final Activity trenutniActivity){
        if(!(trenutniActivity instanceof RegistrovanKorisnik)){
            return;
        }
        ValueEventListener rangEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Integer> rangMapa = new HashMap<>();
                    for(DataSnapshot korisnikSnapshot : dataSnapshot.getChildren()){
                        String korisnikId = korisnikSnapshot.getKey();
                        Integer brzvezda = korisnikSnapshot.child("zvezde").getValue(Integer.class);
                        if(brzvezda != null && korisnikId!=null)
                            rangMapa.put(korisnikId, brzvezda);
                    }
                    List<Map.Entry<String, Integer>> listaZaSortiranje = new ArrayList<>(rangMapa.entrySet());

                    Collections.sort(listaZaSortiranje, new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                            return o2.getValue().compareTo(o1.getValue());
                        }
                    });
                    Integer brojac = 1;

                    for (Map.Entry<String, Integer> entry : listaZaSortiranje) {
                        String korisnikId = entry.getKey();
                        if(korisnikId.equals(mAuth.getCurrentUser().getUid())){
                            String mesto = brojac.toString();
                            ((RegistrovanKorisnik)trenutniActivity).postaviRang(mesto);
                            break;
                        }else{
                            brojac++;
                        }
                    }

                } else {
                    ((RegistrovanKorisnik)trenutniActivity).postaviRang("-");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        listenerManager.addValueEventListener(this.koren.child("korisnici"),rangEventListener);
    }
}
