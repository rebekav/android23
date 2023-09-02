package com.example.slagalica.Kontroleri;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.slagalica.Activities.IgriceActivities.KorakPoKorak;
import com.example.slagalica.PomocneKlase.FirebaseListenerManager;
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

public class KorakPoKorakKontroler {

    private String idIgre;

    private String kojiJeIgrac;
    private Integer brojIgraca, redniBrojPartije;

    private Integer trenutniKorak, trenutniBrojPoena;

    private DatabaseReference referencaNaIgru;

    private DatabaseReference referencaNaKorakPoKorak;

    private ArrayList<Integer> pitanja;

    private FirebaseListenerManager listenerManager;

    private CountDownTimer tajmerZaKraj, tajmerZaKorak;

    private KontrolerKonekcije kontrolerKonekcije;

    private KontrolerKorisnika kontrolerKorisnika;

    private KontrolerDijaloga kontrolerDijaloga;

    private Handler handler;



    public KorakPoKorakKontroler(String idIgre, Integer brojIgraca, Integer redniBrojPartije){
        this.idIgre = idIgre;
        this.brojIgraca = brojIgraca;
        this.redniBrojPartije = redniBrojPartije;
        this.trenutniKorak = 6;
        this.trenutniBrojPoena = 20;
        this.listenerManager = new FirebaseListenerManager();
        this.kontrolerKonekcije = new KontrolerKonekcije();
        this.kontrolerDijaloga = new KontrolerDijaloga();
        this.kontrolerKonekcije = new KontrolerKonekcije();
        this.kontrolerKorisnika = new KontrolerKorisnika();
        this.handler = new Handler();
        this.referencaNaIgru = FirebaseDatabase.getInstance().getReference("koren").child("aktivneIgre").child(idIgre);
        this.referencaNaKorakPoKorak = referencaNaIgru.child("korakPoKorak");
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

    public void kreirajKorakPoKorak(final Activity trenutniActivity){
        if(brojIgraca == 1 && redniBrojPartije == 1){

            pitanja = generisiNizSlucajnihBrojeva(1,4,2);
            StringBuilder pitanjaZaUpisUBazu = new StringBuilder();
            for (int i = 0; i < pitanja.size(); i++) {
                pitanjaZaUpisUBazu.append(pitanja.get(i));

                if (i < pitanja.size() - 1) {
                    pitanjaZaUpisUBazu.append(", ");
                }
            }

            postaviKojiJeIgrac("igrac");

            referencaNaKorakPoKorak.child("pitanja").setValue(pitanjaZaUpisUBazu.toString());
            referencaNaKorakPoKorak.child("igracpoeni").setValue(0);
            referencaNaKorakPoKorak.child("koJeResio").setValue(null);
            referencaNaKorakPoKorak.child("zavrsenaIgra").setValue(false);

            zapocniIgru(trenutniActivity);
        } else if(brojIgraca == 1 && redniBrojPartije == 2){
            postaviKojiJeIgrac("igrac");
            referencaNaKorakPoKorak.child("koJeResio").setValue(null);
            referencaNaKorakPoKorak.child("zavrsenaIgra").setValue(false);
            procitajPitanja(trenutniActivity);
        } else if(brojIgraca == 2 && redniBrojPartije == 1){
            kontrolerDijaloga.prikaziDijalogZaUcitavanje(trenutniActivity,"korak po korak", "čekanje protivnika da uđe u igru");
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
                            referencaNaIgru.child("igrac1Lokacija").setValue("KorakPoKorak");

                            pitanja = generisiNizSlucajnihBrojeva(1,4,2);
                            StringBuilder pitanjaZaUpisUBazu = new StringBuilder();
                            for (int i = 0; i < pitanja.size(); i++) {
                                pitanjaZaUpisUBazu.append(pitanja.get(i));

                                if (i < pitanja.size() - 1) {
                                    pitanjaZaUpisUBazu.append(", ");
                                }
                            }
                            referencaNaKorakPoKorak.child("pitanja").setValue(pitanjaZaUpisUBazu.toString());
                            referencaNaKorakPoKorak.child("igrac1poeni").setValue(0);
                            referencaNaKorakPoKorak.child("igrac2poeni").setValue(0);
                            referencaNaKorakPoKorak.child("stanjeIgre").setValue("igraJeSpremna");
                            referencaNaKorakPoKorak.child("potez").setValue("igrac1");
                            referencaNaKorakPoKorak.child("koJeResio").setValue(null);
                            referencaNaKorakPoKorak.child("zavrsenaIgra").setValue(false);

                            postaviKojiJeIgrac("igrac1");

                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String lokacija = dataSnapshot.getValue(String.class);
                                        if (lokacija.equals("KorakPoKorak")) {
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

                            referencaNaIgru.child("igrac2Lokacija").setValue("KorakPoKorak");
                            postaviKojiJeIgrac("igrac2");
                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String location = dataSnapshot.getValue(String.class);
                                        if (location.equals("KorakPoKorak")) {
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
            kontrolerDijaloga.prikaziDijalogZaUcitavanje(trenutniActivity,"korak po korak", "čekanje protivnika da uđe u igru");
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
                            referencaNaIgru.child("igrac1Lokacija").setValue("KorakPoKorak");

                            procitajPitanja(trenutniActivity);
                            referencaNaKorakPoKorak.child("stanjeIgre").setValue("igraJeSpremna");
                            referencaNaKorakPoKorak.child("potez").setValue("igrac2");
                            referencaNaKorakPoKorak.child("koJeResio").setValue(null);
                            referencaNaKorakPoKorak.child("zavrsenaIgra").setValue(false);

                            postaviKojiJeIgrac("igrac1");

                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String lokacija = dataSnapshot.getValue(String.class);
                                        if (lokacija.equals("KorakPoKorak")) {
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

                            referencaNaIgru.child("igrac2Lokacija").setValue("KorakPoKorak");
                            postaviKojiJeIgrac("igrac2");
                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String location = dataSnapshot.getValue(String.class);
                                        if (location.equals("KorakPoKorak")) {
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
                        referencaNaKorakPoKorak.child("stanjeIgre").removeEventListener(this);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        listenerManager.addValueEventListener(referencaNaKorakPoKorak.child("stanjeIgre"), listenerZaStanjeIgre);
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
                        referencaNaKorakPoKorak.child("stanjeIgre").setValue("igraJePocela")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            zapocniIgru(trenutniActivity);
                                        }
                                    }
                                });
                        referencaNaKorakPoKorak.child("stanjeIgre").removeEventListener(this);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        listenerManager.addValueEventListener(referencaNaKorakPoKorak.child("stanjeIgre"), listenerZaStanjeIgre);
    }

    public void proveriOdgovor(final Activity trenutniActivity, final String unesenOdgovor){
        DatabaseReference referencaNaResenje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("korakpokorak").child("igra" + pitanja.get(0).toString()).child("odgovor");
        referencaNaResenje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                if(Snapshot.exists()){
                    String str = Snapshot.getValue(String.class);
                    if(transformisiString(str).equals((transformisiString(unesenOdgovor)))){
                        referencaNaKorakPoKorak.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Integer poeni = snapshot.child(kojiJeIgrac + "poeni").getValue(Integer.class);
                                    if(trenutniKorak!=7){
                                        poeni += trenutniBrojPoena;
                                    } else {
                                        poeni += 5;
                                    }
                                    referencaNaKorakPoKorak.child(kojiJeIgrac + "poeni").setValue(poeni);
                                    referencaNaKorakPoKorak.child("zavrsenaIgra").setValue(true);
                                    referencaNaKorakPoKorak.child("koJeResio").setValue(kojiJeIgrac);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        ((KorakPoKorak)trenutniActivity).obrisiUnos();
                    }
                } else {
                    ((KorakPoKorak)trenutniActivity).obrisiUnos();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void postaviListenerZaOdgovorenoPitanje(final Activity trenutniActivity){
        ValueEventListener listenerZaOdgovorenoPitanje = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String koJeResio = snapshot.getValue(String.class);
                    if(koJeResio!=null){
                        DatabaseReference referencaNaResenje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("korakpokorak").child("igra" + pitanja.get(0).toString()).child("odgovor");
                        referencaNaResenje.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    ((KorakPoKorak)trenutniActivity).postaviUnos(snapshot.getValue(String.class));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        ((KorakPoKorak)trenutniActivity).zabraniUnos();
                        if(koJeResio.equals("igrac1") || koJeResio.equals("igrac")){
                            ((KorakPoKorak)trenutniActivity).obojiUnos("plava");
                        }else if(koJeResio.equals("igrac2")){
                            ((KorakPoKorak)trenutniActivity).obojiUnos("crvena");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        listenerManager.addValueEventListener(referencaNaKorakPoKorak.child("koJeResio"),listenerZaOdgovorenoPitanje);
    }

    public void postaviListenerZaKrajIgre(final Activity trenutniActivity){
        ValueEventListener listenerZaKrajIgre = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.getValue(Boolean.class)){
                        narednaIgra(trenutniActivity);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        listenerManager.addValueEventListener(referencaNaKorakPoKorak.child("zavrsenaIgra"),listenerZaKrajIgre);
    }

    public void tajmerKoraka(final Activity trenutniActivity){
        tajmerZaKorak = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {

            }
            public void onFinish() {
                otkrijNaredniKorak(trenutniActivity);
            }
        }.start();
    }

    public void iskljuciTajmere(){
        if(tajmerZaKraj != null){
            tajmerZaKraj.cancel();
        }
        if(tajmerZaKorak != null){
            tajmerZaKorak.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void otkrijNaredniKorak(final Activity trenutniActivity){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("korakpokorak").child("igra" + pitanja.get(0).toString());

        if(trenutniKorak >= 0){
            if(trenutniKorak != 6){
                trenutniBrojPoena-=2;
            }
            referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String tekst = snapshot.child(Integer.toString(trenutniKorak+1)).getValue(String.class);
                    ((KorakPoKorak)trenutniActivity).otkrijKorak(trenutniKorak, tekst);
                    trenutniKorak--;
                    tajmerKoraka(trenutniActivity);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            if(tajmerZaKorak!=null){
                tajmerZaKorak.cancel();
            }
            if(brojIgraca == 2) {
                ValueEventListener listenerZaPromenuPoteza = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String cijiJePotez = snapshot.child("potez").getValue(String.class);
                            Boolean potezPromenjen = snapshot.child("potezPromenjen").getValue(Boolean.class);
                            if(cijiJePotez.equals(kojiJeIgrac) && potezPromenjen == null)
                                zavrsiPotez(trenutniActivity);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };

                listenerManager.addValueEventListener(referencaNaKorakPoKorak, listenerZaPromenuPoteza);
            }
        }
    }

    private void procitajPitanja (final Activity trenutniActivity) {
        pitanja = new ArrayList<>();
        referencaNaKorakPoKorak.addListenerForSingleValueEvent(new ValueEventListener() {
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


    public void zapocniIgru(final Activity trenutniActivity){
        if(brojIgraca == 2){
            proveriPotez(trenutniActivity);
        }
        postaviListenerZaKrajIgre(trenutniActivity);
        postaviListenerZaOdgovorenoPitanje(trenutniActivity);

        if(brojIgraca == 1){
            tajmerZaKraj = new CountDownTimer(80000, 1000) {
                public void onTick(long millisUntilFinished) {
                    ((KorakPoKorak)trenutniActivity).azurirajVreme(Long.toString(millisUntilFinished/1000));
                }
                public void onFinish() {
                    narednaIgra(trenutniActivity);
                }
            }.start();
        } else if(brojIgraca == 2){
            tajmerZaKraj = new CountDownTimer(80000, 1000) {
                public void onTick(long millisUntilFinished) {
                    if(millisUntilFinished>10000)
                        ((KorakPoKorak)trenutniActivity).azurirajVreme(Long.toString(millisUntilFinished/1000 - 10));
                    else
                        ((KorakPoKorak)trenutniActivity).azurirajVreme(Long.toString(millisUntilFinished/1000));
                }
                public void onFinish() {
                    narednaIgra(trenutniActivity);
                }
            }.start();
        }
        otkrijNaredniKorak(trenutniActivity);
    }

    public void narednaIgra(final Activity trenutniActivity){
        iskljuciTajmere();
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                listenerManager.removeAllListeners();

                if(pitanja.size()==1){
                    ((KorakPoKorak)trenutniActivity).postaviNameruIzlaska(false);
                    Toast.makeText(trenutniActivity, "kraj igre korak po korak.", Toast.LENGTH_SHORT).show();
                    ((KorakPoKorak)trenutniActivity).onBackPressed();
                    return;
                }

                pitanja.remove(0);

                referencaNaKorakPoKorak.child("pitanja").setValue(Integer.toString(pitanja.get(0)));
                referencaNaKorakPoKorak.child("potezPromenjen").setValue(null);

                ((KorakPoKorak)trenutniActivity).postaviNameruIzlaska(false);
                Intent intent = new Intent(trenutniActivity, KorakPoKorak.class);
                intent.putExtra("IGRA_ID", idIgre);
                intent.putExtra("BROJ_IGRACA", brojIgraca);
                intent.putExtra("REDNI_BROJ_PARTIJE", 2);
                intent.putExtra("NAREDNA_IGRA", "Kraj");
                trenutniActivity.startActivity(intent);
                trenutniActivity.finish();
            }
        }.start();
    }


    public void proveriPotez(final Activity trenutniActivity){
        referencaNaKorakPoKorak.child("potez").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.getValue(String.class)!=null){
                        if(kojiJeIgrac.equals(snapshot.getValue(String.class))){
                            ((KorakPoKorak)trenutniActivity).skloniPrekrivac();
                            zapocniPotez();
                        }else{
                            ((KorakPoKorak)trenutniActivity).prikaziPrekrivac();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void zapocniPotez(){

    }

    public void zavrsiPotez(final Activity trenutniActivity){
        ((KorakPoKorak)trenutniActivity).sakrijTastaturu();
        referencaNaKorakPoKorak.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    referencaNaKorakPoKorak.child("potezPromenjen").setValue(true);
                    if(kojiJeIgrac.equals(snapshot.child("potez").getValue(String.class))&&kojiJeIgrac.equals("igrac1")){
                        referencaNaKorakPoKorak.child("potez").setValue("igrac2");
                    }else if(kojiJeIgrac.equals(snapshot.child("potez").getValue(String.class))&&kojiJeIgrac.equals("igrac2")){
                        referencaNaKorakPoKorak.child("potez").setValue("igrac1");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
