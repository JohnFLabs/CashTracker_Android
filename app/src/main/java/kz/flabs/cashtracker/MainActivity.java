package kz.flabs.cashtracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                HttpGet httpget = new HttpGet("http://172.16.250.64:38800/Workflow/Provider?type=page&id=taskforme&page=0&onlyxml");
                HttpClient httpclient = new DefaultHttpClient();
                httpget.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(params[0], params[1]),"UTF-8", false));
                response = httpclient.execute(httpget);
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

    public void testRestProvider(View view) {
        String serverURL = "http://androidexample.com/media/webservice/JsonReturn.php";
        //String serverURL = "http://172.16.250.9:38555/CashTracker/RestProvider/page/welcome";

        new LongOperation().execute(serverURL);
    }

    // Class with extends AsyncTask class

    private class LongOperation  extends AsyncTask<String, Void, Void> {

        // Required initialization

        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);
        String data ="";
        //TextView uiUpdate = (TextView) findViewById(R.id.output);
        // TextView jsonParsed = (TextView) findViewById(R.id.jsonParsed);
        int sizeData = 0;
        //EditText serverText = (EditText) findViewById(R.id.serverText);


        protected void onPreExecute() {


            //Start Progress Dialog (Message)

             Dialog.setMessage("Please wait..");
             Dialog.show();

            try{
                // Set Request parameter
                data +="&" + URLEncoder.encode("data", "UTF-8") + "=";//+serverText.getText();

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {


            BufferedReader reader=null;

            // Send data
            try
            {


                URL url = new URL(urls[0]);



                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                //wr.write( data );
                wr.flush();



                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {

                    sb.append(line + " ");
                }


                Content = sb.toString();
            }
            catch(Exception ex)
            {
                Error = ex.getMessage();
            }
            finally
            {
                try
                {

                    reader.close();
                }

                catch(Exception ex) {}
            }

            /*****************************************************/
            return null;
        }

        protected void onPostExecute(Void unused) {

            Dialog.dismiss();

            if (Error != null) {

                // uiUpdate.setText("Output : "+Error);

            } else {


                // uiUpdate.setText( Content );

                /****************** Start Parse Response JSON Data *************/

                String OutputData = "";
                JSONObject jsonResponse;

                try {

                    jsonResponse = new JSONObject(Content);

                    JSONArray jsonMainNode = jsonResponse.optJSONArray("Android");
                    android.util.Log.d("jsonResponse", jsonResponse.toString());
                    /*********** Process each JSON Node ************/

                    int lengthJsonArr = jsonMainNode.length();

                    for(int i=0; i < lengthJsonArr; i++)
                    {
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                        String name       = jsonChildNode.optString("name").toString();
                        String number     = jsonChildNode.optString("number").toString();
                        String date_added = jsonChildNode.optString("date_added").toString();


                        OutputData += " Name           : "+ name +" " + "Number      : "+ number +" "
                                + "Time                : "+ date_added +" "
                                +"-------------------------------------------------- ";


                    }

                    //jsonParsed.setText( OutputData );


                } catch (JSONException e) {

                    e.printStackTrace();
                }


            }
        }

    }
}