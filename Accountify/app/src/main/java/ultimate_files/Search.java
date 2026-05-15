package ultimate_files;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
using method
Search searchTool = new Search();
SearchView mySearchView = findViewById(R.id.search_view);

// Function name same hai, parameters same hain
searchTool.input(mySearchView, myCursor, "product_name", new Search.SearchCallback() {
    @Override
    public void output(List<ContentValues> results) {
        myAdapter.updateData(results);
    }
});

*/
public class Search {

    // Background tasks ke liye Thread Pool (Efficient)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // UI par wapas aane ke liye Handler
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // To track latest query time (Concurrency handling)
    private long lastSearchTime = 0;

    public interface SearchCallback {
        void output(List<ContentValues> results);
    }

    // ---------------------------------------------------------
    // METHOD 1: For TextView / EditText
    // ---------------------------------------------------------
    public void input(TextView textView, Cursor cursor, String columnName, SearchCallback callback) {
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performFilterAsync(s.toString(), cursor, columnName, callback);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ---------------------------------------------------------
    // METHOD 2: For SearchView
    // ---------------------------------------------------------
    public void input(SearchView searchView, Cursor cursor, String columnName, SearchCallback callback) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                performFilterAsync(newText, cursor, columnName, callback);
                return true;
            }
        });
    }

    // ---------------------------------------------------------
    // THE ROCKET ENGINE (Async & Optimized)
    // ---------------------------------------------------------
    private void performFilterAsync(String query, Cursor cursor, String columnName, SearchCallback callback) {
        // Timestamp capture karein taaki purane results discard kar sakein
        final long searchTime = System.currentTimeMillis();
        lastSearchTime = searchTime;

        // 1. Background Thread Start
        executor.execute(() -> {
            List<ContentValues> filteredList = new ArrayList<>();

            // Safety Checks
            if (cursor == null || cursor.isClosed()) {
                postResult(filteredList, callback, searchTime);
                return;
            }

            String cleanQuery = query.toLowerCase().trim();
            boolean isEmptyQuery = cleanQuery.isEmpty();

            // 2. OPTIMIZATION: Index lookup OUTSIDE the loop
            int colIndex = cursor.getColumnIndex(columnName);
            if (colIndex == -1) {
                postResult(filteredList, callback, searchTime);
                return;
            }

            // 3. Fast Loop Strategy
            if (cursor.moveToFirst()) {
                do {
                    // Check: Agar user ne naya type kar diya hai, toh ye loop roko (Save CPU)
                    if (searchTime != lastSearchTime) return;

                    String dbValue = cursor.getString(colIndex);

                    // Logic check
                    if (isEmptyQuery || (dbValue != null && dbValue.toLowerCase().contains(cleanQuery))) {
                        ContentValues row = new ContentValues();
                        // Note: cursorRowToContentValues is heavy, but necessary for your requirement
                        DatabaseUtils.cursorRowToContentValues(cursor, row);
                        filteredList.add(row);
                    }
                } while (cursor.moveToNext());
            }

            // 4. Send Result back to UI
            postResult(filteredList, callback, searchTime);
        });
    }

    // Helper to send data to Main Thread safely
    private void postResult(List<ContentValues> results, SearchCallback callback, long searchTime) {
        mainHandler.post(() -> {
            // Sirf tabhi update karein agar ye latest search ka result hai
            if (searchTime == lastSearchTime) {
                callback.output(results);
            }
        });
    }
}