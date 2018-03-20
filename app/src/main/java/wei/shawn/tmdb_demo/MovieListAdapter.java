package wei.shawn.tmdb_demo;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieListAdapter extends BaseAdapter {

    List<JSONObject> movies = new ArrayList<>();
    List<Bitmap> bitmaps = new ArrayList<>();
    Map<Integer, String> genreMap = new HashMap<>();
    int currentPage = 1;
    int totalPages = 0;
    int tabIndex;

    public MovieListAdapter(int index) {
        // Index: 0 = Now Playing, 1 = Upcoming
        tabIndex = index;
        String url;
        if (index == 0)
            url = "https://api.themoviedb.org/3/movie/now_playing?api_key=2c38af8352620caae9a9f46a221a847c&language=en-US&page=1";
        else
            url = "https://api.themoviedb.org/3/movie/upcoming?api_key=2c38af8352620caae9a9f46a221a847c&language=en-US&page=1";
        getGenreMap();
        getMoviesFromUrl(url);
    }

    private void getMoviesFromUrl(String url) {
        String json = getJsonStringFromUrl(url);

        try {
            // Get JSONObjects from the json string
            JSONObject jsonObj = new JSONObject(json);
            JSONArray results = jsonObj.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                movies.add(results.getJSONObject(i));
            }

            // Get movie posters' bitmaps from poster paths
            for (int i = 0; i < results.length(); i++) {
                String posterPath = results.getJSONObject(i).get("poster_path").toString();
                if (posterPath != "null")
                    bitmaps.add(BitmapFactory.decodeStream((InputStream) new URL("https://image.tmdb.org/t/p/w185" + posterPath).getContent()));
                else
                    bitmaps.add(null);
            }

            totalPages = jsonObj.getInt("total_pages");

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
        notifyDataSetChanged();
    }

    private void getGenreMap() {
        String json = getJsonStringFromUrl("https://api.themoviedb.org/3/genre/movie/list?api_key=2c38af8352620caae9a9f46a221a847c&language=en-US");

        try {
            // Get map from json string
            JSONObject jsonObj = new JSONObject(json);
            JSONArray genres = jsonObj.getJSONArray("genres");
            for (int i = 0; i < genres.length(); i++) {
                genreMap.put(genres.getJSONObject(i).getInt("id"), genres.getJSONObject(i).getString("name"));
            }
        } catch (JSONException e) {
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    private static String getJsonStringFromUrl(String url) {
        try {
            // Get movie list
            URL u = new URL(url);
            String json = "";
            HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = stringBuilder.toString();
                return json;
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
            } finally {
                urlConnection.disconnect();
            }
        }
        catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
        return "";
    }

    private void loadMovieItem(View movieItem, int position) {
        try {
            ((ImageView) movieItem.findViewById(R.id.poster)).setImageBitmap(bitmaps.get(position));
            ((TextView) movieItem.findViewById(R.id.title)).setText(movies.get(position).get("title").toString());
            ((TextView) movieItem.findViewById(R.id.popularity)).setText("Popularity: " + movies.get(position).get("popularity").toString());
            JSONArray genreArray = movies.get(position).getJSONArray("genre_ids");
            String genres = "";
            for (int i = 0; i < genreArray.length(); i++) {
                if (genres != "")
                    genres += ", ";
                genres += genreMap.get(genreArray.getInt(i));
            }
            ((TextView) movieItem.findViewById(R.id.genres)).setText("Genres: " + genres);
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    private void getMovieFromNextPage() {
        currentPage++;
        String url;
        if (tabIndex == 0)
            url = "https://api.themoviedb.org/3/movie/now_playing?api_key=2c38af8352620caae9a9f46a221a847c&language=en-US&page=";
        else
            url = "https://api.themoviedb.org/3/movie/upcoming?api_key=2c38af8352620caae9a9f46a221a847c&language=en-US&page=";
        url += Integer.toString(currentPage);
        getMoviesFromUrl(url);
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Object getItem(int position) {
        return movies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.movie_item, null);
        }
        loadMovieItem(convertView, position);

        // Update list if scrolled to the button of the page
        if (position == movies.size() - 1 && currentPage < totalPages)
            getMovieFromNextPage();
        return convertView;
    }
}
