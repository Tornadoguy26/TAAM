package com.example.taam;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taam.structures.Item;

import java.util.ArrayList;

public class ViewItemActivity extends AppCompatActivity {

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_viewitem);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ArrayList<Item> checkedItems = (ArrayList<Item>) getIntent().getSerializableExtra("checkedItems", ArrayList.class);
        Log.d("[TAAM]", "Got: " + checkedItems);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ViewCardsAdapter viewCardsAdapter = new ViewCardsAdapter(checkedItems);
        recyclerView.setAdapter(viewCardsAdapter);

        Button backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(view -> {
            finish();
        });
    }

}
