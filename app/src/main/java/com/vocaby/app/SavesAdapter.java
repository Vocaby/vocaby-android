package com.vocaby.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavesAdapter extends RecyclerView.Adapter<SavesAdapter.SavesViewHolder> {
    private final List<String> saves;
    private final Context ctx;
    private final OnItemTouchListener onItemTouchListener;
    private final DataManager dataManager;
    private final AlertDialog.Builder builder;
    private String token;

    public interface OnItemTouchListener {
        void onItemTouch(int position);
    }

    public SavesAdapter(Context ctx, List<String> saves, OnItemTouchListener onItemTouchListener, Activity activity) {
        this.saves = saves;
        this.onItemTouchListener = onItemTouchListener;
        this.ctx = ctx;
        dataManager = DataManager.getInstance(ctx);

        builder = new AlertDialog.Builder(activity);
    }

    @NonNull
    @Override
    public SavesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        CardView card = (CardView) inflater.inflate(R.layout.save_item, parent, false);
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.token_key), Context.MODE_PRIVATE);
        token = sharedPref.getString(ctx.getString(R.string.token_key), "");
        return new SavesViewHolder(card, onItemTouchListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SavesAdapter.SavesViewHolder holder, int position) {
        String word = saves.get(position);
        holder.savedWord.setText(word);
        holder.unsaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.setMessage("Are you sure you want to delete?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(token.isEmpty()) {
                            dataManager.deleteSave(word);
                            notifyDataSetChanged();
                        } else {
                            String url = ctx.getString(R.string.save_url);
                            RequestQueue q = Volley.newRequestQueue(ctx);
                            JsonObjectRequest jsonObjectRequest = makeDeleteRequest(url + word, word);
                            q.add(jsonObjectRequest);
                        }
                    }
                }).setNegativeButton("No", null);
                showAlertDialog(word);
            }
        });
    }

    private void showAlertDialog(String word) {
        AlertDialog alert = builder.create();
        alert.show();
        Button negativeButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTextColor(ctx.getColor(R.color.color_tertiary));

    }

    @Override
    public int getItemCount() {
        return saves.size();
    }

    public class SavesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView savedWord;
        OnItemTouchListener onItemTouchListener;
        ImageButton unsaveButton;
        public SavesViewHolder(@NonNull View itemView, OnItemTouchListener onItemTouchListener) {
            super(itemView);
            savedWord = itemView.findViewById(R.id.save_item);
            unsaveButton = itemView.findViewById(R.id.unsave_button);
            this.onItemTouchListener = onItemTouchListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemTouchListener.onItemTouch(getAdapterPosition());
        }
    }

    private JsonObjectRequest makeDeleteRequest(String url, String word) {
        return new JsonObjectRequest
                (Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dataManager.deleteSave(word);
                        notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.d("RESPONSE", "Error: " + error
                                    + "\nStatus Code " + error.networkResponse.statusCode
                                    + "\nData " + jsonError);
                        }

                        Toast.makeText(ctx, "Something went wrong while unsaving...", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.token_key), Context.MODE_PRIVATE);
                String token = sharedPref.getString(ctx.getString(R.string.token_key), "");
                Map<String, String> m = new HashMap<>();
                m.put("Content-Type", "application/json; charset=UTF-8");
                m.put("Vocaby-Api-Key",  ctx.getString(R.string.mobile_api_key));
                m.put("Authorization",  "Token " + token);
                return m;
            }
        };
    }
}
