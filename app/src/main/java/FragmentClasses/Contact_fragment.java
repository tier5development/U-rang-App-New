package FragmentClasses;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.login.LoginManager;

import org.json.JSONObject;

import java.util.HashMap;

import HelperClasses.AsyncResponse;
import HelperClasses.RegisterUser;
import us.tier5.u_rang.LoginActivity;
import us.tier5.u_rang.R;
import us.tier5.u_rang.Splash;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Contact_fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Contact_fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Contact_fragment extends Fragment implements AsyncResponse.Response {

    //page variables
    ImageView ivEmailContact;
    ImageView ivCall;

    //server variable
    HashMap<String, String> data = new HashMap<String,String>();
    String route = "/V1/getProgileDetails";
    RegisterUser registerUser = new RegisterUser("POST");

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Contact_fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Contact_fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Contact_fragment newInstance(String param1, String param2) {
        Contact_fragment fragment = new Contact_fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragView = inflater.inflate(R.layout.fragment_contact_fragment, container, false);

        if (mListener != null) {
            mListener.onFragmentInteraction("U-Rang");
        }

        registerUser.delegate = this;

        SharedPreferences prefs = this.getActivity().getSharedPreferences("U-rang", Context.MODE_PRIVATE);
        int user_id = prefs.getInt("user_id", 0);

        //Toast.makeText(getContext(),""+user_id,Toast.LENGTH_SHORT).show();
        data.put("user_id",Integer.toString(user_id));

        registerUser.register(data, route);

        ImageView ivEmailContact = (ImageView) fragView.findViewById(R.id.ivEmailContact);

        ivEmailContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { "lisa@urang.com" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "U-rang Customer Contact");
                intent.putExtra(Intent.EXTRA_TEXT, "");

                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        ivCall = (ImageView) fragView.findViewById(R.id.ivCall);
        ivCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:(800)959-5785"));
                startActivity(intent);
            }
        });

        return fragView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void onFragmentInteraction(String title);
    }

    @Override
    public void processFinish(String output) {
        try {
            JSONObject jsonObject = new JSONObject(output);
            Log.v("PROFILE_STATUS:", jsonObject.toString());
            if (jsonObject.getInt("status_code") == 301) {
                Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                LoginManager.getInstance().logOut();
                SharedPreferences.Editor editor = this.getActivity().getSharedPreferences("U-rang", Context.MODE_PRIVATE).edit();
                editor.putInt("user_id", 0);
                if (editor.commit()) {
                    Intent intent = new Intent(getContext(), Splash.class);
                    startActivity(intent);
                }
            } else if (jsonObject.getInt("status_code") == 400) {
                Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                LoginManager.getInstance().logOut();
                SharedPreferences.Editor editor = this.getActivity().getSharedPreferences("U-rang", Context.MODE_PRIVATE).edit();
                editor.putInt("user_id", 0);
                editor.putBoolean("is_social_registered", false);
                if (editor.commit()) {
                    Intent intent = new Intent(getContext(), Splash.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e)
        {
            //Toast.makeText(getContext(),"Error in fetching order history!",Toast.LENGTH_SHORT).show();
            Log.i("kingsukmajumder","error in profile"+e.toString());
        }
    }
}
