package com.daiyan.accountify.fragment_bills;

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

public class BillsFragment extends Fragment {

    private database_connectivity db_connect_obj;
    private RecyclerView recyclerView;
    private Cursor cursor;
    private ExtendedFloatingActionButton fab;
    private SelectionTracker<Long> tracker;

    public static BillsFragment newInstance() {
        return new BillsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bills, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db_connect_obj = new database_connectivity(getContext());
        recyclerView = view.findViewById(R.id.bills_recycle_view);
        fab = view.findViewById(R.id.Add);
        fab.setText("Create New Invoice");
        fab.setVisibility(View.VISIBLE);



        fab.setOnClickListener(v -> createTemplate());

        loadBillsData();
    }

    private void loadBillsData() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        SQLiteDatabase sqlDb = db_connect_obj.getReadableDatabase();
        cursor = sqlDb.rawQuery("SELECT * FROM " + db_connect_obj.getTable_name_invoice() + " GROUP BY invoice_no ORDER BY id DESC", null);

        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(getContext())
                .setData(R.layout.manage_stock_list_item, cursor)
                .map(RecycleViewHelper.stockList().getName(), "invoice_no")
                .map(RecycleViewHelper.stockList().getQuantity(), "person_name")
                .map(RecycleViewHelper.stockList().getRate(), "invoice_date")
                .map(RecycleViewHelper.stockList().getTotalPrice(), "invoice_final_total")
                .map(RecycleViewHelper.stockList().getPriceLabel(), "invoice_no") // Dummy map for listener trigger
                .map(RecycleViewHelper.stockList().getImage(), "invoice_no")
                .setBindListener((view, cursor1, colName, id) -> {
                    int idx = cursor1.getColumnIndex(colName);
                    if (idx == -1) return false;
                    String val = cursor1.getString(idx);

                    if (id == RecycleViewHelper.stockList().getQuantity()) {
                        ((TextView) view).setText("Sold to " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getRate()) {
                        ((TextView) view).setText("Date: " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getTotalPrice()) {
                        // Agar purani DB hui toh null aa sakta hai, safe check
                        ((TextView) view).setText(("₹ " + (val != null ? val : "0.00").split("\\.")[0]));
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getPriceLabel()) {
                        // FIX: Word as text "Total Amount" exactly as requested
                        ((TextView) view).setText("Total Amount");
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getImage()) {
                        view.setBackground(null);
                        ImageView iconView = (ImageView) view;
                        iconView.setImageResource(R.drawable.invoice_icon);

                        // PADDING ENGINE: Android pixels samajhta hai, DP nahi.
                        // Isliye hum yahan 12dp ko screen density ke hisaab se pixels me convert kar rahe hain.
                        int paddingInDp = 12;
                        float scale = view.getResources().getDisplayMetrics().density;
                        int paddingInPx = (int) (paddingInDp * scale + 0.5f);

                        // Icon ke chaaro taraf padding set kar di
                        iconView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

                        // ScaleType FIT_CENTER kar diya taaki icon stretch ya crop na ho, center me perfectly fit ho
                        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                        return true;
                    }
                    return false;
                })
                .setOnItemClick((cursorClick, position) -> {
                    if (tracker != null && tracker.hasSelection()) {
                        return;
                    }

                    // 1. Click kiye gaye invoice ka number nikalo
                    String clickedInvNo = cursorClick.getString(cursorClick.getColumnIndexOrThrow("invoice_no"));

                    // 2. Database se us invoice ke saare items nikalo
                    SQLiteDatabase db = db_connect_obj.getReadableDatabase();
                    Cursor invCursor = db.rawQuery("SELECT * FROM " + db_connect_obj.getTable_name_invoice() + " WHERE invoice_no = ?", new String[]{clickedInvNo});

                    ArrayList<Bundle> bundleList = new ArrayList<>();
                    if (invCursor != null && invCursor.moveToFirst()) {
                        do {
                            Bundle b = new Bundle();

                            // PRO FIX: DYNAMIC EXTRACTOR
                            // Ab yahan columns ka naam nahi dena, database khud apne columns array me daal dega.
                            // Isse purani DB table par bhi app kabhi crash nahi hogi!
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

                    // 3. Data bhej kar template kholo (VIEW_MODE on karke)
                    Intent intent = new Intent(requireContext(), bills_template.class);
                    intent.putParcelableArrayListExtra("com.daiyan.accountify.fragment_bills", bundleList);
                    intent.putExtra("VIEW_MODE", true);
                    startActivity(intent);
                })
                .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                //
                if (tracker.hasSelection()) {
                    fab.setText("Delete Invoice");
                    fab.setOnClickListener(v -> deleteTradeTable());
                } else {
                    fab.setText("Create New Invoice");
                    fab.setOnClickListener(v -> createTemplate());
                }
            }
        });
    }

    public void deleteTradeTable() {
        for (Long selectionKey : tracker.getSelection()) {
            int position = selectionKey.intValue();
            Cursor cursorNew  = db_connect_obj.get_data(db_connect_obj.getTable_name_trade());
            if (cursorNew != null && cursorNew.moveToPosition(position)) {
                try {
                    String uniqueId = cursorNew.getString(cursorNew.getColumnIndexOrThrow("id"));
                   db_connect_obj.delete_row(db_connect_obj.getTable_name_trade(), "id", uniqueId);
                } catch (Exception e) {
                }
            }

        }
    }

    public void createTemplate() {
        Intent intent = new Intent(requireContext(), Bills_fragment_2.class);
        startActivity(intent);
    }

    public void processSelectedBills() {
        if (tracker == null || !tracker.hasSelection()) return;
        Toast.makeText(getContext(), tracker.getSelection().size() + " Bills Selected", Toast.LENGTH_SHORT).show();
        tracker.clearSelection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}