package ma.ensa.volley;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class FiliereActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText code, libelle;
    private Button bnAdd;
    private ListView filiereListView;
    private RequestQueue requestQueue;
    private String filieresUrl = "http://10.0.2.2:8087/api/v1/filieres";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filiere);

        code = findViewById(R.id.code);
        libelle = findViewById(R.id.libelle);
        bnAdd = findViewById(R.id.bnAdd);
        filiereListView = findViewById(R.id.filiereListView);
        Button btnBackToMain = findViewById(R.id.btnBackToMain); // Ajout du bouton de retour

        bnAdd.setOnClickListener(this);
        bnAdd.setOnClickListener(this);

        // Call a method to fetch and display the list of filières
        fetchFilieresList();
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FiliereActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("code", code.getText().toString());
            jsonBody.put("libelle", libelle.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                filieresUrl, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Après avoir ajouté la filière, mettez à jour la liste en temps réel
                fetchFilieresList();
                Log.d("resultat", response + "");
                // Réinitialisez les champs d'entrée
                code.setText("");
                libelle.setText("");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Erreur", error.toString());
            }
        });
        requestQueue.add(request);
    }

    private void fetchFilieresList() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, filieresUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Handle the JSON array response here and populate it in your ListView
                final List<String> filieresList = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        final JSONObject filiere = response.getJSONObject(i);
                        final String libelle = filiere.getString("libelle");
                        filieresList.add(libelle);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Create an ArrayAdapter and set it to the ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(FiliereActivity.this, android.R.layout.simple_list_item_1, filieresList);
                filiereListView.setAdapter(adapter);

                // Configure the click listener for ListView items
                filiereListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FiliereActivity.this);
                        builder.setTitle("Confirmer la suppression");
                        builder.setMessage("Voulez-vous vraiment supprimer cette filière?");
                        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Supprimer la filière ici en utilisant l'ID ou autre identifiant
                                try {
                                    JSONObject selectedFiliere = response.getJSONObject(position);
                                    String filiereId = selectedFiliere.getString("id");
                                    deleteFiliere(filiereId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Erreur", error.toString());
            }
        });

        requestQueue.add(request);
    }

    private void deleteFiliere(String filiereId) {
        // Construisez l'URL pour supprimer la filière en utilisant l'identifiant
        String deleteUrl = "http://10.0.2.2:8087/api/v1/filieres/" + filiereId;

        // Créez une file d'attente de requêtes Volley
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Envoyez une requête DELETE pour supprimer la filière
        StringRequest request = new StringRequest(Request.Method.DELETE, deleteUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Gérez la réponse après la suppression réussie
                Log.d("Suppression réussie", response);
                // Mettez à jour la liste en temps réel
                fetchFilieresList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Gérez les erreurs lors de la suppression
                Log.d("Erreur lors de la suppression", error.toString());
            }
        });

        // Ajoutez la requête à la file d'attente
        requestQueue.add(request);
    }
    private void showStudentsByFiliere(String filiereId) {
        // Construisez l'URL en utilisant l'ID de la filière pour récupérer les étudiants de cette filière
        String studentsByFiliereUrl = "http://10.0.2.2:8087/api/v1/filieres/" + filiereId + "/students";

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, studentsByFiliereUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Handle the JSON array response here and populate it in your ListView
                final List<String> studentsList = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        final JSONObject student = response.getJSONObject(i);
                        final String studentName = student.getString("name");
                        studentsList.add(studentName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Create an ArrayAdapter and set it to the ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(FiliereActivity.this, android.R.layout.simple_list_item_1, studentsList);
                filiereListView.setAdapter(adapter);

                // Configure the click listener for ListView items
                filiereListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FiliereActivity.this);
                        builder.setTitle("Confirmer la suppression");
                        builder.setMessage("Voulez-vous vraiment supprimer cette filière?");
                        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Supprimer la filière ici en utilisant l'ID ou autre identifiant
                                try {
                                    JSONObject selectedFiliere = response.getJSONObject(position);
                                    String filiereId = selectedFiliere.getString("id");
                                    deleteFiliere(filiereId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Erreur", error.toString());
            }
        });

        requestQueue.add(request);
    }


}
