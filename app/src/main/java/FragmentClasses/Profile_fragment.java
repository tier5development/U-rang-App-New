package FragmentClasses;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cooltechworks.creditcarddesign.CardEditActivity;
import com.cooltechworks.creditcarddesign.CreditCardUtils;
import com.cooltechworks.creditcarddesign.CreditCardView;
import com.facebook.login.LoginManager;
import com.roger.gifloadinglibrary.GifLoadingView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import HelperClasses.AsyncResponse;
import HelperClasses.AsyncResponse2;
import HelperClasses.AsyncResponse3;
import HelperClasses.CheckNetwork;
import HelperClasses.RegisterUser;
import HelperClasses.RegisterUser2;
import HelperClasses.RegisterUser3;
import us.tier5.u_rang.LoginActivity;
import us.tier5.u_rang.R;
import us.tier5.u_rang.Splash;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Profile_fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Profile_fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile_fragment extends Fragment implements AsyncResponse.Response, AsyncResponse2.Response2,View.OnClickListener, AsyncResponse3.Response3 {

    //configuration variables
    FragmentManager manager;
    Bundle mysavedInstance;

    //server variable
    HashMap<String, String> data = new HashMap<String,String>();
    String route = "/V1/getProgileDetails";
    String routeUpdateProfile = "V1/updateProfile";
    RegisterUser registerUser = new RegisterUser("POST");
    RegisterUser2 registerUser2 = new RegisterUser2("POST");
    RegisterUser3 registerUser3 = new RegisterUser3("POST");
    int user_id;

    //gif loader variables

    GifLoadingView mGifLoadingView;


    //page variables
    EditText email;
    EditText name;
    EditText personal_ph;
    EditText address;
    EditText cellPhone;
    EditText officePhone;
    EditText specialInstruction;
    EditText drivingInstruction;
    Button creditCardButton;
    TextView submitProfile;

    String nameOnCard = "";
    String cvvOnCard = "";
    String expiryOnCard = "";
    String cardNumberOnCard = "";


    final int GET_NEW_CARD = 2;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Profile_fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profile_fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile_fragment newInstance(String param1, String param2) {
        Profile_fragment fragment = new Profile_fragment();
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

        mysavedInstance = savedInstanceState;
        View fragView = inflater.inflate(R.layout.fragment_profile_fragment, container, false);

        if (mListener != null) {
            mListener.onFragmentInteraction("U-Rang");
        }

        registerUser.delegate = this;
        registerUser2.delegate = this;
        registerUser3.delegate = this;

        registerUser3.register(data, route);

        manager = ((Activity) getContext()).getFragmentManager();
        mGifLoadingView = new GifLoadingView();
        mGifLoadingView.setImageResource(R.drawable.loading_3);

        //connet to server to get order tracker
        //SharedPreferences preferences = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences prefs = this.getActivity().getSharedPreferences("U-rang", Context.MODE_PRIVATE);
        user_id = prefs.getInt("user_id", 0);
        //Toast.makeText(getContext(),""+user_id,Toast.LENGTH_SHORT).show();

        //Toast.makeText(getContext(),""+user_id,Toast.LENGTH_SHORT).show();
        data.put("user_id",Integer.toString(user_id));



        if(CheckNetwork.isInternetAvailable(getContext())) //returns true if internet available
        {

            mGifLoadingView.show(manager, "Loading");
            mGifLoadingView.setBlurredActionBar(true);
            registerUser.register(data,route);
        }
        else
        {
            Toast.makeText(getContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
        }

        email = (EditText) fragView.findViewById(R.id.email);
        name = (EditText) fragView.findViewById(R.id.name);
        personal_ph = (EditText) fragView.findViewById(R.id.personal_ph);
        address = (EditText) fragView.findViewById(R.id.address);
        cellPhone = (EditText) fragView.findViewById(R.id.cellPhone);
        officePhone = (EditText) fragView.findViewById(R.id.officePhone);
        specialInstruction = (EditText) fragView.findViewById(R.id.specialInstruction);
        drivingInstruction = (EditText) fragView.findViewById(R.id.drivingIns);
        creditCardButton = (Button) fragView.findViewById(R.id.creditCardButton);

        submitProfile= (TextView) fragView.findViewById(R.id.submitProfile);

        personal_ph.addTextChangedListener(new USPhoneNumberFormattingTextWatcher(personal_ph));
        cellPhone.addTextChangedListener(new USPhoneNumberFormattingTextWatcher(cellPhone));
        officePhone.addTextChangedListener(new USPhoneNumberFormattingTextWatcher(officePhone));

        submitProfile.setOnClickListener(this);

        creditCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreditCardView creditCardView = new CreditCardView(getContext());

                /*creditCardView.setCVV(cvv);
                creditCardView.setCardHolderName(name);
                creditCardView.setCardExpiry(expiry);
                creditCardView.setCardNumber(cardNumber);*/

                Intent intent = new Intent(getActivity(), CardEditActivity.class);

                intent.putExtra(CreditCardUtils.EXTRA_CARD_HOLDER_NAME, nameOnCard);
                intent.putExtra(CreditCardUtils.EXTRA_CARD_NUMBER, cardNumberOnCard);
                intent.putExtra(CreditCardUtils.EXTRA_CARD_EXPIRY, cvvOnCard);
                intent.putExtra(CreditCardUtils.EXTRA_CARD_CVV, expiryOnCard);
                intent.putExtra(CreditCardUtils.EXTRA_CARD_SHOW_CARD_SIDE, CreditCardUtils.CARD_SIDE_BACK);
                intent.putExtra(CreditCardUtils.EXTRA_VALIDATE_EXPIRY_DATE, true); // pass "false" to discard expiry date validation.

                startActivityForResult(intent,GET_NEW_CARD);
            }
        });

        return fragView;
    }


    public void onActivityResult(int reqCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK) {

            String cardHolderName = data.getStringExtra(CreditCardUtils.EXTRA_CARD_HOLDER_NAME);
            String cardNumber = data.getStringExtra(CreditCardUtils.EXTRA_CARD_NUMBER);
            String expiry = data.getStringExtra(CreditCardUtils.EXTRA_CARD_EXPIRY);
            String cvv = data.getStringExtra(CreditCardUtils.EXTRA_CARD_CVV);

            // Your processing goes here.

        }
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

    @Override
    public void processFinish(String output) {
        Log.i("kingsukmajumder",output);
        mGifLoadingView.dismiss();
        try
        {
            JSONObject jsonObject = new JSONObject(output);
            if(jsonObject.getBoolean("status"))
            {
                JSONObject response = new JSONObject(jsonObject.getString("response"));
                email.setText(response.getString("email"));

                JSONObject userDetails = new JSONObject(response.getString("user_details"));

                if (!userDetails.getString("name").equals("")) {
                    name.setText(userDetails.getString("name"));
                }
                if(!userDetails.getString("personal_ph").equals("null") || !userDetails.getString("personal_ph").equals(""))
                {
                    personal_ph.setText(userDetails.getString("personal_ph"));
                    Log.i("kingsukmajumder",userDetails.getString("personal_ph"));
                }

                if(!userDetails.getString("address_line_1").equals("null"))
                {
                    address.setText(userDetails.getString("address_line_1"));
                }

                if(userDetails.getInt("cell_phone")!=0)
                {
                    cellPhone.setText(Integer.toString(userDetails.getInt("cell_phone")));
                }
                if(userDetails.getInt("off_phone")!=0)
                {
                    officePhone.setText(Integer.toString(userDetails.getInt("off_phone")));
                }
                if (!userDetails.getString("spcl_instructions").equals("")) {
                    specialInstruction.setText(userDetails.getString("spcl_instructions"));
                }
                if (!userDetails.getString("driving_instructions").equals("")) {
                    drivingInstruction.setText(userDetails.getString("driving_instructions"));
                }

                try
                {
                    JSONObject card_details = new JSONObject(response.getString("card_details"));
                    nameOnCard = card_details.getString("name");
                    cvvOnCard = card_details.getString("cvv");
                    cardNumberOnCard = card_details.getString("card_no");
                    expiryOnCard = card_details.getString("exp_month")+"/"+card_details.getString("exp_year");

                }
                catch (Exception e)
                {
                    Toast.makeText(getContext(),"No Credit Card Info Found",Toast.LENGTH_SHORT).show();
                    Log.i("kingsukmajumder",e.toString());
                }
            } else
            {
                Toast.makeText(getContext(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            //Toast.makeText(getContext(),"Error in fetching order history!",Toast.LENGTH_SHORT).show();
            Log.i("kingsukmajumder","error in profile"+e.toString());
        }
    }



    @Override
    public void processFinish2(String output) {
        Log.i("kingsukmajumder",output);
        mGifLoadingView.dismiss();
        try
        {
            JSONObject jsonObject = new JSONObject(output);
            if(jsonObject.getBoolean("status"))
            {
                Toast.makeText(getContext(),"Profile updated successfully",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getContext(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            Log.i("kingsukmajumder",e.toString());
        }
    }

    @Override
    public void processFinish3(String output) {
        try {
            JSONObject jsonObject = new JSONObject(output);
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

    @Override
    public void onClick(View v) {
        registerUser3.register(data, route);
        if(emailValidator(email.getText().toString()) && !name.getText().toString().equals("") && !address.getText().toString().equals("") &&!personal_ph.getText().toString().equals(""))
        {
            data.put("email",email.getText().toString());
            data.put("name",name.getText().toString());
            data.put("address",address.getText().toString());
            data.put("personal_phone",personal_ph.getText().toString());
            data.put("cell_phone",cellPhone.getText().toString());
            data.put("office_phone",officePhone.getText().toString());
            data.put("spcl_instruction",specialInstruction.getText().toString());
            data.put("driving_instruction",drivingInstruction.getText().toString());

            mGifLoadingView.show(manager, "Loading");
            mGifLoadingView.setBlurredActionBar(true);
            registerUser2.register(data,routeUpdateProfile);
        }
        else
        {
            Toast.makeText(getContext(),"Please enter valid Email, Name, Address and Personal Phone",Toast.LENGTH_LONG).show();
        }
    }

    public boolean emailValidator(String email)
    {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
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

    private class USPhoneNumberFormattingTextWatcher extends PhoneNumberFormattingTextWatcher {
        EditText editText;

        USPhoneNumberFormattingTextWatcher(EditText editText) {
            this.editText = editText;
        }

        //we need to know if the user is erasing or inputing some new character
        private boolean backspacingFlag = false;
        //we need to block the :afterTextChanges method to be called again after we just replaced the EditText text
        private boolean editedFlag = false;
        //we need to mark the cursor position and restore it after the edition
        private int cursorComplement;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //we store the cursor local relative to the end of the string in the EditText before the edition
            cursorComplement = s.length()-editText.getSelectionStart();
            //we check if the user ir inputing or erasing a character
            backspacingFlag = count > after;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // nothing to do here =D
        }

        @Override
        public void afterTextChanged(Editable s) {
            String string = s.toString();
            //what matters are the phone digits beneath the mask, so we always work with a raw string with only digits
            String phone = string.replaceAll("[^\\d]", "");

            //if the text was just edited, :afterTextChanged is called another time... so we need to verify the flag of edition
            //if the flag is false, this is a original user-typed entry. so we go on and do some magic
            if (!editedFlag) {

                //we start verifying the worst case, many characters mask need to be added
                //example: 999999999 <- 6+ digits already typed
                // masked: (999) 999-999
                if (phone.length() >= 6 && !backspacingFlag) {
                    //we will edit. next call on this textWatcher will be ignored
                    editedFlag = true;
                    //here is the core. we substring the raw digits and add the mask as convenient
                    String ans = "(" + phone.substring(0, 3) + ") " + phone.substring(3,6) + "-" + phone.substring(6);
                    editText.setText(ans);
                    //we deliver the cursor to its original position relative to the end of the string
                    editText.setSelection(editText.getText().length()-cursorComplement);

                    //we end at the most simple case, when just one character mask is needed
                    //example: 99999 <- 3+ digits already typed
                    // masked: (999) 99
                } else if (phone.length() >= 3 && !backspacingFlag) {
                    editedFlag = true;
                    String ans = "(" +phone.substring(0, 3) + ") " + phone.substring(3);
                    editText.setText(ans);
                    editText.setSelection(editText.getText().length()-cursorComplement);
                }
                // We just edited the field, ignoring this cycle of the watcher and getting ready for the next
            } else {
                editedFlag = false;
            }
        }
    }
}
