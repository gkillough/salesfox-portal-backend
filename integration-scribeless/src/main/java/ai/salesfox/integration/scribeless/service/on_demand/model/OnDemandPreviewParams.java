package ai.salesfox.integration.scribeless.service.on_demand.model;

import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class OnDemandPreviewParams {
    private Boolean testing;
    private Integer widthInMillimeters;
    private Integer heightInMillimeters;
    private Integer sizeInMillimeters;
    private String fontColor;
    private String handwritingStyle;

    public static OnDemandPreviewParams testing() {
        return new OnDemandPreviewParams(true, null, null, null, "blue", "usther");
    }

    public Optional<Boolean> getTesting() {
        return Optional.ofNullable(testing);
    }

    public void setTesting(Boolean testing) {
        this.testing = testing;
    }

    public Optional<Integer> getWidthInMillimeters() {
        return Optional.ofNullable(widthInMillimeters);
    }

    public void setWidthInMillimeters(Integer widthInMillimeters) {
        this.widthInMillimeters = widthInMillimeters;
    }

    public Optional<Integer> getHeightInMillimeters() {
        return Optional.ofNullable(heightInMillimeters);
    }

    public void setHeightInMillimeters(Integer heightInMillimeters) {
        this.heightInMillimeters = heightInMillimeters;
    }

    public Optional<Integer> getSizeInMillimeters() {
        return Optional.ofNullable(sizeInMillimeters);
    }

    public void setSizeInMillimeters(Integer sizeInMillimeters) {
        this.sizeInMillimeters = sizeInMillimeters;
    }

    public Optional<String> getFontColor() {
        return Optional.ofNullable(fontColor);
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public Optional<String> getHandwritingStyle() {
        return Optional.ofNullable(handwritingStyle);
    }

    public void setHandwritingStyle(String handwritingStyle) {
        this.handwritingStyle = handwritingStyle;
    }

}
