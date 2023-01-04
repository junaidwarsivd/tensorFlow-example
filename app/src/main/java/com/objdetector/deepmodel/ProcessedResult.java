package com.objdetector.deepmodel;

import android.graphics.RectF;

public final class ProcessedResult implements Comparable<ProcessedResult> {
    private final Integer id;
    private final String title;
    private final Float confidence;
    private RectF location;

    public ProcessedResult(final Integer id, final String title,
                           final Float confidence, final RectF location) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
        this.location = location;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() { return title; }

    public Float getConfidence() { return confidence; }

    public RectF getLocation() {
        return new RectF(location);
    }

    public void setLocation(RectF location) {
        this.location = location;
    }

    @Override
    public int compareTo (ProcessedResult other) {
        return Float.compare(other.getConfidence(), this.getConfidence());
    }

    @Override
    public String toString() {
        return "DetectionResult{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", confidence=" + confidence +
                ", location=" + location +
                '}';
    }
}
