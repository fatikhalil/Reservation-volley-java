package ma.ensa.reservationvolley.ui.reservations;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.List;

import ma.ensa.reservationvolley.R;
import ma.ensa.reservationvolley.adapters.ReservationAdapter;
import ma.ensa.reservationvolley.api.VolleySingleton;
import ma.ensa.reservationvolley.models.ReservationInput;


public class ReservationListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private Button buttonAddReservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        recyclerView = findViewById(R.id.reservation_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        buttonAddReservation = findViewById(R.id.buttonAddReservation);
        buttonAddReservation.setOnClickListener(v -> {
            Intent intent = new Intent(ReservationListActivity.this, AddReservationActivity.class);
            startActivity(intent);
        });

        loadReservations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReservations(); // Recharger les réservations à chaque retour à l'activité
    }

    private void loadReservations() {
        String url = "http://192.168.1.160:8082/api/reservations"; // URL de l'endpoint

        // Temps de début
        long startTime = System.currentTimeMillis();

        // Créer une requête GET avec Volley
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Temps de fin
                        long endTime = System.currentTimeMillis();
                        long durationMs = endTime - startTime; // Temps écoulé en millisecondes

                        // Afficher le temps de réponse dans les logs
                        System.out.println("Temps de réponse pour loadReservations : " + durationMs + " ms");

                        try {
                            // Désérialiser la réponse JSON en une liste de réservations
                            Gson gson = new Gson();
                            List<ReservationInput> reservations = gson.fromJson(response.toString(), new TypeToken<List<ReservationInput>>() {}.getType());

                            // Mettre à jour l'adaptateur
                            adapter = new ReservationAdapter(reservations, ReservationListActivity.this);
                            recyclerView.setAdapter(adapter);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ReservationListActivity.this, "Erreur lors de la lecture des réservations", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Temps de fin en cas d'échec
                        long endTime = System.currentTimeMillis();
                        long durationMs = endTime - startTime; // Temps écoulé en millisecondes

                        // Afficher le temps de réponse dans les logs
                        System.out.println("Temps de réponse pour loadReservations (échec) : " + durationMs + " ms");

                        // Gérer l'erreur
                        Toast.makeText(ReservationListActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Ajouter la requête à la file d'attente
        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
}