package com.example.ukyan.httpputtest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    URL url;
    int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        // 接続先のURLを指定してHTTP PUT実行
        int mission_id = 3;
        status = 2; //ステータス値の設定(本来はボタンが押されたタイミングで)
        String card_id = "ghthetfe";
        try {
            url = new URL("http://test-ukyankyan.c9users.io/api/missions/"+ mission_id);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        new HttpPUTTask().execute();
    }

    // AsyncTaskのサブクラスとして、バックグラウンドでHTTP POSTを指定、ヘッダでPUTにオーバーライドしてTextViewに表示するタスクを定義
    //<Void, Void, String>:<入力, 過程, 結果>の型→<doInBackgroundの引数型, doInBackgroundのメソッドの返り値型, onPostExecuteの入力値型>
    //頭の型をVoidにしてやることで外部の変数の読み出しを可能にしていることに注意。
    class HttpPUTTask extends AsyncTask<Void, Void, String> {
        // HttpURLConnectionを使ったデータ取得 (バックグラウンド)
        @Override
        protected String doInBackground(Void... params) {
            String result = "";
            String token = "9566714722251566337d4a4701de6153"; //下記コメント参照
            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection(); //メインスレッドで設定したURLをここで指定

                //送信処理部分
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST"); //GETかPOSTしか指定できないだと…！？じゃあどっちかと言ったらPOSTで
                urlConnection.setRequestProperty("Content-type", "application/json"); //送信するものをJSON形式にするよ(※忘れたら駄目)
                urlConnection.setRequestProperty("X-Http-Method-Override", "put"); //さっきPOSTだって言ったけど本当はやっぱPUTです
                //本番ではこれも設定、現時点ではまだAPIに認証付けてない…
                //urlConnection.setRequestProperty("Authorization", "Token token="+ token);

                JSONObject json = new JSONObject(); //送信用json
                try {
                    json.put("status", status); // {"status":2}の作成、複数属性があるならこの命令を複数並べる
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream()); //送信内容を入れる変数wrの初期化
                wr.write(String.valueOf(json)); //wrにjsonを文字列化したものをバッファに突っ込む
                wr.flush(); //バッファ内容を送信
                wr.close(); //Writer系メソッドは最後にclose()しないと駄目

                //返り値の受信処理部分
                if(urlConnection.getInputStream() != null) {
                    result = IOUtils.toString(urlConnection.getInputStream());
                    Log.d("result", result);
                } else
                    Log.e("error","result is null");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }

        // 返り値取得結果のTextViewへの表示 (UIスレッド)
        @Override
        protected void onPostExecute(String response) {
            Log.d("最後",response);
            if(response != null)
                MainActivity.this.textView.setText(response);
        }
    }
}
