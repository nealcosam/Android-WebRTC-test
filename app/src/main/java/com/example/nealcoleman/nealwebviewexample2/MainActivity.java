package com.example.nealcoleman.nealwebviewexample2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

/*
    Simple WebView code that loads an HTML page containing WebRTC code

    Reference:
    http://www.flapjacksandcode.com/blog/2015/2/17/android-webviews-getusermedia-putting-it-all-together

 */


//************************We need Camera and Microphone run time permissions for this code to work************
// Set them manually, until we do it programmatically
// https://developer.android.com/training/permissions/requesting.html


public class MainActivity extends Activity {
    private String TAG = "WEB MainActivity";
    private WebView mWebView;
    private Button button0;
    private Button button1;
    private Button button2;


    // HTML file in app/src/main/assets folder - this WORKS
    //private static String URL = "file:///android_asset/test.html";

    // HTML file on the server
    //private static String URL = "https://nealcosam.github.io/test2.github.io/sdk-webrtc-onvideo-master/sdk-webrtc-onvideo-master/index.html";
    //private static String URL = "https://nealcosam.github.io/test2.github.io/sdk-webrtc-onvideo-master/sdk-webrtc-onvideo-master/index2.html";
    private static String URL = "https://nealcosam.github.io/test2.github.io/sdk-webrtc-onvideo-master/sdk-webrtc-onvideo-master/index_jquery3.html";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main2);

        mWebView = (WebView) findViewById(R.id.webview);
        button0 =(Button)findViewById(R.id.button0);
        button1 =(Button)findViewById(R.id.button1);
        button2 =(Button)findViewById(R.id.button2);

        // Button handlers
        button0.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hdlButtonFillInForm();
                }
            }
        );

        button1.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hdlButtonToggleVideo();
                }
            }
        );

        button2.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hdlButton2();
                }
            }
        );

        // Enable JavaScript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //////////////// these require API level 16 /////////////////////////////
        // These are required to allow webkitGetUserMedia() for work from a local URL file:
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        //////////////// these require API level 16 /////////////////////////////


        // During testing make sure we're not caching
        // https://stackoverflow.com/questions/21059631/how-to-disable-cache-in-android-webview
        webSettings.setAppCacheEnabled(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);


        // Add JavaScript interface to allow calls from WebView -> Android
        mWebView.addJavascriptInterface(new MyFromJavascriptInterface(this), "Android");

        // Set a web view client
        mWebView.setWebViewClient(new WebViewClient());

        // Set a chrome client
        mWebView.setWebChromeClient(new WebChromeClient() {
            // Need to accept permissions to use the camera and audio
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d(TAG, "onPermissionRequest");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        // Make sure the request is coming from our file
                        // Warning: This check may fail for local files
                        //if(request.getOrigin().toString().equals(URL)) {
                            request.grant(request.getResources());
                        //}
                        //else {
                          //  request.deny();
                        //}
                    }
                });
            }

            // Output the WebView console logs
            // https://developer.android.com/guide/webapps/debugging.html#WebView
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(TAG, "WEBVIEW [" + cm.sourceId() + ", line " + cm.lineNumber() + "] "+cm.message());
                return true;
            }
        });

        // load the page
        mWebView.loadUrl(URL);
    }


    //
    // Interface from JavaScript webpage --> Android
    //
    // *****Note these methods are called on a thread called "JavaBridge" not the main thread
    //
    private class MyFromJavascriptInterface {
        //Context mContext;
        Activity mContext;

        MyFromJavascriptInterface(/*Context c*/Activity c) {
            mContext = c;
        }

        // This function can be called from JavaScript
        @JavascriptInterface
        public void hdlEvent1fromJson(String s) {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        }

        // This function can be called from JavaScript
        @JavascriptInterface
        public void hdlEvent2fromJson(String s) {
            //Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();

            // We're not on the UI thread, so we need runOnUiThread()
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    count++;
                    String s = "Got event from JavaScript, f="+count;
                    setWebpageText(s);
                }
            });
        }
    }


    private static final String MEETING_ID = "id";
    private static final String MEETING_PASS_CODE = "passCode";
    private static final String MEETING_YOUR_NAME = "yourName";


    private static final String meetingId = "9637611056";
    private static final String passCode = "";
    private static final String yourName = "Neal Coleman - Phone";


    private void hdlButtonFillInForm() {
        setWebpageFormValue(MEETING_ID, meetingId);
        setWebpageFormValue(MEETING_PASS_CODE, passCode);
        setWebpageFormValue(MEETING_YOUR_NAME, yourName);
    }

    private void hdlButtonToggleVideo() {
        //
        String js = "javascript:toggleVideoMute();";
        //String js = "" + "nealToggleVideo();";
        executeJavaScript(js);
    }

    private void hdlButton2() {
        Log.d(TAG,"hdlButton2()");

        // method #1
        //String js1 = "javascript:nealTestFunction();";   // works for index2.htm
        String js1 = "javascript:nealTestParamFunction ('abcdef');";   // works for index2.htm
        mWebView.loadUrl(js1); // temp

        // method #2
        //String js = "javascript:nealTestFunction2()";  // Uncaught ReferenceError: nealTestFunction is not defined
        //String js = "nealTestFunction2()";  // Uncaught ReferenceError: nealTestFunction is not defined
        //mWebView.loadUrl(js); // temp

        //executeJavaScript(js);
    }

    int count = 0;
    private static final String FIELD0 = "field0";


    // All WebView access must be done on the UI thread
    private void setWebpageText(String s) {
        String js = "javascript:document.getElementById('"+FIELD0+"').innerHTML = '"+s+"';";
        executeJavaScript(js);
    }

    private void setWebpageFormValue(String field, String val) {
        String js = "javascript:document.getElementById('"+field+"').value = '"+val+"';";
        executeJavaScript(js);
    }

    private void executeJavaScript(String js) {
        // system version check, API 19 = OS 4.4
        if (android.os.Build.VERSION.SDK_INT < 19) {
            // ....................this path has not been tested
            mWebView.loadUrl(js);
        }
        else {
            mWebView.evaluateJavascript(js, null);
        }
    }

}