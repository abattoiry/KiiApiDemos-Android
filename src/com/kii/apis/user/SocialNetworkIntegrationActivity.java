package com.kii.apis.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kii.apis.R;
import com.kii.apis.Constants;
import com.kii.apis.ShowCodeActivity;
import com.kii.apis.Utils;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiSocialCallBack;
import com.kii.cloud.storage.social.KiiSocialConnect;
import com.kii.cloud.storage.social.KiiSocialConnect.SocialNetwork;
import com.kii.cloud.storage.social.twitter.KiiTwitterConnect;

public class SocialNetworkIntegrationActivity extends Activity {

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.social_network_integration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.default_showcode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_code:
                String title = getString(R.string.showcode_title, getTitle());
                String[] group = new String[] {
                        getString(R.string.login_with_facebook_account),
                        getString(R.string.linking_a_kii_account_to_a_facebook_account),
                        getString(R.string.unlinking_a_kii_account_from_a_facebook_account),
                        getString(R.string.login_with_twitter_account),
                        getString(R.string.linking_a_kii_account_to_a_twitter_account),
                        getString(R.string.unlinking_a_kii_account_from_a_twitter_account)
                };
                String[] child = new String[] {
                        "Activity activity = this.getActivity();\r\n"
                                + "KiiSocialConnect conn = Kii.socialConnect(SocialNetwork.FACEBOOK);\r\n"
                                + "conn.initialize(\"__FB_APP_ID__\", null, null);\r\n"
                                + "conn.logIn(activity, null, new KiiSocialCallBack(){\r\n"
                                + "    @Override\r\n"
                                + "    public void onLoginCompleted(SocialNetwork network, KiiUser user, Exception exception) {\r\n"
                                + "        if (exception == null) {\r\n"
                                + "            // Success.\r\n"
                                + "        } else {\r\n"
                                + "            // Failure. Handle error.\r\n"
                                + "        }\r\n"
                                + "    }\r\n"
                                + "});",
                        "Activity activity = this.getActivity();\r\n"
                                + "KiiSocialConnect conn = Kii.socialConnect(SocialNetwork.FACEBOOK);\r\n"
                                + "conn.initialize(\"__FB_APP_ID__\", null, null);\r\n"
                                + "conn.link(activity, null, new KiiSocialCallBack(){\r\n"
                                + "    @Override\r\n"
                                + "    public void onLinkCompleted(SocialNetwork network, KiiUser user, Exception exception) {\r\n"
                                + "        if (exception == null) {\r\n"
                                + "            // Success.\r\n"
                                + "        } else {\r\n"
                                + "            // Failure. Handle error.\r\n"
                                + "        }\r\n"
                                + "    }\r\n"
                                + "});",
                        "Activity activity = this.getActivity();\r\n"
                                + "KiiSocialConnect conn = Kii.socialConnect(SocialNetwork.FACEBOOK);\r\n"
                                + "conn.initialize(\"__FB_APP_ID__\", null, null);\r\n"
                                + "conn.unlink(activity, null, new KiiSocialCallBack(){\r\n"
                                + "    @Override\r\n"
                                + "    public void onUnLinkCompleted(SocialNetwork network, KiiUser user, Exception exception) {\r\n"
                                + "        if (exception == null) {\r\n"
                                + "            // Success.\r\n"
                                + "        } else {\r\n"
                                + "            // Failure. Handle error.\r\n"
                                + "        }\r\n"
                                + "    }\r\n"
                                + "});",
                        "Activity activity = this.getActivity();\r\n"
                                + "KiiSocialConnect connect = Kii.socialConnect(SocialNetwork.TWITTER);\r\n"
                                + "connect.initialize(\"__TWITTER_CONSUMER_KEY__\", \"__TWITTER_CONSUMER_SECRET__\", null);\r\n"
                                + "connect.logIn(activity, null, new KiiSocialCallBack(){\r\n"
                                + "    @Override\r\n"
                                + "    public void onLoginCompleted(SocialNetwork network, KiiUser user, Exception exception) {\r\n"
                                + "        if (exception == null) {\r\n"
                                + "            // Success.\r\n"
                                + "        } else {\r\n"
                                + "            // Failure. Handle error.\r\n"
                                + "        }\r\n"
                                + "    }\r\n"
                                + "});",
                        "Activity activity = this.getActivity();\r\n"
                                + "KiiSocialConnect connect = Kii.socialConnect(SocialNetwork.TWITTER);\r\n"
                                + "connect.initialize(\"__TWITTER_CONSUMER_KEY__\", \"__TWITTER_CONSUMER_SECRET__\", null);\r\n"
                                + "connect.link(activity, null, new KiiSocialCallBack(){\r\n"
                                + "    @Override\r\n"
                                + "    public void onLinkCompleted(SocialNetwork network, KiiUser user, Exception exception) {\r\n"
                                + "        if (exception == null) {\r\n"
                                + "            // Success.\r\n"
                                + "        } else {\r\n"
                                + "            // Failure. Handle error.\r\n"
                                + "        }\r\n"
                                + "    }\r\n"
                                + "});" ,
                        "Activity activity = this.getActivity();\r\n"
                                + "KiiSocialConnect connect = Kii.socialConnect(SocialNetwork.TWITTER);\r\n"
                                + "connect.initialize(\"__TWITTER_CONSUMER_KEY__\", \"__TWITTER_CONSUMER_SECRET__\", null);\r\n"
                                + "connect.unlink(activity, null, new KiiSocialCallBack(){\r\n"
                                + "    @Override\r\n"
                                + "    public void onUnLinkCompleted(SocialNetwork network, KiiUser user, Exception exception) {\r\n"
                                + "        if (exception == null) {\r\n"
                                + "            // Success.\r\n"
                                + "        } else {\r\n"
                                + "            // Failure. Handle error.\r\n"
                                + "        }\r\n"
                                + "    }\r\n"
                                + "});" 
                                };
                Intent intent = new Intent(this, ShowCodeActivity.class);
                intent.putExtra(ShowCodeActivity.EXTRA_TITLE, title);
                intent.putExtra(ShowCodeActivity.EXTRA_GROUPS, group);
                intent.putExtra(ShowCodeActivity.EXTRA_CHILD, child);
                startActivity(intent);
                break;
        }
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);  
        if (requestCode == KiiTwitterConnect.REQUEST_CODE) {
            Kii.socialConnect(SocialNetwork.TWITTER).respondAuthOnActivityResult(
            	    requestCode,
            	    resultCode,
            	    data);
        }
        else {
            Kii.socialConnect(SocialNetwork.FACEBOOK).respondAuthOnActivityResult(
            	    requestCode,
            	    resultCode,
            	    data);
        }
    }

    public void onLoginWithFacebook(View v) { 
    	
        KiiSocialConnect conn = Kii.socialConnect(SocialNetwork.FACEBOOK);
        conn.initialize(Constants.FB_APP_ID, null, null);
        conn.logIn(this, null, new KiiSocialCallBack(){
            @Override
            public void onLoginCompleted(SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.login_succ, 
                    	    Toast.LENGTH_SHORT).show();
                }
                else {
                    // Failure. handle error.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.login_failed, 
                    	    Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    public void onLinkFacebook(View v) {
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            return;
        }
        KiiSocialConnect conn = Kii.socialConnect(SocialNetwork.FACEBOOK);
        conn.initialize(Constants.FB_APP_ID, null, null);
        conn.link(this, null, new KiiSocialCallBack(){
            @Override
            public void onLinkCompleted(SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.link_succ, 
                    	    Toast.LENGTH_SHORT).show();
                } 
                else {
                    // Failure. handle error.
                    exception.printStackTrace();
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.link_failed, 
                    	    Toast.LENGTH_SHORT).show();
                }
            }
        });    	
    }
    
    public void onUnlinkFacebook(View v) {
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            return;
        }
        KiiSocialConnect conn = Kii.socialConnect(SocialNetwork.FACEBOOK);
        conn.initialize(Constants.FB_APP_ID, null, null);
        conn.unlink(this, null, new KiiSocialCallBack(){
            @Override
            public void onUnLinkCompleted(SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.unlink_succ, 
                    	    Toast.LENGTH_SHORT).show();
                } 
                else {
                    // Failure. handle error.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.unlink_failed, 
                    	    Toast.LENGTH_SHORT).show();
                }
            }
        });    	
    }
    
    public void onLoginWithTwitter(View v) {
    	
        KiiSocialConnect connect = Kii.socialConnect(SocialNetwork.TWITTER);
        connect.initialize(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET, null);
        connect.logIn(this, null, new KiiSocialCallBack(){
            @Override
            public void onLoginCompleted(SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.login_succ, 
                    	    Toast.LENGTH_SHORT).show();
                } 
                else {
                    // Failure. Handle error.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.login_failed, 
                    	    Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    public void onLinkTwitter(View v) {
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            return;
        }
        KiiSocialConnect connect = Kii.socialConnect(SocialNetwork.TWITTER);
        connect.initialize(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET, null);
        connect.link(this, null, new KiiSocialCallBack(){
            @Override
            public void onLinkCompleted(SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.link_succ, 
                    	    Toast.LENGTH_SHORT).show();
                } 
                else {
                    // Failure. Handle error.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.link_failed, 
                    	    Toast.LENGTH_SHORT).show();
                }
            }
        });    	
    }
    
    public void onUnlinkTwitter(View v) {
        if (!Utils.isCurrentLogined()) {
            Toast.makeText(this, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            return;
        }
        KiiSocialConnect connect = Kii.socialConnect(SocialNetwork.TWITTER);
        connect.initialize(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET, null);
        connect.unlink(this, null, new KiiSocialCallBack(){
            @Override
            public void onUnLinkCompleted(SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.unlink_succ, 
                    	    Toast.LENGTH_SHORT).show();
                }
                else {
                    // Failure. handle error.
                    Toast.makeText(SocialNetworkIntegrationActivity.this, R.string.unlink_failed, 
                    	    Toast.LENGTH_SHORT).show();
                }
            }
        });    	
    }
}