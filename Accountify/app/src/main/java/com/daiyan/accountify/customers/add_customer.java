package com.daiyan.accountify.customers;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.daiyan.accountify.R;
import ultimate_files.database.database_connectivity;

public class add_customer extends AppCompatActivity {

    // Layout
    androidx.constraintlayout.widget.ConstraintLayout main;

    // ImageViews
    ImageView back;
    ImageView save_new_customer;

    // TextViews
    TextView add_new_customer_page_title;
    TextView store_name;

    // EditTexts
    EditText customer_name;
    EditText mobile_number;
    EditText email_id;
    EditText gstin;
    Cursor cursor;

    // Database
database_connectivity db_connect_obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_customer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize views
        initialization();
        findViewById();

        // Set click listener for the save button
        save_new_customer.setOnClickListener(v -> {save_new_customer_Data();
        });
        back.setOnClickListener(v ->{finish();});

    }

    public void save_new_customer_Data() {
        // Validate that customer name is not empty
        if (customer_name.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Customer name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if(db_connect_obj.is_table_exists(db_connect_obj.getTable_name_parties())){
            cursor = db_connect_obj.searchInColumn(db_connect_obj.getTable_name_parties(),"person_name",customer_name.getText().toString());
        }
        if (cursor.getCount() >0 ) {
            Toast.makeText(this, "This customer name already exsist. Create new customer name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cursor != null) {
            cursor.close();
        }

        // Collect data into ContentValues
        ContentValues table_data = new ContentValues();
        table_data.put("trade_type", "sale");
        table_data.put("person_name", customer_name.getText().toString());
        table_data.put("phone_number", mobile_number.getText().toString());
        table_data.put("email_id", email_id.getText().toString());
        table_data.put("gst_number", gstin.getText().toString());

        // Insert data into the database
        if (db_connect_obj.insert_data(db_connect_obj.getTable_name_parties(), table_data)) {
            Toast.makeText(this, "New Customer Saved!", Toast.LENGTH_SHORT).show();
            // Optionally, finish the activity after saving
            // finish();
        } else {
            Toast.makeText(this, "Error to Save New Customer!", Toast.LENGTH_SHORT).show();
        }
    }
    public void initialization(){
        db_connect_obj = new database_connectivity(this);
    }
    public void findViewById() {
        // Initialize database helpers

        // Layout
        main = findViewById(R.id.main);

        // ImageViews
        back = findViewById(R.id.back);
        save_new_customer = findViewById(R.id.save_new_customer);

        // TextViews
        add_new_customer_page_title = findViewById(R.id.add_new_customer_page_title);
        store_name = findViewById(R.id.business_name);

        // EditTexts
        customer_name = findViewById(R.id.person_name);
        mobile_number = findViewById(R.id.mobile_number);
        email_id = findViewById(R.id.email_id);
        gstin = findViewById(R.id.gstin);

        // Create the 'customers' table if it doesn't exist
        db_connect_obj.create_table_parties();
}}