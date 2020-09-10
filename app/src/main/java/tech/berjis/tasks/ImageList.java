package tech.berjis.tasks;

public class ImageList {
    String image, image_id, parent_id;

    public ImageList(String image, String image_id, String parent_id) {
        this.image = image;
        this.image_id = image_id;
        this.parent_id = parent_id;
    }

    public ImageList() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }
}
