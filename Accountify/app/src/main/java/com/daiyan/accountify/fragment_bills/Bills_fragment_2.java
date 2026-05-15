package com.daiyan.accountify.fragment_bills;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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

public class Bills_fragment_2 extends AppCompatActivity {

    private database_connectivity db_connect_obj;
    private RecyclerView recyclerView;
    private Cursor cursor, partiesCursor;
    private ExtendedFloatingActionButton fab;
    private TextView headerTitle; // NAYA TEXTVIEW VARIABLE

    // Tracker object for selection logic
    private SelectionTracker<Long> tracker;
    ArrayList<Bundle> bundleList = new ArrayList<>();

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
        headerTitle = findViewById(R.id.sales); // HEADER TEXT KO PAKDA

        // THE ARCHITECT FIX: Dynamic Header
        boolean isReportMode = getIntent().getBooleanExtra("IS_REPORT_MODE", false);
        if(isReportMode){
            headerTitle.setText("Create Report");
            fab.setVisibility(View.GONE);
        } else {
            headerTitle.setText("Create Invoice");
            fab.setText("Create New Invoice");
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(v -> createTemplate());

        loadBillsData();
    }

    private void loadBillsData() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        cursor = db_connect_obj.get_data(db_connect_obj.getTable_name_parties());

        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(this)
                .setData(R.layout.customer_list, cursor)
                .map(RecycleViewHelper.customerList().getName(), "person_name")
                .map(RecycleViewHelper.customerList().getPersonType(), "trade_type")
                .map(RecycleViewHelper.customerList().getDueDate(), "phone_number")
                .map(RecycleViewHelper.customerList().getAmount(), "pending")
                .map(RecycleViewHelper.customerList().getStatusLabel(), "pending")
                .map(RecycleViewHelper.customerList().getImage(), "person_name")

                .setBindListener((view, cursor1, colName, id) -> {
                    int idx = cursor1.getColumnIndex(colName);
                    if (idx == -1) return false;
                    String val = cursor1.getString(idx);

                    if (id == RecycleViewHelper.customerList().getDueDate()) {
                        if (val == null || val.trim().isEmpty()) {
                            view.setVisibility(View.GONE);
                        } else {
                            ((TextView) view).setText(val.trim());
                            view.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }

                    if (id == RecycleViewHelper.customerList().getPersonType()) {
                        String typeText = "";
                        ((TextView) view).setText(typeText);
                        view.setVisibility(typeText.isEmpty() ? View.GONE : View.VISIBLE);
                        return true;
                    }

                    if (id == RecycleViewHelper.customerList().getAmount() || id == RecycleViewHelper.customerList().getStatusLabel()) {
                        double amount = 0;
                        try {
                            if (val != null && !val.trim().isEmpty()) amount = Double.parseDouble(val);
                        } catch (NumberFormatException e) { amount = 0; }

                        int color;
                        String statusText;

                        color = Color.parseColor("#404040");
                        statusText = "Sold Value";

                        if (id == RecycleViewHelper.customerList().getAmount()) {
                            ((TextView) view).setText("₹ " + Math.abs(amount));
                        } else {
                            ((TextView) view).setText(statusText);
                        }
                        ((TextView) view).setTextColor(color);
                        return true;
                    }

                    if (id == RecycleViewHelper.customerList().getImage()) {
                        ((ImageView) view).setImageResource(R.drawable.default_contect_logo);
                        return true;
                    }
                    return false;
                })
                .setOnItemClick((cursorClick, position) -> {
                    if (tracker != null && tracker.hasSelection()) {
                        return;
                    }
                    Intent intent = new Intent(this, Bills_fragment_3.class);
                    String name = cursorClick.getString(cursorClick.getColumnIndexOrThrow("person_name"));
                    intent.putExtra("name", name);
                    intent.putExtra("IS_REPORT_MODE", getIntent().getBooleanExtra("IS_REPORT_MODE", false));
                    startActivity(intent);

                    Toast.makeText(this, "Person Clicked", Toast.LENGTH_SHORT).show();
                })
                .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tracker = new SelectionTracker.Builder<>(
                "parties_selection_id",
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
                    fab.setVisibility(View.GONE);
                    fab.setOnClickListener(v -> processSelectedBills());
                } else {
                    fab.setVisibility(View.GONE);                    fab.setOnClickListener(v -> createTemplate());
                }
            }
        });
    }

    public void createTemplate() {
        Intent intent = new Intent(this, bills_template.class);
        startActivity(intent);
    }

    public Bundle createContentBundle(Cursor cursor) {
        Bundle b = new Bundle();

        b.putString("item_name", cursor.getString(cursor.getColumnIndexOrThrow("item_name")));
        b.putString("quantity", cursor.getString(cursor.getColumnIndexOrThrow("quantity")));
        b.putString("rate", cursor.getString(cursor.getColumnIndexOrThrow("rate")));
        b.putString("unit", cursor.getString(cursor.getColumnIndexOrThrow("unit")));
        b.putString("subtotal", cursor.getString(cursor.getColumnIndexOrThrow("subtotal")));

        return b;
    }
    public void processSelectedBills() {
        if (tracker == null || !tracker.hasSelection()) return;

        int count = tracker.getSelection().size();

        for (Long selectionKey : tracker.getSelection()) {
            int position = selectionKey.intValue();
            if (cursor != null && cursor.moveToPosition(position)) {
                bundleList.add(createContentBundle(cursor));
            }
        }

        Toast.makeText(this, count + " Bills Processed", Toast.LENGTH_SHORT).show();
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