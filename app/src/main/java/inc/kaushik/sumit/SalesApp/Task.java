package inc.kaushik.sumit.SalesApp;

/**
 * Created by sumitkaushik on 9/6/17.
 */
public class Task {
    private String clientName,details,location,status;
    public Task(){

    }

    public Task(String clientName, String location,String status,String details) {
        this.clientName=clientName;
        this.details=details;
        this.location=location;
        this.status=status;

    }

    public String getClientName() {
        return clientName;
    }

    public String getDetails() {
        return details;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
