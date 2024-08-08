package com.example.taam.structures;

import java.io.Serializable;

public class Item implements Serializable {
    private int lotNumber;
    private String name;
    private String category;
    private String period;
    private String description;
    private String imageExtension;

    // Necessary for firebase conversion
    public Item() {}

    public Item(int lotNumber, String name, String category, String period, String description) {
        this.lotNumber = lotNumber;
        this.name = name;
        this.category = category;
        this.period = period;
        this.description = description;
    }

    public int getLotNumber() { return lotNumber; }
    public void setLotNumber(int lotNumber) { this.lotNumber = lotNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageExtension() { return imageExtension; }
    public void setImageExtension(String imageExtension) { this.imageExtension = imageExtension; }
}
