package com.example.slagalica.Kontroleri;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.slagalica.Activities.IgriceActivities.Asocijacije;
import com.example.slagalica.PomocneKlase.FirebaseListenerManager;
import com.example.slagalica.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AsocijacijeKontroler {

    private String idIgre;

    private String kojiJeIgrac;
    private Integer brojIgraca, redniBrojPartije;

    private DatabaseReference referencaNaIgru;

    private DatabaseReference referencaNaAsocijacije;

    private ArrayList<Integer> pitanja;

    private FirebaseListenerManager listenerManager;

    private CountDownTimer tajmerZaKraj;

    private KontrolerKonekcije kontrolerKonekcije;

    private KontrolerKorisnika kontrolerKorisnika;

    private KontrolerDijaloga kontrolerDijaloga;

    private Handler handler;

    private Integer poeniA, poeniB, poeniC, poeniD, konacnoPoeni;

    public AsocijacijeKontroler(String idIgre, Integer brojIgraca, Integer redniBrojPartije){
        this.idIgre = idIgre;
        this.brojIgraca = brojIgraca;
        this.redniBrojPartije = redniBrojPartije;
        this.listenerManager = new FirebaseListenerManager();
        this.kontrolerKonekcije = new KontrolerKonekcije();
        this.kontrolerDijaloga = new KontrolerDijaloga();
        this.kontrolerKorisnika = new KontrolerKorisnika();
        this.kontrolerKonekcije = new KontrolerKonekcije();
        this.handler = new Handler();
        this.referencaNaIgru = FirebaseDatabase.getInstance().getReference("koren").child("aktivneIgre").child(idIgre);
        this.referencaNaAsocijacije = referencaNaIgru.child("asocijacije");
        this.poeniA = this.poeniB = this.poeniC = this.poeniD = 6;
        this.konacnoPoeni = 7;
    }
    public FirebaseListenerManager dobaviListenerManager(){
        return listenerManager;
    }
    public void postaviKojiJeIgrac(String vrednost){
        this.kojiJeIgrac = vrednost;
    }

    public static ArrayList<Integer> generisiNizSlucajnihBrojeva (int minimum, int maksimum, int broj){
        if (broj > maksimum - minimum + 1) {
            throw new IllegalArgumentException("ne može da se generiše više nasumičnih brojeva od dostupnog opsega");
        }

        ArrayList<Integer> povratnaVrednost = new ArrayList<>();
        ArrayList<Integer> dostupniBrojevi = new ArrayList<>();
        for (int i = minimum; i <= maksimum; i++) {
            dostupniBrojevi.add(i);
        }

        Random random = new Random();

        while (povratnaVrednost.size() < broj) {
            int randomIndex = random.nextInt(dostupniBrojevi.size());
            int randomNumber = dostupniBrojevi.get(randomIndex);
            dostupniBrojevi.remove(randomIndex);
            povratnaVrednost.add(randomNumber);
        }
        return povratnaVrednost;
    }

    public void kreirajAsocijacije(final Activity trenutniActivity) {
        if (brojIgraca == 1 && redniBrojPartije == 1) {

            pitanja = generisiNizSlucajnihBrojeva(1, 4, 2);
            StringBuilder pitanjaZaUpisUBazu = new StringBuilder();
            for (int i = 0; i < pitanja.size(); i++) {
                pitanjaZaUpisUBazu.append(pitanja.get(i));

                if (i < pitanja.size() - 1) {
                    pitanjaZaUpisUBazu.append(", ");
                }
            }

            postaviKojiJeIgrac("igrac");

            referencaNaAsocijacije.child("pitanja").setValue(pitanjaZaUpisUBazu.toString());
            referencaNaAsocijacije.child("igracpoeni").setValue(0);

            zapocniIgru(trenutniActivity);
        } else if (brojIgraca == 1 && redniBrojPartije == 2) {
            postaviKojiJeIgrac("igrac");

            procitajPitanja(trenutniActivity);
        }else if(brojIgraca == 2 && redniBrojPartije == 1){
            kontrolerDijaloga.prikaziDijalogZaUcitavanje(trenutniActivity,"asocijacije", "čekanje protivnika da uđe u igru");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    kontrolerDijaloga.iskljuciDijalog();
                    Toast.makeText(trenutniActivity, "prošlo je vreme za čekanje igrača", Toast.LENGTH_SHORT).show();

                }
            };



            handler.postDelayed(runnable, 10000);
            referencaNaIgru.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String idDomacina = snapshot.child("igrac1").getValue(String.class);
                        if (idDomacina.equals(kontrolerKorisnika.dobaviMAuth().getCurrentUser().getUid())) {
                            referencaNaIgru.child("igrac1Lokacija").setValue("Asocijacije");

                            pitanja = generisiNizSlucajnihBrojeva(1,4,2);
                            StringBuilder pitanjaZaUpisUBazu = new StringBuilder();
                            for (int i = 0; i < pitanja.size(); i++) {
                                pitanjaZaUpisUBazu.append(pitanja.get(i));

                                if (i < pitanja.size() - 1) {
                                    pitanjaZaUpisUBazu.append(", ");
                                }
                            }
                            referencaNaAsocijacije.child("pitanja").setValue(pitanjaZaUpisUBazu.toString());
                            referencaNaAsocijacije.child("igrac1poeni").setValue(0);
                            referencaNaAsocijacije.child("igrac2poeni").setValue(0);
                            referencaNaAsocijacije.child("stanjeIgre").setValue("igraJeSpremna");
                            referencaNaAsocijacije.child("potez").setValue("igrac1");

                            postaviKojiJeIgrac("igrac1");

                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String lokacija = dataSnapshot.getValue(String.class);
                                        if (lokacija.equals("Asocijacije")) {
                                            listenerZaPocetakIgre(trenutniActivity);
                                            referencaNaIgru.child("igrac2Lokacija").removeEventListener(this);

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            };

                            listenerManager.addValueEventListener(referencaNaIgru.child("igrac2Lokacija"), listenerZaLokacijuProtivnika);


                        } else {

                            referencaNaIgru.child("igrac2Lokacija").setValue("Asocijacije");
                            postaviKojiJeIgrac("igrac2");
                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String location = dataSnapshot.getValue(String.class);
                                        if (location.equals("Asocijacije")) {
                                            listenerZaSpremnuIgru(trenutniActivity);
                                            referencaNaIgru.child("igrac1Lokacija").removeEventListener(this);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            };

                            listenerManager.addValueEventListener(referencaNaIgru.child("igrac1Lokacija"), listenerZaLokacijuProtivnika);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if (brojIgraca == 2 && redniBrojPartije == 2){
            kontrolerDijaloga.prikaziDijalogZaUcitavanje(trenutniActivity,"asocijacije", "čekanje protivnika da uđe u igru");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    kontrolerDijaloga.iskljuciDijalog();
                    Toast.makeText(trenutniActivity, "prošlo je vreme za čekanje igrača", Toast.LENGTH_SHORT).show();

                }
            };

            handler.postDelayed(runnable, 10000);
            referencaNaIgru.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String idDomacina = snapshot.child("igrac1").getValue(String.class);
                        if (idDomacina.equals(kontrolerKorisnika.dobaviMAuth().getCurrentUser().getUid())) {
                            referencaNaIgru.child("igrac1Lokacija").setValue("Asocijacije");
                            referencaNaAsocijacije.child("stanjeIgre").setValue("igraJeSpremna");

                            procitajPitanja(trenutniActivity);


                            postaviKojiJeIgrac("igrac1");

                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String lokacija = dataSnapshot.getValue(String.class);
                                        if (lokacija.equals("Asocijacije")) {
                                            listenerZaPocetakIgre(trenutniActivity);
                                            referencaNaIgru.child("igrac2Lokacija").removeEventListener(this);

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            };

                            listenerManager.addValueEventListener(referencaNaIgru.child("igrac2Lokacija"), listenerZaLokacijuProtivnika);


                        } else {

                            referencaNaIgru.child("igrac2Lokacija").setValue("Asocijacije");
                            postaviKojiJeIgrac("igrac2");
                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String location = dataSnapshot.getValue(String.class);
                                        if (location.equals("Asocijacije")) {
                                            listenerZaSpremnuIgru(trenutniActivity);
                                            referencaNaIgru.child("igrac1Lokacija").removeEventListener(this);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            };

                            listenerManager.addValueEventListener(referencaNaIgru.child("igrac1Lokacija"), listenerZaLokacijuProtivnika);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void listenerZaPocetakIgre(final Activity trenutniActivity){
        ValueEventListener listenerZaStanjeIgre = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String stanjeIgre = dataSnapshot.getValue(String.class);
                    if (stanjeIgre.equals("igraJePocela")) {
                        kontrolerDijaloga.iskljuciDijalog();
                        handler.removeCallbacksAndMessages(null);
                        zapocniIgru(trenutniActivity);
                        referencaNaAsocijacije.child("stanjeIgre").removeEventListener(this);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        listenerManager.addValueEventListener(referencaNaAsocijacije.child("stanjeIgre"), listenerZaStanjeIgre);
    }

    public void listenerZaSpremnuIgru(final Activity trenutniActivity){
        ValueEventListener listenerZaStanjeIgre = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String stanjeIgre = dataSnapshot.getValue(String.class);
                    if (stanjeIgre.equals("igraJeSpremna")) {
                        procitajPitanja(trenutniActivity);
                        kontrolerDijaloga.iskljuciDijalog();
                        handler.removeCallbacksAndMessages(null);
                        referencaNaAsocijacije.child("stanjeIgre").setValue("igraJePocela")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            zapocniIgru(trenutniActivity);
                                        }
                                    }
                                });
                        referencaNaAsocijacije.child("stanjeIgre").removeEventListener(this);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        listenerManager.addValueEventListener(referencaNaAsocijacije.child("stanjeIgre"), listenerZaStanjeIgre);
    }

    private void procitajPitanja (final Activity trenutniActivity) {
        pitanja = new ArrayList<>();
        referencaNaAsocijacije.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String pitanjaString = snapshot.child("pitanja").getValue(String.class);

                    List<String> brojevi = Arrays.asList(pitanjaString.split(",\\s*"));

                    for (String brojString : brojevi) {
                        try {
                            int broj = 0;
                            broj = Integer.parseInt(brojString);
                            pitanja.add(broj);
                        } catch (NumberFormatException e) {
                        }
                    }
                    if(brojIgraca == 1)
                        zapocniIgru(trenutniActivity);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void obrisiOdgovore() {
        referencaNaAsocijacije.child("Apogodio").setValue(null);
        referencaNaAsocijacije.child("Bpogodio").setValue(null);
        referencaNaAsocijacije.child("Cpogodio").setValue(null);
        referencaNaAsocijacije.child("Dpogodio").setValue(null);
        referencaNaAsocijacije.child("Aotvoreno").setValue(null);
        referencaNaAsocijacije.child("Botvoreno").setValue(null);
        referencaNaAsocijacije.child("Cotvoreno").setValue(null);
        referencaNaAsocijacije.child("Dotvoreno").setValue(null);
        referencaNaAsocijacije.child("konacnopogodio").setValue(null);
    }


    public void zapocniIgru(final Activity trenutniActivity){
        azurirajPodatke(trenutniActivity);
        azurirajPoene(trenutniActivity);
        ((Asocijacije)trenutniActivity).zakljucajUpise();
        if(brojIgraca == 2){
            proveriPotez(trenutniActivity);
        }
        tajmerZaKraj = new CountDownTimer(120000, 1000) {
            public void onTick(long millisUntilFinished) {
                ((Asocijacije)trenutniActivity).prikaziPreostaloVreme(millisUntilFinished);
            }
            public void onFinish() {
                narednaIgra(trenutniActivity);

            }
        }.start();
    }

    public void narednaIgra(final Activity trenutniActivity){
        prekiniTajmer();
        listenerManager.removeAllListeners();
        obrisiOdgovore();
        povecajPoene(kojiJeIgrac, poeniA + poeniB + poeniC + poeniD + konacnoPoeni);
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if(pitanja.size()==1){
                    ((Asocijacije)trenutniActivity).postaviNameruIzlaska(false);
                    Toast.makeText(trenutniActivity, "kraj igre asocijacije.", Toast.LENGTH_SHORT).show();
                    ((Asocijacije)trenutniActivity).onBackPressed();
                    return;
                }
                referencaNaAsocijacije.child("pitanja").setValue(Integer.toString(pitanja.get(1)));
                if(brojIgraca == 2)
                    referencaNaAsocijacije.child("potez").setValue("igrac2");

                pitanja.remove(0);

                ((Asocijacije)trenutniActivity).postaviNameruIzlaska(false);
                Intent intent = new Intent(trenutniActivity, Asocijacije.class);
                intent.putExtra("IGRA_ID", idIgre);
                intent.putExtra("BROJ_IGRACA", brojIgraca);
                intent.putExtra("REDNI_BROJ_PARTIJE", 2);
                intent.putExtra("NAREDNA_IGRA", "KorakPoKorak");
                trenutniActivity.startActivity(intent);
                trenutniActivity.finish();
            }
        }.start();
    }

    public void prekiniTajmer() {
        if (tajmerZaKraj != null) {
            tajmerZaKraj.cancel();
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void otvoriPolje(final Activity trenutniActivity, Integer red, Integer kolona){
        Button kliknutoDugme = ((Asocijacije)trenutniActivity).dobaviPolje(red, kolona);
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra"+Integer.toString(pitanja.get(0)));
        String redString = redUString(red);
        referencaNaPitanje = referencaNaPitanje.child(redString).child(Integer.toString(kolona + 1));

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!kliknutoDugme.isEnabled()) {
                        return;
                    }
                    kliknutoDugme.setText(snapshot.getValue(String.class));
                    kliknutoDugme.setEnabled(false);
                    DatabaseReference otvorenaPoljaRef = referencaNaAsocijacije.child(redString + "otvoreno");

                    otvorenaPoljaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String otvoreno = dataSnapshot.getValue(String.class);
                                int ukupno = ukupnoOtvorenihPolja(otvoreno);

                                if (ukupno == 0 && otvoreno == null) {
                                    otvoreno = Integer.toString(kolona + 1);
                                } else {
                                    otvoreno += ", " + Integer.toString(kolona + 1);
                                }


                                otvorenaPoljaRef.setValue(otvoreno).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(brojIgraca == 2){
                                            ((Asocijacije)trenutniActivity).zakljucajDugmad();
                                        }
                                        otkljucajUpise(trenutniActivity);

                                    }
                                });
                            } else {
                                otvorenaPoljaRef.setValue(Integer.toString(kolona + 1)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(brojIgraca == 2){
                                            ((Asocijacije)trenutniActivity).zakljucajDugmad();
                                        }
                                        otkljucajUpise(trenutniActivity);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void prikaziPolje(final Activity trenutniActivity, int red, int kolona) {
        Button dugmeZaPrikaz = ((Asocijacije)trenutniActivity).dobaviPolje(red, kolona);
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra"+Integer.toString(pitanja.get(0)));

        String redString = redUString(red);

        if (!redString.isEmpty()) {
            referencaNaPitanje = referencaNaPitanje.child(redString).child(Integer.toString(kolona + 1));

            referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        dugmeZaPrikaz.setText(snapshot.getValue(String.class));
                        dugmeZaPrikaz.setEnabled(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public String redUString(int broj){
        switch (broj) {
            case 0:
                return "A";
            case 1:
                return "B";
            case 2:
                return "C";
            case 3:
                return "D";
            default:
                return "";
        }
    }

    private int ukupnoOtvorenihPolja(String otvoreno) {
        if (otvoreno == null) {
            return 0;
        }
        String[] polja = otvoreno.split(", ");
        return polja.length;
    }

    public void otkljucajUpise(final Activity trenutniActivity){
        ((Asocijacije)trenutniActivity).otkljucajUpise();

        referencaNaAsocijacije.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("Apogodio").getValue(String.class)!=null){
                        ((Asocijacije)trenutniActivity).zakljucajA();
                    }
                    if(snapshot.child("Bpogodio").getValue(String.class)!=null) {
                        ((Asocijacije) trenutniActivity).zakljucajB();
                    }
                    if(snapshot.child("Cpogodio").getValue(String.class)!=null){
                        ((Asocijacije)trenutniActivity).zakljucajC();
                    }
                    if(snapshot.child("Dpogodio").getValue(String.class)!=null){
                        ((Asocijacije)trenutniActivity).zakljucajD();
                    }
                    if(snapshot.child("konacnopogodio").getValue(String.class)!=null){
                        ((Asocijacije)trenutniActivity).zakljucajKonacno();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void aDalje(final Activity trenutniActivity){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra"+Integer.toString(pitanja.get(0)));
        DatabaseReference referencaNaPoene = referencaNaAsocijacije.child(kojiJeIgrac + "poeni");

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String unesenTekst = ((Asocijacije)trenutniActivity).dobaviATekst();
                String tacanOdgovor = snapshot.child("A").child("Aodgovor").getValue(String.class);

                unesenTekst = transformisiString(unesenTekst);
                tacanOdgovor = transformisiString(tacanOdgovor);

                if (unesenTekst.equals(tacanOdgovor)) {
                    referencaNaAsocijacije.child("Apogodio").setValue(kojiJeIgrac);
                    String aPogodio = kojiJeIgrac;
                    int dodatniPoeni = 0;

                    if (aPogodio == null) {
                        dodatniPoeni += 4;
                    } else {
                        int brojac = aPogodio.split(",").length;
                        dodatniPoeni += 4 - brojac;
                    }
                    referencaNaAsocijacije.child("Aotvoreno").setValue("1, 2, 3, 4");
                } else {
                    ((Asocijacije)trenutniActivity).postaviATekst("");
                    if(brojIgraca == 2){
                        ((Asocijacije)trenutniActivity).zakljucajUpise();
                        ((Asocijacije)trenutniActivity).zakljucajDugmad();
                        zavrsiPotez(trenutniActivity);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void bDalje(final Activity trenutniActivity){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra"+Integer.toString(pitanja.get(0)));
        DatabaseReference referencaNaPoene = referencaNaAsocijacije.child(kojiJeIgrac + "poeni");

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String unesenTekst = ((Asocijacije)trenutniActivity).dobaviBTekst();
                String tacanOdgovor = snapshot.child("B").child("Bodgovor").getValue(String.class);

                unesenTekst = transformisiString(unesenTekst);
                tacanOdgovor = transformisiString(tacanOdgovor);

                if (unesenTekst.equals(tacanOdgovor)) {
                    referencaNaAsocijacije.child("Bpogodio").setValue(kojiJeIgrac);
                    String bPogodio = kojiJeIgrac;
                    int dodatniPoeni = 0;

                    if (bPogodio == null) {
                        dodatniPoeni += 4;
                    } else {
                        int brojac = bPogodio.split(",").length;
                        dodatniPoeni += 4 - brojac;
                    }
                    referencaNaAsocijacije.child("Botvoreno").setValue("1, 2, 3, 4");
                } else {
                    ((Asocijacije)trenutniActivity).postaviBTekst("");
                    if(brojIgraca == 2){
                        ((Asocijacije)trenutniActivity).zakljucajUpise();
                        ((Asocijacije)trenutniActivity).zakljucajDugmad();
                        zavrsiPotez(trenutniActivity);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void cDalje(final Activity trenutniActivity){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra"+Integer.toString(pitanja.get(0)));
        DatabaseReference referencaNaPoene = referencaNaAsocijacije.child(kojiJeIgrac + "poeni");

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String unesenTekst = ((Asocijacije)trenutniActivity).dobaviCTekst();
                String tacanOdgovor = snapshot.child("C").child("Codgovor").getValue(String.class);

                unesenTekst = transformisiString(unesenTekst);
                tacanOdgovor = transformisiString(tacanOdgovor);

                if (unesenTekst.equals(tacanOdgovor)) {
                    referencaNaAsocijacije.child("Cpogodio").setValue(kojiJeIgrac);
                    String cPogodio = kojiJeIgrac;
                    int dodatniPoeni = 0;

                    if (cPogodio == null) {
                        dodatniPoeni += 4;
                    } else {
                        int brojac = cPogodio.split(",").length;
                        dodatniPoeni += 4 - brojac;
                    }
                    referencaNaAsocijacije.child("Cotvoreno").setValue("1, 2, 3, 4");
                } else {
                    ((Asocijacije)trenutniActivity).postaviCTekst("");
                    if(brojIgraca == 2){
                        ((Asocijacije)trenutniActivity).zakljucajUpise();
                        ((Asocijacije)trenutniActivity).zakljucajDugmad();
                        zavrsiPotez(trenutniActivity);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void dDalje(final Activity trenutniActivity){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra"+Integer.toString(pitanja.get(0)));
        DatabaseReference referencaNaPoene = referencaNaAsocijacije.child(kojiJeIgrac + "poeni");

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String unesenTekst = ((Asocijacije)trenutniActivity).dobaviDTekst();
                String tacanOdgovor = snapshot.child("D").child("Dodgovor").getValue(String.class);

                unesenTekst = transformisiString(unesenTekst);
                tacanOdgovor = transformisiString(tacanOdgovor);

                if (unesenTekst.equals(tacanOdgovor)) {
                    referencaNaAsocijacije.child("Dpogodio").setValue(kojiJeIgrac);
                    String dPogodio = kojiJeIgrac;
                    int dodatniPoeni = 0;

                    if (dPogodio == null) {
                        dodatniPoeni += 4;
                    } else {
                        int brojac = dPogodio.split(",").length;
                        dodatniPoeni += 4 - brojac;
                    }
                    referencaNaAsocijacije.child("Dotvoreno").setValue("1, 2, 3, 4");
                } else {
                    ((Asocijacije)trenutniActivity).postaviDTekst("");
                    if(brojIgraca == 2){
                        ((Asocijacije)trenutniActivity).zakljucajUpise();
                        ((Asocijacije)trenutniActivity).zakljucajDugmad();
                        zavrsiPotez(trenutniActivity);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void konacnoDalje(final Activity trenutniActivity){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra"+Integer.toString(pitanja.get(0)));
        DatabaseReference referencaNaPoene = referencaNaAsocijacije.child(kojiJeIgrac + "poeni");

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String unesenTekst = ((Asocijacije)trenutniActivity).dobaviKonacnoTekst();
                String konacnoResenje = snapshot.child("konacanOdgovor").getValue(String.class);

                unesenTekst = transformisiString(unesenTekst);
                konacnoResenje = transformisiString(konacnoResenje);

                if (unesenTekst.equals(konacnoResenje)) {
                    referencaNaAsocijacije.child("konacnopogodio").setValue(kojiJeIgrac);
                    String[] polja = {"A", "B", "C", "D"};
                    final int[] poeniZaUpis = {0};

                    referencaNaAsocijacije.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                if(snapshot.child("Apogodio").getValue(String.class) == null){
                                    poeniZaUpis[0] += 6;
                                }
                                if(snapshot.child("Bpogodio").getValue(String.class) == null){
                                    poeniZaUpis[0] += 6;
                                }
                                if(snapshot.child("Cpogodio").getValue(String.class) == null){
                                    poeniZaUpis[0] += 6;
                                }
                                if(snapshot.child("Dpogodio").getValue(String.class) == null){
                                    poeniZaUpis[0] += 6;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    for (String polje : polja) {
                        final String pogodjeno = polje + "pogodio";
                        final String otvoreno = polje + "otvoreno";

                        referencaNaAsocijacije.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    if (snapshot.child(pogodjeno).getValue(String.class) == null) {
                                        referencaNaAsocijacije.child(pogodjeno).setValue(kojiJeIgrac);
                                        referencaNaAsocijacije.child(otvoreno).setValue("1, 2, 3, 4");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    ((Asocijacije)trenutniActivity).postaviKonacnoTekst("");
                    if(brojIgraca == 2){
                        ((Asocijacije)trenutniActivity).zakljucajUpise();
                        ((Asocijacije)trenutniActivity).zakljucajDugmad();
                        zavrsiPotez(trenutniActivity);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void izmeniDugme(final Activity trenutniActivity,Button button, String pogodio) {
        button.setClickable(false);
        if (pogodio != null) {
            if (pogodio.equals("igrac1") || pogodio.equals("igrac")) {
                ((Asocijacije)trenutniActivity).promeniBoju(button, R.color.plava);
            } else if (pogodio.equals("igrac2")) {
                ((Asocijacije)trenutniActivity).promeniBoju(button, R.color.crvena);
            }
        } else {
            ((Asocijacije)trenutniActivity).promeniBoju(button, R.color.asocijacijeOdgovor);
        }
    }


    public void povecajPoene(String igrac, final Integer poeni){
        DatabaseReference referencaNaPoene = referencaNaAsocijacije.child(igrac + "poeni");
        referencaNaPoene.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Integer trenutniPoeni = snapshot.getValue(Integer.class);
                    trenutniPoeni += poeni;
                    referencaNaPoene.setValue(trenutniPoeni);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void azurirajPoene(final Activity trenutniActivity){
        ValueEventListener aOtvorioListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                String aOtvoreno = Snapshot.getValue(String.class);
                referencaNaAsocijacije.child("Apogodio").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String aPogodio = snapshot.getValue(String.class);
                        if(aOtvoreno == null) return;
                        if(aPogodio!=null && !aPogodio.equals(kojiJeIgrac)) {
                            poeniA = 0;
                        } else if(aPogodio!=null && aPogodio.equals(kojiJeIgrac)){
                            return;
                        }
                        else if(poeniA==2 && aOtvoreno.length() == 10) {
                            return;
                        } else {
                            poeniA--;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        listenerManager.addValueEventListener(referencaNaAsocijacije.child("Aotvoreno"),aOtvorioListener);

        ValueEventListener bOtvorioListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                String bOtvoreno = Snapshot.getValue(String.class);
                referencaNaAsocijacije.child("Bpogodio").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String bPogodio = snapshot.getValue(String.class);
                        if(bOtvoreno == null) return;
                        if(bPogodio!=null && !bPogodio.equals(kojiJeIgrac)) {
                            poeniB = 0;
                        } else if(bPogodio!=null && bPogodio.equals(kojiJeIgrac)){
                            return;
                        }
                        else if(poeniB!=2 && bOtvoreno.length() == 10) {
                            return;
                        } else{
                            poeniB--;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        listenerManager.addValueEventListener(referencaNaAsocijacije.child("Botvoreno"),bOtvorioListener);

        ValueEventListener cOtvorioListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                String cOtvoreno = Snapshot.getValue(String.class);
                referencaNaAsocijacije.child("Cpogodio").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String cPogodio = snapshot.getValue(String.class);
                        if(cOtvoreno == null) return;
                        if(cPogodio!=null && !cPogodio.equals(kojiJeIgrac)) {
                            poeniC = 0;
                        } else if(cPogodio!=null && cPogodio.equals(kojiJeIgrac)){
                            return;
                        }
                        else if(poeniC!=2 && cOtvoreno.length() == 10) {
                            return;
                        }else{
                            poeniC--;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        listenerManager.addValueEventListener(referencaNaAsocijacije.child("Cotvoreno"),cOtvorioListener);

        ValueEventListener dOtvorioListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                String dOtvoreno = Snapshot.getValue(String.class);
                referencaNaAsocijacije.child("Dpogodio").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dPogodio = snapshot.getValue(String.class);
                        if(dOtvoreno == null) return;
                        if(dPogodio!=null && !dPogodio.equals(kojiJeIgrac)) {
                            poeniD = 0;
                        } else if(dPogodio!=null && dPogodio.equals(kojiJeIgrac)){
                            return;
                        }
                        else if(poeniD!=2 && dOtvoreno.length() == 10) {
                            return;
                        }else{
                            poeniD--;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        listenerManager.addValueEventListener(referencaNaAsocijacije.child("Dotvoreno"),dOtvorioListener);


        ValueEventListener konacnoPogodioListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                String konacno = Snapshot.getValue(String.class);
                if(konacno.equals(kojiJeIgrac) == false){
                    konacnoPoeni = 0;
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        listenerManager.addValueEventListener(referencaNaAsocijacije.child("konacnootvoreno"),dOtvorioListener);

    }


    private void azurirajPodatke(final Activity trenutniActivity){
        ValueEventListener Aotvoreno = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                azurirajPogodjeno(trenutniActivity,snapshot, 0);
                azurirajOtvoreno(trenutniActivity,snapshot,0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listenerManager.addValueEventListener(referencaNaAsocijacije, Aotvoreno);

        ValueEventListener Botvoreno = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                azurirajPogodjeno(trenutniActivity,snapshot, 1);
                azurirajOtvoreno(trenutniActivity,snapshot,1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listenerManager.addValueEventListener(referencaNaAsocijacije, Botvoreno);

        ValueEventListener Cotvoreno = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                azurirajPogodjeno(trenutniActivity,snapshot, 2);
                azurirajOtvoreno(trenutniActivity,snapshot,2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listenerManager.addValueEventListener(referencaNaAsocijacije, Cotvoreno);

        ValueEventListener Dotvoreno = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                azurirajPogodjeno(trenutniActivity,snapshot, 3);
                azurirajOtvoreno(trenutniActivity,snapshot,3);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listenerManager.addValueEventListener(referencaNaAsocijacije, Dotvoreno);

        ValueEventListener konacnoOtvoreno = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                azurirajPogodjeno(trenutniActivity,snapshot, 4);
                if(snapshot.child("konacnopogodio").getValue(String.class)!=null)
                    referencaNaAsocijacije.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listenerManager.addValueEventListener(referencaNaAsocijacije, konacnoOtvoreno);

    }


    public void azurirajPogodjeno(final Activity trenutniActivity, DataSnapshot snapshot, Integer polje){

        if(polje == 4){
            String pogodio = snapshot.child("konacnopogodio").getValue(String.class);
            if(pogodio!=null){
                postaviResenjeKonacno(trenutniActivity, snapshot, pogodio);
                return;
            }
        }

        String poljeString = redUString(polje);

        ArrayList<Integer> niz = StringUListuBrojeva(snapshot.child(poljeString + "otvoreno").getValue(String.class));
        String pogodio = snapshot.child(poljeString + "pogodio").getValue(String.class);

        if(pogodio!=null){
            if (!niz.isEmpty()) {
                for (int i = 0; i < niz.size(); i++) {
                    int indeksDugmeta = niz.get(i) - 1;
                    izmeniDugme(trenutniActivity,((Asocijacije)trenutniActivity).dobaviPolje(polje,indeksDugmeta), pogodio);
                    prikaziPolje(trenutniActivity,polje, indeksDugmeta);
                }

                if(niz.size() == 4){
                    if(polje == 0){
                        postaviResenjeA(trenutniActivity,snapshot,pogodio);
                    } else if(polje == 1){
                        postaviResenjeB(trenutniActivity,snapshot,pogodio);
                    } else if(polje == 2){
                        postaviResenjeC(trenutniActivity,snapshot,pogodio);
                    } else if(polje == 3){
                        postaviResenjeD(trenutniActivity,snapshot,pogodio);
                    }
                }
            }
        }
    }


    public void azurirajOtvoreno(final Activity trenutniActivity, DataSnapshot snapshot, int red){

        String redString = redUString(red);

        String otvoreno = snapshot.child(redString + "otvoreno").getValue(String.class);
        String daLiJePogodio = snapshot.child(redString + "pogodio").getValue(String.class);
        String trenutniIgrac = snapshot.child("potez").getValue(String.class);
        if(otvoreno!=null && daLiJePogodio==null && !kojiJeIgrac.equals(trenutniIgrac)){
            ArrayList<Integer> otvorenoLista = StringUListuBrojeva(otvoreno);
            for(Integer broj:otvorenoLista){
                prikaziPolje(trenutniActivity,red,broj-1);
            }
        }

    }

    public void postaviResenjeA(final Activity trenutniActivity, DataSnapshot snapshot, String pogodio){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra" + pitanja.get(0));

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ((Asocijacije)trenutniActivity).postaviATekst(snapshot.child("A").child("Aodgovor").getValue(String.class));
                ((Asocijacije)trenutniActivity).zakljucajA();
                if (pogodio != null) {
                    if (pogodio.equals("igrac1") || pogodio.equals("igrac")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviAOdgovor(), R.color.plava);
                    } else if (pogodio.equals("igrac2")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviAOdgovor(), R.color.crvena);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void postaviResenjeB(final Activity trenutniActivity, DataSnapshot snapshot, String pogodio){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra" + pitanja.get(0));

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ((Asocijacije)trenutniActivity).postaviBTekst(snapshot.child("B").child("Bodgovor").getValue(String.class));
                ((Asocijacije)trenutniActivity).zakljucajB();
                if (pogodio != null) {
                    if (pogodio.equals("igrac1") || pogodio.equals("igrac")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviBOdgovor(), R.color.plava);
                    } else if (pogodio.equals("igrac2")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviBOdgovor(), R.color.crvena);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void postaviResenjeC(final Activity trenutniActivity, DataSnapshot snapshot, String pogodio){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra" + pitanja.get(0));

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ((Asocijacije)trenutniActivity).postaviCTekst(snapshot.child("C").child("Codgovor").getValue(String.class));
                ((Asocijacije)trenutniActivity).zakljucajC();
                if (pogodio != null) {
                    if (pogodio.equals("igrac1") || pogodio.equals("igrac")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviCOdgovor(), R.color.plava);
                    } else if (pogodio.equals("igrac2")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviCOdgovor(), R.color.crvena);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void postaviResenjeD(final Activity trenutniActivity, DataSnapshot snapshot, String pogodio){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra" + pitanja.get(0));

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ((Asocijacije)trenutniActivity).postaviDTekst(snapshot.child("D").child("Dodgovor").getValue(String.class));
                ((Asocijacije)trenutniActivity).zakljucajD();
                if (pogodio != null) {
                    if (pogodio.equals("igrac1") || pogodio.equals("igrac")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviDOdgovor(), R.color.plava);
                    } else if (pogodio.equals("igrac2")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviDOdgovor(), R.color.crvena);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void postaviResenjeKonacno(final Activity trenutniActivity, DataSnapshot snapshot, String pogodio){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("asocijacije").child("igra" + pitanja.get(0));

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ((Asocijacije)trenutniActivity).postaviKonacnoTekst(snapshot.child("konacanOdgovor").getValue(String.class));
                ((Asocijacije)trenutniActivity).zakljucajKonacno();
                if (pogodio != null) {
                    if (pogodio.equals("igrac1") || pogodio.equals("igrac")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviKonacanOdgovor(), R.color.plava);
                    } else if (pogodio.equals("igrac2")) {
                        ((Asocijacije)trenutniActivity).promeniBojuUnosa(((Asocijacije)trenutniActivity).dobaviKonacanOdgovor(), R.color.crvena);
                    }

                    narednaIgra(trenutniActivity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private ArrayList<Integer> StringUListuBrojeva(String input) {
        ArrayList<Integer> array = new ArrayList<>();
        if (input != null) {
            List<String> numberStrings = Arrays.asList(input.split(",\\s*"));
            for (String numberString : numberStrings) {
                try {
                    int number = Integer.parseInt(numberString);
                    array.add(number);
                } catch (NumberFormatException e) {

                }
            }
        }
        return array;
    }

    public void otkljucajDugmad(final Activity trenutniActivity){
        referencaNaAsocijacije.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ArrayList<ArrayList<Integer>> listaListi = new ArrayList<>();
                    String aOtvoreno = snapshot.child("Aotvoreno").getValue(String.class);
                    ArrayList<Integer> aLista = StringUListuBrojeva(aOtvoreno);
                    listaListi.add(aLista);
                    String bOtvoreno = snapshot.child("Botvoreno").getValue(String.class);
                    ArrayList<Integer> bLista = StringUListuBrojeva(bOtvoreno);
                    listaListi.add(bLista);
                    String cOtvoreno = snapshot.child("Cotvoreno").getValue(String.class);
                    ArrayList<Integer> cLista = StringUListuBrojeva(cOtvoreno);
                    listaListi.add(cLista);
                    String dOtvoreno = snapshot.child("Dotvoreno").getValue(String.class);
                    ArrayList<Integer> dLista = StringUListuBrojeva(dOtvoreno);
                    listaListi.add(dLista);

                    ((Asocijacije)trenutniActivity).otkljucajDugmad(listaListi);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void zapocniPotez(final Activity trenutniActivity){
        otkljucajDugmad(trenutniActivity);
        ((Asocijacije)trenutniActivity).zakljucajUpise();
        referencaNaAsocijacije.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String Aotvoreno = snapshot.child("Aotvoreno").getValue(String.class);
                    String BOtvoreno = snapshot.child("Botvoreno").getValue(String.class);
                    String COtvoreno = snapshot.child("Cotvoreno").getValue(String.class);
                    String DOtvoreno = snapshot.child("Dotvoreno").getValue(String.class);
                    if(Aotvoreno!=null && BOtvoreno!=null && COtvoreno!=null && DOtvoreno!=null){
                        if(Aotvoreno.length() == 10 && BOtvoreno.length() == 10 && COtvoreno.length() == 10 && DOtvoreno.length() == 10){
                            otkljucajUpise(trenutniActivity);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void zavrsiPotez(final Activity trenutniActivity){
        ((Asocijacije)trenutniActivity).sakrijTastaturu();
        referencaNaAsocijacije.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(kojiJeIgrac.equals(snapshot.child("potez").getValue(String.class))&&kojiJeIgrac.equals("igrac1")){
                        referencaNaAsocijacije.child("potez").setValue("igrac2");
                    }else if(kojiJeIgrac.equals(snapshot.child("potez").getValue(String.class))&&kojiJeIgrac.equals("igrac2")){
                        referencaNaAsocijacije.child("potez").setValue("igrac1");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void proveriPotez(final Activity trenutniActivity){
        ValueEventListener listenerZaProveruPoteza = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.getValue(String.class)!=null){
                        if(kojiJeIgrac.equals(snapshot.getValue(String.class))){
                            ((Asocijacije)trenutniActivity).skloniPrekrivac();
                            zapocniPotez(trenutniActivity);
                        }else{
                            ((Asocijacije)trenutniActivity).prikaziPrekrivac();
                            ((Asocijacije)trenutniActivity).zakljucajDugmad();
                            ((Asocijacije)trenutniActivity).zakljucajUpise();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listenerManager.addValueEventListener(referencaNaAsocijacije.child("potez"),listenerZaProveruPoteza);
    }

    public String transformisiString(String zaTransformaciju){
        String transformisanString ="";
        for(int i = 0; i < zaTransformaciju.length(); i++) {
            if (zaTransformaciju.charAt(i) == 'Č' || zaTransformaciju.charAt(i) == 'č') {
                transformisanString += 'c';
            } else if (zaTransformaciju.charAt(i) == 'Ć' || zaTransformaciju.charAt(i) == 'ć') {
                transformisanString += 'c';
            } else if (zaTransformaciju.charAt(i) == 'Š' || zaTransformaciju.charAt(i) == 'š') {
                transformisanString += 's';
            } else if (zaTransformaciju.charAt(i) == 'Ž' || zaTransformaciju.charAt(i) == 'ž') {
                transformisanString += 'z';
            } else if (zaTransformaciju.charAt(i) == 'Đ' || zaTransformaciju.charAt(i) == 'đ') {
                transformisanString += "dj";
            }else{
                transformisanString += zaTransformaciju.charAt(i);
            }
        }
        transformisanString = transformisanString.toLowerCase();
        return transformisanString;
    }

}
