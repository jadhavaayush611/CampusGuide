package com.campusguide.campus.notice.enums;

public enum NoticePriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    URGENT(4);

    private final int weight;

    NoticePriority(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public int weight() {
        return weight;
    }

    public static java.util.Comparator<NoticePriority> byWeightDesc() {
        return java.util.Comparator.nullsLast(
                java.util.Comparator.comparingInt(NoticePriority::getWeight).reversed()
        );
    }
}
