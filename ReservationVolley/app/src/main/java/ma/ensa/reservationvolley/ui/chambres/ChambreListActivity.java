package ma.ensa.reservationvolley.ui.chambres;
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
import ma.ensa.reservationvolley.adapters.ChambreAdapter;
import ma.ensa.reservationvolley.api.VolleySingleton;
import ma.ensa.reservationvolley.models.Chambre;


public class ChambreListActivity extends AppCompatActivity implements ChambreAdapter.OnChambreListener {

    private RecyclerView recyclerView;
    private ChambreAdapter adapter;
    private Button btnAddChambre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chambre_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAddChambre = findViewById(R.id.btnAddChambre);
        btnAddChambre.setOnClickListener(v -> {
            Intent intent = new Intent(ChambreListActivity.this, AddChambreActivity.class);
            startActivityForResult(intent, 1);
        });

        loadChambres();
    }

    private void loadChambres() {
        String url = "http://192.168.1.160:8082/api/chambres"; // URL de l'endpoint

        // Créer une requête GET avec Volley
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Désérialiser la réponse JSON en une liste de chambres
                            Gson gson = new Gson();
                            List<Chambre> chambres = gson.fromJson(response.toString(), new TypeToken<List<Chambre>>() {}.getType());

                            // Mesurer la taille des données reçues
                            String jsonString = response.toString();
                            int sizeInBytes = jsonString.getBytes().length; // Taille en octets
                            double sizeInKB = sizeInBytes / 1024.0; // Convertir en KB

                            // Afficher la taille des données dans les logs
                            System.out.println("Taille des données reçues : " + sizeInKB + " KB");

                            // Mettre à jour l'adaptateur
                            adapter = new ChambreAdapter(chambres, ChambreListActivity.this, ChambreListActivity.this);
                            recyclerView.setAdapter(adapter);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ChambreListActivity.this, "Erreur lors de la désérialisation", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Gérer l'erreur
                        Toast.makeText(ChambreListActivity.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Ajouter la requête à la file d'attente
        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK || requestCode == 2 && resultCode == RESULT_OK) {
            loadChambres(); // Recharger les chambres après une modification ou un ajout
        }
    }

    @Override
    public void onChambreClick(int position) {
        Chambre chambre = adapter.getChambreAt(position); // Utiliser la méthode getChambreAt
        Intent intent = new Intent(ChambreListActivity.this, ChambreDetailActivity.class);
        intent.putExtra("CHAMBRE_ID", chambre.getId());
        startActivityForResult(intent, 2);
    }
}