import java.util.ArrayList;
public class Rider {
    private String name;
    private String assignedArea;
    private ArrayList<Parcel> assignedParcels;
    public static final int MAX_PARCELS = 150;
    public Rider(String name, String assignedArea) {
        this.name = name;
        this.assignedArea = assignedArea;
        this.assignedParcels = new ArrayList<Parcel>();
    }
    public String getName() {
        return name;
    }
    public String getAssignedArea() {
        return assignedArea;
    }
    public int getParcelCount() {
        return assignedParcels.size();
    }
    public int getRemainingCapacity() {
        return MAX_PARCELS - assignedParcels.size();
    }
    public boolean assignParcel(Parcel parcel) {
        if (assignedParcels.size() >= MAX_PARCELS) {
            return false;
        }
        assignedParcels.add(parcel);
        parcel.setAssignedRider(this);
        return true;
    }
}
