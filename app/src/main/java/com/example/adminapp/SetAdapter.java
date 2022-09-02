package com.example.adminapp;

import static com.example.adminapp.CategoryActivity.catList;
import static com.example.adminapp.CategoryActivity.selected_cat_index;
import static com.example.adminapp.SetsActivity.selected_set_index;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Map;

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.ViewHolder> {
    private List<String> setIDs;

    public SetAdapter(List<String> setIDs) {
        this.setIDs = setIDs;
    }

    @NonNull
    @Override
    public SetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetAdapter.ViewHolder holder, int position) {
        String setID = setIDs.get(position);
        holder.setData(position,setID,this);
    }

    @Override
    public int getItemCount() {
        return setIDs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView setName;
        private ImageView deleteSetB;
        private Dialog loadingDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            setName = itemView.findViewById(R.id.catName);
            deleteSetB = itemView.findViewById(R.id.catDelB);

            loadingDialog=new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        }
        private void setData(int pos,String setID,SetAdapter adapter)
        {
            setName.setText("SET " + String.valueOf(pos + 1));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_set_index = pos;
                    Intent intent = new Intent(itemView.getContext(),QuestionsActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });
            deleteSetB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Set")
                            .setMessage("Do you want to this Set?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteSet(pos,setID, itemView.getContext(),adapter);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }
        private void deleteSet(int pos, String setID, Context context,SetAdapter adapter)
        {
            loadingDialog.show();


            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                    .collection(setID).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            WriteBatch batch = firestore.batch();
                            for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                            {
                                batch.delete(doc.getReference());
                            }
                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Map<String,Object> catDoc = new ArrayMap<>();
                                    int index = 1;
                                    for(int i=0;i<setIDs.size();i++)
                                    {
                                        if(i!= pos)
                                        {
                                            catDoc.put("SET" + String.valueOf(index) + "_ID",setIDs.get(i));
                                            index++;
                                        }
                                    }
                                    catDoc.put("SETS",index-1);

                                    firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                                            .update(catDoc)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(context,"Set deleted Successfully",Toast.LENGTH_SHORT).show();
                                                    SetsActivity.setIDs.remove(pos);
                                                    catList.get(selected_cat_index).setNoOfSets(String.valueOf(SetsActivity.setIDs.size()));
                                                    adapter.notifyDataSetChanged();
                                                    loadingDialog.dismiss();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                    loadingDialog.dismiss();
                                                }
                                            });
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });
        }
    }
}
