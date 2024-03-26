package vnpay.com.vn.service.dto;

public class FieldConfig {

    private String name;
    private String typeObject;
    private String align;

    public FieldConfig(String name, String typeObject, String align) {
        this.name = name;
        this.typeObject = typeObject;
        this.align = align;
    }

    public FieldConfig() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeObject() {
        return typeObject;
    }

    public void setTypeObject(String typeObject) {
        this.typeObject = typeObject;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }
}
