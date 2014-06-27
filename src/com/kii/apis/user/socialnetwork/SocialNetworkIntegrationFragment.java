package com.kii.apis.user.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kii.apis.Constants;
import com.kii.apis.R;
import com.kii.apis.ShowCodeActivity;
import com.kii.apis.Utils;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiSocialCallBack;
import com.kii.cloud.storage.social.KiiSocialConnect;
import com.kii.cloud.storage.social.twitter.KiiTwitterConnect;

/**
 * Fragment for Social Network Integration Page
 */
public class SocialNetworkIntegrationFragment extends Fragment {
    public static SocialNetworkIntegrationFragment newInstance() {
        SocialNetworkIntegrationFragment fragment = new SocialNetworkIntegrationFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.social_network_integration, container, false);

        int[] ids = {
                R.id.ButtonLoginWithFacebook, R.id.ButtonLinkFacebook, R.id.buttonUnlinkFacebook,
                R.id.ButtonLoginWithTwitter, R.id.buttonLinkTwitter, R.id.ButtonUnlinkTwitter

        };
        for (int id : ids) {
            root.findViewById(id).setOnClickListener(mClickListener);
        }
        return root;
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.ButtonLoginWithFacebook:
                onLoginWithFacebook();
                break;
            case R.id.ButtonLinkFacebook:
                onLinkFacebook();
                break;
            case R.id.buttonUnlinkFacebook:
                onUnlinkFacebook();
                break;
            case R.id.ButtonLoginWithTwitter:
                onLoginWithTwitter();
                break;
            case R.id.buttonLinkTwitter:
                onLinkTwitter();
                break;
            case R.id.ButtonUnlinkTwitter:
                onUnlinkTwitter();
                break;
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.default_showcode, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Activity activity = getActivity();
        if (activity == null) { return true; }

        switch (item.getItemId()) {
        case R.id.action_code:
            String title = getString(R.string.showcode_title, activity.getTitle());
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
            Intent intent = new Intent(activity, ShowCodeActivity.class);
            intent.putExtra(ShowCodeActivity.EXTRA_TITLE, title);
            intent.putExtra(ShowCodeActivity.EXTRA_GROUPS, group);
            intent.putExtra(ShowCodeActivity.EXTRA_CHILD, child);
            startActivity(intent);
            break;
        }
        return true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        activity.setTitle(R.string.social_network_integration_title);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KiiTwitterConnect.REQUEST_CODE) {
            Kii.socialConnect(KiiSocialConnect.SocialNetwork.TWITTER).respondAuthOnActivityResult(
                    requestCode,
                    resultCode,
                    data);
        }
        else {
            Kii.socialConnect(KiiSocialConnect.SocialNetwork.FACEBOOK).respondAuthOnActivityResult(
                    requestCode,
                    resultCode,
                    data);
        }
    }

    public void onLoginWithFacebook() {
        final Activity activity = getActivity();
        if (activity == null) { return; }

        KiiSocialConnect conn = Kii.socialConnect(KiiSocialConnect.SocialNetwork.FACEBOOK);
        conn.initialize(Constants.FB_APP_ID, null, null);
        conn.logIn(activity, null, new KiiSocialCallBack(){
            @Override
            public void onLoginCompleted(KiiSocialConnect.SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(activity, R.string.login_succ,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // Failure. handle error.
                    Toast.makeText(activity, R.string.login_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onLinkFacebook() {
        final Activity activity = getActivity();
        if (activity == null) { return; }

        if (!Utils.isCurrentLogined()) {
            Toast.makeText(activity, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            return;
        }
        KiiSocialConnect conn = Kii.socialConnect(KiiSocialConnect.SocialNetwork.FACEBOOK);
        conn.initialize(Constants.FB_APP_ID, null, null);
        conn.link(activity, null, new KiiSocialCallBack(){
            @Override
            public void onLinkCompleted(KiiSocialConnect.SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(activity, R.string.link_succ,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // Failure. handle error.
                    exception.printStackTrace();
                    Toast.makeText(activity, R.string.link_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onUnlinkFacebook() {
        final Activity activity = getActivity();
        if (activity == null) { return; }

        if (!Utils.isCurrentLogined()) {
            Toast.makeText(activity, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            return;
        }
        KiiSocialConnect conn = Kii.socialConnect(KiiSocialConnect.SocialNetwork.FACEBOOK);
        conn.initialize(Constants.FB_APP_ID, null, null);
        conn.unlink(activity, null, new KiiSocialCallBack(){
            @Override
            public void onUnLinkCompleted(KiiSocialConnect.SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(activity, R.string.unlink_succ,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // Failure. handle error.
                    Toast.makeText(activity, R.string.unlink_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onLoginWithTwitter() {
        final Activity activity = getActivity();
        if (activity == null) { return; }

        KiiSocialConnect connect = Kii.socialConnect(KiiSocialConnect.SocialNetwork.TWITTER);
        connect.initialize(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET, null);
        connect.logIn(activity, null, new KiiSocialCallBack(){
            @Override
            public void onLoginCompleted(KiiSocialConnect.SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(activity, R.string.login_succ,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // Failure. Handle error.
                    Toast.makeText(activity, R.string.login_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onLinkTwitter() {
        final Activity activity = getActivity();
        if (activity == null) { return; }

        if (!Utils.isCurrentLogined()) {
            Toast.makeText(activity, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            return;
        }
        KiiSocialConnect connect = Kii.socialConnect(KiiSocialConnect.SocialNetwork.TWITTER);
        connect.initialize(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET, null);
        connect.link(activity, null, new KiiSocialCallBack(){
            @Override
            public void onLinkCompleted(KiiSocialConnect.SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(activity, R.string.link_succ,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // Failure. Handle error.
                    Toast.makeText(activity, R.string.link_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onUnlinkTwitter() {
        final Activity activity = getActivity();
        if (activity == null) { return; }

        if (!Utils.isCurrentLogined()) {
            Toast.makeText(activity, R.string.need_to_login_first, Toast.LENGTH_LONG).show();
            return;
        }
        KiiSocialConnect connect = Kii.socialConnect(KiiSocialConnect.SocialNetwork.TWITTER);
        connect.initialize(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET, null);
        connect.unlink(activity, null, new KiiSocialCallBack(){
            @Override
            public void onUnLinkCompleted(KiiSocialConnect.SocialNetwork network, KiiUser user, Exception exception) {
                if (exception == null) {
                    // Success.
                    Toast.makeText(activity, R.string.unlink_succ,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // Failure. handle error.
                    Toast.makeText(activity, R.string.unlink_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
