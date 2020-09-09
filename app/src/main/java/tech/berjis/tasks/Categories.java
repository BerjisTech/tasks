package tech.berjis.tasks;

public class Categories {
    String name, image;

    public Categories(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public Categories(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
