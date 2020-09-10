package tech.berjis.tasks;

class Services {
    String category, location, service_id, text, user;
    long price, requests, time;

    public Services(String category, String location, String service_id, String text, String user, long price, long requests, long time) {
        this.category = category;
        this.location = location;
        this.service_id = service_id;
        this.text = text;
        this.user = user;
        this.price = price;
        this.requests = requests;
        this.time = time;
    }

    public Services(){}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getRequests() {
        return requests;
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
