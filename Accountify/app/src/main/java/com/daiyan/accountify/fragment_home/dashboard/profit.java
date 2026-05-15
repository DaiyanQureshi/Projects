package com.daiyan.accountify.fragment_home.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import ultimate_files.RecycleViewHelper;
import ultimate_files.database.database_connectivity;

public class profit extends AppCompatActivity {

    Cursor cursor;
    database_connectivity db_connect_obj;
    ExtendedFloatingActionButton fab;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profit);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db_connect_obj = new database_connectivity(this);
        fab = findViewById(R.id.Add);
        fab.setText("Add New Stock");
        recyclerView = findViewById(R.id.stock_item_recycle_view);

        // THE FIX 1: Method ko call karna zaroori hai
        load_profit_list();
    }

    public void load_profit_list() {

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // Trade table mein har item ki in-stock aur sold history hoti hai
        cursor = db_connect_obj.get_data(db_connect_obj.getTable_name_trade());

        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(this)
                .setData(R.layout.manage_stock_list_item, cursor)
                .map(RecycleViewHelper.stockList().getName(), "item_name")
                .map(RecycleViewHelper.stockList().getQuantity(), "sold_quantity")
                .map(RecycleViewHelper.stockList().getRate(), "in_stock_avg_rate")
                .map(RecycleViewHelper.stockList().getTotalPrice(), "in_stock_total_amount") // Dummy map, actual calculation aage hogi
                .map(RecycleViewHelper.stockList().getImage(), "item_name")
                .map(RecycleViewHelper.stockList().getPriceLabel(), "item_name")
                .setBindListener((view, cursor1, colName, id) -> {

                    int idx = cursor1.getColumnIndex(colName);
                    if (idx == -1) return false;

                    // 1. Kitna maal bika (Sold Qty)
                    if (id == RecycleViewHelper.stockList().getQuantity()) {
                        String soldQty = cursor1.getString(cursor1.getColumnIndexOrThrow("sold_quantity"));
                        String unit = cursor1.getString(cursor1.getColumnIndexOrThrow("sold_unit"));
                        unit = (unit != null) ? unit : "";
                        ((TextView) view).setText("Sold: " + soldQty + " " + unit);
                        return true;
                    }

                    // 2. Maal kitne me kharida tha (Avg Cost)
                    if (id == RecycleViewHelper.stockList().getRate()) {
                        String avgBuyRate = cursor1.getString(cursor1.getColumnIndexOrThrow("in_stock_avg_rate"));
                        ((TextView) view).setText("Avg Cost: ₹ " + avgBuyRate);
                        return true;
                    }

                    // 3. Profit ka label
                    if (id == RecycleViewHelper.stockList().getPriceLabel()) {
                        ((TextView) view).setText("Net Profit");
                        return true;
                    }

                    // 4. THE FIX 3: Asli Profit Calculation
                    if (id == RecycleViewHelper.stockList().getTotalPrice()) {
                        double soldQty = cursor1.getDouble(cursor1.getColumnIndexOrThrow("sold_quantity"));
                        double soldTotalAmount = cursor1.getDouble(cursor1.getColumnIndexOrThrow("sold_total_amount"));
                        double inStockAvgRate = cursor1.getDouble(cursor1.getColumnIndexOrThrow("in_stock_avg_rate"));

                        // Formula: Profit = Kitne ka bika - (Kitna bika * Ek item kharidne me kitna laga)
                        double profitAmount = soldTotalAmount - (soldQty * inStockAvgRate);

                        TextView profitText = (TextView) view;
                        profitText.setText("₹ " + String.format(java.util.Locale.getDefault(), "%.2f", profitAmount));

                        // Profit h toh Green, Loss h toh Red
                        if (profitAmount >= 0) {
                            profitText.setTextColor(Color.parseColor("#08BD7C"));
                        } else {
                            profitText.setTextColor(Color.parseColor("#AB0836"));
                        }
                        return true;
                    }

                    // 5. Image Icon
                    if (id == RecycleViewHelper.stockList().getImage()) {
                        ((ImageView) view).setImageResource(R.drawable.product_icon);
                        return true;
                    }

                    return false;
                })
                .build();

        // THE FIX 2: Layout Manager aur Adapter assign karna zaroori hai
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}