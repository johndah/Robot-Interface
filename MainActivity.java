package com.example.johnd.systmcontrolrobot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.controlwear.virtual.joystick.android.JoystickView;
public class MainActivity extends Activity  {

    private Pubnub mPubNub;
    public static final String PUBLISH_KEY = "*Insert your key here*";
    public static final String SUBSCRIBE_KEY = "*Insert your key here*";
    public static final String CHANNEL = "sysCtrlRobot";
    private double linVel, angVel, x, y, forwardFactor, backwardFactor;
    private boolean parking = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate PubNub. We will cover this shortly.
        initPubNub();

        final TextView linVelText = (TextView) findViewById(R.id.linVelText);
        final TextView angVelText = (TextView) findViewById(R.id.angVelText);

        final Button breakButton = findViewById(R.id.breakButton);
        breakButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                linVel = 0;
                angVel = 0;
                publish();
                linVelText.setText("Linear Velocity = " + ((double) (Math.round(linVel*100)))/100);
                angVelText.setText("Angular Velocity = " + ((double) (Math.round(angVel*100)))/100);
            }
        });

        final Button parkButton = findViewById(R.id.park);
        parkButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (parking) {
                    parkButton.setText("Park");
                    parking = false;
                } else {
                    parkButton.setText("Stop parking");
                    parking = true;
                }
            }
        });


        final JoystickView joystick = findViewById(R.id.joystickView);
        joystick.setFixedCenter(false);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {


            public void onMove(int angle, int strength) {
                x = 50 - joystick.getNormalizedX();
                y = 50 - joystick.getNormalizedY();

                forwardFactor = 0.8;
                backwardFactor = 0.4;

                if (y > 0)
                    linVel = forwardFactor*y/30;
                else
                    linVel = backwardFactor*y/30;

                if (linVel >= -0.2)
                    angVel = x/12;
                else
                    angVel = - x/12;

                linVelText.setText("Linear Velocity = " + ((double) (Math.round(linVel*100)))/100);
                angVelText.setText("Angular Velocity = " + ((double) (Math.round(angVel*100)))/100);
                publish();

            }
        }, 100);

    }


    public void initPubNub(){
        this.mPubNub = new Pubnub(
                PUBLISH_KEY,
                SUBSCRIBE_KEY
        );
        this.mPubNub.setUUID("AndroidSysCtrlRobot");
        subscribe();
    }

    public void subscribe(){
        try {
            this.mPubNub.subscribe(CHANNEL, new Callback() {

                public void successCallback(String channel, Object message) {
                    Log.d("PUBNUB","SUBSCRIBE : " + channel + " : "
                            + message.getClass() + " : " + message.toString());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void publish(){
        JSONObject js = new JSONObject();
        try {
            System.out.println(linVel);
            js.put("linVel", linVel);
            js.put("angVel", angVel);

        } catch (JSONException e) { e.printStackTrace(); }

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response) {
                Log.d("PUBNUB",response.toString());
            }
        };
        this.mPubNub.publish(CHANNEL, js, callback);
    }


}
