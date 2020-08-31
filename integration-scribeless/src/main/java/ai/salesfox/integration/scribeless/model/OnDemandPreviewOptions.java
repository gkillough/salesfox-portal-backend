package ai.salesfox.integration.scribeless.model;

import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class OnDemandPreviewOptions {
    private Boolean testing;
    private Integer widthInMillimeters;
    private Integer heightInMillimeters;
    private Integer sizeInMillimeters;
    private String fontColor;
    private String handwritingStyle;

    public static OnDemandPreviewOptions testing() {
        return new OnDemandPreviewOptions(true, null, null, null, "blue", "usther");
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
