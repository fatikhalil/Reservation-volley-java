package ma.ensa.reservationvolley.ui.chambres;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import ma.ensa.reservationvolley.R;
import ma.ensa.reservationvolley.api.VolleySingleton;
import ma.ensa.reservationvolley.models.Chambre;
import ma.ensa.reservationvolley.models.DispoChambre;
import ma.ensa.reservationvolley.models.TypeChambre;

public class AddChambreActivity extends AppCompatActivity {

    private Spinner spinnerTypeChambre, spinnerDispoChambre;
    private EditText editPrix;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chambre);

        // Initialisation des vues
        spinnerTypeChambre = findViewById(R.id.spinnerTypeChambre);
        editPrix = findViewById(R.id.editPrix);
        spinnerDispoChambre = findViewById(R.id.spinnerDispoChambre);
        btnSave = findViewById(R.id.btnUpdate);

        // Configurer les Spinners
        setupSpinners();

        // Bouton de sauvegarde
        btnSave.setOnClickListener(v -> saveChambre());
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

    private void saveChambre() {
        String prixStr = editPrix.getText().toString();

        // Validation des champs
        if (prixStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Créer une nouvelle chambre
            Chambre chambre = new Chambre();
            chambre.setTypeChambre((TypeChambre) spinnerTypeChambre.getSelectedItem());
            chambre.setPrix(Double.parseDouble(prixStr));
            chambre.setDispoChambre((DispoChambre) spinnerDispoChambre.getSelectedItem());

            // Convertir l'objet Chambre en JSON
            Gson gson = new Gson();
            String jsonBody = gson.toJson(chambre);

            // URL de l'endpoint pour ajouter une chambre
            String url = "http://192.168.1.160:8082/api/chambres";

            // Créer une requête POST avec Volley
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, new JSONObject(jsonBody),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Gérer la réponse JSON
                            Toast.makeText(AddChambreActivity.this, "Room added", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish(); // Fermer l'activité après l'ajout
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Gérer l'erreur
                            Toast.makeText(AddChambreActivity.this, "Erreur d'ajout: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            // Ajouter la requête à la file d'attente
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Prix invalide", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la création de la chambre", Toast.LENGTH_SHORT).show();
        }
    }
}