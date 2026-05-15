package com.daiyan.accountify.fragment_bills;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daiyan.accountify.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import ultimate_files.RecycleViewHelper;
import ultimate_files.database.database_connectivity;

public class Bills_fragment_3 extends AppCompatActivity {

    private database_connectivity db_connect_obj;
    private RecyclerView recyclerView;
    private Cursor cursor;
    private ExtendedFloatingActionButton fab;
    private TextView headerTitle;

    private SelectionTracker<Long> tracker;
    ArrayList<Bundle> bundleList = new ArrayList<>();
    String getName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bills_fragment2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db_connect_obj = new database_connectivity(this);

        recyclerView = findViewById(R.id.bills_recycle_view);
        fab = findViewById(R.id.Add);
        fab.setText("Create");

        headerTitle = findViewById(R.id.sales);

        // Dynamic Header based on Mode
        boolean isReportMode = getIntent().getBooleanExtra("IS_REPORT_MODE", false);
        if(isReportMode){
            headerTitle.setText("Create Report");
            fab.setVisibility(View.GONE);

        } else {
            headerTitle.setText("Create Invoice");
            fab.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);

        }

        fab.setOnClickListener(v -> createTemplate());

        loadBillsData(getData());
        fab.setVisibility(View.GONE);
    }

    public String getName(){
        return getName;
    }

    public String getData(){
        Bundle bundle = getIntent().getExtras();
        String name = (bundle != null) ? bundle.getString("name"): "";
        getName = name;
        return name;
    }

    private void loadBillsData(String name) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        boolean isReportMode = getIntent().getBooleanExtra("IS_REPORT_MODE", false);

        // THE ARCHITECT FIX: STRICT MODE-BASED ROUTING
        if (isReportMode) {
            // REPORT MODE: Person ka saara data (Sale + Purchase)
            cursor = db_connect_obj.searchInColumn(db_connect_obj.getTable_name_product(), "person_name", name);
        } else {
            // INVOICE MODE: Strictly sirf 'Sale' data aayega us person ka
            cursor = db_connect_obj.searchInMultiColumnsMultiCondition(
                    db_connect_obj.getTable_name_product(),
                    new String[][]{
                            {"trade_type", "==", "sale", "AND", "("},
                            {"person_name", "==", name, "OR", ")"}
                    }
            );
        }

        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(this)
                .setData(R.layout.manage_stock_list_item, cursor)
                .map(RecycleViewHelper.stockList().getName(), "item_name")
                .map(RecycleViewHelper.stockList().getQuantity(), "quantity")
                .map(RecycleViewHelper.stockList().getRate(), "trade_date")
                .map(RecycleViewHelper.stockList().getTotalPrice(), "total_amount")
                .map(RecycleViewHelper.stockList().getImage(), "item_name")
                .map(RecycleViewHelper.stockList().getPriceLabel(), "item_name")
                .setBindListener((view, cursor1, colName, id) -> {
                    int idx = cursor1.getColumnIndex(colName);
                    if (idx == -1) return false;
                    String val = cursor1.getString(idx);

                    if (id == RecycleViewHelper.stockList().getQuantity()) {
                        int tradeTypeIdx = cursor1.getColumnIndex("trade_type");
                        String tradeType = (tradeTypeIdx != -1) ? cursor1.getString(tradeTypeIdx) : "";

                        String label = tradeType.equalsIgnoreCase("sale") ? "Sold to " : "Bought from ";
                        ((TextView) view).setText(label + getName());
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getRate()) {
                        ((TextView) view).setText("Date: " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getTotalPrice()) {
                        ((TextView) view).setText("₹ " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getPriceLabel()) {
                        int rateIdx = cursor1.getColumnIndex("rate");
                        int qtyIdx = cursor1.getColumnIndex("quantity");
                        int unitIdx = cursor1.getColumnIndex("unit");
                        String unit = (unitIdx != -1) ? cursor1.getString(unitIdx) : "";
                        String qtyStr = (qtyIdx != -1) ? cursor1.getString(qtyIdx) : "";
                        String rateStr = (rateIdx != -1) ? cursor1.getString(rateIdx) : "";
                        ((TextView) view).setText(qtyStr + " x " + rateStr + " " + unit);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getImage()) {
                        ((ImageView) view).setImageResource(R.drawable.product_icon);
                        return true;
                    }
                    return false;
                })
                .setOnItemClick((cursorClick, position) -> {
                    if (tracker != null && tracker.hasSelection()) {
                        return;
                    }

                    bundleList.clear();
                    bundleList.add(createContentBundle(cursorClick));

                    Intent intent = new Intent(this, bills_template.class);
                    intent.putParcelableArrayListExtra("com.daiyan.accountify.fragment_bills", bundleList);
                    intent.putExtra("IS_REPORT_MODE", getIntent().getBooleanExtra("IS_REPORT_MODE", false));
                    startActivity(intent);
                })
                .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tracker = new SelectionTracker.Builder<>(
                "bills_selection_id",
                recyclerView,
                new StableIdKeyProvider(recyclerView),
                new RecycleViewHelper.GenericItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.<Long>createSelectAnything())
                .build();

        adapter.setTracker(tracker);

        tracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (tracker.hasSelection()) {
                    fab.setVisibility(View.VISIBLE);
                    fab.setOnClickListener(v -> processSelectedBills());
                } else {
                    fab.setVisibility(View.GONE);
                    fab.setOnClickListener(v -> createTemplate());
                }
            }
        });
    }

    public void createTemplate() {
        Intent intent = new Intent(this, bills_template.class);
        startActivity(intent);
    }

    public Bundle createContentBundle(Cursor cursorN) {
        Bundle b = new Bundle();
        b.putString("person_name", getName());

        // Safe data extraction for bundle
        b.putString("phone_number", cursorN.getString(cursorN.getColumnIndexOrThrow("phone_number")));
        b.putString("trade_type", cursorN.getString(cursorN.getColumnIndexOrThrow("trade_type")));
        b.putString("item_name", cursorN.getString(cursorN.getColumnIndexOrThrow("item_name")));
        b.putString("quantity", cursorN.getString(cursorN.getColumnIndexOrThrow("quantity")));
        b.putString("rate", cursorN.getString(cursorN.getColumnIndexOrThrow("rate")));
        b.putString("unit", cursorN.getString(cursorN.getColumnIndexOrThrow("unit")));
        b.putString("subtotal", cursorN.getString(cursorN.getColumnIndexOrThrow("subtotal")));
        b.putString("trade_date", cursorN.getString(cursorN.getColumnIndexOrThrow("trade_date")));
        b.putString("gst_in_percent", cursorN.getString(cursorN.getColumnIndexOrThrow("gst_in_percent")));
        b.putString("gst_in_rupee", cursorN.getString(cursorN.getColumnIndexOrThrow("gst_in_rupee")));
        b.putString("discount_in_percent", cursorN.getString(cursorN.getColumnIndexOrThrow("discount_in_percent")));
        b.putString("discount_in_rupee", cursorN.getString(cursorN.getColumnIndexOrThrow("discount_in_rupee")));
        return b;
    }

    public void processSelectedBills() {
        if (tracker == null || !tracker.hasSelection()) return;

        bundleList.clear();

        for (Long selectionKey : tracker.getSelection()) {
            int position = selectionKey.intValue();
            if (cursor != null && cursor.moveToPosition(position)) {
                bundleList.add(createContentBundle(cursor));
            }
        }

        Intent intent = new Intent(this, bills_template.class);
        intent.putParcelableArrayListExtra("com.daiyan.accountify.fragment_bills", bundleList);
        intent.putExtra("IS_REPORT_MODE", getIntent().getBooleanExtra("IS_REPORT_MODE", false));
        startActivity(intent);

        tracker.clearSelection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}