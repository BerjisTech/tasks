package tech.berjis.tasks;

public class Orders {
    String seller, service, status, user, request, currency;
    long time;

    public Orders(String seller, String service, String status, String user, String request, String currency, long time) {
        this.seller = seller;
        this.service = service;
        this.status = status;
        this.user = user;
        this.request = request;
        this.currency = currency;
        this.time = time;
    }

    public Orders() {
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
