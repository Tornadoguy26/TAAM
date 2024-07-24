package com.example.taam;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.taam.structures.Item;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase db;

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

        db = FirebaseDatabase.getInstance("https://taam-1c732-default-rtdb.firebaseio.com/");
        DatabaseReference dbRef = db.getReference("Items");

        Item item1 = new Item(10101, "vase", "Qing", "Prehistoric", "very lovely");
        dbRef.child("" + item1.getLotNumber()).setValue(item1);

        Item item2 = new Item(20202, "bottle", "Qing", "Prehistoric", "lots of water");
        dbRef.child("" + item2.getLotNumber()).setValue(item2);

        Item item3 = new Item(30303, "laptop", "Qing", "Jurrasic", "refurbished like new");
        dbRef.child("" + item3.getLotNumber()).setValue(item3);

        TextView tv = findViewById(R.id.mainLabel);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder total = new StringBuilder();

                Log.d("[TAAM]", "\nNEW DATA: " + snapshot.getValue());
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Item item = snap.getValue(Item.class);
                    assert item != null;
                    Log.d("[TAAM]", "Data: " + item.getName());

                    total.append(item.getName()).append("\n");
                }

                tv.setText(total.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });



    }
}