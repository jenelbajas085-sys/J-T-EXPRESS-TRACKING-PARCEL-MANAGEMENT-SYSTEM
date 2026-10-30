public class Ticket {
    private String ticketId;
    private String receiver;
    private String concern;
    private String status;
    private String closedBy;
    public Ticket(String ticketId, String receiver,
            String concern, String status) {
        this.ticketId = ticketId;
        this.receiver = receiver;
        this.concern = concern;
        this.status = status;
        this.closedBy = "";
    }
    public String getTicketId() {
        return ticketId;
    }
    public String getReceiver() {
        return receiver;
    }
    public String getConcern() {
        return concern;
    }
    public String getStatus() {
        return status;
    }
    public String getClosedBy() {
        return closedBy;
    }
    public void close(String supervisorName) {
        status = "Closed";
        closedBy = supervisorName;
    }
}
