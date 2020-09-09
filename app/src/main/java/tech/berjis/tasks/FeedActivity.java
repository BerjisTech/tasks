package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseFirestoreSettings firestoreSettings;
    private FirestoreRecyclerAdapter<Categories, FeedActivity.CategoriesViewHolder> adapter;

    RecyclerView categoryRecycler;
    ImageView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firestoreSettings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build();
        firestore.setFirestoreSettings(firestoreSettings);

        categoryRecycler = findViewById(R.id.categoryRecycler);
        search = findViewById(R.id.search);

        staticOnClick();
        loadWorkers();
    }

    private void staticOnClick() {
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, WhichTaskActivity.class));
            }
        });
    }

    private void loadWorkers() {
        Intent c_intent = getIntent();
        Bundle c_bundle = c_intent.getExtras();

        assert c_bundle != null;
        String category = c_bundle.getString("category");
        String location = c_bundle.getString("location");
        String minimum = c_bundle.getString("minimum");
        String maximum = c_bundle.getString("maximum");


    }

    @Override
    protected void onStart() {
        super.onStart();

        categoryRecycler.setLayoutManager(new LinearLayoutManager(FeedActivity.this, RecyclerView.HORIZONTAL, false));
        FirestoreRecyclerOptions<Categories> options = new FirestoreRecyclerOptions.Builder<Categories>()
                .setQuery(firestore.collection("Categories"), Categories.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Categories, FeedActivity.CategoriesViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull FeedActivity.CategoriesViewHolder CategoriesViewHolder, int i, @NonNull Categories Categories) {
                CategoriesViewHolder.setCategories(Categories.getName(), Categories.getImage());
            }

            @NonNull
            @Override
            public FeedActivity.CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories, parent, false);
                return new FeedActivity.CategoriesViewHolder(view);
            }
        };
        categoryRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }


    static class CategoriesViewHolder extends RecyclerView.ViewHolder {
        private View view;

        CategoriesViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setCategories(final String c_name, String c_image) {
            final TextView name = view.findViewById(R.id.name);
            CircleImageView image = view.findViewById(R.id.image);
            name.setText(c_name);
            Picasso.get().load(c_image).into(image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent c_intent = new Intent(view.getContext(), FeedActivity.class);
                    Bundle c_bundle = new Bundle();
                    c_bundle.putString("category", c_name);
                    c_bundle.putString("location", "");
                    c_bundle.putString("minimum", "");
                    c_bundle.putString("maximum", "");
                    c_intent.putExtras(c_bundle);
                    view.getContext().startActivity(c_intent);
                }
            });
        }
    }
}
