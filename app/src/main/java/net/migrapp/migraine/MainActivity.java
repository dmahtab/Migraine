package net.migrapp.migraine;
//Samsung API is licenced under Apache License

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.sip.SipSession;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    //private TextView helloWorld;

    private static final String APP_TAG = "NET_MIGRAP_MIGRAINE";
    private HealthDataService healthDataService;
    private static MainActivity mInstance = null;
    private HealthDataStore mStore;
    private HealthConnectionErrorResult mConnError;
    private Set<HealthPermissionManager.PermissionKey> mKeySet;

    private void showConnectionFailureDialog(HealthConnectionErrorResult error) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        mConnError = error;
        mInstance = this;
        String message = "Connection with S Health is not available";
        if (mConnError.hasResolution()) {
            switch (error.getErrorCode()) {
                case HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED:
                    message = "Please install S Health";
                    break;
                case HealthConnectionErrorResult.OLD_VERSION_PLATFORM:
                    message = "Please upgrade S Health";
                    break;
                case HealthConnectionErrorResult.PLATFORM_DISABLED:
                    message = "Please enable S Health";
                    break;
                case HealthConnectionErrorResult.USER_AGREEMENT_NEEDED:
                    message = "Please agree with S Health policy";
                    break;
                default:
                    message = "Please make S Health available";
                    break;
            }
        }
        alert.setMessage(message);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mConnError.hasResolution()) {
                    mConnError.resolve(mInstance);
                }
            }
        });
        if (error.hasResolution()) {
            alert.setNegativeButton("Cancel", null);
        }
        alert.show();
    }
    /*
    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {
        @Override
        public void onConnected() {
            Log.d(APP_TAG, "Health data service is connected.");
            HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
            try {
                Log.i(APP_TAG, "About to setup listener");
                // Check whether the permissions that this application needs are acquired
                Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeySet);
                if (resultMap.containsValue(Boolean.FALSE)) {
                    // Request the permission for reading step counts if it is not acquired
                    Log.i(APP_TAG, "Setup Listener");
                    pmsManager.requestPermissions(mKeySet, MainActivity.this).setResultListener(mPermissionListener);
                } else {
                    // Get the current step count and display it
                    // ...
                    afterCall();
                }
            } catch (Exception e) {
                Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
                Log.e(APP_TAG, "Permission setting fails.");
            }
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Log.d(APP_TAG, "Health data service is not available.");
            showConnectionFailureDialog(error);
        }

        @Override
        public void onDisconnected() {
            Log.d(APP_TAG, "Health data service is disconnected.");
        }
    };

    private final HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult> mPermissionListener =
            new HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult>() {
                @Override
                public void onResult(HealthPermissionManager.PermissionResult result) {
                    Log.d(APP_TAG, "Permission callback is received.");
                    Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();
                    if (resultMap.containsValue(Boolean.FALSE)) {
                        // Requesting permission fails]

                    } else {
                        // Get the current step count and display it
                        afterCall();
                    }
                }
            };

    private final HealthResultHolder.ResultListener<HealthDataResolver.ReadResult> mListener = new HealthResultHolder.ResultListener<ReadResult>() {
        @Override
        public void onResult(HealthDataResolver.ReadResult result) {
            int count = 0;
            Cursor c = null;

            try {
                c = result.getResultCursor();
                if (c != null) {
                    while (c.moveToNext()) {
                        count += c.getInt(c.getColumnIndex(HealthConstants.StepCount.COUNT));
                    }
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            MainActivity.getInstance().drawStepCount(String.valueOf(count));
        }
    };
    */
    private void afterCall() {
        // helloWorld.setText(HealthConstants.Sleep.START_TIME);

        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        HealthDataResolver.Filter filter = filterIt(HealthConstants.CaffeineIntake.START_TIME);

        //HealthResultHolder.ResultListener<HealthDataResolver.ReadResult> mListener = new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>();





        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setProperties(new String[] {HealthConstants.StepCount.COUNT})
                .setFilter(filter)
                .build();

        try {
            resolver.read(request).setResultListener(mListener);
        } catch (Exception e) {
            Log.e(MainActivity.APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.e(MainActivity.APP_TAG, "Getting step count fails.");
        }

        Log.i( APP_TAG, "CALDER WAS HERE" + HealthConstants.WaterIntake.AMOUNT);
    }



    protected HealthDataResolver.Filter filterIt(String startTime) {
        double currentTime = System.currentTimeMillis();
        double yesterday = currentTime - 86400000;

        return HealthDataResolver.Filter.greaterThan(
                startTime, yesterday
        );
    }

    protected HealthDataResolver.Filter filterIt(String startTime, String endTime) {
        double currentTime = System.currentTimeMillis();
        double yesterday = currentTime - 86400000;

        return HealthDataResolver.Filter.and(
                HealthDataResolver.Filter.greaterThan(
                        startTime, yesterday
                ),
                HealthDataResolver.Filter.lessThan(
                        endTime, currentTime
                )
        );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //helloWorld = (TextView)findViewById(R.id.helloWorld);

        mKeySet = new HashSet<HealthPermissionManager.PermissionKey>();
        //Add Permissions
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.CaffeineIntake.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.OxygenSaturation.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.Sleep.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.WaterIntake.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));



        healthDataService = new HealthDataService();
        healthDataService.initialize(this);

        mStore = new HealthDataStore(this, mConnectionListener);
        // Request the connection to the health data store
        mStore.connectService();
    }


    @Override
    public void onDestroy() {
        mStore.disconnectService();
        super.onDestroy();
    }
}

