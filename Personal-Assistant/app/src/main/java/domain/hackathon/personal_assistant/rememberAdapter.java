package domain.hackathon.personal_assistant;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

public class rememberAdapter extends RecyclerView.Adapter<rememberAdapter.ViewHolder> {
    private List<rememberDisplay> mDataset;
    private Context mcontext;
    FirebaseAuth auth;
    StorageReference storageRef;
    FirebaseStorage storage;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title, body;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.remtitle);
            body = (TextView) v.findViewById(R.id.rembody);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public rememberAdapter(List<rememberDisplay> myDataset, Context context) {
        mDataset = myDataset;
        mcontext = context;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        auth = FirebaseAuth.getInstance();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public rememberAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.remember_rec_layout, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final rememberDisplay info = mDataset.get(position);
        holder.title.setText(info.getTitle());
        if (info.type.equals("List")) {
            StorageReference dlref = storageRef.child("text-files").child(auth.getCurrentUser().getUid().toString()).child(info.title + ".txt");

            final File fileNameOnDevice = new File(mcontext.getFilesDir() + "dlfile.txt");
            dlref.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    String dis;

                    try {
                        FileReader fileReader = new FileReader(fileNameOnDevice.getAbsolutePath().toString());

                        BufferedReader bufferedReader = new BufferedReader(fileReader);

                        while ((line = bufferedReader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        sb = sb.deleteCharAt(sb.length() - 1);
                        dis = sb.toString();
                        if (fileNameOnDevice != null)
                            fileNameOnDevice.delete();

                        bufferedReader.close();
                        holder.body.setText(dis);

                    } catch (Exception ex) {
                        Log.d("notesActivity", "File exception " + ex);
                    }
                }
            });
        } else {
            holder.body.setText(info.getBody());
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mDataset.size();
    }
}
