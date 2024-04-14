package com.example.noteapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteapp.model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private DatabaseReference myRef;

    private FirebaseFirestore firestore;

    private RecyclerView rvNotes;
    private FloatingActionButton btnAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Note App");


        rvNotes = findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        btnAdd = findViewById(R.id.btn_add);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("posts");
        firestore = FirebaseFirestore.getInstance();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNote();
            }
        });
    }


    public void addNote() {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mview = inflater.inflate(R.layout.add_note, null);
        mDialog.setView(mview);

        AlertDialog dialog = mDialog.create();
        dialog.setCancelable(true);
        dialog.show();

        Button save = mview.findViewById(R.id.btn_save);
        EditText editTitle = mview.findViewById(R.id.edt_title);
        EditText editContent = mview.findViewById(R.id.edt_content);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = myRef.push().getKey();
                String title = editTitle.getText().toString();
                String content = editContent.getText().toString();


                myRef.child(id).setValue(new Post(id,title,content,getRandomColor())).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Add note sucessful",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Fail add note",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(myRef, Post.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Post, PostHolder>(options) {
            @Override
            public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_items, parent, false);

                return new PostHolder(view);
            }

            @Override
            protected void onBindViewHolder(PostHolder holder, int position, Post model) {
                holder.tvTitle.setText(model.getTitle());
                holder.tvContent.setText(model.getContent());
                holder.layoutNote.setBackgroundColor(Color.parseColor(model.getColor()));

                ImageView ivaction = holder.itemView.findViewById(R.id.iv_action);

                ivaction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(view.getContext(),view);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {
                                return false;
                            }
                        });

                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {

                                return false;
                            }
                        });

                        popupMenu.show();
                    }
                });
            }
        };

        rvNotes.setAdapter(adapter);
        adapter.startListening();
    }

    public static class PostHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle;
        public TextView tvContent;
        private LinearLayout layoutNote;
        public PostHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_title);
            tvContent = view.findViewById(R.id.tv_content);
            layoutNote = view.findViewById(R.id.layout_note);
        }


    }

    private String getRandomColor() {
        ArrayList<String> colors = new ArrayList<>();
        colors.add("#f5f5dc");
        colors.add("#e4c7c6");
        colors.add("#ab4f3d");
        colors.add("#49437f");
        colors.add("#355886");

        Random random = new Random();
        return colors.get(random.nextInt(colors.size()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.mi_logout);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mAuth.signOut();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
//            case R.id.mi_logout:
        }
        return super.onOptionsItemSelected(item);
    }
}