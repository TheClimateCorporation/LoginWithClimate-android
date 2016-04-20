package com.climate.login;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginButton extends Button implements View.OnClickListener {

	private Context context;
	private String clientID = "";
	private String clientSecret = "";
	private LoginListener loginListener;

	public LoginButton(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public LoginButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public LoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		init();
	}

	private void init() {
		setBackgroundResource(R.drawable.loginbutton);
		setOnClickListener(this);
	}

	public void registerListener(LoginListener loginListener) {
		this.loginListener = loginListener;
	}

	public void setCredentials(String clientID, String clientSecret) {
		this.clientID = clientID;
		this.clientSecret = clientSecret;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onClick(View v) {
		WebView wv = new WebView(context);
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		final Dialog dialog = new Dialog(context);
		final ProgressDialog progressDialog = new ProgressDialog(context);


		progressDialog.setMessage("Loading...");
		progressDialog.show();
		wv.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				if (!url.startsWith("done:")) {
					progressDialog.hide();
					dialog.show();
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (!url.startsWith("done:")) {
					return false;
				} else {
					dialog.dismiss();
					progressDialog.show();

					Uri parse = Uri.parse(url);
					final String code = parse.getQueryParameter("code");
					Log.d("tcc", "got code " + code);

					RequestQueue queue = Volley.newRequestQueue(context);
					String tokenURL = "https://climate.com/api/oauth/token";

					StringRequest stringRequest = new StringRequest(Request.Method.POST, tokenURL,
							new Response.Listener<String>() {
								@Override
								public void onResponse(String response) {
									Log.d("tcc", response);
									progressDialog.hide();
									try {
										JSONObject session = new JSONObject(response);
										loginListener.onLogin(session);
										Log.d("tcc", session.toString(2));
									} catch (JSONException e) {
										loginListener.onError(e);
									}
								}
							},
							new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
									Log.d("tcc", new String(error.networkResponse.data));
									progressDialog.hide();
									loginListener.onError(error);
								}
							}) {

						@Override
						public Map<String, String> getHeaders() throws AuthFailureError {

							Map<String, String> headers = super.getHeaders();
							HashMap<String, String> map = new HashMap<>();
							map.putAll(headers);

							String credentials = clientID + ":" + clientSecret;
							String auth = "Basic "
									+ Base64.encodeToString(credentials.getBytes(),
									Base64.NO_WRAP);
							map.put("Authorization", auth);
							return map;
						}

						@Override
						public String getBodyContentType() {
							return "application/x-www-form-urlencoded;";
						}

						@Override
						protected String getParamsEncoding() {
							return "utf-8";
						}

						@Override
						protected Map<String, String> getParams() throws AuthFailureError {
							Map<String, String> params = new HashMap<>();
							params.put("grant_type", "authorization_code");
							params.put("scope", "user openid");
							params.put("redirect_uri", "done://");
							params.put("code", code);
							return params;
						}
					};
					queue.add(stringRequest);

					return true;
				}

			}
		});

		dialog.setContentView(wv);
		wv.loadUrl("https://climate.com/static/app-login/index.html?mobile=true&page=oidcauthn&response_type=code&redirect_uri=done://&scope=openid user&client_id=" + clientID);
	}

	public interface LoginListener {
		void onLogin(JSONObject session);
		void onError(Exception exception);
	}

}
