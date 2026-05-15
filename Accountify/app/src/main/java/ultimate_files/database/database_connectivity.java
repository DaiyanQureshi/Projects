package ultimate_files.database;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;

import ultimate_files.AppExecutor;

public class database_connectivity extends sqlite_database{
    private final String[] column_of_product_table = {
            "person_name","phone_number","trade_type","supplier_id","item_name","quantity","rate","unit","trade_date",
            "subtotal","discount_in_percent","discount_in_rupee","gst_in_percent","gst_in_rupee",
            "total_amount","payment_type","payment_date","payment_amount","payment_done","pending",
            "image_path","document_path","description",
//            "in_stock_quantity", "pending_receive_amount", "pending_sent_amount", "is_delete"
    };

    private String[] column_of_parties_table = {
            "trade_type","person_name",
            "phone_number","gst_number",
            "email_id","user_business_address",
            "pending","opening_pending",
            "total_payment_amount"
    };
    private String[] column_of_trade_table = {
            "item_name","in_stock_quantity","sold_quantity","in_stock_last_rate","sold_last_rate",
            "in_stock_avg_rate","sold_avg_rate","in_stock_unit","sold_unit","in_stock_total_amount", "sold_total_amount"
    };
    private final String[] column_profile = {
            "user_business_name", "user_name","user_phone_number", "user_emailid",
            "user_gstin", "user_business_address",
    };
    private final String[] column_of_dashboard_table = {
            "filter","start_date","end_date","profit","sales","payable","receivable","purchase","in_stock_value","sold_value"
    };// filter today,weekly,monthly,yearly,custom->only 5 rows possible


    private final String[] column_of_recent_activity_table = {
            "entity_name", "activity_type", "amount", "timestamp", "icon_type"
    };
    private final String[] column_of_bills_table = {
            "trade_id","trade_type","person_name","bill_type","due_date","total_amount"
    };
    // Isko apne purane column_of_report_table se REPLACE kar de
// Isko apne purane column_of_report_table se REPLACE kar de
    private final String[] column_of_report_table = {
            "invoice_no", "invoice_date", "due_date", "po_number",
            "business_name", "business_phone", "business_email",
            "shipping_name", "shipping_address", "shipping_contact",
            "invoice_subtotal", "invoice_discount", "invoice_tax", "invoice_final_total",
            "invoice_discount_percent", "invoice_tax_percent",
            "person_name","phone_number","trade_type","item_name","quantity","rate","unit",
            "discount_in_percent","discount_in_rupee","gst_in_percent","gst_in_rupee",
            "total_amount","description", "trade_date" // YAHAN BHI TRADE_DATE ADD HUA HAI
    };

    private final String[] column_of_invoice_table = {
            "invoice_no", "invoice_date", "due_date", "po_number",
            "business_name", "business_phone", "business_email",
            "shipping_name", "shipping_address", "shipping_contact",
            "invoice_subtotal", "invoice_discount", "invoice_tax", "invoice_final_total",
            "invoice_discount_percent", "invoice_tax_percent",
            "person_name","phone_number","trade_type","item_name","quantity","rate","unit",
            "discount_in_percent","discount_in_rupee","gst_in_percent","gst_in_rupee",
            "total_amount","description", "trade_date"
    };
    private final String[] column_of_payment_logs_table = {
            "product_id", "person_name", "amount", "payment_type", "payment_date"
    };

    // Table name define kar
    private static final String table_name_invoice = "invoice_data";


    public void create_table_invoice(){
        create_tables(table_name_invoice, column_of_invoice_table);
    }

    public String getTable_name_invoice() {
        return table_name_invoice;
    }


    // THE ARCHITECT FIX: Dynamic numbering for both Invoice and Report
    public String getNextDocumentNumber(boolean isReport) {
        String targetTable = isReport ? table_name_report : table_name_invoice;
        String prefix = isReport ? "REP" : "INV";

        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();
        // Distinct invoice_no ka count nikalna hai taaki series sahi chale
        android.database.Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT invoice_no) FROM " + targetTable, null);

        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }

        // REP-001 ya INV-001 format return karega
        return String.format(java.util.Locale.getDefault(), "%s-%03d", prefix, count + 1);
    }

    private static final String
            table_name_profile = "profile",
            table_name_report = "report",
            table_name_bills = "bills",
            table_name_recent_activity = "recent_activity",
            table_name_product = "product",
            table_name_parties = "parties",
            table_name_trade = "trade",
            table_name_dashboard = "dashboard",
            table_name_payment_logs = "payment_logs";


    public String[] getColumn(String table_name){
        switch (table_name){

            case table_name_profile:
                return column_profile;

            case table_name_report:
                return column_of_report_table;

            case table_name_bills:
                return column_of_bills_table;

            case table_name_recent_activity:
                return column_of_recent_activity_table;

            case table_name_product:
                return column_of_product_table;

            case table_name_parties:
                return column_of_parties_table;

            case table_name_trade:
                return column_of_trade_table;

            case table_name_dashboard:
                return column_of_dashboard_table;

            case table_name_payment_logs:
                return column_of_payment_logs_table;

            default:
                return null;

        }
    }






    public String getTable_name_payment_logs() {return table_name_payment_logs;}
        public String getTable_name_profile() {
        return table_name_profile;
    }

    public String getTable_name_report() {
        return table_name_report;
    }

    public String getTable_name_bills() {
        return table_name_bills;
    }
    public String getTable_name_recent_activity() {
        return table_name_recent_activity;
    }
    public String getTable_name_product() {
        return table_name_product;
    }
    public String getTable_name_parties() {
        return table_name_parties;
    }
    public String getTable_name_trade() {
        return table_name_trade;
    }
    public String getTable_name_dashboard() {
        return table_name_dashboard;
    }


    public database_connectivity(Context context) {
        super(context);
        setup_full_database();
    }

    public void setup_full_database() {
        // 1. Saari Tables banayein
        create_table_profile();
        create_table_report();
        create_table_bills();
        create_table_recent_activity();
        create_table_product();
        create_table_parties();
        create_table_trade();
        create_table_dashboard();
        initialize_dashboard_rows();
        create_table_invoice();
        create_table_payment_logs();

        // 2. Triggers Activate Karein (Product table banne ke baad)
        activate_triggers();
        activate_dashboard_triggers();
        activate_recent_activity_triggers();
        activate_payment_log_triggers();
    }



    private void activate_triggers() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TRIGGER IF EXISTS sync_after_insert");
        db.execSQL("DROP TRIGGER IF EXISTS sync_after_update");
        db.execSQL("DROP TRIGGER IF EXISTS sync_after_delete");

        // =========================================================================
        // 1. INSERT TRIGGER (PERPETUAL MOVING AVERAGE)
        // =========================================================================
        String triggerInsert = "CREATE TRIGGER sync_after_insert AFTER INSERT ON " + getTable_name_product() + " " +
                "BEGIN " +
                "  INSERT INTO " + getTable_name_parties() + " (person_name, trade_type, phone_number, pending, total_payment_amount) " +
                "  SELECT NEW.person_name, NEW.trade_type, NEW.phone_number, 0, 0 " +
                "  WHERE NOT EXISTS (SELECT 1 FROM " + getTable_name_parties() + " WHERE person_name = NEW.person_name); " +

                "  UPDATE " + getTable_name_parties() + " SET " +
                "  trade_type = NEW.trade_type, " +
                "  phone_number = CASE WHEN NEW.phone_number != '' THEN NEW.phone_number ELSE phone_number END, " +
                "  pending = CAST(COALESCE(pending, 0) AS REAL) + CASE WHEN NEW.trade_type = 'sale' THEN CAST(NEW.pending AS REAL) ELSE -CAST(NEW.pending AS REAL) END, " +
                "  total_payment_amount = CAST(COALESCE(total_payment_amount, 0) AS REAL) + CASE WHEN NEW.trade_type = 'sale' THEN CAST(NEW.payment_amount AS REAL) ELSE -CAST(NEW.payment_amount AS REAL) END " +
                "  WHERE person_name = NEW.person_name; " +

                "  INSERT INTO " + getTable_name_trade() + " (item_name, in_stock_quantity, sold_quantity, in_stock_avg_rate, sold_avg_rate, in_stock_total_amount, sold_total_amount) " +
                "  SELECT NEW.item_name, 0, 0, 0, 0, 0, 0 " +
                "  WHERE NOT EXISTS (SELECT 1 FROM " + getTable_name_trade() + " WHERE item_name = NEW.item_name); " +

                "  UPDATE " + getTable_name_trade() + " SET " +
                "  in_stock_avg_rate = CASE " +
                "      WHEN NEW.trade_type = 'purchase' AND (CAST(in_stock_quantity AS REAL) + CAST(NEW.quantity AS REAL)) > 0 THEN " +
                "          (CAST(in_stock_total_amount AS REAL) + CAST(NEW.total_amount AS REAL)) / (CAST(in_stock_quantity AS REAL) + CAST(NEW.quantity AS REAL)) " +
                "      ELSE in_stock_avg_rate END, " +

                // FIX 1: sold_avg_rate ab total_amount par calculate hoga, in_stock_avg_rate par nahi
                "  sold_avg_rate = CASE " +
                "      WHEN NEW.trade_type = 'sale' AND (CAST(sold_quantity AS REAL) + CAST(NEW.quantity AS REAL)) > 0 THEN " +
                "          (CAST(sold_total_amount AS REAL) + CAST(NEW.total_amount AS REAL)) / (CAST(sold_quantity AS REAL) + CAST(NEW.quantity AS REAL)) " +
                "      ELSE sold_avg_rate END, " +

                "  in_stock_total_amount = CASE " +
                "      WHEN NEW.trade_type = 'purchase' THEN CAST(in_stock_total_amount AS REAL) + CAST(NEW.total_amount AS REAL) " +
                "      ELSE MAX(0, CAST(in_stock_total_amount AS REAL) - (CAST(NEW.quantity AS REAL) * CAST(in_stock_avg_rate AS REAL))) END, " +

                // FIX 2: sold_total_amount me direct actual sale value add ki jayegi
                "  sold_total_amount = CASE " +
                "      WHEN NEW.trade_type = 'sale' THEN CAST(sold_total_amount AS REAL) + CAST(NEW.total_amount AS REAL) " +
                "      ELSE sold_total_amount END, " +

                "  in_stock_quantity = MAX(0, CAST(in_stock_quantity AS REAL) + CASE WHEN NEW.trade_type = 'purchase' THEN CAST(NEW.quantity AS REAL) ELSE -CAST(NEW.quantity AS REAL) END), " +
                "  sold_quantity = CAST(sold_quantity AS REAL) + CASE WHEN NEW.trade_type = 'sale' THEN CAST(NEW.quantity AS REAL) ELSE 0 END " +
                "  WHERE item_name = NEW.item_name; " +

                "  UPDATE " + getTable_name_trade() + " SET in_stock_total_amount = 0 WHERE in_stock_quantity <= 0 AND item_name = NEW.item_name; " +
                "END;";
        // =========================================================================
        // 2. DELETE TRIGGER
        // =========================================================================
        String triggerDelete = "CREATE TRIGGER sync_after_delete AFTER DELETE ON " + getTable_name_product() + " " +
                "BEGIN " +
                "  UPDATE " + getTable_name_parties() + " SET " +
                "  pending = CAST(COALESCE(pending, 0) AS REAL) - CASE WHEN OLD.trade_type = 'sale' THEN CAST(OLD.pending AS REAL) ELSE -CAST(OLD.pending AS REAL) END, " +
                "  total_payment_amount = CAST(COALESCE(total_payment_amount, 0) AS REAL) - CASE WHEN OLD.trade_type = 'sale' THEN CAST(OLD.payment_amount AS REAL) ELSE -CAST(OLD.payment_amount AS REAL) END " +
                "  WHERE person_name = OLD.person_name; " +

                "  UPDATE " + getTable_name_trade() + " SET " +
                "  in_stock_avg_rate = CASE " +
                "      WHEN OLD.trade_type = 'purchase' AND (CAST(in_stock_quantity AS REAL) - CAST(OLD.quantity AS REAL)) > 0 THEN " +
                "          (CAST(in_stock_total_amount AS REAL) - CAST(OLD.total_amount AS REAL)) / (CAST(in_stock_quantity AS REAL) - CAST(OLD.quantity AS REAL)) " +
                "      ELSE in_stock_avg_rate END, " +
                "  sold_avg_rate = CASE " +
                "      WHEN OLD.trade_type = 'sale' AND (CAST(sold_quantity AS REAL) - CAST(OLD.quantity AS REAL)) > 0 THEN " +
                "          (CAST(sold_total_amount AS REAL) - (CAST(OLD.quantity AS REAL) * CAST(in_stock_avg_rate AS REAL))) / (CAST(sold_quantity AS REAL) - CAST(OLD.quantity AS REAL)) " +
                "      ELSE sold_avg_rate END, " +
                "  in_stock_total_amount = CASE " +
                "      WHEN OLD.trade_type = 'purchase' THEN MAX(0, CAST(in_stock_total_amount AS REAL) - CAST(OLD.total_amount AS REAL)) " +
                "      ELSE CAST(in_stock_total_amount AS REAL) + (CAST(OLD.quantity AS REAL) * CAST(in_stock_avg_rate AS REAL)) END, " +
                "  sold_total_amount = CASE " +
                "      WHEN OLD.trade_type = 'sale' THEN MAX(0, CAST(sold_total_amount AS REAL) - (CAST(OLD.quantity AS REAL) * CAST(in_stock_avg_rate AS REAL))) " +
                "      ELSE sold_total_amount END, " +
                "  in_stock_quantity = MAX(0, CAST(in_stock_quantity AS REAL) - CASE WHEN OLD.trade_type = 'purchase' THEN CAST(OLD.quantity AS REAL) ELSE -CAST(OLD.quantity AS REAL) END), " +
                "  sold_quantity = MAX(0, CAST(sold_quantity AS REAL) - CASE WHEN OLD.trade_type = 'sale' THEN CAST(OLD.quantity AS REAL) ELSE 0 END) " +
                "  WHERE item_name = OLD.item_name; " +

                "  UPDATE " + getTable_name_trade() + " SET in_stock_total_amount = 0 WHERE in_stock_quantity <= 0 AND item_name = OLD.item_name; " +
                "END;";

        // =========================================================================
        // 3. UPDATE TRIGGER (FIXED THE NEGATIVE -100 ISSUE)
        // =========================================================================
        String triggerUpdate = "CREATE TRIGGER sync_after_update AFTER UPDATE ON " + getTable_name_product() + " " +
                "BEGIN " +
                // PARTIES REVERT & APPLY
                "  UPDATE " + getTable_name_parties() + " SET " +
                "  pending = CAST(COALESCE(pending, 0) AS REAL) - CASE WHEN OLD.trade_type = 'sale' THEN CAST(OLD.pending AS REAL) ELSE -CAST(OLD.pending AS REAL) END " +
                "                                               + CASE WHEN NEW.trade_type = 'sale' THEN CAST(NEW.pending AS REAL) ELSE -CAST(NEW.pending AS REAL) END, " +
                "  total_payment_amount = CAST(COALESCE(total_payment_amount, 0) AS REAL) - CASE WHEN OLD.trade_type = 'sale' THEN CAST(OLD.payment_amount AS REAL) ELSE -CAST(OLD.payment_amount AS REAL) END " +
                "                                                                         + CASE WHEN NEW.trade_type = 'sale' THEN CAST(NEW.payment_amount AS REAL) ELSE -CAST(NEW.payment_amount AS REAL) END " +
                "  WHERE person_name = NEW.person_name; " +

                // TRADE REVERT OLD
                "  UPDATE " + getTable_name_trade() + " SET " +
                "  in_stock_quantity = MAX(0, CAST(in_stock_quantity AS REAL) - CASE WHEN OLD.trade_type = 'purchase' THEN CAST(OLD.quantity AS REAL) ELSE -CAST(OLD.quantity AS REAL) END), " +
                "  in_stock_total_amount = MAX(0, CAST(in_stock_total_amount AS REAL) - CASE WHEN OLD.trade_type = 'purchase' THEN CAST(OLD.total_amount AS REAL) ELSE -(CAST(OLD.quantity AS REAL) * CAST(in_stock_avg_rate AS REAL)) END), " +
                "  sold_quantity = MAX(0, CAST(sold_quantity AS REAL) - CASE WHEN OLD.trade_type = 'sale' THEN CAST(OLD.quantity AS REAL) ELSE 0 END), " +
                "  sold_total_amount = MAX(0, CAST(sold_total_amount AS REAL) - CASE WHEN OLD.trade_type = 'sale' THEN CAST(OLD.total_amount AS REAL) ELSE 0 END) " +
                "  WHERE item_name = OLD.item_name; " +

                "  UPDATE " + getTable_name_trade() + " SET in_stock_total_amount = 0 WHERE in_stock_quantity <= 0 AND item_name = OLD.item_name; " +

                "  UPDATE " + getTable_name_trade() + " SET " +
                "  in_stock_avg_rate = CASE WHEN in_stock_quantity > 0 THEN in_stock_total_amount / in_stock_quantity ELSE 0 END, " +
                "  sold_avg_rate = CASE WHEN sold_quantity > 0 THEN sold_total_amount / sold_quantity ELSE 0 END " +
                "  WHERE item_name = OLD.item_name; " +

                // TRADE APPLY NEW
                "  INSERT INTO " + getTable_name_trade() + " (item_name, in_stock_quantity, sold_quantity, in_stock_avg_rate, sold_avg_rate, in_stock_total_amount, sold_total_amount) " +
                "  SELECT NEW.item_name, 0, 0, 0, 0, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM " + getTable_name_trade() + " WHERE item_name = NEW.item_name); " +

                "  UPDATE " + getTable_name_trade() + " SET " +
                "  in_stock_quantity = CAST(in_stock_quantity AS REAL) + CASE WHEN NEW.trade_type = 'purchase' THEN CAST(NEW.quantity AS REAL) ELSE -CAST(NEW.quantity AS REAL) END, " +
                "  in_stock_total_amount = CAST(in_stock_total_amount AS REAL) + CASE WHEN NEW.trade_type = 'purchase' THEN CAST(NEW.total_amount AS REAL) ELSE -(CAST(NEW.quantity AS REAL) * CAST(in_stock_avg_rate AS REAL)) END, " +
                "  sold_quantity = CAST(sold_quantity AS REAL) + CASE WHEN NEW.trade_type = 'sale' THEN CAST(NEW.quantity AS REAL) ELSE 0 END, " +
                "  sold_total_amount = CAST(sold_total_amount AS REAL) + CASE WHEN NEW.trade_type = 'sale' THEN CAST(NEW.total_amount AS REAL) ELSE 0 END " +
                "  WHERE item_name = NEW.item_name; " +

                "  UPDATE " + getTable_name_trade() + " SET in_stock_total_amount = 0 WHERE in_stock_quantity <= 0 AND item_name = NEW.item_name; " +

                "  UPDATE " + getTable_name_trade() + " SET " +
                "  in_stock_avg_rate = CASE WHEN in_stock_quantity > 0 THEN in_stock_total_amount / in_stock_quantity ELSE 0 END, " +
                "  sold_avg_rate = CASE WHEN sold_quantity > 0 THEN sold_total_amount / sold_quantity ELSE 0 END " +
                "  WHERE item_name = NEW.item_name; " +
                "END;";

        try {
            db.execSQL(triggerInsert);
            db.execSQL(triggerDelete);
            db.execSQL(triggerUpdate);
        } catch (Exception e) {
            android.util.Log.e("DB_ERROR", "Trade calculation triggers failed: " + e.getMessage());
        }
    }

    private void activate_dashboard_triggers() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TRIGGER IF EXISTS sync_dashboard_insert");
        db.execSQL("DROP TRIGGER IF EXISTS sync_dashboard_update");
        db.execSQL("DROP TRIGGER IF EXISTS sync_dashboard_delete");

        // =========================================================================
        // OLD DASHBOARD LOGIC RESTORED (INCREMENTAL SYNC)
        // =========================================================================
        String triggerInsert = "CREATE TRIGGER sync_dashboard_insert AFTER INSERT ON " + getTable_name_product() + " " +
                "BEGIN " +
                "  UPDATE " + getTable_name_dashboard() + " SET " +
                "  sales = COALESCE(sales, 0) + CASE WHEN NEW.trade_type = 'sale' THEN NEW.total_amount ELSE 0 END, " +
                "  purchase = COALESCE(purchase, 0) + CASE WHEN NEW.trade_type = 'purchase' THEN NEW.total_amount ELSE 0 END, " +
                "  payable = COALESCE(payable, 0) + CASE WHEN NEW.trade_type = 'purchase' AND NEW.pending > 0 THEN NEW.pending ELSE 0 END, " +
                "  receivable = COALESCE(receivable, 0) + CASE WHEN NEW.trade_type = 'sale' AND NEW.pending > 0 THEN NEW.pending ELSE 0 END " +
                "  WHERE " +
                "     (filter = 'Today' AND NEW.trade_date = start_date) OR " +
                "     (filter = 'Weekly' AND NEW.trade_date BETWEEN start_date AND end_date) OR " +
                "     (filter = 'Monthly' AND NEW.trade_date BETWEEN start_date AND end_date) OR " +
                "     (filter = 'Yearly' AND NEW.trade_date BETWEEN start_date AND end_date) OR " +
                "     (filter = 'All time'); " +
                "  " +
                "  UPDATE " + getTable_name_dashboard() + " SET " +
                "  in_stock_value = (SELECT SUM(COALESCE(in_stock_total_amount, 0)) FROM " + getTable_name_trade() + "), " +
                "  sold_value = (SELECT SUM(COALESCE(sold_total_amount, 0)) FROM " + getTable_name_trade() + "), " +
                "  profit = COALESCE(sales, 0) - COALESCE(purchase, 0); " +
                "END;";

        String triggerUpdate = "CREATE TRIGGER sync_dashboard_update AFTER UPDATE ON " + getTable_name_product() + " " +
                "BEGIN " +
                "  UPDATE " + getTable_name_dashboard() + " SET " +
                "  sales = COALESCE(sales, 0) - CASE WHEN OLD.trade_type = 'sale' THEN OLD.total_amount ELSE 0 END + CASE WHEN NEW.trade_type = 'sale' THEN NEW.total_amount ELSE 0 END, " +
                "  purchase = COALESCE(purchase, 0) - CASE WHEN OLD.trade_type = 'purchase' THEN OLD.total_amount ELSE 0 END + CASE WHEN NEW.trade_type = 'purchase' THEN NEW.total_amount ELSE 0 END, " +
                "  payable = COALESCE(payable, 0) - CASE WHEN OLD.trade_type = 'purchase' AND OLD.pending > 0 THEN OLD.pending ELSE 0 END " +
                "                                 + CASE WHEN NEW.trade_type = 'purchase' AND NEW.pending > 0 THEN NEW.pending ELSE 0 END, " +
                "  receivable = COALESCE(receivable, 0) - CASE WHEN OLD.trade_type = 'sale' AND OLD.pending > 0 THEN OLD.pending ELSE 0 END " +
                "                                       + CASE WHEN NEW.trade_type = 'sale' AND NEW.pending > 0 THEN NEW.pending ELSE 0 END " +
                "  WHERE " +
                "     (filter = 'Today' AND NEW.trade_date = start_date) OR " +
                "     (filter = 'Weekly' AND NEW.trade_date BETWEEN start_date AND end_date) OR " +
                "     (filter = 'Monthly' AND NEW.trade_date BETWEEN start_date AND end_date) OR " +
                "     (filter = 'Yearly' AND NEW.trade_date BETWEEN start_date AND end_date) OR " +
                "     (filter = 'All time'); " +
                "  " +
                "  UPDATE " + getTable_name_dashboard() + " SET " +
                "  profit = COALESCE(sales, 0) - COALESCE(purchase, 0); " +
                "END;";

        String triggerDelete = "CREATE TRIGGER sync_dashboard_delete AFTER DELETE ON " + getTable_name_product() + " " +
                "BEGIN " +
                "  UPDATE " + getTable_name_dashboard() + " SET " +
                "  sales = COALESCE(sales, 0) - CASE WHEN OLD.trade_type = 'sale' THEN OLD.total_amount ELSE 0 END, " +
                "  purchase = COALESCE(purchase, 0) - CASE WHEN OLD.trade_type = 'purchase' THEN OLD.total_amount ELSE 0 END, " +
                "  payable = COALESCE(payable, 0) - CASE WHEN OLD.trade_type = 'purchase' AND OLD.pending > 0 THEN OLD.pending ELSE 0 END, " +
                "  receivable = COALESCE(receivable, 0) - CASE WHEN OLD.trade_type = 'sale' AND OLD.pending > 0 THEN OLD.pending ELSE 0 END " +
                "  WHERE " +
                "     (filter = 'Today' AND OLD.trade_date = start_date) OR " +
                "     (filter = 'Weekly' AND OLD.trade_date BETWEEN start_date AND end_date) OR " +
                "     (filter = 'Monthly' AND OLD.trade_date BETWEEN start_date AND end_date) OR " +
                "     (filter = 'Yearly' AND OLD.trade_date BETWEEN start_date AND end_date) OR " +
                "     (filter = 'All time'); " +
                "  " +
                "  UPDATE " + getTable_name_dashboard() + " SET " +
                "  in_stock_value = (SELECT SUM(COALESCE(in_stock_total_amount, 0)) FROM " + getTable_name_trade() + "), " +
                "  sold_value = (SELECT SUM(COALESCE(sold_total_amount, 0)) FROM " + getTable_name_trade() + "), " +
                "  profit = COALESCE(sales, 0) - COALESCE(purchase, 0); " +
                "END;";

        try {
            db.execSQL(triggerInsert);
            db.execSQL(triggerUpdate);
            db.execSQL(triggerDelete);
        } catch (Exception e) {
            android.util.Log.e("DASH_ERROR", "Triggers Failed: " + e.getMessage());
        }
    }

    private void activate_recent_activity_triggers() {

        SQLiteDatabase db = this.getWritableDatabase();



        db.execSQL("DROP TRIGGER IF EXISTS log_product_activity");

        db.execSQL("DROP TRIGGER IF EXISTS log_invoice_activity");

        db.execSQL("DROP TRIGGER IF EXISTS log_report_activity");



        // Fix: Used COALESCE to prevent NULL concatenation crashes

        String trigProduct = "CREATE TRIGGER log_product_activity AFTER INSERT ON product BEGIN " +

                "INSERT INTO recent_activity (entity_name, activity_type, amount, timestamp, icon_type) " +

                "VALUES (COALESCE(NEW.item_name, 'Item') || ' (' || COALESCE(NEW.person_name, 'Unknown') || ')', " +

                "CASE WHEN NEW.trade_type='sale' THEN 'Sold' ELSE 'Bought' END, " +

                "COALESCE(NEW.total_amount, '0'), datetime('now','localtime'), 'product'); " +

                "DELETE FROM recent_activity WHERE id NOT IN (SELECT id FROM recent_activity ORDER BY id DESC LIMIT 10); END;";



        String trigInvoice = "CREATE TRIGGER log_invoice_activity AFTER INSERT ON invoice_data BEGIN " +

                "INSERT INTO recent_activity (entity_name, activity_type, amount, timestamp, icon_type) " +

                "VALUES ('Invoice #' || COALESCE(NEW.invoice_no, 'NA'), 'Invoice Generated (' || COALESCE(NEW.person_name, 'Unknown') || ')', " +

                "COALESCE(NEW.invoice_final_total, '0'), datetime('now','localtime'), 'invoice'); " +

                "DELETE FROM recent_activity WHERE id NOT IN (SELECT id FROM recent_activity ORDER BY id DESC LIMIT 10); END;";



        String trigReport = "CREATE TRIGGER log_activity_report_insert AFTER INSERT ON report BEGIN " +

                "INSERT INTO recent_activity (entity_name, activity_type, amount, timestamp, icon_type) " +

                "VALUES ('Report #' || COALESCE(NEW.invoice_no, 'NA'), 'Report Generated', " +

                "COALESCE(NEW.invoice_final_total, '0'), datetime('now','localtime'), 'report'); " +

                "DELETE FROM recent_activity WHERE id NOT IN (SELECT id FROM recent_activity ORDER BY id DESC LIMIT 10); END;";



        try {

            db.execSQL(trigProduct);

            db.execSQL(trigInvoice);

            db.execSQL(trigReport);

        } catch (Exception e) {

            Log.e("ACTIVITY_ERROR", "Recent Activity Triggers Failed: " + e.getMessage());

        }

    }



    private void activate_payment_log_triggers() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TRIGGER IF EXISTS sync_payment_log_insert");
        db.execSQL("DROP TRIGGER IF EXISTS sync_payment_log_update");
        db.execSQL("DROP TRIGGER IF EXISTS sync_payment_log_delete");

        // 1. INSERT TRIGGER: Jab pehli baar naya bill banta hai aur kuch payment aati hai
        String trigPayInsert = "CREATE TRIGGER sync_payment_log_insert AFTER INSERT ON " + getTable_name_product() + " " +
                "WHEN CAST(CASE WHEN NEW.payment_amount = '' THEN '0' ELSE COALESCE(NEW.payment_amount, '0') END AS REAL) > 0 BEGIN " +
                "  INSERT INTO " + getTable_name_payment_logs() + " (product_id, person_name, amount, payment_type, payment_date) " +
                "  VALUES (NEW.id, COALESCE(NEW.person_name, 'Unknown'), " +
                "  NEW.payment_amount, " +
                "  CASE WHEN NEW.trade_type = 'sale' THEN 'Payment Received' ELSE 'Payment Sent' END, " +
                "  datetime('now','localtime')); " +
                "END;";

        // 2. UPDATE TRIGGER: Asli Magic Yahan Hai!
        // Ye sirf tab trigger hoga jab payment_amount change hoga. Aur ye sirf DIFFERENCE amount ka naya log banayega.
        String trigPayUpdate = "CREATE TRIGGER sync_payment_log_update AFTER UPDATE ON " + getTable_name_product() + " " +
                "WHEN CAST(CASE WHEN NEW.payment_amount = '' THEN '0' ELSE COALESCE(NEW.payment_amount, '0') END AS REAL) != " +
                "     CAST(CASE WHEN OLD.payment_amount = '' THEN '0' ELSE COALESCE(OLD.payment_amount, '0') END AS REAL) " +
                "BEGIN " +
                "  INSERT INTO " + getTable_name_payment_logs() + " (product_id, person_name, amount, payment_type, payment_date) " +
                "  VALUES (NEW.id, COALESCE(NEW.person_name, 'Unknown'), " +
                "  CAST(CASE WHEN NEW.payment_amount = '' THEN '0' ELSE COALESCE(NEW.payment_amount, '0') END AS REAL) - " +
                "  CAST(CASE WHEN OLD.payment_amount = '' THEN '0' ELSE COALESCE(OLD.payment_amount, '0') END AS REAL), " +
                "  CASE WHEN NEW.trade_type = 'sale' THEN 'Payment Received' ELSE 'Payment Sent' END, " +
                "  datetime('now','localtime')); " +
                "END;";

        // 3. DELETE TRIGGER: Agar galti se poora bill hi uda diya toh uski saari history clear karni hogi
        String trigPayDelete = "CREATE TRIGGER sync_payment_log_delete AFTER DELETE ON " + getTable_name_product() + " " +
                "BEGIN " +
                "  DELETE FROM " + getTable_name_payment_logs() + " WHERE product_id = OLD.id; " +
                "END;";

        try {
            db.execSQL(trigPayInsert);
            db.execSQL(trigPayUpdate);
            db.execSQL(trigPayDelete);
        } catch (Exception e) {
            android.util.Log.e("PAYMENT_LOG_ERROR", "Payment Triggers Failed: " + e.getMessage());
        }
    }
    public void initialize_dashboard_rows() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Java mein dates calculate karo (Format YYYY-MM-DD hona strict zaroori hai)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        java.util.Calendar cal = java.util.Calendar.getInstance();

        String today = sdf.format(cal.getTime());

        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        String weekStart = sdf.format(cal.getTime());
        cal.add(java.util.Calendar.DATE, 6);
        String weekEnd = sdf.format(cal.getTime());

        cal.setTime(new java.util.Date());
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        String monthStart = sdf.format(cal.getTime());

        cal.setTime(new java.util.Date());
        cal.set(java.util.Calendar.DAY_OF_YEAR, 1);
        String yearStart = sdf.format(cal.getTime());

        // Ab dates ke sath rows insert karo
        String[][] filters = {
                {"Today", today, today},
                {"Weekly", weekStart, weekEnd},
                {"Monthly", monthStart, today},
                {"Yearly", yearStart, today},
                {"All time", "2000-01-01", today}, // Shuruat se aaj tak
                {"Custom", null, null} // Custom khali rahega jab tak user na bhare
        };

        for (String[] f : filters) {
            String filterName = f[0];
            String startDate = f[1];
            String endDate = f[2];

            String startDateVal = (startDate == null) ? "NULL" : "'" + startDate + "'";
            String endDateVal = (endDate == null) ? "NULL" : "'" + endDate + "'";

            db.execSQL("INSERT INTO " + getTable_name_dashboard() +
                    " (filter, start_date, end_date, profit, sales, payable, receivable, purchase, in_stock_value, sold_value) " +
                    " SELECT '" + filterName + "', " + startDateVal + ", " + endDateVal + ", '0', '0', '0', '0', '0', '0', '0' " +
                    " WHERE NOT EXISTS (SELECT 1 FROM " + getTable_name_dashboard() + " WHERE filter = '" + filterName + "')");

            // Agar pehle se exist karta hai, toh bas dates update kardo taaki hamesha fresh rahe
            if (startDate != null) {
                db.execSQL("UPDATE " + getTable_name_dashboard() +
                        " SET start_date = '" + startDate + "', end_date = '" + endDate + "' " +
                        " WHERE filter = '" + filterName + "'");
            }
        }
    }

    public void updateCustomDashboardValues(String startDate, String endDate) {
        // startDate aur endDate format "YYYY-MM-DD" mein hona chahiye

        AppExecutor.getInstance().diskIO().execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();

            // Product table se sum nikalne ki query
            String query = "SELECT " +
                    "SUM(CASE WHEN trade_type = 'sale' THEN total_amount ELSE 0 END) as total_sales, " +
                    "SUM(CASE WHEN trade_type = 'purchase' THEN total_amount ELSE 0 END) as total_purchase, " +
                    "SUM(CASE WHEN trade_type = 'sale' AND pending > 0 THEN pending ELSE 0 END) as total_receivable, " +
                    "SUM(CASE WHEN trade_type = 'purchase' AND pending > 0 THEN pending ELSE 0 END) as total_payable " +
                    "FROM " + getTable_name_product() + " " +
                    "WHERE trade_date BETWEEN ? AND ?";

            Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});

            if (cursor != null && cursor.moveToFirst()) {
                double sales = cursor.getDouble(cursor.getColumnIndexOrThrow("total_sales"));
                double purchase = cursor.getDouble(cursor.getColumnIndexOrThrow("total_purchase"));
                double receivable = cursor.getDouble(cursor.getColumnIndexOrThrow("total_receivable"));
                double payable = cursor.getDouble(cursor.getColumnIndexOrThrow("total_payable"));
                double profit = sales - purchase;

                // In-stock aur Sold value global hoti hai, date specific nahi, isliye trade table se nikalenge
                Cursor stockCursor = db.rawQuery("SELECT SUM(in_stock_total_amount) as in_stock, SUM(sold_total_amount) as sold FROM " + getTable_name_trade(), null);
                double inStock = 0, soldVal = 0;
                if (stockCursor != null && stockCursor.moveToFirst()) {
                    inStock = stockCursor.getDouble(0);
                    soldVal = stockCursor.getDouble(1);
                    stockCursor.close();
                }

                // Custom row ko update karna
                ContentValues cv = new ContentValues();
                cv.put("start_date", startDate);
                cv.put("end_date", endDate);
                cv.put("sales", sales);
                cv.put("purchase", purchase);
                cv.put("receivable", receivable);
                cv.put("payable", payable);
                cv.put("profit", profit);
                cv.put("in_stock_value", inStock);
                cv.put("sold_value", soldVal);

                db.update(getTable_name_dashboard(), cv, "filter = ?", new String[]{"Custom"});
            }

            if (cursor != null) {
                cursor.close();
            }
        });
    }
    public void create_table_profile(){
        create_tables(getTable_name_profile(), getColumn(getTable_name_profile()));
    }
    public void create_table_report(){
        create_tables(getTable_name_report(), getColumn(getTable_name_report()));
    }
    public void create_table_bills(){
        create_tables(getTable_name_bills(), getColumn(getTable_name_bills()));
    }
    public void create_table_recent_activity(){
        create_tables(getTable_name_recent_activity(), getColumn(getTable_name_recent_activity()));
    }
    public void create_table_product(){
        create_tables(getTable_name_product(), getColumn(getTable_name_product()));
    }
    public void create_table_parties(){
        create_tables(getTable_name_parties(), getColumn(getTable_name_parties()));
    }
    public void create_table_trade() {
        create_tables(getTable_name_trade(), getColumn(getTable_name_trade()));
    }
    public void create_table_dashboard() {
        create_tables(getTable_name_dashboard(), getColumn(getTable_name_dashboard()));
    }
    public void create_table_payment_logs(){
        create_tables(table_name_payment_logs, column_of_payment_logs_table);
    }






}

