package ui;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.Chwile;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import thesis.dobrechwile.Publikacja;
import thesis.dobrechwile.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Chwile> chwileList;

    public RecyclerAdapter(Context context, List<Chwile> chwileList) {
        this.context = context;
        this.chwileList = chwileList;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wpis, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        Chwile chwile = chwileList.get(position);
        String imageUrl;
        holder.title.setText(chwile.getTitle());
        holder.description.setText(chwile.getDescription());
        holder.location.setText(chwile.getLocation());
        imageUrl = chwile.getImageUrl();
        Picasso.get().load(imageUrl).placeholder(R.drawable.niebo1).fit().into(holder.imageView);
        holder.date.setText(chwile.getTimeAdded());

    }

    @Override
    public int getItemCount() {
        return chwileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title, description, date, nick, location;
        public ImageView imageView;
        public ImageButton deleteButton;
        public LinearLayout layout;
        String userId;
        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            layout=itemView.findViewById(R.id.tab_layout);
            title = itemView.findViewById(R.id.titleList);
            description = itemView.findViewById(R.id.descriptionList);
            date = itemView.findViewById(R.id.dateList);
            imageView = itemView.findViewById(R.id.imageList);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            location = itemView.findViewById(R.id.locationList);

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent (itemView.getContext(), Publikacja.class);
                    intent.putExtra("wyswietlanie", "tak");
                    intent.putExtra("date", chwileList.get(getAdapterPosition()).getTimeAdded());
                    intent.putExtra("location", chwileList.get(getAdapterPosition()).getLocation());
                    intent.putExtra("title", chwileList.get(getAdapterPosition()).getTitle());
                    intent.putExtra("description",chwileList.get(getAdapterPosition()).getDescription());
                    intent.putExtra("imageUrl",chwileList.get(getAdapterPosition()).getImageUrl());
                    intent.putExtra("documentUid",chwileList.get(getAdapterPosition()).getUid());
                    itemView.getContext().startActivity(intent);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeAt(getAdapterPosition());

                }
            });

        }

        private void removeAt(int adapterPosition) {
            String uid = chwileList.get(adapterPosition).getUid();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference documentReference = firebaseFirestore.collection("Chwile").document(uid);
            documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(itemView.getContext(),  "Wpis został usunięty", Toast.LENGTH_SHORT).show();
                }
            });
            chwileList.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);
            notifyItemRangeChanged(adapterPosition, chwileList.size());
        }
    }
}
