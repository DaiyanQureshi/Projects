package com.daiyan.accountify.customers;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daiyan.accountify.R;
import ultimate_files.RecycleViewHelper;
import ultimate_files.database.database_connectivity;

import ultimate_files.money_formate;

public class customers extends AppCompatActivity {
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
        setContentView(R.layout.activity_customers);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById();
        initialization();
        if(db_connect_obj.is_table_exists(db_connect_obj.getTable_name_parties())){set_item();}
        back.setOnClickListener(v ->{finish();});



    }
    public void findViewById() {
        money_formate = new money_formate();
        back = findViewById(R.id.back);
        recyclerView = findViewById(R.id.stock_item_recycle_view);
        addButton = findViewById(R.id.add_customer);
    }
    public void initialization(){
        db_connect_obj = new database_connectivity(this);
        if(db_connect_obj.is_table_exists(db_connect_obj.getTable_name_parties())){
            cursor = db_connect_obj.searchInColumn(db_connect_obj.getTable_name_parties(),"trade_type","sale");
        }
    }
    public void set_item() {
        RecycleViewHelper adapter = new RecycleViewHelper.Builder(this)
                .setData(R.layout.customer_list, cursor)

                // MAPPING
                .map(RecycleViewHelper.customerList().getName(), "person_name")
                .map(RecycleViewHelper.customerList().getDueDate(), "phone_number")  // Phone at DueDate ID
                .map(RecycleViewHelper.customerList().getPersonType(), "trade_type") // Trade Type check
                .map(RecycleViewHelper.customerList().getAmount(), "pending")
                .map(RecycleViewHelper.customerList().getStatusLabel(), "pending")

                // LOGIC CALL (Sirf ye line change karni hai true/false ke liye)
                .setBindListener(RecycleViewHelper.Logic.Customer(false))

                .build();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    public void addCustomer(View view){
        Intent intent = new Intent(this, add_customer.class);
        startActivity(intent);
    }
}