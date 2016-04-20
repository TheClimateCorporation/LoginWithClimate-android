package com.climate.loginapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.climate.login.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LoginButton.LoginListener {

	private static final String TAG = "tcc";
	private TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		textView = (TextView)findViewById(R.id.textView);
		LoginButton loginButton = (LoginButton) findViewById(R.id.login);
		loginButton.registerListener(this);
		loginButton.setCredentials("dpcniksl1rb6mo", "h4p9u356pvqcpmfj56h58o60l9");
	}

	@Override
	public void onLogin(final JSONObject session) {
		Log.d(TAG, "onLogin");

		JsonObjectRequest jsObjRequest = new JsonObjectRequest
				(Request.Method.GET, "https://hack.climate.com/api/fields", null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "fields: ");
						try {
							Log.d(TAG, response.toString(2));
							JSONArray fields = response.optJSONArray("fields");
							StringBuilder stringBuilder = new StringBuilder("Fields:\n\n");
							for(int i = 0; i < fields.length(); ++i) {
								stringBuilder.append(fields.optJSONObject(i).optString("name"));
								stringBuilder.append("\n");
							}
							textView.setText(stringBuilder.toString());

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d(TAG, "onErrorResponse: " + error.getMessage());
					}
				}) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {

				Map<String, String> headers = super.getHeaders();
				HashMap<String, String> map = new HashMap<>();
				map.putAll(headers);
				String auth = null;
				auth = "Bearer " + session.opt("access_token");
				map.put("Authorization", auth);
				return map;
			}
		};
		RequestQueue queue = Volley.newRequestQueue(this);
		queue.add(jsObjRequest);
	}

	@Override
	public void onError(Exception exception) {
		Log.d(TAG, "onError");
	}
}
