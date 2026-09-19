package com.algoadda.core.bot.dto;

public class VersionDiffLineDto {

    public enum DiffType {
        ADDED,
        REMOVED,
        UNCHANGED
    }

    private DiffType type;
    private String text;
    private Integer lineNumberFrom;
    private Integer lineNumberTo;

    public VersionDiffLineDto() {
    }

    public VersionDiffLineDto(DiffType type, String text, Integer lineNumberFrom, Integer lineNumberTo) {
        this.type = type;
        this.text = text;
        this.lineNumberFrom = lineNumberFrom;
        this.lineNumberTo = lineNumberTo;
    }

    public DiffType getType() { return type; }
    public void setType(DiffType type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getLineNumberFrom() { return lineNumberFrom; }
    public void setLineNumberFrom(Integer lineNumberFrom) { this.lineNumberFrom = lineNumberFrom; }

    public Integer getLineNumberTo() { return lineNumberTo; }
    public void setLineNumberTo(Integer lineNumberTo) { this.lineNumberTo = lineNumberTo; }
}
