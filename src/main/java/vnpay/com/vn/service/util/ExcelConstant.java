package vnpay.com.vn.service.util;

import vnpay.com.vn.service.dto.FieldConfig;
import vnpay.com.vn.service.dto.HeaderConfig;

import java.util.Arrays;
import java.util.List;

public interface ExcelConstant {

    interface AccountingObject {
        List<vnpay.com.vn.service.dto.HeaderConfig> HeaderConfig = List.of(
            new HeaderConfig("Tên đăng nhập", 10, 0, 1, 1),
            new HeaderConfig("Họ", 10, 1, 1, 1),
            new HeaderConfig("Tên", 10, 2, 1, 1),
            new HeaderConfig("Email", 10, 3, 1, 1),
            new HeaderConfig("Ngày sinh", 10, 4, 1, 1),
            new HeaderConfig("Địa chỉ", 10, 5, 1, 1),
            new HeaderConfig("Số điện thoai", 10, 6, 1, 1),
            new HeaderConfig("Trạng thái", 10, 7, 1, 1)
        );

        List<vnpay.com.vn.service.dto.FieldConfig> FieldConfig = List.of(
            new FieldConfig("login", "String", "text-left"),
            new FieldConfig("firstName", "String", "text-left"),
            new FieldConfig("lastName", "String", "text-left"),
            new FieldConfig("email", "String", "text-left"),
            new FieldConfig("dateOfBirth", "String", "text-center"),
            new FieldConfig("address", "String", "text-left"),
            new FieldConfig("phoneNumber", "String", "text-left"),
            new FieldConfig("activated", "String", "text-center")
        );
    }

    interface AccountingObjectOld {
        String NAME = "User";
        List<String> HEADER = Arrays.asList(
            "Tên đăng nhập",
            "Họ",
            "Tên",
            "Email",
            "Ngày sinh",
            "Địa chỉ",
            "Số điện thoai",
            "Trạng thái"
        );
        List<String> FIELD = Arrays.asList(
            "login",
            "first_name",
            "last_name",
            "email",
            "date_of_birth",
            "address",
            "phone_number",
            "activated"
        );
    }
}
