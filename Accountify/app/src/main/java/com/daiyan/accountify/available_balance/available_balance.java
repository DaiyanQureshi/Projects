package com.daiyan.accountify.available_balance;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.daiyan.accountify.R;
import ultimate_files.database.sqlite_database;

public class available_balance extends AppCompatActivity {


    // Layouts
    ConstraintLayout mainLayout;
    ScrollView scrollView2;
    ConstraintLayout main;
    FrameLayout current_balance_panel;

    // ImageViews
    ImageView back;
    ImageView available_balance_save;

    // TextViews
    TextView available_balance_page_title;
    TextView current_balance_text;
    TextView textView7; // Spacer

    // EditTexts
    EditText current_balance;

    // Database
    sqlite_database sqlite_db_obj;
    private static final String TABLE_NAME = "available_balance";

    public static String get_table_name() {
        return TABLE_NAME;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_available_balance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        findViewById();
        if(sqlite_db_obj.is_table_exists(TABLE_NAME)){load_available_balance_data();}
        // Set click listener for the save button
        available_balance_save.setOnClickListener(v -> {
            save_balance_Data();
        });
        back.setOnClickListener(v ->{finish();});
    }
    private void load_available_balance_data() {
        Cursor cursor = sqlite_db_obj.get_data(TABLE_NAME);

        if (cursor != null && cursor.moveToFirst()) {
            current_balance.setText(cursor.getString(1));
            }

        if (cursor != null) {
            cursor.close();
        }
    }
    public void save_balance_Data() {
        // Validate that current balance is not empty
        String balance = current_balance.getText().toString().trim();
        if (balance.isEmpty()) {
            Toast.makeText(this, "Current balance cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect data into ContentValues
        ContentValues table_data = new ContentValues();
        table_data.put("current_balance", balance);


        if (sqlite_db_obj.update_data(TABLE_NAME, table_data, "id", "1")) {
            Toast.makeText(this, "Balance Saved!", Toast.LENGTH_SHORT).show();
        } else {
            if (sqlite_db_obj.insert_data(TABLE_NAME, table_data)) {
                Toast.makeText(this, "Balance Saved!", Toast.LENGTH_SHORT).show();
            }
                else{Toast.makeText(this, "Error to Save Balance!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void findViewById() {
        // Initialize database helpers
        sqlite_db_obj = new sqlite_database(this);

        // Layouts
        mainLayout = findViewById(R.id.mainLayout);
        scrollView2 = findViewById(R.id.scrollView2);
        main = findViewById(R.id.main);
        current_balance_panel = findViewById(R.id.current_balance_panel);

        // ImageViews
        back = findViewById(R.id.back);
        available_balance_save = findViewById(R.id.available_balance_save);

        // TextViews
        available_balance_page_title = findViewById(R.id.available_balance_page_title);
        current_balance_text = findViewById(R.id.current_balance_text);
        textView7 = findViewById(R.id.textView7);

        // EditTexts
        current_balance = findViewById(R.id.current_balance);

        // Create the 'available_balance' table if it doesn't exist
        // It will store a log of balance entries.
//        sqlite_db_obj.create_table(
//                TABLE_NAME,
//                "current_balance"
//        );
    }
}