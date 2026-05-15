package com.daiyan.accountify.suppliers;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daiyan.accountify.R;
import ultimate_files.database.database_connectivity;
import ultimate_files.money_formate;
import ultimate_files.RecycleViewHelper; // Import Universal Helper

public class suppliers extends AppCompatActivity {

    RecyclerView recyclerView;
    Button addButton;
    ImageView back;
    Cursor cursor;
    TextView text;
    money_formate money_formate;
    database_connectivity db_connect_obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suppliers);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById();
        initialization();

        if(db_connect_obj.is_table_exists(db_connect_obj.getTable_name_parties())){
            set_item();
        }

        back.setOnClickListener(v ->{finish();});
    }

    public void findViewById() {
        money_formate = new money_formate();
        back = findViewById(R.id.back);
        recyclerView = findViewById(R.id.supplier_list_recycle_view);
        addButton = findViewById(R.id.add_supplier);
    }

    public void initialization(){
        db_connect_obj = new database_connectivity(this);
        if(db_connect_obj.is_table_exists(db_connect_obj.getTable_name_parties())){
            // "purchase" filter ensures we only get Suppliers
            cursor = db_connect_obj.searchInColumn(db_connect_obj.getTable_name_parties(), "trade_type", "purchase");
        }
    }

    public void addSupplier(View view){
        Intent intent = new Intent(this, add_supplier.class);
        startActivity(intent);
    }

    public void set_item() {
        // Safety check for cursor
        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        // Using Universal RecycleViewHelper
        RecycleViewHelper adapter = new RecycleViewHelper.Builder(this)
                .setData(R.layout.customer_list, cursor) // Using customer_list layout as requested

                // --- MAPPINGS ---
                .map(RecycleViewHelper.customerList().getName(), "person_name")

                // Show Phone Number at the Due Date location
                .map(RecycleViewHelper.customerList().getDueDate(), "phone_number")

                // Pass trade_type to identify if it is "purchase" (Supplier)
                .map(RecycleViewHelper.customerList().getPersonType(), "trade_type")

                // Pending Amount mapping
                .map(RecycleViewHelper.customerList().getAmount(), "pending")
                .map(RecycleViewHelper.customerList().getStatusLabel(), "pending")

                // --- LOGIC ---
                // 'true' means show the identity tag (e.g., "(Supplier)")
                .setBindListener(RecycleViewHelper.Logic.Customer(false))

                .build();

        // RecyclerView Setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
}