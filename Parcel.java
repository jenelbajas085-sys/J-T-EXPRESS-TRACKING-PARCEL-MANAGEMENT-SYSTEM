public class Parcel {
    private String trackingNumber;
    private String sender;
    private String receiver;
    private String destination;
    private String status;
    private Rider assignedRider;
    private String deliveredBy;
    private String deliveredTo;
    public Parcel(String trackingNumber, String sender,
            String receiver, String destination,
            String status) {
        this.trackingNumber = trackingNumber;
        this.sender = sender;
        this.receiver = receiver;
        this.destination = destination;
        this.status = status;
    }
    public String getTrackingNumber() {
        return trackingNumber;
    }
    public String getSender() {
        return sender;
    }
    public String getReceiver() {
        return receiver;
    }
    public String getDestination() {
        return destination;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Rider getAssignedRider() {
        return assignedRider;
    }
    public void setAssignedRider(Rider assignedRider) {
        this.assignedRider = assignedRider;
    }
    public String getDeliveredBy() {
        return deliveredBy;
    }
    public String getDeliveredTo() {
        return deliveredTo;
    }
    public void deliver() {
        if (assignedRider != null) {
            status = "Delivered";
            deliveredBy = assignedRider.getName();
            deliveredTo = receiver;
        }
    }
}
