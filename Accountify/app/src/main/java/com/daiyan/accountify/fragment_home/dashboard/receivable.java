package com.daiyan.accountify.fragment_home.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
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
import com.daiyan.accountify.new_sale.trade_sale;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import ultimate_files.RecycleViewHelper;
import ultimate_files.database.database_connectivity;

public class receivable extends AppCompatActivity {

    Cursor cursor;
    database_connectivity db_connect_obj;
    ExtendedFloatingActionButton fab;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receivable);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db_connect_obj = new database_connectivity(this);
        fab = findViewById(R.id.Add);
        fab.setText("Add New Sale");
        fab.setOnClickListener(v->addSale(v));
        recyclerView = findViewById(R.id.stock_item_recycle_view);

        // THE FIX 1: Function call karna zaroori hai
        set_item_for_product();
    }
    public void addSale(View v){
        Intent intent = new Intent(this, trade_sale.class);
        startActivity(intent);

    }

    public void set_item_for_product() {

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // Query product table par hai
        cursor = db_connect_obj.searchInMultiColumnsMultiCondition(
                db_connect_obj.getTable_name_parties(),
                new String[][]{
                        {"pending", ">", "0", "AND", "("}, {"pending", ">", "0", "OR", ")"}
                }
        );

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

        // THE FIX 3: Adapter aur Layout Manager set karna zaroori hai
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}