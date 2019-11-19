package pms.co.pmsapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import pms.co.pmsapp.utils.RecyclerTouchListener;
import pms.co.pmsapp.utils.SimpleDividerItemDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompletedTasksFragment extends Fragment {

    //region Variable Declaration
    private static final String TAG = CompletedTasksFragment.class.getSimpleName( );
    private Context context;
    private ArrayList<Case> arrayList;
    private String verifier;
    private MainAdapter mainAdapter;
    private int PAGE_START = 1;
    private int TOTAL_PAGE = 1;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    //endregion

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.fragment_completed_tasks, container, false );
        context = getActivity( );

        verifier = getArguments( ).getString( "verifier" );

        arrayList = new ArrayList<>( );

        initProgressViews( view );

        initSwipeRefreshLayout(view);

        initRecyclerView(view);

        progressDialog.show( );
        getData( 1, "", "all" );

        recyclerView.addOnScrollListener( new RecyclerView.OnScrollListener( ) {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled( recyclerView, dx, dy );
                int lastVisibleItem = 0;
                int totalItemCount = Objects.requireNonNull( recyclerView.getAdapter( ) ).getItemCount( );
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

        recyclerView.addOnItemTouchListener( new RecyclerTouchListener( context, recyclerView, new RecyclerTouchListener.ClickListener( ) {
            @Override
            public void onClick(View view, int position) {
                String docId = arrayList.get( position ).getDocId( );
                String path = arrayList.get( position ).getPath( );
                String formPath = arrayList.get( position ).getFormpath( );
                String fileId = arrayList.get( position ).getFileId( );
                Intent intent = new Intent( context, PhotoActivity.class );
                intent.putExtra( "document_id", docId );
                intent.putExtra( "path", path );
                intent.putExtra( "form_path", formPath );
                intent.putExtra( "fileId", fileId );
                intent.putExtra( "status", "complete" );
                intent.putExtra( "verifier", verifier );
                startActivity( intent );
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        return view;
    }

    private void getData(int page, String key, String type) {

        HashMap<String, String> veri = new HashMap<>();
        veri.put("verifier", verifier);

        Log.d( TAG, "getData: " + key + type );

        ApiInterface apiInterface = ApiClient.getRetrofitInstance().create( ApiInterface.class );
        Call<ResponseTask> call = apiInterface.completedTask( veri, page, key, type);
        call.enqueue( new Callback<ResponseTask>( ) {
            @Override
            public void onResponse(Call<ResponseTask> call, Response<ResponseTask> response) {
                ResponseTask responseTask = response.body();
                if(responseTask!=null){
                    ResponseData responseData = responseTask.getResponseData();
                    if(responseData!=null){
                        progressBar.setVisibility( View.GONE );
                        Log.d( TAG, "onResponse: " + responseTask.getStatus() + key + type );
                        TOTAL_PAGE = Integer.parseInt(responseData.getTotalPage());
                        arrayList.addAll(responseData.getCaseArrayList());
                        mainAdapter.notifyDataSetChanged();
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

    void initProgressViews(View view){

        progressDialog = new ProgressDialog( context );
        progressDialog.setTitle( "Loading" );
        progressDialog.setMessage( "Please Wait..." );
        progressDialog.setCanceledOnTouchOutside( true );

        progressBar = view.findViewById(R.id.progressBar_completed_task);
        progressBar.setVisibility( View.GONE );
    }

    void initSwipeRefreshLayout(View view){

        swipeRefreshLayout = view.findViewById( R.id.swipe_refresh_layout_completed_task );
        swipeRefreshLayout.setOnRefreshListener( () -> {
            progressDialog.show( );
            arrayList.clear( );
            PAGE_START = 1;
            getData( 1 , "", "all");
        } );

    }

    void initRecyclerView(View view){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( context );
        linearLayoutManager.setOrientation( RecyclerView.VERTICAL );

        recyclerView = (RecyclerView) view.findViewById( R.id.rvCompletedTasks );
        recyclerView.setLayoutManager( linearLayoutManager );
        recyclerView.addItemDecoration( new SimpleDividerItemDecoration( context ) );

        mainAdapter = new MainAdapter( arrayList );
        recyclerView.setAdapter( mainAdapter );
    }

    public void beginSearch(String key, String type){
        Log.d( TAG, "beginSearch: " + key + type );

        getData( PAGE_START , key, type);
    }
}