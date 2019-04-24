package br.org.catolicasc.pokemon;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.json.JSONObject;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Button option1;
    private Button option2;
    private Button option3;
    private Button option4;
    private TextView hitAndMiss;
    private ImageView imageView;
    private ProgressBar progressBar;
    private AlertDialog alert;


    private ArrayList<Pokemon> pokemonList = new ArrayList<Pokemon>();
    private int hits;
    private int misses;


    protected String rightPokemonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        hitAndMiss = findViewById(R.id.hitAndMiss);
        progressBar = findViewById(R.id.progressBar2);


        View.OnClickListener listenerForRightChoice = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String buttonValue = b.getText().toString();
                if (checkForRightPokemon(buttonValue, rightPokemonName)) {
                    hits++;
                    updateScore();
                    getNewPokemon();
                } else {
                    misses++;
                    String title = "Errou!";
                    String msg = "Você errou o nome do pokemon. O nome correto era: " + rightPokemonName + ".";
                    msg += " Total de acertos: " + hits;
                    showAlert(title, msg);
                }
            }
        };

        option1.setOnClickListener(listenerForRightChoice);
        option2.setOnClickListener(listenerForRightChoice);
        option3.setOnClickListener(listenerForRightChoice);
        option4.setOnClickListener(listenerForRightChoice);




        // Try get the data from web
        try {
            JSONObject pokemonJson = readJson("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json");
            JSONArray list = pokemonJson.getJSONArray("pokemon");
            JSONObject jsonLineItem;
            String name, imgUrl;
            int id, num;

            for (int i = 0; i < list.length(); i++) {
                jsonLineItem = (JSONObject) list.get(i);
                id = jsonLineItem.getInt("id");
                num = jsonLineItem.getInt("num");
                name = jsonLineItem.getString("name");
                imgUrl = jsonLineItem.getString("img");
                pokemonList.add(new Pokemon(id, num, name, imgUrl));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        getNewPokemon();
    }

    private void showAlert(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(MainActivity.this, "Reiniciando o game", Toast.LENGTH_SHORT).show();
                hits = 0;
                updateScore();
                getNewPokemon();
            }
        });
        alert = builder.create();
        alert.show();
    }

    private boolean checkForRightPokemon(String name, String rightName) {
        return name.equals(rightName);
    }

    private void updateScore() {
        String txt = "Acertos: " + hits + "  Erros: " + misses;
        hitAndMiss.setText(txt);
    }

    private void getNewPokemon() {

        imageView = findViewById(R.id.imageView);

        String imgUrl="";
        int nAleatorio = 0;

        Random num = new Random();
        nAleatorio = num.nextInt(pokemonList.size());
        imgUrl = pokemonList.get(nAleatorio).getImgUrl();
        rightPokemonName = pokemonList.get(nAleatorio).getName();

        ImageDownloader imageDownloader = new ImageDownloader();

        try {
            Bitmap imagem = imageDownloader.execute(imgUrl).get();
            int nh = (int) ( imagem.getHeight() * (512.0 / imagem.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(imagem, 480, nh, true);
            imageView.setImageBitmap(scaled);
        } catch (Exception e) {
            Log.e(TAG, "downloadImagem: Impossível baixar imagem"
                    + e.getMessage());
        }

        ArrayList<Pokemon> list = new ArrayList<Pokemon>(4);
        list.add(pokemonList.get(nAleatorio)); // Add o primeiro q é o certo

        Pokemon poke;
        int i = 0;
        while (i < 3) {
            nAleatorio = num.nextInt(pokemonList.size());
            poke = pokemonList.get(nAleatorio);
            if (list.contains(poke)) continue;

            list.add(poke);
            i++;
        }

        Collections.shuffle(list);

        option1.setText(list.get(0).getName());
        option2.setText(list.get(1).getName());
        option3.setText(list.get(2).getName());
        option4.setText(list.get(3).getName());

    }

    private JSONObject readJson(String url) {
        JsonTask readPokemon = new JsonTask();

        try {
            String data = readPokemon.execute(url).get();
            JSONObject pokemonJson = new JSONObject(data);
            return pokemonJson;
        } catch (Exception e) {
            Log.e(TAG, "readJson: Erro buscar json: " + e.getMessage());
        }

        return null;
    }


    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int resposta = connection.getResponseCode();

                if (resposta != HttpURLConnection.HTTP_OK) { // se resposta não foi OK
                    if (resposta == HttpURLConnection.HTTP_MOVED_TEMP  // se for um redirect
                            || resposta == HttpURLConnection.HTTP_MOVED_PERM
                            || resposta == HttpURLConnection.HTTP_SEE_OTHER) {
                        // pegamos a nova URL e abrimos nova conexão!
                        String novaUrl = connection.getHeaderField("Location");
                        connection = (HttpURLConnection) new URL(novaUrl).openConnection();
                    }
                }
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: Erro ao baixar imagem"
                        + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
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

                while ((line = reader.readLine()) != null) buffer.append(line+"\n");

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
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
