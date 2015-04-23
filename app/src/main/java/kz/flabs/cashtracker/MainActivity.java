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
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

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

    public class SendLoginRequest extends AsyncTask<String, Integer, Integer> {
        Integer status;
        @Override
        protected Integer doInBackground(String... params) {
            try {
                HttpGet httpget = new HttpGet("http://172.16.250.64:38800/Login");
                HttpClient httpclient = new DefaultHttpClient();
                httpget.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(params[0], params[1]),"UTF-8", false));
                HttpResponse response = httpclient.execute(httpget);
                status = response.getStatusLine().getStatusCode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return status;
        }

        protected void onPostExecute(Integer status) {
            String status_i = status + " ";
            android.util.Log.d("error", status_i);
            if (status == 401) {
                android.util.Log.d("error", "Auth failed");
                showAlert("Неверное имя пользователя или пароль");
            }
            if (status == 200) {
                android.util.Log.d("info", "Auth ok");
                Intent intent = new Intent(MainActivity.this, docs_page.class);
                startActivity(intent);
            }
        }
    }
}
