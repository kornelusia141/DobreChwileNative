package thesis.dobrechwile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import model.Chwile;
import ui.RecyclerAdapter;
import util.ChwileApi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.StructuredQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListaChwil extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private List<Chwile> chwileList;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;

    private CollectionReference collectionReference = db.collection("Chwile");
    private TextView noPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_chwil);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        noPosts = findViewById(R.id.noPostTextView);
        chwileList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.add:
                if(user != null && firebaseAuth !=null){
                    startActivity(new Intent(ListaChwil.this, Publikacja.class));
                    //finish();
                }
                break;
            case R.id.wyloguj:
                if(user != null && firebaseAuth != null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(ListaChwil.this, MainActivity.class));
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        collectionReference.whereEqualTo("userId", ChwileApi.getInstance().getUserId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    chwileList.clear();
                    for(QueryDocumentSnapshot chwile : queryDocumentSnapshots){
                        Chwile chwile1 = chwile.toObject(Chwile.class);
                        chwile1.setUid(chwile.getId().toString());
                        chwileList.add(chwile1);
                    }
                    Collections.sort(chwileList, new Comparator<Chwile>() {
                        @Override
                        public int compare(Chwile chwile, Chwile t1) {
                            return chwile.getTimeAdded().compareToIgnoreCase(t1.getTimeAdded());
                        }
                    });
                    Collections.reverse(chwileList);
                    recyclerAdapter = new RecyclerAdapter(ListaChwil.this, chwileList);
                    recyclerView.setAdapter(recyclerAdapter);
                    recyclerAdapter.notifyDataSetChanged();

                }else{
                    noPosts.setVisibility(View.VISIBLE);

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}