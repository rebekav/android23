package com.example.slagalica.Kontroleri;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.slagalica.Activities.IgriceActivities.Spojnice;
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

public class SpojniceKontroler {
    private String idIgre;

    private String kojiJeIgrac;
    private Integer brojIgraca, redniBrojPartije;

    private DatabaseReference referencaNaIgru;

    private DatabaseReference referencaNaSpojnice;

    private ArrayList<Integer> pitanja;

    private FirebaseListenerManager listenerManager;

    private CountDownTimer tajmerZaKraj;

    private Integer brojPokusaja;

    private KontrolerKonekcije kontrolerKonekcije;

    private KontrolerKorisnika kontrolerKorisnika;

    private KontrolerDijaloga kontrolerDijaloga;

    private Handler handler;

    private Integer redniBrojPoteza;



    public SpojniceKontroler(String idIgre, Integer brojIgraca, Integer redniBrojPartije){
        this.idIgre = idIgre;
        this.brojIgraca = brojIgraca;
        this.redniBrojPartije = redniBrojPartije;
        this.brojPokusaja = 5;
        this.redniBrojPoteza = 1;
        this.listenerManager = new FirebaseListenerManager();
        this.kontrolerKonekcije = new KontrolerKonekcije();
        this.kontrolerKorisnika = new KontrolerKorisnika();
        this.kontrolerDijaloga = new KontrolerDijaloga();
        this.handler = new Handler();
        this.referencaNaIgru = FirebaseDatabase.getInstance().getReference("koren").child("aktivneIgre").child(idIgre);
        this.referencaNaSpojnice = referencaNaIgru.child("spojnice");
    }
    public FirebaseListenerManager dobaviListenerManager(){
        return listenerManager;
    }

    public KontrolerKonekcije dobaviKontrolerKonekcije(){return this.kontrolerKonekcije;}
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

    public void kreirajSpojnice(final Activity trenutniActivity){
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

            referencaNaSpojnice.child("pitanja").setValue(pitanjaZaUpisUBazu.toString());
            referencaNaSpojnice.child("igracpoeni").setValue(0);

            zapocniIgru(trenutniActivity);
        } else if(brojIgraca == 1 && redniBrojPartije == 2){
            postaviKojiJeIgrac("igrac");

            procitajPitanja(trenutniActivity);
        } else if(brojIgraca == 2 && redniBrojPartije == 1){
            kontrolerDijaloga.prikaziDijalogZaUcitavanje(trenutniActivity,"spojnice", "čekanje protivnika da uđe u igru");
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
                            referencaNaIgru.child("igrac1Lokacija").setValue("Spojnice");

                            pitanja = generisiNizSlucajnihBrojeva(1,4,2);
                            StringBuilder pitanjaZaUpisUBazu = new StringBuilder();
                            for (int i = 0; i < pitanja.size(); i++) {
                                pitanjaZaUpisUBazu.append(pitanja.get(i));

                                if (i < pitanja.size() - 1) {
                                    pitanjaZaUpisUBazu.append(", ");
                                }
                            }
                            referencaNaSpojnice.child("pitanja").setValue(pitanjaZaUpisUBazu.toString());
                            referencaNaSpojnice.child("igrac1poeni").setValue(0);
                            referencaNaSpojnice.child("igrac2poeni").setValue(0);
                            referencaNaSpojnice.child("stanjeIgre").setValue("igraJeSpremna");
                            referencaNaSpojnice.child("potez").setValue("igrac1");
                            referencaNaSpojnice.child("brojPoteza").setValue(1);

                            postaviKojiJeIgrac("igrac1");

                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String lokacija = dataSnapshot.getValue(String.class);
                                        if (lokacija.equals("Spojnice")) {
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

                            referencaNaIgru.child("igrac2Lokacija").setValue("Spojnice");
                            postaviKojiJeIgrac("igrac2");
                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String location = dataSnapshot.getValue(String.class);
                                        if (location.equals("Spojnice")) {
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
            kontrolerDijaloga.prikaziDijalogZaUcitavanje(trenutniActivity,"spojnice", "čekanje protivnika da uđe u igru");
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
                            referencaNaIgru.child("igrac1Lokacija").setValue("Spojnice");

                            procitajPitanja(trenutniActivity);
                            referencaNaSpojnice.child("stanjeIgre").setValue("igraJeSpremna");
                            referencaNaSpojnice.child("potez").setValue("igrac2");
                            referencaNaSpojnice.child("brojPoteza").setValue(1);
                            referencaNaSpojnice.child("neodgovoreno").setValue(null);


                            postaviKojiJeIgrac("igrac1");

                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String lokacija = dataSnapshot.getValue(String.class);
                                        if (lokacija.equals("Spojnice")) {
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

                            referencaNaIgru.child("igrac2Lokacija").setValue("Spojnice");
                            postaviKojiJeIgrac("igrac2");
                            ValueEventListener listenerZaLokacijuProtivnika = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String location = dataSnapshot.getValue(String.class);
                                        if (location.equals("Spojnice")) {
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
                        referencaNaSpojnice.child("stanjeIgre").removeEventListener(this);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        listenerManager.addValueEventListener(referencaNaSpojnice.child("stanjeIgre"), listenerZaStanjeIgre);
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
                        referencaNaSpojnice.child("stanjeIgre").setValue("igraJePocela")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            zapocniIgru(trenutniActivity);
                                        }
                                    }
                                });
                        referencaNaSpojnice.child("stanjeIgre").removeEventListener(this);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        listenerManager.addValueEventListener(referencaNaSpojnice.child("stanjeIgre"), listenerZaStanjeIgre);
    }

    private void procitajPitanja (final Activity trenutniActivity) {
        pitanja = new ArrayList<>();
        referencaNaSpojnice.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void ucitajPojmoveIPitanje(final Activity trenutniActivity){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("spojnice").child("igra" + Integer.toString(pitanja.get(0)));

        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    DataSnapshot leviPojmovi = snapshot.child("leviPojmovi");
                    DataSnapshot desniPojmovi = snapshot.child("desniPojmovi");

                    for(Integer i = 0; i<5;i++){

                        String broj = Integer.toString(i+1);


                        ((Spojnice)trenutniActivity).postaviTekstZaLeviPojam(i,leviPojmovi.child(broj).getValue(String.class));
                        ((Spojnice)trenutniActivity).postaviTekstZaDesniPojam(i,desniPojmovi.child(broj).child("odgovor").getValue(String.class));

                    }
                    ((Spojnice)trenutniActivity).postaviTekstZaPitanje(snapshot.child("pitanje").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void kontrolerZaLeveDugmadi(final Activity trenutniActivity, Integer staObojiti){
        referencaNaSpojnice.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<Integer> neotvoreno = new ArrayList<>();

                    for(Integer i = 1;i<=5;i++){
                        neotvoreno.add(i);
                    }

                    if(brojIgraca == 1){
                        String igracotvorio = snapshot.child("igracotvorio").getValue(String.class);
                        List<String> stringoviBrojeva = new ArrayList<String>();

                        if(igracotvorio != null){
                            stringoviBrojeva = Arrays.asList(igracotvorio.split(",\\s*"));

                            for (String stringBroj : stringoviBrojeva) {
                                try {
                                    int broj = 0;
                                    broj = Integer.parseInt(stringBroj);
                                    neotvoreno.remove(neotvoreno.indexOf(broj));
                                } catch (NumberFormatException e) {
                                }
                            }
                        }

                        String neodgovoreno = snapshot.child("neodgovoreno").getValue(String.class);

                        if(neodgovoreno != null){
                            stringoviBrojeva = Arrays.asList(neodgovoreno.split(",\\s*"));

                            for (String stringBroj : stringoviBrojeva) {
                                try {
                                    int broj = 0;
                                    broj = Integer.parseInt(stringBroj);
                                    neotvoreno.remove(neotvoreno.indexOf(broj));
                                } catch (NumberFormatException e) {
                                }
                            }
                        }

                        if(neotvoreno.isEmpty() == false){
                            for (int i = 0; i < neotvoreno.size(); i++) {
                                ((Spojnice)trenutniActivity).obojiLeviPojam("spojniceTekst", neotvoreno.get(i) - 1);
                            }
                        }

                        if(kojiJeIgrac.equals("igrac") || kojiJeIgrac.equals("igrac1")){
                            ((Spojnice)trenutniActivity).obojiLeviPojam("plava",staObojiti);
                        } else if(kojiJeIgrac.equals("igrac2")){
                            ((Spojnice)trenutniActivity).obojiLeviPojam("crvena",staObojiti);
                        }
                    } else if(brojIgraca == 2) {
                        String igrac1otvorio = snapshot.child("igrac1otvorio").getValue(String.class);
                        List<String> stringoviBrojeva = new ArrayList<String>();

                        if(igrac1otvorio != null){
                            stringoviBrojeva = Arrays.asList(igrac1otvorio.split(",\\s*"));

                            for (String stringBroj : stringoviBrojeva) {
                                try {
                                    int broj = 0;
                                    broj = Integer.parseInt(stringBroj);
                                    neotvoreno.remove(neotvoreno.indexOf(broj));
                                } catch (NumberFormatException e) {
                                }
                            }
                        }


                        String igrac2otvorio = snapshot.child("igrac2otvorio").getValue(String.class);

                        if(igrac2otvorio != null){
                            stringoviBrojeva = Arrays.asList(igrac2otvorio.split(",\\s*"));

                            for (String stringBroj : stringoviBrojeva) {
                                try {
                                    int broj = 0;
                                    broj = Integer.parseInt(stringBroj);
                                    neotvoreno.remove(neotvoreno.indexOf(broj));
                                } catch (NumberFormatException e) {
                                }
                            }
                        }

                        String neodgovoreno = snapshot.child("neodgovoreno").getValue(String.class);

                        if(neodgovoreno != null){
                            stringoviBrojeva = Arrays.asList(neodgovoreno.split(",\\s*"));

                            for (String stringBroj : stringoviBrojeva) {
                                try {
                                    int broj = 0;
                                    broj = Integer.parseInt(stringBroj);
                                    neotvoreno.remove(neotvoreno.indexOf(broj));
                                } catch (NumberFormatException e) {
                                }
                            }
                        }

                        if(neotvoreno.isEmpty() == false){
                            for (int i = 0; i < neotvoreno.size(); i++) {
                                ((Spojnice)trenutniActivity).obojiLeviPojam("spojniceTekst", neotvoreno.get(i) - 1);
                            }
                        }

                        if(kojiJeIgrac.equals("igrac") || kojiJeIgrac.equals("igrac1")){
                            ((Spojnice)trenutniActivity).obojiLeviPojam("plava",staObojiti);
                        } else if(kojiJeIgrac.equals("igrac2")){
                            ((Spojnice)trenutniActivity).obojiLeviPojam("crvena",staObojiti);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void daLiJeIgraGotova(final Activity trenutniActivity){
        if(brojIgraca == 1){
            ValueEventListener listenerZaKrajIgre = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String igracotvorio = snapshot.child("igracotvorio").getValue(String.class);
                        Integer brojac = 0;

                        if(igracotvorio!=null && !igracotvorio.equals("")){
                            for(int i = 0;i<igracotvorio.length();i++){
                                if (igracotvorio.charAt(i) == ',') {
                                    brojac++;
                                }
                            }
                            if(brojac == 0){
                                brojac = 1;
                            } else brojac++;
                        }

                        String neodgovoreno = snapshot.child("neodgovoreno").getValue(String.class);

                        if(neodgovoreno!=null && !neodgovoreno.equals("")){
                            for(int i = 0;i<neodgovoreno.length();i++){
                                if (neodgovoreno.charAt(i) == ',') {
                                    brojac++;
                                }
                            }
                            if(brojac == 0){
                                brojac = 1;
                            } else brojac++;
                        }



                        if(brojac == 5){
                            if(pitanja.size() == 1){
                                zavrsiIgru(trenutniActivity);
                            }else{
                                narednaIgra(trenutniActivity);
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            listenerManager.addValueEventListener(referencaNaSpojnice, listenerZaKrajIgre);
        } else if(brojIgraca == 2) {
            ValueEventListener listenerZaKrajIgre = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String igrac1otvorio = snapshot.child("igrac1otvorio").getValue(String.class);
                        Integer brojac = 0;

                        if(igrac1otvorio!=null && !igrac1otvorio.equals("")){
                            for(int i = 0;i<igrac1otvorio.length();i++){
                                if (igrac1otvorio.charAt(i) == ',') {
                                    brojac++;
                                }
                            }
                            if(brojac == 0){
                                brojac = 1;
                            } else brojac++;
                        }

                        if(brojac == 5){
                            if(pitanja.size() == 1){
                                zavrsiIgru(trenutniActivity);
                            }else{
                                narednaIgra(trenutniActivity);
                            }
                            return;
                        }


                        String igrac2otvorio = snapshot.child("igrac2otvorio").getValue(String.class);

                        if(igrac2otvorio!=null && !igrac2otvorio.equals("")){
                            for(int i = 0;i<igrac2otvorio.length();i++){
                                if (igrac2otvorio.charAt(i) == ',') {
                                    brojac++;
                                }
                            }
                            if(brojac == 0){
                                brojac = 1;
                            } else brojac++;
                        }

                        if(brojac == 5){
                            if(pitanja.size() == 1){
                                zavrsiIgru(trenutniActivity);
                            }else{
                                narednaIgra(trenutniActivity);
                            }
                            return;
                        }


                        String neodgovoreno = snapshot.child("neodgovoreno").getValue(String.class);

                        if(neodgovoreno!=null && !neodgovoreno.equals("")){
                            for(int i = 0;i<neodgovoreno.length();i++){
                                if (neodgovoreno.charAt(i) == ',') {
                                    brojac++;
                                }
                            }
                            if(brojac == 0){
                                brojac = 1;
                            } else brojac++;
                        }


                        Integer rBrojPoteza = snapshot.child("brojPoteza").getValue(Integer.class);

                        if(rBrojPoteza == 3 && redniBrojPartije == 1){
                            narednaIgra(trenutniActivity);
                            return;
                        } else if(rBrojPoteza == 3 && redniBrojPartije == 2){
                            zavrsiIgru(trenutniActivity);
                            return;
                        }


                        if(brojac == 5 && rBrojPoteza == 3 && redniBrojPartije == 1){
                            narednaIgra(trenutniActivity);
                        } else if(brojac == 5 && rBrojPoteza == 3 && redniBrojPartije == 2){
                            zavrsiIgru(trenutniActivity);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            listenerManager.addValueEventListener(referencaNaSpojnice, listenerZaKrajIgre);
        }
    }


    public void zapocniIgru(final Activity trenutniActivity){
        ucitajPojmoveIPitanje(trenutniActivity);

        if(brojIgraca == 2){
            proveriPotez(trenutniActivity);
            postaviListenerZaOdgovore(trenutniActivity);
        }
        daLiJeIgraGotova(trenutniActivity);
        tajmerZaKraj = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                ((Spojnice)trenutniActivity).azurirajVreme(Long.toString(millisUntilFinished/1000));
            }
            public void onFinish() {
                if(brojIgraca == 1){
                    if(pitanja.size()==1) {
                        zavrsiIgru(trenutniActivity);
                    }else{
                        narednaIgra(trenutniActivity);
                    }
                }else if(brojIgraca == 2){
                    if(redniBrojPartije == 1){
                        narednaIgra(trenutniActivity);
                    } else if(redniBrojPartije == 2){
                        zavrsiIgru(trenutniActivity);
                    }
                }

            }
        }.start();
    }

    public void narednaIgra(final Activity trenutniActivity){
        iskljuciTajmer();
        listenerManager.removeAllListeners();
        ((Spojnice)trenutniActivity).skloniPrekrivac();
        ((Spojnice)trenutniActivity).prikaziPrekrivac();

        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {

                if(brojIgraca == 1) {
                    referencaNaSpojnice.child("pitanja").setValue(pitanja.get(1).toString());
                    referencaNaSpojnice.child("igracotvorio").setValue("");
                    referencaNaSpojnice.child("neodgovoreno").setValue(null);
                } else if(brojIgraca == 2){
                    if(kojiJeIgrac.equals("igrac1")){
                        referencaNaSpojnice.child("igrac1otvorio").setValue("");
                        referencaNaSpojnice.child("igrac2otvorio").setValue("");
                        referencaNaSpojnice.child("neodgovoreno").setValue(null);
                        referencaNaSpojnice.child("brojPoteza").setValue(1);
                        referencaNaSpojnice.child("pitanja").setValue(pitanja.get(1).toString());
                    }
                }

                ((Spojnice)trenutniActivity).postaviNameruIzlaska(false);
                Intent intent = new Intent(trenutniActivity, Spojnice.class);
                intent.putExtra("IGRA_ID", idIgre);
                intent.putExtra("BROJ_IGRACA", brojIgraca);
                intent.putExtra("REDNI_BROJ_PARTIJE", 2);
                intent.putExtra("NAREDNA_IGRA", "Asocijacije");
                trenutniActivity.startActivity(intent);
                trenutniActivity.finish();

            }
        }.start();
    }


    public void zavrsiIgru(final Activity trenutniActivity){
        iskljuciTajmer();
        listenerManager.removeAllListeners();
        ((Spojnice)trenutniActivity).skloniPrekrivac();
        ((Spojnice)trenutniActivity).prikaziPrekrivac();
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {

                if(brojIgraca == 1){
                    referencaNaSpojnice.child("igracotvorio").setValue("");
                    referencaNaSpojnice.child("neodgovoreno").setValue(null);
                } else if(brojIgraca == 2){
                    referencaNaSpojnice.child("igrac1otvorio").setValue("");
                    referencaNaSpojnice.child("igrac2otvorio").setValue("");
                    referencaNaSpojnice.child("neodgovoreno").setValue(null);
                    referencaNaSpojnice.child("brojPoteza").setValue(1);
                }

                Toast.makeText(trenutniActivity, "kraj igre spojnice.", Toast.LENGTH_SHORT).show();
                ((Spojnice)trenutniActivity).postaviNameruIzlaska(false);
                ((Spojnice)trenutniActivity).onBackPressed();

            }
        }.start();
    }


    public void proveriOdgovor(final Activity trenutniActivity, final Integer levoDugme, final Integer desnoDugme){
        DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("spojnice").child("igra" + pitanja.get(0));
        referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    DataSnapshot leviPojmovi = snapshot.child("leviPojmovi");
                    DataSnapshot desniPojmovi = snapshot.child("desniPojmovi");

                    Integer tacanOdgovor = desniPojmovi.child(Integer.toString(desnoDugme+1)).child("ideUz").getValue(Integer.class);

                    if(tacanOdgovor == levoDugme + 1){
                        referencaNaSpojnice.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    String otvorenaPolja = snapshot.child(kojiJeIgrac + "otvorio").getValue(String.class);
                                    int brojac = 0;

                                    if(otvorenaPolja!=null){
                                        for(int i = 0;i<otvorenaPolja.length();i++){
                                            if (otvorenaPolja.charAt(i) == ',') {
                                                brojac++;
                                            }
                                        }
                                    }


                                    if(brojac==0 && (otvorenaPolja==null || otvorenaPolja.equals(""))){
                                        otvorenaPolja = Integer.toString(levoDugme+1);
                                    } else {
                                        otvorenaPolja += ", "+Integer.toString(levoDugme+1);
                                    }

                                    referencaNaSpojnice.child(kojiJeIgrac+"otvorio").setValue(otvorenaPolja);

                                    if(kojiJeIgrac.equals("igrac") || kojiJeIgrac.equals("igrac1")){
                                        ((Spojnice)trenutniActivity).obojiPar("plava",levoDugme,desnoDugme);
                                    } else {
                                        ((Spojnice)trenutniActivity).obojiPar("crvena",levoDugme,desnoDugme);
                                    }

                                    Integer poeni = snapshot.child(kojiJeIgrac + "poeni").getValue(Integer.class);
                                    poeni += 2;
                                    referencaNaSpojnice.child(kojiJeIgrac + "poeni").setValue(poeni);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        referencaNaSpojnice.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    ((Spojnice)trenutniActivity).zakljucajLevoDugme(levoDugme);
                                    ((Spojnice)trenutniActivity).obojiLeviPojam("crna", levoDugme);
                                    String neodgovoreno = snapshot.child("neodgovoreno").getValue(String.class);
                                    int brojac = 0;

                                    if(neodgovoreno!=null){
                                        for(int i = 0;i<neodgovoreno.length();i++){
                                            if (neodgovoreno.charAt(i) == ',') {
                                                brojac++;
                                            }
                                        }
                                    }


                                    if(brojac==0 && (neodgovoreno==null || neodgovoreno.equals(""))){
                                        neodgovoreno = Integer.toString(levoDugme+1);
                                    } else {
                                        neodgovoreno += ", "+Integer.toString(levoDugme+1);
                                    }

                                    referencaNaSpojnice.child("neodgovoreno").setValue(neodgovoreno);
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

        new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                brojPokusaja--;
                if(brojPokusaja == 0 && brojIgraca == 2){
                    zavrsiPotez();
                }
            }
        }.start();
    }


    public void iskljuciTajmer(){
        if(tajmerZaKraj != null){
            tajmerZaKraj.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void proveriPotez(final Activity trenutniActivity){
        ValueEventListener potezListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.getValue(String.class)!=null){
                        if(kojiJeIgrac.equals(snapshot.getValue(String.class))){
                            ((Spojnice)trenutniActivity).skloniPrekrivac();
                            zapocniPotez(trenutniActivity);
                        }else{
                            ((Spojnice)trenutniActivity).prikaziPrekrivac();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        listenerManager.addValueEventListener(referencaNaSpojnice.child("potez"), potezListener);
    }


    public void oslobodiDugmad(final Activity trenutniActivity) {
        referencaNaSpojnice.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String neodgovoreno = snapshot.child("neodgovoreno").getValue(String.class);
                    if(neodgovoreno!=null){
                        List<String> stringBrojeva = Arrays.asList(neodgovoreno.split(",\\s*"));
                        for (String brojString : stringBrojeva) {
                            try {
                                int broj = 0;
                                broj= Integer.parseInt(brojString);
                                ((Spojnice)trenutniActivity).otkljucajLevoDugme(broj - 1);
                                ((Spojnice)trenutniActivity).obojiLeviPojam("spojniceTekst",broj - 1);

                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void zapocniPotez(final Activity trenutniActivity){
        referencaNaSpojnice.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String neodgovoreno = snapshot.child("neodgovoreno").getValue(String.class);


                    int brojac = 0;
                    if(neodgovoreno!=null && neodgovoreno.equals("")){
                        brojPokusaja = 0;
                    }
                    else if(neodgovoreno!=null) {

                        for(int i = 0;i<neodgovoreno.length();i++){
                            if (neodgovoreno.charAt(i) == ',') {
                                brojac++;
                            }
                        }
                        brojPokusaja = brojac + 1;
                    }
                    else {
                        brojPokusaja = 5;
                    }

                    oslobodiDugmad(trenutniActivity);


                    referencaNaSpojnice.child("neodgovoreno").setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void zavrsiPotez(){
        referencaNaSpojnice.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(kojiJeIgrac.equals(snapshot.child("potez").getValue(String.class))&&kojiJeIgrac.equals("igrac1")){
                        referencaNaSpojnice.child("potez").setValue("igrac2");
                    }else if(kojiJeIgrac.equals(snapshot.child("potez").getValue(String.class))&&kojiJeIgrac.equals("igrac2")){
                        referencaNaSpojnice.child("potez").setValue("igrac1");
                    }
                    redniBrojPoteza = snapshot.child("brojPoteza").getValue(Integer.class);

                    redniBrojPoteza++;

                    referencaNaSpojnice.child("brojPoteza").setValue(redniBrojPoteza);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postaviListenerZaOdgovore(final Activity trenutniActivity){

        ValueEventListener odgovoriListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                if(Snapshot.exists()){
                    String trenutniIgrac = Snapshot.child("potez").getValue(String.class);

                    if(trenutniIgrac!=null&&kojiJeIgrac!=null){

                        if(kojiJeIgrac!=null&&kojiJeIgrac.equals("igrac2")){
                            ValueEventListener promenaIgrac1 = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        List<Integer> otvorenoBrojevi = new ArrayList<>();
                                        String otvorenoString = snapshot.getValue(String.class);

                                        List<String> brojStringovi = new ArrayList<String>();

                                        if (otvorenoString != null) {
                                            brojStringovi = Arrays.asList(otvorenoString.split(",\\s*"));
                                            for (String trenutanBroj : brojStringovi) {
                                                try {
                                                    int broj = 0;
                                                    broj = Integer.parseInt(trenutanBroj);
                                                    otvorenoBrojevi.add(broj);
                                                } catch (NumberFormatException e) {
                                                }
                                            }
                                        }
                                        if(!otvorenoBrojevi.isEmpty()){

                                            for(int i = 0; i<otvorenoBrojevi.size();i++){
                                                final Integer broj = otvorenoBrojevi.get(i)-1;

                                                DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("spojnice").child("igra" + pitanja.get(0));


                                                referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                        DataSnapshot desniPojmovi = snapshot.child("desniPojmovi");

                                                        Integer indeksNadjenog = 0;

                                                        for(DataSnapshot trenutniPojam:desniPojmovi.getChildren()){
                                                            Integer ideUz = trenutniPojam.child("ideUz").getValue(Integer.class);
                                                            if(ideUz == broj + 1){
                                                                ((Spojnice)trenutniActivity).obojiPar("plava",broj, indeksNadjenog);
                                                                break;
                                                            }
                                                            indeksNadjenog ++;

                                                        }

                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            };
                            listenerManager.addValueEventListener(referencaNaSpojnice.child("igrac1otvorio"),promenaIgrac1);
                        } else if(kojiJeIgrac!=null&&kojiJeIgrac.equals("igrac1")){
                            ValueEventListener promenaIgrac2 = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        List<Integer> otvorenoBrojevi = new ArrayList<>();
                                        String otvorenoString = snapshot.getValue(String.class);

                                        List<String> brojStringovi = new ArrayList<String>();

                                        if (otvorenoString != null) {
                                            brojStringovi = Arrays.asList(otvorenoString.split(",\\s*"));
                                            for (String trenutanBroj : brojStringovi) {
                                                try {
                                                    int broj = 0;
                                                    broj = Integer.parseInt(trenutanBroj);
                                                    otvorenoBrojevi.add(broj);
                                                } catch (NumberFormatException e) {
                                                }
                                            }
                                        }
                                        if(!otvorenoBrojevi.isEmpty()){

                                            for(int i = 0; i<otvorenoBrojevi.size();i++){
                                                final Integer broj = otvorenoBrojevi.get(i)-1;

                                                DatabaseReference referencaNaPitanje = FirebaseDatabase.getInstance().getReference("koren").child("igrice").child("spojnice").child("igra" + pitanja.get(0));

                                                referencaNaPitanje.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                        DataSnapshot desniPojmovi = snapshot.child("desniPojmovi");

                                                        Integer indeksNadjenog = 0;

                                                        for(DataSnapshot trenutniPojam:desniPojmovi.getChildren()){
                                                            Integer ideUz = trenutniPojam.child("ideUz").getValue(Integer.class);
                                                            if(ideUz == broj + 1){
                                                                ((Spojnice)trenutniActivity).obojiPar("crvena",broj, indeksNadjenog);
                                                                break;
                                                            }
                                                            indeksNadjenog ++;

                                                        }

                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            };
                            listenerManager.addValueEventListener(referencaNaSpojnice.child("igrac2otvorio"),promenaIgrac2);
                        }

                        ValueEventListener listenerZaNeodgovorene = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                                if(Snapshot.exists()){
                                    List<Integer> neodgovorenoBrojevi = new ArrayList<>();

                                    String neodgovoreno = Snapshot.getValue(String.class);

                                    List<String> brojStringovi;

                                    if (neodgovoreno != null) {
                                        brojStringovi = Arrays.asList(neodgovoreno.split(",\\s*"));

                                        for (String trenutanBroj : brojStringovi) {
                                            try {
                                                int broj = 0;
                                                broj = Integer.parseInt(trenutanBroj);
                                                neodgovorenoBrojevi.add(broj);
                                            } catch (NumberFormatException e) {
                                            }
                                        }
                                    }

                                    if(!neodgovorenoBrojevi.isEmpty()){
                                        for(int i = 0; i<neodgovorenoBrojevi.size();i++){
                                            ((Spojnice)trenutniActivity).zakljucajLevoDugme(neodgovorenoBrojevi.get(i) - 1);
                                            ((Spojnice)trenutniActivity).obojiLeviPojam("crna", neodgovorenoBrojevi.get(i) - 1);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        };

                        listenerManager.addValueEventListener(referencaNaSpojnice.child("neodgovoreno"),listenerZaNeodgovorene);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        listenerManager.addValueEventListener(referencaNaSpojnice,odgovoriListener);

    }


}
