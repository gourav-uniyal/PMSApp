package pms.co.pmsapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pms.co.pmsapp.R;
import pms.co.pmsapp.activity.PhotoActivity;
import pms.co.pmsapp.adapter.MainAdapter;
import pms.co.pmsapp.model.Case;
import pms.co.pmsapp.utils.AppController;
import pms.co.pmsapp.utils.EndPoints;
import pms.co.pmsapp.utils.RecyclerTouchListener;
import pms.co.pmsapp.utils.SimpleDividerItemDecoration;

public class IncompleteTasksFragment extends Fragment {

    private static final String TAG = IncompleteTasksFragment.class.getSimpleName();
    private Context context;
    private ArrayList<Case> arrayList;
    private MainAdapter mainAdapter;
    private RecyclerView recyclerView;
    private String verifier;
    private String status;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incomplete_tasks, container, false);
        context = getActivity();

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");

        recyclerView = (RecyclerView) view.findViewById(R.id.rvIncompleteTasks);
        verifier = getArguments().getString("verifier");
        arrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation( LinearLayoutManager.VERTICAL );
        getData();

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String docId = arrayList.get(position).getDocId();
                String path = arrayList.get(position).getPath();
                String formPath = arrayList.get(position).getFormpath();
                String fileId = arrayList.get(position).getFileId();
                Intent intent = new Intent(context, PhotoActivity.class);
                intent.putExtra("document_id", docId);
                intent.putExtra("path", path);
                intent.putExtra("form_path", formPath);
                intent.putExtra("fileId", fileId);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        return view;
    }

    private void getData() {

        Map<String, String> params = new HashMap<>();
        params.put("verifier", verifier);

        final JSONObject jsonObject1 = new JSONObject(params);

        progressDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, EndPoints.INCOMPLETE_TASK_API,
                jsonObject1, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                progressDialog.dismiss();

                try {
                    Log.v(TAG, "Response" + response.toString());
                    status = response.getString("status");
                    Log.v(TAG, "VolleySuccess" + status);

                    if (status.equals("success")) {

                        JSONArray jsonArray = new JSONArray(response.getString("data"));

                        Log.v(TAG, "JsonArray" + jsonArray.toString());

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = (jsonArray.getJSONObject(i));

                            Case data = new Case();
                            data.setFileId(jsonObject.getString("file_id"));
                            data.setClientId(jsonObject.getString("client_id"));
                            data.setDate(jsonObject.getString("created_at"));
                            data.setSubject(jsonObject.getString("subject"));
                            data.setCustomerName( jsonObject.getString( "customer_name" ) );

                            JSONObject jsonObject1 = (JSONObject) jsonArray.getJSONObject(i).get("client");
                            data.setClientName(jsonObject1.getString("name"));

                            JSONArray jsonArray1 = jsonObject.getJSONArray("documents");
                            if (jsonArray1 != null) {
                                for (int j = 0; j < jsonArray1.length(); j++) {
                                    data.setDocId(jsonArray1.getJSONObject(j).getString("id"));
                                    data.setVerification(jsonArray1.getJSONObject(j).getString("verification_point"));
                                    data.setPath(jsonArray1.getJSONObject(j).getString("path"));
                                    data.setFormpath(jsonArray1.getJSONObject(j).getString("form_path"));
                                    data.setDocName(jsonArray1.getJSONObject(j).getString("name"));
                                    arrayList.add(data);
                                }
                            }
                        }
                        mainAdapter = new MainAdapter(arrayList);
                        recyclerView.setAdapter(mainAdapter);

                    } else {
                        Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(com.android.volley.VolleyError error) {
                progressDialog.dismiss();
                Log.e(TAG, error.getLocalizedMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }
}
