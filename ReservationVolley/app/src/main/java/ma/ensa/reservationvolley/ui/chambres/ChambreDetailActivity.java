package ma.ensa.reservationvolley.ui.chambres;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.android.volley.toolbox.StringRequest;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import ma.ensa.reservationvolley.R;
import ma.ensa.reservationvolley.api.VolleySingleton;
import ma.ensa.reservationvolley.models.Chambre;
import ma.ensa.reservationvolley.models.DispoChambre;
import ma.ensa.reservationvolley.models.TypeChambre;

public class ChambreDetailActivity extends AppCompatActivity {

    private Spinner spinnerTypeChambre, spinnerDispoChambre;
    private EditText editPrix;
    private Button btnUpdate, btnDelete;
    private Chambre chambre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chambre_detail);

        // Initialisation des vues
        spinnerTypeChambre = findViewById(R.id.spinnerTypeChambre);
        editPrix = findViewById(R.id.editPrix);
        spinnerDispoChambre = findViewById(R.id.spinnerDispoChambre);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        // Configurer les Spinners
        setupSpinners();

        // Récupérer l'ID de la chambre depuis l'intent
        Long chambreId = getIntent().getLongExtra("CHAMBRE_ID", -1);
        if (chambreId == -1) {
            Toast.makeText(this, "Chambre introuvable", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            loadChambreDetails(chambreId);
        }

        // Bouton de mise à jour
        btnUpdate.setOnClickListener(v -> updateChambre());

        // Bouton de suppression
        btnDelete.setOnClickListener(v -> deleteChambre());
    }

    private void setupSpinners() {
        // Adapter pour TypeChambre
        ArrayAdapter<TypeChambre> typeChambreAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, TypeChambre.values());
        typeChambreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeChambre.setAdapter(typeChambreAdapter);

        // Adapter pour DispoChambre
        ArrayAdapter<DispoChambre> dispoChambreAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, DispoChambre.values());
        dispoChambreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDispoChambre.setAdapter(dispoChambreAdapter);
    }

    private void loadChambreDetails(Long chambreId) {
        String url = "http://192.168.1.160:8082/api/chambres/" + chambreId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Désérialiser la réponse JSON en un objet Chambre
                            Gson gson = new Gson();
                            chambre = gson.fromJson(response.toString(), Chambre.class);

                            // Mettre à jour les vues avec les détails de la chambre
                            spinnerTypeChambre.setSelection(Arrays.asList(TypeChambre.values()).indexOf(chambre.getTypeChambre()));
                            editPrix.setText(String.valueOf(chambre.getPrix()));
                            spinnerDispoChambre.setSelection(Arrays.asList(DispoChambre.values()).indexOf(chambre.getDispoChambre()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ChambreDetailActivity.this, "Erreur lors de la lecture de la réponse JSON", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ChambreDetailActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void updateChambre() {
        String prixStr = editPrix.getText().toString();

        if (prixStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            chambre.setTypeChambre((TypeChambre) spinnerTypeChambre.getSelectedItem());
            chambre.setPrix(Double.parseDouble(prixStr));
            chambre.setDispoChambre((DispoChambre) spinnerDispoChambre.getSelectedItem());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Prix invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir l'objet Chambre en JSON
        Gson gson = new Gson();
        String jsonBody = gson.toJson(chambre);

        try {
            JSONObject jsonObject = new JSONObject(jsonBody);
            String url = "http://192.168.1.160:8082/api/chambres/" + chambre.getId();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.PUT, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(ChambreDetailActivity.this, "Room updated", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ChambreDetailActivity.this, "Erreur de mise à jour: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la création du JSON", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteChambre() {
        new AlertDialog.Builder(this)
                .setTitle("Delete room")
                .setMessage("Are you sure you want to delete this room?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    String url = "http://192.168.1.160:8082/api/chambres/" + chambre.getId();

                    // Temps de début
                    long startTime = System.currentTimeMillis();

                    StringRequest stringRequest = new StringRequest(
                            Request.Method.DELETE, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Temps de fin
                                    long endTime = System.currentTimeMillis();
                                    long durationMs = endTime - startTime; // Temps écoulé en millisecondes

                                    // Mesurer la taille des données reçues
                                    int sizeInBytes = response.getBytes().length; // Taille en octets
                                    double sizeInKB = sizeInBytes / 1024.0; // Convertir en KB

                                    // Afficher les résultats dans les logs
                                    System.out.println("Taille des données reçues (DELETE) : " + sizeInKB + " KB");
                                    System.out.println("Temps de réponse (DELETE) : " + durationMs + " ms");

                                    // Si la réponse est vide, afficher un message de succès
                                    Toast.makeText(ChambreDetailActivity.this, "Room deleted", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // Temps de fin en cas d'échec
                                    long endTime = System.currentTimeMillis();
                                    long durationMs = endTime - startTime; // Temps écoulé en millisecondes

                                    // Afficher les résultats dans les logs
                                    System.out.println("Temps de réponse (DELETE - échec) : " + durationMs + " ms");

                                    if (error.networkResponse != null) {
                                        int statusCode = error.networkResponse.statusCode;
                                        if (statusCode == 204) {
                                            // Réponse vide (No Content)
                                            Toast.makeText(ChambreDetailActivity.this, "Room deleted", Toast.LENGTH_SHORT).show();
                                            setResult(RESULT_OK);
                                            finish();
                                        } else if (statusCode == 400) {
                                            // Erreur de contrainte de clé étrangère
                                            Toast.makeText(ChambreDetailActivity.this, "The room is reserved and cannot be deleted.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Autre erreur
                                            Toast.makeText(ChambreDetailActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Erreur réseau
                                        Toast.makeText(ChambreDetailActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                    );

                    VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
                })
                .setNegativeButton("Non", null)
                .show();
    }

}