package vnpay.com.vn.service.util;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.dhatim.fastexcel.BorderStyle;
import org.dhatim.fastexcel.Worksheet;
import vnpay.com.vn.service.Utils.Utils;
import vnpay.com.vn.service.dto.FieldConfig;
import vnpay.com.vn.service.dto.HeaderConfig;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.apache.poi.ss.usermodel.FontFormatting.U_SINGLE;

public class FastExcelUtils {
    private static final String LEFT = "text-left";
    private static final String RIGHT = "text-right";
    private static final String CENTER = "text-center";

    public static void SetHeaderExcel(
        SXSSFSheet sheet,
        Integer totalColumn,
        List<CellStyle> cellStyleList,
        String titleReport
    ) {
        CellStyle styleHeaderB = cellStyleList.get(0);
        CellStyle styleHeaderI = cellStyleList.get(4);
        CellStyle styleHeaderC = cellStyleList.get(7);
        int totalColumnR = totalColumn;
        totalColumn = Math.max(totalColumn, 5);
        int center = (int) Math.floor(((double) totalColumn) * 2 / 3);

        Row rowCompany = sheet.createRow(0);
        Cell cell0Company = rowCompany.createCell(0);
        cell0Company.setCellValue("VNPAY - Công ty Cổ phần Giải pháp Thanh toán Việt Nam".toUpperCase()
        );
        cell0Company.setCellStyle(styleHeaderB);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, center - 2));
        Row rowAddress = sheet.createRow(1);
        Cell cell0Address = rowAddress.createCell(0);
        cell0Address.setCellValue("Trụ sở chính. Tầng 8, số 22 Láng Hạ, phường Láng Hạ, quận Đống Đa, Hà Nội");
        cell0Address.setCellStyle(styleHeaderI);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, center - 2));
        Row rowTaxCode = sheet.createRow(2);
        Cell cell0TaxCode = rowTaxCode.createCell(0);
        cell0TaxCode.setCellStyle(styleHeaderI);
        cell0TaxCode.setCellValue("0123456789");
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, center - 2));
        CellUtil.setAlignment(cell0Company, org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        CellUtil.setAlignment(cell0Address, org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        CellUtil.setAlignment(cell0TaxCode, org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);

        //Set Tiêu đề
        if (titleReport != null) {
            Row rowTitle = sheet.createRow(5);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellStyle(styleHeaderC);
            cellTitle.setCellValue(titleReport);
            CellUtil.setAlignment(cellTitle, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            CellUtil.setVerticalAlignment(cellTitle, org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, totalColumnR - 1));
        }
    }

    public static List<CellStyle> getCellStyleList(SXSSFWorkbook workbook) {
        List<CellStyle> cellStyleList = new ArrayList<>();

        Font fontDf = workbook.createFont();
        fontDf.setFontName("Times New Roman");
        fontDf.setBold(false);
        fontDf.setFontHeightInPoints((short) 11);

        Font fontDf1 = workbook.createFont();
        fontDf1.setFontName("Times New Roman");
        fontDf1.setBold(false);
        fontDf1.setFontHeightInPoints((short) 6);

        Font fontB = workbook.createFont();
        fontB.setFontName("Times New Roman");
        fontB.setBold(true);
        fontB.setFontHeightInPoints((short) 11);

        Font fontC = workbook.createFont();
        fontC.setFontName("Times New Roman");
        fontC.setBold(true);
        fontC.setFontHeightInPoints((short) 14);

        Font fontBI = workbook.createFont();
        fontBI.setFontName("Times New Roman");
        fontBI.setBold(true);
        fontBI.setItalic(true);
        fontBI.setFontHeightInPoints((short) 11);

        Font fontH = workbook.createFont();
        fontH.setFontName("Times New Roman");
        fontH.setBold(true);
        fontH.setItalic(false);
        fontH.setUnderline(U_SINGLE);
        fontH.setFontHeightInPoints((short) 11);

        Font fontI = workbook.createFont();
        fontI.setFontName("Times New Roman");
        fontI.setBold(false);
        fontI.setItalic(true);
        fontI.setFontHeightInPoints((short) 11);

        Font fontRed = workbook.createFont();
        fontRed.setFontName("Times New Roman");
        fontRed.setColor(Font.COLOR_RED);
        fontRed.setFontHeightInPoints((short) 11);

        // 0. Đậm k border
        CellStyle styleHeaderB = workbook.createCellStyle();
        styleHeaderB.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleHeaderB.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderB.setWrapText(true);
        styleHeaderB.setFont(fontB);
        cellStyleList.add(styleHeaderB);

        // 1. Đậm có border
        CellStyle styleHeaderBorderB = workbook.createCellStyle();
        styleHeaderBorderB.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleHeaderBorderB.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderBorderB.setWrapText(true);
        styleHeaderBorderB.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderB.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderB.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderB.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderB.setFont(fontB);
        cellStyleList.add(styleHeaderBorderB);

        // 2. Thường k border
        CellStyle styleHeaderN = workbook.createCellStyle();
        styleHeaderN.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleHeaderN.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderN.setWrapText(true);
        styleHeaderN.setFont(fontDf);
        cellStyleList.add(styleHeaderN);

        // 3. Thường có border
        CellStyle styleHeaderBorderN = workbook.createCellStyle();
        styleHeaderBorderN.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleHeaderBorderN.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderBorderN.setWrapText(true);
        styleHeaderBorderN.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderN.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderN.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderN.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderN.setFont(fontDf);
        cellStyleList.add(styleHeaderBorderN);

        // 4. Nghiêng k border
        CellStyle styleHeaderI = workbook.createCellStyle();
        styleHeaderI.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleHeaderI.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderI.setWrapText(true);
        styleHeaderI.setFont(fontI);
        cellStyleList.add(styleHeaderI);

//        // 5. Số thường có border
//        DataFormat format = workbook.createDataFormat();
//        CellStyle styleNumber = workbook.createCellStyle();
//        styleNumber.cloneStyleFrom(cellStyleList.get(3));
//        styleNumber.setDataFormat(format.getFormat(Utils.getFormatNumber(Constants.SystemOption.DDSo_TienVND, userDTO)));
//        cellStyleList.add(styleNumber);
//
//        // 6. Số đậm có border
//        CellStyle styleNumberBold = workbook.createCellStyle();
//        styleNumberBold.cloneStyleFrom(cellStyleList.get(1));
//        styleNumberBold.setDataFormat(format.getFormat(Utils.getFormatNumber(Constants.SystemOption.DDSo_TienVND, userDTO)));
//        cellStyleList.add(styleNumberBold);

        // 7. Đậm k border title
        CellStyle styleHeaderTitle = workbook.createCellStyle();
        styleHeaderTitle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleHeaderTitle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderTitle.setWrapText(true);
        styleHeaderTitle.setFont(fontC);
        cellStyleList.add(styleHeaderTitle);

        // 8. Đậm nghiêng k có border
        CellStyle styleTextBI = workbook.createCellStyle();
        styleTextBI.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleTextBI.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleTextBI.setWrapText(true);
        styleTextBI.setFont(fontBI);
        cellStyleList.add(styleTextBI);

        // 9. đậm nghiêng có border
        CellStyle styleTextBorderBI = workbook.createCellStyle();
        styleTextBorderBI.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleTextBorderBI.cloneStyleFrom(cellStyleList.get(1));
        styleTextBorderBI.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleTextBorderBI.setWrapText(true);
        styleTextBorderBI.setFont(fontBI);
        cellStyleList.add(styleTextBorderBI);

        // 10. Thường có border dưới
        CellStyle styleHeaderBorderBottom = workbook.createCellStyle();
        styleHeaderBorderBottom.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleHeaderBorderBottom.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderBorderBottom.setWrapText(true);
        styleHeaderBorderBottom.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderBottom.setFont(fontDf);
        cellStyleList.add(styleHeaderBorderBottom);

        // 11. Thường nghiêng k có border trên
        CellStyle styleNoneBorderTop = workbook.createCellStyle();
        styleNoneBorderTop.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleNoneBorderTop.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleNoneBorderTop.setWrapText(true);
        styleNoneBorderTop.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleNoneBorderTop.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleNoneBorderTop.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleNoneBorderTop.setFont(fontI);
        cellStyleList.add(styleNoneBorderTop);

        // 12. Thường k có border phải
        CellStyle styleNoneBorderRight = workbook.createCellStyle();
        styleNoneBorderRight.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleNoneBorderRight.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleNoneBorderRight.setWrapText(true);
        styleNoneBorderRight.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleNoneBorderRight.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleNoneBorderRight.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleNoneBorderRight.setFont(fontDf);
        cellStyleList.add(styleNoneBorderRight);

        // 13. Đậm nghiêng k có border trái
        CellStyle styleNoneBorderLeft = workbook.createCellStyle();
        styleNoneBorderLeft.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleNoneBorderLeft.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleNoneBorderLeft.setWrapText(true);
        styleNoneBorderLeft.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleNoneBorderLeft.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleNoneBorderLeft.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleNoneBorderLeft.setFont(fontBI);
        cellStyleList.add(styleNoneBorderLeft);

        // 14. Thường không border, chữ nhỏ góc trên
        CellStyle styleHeaderBorder1 = workbook.createCellStyle();
        styleHeaderBorder1.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleHeaderBorder1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderBorder1.setWrapText(true);
        styleHeaderBorder1.setFont(fontDf1);
        cellStyleList.add(styleHeaderBorder1);

        //15.Đậm in hoa có gạch chân không border
        CellStyle styleTextH = workbook.createCellStyle();
        styleTextH.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleTextH.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleTextH.setWrapText(true);
        styleTextH.setFont(fontH);
        cellStyleList.add(styleTextH);

        // 16. Chữ đỏ
        CellStyle styleColorRed = workbook.createCellStyle();
        styleColorRed.cloneStyleFrom(cellStyleList.get(5));
        styleColorRed.setFont(fontRed);
        cellStyleList.add(styleColorRed);

        // 17. Thường có border trên
        CellStyle styleHeaderBorderTop = workbook.createCellStyle();
        styleHeaderBorderTop.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styleHeaderBorderTop.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderBorderTop.setWrapText(true);
        styleHeaderBorderTop.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        styleHeaderBorderTop.setFont(fontDf);
        cellStyleList.add(styleHeaderBorderTop);

        return cellStyleList;
    }

    public static void genBodyDynamicReportExcel(
        SXSSFSheet sheet,
        List<FieldConfig> fieldConfig,
        List<HeaderConfig> headerConfig,
        List data,
        int startRow,
        List<CellStyle> cellStyleList
    ) {
        for (HeaderConfig config : headerConfig) {
            Row rowHeader = sheet.getRow(config.getRow());
            if (rowHeader == null) {
                rowHeader = sheet.createRow(config.getRow());
            }
            Cell cell = rowHeader.createCell(config.getCol());
            cell.setCellValue(config.getName());
            if (config.getColspan() != 1 || config.getRowspan() != 1) {
                CellRangeAddress cellRangeAddress = new CellRangeAddress(
                    config.getRow(),
                    config.getRow() + config.getRowspan() - 1,
                    config.getCol(),
                    config.getCol() + config.getColspan() - 1
                );
                sheet.addMergedRegion(cellRangeAddress);
                RegionUtil.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN, cellRangeAddress, sheet);
                RegionUtil.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN, cellRangeAddress, sheet);
            }
            cell.setCellStyle(cellStyleList.get(1));
            CellUtil.setAlignment(cell, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            CellUtil.setVerticalAlignment(cell, org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            if (config.getRowspan() > 1) {
                startRow = config.getRow() + config.getRowspan();
            } else {
                startRow = config.getRow() + 1;
            }
        }

        int dataSize = data.size();
        for (int i = 0; i < dataSize; i++) {
            Row row = sheet.createRow(i + startRow);
            int fieldSize = fieldConfig.size();
            for (int j = 0; j < fieldSize; j++) {
                Cell cell = row.createCell(j);
                Object fieldValue;
                if (fieldConfig.get(j).getName().equals("note")) {
                    fieldValue = "";
                } else {
                    fieldValue = ReflectionUtils.getFieldValue(data.get(i), fieldConfig.get(j).getName());
                }
                cell.setCellStyle(cellStyleList.get(3));
                String formattedData;
                if (fieldValue != null) {
                    switch (fieldConfig.get(j).getAlign()) {
                        case LEFT:
                            CellUtil.setAlignment(cell, org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
                            break;
                        case RIGHT:
                            CellUtil.setAlignment(cell, org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);
                            break;
                        case CENTER:
                            CellUtil.setAlignment(cell, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
                            break;
                    }
                    if (fieldValue instanceof String) {
                        formattedData = fieldValue.toString();
                        cell.setCellValue(formattedData);
                    } else if (fieldValue instanceof BigDecimal) {
                        if (((BigDecimal) fieldValue).doubleValue() != 0) {
                            cell.setCellValue(((BigDecimal) fieldValue).doubleValue());
                            cell.setCellStyle(cellStyleList.get(5));
                        }
                    } else if (fieldValue instanceof LocalDate) {
                        formattedData = ((LocalDate) fieldValue).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        cell.setCellValue(formattedData);
                    } else if (fieldValue instanceof LocalDateTime) {
                        formattedData = ((LocalDateTime) fieldValue).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                        cell.setCellValue(formattedData);
                    } else if (fieldValue instanceof Integer) {
                        if (((Integer) fieldValue).doubleValue() != 0) {
                            cell.setCellValue(((Integer) fieldValue).doubleValue());
                        } else {
                            cell.setCellValue("");
                        }
                    }
                }
            }
        }
    }

    public static void SetFooterExcel(
        SXSSFSheet sheet,
        Integer startRow,
        Integer totalColumn,
        List<CellStyle> cellStyleList
    ) {
        Row rowFooter1 = sheet.createRow(startRow);
        Cell cellSoTrang = rowFooter1.createCell(0);
        cellSoTrang.setCellValue("- Dữ liệu này có ... trang, đánh số từ trang 01 đến trang số ...");
        cellSoTrang.setCellStyle(cellStyleList.get(2));
        CellUtil.setAlignment(cellSoTrang, org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 0, (totalColumn - 1)));
        startRow++;

        Row rowFooter2 = sheet.createRow(startRow);
        Cell cellNgayMoSo = rowFooter2.createCell(0);
        cellNgayMoSo.setCellValue("- Ngày xem: ..........................................");
        CellUtil.setAlignment(cellNgayMoSo, org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        cellNgayMoSo.setCellStyle(cellStyleList.get(2));
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 0, (totalColumn - 1)));
        startRow++;

        Row rowFooter3 = sheet.createRow(startRow);
        Cell cellRowNgayThang = rowFooter3.createCell(totalColumn - 3);
        cellRowNgayThang.setCellValue("Ngày.....tháng.....năm........");
        cellRowNgayThang.setCellStyle(cellStyleList.get(4));
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, (totalColumn - 3), (totalColumn - 1)));
        CellUtil.setAlignment(cellRowNgayThang, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        CellUtil.setVerticalAlignment(cellRowNgayThang, org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);

        Row rowTitleSigned = sheet.createRow(startRow + 2);
        Row rowSubTitleSigned = sheet.createRow(startRow + 3);
        Row row = sheet.createRow(startRow + 6);
        int vtNguoiLap = 1;
        Cell cellTitle = rowTitleSigned.createCell(vtNguoiLap);
        cellTitle.setCellValue("Người lập");
        cellTitle.setCellStyle(cellStyleList.get(0));
        CellUtil.setAlignment(cellTitle, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        CellUtil.setVerticalAlignment(cellTitle, org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);

        Cell cellSubTitle = rowSubTitleSigned.createCell(vtNguoiLap);
        cellSubTitle.setCellValue("(Ký, họ tên)");
        cellSubTitle.setCellStyle(cellStyleList.get(4));
        CellUtil.setAlignment(cellSubTitle, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        CellUtil.setVerticalAlignment(cellSubTitle, org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);

        Cell cell = row.createCell(vtNguoiLap);
        cell.setCellStyle(cellStyleList.get(0));
        CellUtil.setAlignment(cell, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        CellUtil.setVerticalAlignment(cell, org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        cell.setCellValue("");

        int vtKeToanTruong = (totalColumn - 3) > 1 ? (totalColumn - 3) : 3;
        Cell cellTitle1 = rowTitleSigned.createCell(vtKeToanTruong);
        cellTitle1.setCellValue("Đại diện");
        cellTitle1.setCellStyle(cellStyleList.get(0));
        CellUtil.setAlignment(cellTitle1, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        CellUtil.setVerticalAlignment(cellTitle1, org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        sheet.addMergedRegion(new CellRangeAddress(startRow + 2, startRow + 2, (totalColumn - 3), (totalColumn - 1)));

        Cell cellSubTitle1 = rowSubTitleSigned.createCell(vtKeToanTruong);
        cellSubTitle1.setCellValue("(Ký, họ tên)");
        cellSubTitle1.setCellStyle(cellStyleList.get(4));
        CellUtil.setAlignment(cellSubTitle1, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        CellUtil.setVerticalAlignment(cellSubTitle1, org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        sheet.addMergedRegion(new CellRangeAddress(startRow + 3, startRow + 3, (totalColumn - 3), (totalColumn - 1)));

        Cell cell1 = row.createCell(vtKeToanTruong);
        cell1.setCellStyle(cellStyleList.get(0));
        CellUtil.setAlignment(cell1, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        CellUtil.setVerticalAlignment(cell1, org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        cell1.setCellValue("");
        sheet.addMergedRegion(new CellRangeAddress(startRow + 6, startRow + 6, (totalColumn - 3), (totalColumn - 1)));
    }

}
