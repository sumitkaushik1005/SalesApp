package inc.kaushik.sumit.SalesApp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sumitkaushik on 9/6/17.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {
    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.view_completed,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
Task task=taskList.get(position);
        holder.cName.setText(task.getClientName());
        holder.details.setText(task.getDetails());
        holder.status.setText(task.getStatus());
        holder.cLocation.setText(task.getLocation());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder {
        public TextView cName,cLocation,details,status;
        public MyViewHolder(View itemView) {
            super(itemView);
            cName=(TextView)itemView.findViewById(R.id.clientName);
            details=(TextView)itemView.findViewById(R.id.details);
            cLocation=(TextView)itemView.findViewById(R.id.location);
            status=(TextView)itemView.findViewById(R.id.status);

        }
    }
}
