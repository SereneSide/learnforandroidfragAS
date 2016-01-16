/*
 * Copyright (c) 2015 GPL by J.M.Goebel. Distributed under the GNU GPL v3.
 *
 * 08.06.2015
 *
 * This file is part of learnforandroid.
 *
 * learnforandroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  learnforandroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.liberty.android.fantastischmemo.downloader.quizlet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import org.de.jmg.learn.R;
import org.de.jmg.lib.lib;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;

//import roboguice.RoboGuice;
//import roboguice.activity.RoboActionBarActivity;

public class LoginQuizletActivity extends AppCompatActivity {
    static {
        //RoboGuice.setUseAnnotationDatabases(false);
    }
    public String AccessToken;
    public boolean blnUpload;
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (org.liberty.android.fantastischmemo.downloader.quizlet.lib.dlg == null)
        {
            try
            {
                org.liberty.android.fantastischmemo.downloader.quizlet.lib.dlg = new QuizletOAuth2AccessCodeRetrievalFragment();
                org.liberty.android.fantastischmemo.downloader.quizlet.lib.dlg.setAuthCodeReceiveListener(new OauthAccessCodeRetrievalFragment.AuthCodeReceiveListener()
                {
                    @Override
                    public void onAuthCodeReceived(String... codes)
                    {
                        if (codes != null && codes.length > 0)
                        {
                            AccessToken = codes[0];
                            new GetAccessTokenTask().execute(codes);
                        }
                        else
                        {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }

                    }

                    @Override
                    public void onAuthCodeError(String error)
                    {
                        lib.ShowMessage(LoginQuizletActivity.this, error, "");
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
QuizletOAuth2AccessCodeRetrievalFragment dlg;

                    @Override
                    public void onCancelled()
                    {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                });

                //dlg.show(this.getSupportFragmentManager(), "OauthAccessCodeRetrievalFragment");
            }
            catch (Exception ex)
            {
                AlertDialog.Builder A = new AlertDialog.Builder(this);
                A.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                A.setMessage(ex.getMessage());
                A.setTitle(this.getString(R.string.Error));
                AlertDialog dlg = A.create();
                dlg.show();
            }

        }
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            org.liberty.android.fantastischmemo.downloader.quizlet.lib.dlg.processCallbackUrl(uri.toString());
            finish();
        }
        else
        {
            blnUpload = intent.getBooleanExtra("upload",false);
            String url = org.liberty.android.fantastischmemo.downloader.quizlet.lib.dlg.getLoginUrl();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }


    }
    private class GetAccessTokenTask extends AsyncTask<String, Void, String[]> {

        private ProgressDialog progressDialog;

        private Exception backgroundTaskException = null;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginQuizletActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading));
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public String[] doInBackground(String... requests) {
            try {
                return org.liberty.android.fantastischmemo.downloader.quizlet.lib.getAccessTokens(requests);
            } catch (Exception e) {
                backgroundTaskException = e;
                return null;
            }
        }

        @Override
        public void onPostExecute(String[] accessTokens){
            progressDialog.dismiss();

            if (backgroundTaskException != null) {
                lib.ShowException(LoginQuizletActivity.this, backgroundTaskException);
                setResult(Activity.RESULT_CANCELED);
                finish();
            }

            if (accessTokens!=null && accessTokens.length == 2)
            {
                Intent intent = new Intent();
                intent.putExtra("AuthCode", AccessToken);
                intent.putExtra("user", accessTokens[1]);
                intent.putExtra("accessToken", accessTokens[0]);
                intent.putExtra("upload",blnUpload);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            else
            {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        }
    }

}