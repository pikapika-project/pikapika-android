package com.wamp42.pikapika.data;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.wamp42.pikapika.R;
import com.wamp42.pikapika.models.Coords;
import com.wamp42.pikapika.models.LoginData;
import com.wamp42.pikapika.models.PokemonLocation;
import com.wamp42.pikapika.models.PokemonToken;
import com.wamp42.pikapika.models.Provider;
import com.wamp42.pikapika.network.RestClient;
import com.wamp42.pikapika.utils.Debug;
import com.wamp42.pikapika.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class DataManager {

    public static String AUTH_URL = "https://android.clients.google.com/auth";

    private static DataManager dataManagerInstance;
    private RestClient restClient = new RestClient();

    public static DataManager getDataManager(){
        if(dataManagerInstance == null)
            dataManagerInstance = new DataManager();
        return dataManagerInstance;
    }

    //should be just one in all the flow
    private Callback mGoogleLoginCallback;
    private Context mContext;
    private String mUser;
    private String mPass;
    private String mProvider;

    private String tokenExpiredTime ="";

    public String getTokenExpiredTime() {
        return tokenExpiredTime;
    }

    /*public void login(Context context, String user, String pass, Location location, String loginType, Callback callback){
        Coords coords;
        if(location != null){
            coords = new Coords(location.getLatitude(),location.getLongitude());
        } else {
            //This shouldn't happened
            coords = new Coords(0.0,0.0);
        }
        PokemonLocation pokemonLocation = new PokemonLocation(coords);
        LoginData loginData = new LoginData(user,pass,loginType,pokemonLocation);
        //convert object to json
        String jsonInString = new Gson().toJson(loginData);
        //save locally
        PokemonHelper.saveDataLogin(context,jsonInString);
        //do the request
        restClient.postJson(jsonInString,"trainers/login",callback);
    }*/

    public void loginWithToken(Context context, String user, String token, String timeExpire , Location location, String loginType, Callback callback){

        PokemonLocation pokemonLocation;
        if(location == null){
            pokemonLocation = new PokemonLocation(0,0, 0);
        } else {
            pokemonLocation = new PokemonLocation(location.getLatitude(),location.getLongitude(), location.getAltitude());
        }
        LoginData loginData = new LoginData(user, new Provider("google",token,timeExpire), pokemonLocation);
        //convert object to json
        String jsonInString = new Gson().toJson(loginData);
        //do the request
        restClient.postJson(jsonInString,"trainers/login",callback);
        //save locally
        loginData.setPassword(mPass);
        jsonInString = new Gson().toJson(loginData);
        PokemonHelper.saveDataLogin(context,jsonInString);
        cleanTemps();
    }

    public void heartbeat(String token,String lat, String lng, Callback callback){
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", token);
        restClient.get("pokemons/"+lat+"/"+lng+"/heartbeat", params, callback);
    }

    private void cleanTemps(){
        mUser = "";
        mPass = "";
        mProvider = "";
    }

    public void oauthGoogle(String user, String pass, String provider, Callback callback, Context context){
        mGoogleLoginCallback = callback;
        mContext = context;
        mUser = user;
        mPass = pass;
        mProvider = provider;

        // Google Parts
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Locale locale = Locale.getDefault();
        String device_country = locale.getCountry();
        String country_code;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            country_code = tm.getSimCountryIso();
        } catch(Exception e) {
            country_code = device_country;
        }
        if (country_code == null)country_code = device_country;

        String language = locale.getLanguage();
        String skdVersion = Build.VERSION.SDK_INT + "";

        RequestBody formBody = new FormBody.Builder()
                .add("accountType", "HOSTED_OR_GOOGLE")
                .add("Email", user)
                .add("has_permission", "1")
                .add("add_account", "1")
                .add("Passwd", pass)
                .add("service", "ac2dm")
                .add("source", "android")
                .add("androidId", android_id)
                .add("device_country", country_code)
                .add("operatorCountry", country_code)
                .add("lang", language)
                .add("sdk_version", skdVersion)
                .build();

        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(formBody)
                .build();
        restClient.getClient().newCall(request).enqueue(googleOAuthCallback);
    }

    private void oauthGoogleNiantic(String email,String master_token, Callback callback, Context context){

        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Locale locale = Locale.getDefault();
        String device_country = locale.getCountry();
        String country_code;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            country_code = tm.getSimCountryIso();
        } catch(Exception e) {
            country_code = device_country;
        }
        if (country_code == null)country_code = device_country;

        String language = locale.getLanguage();
        String skdVersion = Build.VERSION.SDK_INT + "";

        String oauth_service = "audience:server:client_id:848232511240-7so421jotr2609rmqakceuu1luuq0ptb.apps.googleusercontent.com";
        String app = "com.nianticlabs.pokemongo";
        String client_sig = "321187995bc7cdc2b5fc91b11a96e2baa8602c62";

        RequestBody formBody = new FormBody.Builder()
                .add("accountType", "HOSTED_OR_GOOGLE")
                .add("Email", email)
                .add("EncryptedPasswd", master_token)
                .add("has_permission", "1")
                .add("service", oauth_service)
                .add("source", "android")
                .add("androidId", android_id)
                .add("app", app)
                .add("client_sig", client_sig)
                .add("device_country", country_code)
                .add("operatorCountry", country_code)
                .add("lang", language)
                .add("sdk_version", skdVersion)
                .build();

        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(formBody)
                .build();
        restClient.getClient().newCall(request).enqueue(callback);
    }

    /******* Callbacks *******/


    final Callback googleOAuthCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(mGoogleLoginCallback != null)
                mGoogleLoginCallback.onFailure(call,e);
            mGoogleLoginCallback = null;
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //check first if the request was ok
            if (response.code() == 200){
                String jsonStr = response.body().string();
                if(!jsonStr.isEmpty()) {
                    Debug.d(jsonStr);
                    try {
                        HashMap<String,String> formMap = Utils.getMapFromForm(jsonStr);
                        if(formMap.containsKey("Token")){
                            //request auth with niantic
                            String user = mUser;
                            oauthGoogleNiantic(user,formMap.get("Token"),nianticOAuthCallback,mContext);
                            return;
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                response.body().close();
            }
            if(mGoogleLoginCallback != null)
                mGoogleLoginCallback.onResponse(call,response);
            mGoogleLoginCallback = null;
        }
    };

    final Callback nianticOAuthCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(mGoogleLoginCallback != null)
                mGoogleLoginCallback.onFailure(call,e);
            mGoogleLoginCallback = null;
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //check first if the request was ok
            if (response.code() == 200){
                String jsonStr = response.body().string();
                if(!jsonStr.isEmpty()) {
                    Debug.d(jsonStr);
                    try {
                        HashMap<String,String> formMap = Utils.getMapFromForm(jsonStr);
                        if(formMap.containsKey("Auth")){
                            tokenExpiredTime = formMap.get("Expiry");
                            //request auth with niantic
                            String user = mUser;
                            String provider = mProvider;
                            loginWithToken(
                                    mContext,
                                    user,
                                    formMap.get("Auth"),
                                    tokenExpiredTime,
                                    PokemonHelper.lastLocation,
                                    provider,
                                    mGoogleLoginCallback);
                            return;
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                response.body().close();
            }
            if(mGoogleLoginCallback != null)
                mGoogleLoginCallback.onFailure(call,new IOException("Code error "+response.code()));
            mGoogleLoginCallback = null;
        }
    };

}
