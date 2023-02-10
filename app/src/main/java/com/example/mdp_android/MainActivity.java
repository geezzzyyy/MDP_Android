package com.example.mdp_android;

import static java.lang.Character.toUpperCase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.mdp_android.bluetooth.BluetoothFrag;
import com.example.mdp_android.bluetooth.Constants;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    static Robot robot = new Robot();
    MutableLiveData<String> listen = new MutableLiveData<>();
    public static TextView robotCoordinateXText;
    public static TextView robotCoordinateYText;
    public static TextView robotDirText;
    public static TextView robotStatusText;
    private static MapGrid mapGrid;
    BluetoothFrag bluetoothFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // setting activity_main layout upon startup

        listen.setValue("Default");

        mapGrid = findViewById(R.id.mapGrid);

        //assign robot coordinates
        robotCoordinateXText = findViewById(R.id.robotXCoordinate);
        robotCoordinateYText = findViewById(R.id.robotYCoordinate);

        //assign robot direction
        robotDirText = findViewById(R.id.robotDirection);

        //assign robot status
        robotStatusText = findViewById(R.id.robotStatus);

        // Remove shadow of action bar
        getSupportActionBar().setElevation(0);
        // Set layout to shift up when soft keyboard is open
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            bluetoothFragment = new BluetoothFrag();
            transaction.replace(R.id.bluetooth_stream_box, bluetoothFragment);
            transaction.commit();
        }

        // up button is pressed to move robot forward
        findViewById(R.id.upButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // to check if is moving out of range
                if ((robot.getY() != 17 && robot.getY() >= 0 && (robot.getDirection() == 'N' || robot.getDirection() == 'S'))
                        || (robot.getX() != 17 && robot.getX() >= 0 && (robot.getDirection() == 'E' || robot.getDirection() == 'W'))
                        || ((robot.getX() == 17 || robot.getY() == 17) && (robot.getDirection() == 'W' || robot.getDirection() == 'S'))
                        || ((robot.getX() == 0 || robot.getY() == 0) && (robot.getDirection() == 'N' || robot.getDirection() == 'E'))
                ) {
                    robot.moveRobotForward();
                    mapGrid.invalidate();

                    outgoingMessage(Constants.FORWARD);
                    Toast.makeText(MainActivity.this, "Move forward", Toast.LENGTH_SHORT).show();
                }

                if (robot.getX() != -1 && robot.getY() != -1) {
                    robotCoordinateXText.setText(String.valueOf(robot.getX()));
                    robotCoordinateYText.setText(String.valueOf(robot.getY()));
                    robotDirText.setText(String.valueOf(robot.getDirection()));
                } else {
                    robotCoordinateXText.setText("-");
                    robotCoordinateYText.setText("-");
                    robotDirText.setText("-");
                }

            }
        });

        // left button is pressed to turn the robot in the clockwise dir
        findViewById(R.id.leftButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robot.moveRobotTurnLeft();
                mapGrid.invalidate();
                outgoingMessage(Constants.TURN_LEFT);
                Toast.makeText(MainActivity.this, "Turn Left", Toast.LENGTH_SHORT).show();

                if (robot.getX() != -1 && robot.getY() != -1) {
                    robotCoordinateXText.setText(String.valueOf(robot.getX()));
                    robotCoordinateYText.setText(String.valueOf(robot.getY()));
                    robotDirText.setText(String.valueOf(robot.getDirection()));
                } else {
                    robotCoordinateXText.setText("-");
                    robotCoordinateYText.setText("-");
                    robotDirText.setText("-");
                }
            }
        });

        // right button is pressed to turn the robot in the anti-clockwise dir
        findViewById(R.id.rightButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robot.moveRobotTurnRight();
                mapGrid.invalidate();

                outgoingMessage(Constants.TURN_RIGHT);
                Toast.makeText(MainActivity.this, "Turn Right", Toast.LENGTH_SHORT).show();

                if (robot.getX() != -1 && robot.getY() != -1) {
                    robotCoordinateXText.setText(String.valueOf(robot.getX()));
                    robotCoordinateYText.setText(String.valueOf(robot.getY()));
                    robotDirText.setText(String.valueOf(robot.getDirection()));
                } else {
                    robotCoordinateXText.setText("-");
                    robotCoordinateYText.setText("-");
                    robotDirText.setText("-");
                }
            }
        });

        // button to initiate challenge 1 - autonomous image recognition task
        findViewById(R.id.imageRecognitionStartButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outgoingMessage("begin");
            }
        });

        // TODO: add command to button to initiate challenge 2
        findViewById(R.id.fastestRobotStartButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outgoingMessage("fastest");
            }
        });

        listen.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s == "Move") {
                    if (robot.getX() != -1 && robot.getY() != -1) {
                        robot.moveRobotForward();
                        mapGrid.invalidate();
                        robotCoordinateXText.setText(String.valueOf(robot.getX()));
                        robotCoordinateYText.setText(String.valueOf(robot.getY()));
                        robotDirText.setText(String.valueOf(robot.getDirection()));
                    }
                    Log.d(TAG, "MOVE");
                } else if (s == "Left") {
                    if (robot.getX() != -1 && robot.getY() != -1) {
                        robot.moveRobotTurnLeft();
                        mapGrid.invalidate();
                        robotCoordinateXText.setText(String.valueOf(robot.getX()));
                        robotCoordinateYText.setText(String.valueOf(robot.getY()));
                        robotDirText.setText(String.valueOf(robot.getDirection()));
                    }
                    Log.d(TAG, "LEFT");
                } else {
                    Log.d(TAG, "CHANGE VALUE: " + s);
                }
            }
        });
    }

    // sending messages through bluetooth
    public void outgoingMessage(String sendMsg) {
        bluetoothFragment.sendBluetoothMessage(sendMsg);
    }

    // obstacle direction
    public static void showObstaclePopup(Context c, View view, Obstacle obstacle) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.obstacle_popup_window, null);

        // create popup window for labelling obstacle direction
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        Button btnN = (Button) popupView.findViewById(R.id.obstacleNorthSide);
        Button btnS = (Button) popupView.findViewById(R.id.obstacleSouthSide);
        Button btnE = (Button) popupView.findViewById(R.id.obstacleEastSide);
        Button btnW = (Button) popupView.findViewById(R.id.obstacleWestSide);

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        MapGrid mapGrid = view.findViewById(R.id.mapGrid);

        btnN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGrid.setObstacleSide(obstacle, 'N');
                popupWindow.dismiss();
            }
        });

        btnS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGrid.setObstacleSide(obstacle, 'S');
                popupWindow.dismiss();
            }
        });

        btnE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGrid.setObstacleSide(obstacle, 'E');
                popupWindow.dismiss();
            }
        });

        btnW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGrid.setObstacleSide(obstacle, 'W');
                popupWindow.dismiss();
            }
        });

        // dismiss the popup window
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    public static boolean setRobotPosition(int x, int y, char direction) {
        if (0 <= x && x <= 19 && 0 <= y && y <= 19 && (direction == 'n' || direction == 's' || direction == 'e' || direction == 'w')) {
            robot.setCoordinates(x, y);
            robot.setDirection(toUpperCase(direction));
            robotCoordinateXText.setText(String.valueOf(robot.getX()));
            robotCoordinateYText.setText(String.valueOf(robot.getY()));
            robotDirText.setText(String.valueOf(robot.getDirection()));
            mapGrid.invalidate();
            return true;
        }
        return false;
    }

    public static boolean exploreTarget(int obstacleNumber, int targetID) {
        // if obstacle number exists in map
        if (obstacleNumber >= 1 && obstacleNumber <= Map.getInstance().getObstacles().size() && obstacleNumber <= 8 && targetID <= 40 && targetID >= 11) {
            Obstacle obstacle = Map.getInstance().getObstacles().get(obstacleNumber - 1);
            obstacle.explore(targetID);
            mapGrid.invalidate();
            return true;
        }
        return false;
    }

    public static void updateRobotStatus(String status) {
        robot.setStatus(status);
        robotStatusText.setText(robot.getStatus());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}