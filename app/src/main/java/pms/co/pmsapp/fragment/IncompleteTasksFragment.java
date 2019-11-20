package pms.co.pmsapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ferfalk.simplesearchview.SimpleSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import pms.co.pmsapp.R;
import pms.co.pmsapp.activity.PhotoActivity;
import pms.co.pmsapp.adapter.MainAdapter;
import pms.co.pmsapp.interfaces.ApiInterface;
import pms.co.pmsapp.libs.ApiClient;
import pms.co.pmsapp.model.Case;
import pms.co.pmsapp.model.ResponseData;
import pms.co.pmsapp.model.ResponseTask;
import pms.co.pmsapp.model.Verifier;
import pms.co.pmsapp.utils.RecyclerTouchListener;
import pms.co.pmsapp.utils.SimpleDividerItemDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncompleteTasksFragment extends Fragment  {

    //region Variable Declaration
    private static final String TAG = IncompleteTasksFragment.class.getSimpleName();
    private Context context;
    private ArrayList<Case> arrayList;
    private MainAdapter mainAdapter;
    private String verifier;
    private int PAGE_START = 1;
    private int TOTAL_PAGE = 1;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    //endregion

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incomplete_tasks, container, false);

        context = getActivity();

        verifier = getArguments().getString("verifier");

        initProgressView( view );
        initRecylerView( view );
        initSwipeRefreshLayout( view );

        progressDialog.show();
        getData(1, "", "all");

        recyclerView.addOnScrollListener( new RecyclerView.OnScrollListener( ) {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled( recyclerView, dx, dy );
                int lastVisibleItem = 0;
                int totalItemCount = Objects.requireNonNull( recyclerView.getAdapter( ) ).getItemCount();
                lastVisibleItem = ((LinearLayoutManager) Objects.requireNonNull( recyclerView.getLayoutManager( ) )).findLastVisibleItemPosition( );
                if (PAGE_START < TOTAL_PAGE) {
                    if (lastVisibleItem == totalItemCount - 1) {
                        progressBar.setVisibility( View.VISIBLE );
                        ++PAGE_START;
                        getData( PAGE_START, "", "all" );
                    }
                }
            }
        } );

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
                intent.putExtra( "verifier", verifier);
                intent.putExtra( "status", "incomplete");
                startActivity(intent);
            }
            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        return view;
    }

    private void getData(final int page, String key, String type) {

        HashMap<String, String> veri = new HashMap<>();
        veri.put("verifier", verifier);

        Log.d( TAG, "getData: " + key + type );

        ApiInterface apiInterface = ApiClient.getRetrofitInstance().create( ApiInterface.class );
        Call<ResponseTask> call = apiInterface.incompletedTask( veri, page, key, type);
        call.enqueue( new Callback<ResponseTask>( ) {
            @Override
            public void onResponse(Call<ResponseTask> call, Response<ResponseTask> response) {
                ResponseTask responseTask = response.body();
                if(responseTask.getResponseData()!=null){
                    ResponseData responseData = responseTask.getResponseData();
                    if(responseData.getCaseArrayList()!=null){
                        progressBar.setVisibility( View.GONE );
                        if(!key.equals( "" ))
                            Log.d( TAG, "onResponse: " + responseTask.getStatus() + key + type + "api hit hui search se");
                        TOTAL_PAGE = Integer.parseInt(responseData.getTotalPage());
                        arrayList.addAll(responseData.getCaseArrayList());
                        progressDialog.dismiss();
                        swipeRefreshLayout.setRefreshing( false );
                    }
                    mainAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<ResponseTask> call, Throwable t) {
                Toast.makeText( getActivity(), "Error on loading Response", Toast.LENGTH_SHORT ).show();
            }
        } );
    }

    void initProgressView(View view){
        //dialog progress bar
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside( false );

        //footer progress bar
        progressBar = view.findViewById(R.id.progressBar_incompleted_task);
        progressBar.setVisibility( View.GONE );
    }

    void initSwipeRefreshLayout(View view){

        swipeRefreshLayout = view.findViewById( R.id.swipe_refresh_layout_incompleted_task );
        swipeRefreshLayout.setOnRefreshListener( () -> {
            progressDialog.show();
            arrayList.clear();
            mainAdapter.notifyDataSetChanged();
            getData(1, "", "all");
        } );
    }

    void initRecylerView(View view){
        arrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation( RecyclerView.VERTICAL );

        recyclerView = (RecyclerView) view.findViewById( R.id.rvIncompleteTasks );
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));

        mainAdapter = new MainAdapter( arrayList );
        recyclerView.setAdapter(mainAdapter);
    }

    public void beginSearch(String key, String type){
        Log.d( TAG, "beginSearch: " + key + type );

        getData( PAGE_START , key, type);
    }
}