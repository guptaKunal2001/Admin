package com.example.adminapp;

import static com.example.adminapp.CategoryActivity.catList;
import static com.example.adminapp.CategoryActivity.selected_cat_index;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetsActivity extends AppCompatActivity {
    private RecyclerView setsView;
    private Button addSetB;
    private SetAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;
    public static List<String> setIDs = new ArrayList<>();
    public static int selected_set_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        androidx.appcompat.widget.Toolbar toolbar=findViewById(R.id.sa_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sets");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setsView = findViewById(R.id.sets_recycler);
        addSetB = findViewById(R.id.addSetb);

        loadingDialog=new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        addSetB.setText("ADD NEW SET");

        addSetB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewSet();
            }
        });
        firestore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setsView.setLayoutManager(layoutManager);

        loadSets();
    }
    private void loadSets()
    {
        setIDs.clear();
        loadingDialog.show();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
               long noOfSets = (long)documentSnapshot.get("SETS");
               for(int i=1;i<=noOfSets;i++)
               {
                   setIDs.add(documentSnapshot.getString("SET" + String.valueOf(i) + "_ID"));
               }
               catList.get(selected_cat_index).setSetCounter(documentSnapshot.getString("COUNTER"));
               catList.get(selected_cat_index).setNoOfSets(String.valueOf(noOfSets));

                adapter = new SetAdapter(setIDs);
                setsView.setAdapter(adapter);
                loadingDialog.dismiss();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
    }
    private void addNewSet()
    {
        loadingDialog.show();

        String curr_cat_id = catList.get(selected_cat_index).getId();
        String curr_counter = catList.get(selected_cat_index).getSetCounter();

        Map<String,Object> qData = new ArrayMap<>();
        qData.put("COUNT","0");

        firestore.collection("QUIZ").document(curr_cat_id)
                .collection(curr_counter).document("QUESTIONS_LIST")
                .set(qData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Map<String,Object> catDoc = new ArrayMap<>();
                        catDoc.put("COUNTER",String.valueOf(Integer.valueOf(curr_counter) + 1));
                        catDoc.put("SET" + String.valueOf(setIDs.size() + 1) + "_ID",curr_counter);
                        catDoc.put("SETS",setIDs.size()+1);

                        firestore.collection("QUIZ").document(curr_cat_id)
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(SetsActivity.this,"Set Added Successfully",Toast.LENGTH_SHORT).show();
                                        setIDs.add(curr_counter);
                                        catList.get(selected_cat_index).setNoOfSets(String.valueOf(setIDs.size()));
                                        catList.get(selected_cat_index).setSetCounter(String.valueOf(Integer.valueOf(curr_counter) + 1));

                                        adapter.notifyItemInserted(setIDs.size());
                                        loadingDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SetsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}