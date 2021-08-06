package com.example.richard.ectablet.Adapters;

public class SpinnerIndNavigationAdapter {
    String indicationName;
    String imagePath;

    public SpinnerIndNavigationAdapter(String indicationName, String imagePath) {
        this.indicationName = indicationName;
        this.imagePath = imagePath;
    }

    public String getIndicationName() {
        return indicationName;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String geImagePath() {
        return imagePath;
    }

    public void setIndicationName(String indicationName) {
        this.indicationName = indicationName;
    }
}
