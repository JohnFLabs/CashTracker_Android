package kz.flabs.cashtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void LogIn(View view) throws IOException {
        android.util.Log.d("info", "111");
        EditText loginField = (EditText) findViewById(R.id.login);
        EditText pwdField = (EditText) findViewById(R.id.pwd);

        if(loginField.getText().length() != 0 && pwdField.getText().length() != 0){
            new SendLoginRequest().execute(loginField.getText().toString(),pwdField.getText().toString());
        }else{
              showNotif("Проверьте заполненность полей ввода и повторите попытку");
        }

    }

    public void showNotif(String message) {
        Toast toast = Toast.makeText(getApplicationContext(),
                message,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    public void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }



    public class SendLoginRequest extends AsyncTask<String, Integer, HttpResponse> {
        Integer status;
        HttpResponse response;
        String response_string;
        @Override
        protected HttpResponse doInBackground(String... params) {
            try {
                HttpPost httppost = new HttpPost("http://172.16.250.9:38555/Administrator/rest/session");
                HttpClient httpclient = new DefaultHttpClient();
                httppost.setHeader("Accept", "application/json");
                httppost.setHeader("Content-type", "application/json");
                httppost.setHeader("X-Request-With", "XMLHttpRequest");
                JSONObject auth = new JSONObject();
                auth.put("login", params[0]);
                auth.put("pwd", params[1]);
                JSONObject authUser = new JSONObject();
                authUser.put("authUser",auth);
                StringEntity se = new StringEntity(authUser.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se);
                response = httpclient.execute(httppost);
                HttpEntity resEntityGet = response.getEntity();

                if (resEntityGet != null) {

                    //String response_string = null;
                    try {
                        response_string = EntityUtils.toString(resEntityGet);
                        android.util.Log.d("GET RESPONSE", response_string);
                       // intent.putExtra("response", response_string);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return response;
        }

        protected void onPostExecute(HttpResponse response) {
            status = response.getStatusLine().getStatusCode();

            if (status == 401) {
                android.util.Log.d("error", "Auth failed");
                showAlert("Неверное имя пользователя или пароль");
            }
            if (status == 200) {
                android.util.Log.d("info", "Auth ok");
                Intent intent = new Intent(MainActivity.this, docs_page.class);
                intent.putExtra("response", response_string);
                startActivity(intent);
            }
        }
    }

    public void makeView() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://172.16.250.9:38555/CashTracker/rest/page/welcome";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
        public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub
               String testresp = "Response => "+response.toString();
               android.util.Log.d("response", testresp);
               // findViewById(R.id.progressBar1).setVisibility(View.GONE);

                try {
                    //JSONArray jsonMainNode = response.optJSONArray("_Page");
                    JSONObject _Page = response.getJSONObject("_Page");
                    JSONObject includedPages = _Page.getJSONArray("includedPages").getJSONObject(0);
                    JSONObject captions = includedPages.getJSONObject("captions");
                   // JSONArray jsonMainNode = jobject.optJSONArray("elements");
                    String login_button_caption = captions.getJSONArray("login_button").getString(0);
                    String login_caption = captions.getJSONArray("login_login").getString(0);
                    String pwd_caption = captions.getJSONArray("login_pwd").getString(0);
                    String promo_caption = captions.getJSONArray("promo_line1").getString(0);


                    setContentView(R.layout.activity_main);
                    Button login_button = (Button)findViewById (R.id.loginbtn);
                    login_button.setText(login_button_caption);

                    EditText login_edittext = (EditText)findViewById (R.id.login);
                    login_edittext.setHint(login_caption);

                    EditText pwd_edittext = (EditText)findViewById (R.id.pwd);
                    pwd_edittext.setHint(pwd_caption);

                    TextView promo_textview = (TextView)findViewById (R.id.textView);
                    promo_textview.setText(promo_caption);

                    //JSONObject includedPages = jsonMainNode.getJSONObject(0);
                    //JSONArray elements = includedPages.getJSONArray("elements");
                  //  String logInCaption = elements.getJSONObject(0).toString();

                   // showAlert(OutputData);
                    //jsonParsed.setText( OutputData );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        });
        queue.add(jsObjRequest);


    }
}