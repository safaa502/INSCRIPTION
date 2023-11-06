package ma.ensa.volley;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListeEtuActivity extends AppCompatActivity {

    private ListView etudiantListView;
    private RequestQueue requestQueue;
    private String studentsUrl = "http://10.0.2.2:8087/api/v1/student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_etu);

        etudiantListView = findViewById(R.id.etudiantListView);
        // Obtenez l'ID de la filière à partir de l'intent
        String filiereId = getIntent().getStringExtra("filiere_id");

        // Construisez l'URL en utilisant l'ID de la filière pour récupérer les étudiants de cette filière
        String studentsByFiliereUrl = "http://10.0.2.2:8087/api/v1/filieres/" + filiereId + "/students";

        // Call a method to fetch and display the list of students in this filière
        fetchStudentsByFiliere(studentsByFiliereUrl);
    }

    private void fetchStudentsByFiliere(String url) {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
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
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ListeEtuActivity.this, android.R.layout.simple_list_item_1, studentsList);
                etudiantListView.setAdapter(adapter);
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
