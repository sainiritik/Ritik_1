package com.example.ritik_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Contacts extends AppCompatActivity {
    FloatingActionButton fab;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    List<ContactDetail> dataList;
    ContactItem adapter;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(Contacts.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(Contacts.this);
        builder.setCancelable(false);
        builder.setView(R.layout.activity_progressbar_wait);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();

        adapter = new ContactItem(Contacts.this, dataList);
        recyclerView.setAdapter(adapter);

        // Get the currently logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid).child("contacts");
            dialog.show();
            eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    dataList.clear();
                    for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                        ContactDetail dataClass = itemSnapshot.getValue(ContactDetail.class);

                        assert dataClass != null;
                        dataClass.setKey(itemSnapshot.getKey());

                        dataList.add(dataClass);
                    }
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dialog.dismiss();
                }
            });
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Contacts.this, AddContact.class);
                startActivity(intent);
            }
        });
    }
    public void searchList(String text){
        ArrayList<ContactDetail> searchList = new ArrayList<>();
        for (ContactDetail dataClass: dataList){
            if (dataClass.getDataName().toLowerCase().contains(text.toLowerCase())){
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
    }
}
