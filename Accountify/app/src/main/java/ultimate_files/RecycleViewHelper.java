package ultimate_files;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.daiyan.accountify.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecycleViewHelper extends RecyclerView.Adapter<RecycleViewHelper.ViewHolder> {

    private Context context;
    private Cursor cursor;
    private int layoutResId;
    private Map<Integer, String> mapping;
    private BindListener bindListener;
    private OnItemClickListener itemClickListener;

    // NEW: Tracker Reference Add Kiya Hai
    private SelectionTracker<Long> tracker;

    public static class StockIds {
        public int getImage() { return R.id.dp; }
        public int getName() { return R.id.Name; }
        public int getQuantity() { return R.id.total_quantity; }
        public int getRate() { return R.id.avg_rate; }
        public int getTotalPrice() { return R.id.payable_or_receivable; }
        public int getPriceLabel() { return R.id.payable_or_receivable_text; }
    }
    public static StockIds stockList() { return new StockIds(); }

    public static class CustomerIds {
        public int getImage() { return R.id.dp; }
        public int getName() { return R.id.Name; }
        public int getPersonType() { return R.id.person_type; }
        public int getDueDate() { return R.id.due_date; }
        public int getAmount() { return R.id.payable_or_receivable; }
        public int getStatusLabel() { return R.id.payable_or_receivable_text; }
    }
    public static CustomerIds customerList() { return new CustomerIds(); }

    public static class TradeIds {
        public int getNumbering() { return R.id.numbering; }
        public int getTitle() { return R.id.title; }
        public int getSubtotalText() { return R.id.subtotal_text; }
        public int getSubtotalValue() { return R.id.subtotal; }
        public int getDiscountPercent() { return R.id.discount_in_percent; }
        public int getDiscountRupee() { return R.id.discount_in_rupee; }
        public int getTaxPercent() { return R.id.tax_in_percent; }
        public int getTaxRupee() { return R.id.tax_in_rupee; }
        public int getTotalAmount() { return R.id.total_amount; }
        public int getReceivedAmount() { return R.id.received_amount; }
        public int getCheckbox() { return R.id.received_checkbox; }
    }
    public static TradeIds tradeList() { return new TradeIds(); }


    public static class Logic {
        public static BindListener Customer(boolean showIdentity) {
            return (view, cursor, colName, id) -> {
                int colIndex = cursor.getColumnIndex(colName);
                if (colIndex == -1) return false;

                String valStr = cursor.getString(colIndex);
                CustomerIds ids = customerList();

                if (id == ids.getAmount() || id == ids.getStatusLabel()) {
                    double val = 0;
                    try {
                        if (valStr != null && !valStr.trim().isEmpty()) val = Double.parseDouble(valStr);
                    } catch (NumberFormatException e) { val = 0; }

                    int color;
                    String statusText;

                    if (val > 0) {
                        color = Color.parseColor("#4CAF50");
                        statusText = "Receivable";
                    } else if (val < 0) {
                        color = Color.parseColor("#DB5555");
                        statusText = "Payable";
                    } else {
                        color = Color.parseColor("#7F8081");
                        statusText = "No Due";
                    }

                    if (id == ids.getAmount()) {
                        ((TextView)view).setText("₹ " + Math.abs(val));
                    } else {
                        ((TextView)view).setText(statusText);
                    }
                    ((TextView)view).setTextColor(color);
                    return true;
                }

                if (id == ids.getPersonType()) {
                    if (showIdentity) {
                        String typeText = "";
                        if (valStr != null) {
                            if (valStr.toLowerCase().contains("sale")) typeText = "(Customer)";
                            else if (valStr.toLowerCase().contains("purchase")) typeText = "(Supplier)";
                        }
                        ((TextView)view).setText(typeText);
                        ((TextView)view).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView)view).setVisibility(View.GONE);
                    }
                    return true;
                }

                if (id == ids.getDueDate()) {
                    // 1. Check karo ki string null, empty ya sirf spaces to nahi hai
                    if (valStr == null || valStr.trim().isEmpty()) {
                        // Agar number nahi hai toh view ko puri tarah hata do
                        view.setVisibility(View.GONE);
                    } else {
                        // Agar number hai toh usko set karo
                        ((TextView)view).setText(valStr.trim());
                        // 2. View ko wapas visible karna zaroori hai
                        view.setVisibility(View.VISIBLE);
                    }
                    return true;
                }

                // YEH LINE MISSING THI. Ise function ke bilkul end mein lambda close hone se pehle rakhna hai.
                return false;
            };
        }
    }

    public static class Builder {
        private Context context;
        private Cursor cursor;
        private int layoutResId;
        private Map<Integer, String> mapping = new HashMap<>();
        private BindListener bindListener;
        private OnItemClickListener itemClickListener;

        public Builder(Context context) { this.context = context; }

        public Builder setData(int layoutResId, Cursor cursor) {
            this.layoutResId = layoutResId;
            this.cursor = cursor;
            return this;
        }
        public Builder map(int viewId, String dbColumnName) {
            this.mapping.put(viewId, dbColumnName);
            return this;
        }
        public Builder setBindListener(BindListener listener) {
            this.bindListener = listener;
            return this;
        }
        public Builder setOnItemClick(OnItemClickListener listener) {
            this.itemClickListener = listener;
            return this;
        }
        public RecycleViewHelper build() { return new RecycleViewHelper(this); }
    }

    public interface BindListener {
        boolean onBind(View view, Cursor cursor, String columnName, int viewId);
    }
    public interface OnItemClickListener {
        void onClick(Cursor cursor, int position);
    }

    private RecycleViewHelper(Builder builder) {
        this.context = builder.context;
        this.cursor = builder.cursor;
        this.layoutResId = builder.layoutResId;
        this.mapping = builder.mapping;
        this.bindListener = builder.bindListener;
        this.itemClickListener = builder.itemClickListener;

        // NEW: Tracker ko kaam karne ke liye IDs stable honi zaroori hain
        setHasStableIds(true);
    }

    // NEW: Setter for tracker
    public void setTracker(SelectionTracker<Long> tracker) {
        this.tracker = tracker;
    }

    // NEW: Stable IDs generator
    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor == null || !cursor.moveToPosition(position)) return;

        // NEW & FIXED: Integrated, transparent selection logic
        // Tracker State Update UI logic
        // Tracker State Update UI logic
        if (tracker != null) {
            // SelectionTracker automatically check karega aur view ko 'Activated' state dega
            holder.itemView.setActivated(tracker.isSelected((long) position));
        }

        for (Map.Entry<Integer, String> entry : mapping.entrySet()) {
            int viewId = entry.getKey();
            String columnName = entry.getValue();
            View view = holder.itemView.findViewById(viewId);

            if (view == null) continue;
            boolean isHandled = false;

            if (bindListener != null) {
                isHandled = bindListener.onBind(view, cursor, columnName, viewId);
            }
            if (!isHandled) {
                bindDefault(view, cursor, columnName);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                cursor.moveToPosition(holder.getAdapterPosition());
                itemClickListener.onClick(cursor, holder.getAdapterPosition());
            }
        });
    }

    private void bindDefault(View view, Cursor cursor, String columnName) {
        int idx = cursor.getColumnIndex(columnName);
        if (idx == -1) return;

        String data = cursor.getString(idx);

        if (view instanceof TextView) {
            ((TextView) view).setText(data != null ? data : "");
        } else if (view instanceof ImageView) {
            try {
                if (data != null && !data.isEmpty()) ((ImageView) view).setImageResource(Integer.parseInt(data));
            } catch (Exception e) { }
        }
    }

    private void bindDefault(View view, List<ContentValues> values, int position, String columnName) {
        if (values == null || position < 0 || position >= values.size()) return;
        ContentValues row = values.get(position);
        if (!row.containsKey(columnName)) return;
        String data = row.getAsString(columnName);

        if (view instanceof TextView) {
            ((TextView) view).setText(data != null ? data : "");
        } else if (view instanceof ImageView) {
            try {
                if (data != null && !data.isEmpty()) ((ImageView) view).setImageResource(Integer.parseInt(data));
            } catch (Exception e) { }
        }
    }

    @Override
    public int getItemCount() { return (cursor == null) ? 0 : cursor.getCount(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) { super(itemView); }

        // NEW: Tracker ko view details return karne ka logic
        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<Long>() {
                @Override
                public int getPosition() {
                    return getAdapterPosition();
                }

                @Nullable
                @Override
                public Long getSelectionKey() {
                    return (long) getAdapterPosition();
                }
            };
        }
    }

    // NEW: Generic Lookup Class jo kisi bhi nayi file mein sidha use ho jayegi
    public static class GenericItemDetailsLookup extends ItemDetailsLookup<Long> {
        private final RecyclerView recyclerView;

        public GenericItemDetailsLookup(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Nullable
        @Override
        public ItemDetails<Long> getItemDetails(@NonNull MotionEvent event) {
            View view = recyclerView.findChildViewUnder(event.getX(), event.getY());
            if (view != null) {
                RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
                if (holder instanceof RecycleViewHelper.ViewHolder) {
                    return ((RecycleViewHelper.ViewHolder) holder).getItemDetails();
                }
            }
            return null;
        }
    }
}