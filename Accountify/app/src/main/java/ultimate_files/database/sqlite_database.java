package ultimate_files.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import androidx.annotation.Nullable;

public class sqlite_database extends SQLiteOpenHelper {

    static final String database_default_file_name = "Business1.db";

    public String get_database_default_file_name() {
        return database_default_file_name;
    }

    public sqlite_database(@Nullable Context context) {
        super(context, database_default_file_name, null, 1);
    }

    public sqlite_database(@Nullable Context context, String database_new_file_name) {
        super(context, database_new_file_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // We create tables manually
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Handle upgrades if needed
    }

    // ==========================================
    // DATA SANITIZATION ENGINE (NEW ADDED)
    // ==========================================

    // Yeh function specifically un keys ko dhundhega aur convert karega
    private void sanitizeData(ContentValues table_data) {
        if (table_data == null) return;

        if (table_data.containsKey("item_name")) {
            String val = table_data.getAsString("item_name");
            if (val != null) {
                table_data.put("item_name", toTitleCase(val));
            }
        }
        if (table_data.containsKey("person_name")) {
            String val = table_data.getAsString("person_name");
            if (val != null) {
                table_data.put("person_name", toTitleCase(val));
            }
        }
    }

    // Yeh core logic hai jo kisi bhi string ko properly Title Case mein badalta hai
    private String toTitleCase(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Faltu spaces ko remove kar deta hai taaki design kharab na ho
        text = text.trim().replaceAll(" +", " ");

        StringBuilder converted = new StringBuilder();
        boolean convertNext = true;

        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }
        return converted.toString();
    }
    // ==========================================

    public void create_tables(String table_name, String[] column_names) { // done
        SQLiteDatabase sqlite_obj = this.getWritableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ");
        query.append(table_name);
        query.append(" (id INTEGER PRIMARY KEY AUTOINCREMENT");

        for (String column_name : column_names) {
            query.append(", ");
            query.append(column_name);
            query.append(" TEXT");
        }
        query.append(")");

        sqlite_obj.execSQL(query.toString());
    }

    public boolean insert_data(String table_name, ContentValues table_data) {
        // Data insert hone se theek pehle sanitizer chalega
        sanitizeData(table_data);

        SQLiteDatabase sqlite_obj = this.getWritableDatabase();
        long flag = sqlite_obj.insert(table_name, null, table_data);

        return flag != -1;
    }

    public boolean update_data(String table_name, ContentValues table_data, String coloum_name, String coloum_value) {
        // Data update hone se theek pehle sanitizer chalega
        sanitizeData(table_data);

        SQLiteDatabase sqlite_obj = this.getWritableDatabase();
        String whereClause = coloum_name + " = ?";
        String[] whereArgs = new String[]{coloum_value};
        int rowsAffected = sqlite_obj.update(table_name, table_data, whereClause, whereArgs);
        return rowsAffected > 0;
    }

    public Cursor get_data(String table_name) {
        SQLiteDatabase sqlite_obj = this.getReadableDatabase();
        return sqlite_obj.rawQuery("SELECT * FROM " + table_name, null);
    }

    public List<String> searchTableNamesByPrefix(String prefix) {
        List<String> tableNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE ?";

        String[] selectionArgs = {prefix + "%"};

        try {
            cursor = db.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex("name");

                do {
                    if (nameIndex != -1) {
                        tableNames.add(cursor.getString(nameIndex));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("SQLiteDB", "Error searching table names by prefix: " + prefix, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tableNames;
    }

    public boolean delete_row(String table_name, String coloum_name, String coloum_value) {
        SQLiteDatabase sqlite_obj = this.getWritableDatabase();
        String whereClause = coloum_name + " = ?";
        String[] whereArgs = new String[]{coloum_value};
        int rowsAffected = sqlite_obj.delete(table_name, whereClause, whereArgs);
        return rowsAffected > 0;
    }

    public void delete_table(String table_name) {
        SQLiteDatabase sqlite_obj = this.getWritableDatabase();
        sqlite_obj.execSQL("DROP TABLE IF EXISTS " + table_name);
    }

    public boolean is_table_exists(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});

            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // --- UPDATED SEARCH QUERIES (FOR "YYYY-MM-DD" FORMAT) ---

    public Cursor searchByYear(String fullDate, String table_name, String date_coloum_name) {
        SQLiteDatabase db = this.getReadableDatabase();

        String year = LocalDate.parse(fullDate).format(DateTimeFormatter.ofPattern("yyyy"));

        String query = "SELECT * FROM " + table_name
                + " WHERE strftime('%Y', " + date_coloum_name + ") = ?";

        String[] selectionArgs = {year};
        return db.rawQuery(query, selectionArgs);
    }

    public Cursor searchByMonth(String fullDate, String table_name, String date_coloum_name) {
        SQLiteDatabase db = this.getReadableDatabase();

        String yearMonth = LocalDate.parse(fullDate).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        String query = "SELECT * FROM " + table_name
                + " WHERE strftime('%Y-%m', " + date_coloum_name + ") = ?";

        String[] selectionArgs = {yearMonth};
        return db.rawQuery(query, selectionArgs);
    }

    public Cursor searchByMonthInAnyYear(String fullDate, String table_name, String date_coloum_name) {
        SQLiteDatabase db = this.getReadableDatabase();

        String month = LocalDate.parse(fullDate).format(DateTimeFormatter.ofPattern("MM"));

        String query = "SELECT * FROM " + table_name
                + " WHERE strftime('%m', " + date_coloum_name + ") = ?";

        String[] selectionArgs = {month};
        return db.rawQuery(query, selectionArgs);
    }

    public Cursor searchByDate(String fullDate, String table_name, String date_coloum_name) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + table_name + " WHERE " + date_coloum_name + " = ?";

        String[] selectionArgs = {fullDate};
        return db.rawQuery(query, selectionArgs);
    }

    public Cursor searchInColumn(String tableName, String columnName, String columnValue) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = columnName + " = ?";
        String[] selectionArgs = {columnValue};
        return db.query(tableName, null, selection, selectionArgs, null, null, null);
    }

    public Cursor searchInMultiColumns(String tableName, String[][] searchParameters) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (searchParameters == null || searchParameters.length == 0) {
            return null;
        }

        StringBuilder selectionBuilder = new StringBuilder();
        ArrayList<String> selectionArgsList = new ArrayList<>();

        for (int i = 0; i < searchParameters.length; i++) {
            String[] param = searchParameters[i];

            if (param == null || param.length < 2) {
                continue;
            }

            String columnName = param[0];
            String columnValue = param[1];

            if (i > 0) {
                selectionBuilder.append(" AND ");
            }

            selectionBuilder.append(columnName).append(" = ?");

            selectionArgsList.add(columnValue);
        }

        if (selectionArgsList.isEmpty()) {
            return null;
        }

        String selection = selectionBuilder.toString();
        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        return db.query(
                tableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    public Cursor searchInMultiColumnsMultiCondition(String tableName, String[][] searchParameters) {

        SQLiteDatabase db = this.getReadableDatabase();

        if (searchParameters == null || searchParameters.length == 0) {
            return null;
        }

        StringBuilder selectionBuilder = new StringBuilder();
        ArrayList<String> selectionArgsList = new ArrayList<>();

        final String[] numericOperators = {">", "<", ">=", "<=", "!=", "<>"};

        for (int i = 0; i < searchParameters.length; i++) {
            String[] param = searchParameters[i];

            if (param == null || param.length < 3) {
                continue;
            }

            String columnName = param[0];
            String operator = param[1].trim();
            String columnValue = param[2];

            String logicalJoiner = (param.length > 3) ? param[3].trim().toUpperCase() : "";
            String grouping = (param.length > 4) ? param[4].trim() : "";

            if (i > 0) {
                String prevJoiner = (searchParameters[i - 1].length > 3) ? searchParameters[i - 1][3].trim().toUpperCase() : "AND";

                if (prevJoiner.equals("AND") || prevJoiner.equals("OR")) {
                    selectionBuilder.append(" ").append(prevJoiner).append(" ");
                } else {
                    selectionBuilder.append(" AND ");
                }
            }

            if (grouping.startsWith("(")) {
                selectionBuilder.append("(");
            }

            boolean requiresCasting = Arrays.asList(numericOperators).contains(operator);

            if (requiresCasting) {
                selectionBuilder.append("CAST(").append(columnName).append(" AS REAL)").append(" ").append(operator).append(" ?");
            } else {
                selectionBuilder.append(columnName).append(" ").append(operator).append(" ?");
            }

            selectionArgsList.add(columnValue);

            if (grouping.endsWith(")")) {
                selectionBuilder.append(")");
            }
        }

        if (selectionArgsList.isEmpty()) {
            return null;
        }

        String selection = selectionBuilder.toString();
        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        return db.query(
                tableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }
}