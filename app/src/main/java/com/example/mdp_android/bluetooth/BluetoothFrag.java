package com.example.mdp_android.bluetooth;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
    public class BluetoothFrag extends Fragment {

    private static final String TAG = "BluetoothFrag";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BLUETOOTH = 3;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // gets the local device Bluetooth Adapter - to perform Bluetooth tasks
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // if mBluetoothAdapter == null, means that device does not support Bluetooth
        FragmentActivity activity = getActivity();
        if (mBluetoothAdapter == null && activity != null) {
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBluetoothAdapter == null) {
            return;
        }
        // If Bluetooth is not on, send user a request to enable it
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
            // Otherwise, setup the bluetooth service
        } else if (mBluetoothService == null) {
            setUpBluetoothFrag();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothService != null) {
            mBluetoothService.stopAllThreads();
        }
    }

    @Override
    // in case where Bluetooth is not enabled at start, onResume called once Bluetooth is enabled
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ON Resume in BTFragment");

        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NOTHING) {
                // Start the Bluetooth chat services
                mBluetoothService.startService();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bluetooth_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = view.findViewById(R.id.in);
        mOutEditText = view.findViewById(R.id.message_out);
        mSendButton = view.findViewById(R.id.send_button);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setUpBluetoothFrag() {
        Log.d(TAG, "Setting up fragment");

        // Initialize the array adapter for the conversation thread
        FragmentActivity activity = getActivity();
        if (activity == null) { return; }

        mConversationArrayAdapter = new ArrayAdapter<>(activity, R.layout.bluetooth_message);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = view.findViewById(R.id.message_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        // Initialize the BluetoothService to perform bluetooth connections
        mBluetoothService = new BluetoothService(activity, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }

    // Makes bluetooth device discoverable by other devices
    @SuppressLint("MissingPermission")
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void sendBluetoothMessage(String message) {sendMessage(message);}

    // write messages to external Bluetooth device
    private void sendMessage(String message) {

        // to check if device has been connected to any Bluetooth device
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.no_connected_device, Toast.LENGTH_SHORT).show();
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            mBluetoothService.writeMessage(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);

        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    // used to update status in action bar - using res
    private void setStatus(int resourceId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resourceId);
    }

    // used to update status on action bar - using subTitle
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.connected_to_device, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.connecting);
                            break;
                        case BluetoothService.STATE_LISTENING:
                        case BluetoothService.STATE_NOTHING:
                            setStatus(R.string.not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    boolean messageIsCommand = false;
                    if (readMessage.split(",")[0].equals("ROBOT")){
                        String[] splitString = readMessage.split(",");
                        if (splitString.length == 4 && isInteger(splitString[1]) && isInteger(splitString[2]) && isInteger(splitString[3])){
                            String direction;
                            switch (Integer.parseInt(splitString[3])) {
                                case 0:
                                    direction = "E";
                                    break;
                                case 90:
                                    direction = "N";
                                    break;
                                case 180:
                                    direction = "W";
                                    break;
                                case 270:
                                    direction = "S";
                                    break;
                                default:
                                    direction = null;
                            }
                            if (direction.length() == 1 && MainActivity.setRobotPosition(Integer.parseInt(splitString[1]), Integer.parseInt(splitString[2]), direction.charAt(0))){
                                messageIsCommand = true;
                            } else if (direction.length() == 1){
                                // TODO: check message
                                Toast.makeText(activity, "Robot is out of range", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(activity, "Direction not recognized", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else if (readMessage.split(",")[0].split(":")[0].equals("{\"cat\"")) {
                        Log.d(TAG, readMessage.split(",")[1]);
                        String[] splitString = readMessage.split(",");
                        if (splitString.length == 2 ){
                            Log.d(TAG, splitString[1]);
                            String catString = splitString[0].split(":")[1].replace("}", "");
                            String statusString = splitString[1].split(":")[1].replace("}", "");
                            Log.d(TAG, statusString);
                            if (MainActivity.updateRobotStatus(catString + ":" + statusString)){
                                messageIsCommand = true;
                            } else {
                                Toast.makeText(activity, "TargetID/ObstacleID is out of range", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else if (readMessage.split(",")[0].equals("TARGET")){
                        String[] splitString = readMessage.split(",");
                        if (splitString.length == 3 && isInteger(splitString[1]) && isInteger(splitString[2])){
                            if (MainActivity.exploreTarget(Integer.parseInt(splitString[1]), Integer.parseInt(splitString[2]))){
                                messageIsCommand = true;
                            } else {
                                Toast.makeText(activity, "TargetID/ObstacleID is out of range", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    if (!messageIsCommand){
                        mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to: " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    setUpBluetoothFrag();
                } else {
                    // Bluetooth not enabled
                    Log.d(TAG, "Bluetooth not enabled");
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        Toast.makeText(activity, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                }
        }
    }

    // to establish a Bluetooth connection with external device
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        Bundle extras = data.getExtras();
        if (extras == null) {
            return;
        }
        String address = extras.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connectToRemoteDevice(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
            case R.id.disconnect: {
                // Ensure this device is discoverable by others
                if (mBluetoothService == null) return false;
                mBluetoothService.disconnectRemoteDevice();
                return true;
            }
        }
        return false;
    }

    private boolean isInteger( String input ) {
        try {
            Integer.parseInt( input );
            return true;
        }
        catch( NumberFormatException e ) {
            return false;
        }
    }
}
