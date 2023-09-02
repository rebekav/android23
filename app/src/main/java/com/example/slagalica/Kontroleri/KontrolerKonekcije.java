package com.example.slagalica.Kontroleri;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.example.slagalica.Activities.IgriceActivities.Spojnice;
import com.example.slagalica.Activities.OstaliActivities.DvaIgraca;
import com.example.slagalica.Activities.OstaliActivities.JedanIgrac;
import com.example.slagalica.Activities.KorisnikActivities.RegistrovanKorisnik;
import com.example.slagalica.PomocneKlase.FirebaseListenerManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class KontrolerKonekcije {

    private FirebaseAuth mAuth;

    private FirebaseListenerManager listenerManager;

    private DatabaseReference koren;

    public KontrolerKonekcije(){
        this.mAuth = FirebaseAuth.getInstance();
        this.listenerManager = new FirebaseListenerManager();
        this.koren = FirebaseDatabase.getInstance().getReference("koren");
    }

    public FirebaseListenerManager dobaviListenerManager(){
        return listenerManager;
    }


    public void ukljuciListenerZaAktivneIgre(final Activity trenutniActivity){
        final String[] IdTrenutneIgre = {null};
        KontrolerDijaloga kontrolerDijaloga = new KontrolerDijaloga();
        ValueEventListener igraEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot igra : dataSnapshot.getChildren()){
                    if(IdTrenutneIgre[0] == null){
                        break;
                    }
                    else{
                        String IdIgre = igra.getKey();
                        if(IdIgre != null){
                            if(IdTrenutneIgre[0].equals(IdIgre)){
                                if(kontrolerDijaloga.dobaviDijalog()!=null){
                                    kontrolerDijaloga.iskljuciDijalog();
                                }
                                return;
                            }
                        }else{
                            continue;
                        }
                    }
                }

                for (DataSnapshot igra : dataSnapshot.getChildren()) {

                    String igrac1Id = igra.child("igrac1").getValue(String.class);
                    String igrac2Id = igra.child("igrac2").getValue(String.class);
                    String stanjeIgre = igra.child("stanjeIgre").getValue(String.class);
                    String IdIgre = igra.getKey();
                    IdTrenutneIgre[0] = IdIgre;

                    if(igrac1Id != null && igrac2Id != null && IdIgre != null && stanjeIgre != null){
                        if (igrac2Id.equals(mAuth.getCurrentUser().getUid()) && stanjeIgre.equals("cekanjeProtivnika")) {
                            ((RegistrovanKorisnik)trenutniActivity).prikaziPozivZaIgru(IdTrenutneIgre[0]);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        listenerManager.addValueEventListener(this.koren.child("aktivneIgre"), igraEventListener);
    }

    public void unistiIgruZaDvaIgraca(final Activity trenutniActivity, String idIgre, String razlog){
        if(idIgre != null){
            obrisiPodatkeIgre(idIgre);
        }
        Toast.makeText(trenutniActivity, razlog, Toast.LENGTH_SHORT).show();
        trenutniActivity.onBackPressed();
    }

    public void kreirajIgruZaDvaIgraca(final Activity trenutniActivity,final String idProtivnika){
        //Ako je null - mora se naci slobodan igrac
        //Ako nije null - igra se sa prijateljem

        if(idProtivnika == null){
            koren.child("korisnici").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Integer trenutnoTokena = snapshot.child("tokeni").getValue(Integer.class);
                        if(trenutnoTokena <= 0){
                            unistiIgruZaDvaIgraca(trenutniActivity,null,"nemate dovoljno tokena za započinjanje partije");
                        } else {
                            DatabaseReference referencaAktivnihKorisnika = koren.child("aktivniKorisnici");
                            referencaAktivnihKorisnika.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Boolean postojiLiSlobodanIgrac = false;
                                    String idSlobodnogIgraca = null;
                                    for(DataSnapshot trenutniKorisnik:snapshot.getChildren()){
                                        idSlobodnogIgraca = trenutniKorisnik.getKey();

                                        if(postojiLiSlobodanIgrac == true){
                                            break;
                                        }

                                        if(idSlobodnogIgraca.equals(mAuth.getCurrentUser().getUid())){
                                            continue;
                                        }

                                        if(idSlobodnogIgraca == null){
                                            unistiIgruZaDvaIgraca(trenutniActivity, null,"nema slobodnog igrača");
                                        }

                                        if(idSlobodnogIgraca != null && trenutniKorisnik.getValue(Boolean.class)){
                                            postojiLiSlobodanIgrac = true;
                                            DatabaseReference referencaNaAktivneIgre = koren.child("aktivneIgre");
                                            String idIgre = referencaNaAktivneIgre.push().getKey();

                                            Map<String, Object> podaciIgre = new HashMap<>();
                                            podaciIgre.put("igrac1", mAuth.getCurrentUser().getUid());
                                            podaciIgre.put("igrac2", idSlobodnogIgraca);
                                            podaciIgre.put("stanjeIgre", "cekanjeProtivnika");

                                            referencaNaAktivneIgre.child(idIgre).setValue(podaciIgre);

                                            ((DvaIgraca)trenutniActivity).postaviIdIgre(idIgre);


                                            ValueEventListener stanjeIgreListener = new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    if (dataSnapshot.exists()) {
                                                        String stanjeIgre = dataSnapshot.getValue(String.class);
                                                        if (stanjeIgre != null) {
                                                            if (stanjeIgre.equals("aktivna")) {
                                                                ((DvaIgraca)trenutniActivity).pokreniIgruDvaIgraca(idIgre);
                                                                koren.child("aktivneIgre").child(idIgre).child("stanjeIgre").removeEventListener(this);
                                                            } else if (stanjeIgre.equals("odbijena")) {
                                                                unistiIgruZaDvaIgraca(trenutniActivity, idIgre, "igrač koji je pronađen, odbio je partiju ili nema dovoljno tokena da igra");
                                                                koren.child("aktivneIgre").child(idIgre).child("stanjeIgre").removeEventListener(this);
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            };

                                            listenerManager.addValueEventListener(koren.child("aktivneIgre").child(idIgre).child("stanjeIgre"),stanjeIgreListener);


                                        }
                                    }
                                    if(postojiLiSlobodanIgrac == false){
                                        unistiIgruZaDvaIgraca(trenutniActivity, null,  "nema slobodnog igrača");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }  else {
                DatabaseReference referencaNaAktivnost = koren.child("aktivniKorisnici").child(idProtivnika);
                referencaNaAktivnost.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            if(snapshot.getValue(Boolean.class) == false){
                                Toast.makeText(trenutniActivity, "prijatelj trenutno nije aktivan", Toast.LENGTH_SHORT).show();
                                ((DvaIgraca)trenutniActivity).onBackPressed();
                            } else {
                                DatabaseReference referencaNaAktivneIgre = koren.child("aktivneIgre");
                                String idIgre = referencaNaAktivneIgre.push().getKey();

                                Map<String, Object> podaciIgre = new HashMap<>();
                                podaciIgre.put("igrac1", mAuth.getCurrentUser().getUid());
                                podaciIgre.put("igrac2", idProtivnika);
                                podaciIgre.put("stanjeIgre", "cekanjeProtivnika");
                                podaciIgre.put("prijateljska",true);

                                referencaNaAktivneIgre.child(idIgre).setValue(podaciIgre);

                                ((DvaIgraca)trenutniActivity).postaviIdIgre(idIgre);


                                ValueEventListener stanjeIgreListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {
                                            String stanjeIgre = dataSnapshot.getValue(String.class);
                                            if (stanjeIgre != null) {
                                                if (stanjeIgre.equals("aktivna")) {
                                                    ((DvaIgraca)trenutniActivity).pokreniIgruDvaIgraca(idIgre);
                                                    koren.child("aktivneIgre").child(idIgre).child("stanjeIgre").removeEventListener(this);
                                                } else if (stanjeIgre.equals("odbijena")) {
                                                    unistiIgruZaDvaIgraca(trenutniActivity, idIgre, "prijatelj je odbio partiju");
                                                    koren.child("aktivneIgre").child(idIgre).child("stanjeIgre").removeEventListener(this);
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                };

                                listenerManager.addValueEventListener(koren.child("aktivneIgre").child(idIgre).child("stanjeIgre"),stanjeIgreListener);


                            }
                        }else{
                            Toast.makeText(trenutniActivity, "prijatelj trenutno nije aktivan", Toast.LENGTH_SHORT).show();
                            ((DvaIgraca)trenutniActivity).onBackPressed();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
    }

    public void obrisiTokenZaPocetakIgre(String idIgre){
        koren.child("aktivneIgre").child(idIgre).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Boolean daLiJePrijateljska = snapshot.child("prijateljska").getValue(Boolean.class);
                    if(daLiJePrijateljska == null){
                        koren.child("korisnici").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Integer trenutnoTokena = snapshot.child("tokeni").getValue(Integer.class);
                                    trenutnoTokena--;
                                    koren.child("korisnici").child(mAuth.getCurrentUser().getUid()).child("tokeni").setValue(trenutnoTokena);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void kreirajIgruZaJednogIgraca(final Activity trenutniActivity){
        DatabaseReference referencaNaAktivneIgre = koren.child("aktivneIgre");
        String idIgre = referencaNaAktivneIgre.push().getKey();

        Map<String, Object> podaciIgre = new HashMap<>();
        if(mAuth.getCurrentUser() != null) {
            podaciIgre.put("igrac", mAuth.getCurrentUser().getUid());
        } else {
            podaciIgre.put("igrac", "gost");
        }

        referencaNaAktivneIgre.child(idIgre).setValue(podaciIgre);

        ((JedanIgrac)trenutniActivity).postaviIdIgre(idIgre);
    }

    public void ucitajPoeneZaJednogIgraca(final Activity trenutniActivity, String idIgre){
        if(idIgre == null){
            return;
        }
        DatabaseReference referencaNaIgru = koren.child("aktivneIgre").child(idIgre);
        referencaNaIgru.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Integer spojnice, asocijacije, korakPoKorak;
                    spojnice = snapshot.child("spojnice").child("igracpoeni").getValue(Integer.class);
                    asocijacije = snapshot.child("asocijacije").child("igracpoeni").getValue(Integer.class);
                    korakPoKorak = snapshot.child("korakPoKorak").child("igracpoeni").getValue(Integer.class);
                    ((JedanIgrac)trenutniActivity).azurirajPoeneIgraca(spojnice, asocijacije, korakPoKorak);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void ucitajPoeneZaDvaIgraca(final Activity trenutniActivity, String idIgre){
        if(idIgre == null){
            return;
        }
        DatabaseReference referencaNaIgru = koren.child("aktivneIgre").child(idIgre);
        referencaNaIgru.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Integer spojnice, asocijacije, korakPoKorak;
                    spojnice = snapshot.child("spojnice").child("igrac1poeni").getValue(Integer.class);
                    asocijacije = snapshot.child("asocijacije").child("igrac1poeni").getValue(Integer.class);
                    korakPoKorak = snapshot.child("korakPoKorak").child("igrac1poeni").getValue(Integer.class);
                    Integer spojnice2, asocijacije2, korakPoKorak2;
                    spojnice2 = snapshot.child("spojnice").child("igrac2poeni").getValue(Integer.class);
                    asocijacije2 = snapshot.child("asocijacije").child("igrac2poeni").getValue(Integer.class);
                    korakPoKorak2 = snapshot.child("korakPoKorak").child("igrac2poeni").getValue(Integer.class);
                    ((DvaIgraca)trenutniActivity).azurirajPoeneIgraca(spojnice, asocijacije, korakPoKorak, spojnice2, asocijacije2, korakPoKorak2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void postaviListenerZaNagliPrekidIgre(final Activity trenutniActivity, final String idIgre){
        ValueEventListener listenerZaNagliPrekidIgre = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() == false){
                    Toast.makeText(trenutniActivity,"protivnik je napustio igru", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(trenutniActivity, JedanIgrac.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    trenutniActivity.startActivity(intent);
                    trenutniActivity.finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listenerManager.addValueEventListener(koren.child("aktivneIgre").child(idIgre),listenerZaNagliPrekidIgre);
    }

    public void ocistiSvePreostaleIgre(){
        koren.child("aktivneIgre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot trenutnaIgra: snapshot.getChildren()){
                        if(trenutnaIgra.child("igrac").getValue(String.class)!=null && trenutnaIgra.child("igrac").getValue(String.class).equals(mAuth.getCurrentUser().getUid())){
                            String idTrenutneIgre = trenutnaIgra.getKey();
                            koren.child("aktivneIgre").child(idTrenutneIgre).setValue(null);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void proglasiKrajIgreIUpisiBodove(final Activity trenutniActivity, final String idIgre){
        koren.child("aktivneIgre").child(idIgre).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    final String igrac1 = snapshot.child("igrac1").getValue(String.class);
                    final String igrac2 = snapshot.child("igrac2").getValue(String.class);

                    final Integer spojniceBodovi1 = snapshot.child("spojnice").child("igrac1poeni").getValue(Integer.class);
                    final Integer asocijacijeBodovi1 = snapshot.child("asocijacije").child("igrac1poeni").getValue(Integer.class);
                    final Integer korakPoKorakBodovi1 = snapshot.child("korakPoKorak").child("igrac1poeni").getValue(Integer.class);
                    final Integer zbirPoenaPrvogIgraca = spojniceBodovi1 + asocijacijeBodovi1 + korakPoKorakBodovi1;

                    final Integer spojniceBodovi2 = snapshot.child("spojnice").child("igrac2poeni").getValue(Integer.class);
                    final Integer asocijacijeBodovi2 = snapshot.child("asocijacije").child("igrac2poeni").getValue(Integer.class);
                    final Integer korakPoKorakBodovi2 = snapshot.child("korakPoKorak").child("igrac2poeni").getValue(Integer.class);
                    final Integer zbirPoenaDrugogIgraca = spojniceBodovi2 + asocijacijeBodovi2 + korakPoKorakBodovi2;
                    if(mAuth.getCurrentUser().getUid().equals(igrac1)){

                        koren.child("korisnici").child(igrac1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Integer spojniceTrenutnoBodova = snapshot.child("spojniceBodovi").getValue(Integer.class);
                                    Integer asocijacijeTrenutnoBodova = snapshot.child("asocijacijeBodovi").getValue(Integer.class);
                                    Integer korakPoKorakTrenutnoBodova = snapshot.child("korakpokorakBodovi").getValue(Integer.class);

                                    spojniceTrenutnoBodova += spojniceBodovi1;
                                    asocijacijeTrenutnoBodova += asocijacijeBodovi1;
                                    korakPoKorakTrenutnoBodova += korakPoKorakBodovi1;

                                    koren.child("korisnici").child(igrac1).child("spojniceBodovi").setValue(spojniceTrenutnoBodova);
                                    koren.child("korisnici").child(igrac1).child("asocijacijeBodovi").setValue(asocijacijeTrenutnoBodova);
                                    koren.child("korisnici").child(igrac1).child("korakpokorakBodovi").setValue(korakPoKorakTrenutnoBodova);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    else if(mAuth.getCurrentUser().getUid().equals(igrac2)){
                        koren.child("korisnici").child(igrac2).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Integer spojniceTrenutnoBodova = snapshot.child("spojniceBodovi").getValue(Integer.class);
                                    Integer asocijacijeTrenutnoBodova = snapshot.child("asocijacijeBodovi").getValue(Integer.class);
                                    Integer korakPoKorakTrenutnoBodova = snapshot.child("korakpokorakBodovi").getValue(Integer.class);

                                    spojniceTrenutnoBodova += spojniceBodovi2;
                                    asocijacijeTrenutnoBodova += asocijacijeBodovi2;
                                    korakPoKorakTrenutnoBodova += korakPoKorakBodovi2;

                                    koren.child("korisnici").child(igrac2).child("spojniceBodovi").setValue(spojniceTrenutnoBodova);
                                    koren.child("korisnici").child(igrac2).child("asocijacijeBodovi").setValue(asocijacijeTrenutnoBodova);
                                    koren.child("korisnici").child(igrac2).child("korakpokorakBodovi").setValue(korakPoKorakTrenutnoBodova);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }


                    if(mAuth.getCurrentUser().getUid().equals(igrac1)){
                        koren.child("korisnici").child(igrac1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Integer trenutnoZvezda = snapshot.child("zvezde").getValue(Integer.class);
                                    Integer trenutnoTokena = snapshot.child("tokeni").getValue(Integer.class);
                                    Integer trenutnoPobeda = snapshot.child("pobede").getValue(Integer.class);
                                    Integer trenutnoPoraza = snapshot.child("porazi").getValue(Integer.class);
                                    Integer trenutnoPartija = snapshot.child("brojPartija").getValue(Integer.class);

                                    Integer partijeZaUpis = trenutnoPartija + 1;
                                    koren.child("korisnici").child(igrac1).child("brojPartija").setValue(partijeZaUpis);

                                    if(zbirPoenaPrvogIgraca>zbirPoenaDrugogIgraca){
                                        Integer zvezdeZaUpis = trenutnoZvezda + 10 + zbirPoenaPrvogIgraca/40;
                                        Integer tokeniZaUpis = trenutnoTokena + zvezdeZaUpis/50;
                                        Integer pobedaZaUpis = trenutnoPobeda + 1;
                                        koren.child("korisnici").child(igrac1).child("zvezde").setValue(zvezdeZaUpis);
                                        koren.child("korisnici").child(igrac1).child("tokeni").setValue(tokeniZaUpis);
                                        koren.child("korisnici").child(igrac1).child("pobede").setValue(pobedaZaUpis);
                                    } else {
                                        Integer zvezdeZaUpis = trenutnoZvezda - 10 + zbirPoenaPrvogIgraca/40;
                                        Integer tokeniZaUpis = trenutnoTokena + zvezdeZaUpis/50;
                                        Integer poraziZaUpis = trenutnoPoraza + 1;
                                        koren.child("korisnici").child(igrac1).child("zvezde").setValue(zvezdeZaUpis);
                                        koren.child("korisnici").child(igrac1).child("tokeni").setValue(tokeniZaUpis);
                                        koren.child("korisnici").child(igrac1).child("porazi").setValue(poraziZaUpis);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else if(mAuth.getCurrentUser().getUid().equals(igrac2)){
                        koren.child("korisnici").child(igrac2).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Integer trenutnoZvezda = snapshot.child("zvezde").getValue(Integer.class);
                                    Integer trenutnoTokena = snapshot.child("tokeni").getValue(Integer.class);
                                    Integer trenutnoPobeda = snapshot.child("pobede").getValue(Integer.class);
                                    Integer trenutnoPoraza = snapshot.child("porazi").getValue(Integer.class);
                                    Integer trenutnoPartija = snapshot.child("brojPartija").getValue(Integer.class);

                                    Integer partijeZaUpis = trenutnoPartija + 1;
                                    koren.child("korisnici").child(igrac2).child("brojPartija").setValue(partijeZaUpis);


                                    if(zbirPoenaDrugogIgraca>zbirPoenaPrvogIgraca){
                                        Integer zvezdeZaUpis = trenutnoZvezda + 10 + zbirPoenaDrugogIgraca/40;
                                        Integer tokeniZaUpis = trenutnoTokena + zvezdeZaUpis/50;
                                        Integer pobedaZaUpis = trenutnoPobeda + 1;
                                        koren.child("korisnici").child(igrac2).child("zvezde").setValue(zvezdeZaUpis);
                                        koren.child("korisnici").child(igrac2).child("tokeni").setValue(tokeniZaUpis);
                                        koren.child("korisnici").child(igrac2).child("pobede").setValue(pobedaZaUpis);
                                    } else {
                                        Integer zvezdeZaUpis = trenutnoZvezda - 10 + zbirPoenaDrugogIgraca/40;
                                        Integer tokeniZaUpis = trenutnoTokena + zvezdeZaUpis/50;
                                        Integer poraziZaUpis = trenutnoPoraza + 1;
                                        koren.child("korisnici").child(igrac2).child("zvezde").setValue(zvezdeZaUpis);
                                        koren.child("korisnici").child(igrac2).child("tokeni").setValue(tokeniZaUpis);
                                        koren.child("korisnici").child(igrac2).child("porazi").setValue(poraziZaUpis);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }


                    if(mAuth.getCurrentUser().getUid().equals(igrac1)){
                        if(zbirPoenaPrvogIgraca>zbirPoenaDrugogIgraca){
                            ((DvaIgraca)trenutniActivity).porukaOPobedi();
                        } else {
                            ((DvaIgraca)trenutniActivity).porukaOPorazu();
                        }
                    } else if(mAuth.getCurrentUser().getUid().equals(igrac2)){
                        if(zbirPoenaPrvogIgraca<zbirPoenaDrugogIgraca){
                            ((DvaIgraca)trenutniActivity).porukaOPobedi();
                        } else {
                            ((DvaIgraca)trenutniActivity).porukaOPorazu();
                        }
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void dodajTokeneNaDan(Activity activity) {

        koren.child("korisnici").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Calendar poslednjeVremeDodele = Calendar.getInstance();
                    Calendar danas = Calendar.getInstance();
                    Long vremeLong = snapshot.child("poslednjeVremeDodele").getValue(Long.class);
                    if(vremeLong == null){
                        vremeLong = 0L;
                    }
                    poslednjeVremeDodele.setTimeInMillis(vremeLong);

                    if (danas.get(Calendar.YEAR) > poslednjeVremeDodele.get(Calendar.YEAR) ||
                            danas.get(Calendar.DAY_OF_YEAR) > poslednjeVremeDodele.get(Calendar.DAY_OF_YEAR)) {

                        Integer trenutnoTokena = snapshot.child("tokeni").getValue(Integer.class);
                        trenutnoTokena+=5;
                        koren.child("korisnici").child(mAuth.getCurrentUser().getUid()).child("tokeni").setValue(trenutnoTokena);
                        koren.child("korisnici").child(mAuth.getCurrentUser().getUid()).child("poslednjeVremeDodele").setValue(danas.getTimeInMillis());

                        KontrolerDijaloga kontrolerDijaloga = new KontrolerDijaloga();
                        kontrolerDijaloga.tokeniNaDan(activity);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    public void obrisiPodatkeIgre(String idIgre){
        if(idIgre != null){
            koren.child("aktivneIgre").child(idIgre).setValue(null);
        }
    }


}
