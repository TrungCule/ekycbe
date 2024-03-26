package vnpay.com.vn.service.dto;

public class HeaderConfig {

    private String name;
    private Integer row;
    private Integer col;
    private Integer rowspan;
    private Integer colspan;

    public HeaderConfig(String name, Integer row, Integer col, Integer rowspan, Integer colspan) {
        this.name = name;
        this.row = row;
        this.col = col;
        this.rowspan = rowspan;
        this.colspan = colspan;
    }

    public HeaderConfig() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getCol() {
        return col;
    }

    public void setCol(Integer col) {
        this.col = col;
    }

    public Integer getRowspan() {
        return rowspan;
    }

    public void setRowspan(Integer rowspan) {
        this.rowspan = rowspan;
    }

    public Integer getColspan() {
        return colspan;
    }

    public void setColspan(Integer colspan) {
        this.colspan = colspan;
    }
}
