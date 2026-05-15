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

public class purchase extends AppCompatActivity {

    Cursor cursor;
    database_connectivity db_connect_obj;
    ExtendedFloatingActionButton fab;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_purchase);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db_connect_obj = new database_connectivity(this);
        fab = findViewById(R.id.Add);
        fab.setText("Add New Stock");
        fab.setOnClickListener(v->addPurchase(v));
        recyclerView = findViewById(R.id.stock_item_recycle_view);

        // THE FIX 1: Function call karna zaroori hai
        set_item_for_product();
    }
    public void addPurchase(View v){
        Intent intent = new Intent(this, trade_sale.class);
        startActivity(intent);

    }

    public void set_item_for_product() {

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // Query product table par hai
        cursor = db_connect_obj.searchInColumn(db_connect_obj.getTable_name_product(), "trade_type", "purchase");

        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(this)
                .setData(R.layout.manage_stock_list_item, cursor)
                // THE FIX 2: Sahi column names map kiye (Product table wale)
                .map(RecycleViewHelper.stockList().getName(), "item_name")
                .map(RecycleViewHelper.stockList().getQuantity(), "quantity")
                .map(RecycleViewHelper.stockList().getRate(), "rate")
                .map(RecycleViewHelper.stockList().getTotalPrice(), "total_amount")
                .map(RecycleViewHelper.stockList().getImage(), "item_name")
                .map(RecycleViewHelper.stockList().getPriceLabel(), "item_name")
                .setBindListener((view, cursor1, colName, id) -> {

                    int idx = cursor1.getColumnIndex(colName);
                    if (idx == -1) return false;
                    String val = cursor1.getString(idx);

                    if (id == RecycleViewHelper.stockList().getQuantity()) {
                        // Sahi unit column
                        int unitIdx = cursor1.getColumnIndex("unit");
                        String unit = (unitIdx != -1) ? cursor1.getString(unitIdx) : "";
                        ((TextView) view).setText("Qty: " + val + " " + unit);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getRate()) {
                        ((TextView) view).setText("Rate: " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getTotalPrice()) {
                        ((TextView) view).setText("₹ " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getPriceLabel()) {
                        int personIdx = cursor1.getColumnIndex("person_name");
                        String person = (personIdx != -1) ? cursor1.getString(personIdx) : "";
                        ((TextView) view).setText("To: " + person);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getImage()) {
                        ((ImageView) view).setImageResource(R.drawable.product_icon);
                        return true;
                    }
                    return false;
                })
                .build();

        // THE FIX 3: Adapter aur Layout Manager set karna zaroori hai
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}