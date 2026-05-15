package ultimate_files;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class date_formate_checker {
    private static final DateTimeFormatter FORMATTER_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATTER_DASH = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String getSmartFormattedDate(String dateString) {
        // Step 1: Defensive check for empty or null data
        if (dateString == null || dateString.trim().isEmpty()) {
            return "N/A";
        }

        // Tumhari database me date kis format me save ho rahi hai (DD-MM-YYYY)
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date date = inputFormat.parse(dateString);

            // Input date ka year nikalna
            Calendar targetCalendar = Calendar.getInstance();
            targetCalendar.setTime(date);
            int targetYear = targetCalendar.get(Calendar.YEAR);

            // Aaj ka (Current) year nikalna
            Calendar currentCalendar = Calendar.getInstance();
            int currentYear = currentCalendar.get(Calendar.YEAR);

            // Step 2: Year compare karke format decide karna
            if (targetYear == currentYear) {
                // Same year: Only Date and Month (e.g., 15 Feb)
                SimpleDateFormat currentYearFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
                return currentYearFormat.format(date);
            } else {
                // Past/Future year: Date, Month and short Year (e.g., 15 Feb '25)
                // Note: SQL aur Java me single quote print karne ke liye do baar '' likhna padta hai
                SimpleDateFormat otherYearFormat = new SimpleDateFormat("dd MMM ''yy", Locale.getDefault());
                return otherYearFormat.format(date);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            // Agar koi galat format ki date aa gayi toh app crash nahi hogi
            // seedha wahi galat date string return kar dega taaki UI chalta rahe
            return dateString;
        }
    }
    public String ckeck_date(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        LocalDate localDate = null;

        try {
            localDate = LocalDate.parse(dateString, FORMATTER_SLASH);
        }
        catch (DateTimeParseException e1) {
            try {
                localDate = LocalDate.parse(dateString, FORMATTER_DASH);
            }
            catch (DateTimeParseException e2) {
                return null;
            }
        }

        if (localDate != null) {

            return localDate.format(FORMATTER_YYYY_MM_DD);
        }

        return null;
    }

}
