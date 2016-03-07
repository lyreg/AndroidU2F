package org.esec.mcg.androidu2f;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.esec.mcg.androidu2f.client.op.U2FServerRequest;
import org.esec.mcg.androidu2f.msg.U2FIntentType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EnrollFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EnrollFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EnrollFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final int REG_ACTIVITY_RES_1 = 1;

    private String username;
    private String password;

    private String registerRequest = "{\"authenticateRequests\": [], \n" +
            "\"registerRequests\": [{\"challenge\": \"9s80ruHc6q9shJM5WLfOmz-ejb_Rm8dmWCnOvgZ2ovw\", \"version\": \"U2F_V2\", \"appId\": \"http://localhost:8081\"}]}";

    /**
     * Client's clientRegister operation.
     */
    private U2FServerRequest serverRequestMessage = new U2FServerRequest();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param username Parameter 1.
     * @param password Parameter 2.
     * @return A new instance of fragment EnrollFragment.
     */
    public static EnrollFragment newInstance(String username, String password) {
        EnrollFragment fragment = new EnrollFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        args.putString(PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    public EnrollFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(USERNAME);
            password = getArguments().getString(PASSWORD);
        }

        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", username);
        parameters.put("password", password);
        parameters.put("version", "U2F_V2");

        new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
////                    final String response = HTTP.post(new URL("https://demo.yubico.com/wsapi/u2f/enroll"), parameters);
////                    final String response = HTTP.post(new URL("http://192.168.1.140:8080/fidouaf/v1/history"), parameters);
////                    final String response = HTTP.get(new URL("http://192.168.1.140:8080/fidouaf/v1/history"));
////                    final String response = HTTP.get(new URL("http://openidconnect.ebay.com/fidouaf/v1/public/regRequest/yg"));
////                    final String response = HTTP.get(new URL("http://192.168.1.128:8000"));
//
//                    LogUtils.d(response);
//                } catch (IOException e) {
//
//                }
                // non-normative, defined according to UAF protocol.
                Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                i.addCategory("android.intent.category.DEFAULT");
                i.setType("application/fido.u2f_client+json");

                // Get the U2F Server's enroll url
                SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String endPoint = pf.getString("server_endpoint", "http://192.168.1.101:8000");
                String enrollPoint = pf.getString("enroll", "/enroll");

                final String response;
                try {
                    response = getServerRequest(new URL(endPoint + enrollPoint));
                    Bundle data = new Bundle();
                    data.putString("message", response);
                    data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_REG.name());
                    i.putExtras(data);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                // call u2f client
                getActivity().startActivityForResult(i, REG_ACTIVITY_RES_1);
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_enroll, container, false);
    }

    /**
     * Get U2F server request.
     * @return U2F server request.
     */
    private String getServerRequest(URL url) {
        return "{\"type\": \"u2f_register_request\", \"signRequest\": [], \"registerRequests\": [{\"challenge\": \"mLkHCmQZGbZEXefhWByeKo5zTFldYLIZFRGeHdvTFBc=\", \"version\": \"U2F_V2\", \"appId\": \"http://localhost:8000\"}]}";
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
