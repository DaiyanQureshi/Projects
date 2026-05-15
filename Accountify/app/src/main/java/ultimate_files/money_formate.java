package ultimate_files;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
public class money_formate {
    Locale indiaLocale;
    NumberFormat formatter;

    public money_formate(){
        indiaLocale = new Locale("en", "IN");

         formatter = NumberFormat.getCurrencyInstance(indiaLocale);

        try {
            DecimalFormat decimalFormatter = (DecimalFormat) formatter;
            DecimalFormatSymbols symbols = decimalFormatter.getDecimalFormatSymbols();

            symbols.setCurrencySymbol("₹ ");
            decimalFormatter.setDecimalFormatSymbols(symbols);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String set(String money) {
        // 1. Agar string literally "null" word hai ya khali hai, toh pehle hi rok do
        if (money == null || money.trim().isEmpty() || money.trim().equalsIgnoreCase("null")) {
            return formatter.format(BigDecimal.ZERO);
        }

        // 2. Shield (Try-Catch) jo tere crash ko rokega
        try {
            return formatter.format(new BigDecimal(plain_string(money)));
        } catch (NumberFormatException e) {
            // Agar DB se kachra data aayega, toh app crash nahi hogi, bas ₹ 0.00 dikhayegi
            android.util.Log.e("DATA_ERROR", "Galat number parse hua: [" + money + "]");
            return formatter.format(BigDecimal.ZERO);
        }
    }
    public String plain_string(String money){
        if ((money == null || money.trim().isEmpty())) {
            return "";
        }

        return money.trim()
                .replace(",", "")
                .replaceAll("\\s", "") // Use this instead of .replace(" ", "")
                        .replace("₹", "");
    }

}
