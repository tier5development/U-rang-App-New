package us.tier5.u_rang;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

import HelperClasses.AsyncResponse;
import HelperClasses.AsyncResponseSms;
import HelperClasses.AsyncSmsTracker;
import HelperClasses.EmailValidator;
import HelperClasses.RegisterSms;
import HelperClasses.RegisterUser;
import HelperClasses.RegisterUser2;

public class MainActivity extends AppCompatActivity implements AsyncResponse.Response,View.OnClickListener, AsyncResponseSms.ResponseSms, AsyncSmsTracker.ResponseSms{

    EditText email;
    EditText pass;
    EditText again;
    EditText mob;
    TextView cont;
    EditText name;
    EditText etRefEmail;

    TextView signin;
    Button btnRegister;
    EditText edt;

    String smsVerificationCode="";

    RegisterUser registerUser = new RegisterUser("POST");
    HashMap<String,String> data = new HashMap<>();
    String route = "/V1/sign-up-user";


    RegisterSms registerSms = new RegisterSms("POST");
    HashMap<String,String> dataSms = new HashMap<>();
    String routeSms = "https://www.textinbulk.com/app/api/send/ND";

    SmsListener smsListener = new SmsListener();

    ProgressDialog loading;
    ProgressDialog loadingSms;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsListener.delegate=this;

        registerUser.delegate = this;
        registerSms.delegate=this;


        email = (EditText)findViewById(R.id.email);

        pass = (EditText)findViewById(R.id.pass);

         again= (EditText)findViewById(R.id.again);

        mob = (EditText)findViewById(R.id.mob);

        cont = (TextView)findViewById(R.id.cont);

        name = (EditText) findViewById(R.id.name);

        signin = (TextView)findViewById(R.id.signin);

        btnRegister = (Button) findViewById(R.id.btnRegister);

        etRefEmail = (EditText) findViewById(R.id.etRefEmail);


        btnRegister.setOnClickListener(this);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void processFinish(String output) {
        loading.dismiss();
        Log.i("kingsukmajumder","response after login: "+output);
        try
        {
            JSONObject jsonObject = new JSONObject(output);
            Boolean status = jsonObject.getBoolean("status");

            if(status)
            {
                String responseObj = jsonObject.getString("response");
                JSONObject user_data = new JSONObject(responseObj);

                int user_id = user_data.getInt("user_id");

                /*for (int i=0;i<jsonArray.length();i++)
                {
                    user_id = jsonArray.getJSONObject(i).getInt("id");

                }*/
                SharedPreferences.Editor editor = getSharedPreferences("U-rang", MODE_PRIVATE).edit();
                editor.putInt("user_id", user_id);
                if(editor.commit())
                {
                    Toast.makeText(getApplicationContext(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,Dashboard.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
                else
                {
                    Toast.makeText(getApplication(),"Some problem occoured, You may have to login again when you launch the app!",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this,Dashboard.class);
                    startActivity(intent);
                }

            }
            else
            {
                Snackbar.make(this.findViewById(android.R.id.content), jsonObject.getString("message"), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Toast.makeText(getApplicationContext(),jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
            }

        }
        catch (Exception e)
        {
            Log.i("kingsukmajumder",e.toString());
            //Toast.makeText(getApplication(),"Error in login",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        if(!email.getText().toString().equals("") && !mob.getText().toString().equals("") && !again.getText().toString().equals("") && !pass.getText().toString().equals("") && !name.getText().toString().equals(""))
        {
            EmailValidator emailValidator = new EmailValidator();
            if(emailValidator.validate(email.getText().toString()))
            {
                smsVerificationCode=String.valueOf((int)(Math.random()*9000)+1000);
                Log.i("kingsukmajumder","sms verification code is "+smsVerificationCode);
                dataSms.put("to",mob.getText().toString());
                dataSms.put("body",smsVerificationCode);
                //loadingSms = ProgressDialog.show(this, "","Verifying your number", true, false);
                registerSms.register(dataSms,routeSms);

                createSmsPopup();

                /*data.put("email",email.getText().toString());
                data.put("name",name.getText().toString());
                data.put("personal_phone",mob.getText().toString());
                data.put("password",pass.getText().toString());
                data.put("conf_password",again.getText().toString());
                Log.i("kingsukmajumder",data.toString());
                loading = ProgressDialog.show(this, "","Please wait", true, false);
                registerUser.register(data,route);*/
            }
            else
            {
                Toast.makeText(this, "Please enter a valid email address!", Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
        }

    }

    public void createSmsPopup()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.popupphoneno, null);
        dialogBuilder.setView(dialogView);

        edt = (EditText) dialogView.findViewById(R.id.etCoupone);

        // edt.setText(String.valueOf((int)(Math.random()*9000)+1000));

        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Enter sms verification code:");
        dialogBuilder.setMessage("Please wait for sometime to receive it in your phone.");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                if(edt.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(),"You need to enter a the message code",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    if(smsVerificationCode.equals(edt.getText().toString()))
                    {
                        data.put("email",email.getText().toString());
                        data.put("name",name.getText().toString());
                        data.put("personal_phone",mob.getText().toString());
                        data.put("password",pass.getText().toString());
                        data.put("conf_password",again.getText().toString());
                        data.put("ref_name",etRefEmail.getText().toString());
                        loading = ProgressDialog.show(MainActivity.this, "","Please wait", true, false);
                        registerUser.register(data,route);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Verification code did'nt matched!", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void processFinishSms(String output) {
        //loadingSms.dismiss();
        Log.i("kingsukmajumder","sms sending response "+output);
    }

    @Override
    public void smsReceived(String output) {
        edt.setText(output);
    }
}
