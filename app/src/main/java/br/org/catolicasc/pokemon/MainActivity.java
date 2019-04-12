package br.org.catolicasc.pokemon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import org.json.JSONObject;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private JSONObject  pokemonJson;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readJson();



        try {
            System.out.println(pokemonJson.getJSONArray("pokemon"));
            JSONArray list = pokemonJson.getJSONArray("pokemon");
            System.out.println(list.length());
            Random num = new Random();
            int nAleatorio = num.nextInt(list.length());
            JSONObject jsonLineItem = (JSONObject) list.get(nAleatorio);
            String imgUrl = jsonLineItem.getString("img");
            System.out.println(imgUrl);


            imageView = findViewById(R.id.imageView);

            ImageDownloader imageDownloader = new ImageDownloader();

            imgUrl = imgUrl.replace("http", "https");
            try {
                // baixar a imagem da internet
                Bitmap imagem = imageDownloader.execute(imgUrl).get();
                // atribuir a imagem ao imageView
                imageView.setImageBitmap(imagem);
            } catch (Exception e) {
                Log.e(TAG, "downloadImagem: Impossível baixar imagem"
                        + e.getMessage());
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }


    }

    private void readJson() {
        String url = "https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json";
        JsonTask readPokemon = new JsonTask();

        try {
            String data = readPokemon.execute(url).get();
            pokemonJson = new JSONObject(data);
//            J
//            System.out.println(pokemonJson);
        } catch (Exception e) {
            Log.e(TAG, "readJson: Erro buscar json: " + e.getMessage());
        }
    }


    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: Erro ao baixar imagem"
                        + e.getMessage());
            }

            return null;
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Aguarde...");
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
//                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            System.out.println(result);
        }
    }
}
