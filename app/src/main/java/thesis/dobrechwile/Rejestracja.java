package thesis.dobrechwile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import util.ChwileApi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Rejestracja extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");
    private Button registerButton;
    private EditText emailText;
    private EditText passwordText;
    private ProgressBar progressBar;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rejestracja);

        firebaseAuth = FirebaseAuth.getInstance();
        loginButton = findViewById(R.id.login_button_v2);

        registerButton = findViewById(R.id.register_button);
        progressBar = findViewById(R.id.registration_progress);
        emailText = findViewById(R.id.email_r);
        passwordText = findViewById(R.id.password_r);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Rejestracja.this, Logowanie.class));
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    //logged in

                } else {
                    //not logged in
                }
            }
            };

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(emailText.getText().toString())
                && !TextUtils.isEmpty(passwordText.getText().toString())) {

                    String email = emailText.getText().toString().trim();
                    String password = passwordText.getText().toString().trim();
                    createAccount(email, password);
                }else{
                    Toast.makeText(Rejestracja.this, "Uzupełnij wszystkie pola",
                            Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    private void createAccount(String email, String password){
        if(!TextUtils.isEmpty(email)
        && !TextUtils.isEmpty(password)){

            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        currentUser = firebaseAuth.getCurrentUser();
                        assert currentUser != null;
                        final String currentUserId = currentUser.getUid();

                        Map<String, String> userObj = new HashMap<>();
                        userObj.put("userId", currentUserId);

                        collectionReference.add(userObj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.getResult().exists()){
                                            progressBar.setVisibility(View.INVISIBLE);

                                            ChwileApi chwileApi = ChwileApi.getInstance();
                                            chwileApi.setUserId(currentUserId);

                                            Intent intent = new Intent (Rejestracja.this, ListaChwil.class);
                                            intent.putExtra("userId", currentUserId);
                                            startActivity(intent);
                                        }else{
                                            progressBar.setVisibility(View.INVISIBLE);

                                        }
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });

                    }
                    else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(Rejestracja.this,  "Hasło musi zawierać conajmniej 6 znaków", Toast.LENGTH_SHORT).show();


                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


        }
        else{

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}