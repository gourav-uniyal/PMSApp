package pms.co.pmsapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import pms.co.pmsapp.R;

public class PdfActivity extends AppCompatActivity {

    private String TAG = PdfActivity.class.getSimpleName();
    private WebView webView;
    private String pdfurl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_pdf );

        pdfurl = getIntent().getStringExtra( "pdfurl" );


        Log.v( TAG, "PDF Opening");

        webView = findViewById( R.id.webview );

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setBuiltInZoomControls(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("https://docs.google.com/viewer?url=" + pdfurl );

    }
}
