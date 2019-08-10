package pms.co.pmsapp.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import pms.co.pmsapp.R;
import pms.co.pmsapp.model.Case;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private String TAG = MainAdapter.class.getSimpleName();
    private ArrayList<Case> arrayList;

    public MainAdapter(ArrayList<Case> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_home, viewGroup, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.ViewHolder viewHolder, int i) {
        Case data = arrayList.get(i);
        if (i % 2 == 0) {
            viewHolder.linearLayout.setBackgroundResource(R.color.home_row_grey);
        } else {
            viewHolder.linearLayout.setBackgroundResource(R.color.home_row_white);
        }

        viewHolder.lblFileId.setText(data.getFileId());
        viewHolder.lblClientName.setText(data.getClientName());
        viewHolder.lblDate.setText(parseDateToddMMyyyy(data.getDate()));
        if (data.getCustomerName().equals(""))
            viewHolder.lblCustomerName.setVisibility(View.GONE);
        else
            viewHolder.lblCustomerName.setText(data.getCustomerName());
        viewHolder.lblVerification.setText(data.getVerification());
        viewHolder.lblSubject.setText(data.getSubject());
        viewHolder.lblDocumentName.setText(data.getDocName());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView lblDocumentName;
        private TextView lblFileId;
        private TextView lblVerification;
        private TextView lblDate;
        private TextView lblClientName;
        private TextView lblCustomerName;
        private TextView lblSubject;
        private LinearLayout linearLayout;

        ViewHolder(View itemView) {
            super(itemView);

            linearLayout = (LinearLayout) itemView.findViewById(R.id.linear_row_main);
            lblCustomerName = (TextView) itemView.findViewById(R.id.lbl_customer_name);
            lblDate = (TextView) itemView.findViewById(R.id.lbl_date);
            lblFileId = (TextView) itemView.findViewById(R.id.lbl_file_id);
            lblClientName = (TextView) itemView.findViewById(R.id.lbl_client_name);
            lblVerification = (TextView) itemView.findViewById(R.id.lbl_verification_point);
            lblSubject = (TextView) itemView.findViewById(R.id.lbl_subject);
            lblDocumentName = (TextView) itemView.findViewById(R.id.lbl_document_name);
        }
    }

    private String parseDateToddMMyyyy(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd-MMM-yy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        str = str.replace("-", " ");
        return str;
    }

}
