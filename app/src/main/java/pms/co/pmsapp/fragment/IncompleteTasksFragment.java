package pms.co.pmsapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
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
    private ArrayList<Case> filterArrayList;
    private ArrayList<Case> normalArrayList;

    private int PAGE_START = 1;
    private int TOTAL_PAGE = 1;
    private int FILTER_PAGE = 1;

    private MainAdapter mainAdapter;
    private String verifier;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private String key="";
    private String type="all";
    //endregion

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incomplete_tasks, container, false);

        context = getActivity();

        verifier = getArguments().getString("verifier");

        arrayList = new ArrayList<>();
        filterArrayList = new ArrayList<>();
        normalArrayList = new ArrayList<>();

        initProgressView( view );
        initRecylerView( view );
        initSwipeRefreshLayout( view );

        progressDialog.show();
        getData(1, key, type);

        recyclerView.addOnScrollListener( new RecyclerView.OnScrollListener( ) {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled( recyclerView, dx, dy );
                int lastVisibleItem = 0;
                int totalItemCount = Objects.requireNonNull( recyclerView.getAdapter( ) ).getItemCount();
                lastVisibleItem = ((LinearLayoutManager) Objects.requireNonNull( recyclerView.getLayoutManager( ) )).findLastVisibleItemPosition( );

                if(!key.equals( "" )) {
                    if (FILTER_PAGE < TOTAL_PAGE)
                        if (lastVisibleItem == totalItemCount - 1) {
                            progressBar.setVisibility( View.VISIBLE );
                            ++FILTER_PAGE;
                            Log.d( TAG, "onScrolled: " + key + type );
                            getData( FILTER_PAGE, key, type );
                        }
                }
                else {
                    if (PAGE_START < TOTAL_PAGE)
                        if (lastVisibleItem == totalItemCount - 1) {
                            progressBar.setVisibility( View.VISIBLE );
                            ++PAGE_START;
                            Log.d( TAG, "onScrolled: " + key + type );
                            getData( PAGE_START, key, type );
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
                        arrayList.clear();
                        if(!key.equals( "" )) {
                            Log.d( TAG, "onResponse: " + responseTask.getStatus( ) + " key: " + key + " type: " + type + " filter arraylist initialise" );
                            filterArrayList.addAll(responseTask.getResponseData().getCaseArrayList());
                            arrayList.addAll(filterArrayList);
                        }
                        else {
                            Log.d( TAG, "onResponse: " + responseTask.getStatus( ) + " key: " + key + " type: " + type + " normal arraylist initialise" );
                            normalArrayList.addAll(responseTask.getResponseData().getCaseArrayList());
                            arrayList.addAll(normalArrayList);
                        }
                        mainAdapter.notifyDataSetChanged();
                        TOTAL_PAGE = Integer.parseInt(responseData.getTotalPage());
                        progressDialog.dismiss();
                        swipeRefreshLayout.setRefreshing( false );
                    }
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
            if(!key.equals( "" )) {
                filterArrayList.clear( );
                Log.d( TAG, "initSwipeRefreshLayout: " + key + type );
                FILTER_PAGE = 1;
                getData( FILTER_PAGE, key, type );
            }
            else {
                normalArrayList.clear( );
                Log.d( TAG, "initSwipeRefreshLayout: " + key + type);
                PAGE_START = 1;
                getData( PAGE_START , key, type);
            }
        } );
    }

    void initRecylerView(View view){
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

        this.key = key;
        this.type = type;

        progressDialog.show();

        filterArrayList.clear();

        FILTER_PAGE = 1;
        getData( FILTER_PAGE, this.key, this.type);
    }

    public void endSearch(String key, String type){

        this.key = key;
        this.type = type;

        arrayList.clear();
        arrayList.addAll(normalArrayList);

        Log.d( TAG, "endSearch: " + "arraylist initialised to normal" );

        mainAdapter.notifyDataSetChanged();
    }
}