package ma.ensa.volley;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class StudentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText username;
    private EditText password;
    private EditText firstname;
    private EditText lastname;
    private EditText phone;
    private Button addStudentBtn;

    private List<Student> students;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        recyclerView = findViewById(R.id.recyclerView);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        firstname = findViewById(R.id.firstname);
        lastname = findViewById(R.id.lastname);
        phone = findViewById(R.id.phone);
        addStudentBtn = findViewById(R.id.addStudent);

        requestQueue = Volley.newRequestQueue(this);

        loadStudents(); // Charge la liste des étudiants depuis l'API

        addStudentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStudent();
            }
        });
    }

    private void loadStudents() {
        String studentUrl = "http://192.168.1.103:8080/api/student";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, studentUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Type type = new TypeToken<List<Student>>() {}.getType();
                            students = new Gson().fromJson(response.getJSONArray("students").toString(), type);
                            // Mettez en place une adaptation de votre choix pour afficher les étudiants dans le RecyclerView.
                            // Vous pouvez créer un adaptateur personnalisé si vous le souhaitez.
                            // recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(StudentActivity.this));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(StudentActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void addStudent() {
        String studentUrl = "http://192.168.1.103:8080/api/student";

        String newUsername = username.getText().toString();
        String newPassword = password.getText().toString();
        String newFirstname = firstname.getText().toString();
        String newLastname = lastname.getText().toString();
        String newPhone = phone.getText().toString();

        // Créez un nouvel étudiant en utilisant les valeurs saisies
        Student newStudent = new Student();
        newStudent.setUsername(newUsername);
        newStudent.setPassword(newPassword);
        newStudent.setName(newFirstname); // Utilisation de 'name' pour le prénom
        newStudent.setEmail(newLastname); // Utilisation de 'email' pour le nom
        newStudent.setPhone(Integer.parseInt(newPhone)); // Conversion en entier pour le téléphone

        // Convertissez l'objet nouvel étudiant en JSON
        String studentJSON = new Gson().toJson(newStudent);

        try {
            JSONObject jsonObject = new JSONObject(studentJSON);
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, studentUrl, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(StudentActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
                            loadStudents(); // Rechargez la liste des étudiants après l'ajout
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(StudentActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
