package com.example.mdp_android.bluetooth;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    String TURN_RIGHT = "D";
    String TURN_LEFT = "A";
    String FORWARD = "W";
    String BACKWARD = "S";
    String STOP = "P";

}
