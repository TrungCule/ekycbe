package vnpay.com.vn.service.Utils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Utils {

    public static String getPeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.getDayOfMonth() == 1 && fromDate.getYear() == toDate.getYear()) {
            // Năm
            if (fromDate.getMonthValue() == 1 && toDate.getDayOfMonth() == 31 && toDate.getMonthValue() == 12) return String.format(
                "Năm %1$s",
                fromDate.getYear()
            );
            // Quý
            // List<int> lstMonth = new int[] {1,4,7,10};
            if (Arrays.asList(1, 4, 7, 10).contains(fromDate.getMonthValue())) {
                List<String> chars = Arrays.asList("I", "II", "III", "IV");
                LocalDate test = fromDate.plusMonths(3).plusDays(-1).atTime(0, 0, 0, 0).toLocalDate();
                if (
                    fromDate.plusMonths(3).plusDays(-1).atTime(0, 0, 0, 0).toLocalDate().equals(toDate.atTime(0, 0, 0, 0).toLocalDate())
                ) return String.format("Quý %1$s năm %2$s", chars.get(((fromDate.getMonthValue() - 1) / 3)), fromDate.getYear());
            }
            // tháng
            if (
                fromDate.plusMonths(1).plusDays(-1).atTime(0, 0, 0, 0).toLocalDate().equals(toDate.atTime(0, 0, 0, 0).toLocalDate())
            ) return String.format("Tháng %1$s năm %2$s", fromDate.getMonthValue(), fromDate.getYear());
        }
        return String.format("Từ ngày %1$s đến ngày %2$s", convertDate(fromDate), convertDate(toDate));
    }

    // Convert PostedDate, Date
    public static String convertDate(LocalDate date) {
        if (date == null) {
            return null;
        } else {
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }


}
