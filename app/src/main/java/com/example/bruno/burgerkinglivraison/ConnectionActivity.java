package com.example.bruno.burgerkinglivraison;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ConnectionActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtUsername, txtPassword;
    private Button btnAnnuler, btnSeConnecter;
    private static ArrayList<User> uneListe = new ArrayList<>();

    private static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        this.txtUsername = (EditText) findViewById(R.id.idUsername);
        this.txtPassword = (EditText) findViewById(R.id.idPassword);
        this.btnAnnuler = (Button) findViewById(R.id.idAnnuler);
        this.btnSeConnecter = (Button) findViewById(R.id.idSeConnecter);

        // rendre écoutable les boutons
        this.btnSeConnecter.setOnClickListener((View.OnClickListener) this);
        this.btnAnnuler.setOnClickListener((View.OnClickListener) this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.idAnnuler:
                this.txtUsername.setText("");
                this.txtPassword.setText("");
                break;
            case R.id.idSeConnecter:
                final String username = this.txtUsername.getText().toString();
                final String password = this.txtPassword.getText().toString();


                try {
                /* Execution de la tache asynchrone */
                    Thread unT = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            ExecutionConnection uneExe = new ExecutionConnection();
                            try {
                                uneListe = uneExe.execute().get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            for (User unUser : uneListe) {
                                //remplir les données
                                BddUtilisateur.remplirDonnees(unUser);
                            }
                            user = BddUtilisateur.verifUser(username, password);

                        }
                    });
                    unT.start();
                    unT.join();
                } catch(Exception e){
                    e.printStackTrace();
                    Log.e("Erreur : ", "" + e);
                }

                if (user == null) {
                    Toast.makeText(this, "Identifiant ou mdp erronés ", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Bienvenue " + user.getPrenom(), Toast.LENGTH_LONG).show();

                    //Appel d'une nouvelle vue !
                    Intent unIntent = new Intent(this, CommandesActivity.class);
                    unIntent.putExtra("restaurant_id", user.getRestaurant_id());
                    this.startActivity(unIntent);
                }

                break;
        }
    }

}

// classe synchrone pour la lecture des users
class ExecutionConnection extends AsyncTask<Void, Void, ArrayList<User>> {
    @Override
    protected ArrayList<User> doInBackground(Void... params) {
        ArrayList<User> liste = new ArrayList<>();
        URL uneURL;
        String resultat = "";

        try {
            uneURL = new URL("http://192.168.1.18/androidBurgerking/connexion.php");
            HttpURLConnection urlConnection = (HttpURLConnection) uneURL.openConnection();
            Log.e("ca marche", "ca marche");

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader unBuffer = new BufferedReader(new InputStreamReader(in));

                StringBuilder unSB = new StringBuilder();
                String ligne;
                while ((ligne = unBuffer.readLine()) != null) {
                    unSB.append(ligne);
                }
                // on obtient une chaine contenant le resultat du fichier de l'URL
                resultat = unSB.toString();

                // traitement JSON du resultat
                try {
                    JSONArray tabJson = new JSONArray(resultat);
                    for (int i = 0; i < tabJson.length(); i++) {
                        JSONObject unObjet = tabJson.getJSONObject(i);
                        int id = unObjet.getInt("id");
                        String username = unObjet.getString("username");
                        String email = unObjet.getString("email");
                        String password = unObjet.getString("password");
                        String role = unObjet.getString("role");
                        String nom = unObjet.getString("nom");
                        String prenom = unObjet.getString("prenom");
                        String telephone = unObjet.getString("telephone");
                        String ville = unObjet.getString("ville");
                        String cp = unObjet.getString("cp");
                        String adresse = unObjet.getString("adresse");
                        String dateInscription = unObjet.getString("dateInscription");
                        int restaurant_id = unObjet.getInt("restaurant_id");
                        User unUser = new User(id, username, email, password, role, nom, prenom, telephone, ville, cp, adresse, dateInscription, restaurant_id);
                        liste.add(unUser);
                    }
                } catch (JSONException e) {
                    Log.e("Erreur :", "Erreur de parse de Json");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Erreur : ", "" + e);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("MalformedURLException :", ""+e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("IOException :", ""+e);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Erreur : ", "" + e);
        }

        return liste;
    }
}
