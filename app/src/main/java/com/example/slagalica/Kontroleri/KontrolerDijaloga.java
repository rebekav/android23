package com.example.slagalica.Kontroleri;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.slagalica.Activities.OstaliActivities.DvaIgraca;
import com.example.slagalica.Activities.KorisnikActivities.MainActivity;
import com.example.slagalica.Activities.KorisnikActivities.RegistrovanKorisnik;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class KontrolerDijaloga {
    private Dialog dialog;

    private KontrolerKorisnika kontrolerKorisnika;

    public KontrolerDijaloga(){
        kontrolerKorisnika = new KontrolerKorisnika();
    }

    public Dialog dobaviDijalog(){
        return dialog;
    }

    public void prikaziPozivZaIgru(final Activity trenutniActivity, String idIgre){
        DatabaseReference referencaIgre = FirebaseDatabase.getInstance().getReference("koren").child("aktivneIgre").child(idIgre);
        DatabaseReference referencaNaIgraca = FirebaseDatabase.getInstance().getReference("koren").child("korisnici").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        AlertDialog.Builder builder = new AlertDialog.Builder(trenutniActivity);
        builder.setTitle("Poziv")
                .setMessage("Pozvani ste u igru!")
                .setPositiveButton("Prihvati", (dialog, which) -> {

                    referencaIgre.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot Snapshot) {
                            if(Snapshot.exists()){
                                final Boolean daLiJePrijateljska = Snapshot.child("prijateljska").getValue(Boolean.class);
                                referencaNaIgraca.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            Integer trenutnoTokena = snapshot.child("tokeni").getValue(Integer.class);
                                            if(daLiJePrijateljska != null){
                                                referencaIgre.child("stanjeIgre").setValue("aktivna")
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Intent intent = new Intent(trenutniActivity, DvaIgraca.class);
                                                                intent.putExtra("POZVAN", true);
                                                                intent.putExtra("IGRA_ID", idIgre);
                                                                trenutniActivity.startActivity(intent);
                                                            }
                                                        });
                                            }
                                            else {
                                                if(trenutnoTokena > 0){
                                                    referencaIgre.child("stanjeIgre").setValue("aktivna")
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Intent intent = new Intent(trenutniActivity, DvaIgraca.class);
                                                                    intent.putExtra("POZVAN", true);
                                                                    intent.putExtra("IGRA_ID", idIgre);
                                                                    trenutniActivity.startActivity(intent);
                                                                }
                                                            });
                                                } else {
                                                    referencaIgre.child("stanjeIgre").setValue("odbijena");
                                                    Toast.makeText(trenutniActivity, "nemate dovoljno tokena da prihvatite partiju", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            } else {
                                Toast.makeText(trenutniActivity, "zahtev za igru vise ne postoji", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                })
                .setNegativeButton("Odbij", (dialog, which) -> {
                    referencaIgre.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                referencaIgre.child("stanjeIgre").setValue("odbijena");
                            } else {
                                Toast.makeText(trenutniActivity, "zahtev za igru vise ne postoji", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                });
        dialog = builder.create();
        dialog.show();
    }

    public void prikaziDijalogZaZatvaranje(final Activity trenutniActivity){
        if(trenutniActivity instanceof RegistrovanKorisnik || trenutniActivity instanceof MainActivity){
            AlertDialog.Builder builder = new AlertDialog.Builder(trenutniActivity);
            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("zatvaranje aplikacije")
                    .setMessage("da li ste sigurni da želite da zatvorite aplikaciju?")
                    .setPositiveButton("da", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            kontrolerKorisnika.obrisiKorisnikaSaListeAktivnih();
                            trenutniActivity.finish();
                        }
                    })
                    .setNegativeButton("ne", null);
            dialog = builder.create();
            dialog.show();
        }
    }

    public void prikaziDijalogZaUcitavanje(final Activity trenutniActivity, String naslov, String poruka){
        AlertDialog.Builder builder = new ProgressDialog.Builder(trenutniActivity);
        builder.setTitle(naslov).setMessage(poruka).setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }

    public void iskljuciDijalog(){
        if(dialog!=null)
            dialog.cancel();
    }

    public void tokeniPriRegistraciji(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("bravo!")
                .setMessage("pošto ste se prvi put registrovali dobili ste za nagradu 5 tokena.")
                .setPositiveButton("hvala", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog = builder.create();
        dialog.show();
    }

    public void tokeniNaDan(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("dobrodošli!")
                .setMessage("dobili ste 5 tokena za danas.")
                .setPositiveButton("hvala", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog = builder.create();
        dialog.show();
    }


}
