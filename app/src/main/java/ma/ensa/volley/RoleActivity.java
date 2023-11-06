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

public class RoleActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText name;
    private Button bnAdd;
    private ListView roleListView;
    private RequestQueue requestQueue;
    private String rolesUrl = "http://10.0.2.2:8087/api/v1/roles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        name = findViewById(R.id.name);
        bnAdd = findViewById(R.id.bnAdd);
        roleListView = findViewById(R.id.roleListView);
        Button btnBackToMain = findViewById(R.id.btnBackToMain); // Ajout du bouton de retour

        bnAdd.setOnClickListener(this);
        bnAdd.setOnClickListener(this);

        // Call a method to fetch and display the list of roles
        fetchRolesList();
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RoleActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                rolesUrl, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // After adding the role, update the list in real-time
                fetchRolesList();
                Log.d("resultat", response + "");
                // Reset the input field
                name.setText("");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Erreur", error.toString());
            }
        });
        requestQueue.add(request);
    }

    private void fetchRolesList() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, rolesUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Handle the JSON array response here and populate it in your ListView
                final List<String> rolesList = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        final JSONObject role = response.getJSONObject(i);
                        final String roleName = role.getString("name");
                        rolesList.add(roleName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Create an ArrayAdapter and set it to the ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(RoleActivity.this, android.R.layout.simple_list_item_1, rolesList);
                roleListView.setAdapter(adapter);

                // Configure the click listener for ListView items
                roleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RoleActivity.this);
                        builder.setTitle("Confirm Deletion");
                        builder.setMessage("Do you really want to delete this role?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete the role here using the ID or other identifier
                                try {
                                    JSONObject selectedRole = response.getJSONObject(position);
                                    String roleId = selectedRole.getString("id");
                                    deleteRole(roleId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                Log.d("Error", error.toString());
            }
        });

        requestQueue.add(request);
    }

    private void deleteRole(String roleId) {
        // Build the URL to delete the role using the identifier
        String deleteUrl = "http://10.0.2.2:8087/api/v1/roles/" + roleId;

        // Create a Volley request queue
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Send a DELETE request to delete the role
        StringRequest request = new StringRequest(Request.Method.DELETE, deleteUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Handle the response after successful deletion
                Log.d("Deletion Successful", response);
                // Update the list in real-time
                fetchRolesList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle errors during deletion
                Log.d("Error during deletion", error.toString());
            }
        });

        // Add the request to the queue
        requestQueue.add(request);
    }
}
