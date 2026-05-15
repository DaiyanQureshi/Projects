package com.daiyan.accountify.fragment_report;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daiyan.accountify.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import ultimate_files.RecycleViewHelper;
import ultimate_files.database.database_connectivity;

public class ReportFragment extends Fragment {

    private database_connectivity db_connect_obj;
    private RecyclerView recyclerView;
    private Cursor cursor;
    private ExtendedFloatingActionButton fab;
    private SelectionTracker<Long> tracker;

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db_connect_obj = new database_connectivity(getContext());
        recyclerView = view.findViewById(R.id.report_recycle_view);
        fab = view.findViewById(R.id.Add);
        fab.setVisibility(View.GONE);
        fab.setText("Delete Invoice");

        fab.setOnClickListener(v -> createTemplate());

        loadReportData();
        fab.setVisibility(View.VISIBLE);
        fab.setText("Create Report");
    }

    // THE ARCHITECT FIX: Automatically refresh hoga list jab tu naya report save karke wapas aayega
    @Override
    public void onResume() {
        super.onResume();
        loadReportData();
    }

    private void loadReportData() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        // REPORT TABLE se data uthao
        SQLiteDatabase sqlDb = db_connect_obj.getReadableDatabase();
        cursor = sqlDb.rawQuery("SELECT * FROM " + db_connect_obj.getTable_name_report() + " GROUP BY invoice_no ORDER BY id DESC", null);

        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(getContext())
                .setData(R.layout.manage_stock_list_item, cursor)
                .map(RecycleViewHelper.stockList().getName(), "invoice_no")
                .map(RecycleViewHelper.stockList().getQuantity(), "business_name") // Person name humne yahan save kiya tha
                .map(RecycleViewHelper.stockList().getRate(), "invoice_date")
                .map(RecycleViewHelper.stockList().getTotalPrice(), "invoice_final_total")
                .map(RecycleViewHelper.stockList().getPriceLabel(), "invoice_no")
                .map(RecycleViewHelper.stockList().getImage(), "invoice_no")
                .setBindListener((view, cursor1, colName, id) -> {
                    int idx = cursor1.getColumnIndex(colName);
                    if (idx == -1) return false;
                    String val = cursor1.getString(idx);

                    if (id == RecycleViewHelper.stockList().getQuantity()) {
                        ((TextView) view).setText("Report For: " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getRate()) {
                        ((TextView) view).setText("Date: " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getTotalPrice()) {
                        ((TextView) view).setText("₹ " + (val != null ? val.split("\\.")[0] : "0"));
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getPriceLabel()) {
                        ((TextView) view).setText("Total Amount");
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getImage()) {
                        view.setBackground(null);
                        ImageView iconView = (ImageView) view;
                        iconView.setImageResource(R.drawable.report_icon); // Custom icon lagana chahe toh change kar lena

                        int paddingInDp = 9;
                        float scale = view.getResources().getDisplayMetrics().density;
                        int paddingInPx = (int) (paddingInDp * scale + 0.5f);
                        iconView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
                        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        return true;
                    }
                    return false;
                })
                .setOnItemClick((cursorClick, position) -> {
                    if (tracker != null && tracker.hasSelection()) {
                        return;
                    }

                    String clickedInvNo = cursorClick.getString(cursorClick.getColumnIndexOrThrow("invoice_no"));

                    SQLiteDatabase db = db_connect_obj.getReadableDatabase();
                    Cursor invCursor = db.rawQuery("SELECT * FROM " + db_connect_obj.getTable_name_report() + " WHERE invoice_no = ?", new String[]{clickedInvNo});

                    ArrayList<Bundle> bundleList = new ArrayList<>();
                    if (invCursor != null && invCursor.moveToFirst()) {
                        do {
                            Bundle b = new Bundle();
                            for (String dbColumnName : invCursor.getColumnNames()) {
                                int colIndex = invCursor.getColumnIndex(dbColumnName);
                                if (colIndex != -1) {
                                    String value = invCursor.getString(colIndex);
                                    b.putString(dbColumnName, value != null ? value : "");
                                }
                            }
                            bundleList.add(b);
                        } while (invCursor.moveToNext());
                        invCursor.close();
                    }

                    // THE ROUTING: Template ko pata chalna chahiye ki Report dikhani hai
                    Intent intent = new Intent(requireContext(), com.daiyan.accountify.fragment_bills.bills_template.class);
                    intent.putParcelableArrayListExtra("com.daiyan.accountify.fragment_bills", bundleList);
                    intent.putExtra("VIEW_MODE", true);
                    intent.putExtra("IS_REPORT_MODE", true);
                    startActivity(intent);
                })
                .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        tracker = new SelectionTracker.Builder<>(
                "report_selection_id",
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
                    fab.setText("Create Report");
                } else {
                    fab.setVisibility(View.GONE);
                    fab.setOnClickListener(v -> createTemplate());
                }
            }
        });
    }

    public void createTemplate(){
        Intent intent = new Intent(requireContext(), com.daiyan.accountify.fragment_bills.Bills_fragment_2.class);
        intent.putExtra("IS_REPORT_MODE", true);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}