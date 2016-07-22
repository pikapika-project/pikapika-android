package com.wamp42.pikapika.data;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.GoogleMap;
import com.wamp42.pikapika.R;
import com.wamp42.pikapika.models.PokemonResult;
import com.wamp42.pikapika.models.PokemonToken;

import java.util.HashMap;
import java.util.List;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class PokemonHelper {
    final public static String GOOGLE_PROVIDER = "google";
    final public static String PTC_PROVIDER = "ptc";
    final public static String USER_PARAMETER = "user";
    final public static String PASS_PARAMETER = "pass";
    final public static String PROVIDER_PARAMETER = "provider";
    final public static String TOKEN_PARAMETER = "accessToken";
    final public static String EXPIRE_TIME_PARAMETER = "expire_time";
    final public static String CURRENT_TIME_PARAMETER = "provider";

    //share the marker in order to
    static public HashMap<String,String> markersMap = new HashMap<>();
    //keep the pokemon result objects in memory
    static public List<PokemonResult> pokemonResultList;
    //share the lastValidLocation;
    static public Location lastLocation;

    static public void drawPokemonResult(Context context, GoogleMap map, List<PokemonResult> locationList){
        //clear markers
        map.clear();
        //clear makers map
        PokemonHelper.markersMap.clear();
        for(PokemonResult location:locationList){
            //draw each pokemon mark
            location.drawMark(map, context);
        }
    }

    public static ProgressDialog showLoading(Context context) {
        return ProgressDialog.show(context, context.getString(R.string.please_wait), context.getString(R.string.loading_data), true);
    }

    public static void showAlert(final Activity activity, final String title, final String body) {
        activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(body)
                            .setTitle(title);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
    }

    public static void saveUserData(Context context,String user, String pass,String provider){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USER_PARAMETER, user);
        editor.putString(PASS_PARAMETER, pass);
        editor.putString(PROVIDER_PARAMETER, provider);
        editor.apply();
    }

    public static void saveTokenData(Context context,String token, String expireTime){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(TOKEN_PARAMETER, token);
        editor.putString(EXPIRE_TIME_PARAMETER, expireTime);
        editor.apply();
    }

    public static void saveTokenData(Context context, PokemonToken pokemonToken){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(TOKEN_PARAMETER, pokemonToken.getAccessToken());
        editor.putString(EXPIRE_TIME_PARAMETER, pokemonToken.getExpire_time());
        editor.apply();
    }

    public static  PokemonToken getTokenFromData(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String token = sharedPref.getString(PokemonHelper.TOKEN_PARAMETER,"");
        String time = sharedPref.getString(PokemonHelper.EXPIRE_TIME_PARAMETER,"");
        return new PokemonToken(token,time,"");
    }
}