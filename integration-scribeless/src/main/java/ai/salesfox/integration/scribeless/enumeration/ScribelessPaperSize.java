package ai.salesfox.integration.scribeless.enumeration;

import lombok.Getter;

public enum ScribelessPaperSize {
    A4,
    A5,
    A5_LANDSCAPE("A5_landscape"),
    A6,
    A6_LANDSCAPE("A6_landscape"),
    LETTER("letter"),
    HALF_LETTER("half_letter"),
    HALF_LETTER_LANDSCAPE("half_letter_landscape"),
    POSTCARD("postcard"),
    POSTCARD_LANDSCAPE("postcard_landscape");

    @Getter
    private final String text;

    ScribelessPaperSize() {
        this.text = name();
    }

    ScribelessPaperSize(String text) {
        this.text = text;
    }

}
