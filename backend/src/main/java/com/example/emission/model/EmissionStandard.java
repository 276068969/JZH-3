package com.example.emission.model;

public enum EmissionStandard {
    GUO_SIX("国六", 0.3, 30.0, 70.0, 0.25),
    GUO_FIVE("国五", 0.5, 50.0, 90.0, 0.35),
    GUO_FOUR("国四", 0.8, 80.0, 120.0, 0.50);

    private final String label;
    private final double coLimit;
    private final double hcLimit;
    private final double noxLimit;
    private final double opacityLimit;

    EmissionStandard(String label, double coLimit, double hcLimit, double noxLimit, double opacityLimit) {
        this.label = label;
        this.coLimit = coLimit;
        this.hcLimit = hcLimit;
        this.noxLimit = noxLimit;
        this.opacityLimit = opacityLimit;
    }

    public String getLabel() {
        return label;
    }

    public double getCoLimit() {
        return coLimit;
    }

    public double getHcLimit() {
        return hcLimit;
    }

    public double getNoxLimit() {
        return noxLimit;
    }

    public double getOpacityLimit() {
        return opacityLimit;
    }

    public static EmissionStandard fromLabel(String label) {
        if (label == null) {
            return GUO_FOUR;
        }
        if (label.contains("国六")) {
            return GUO_SIX;
        }
        if (label.contains("国五")) {
            return GUO_FIVE;
        }
        return GUO_FOUR;
    }
}
