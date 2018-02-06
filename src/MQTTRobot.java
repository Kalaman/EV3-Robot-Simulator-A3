import org.fusesource.mqtt.client.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by Kalaman on 09.01.18.
 */
public class MQTTRobot {
    private MQTT mqtt;
    private static BlockingConnection connection;
    private Topic [] topic;

    private ArrayList<MQTTListener> mqttListener;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1883;
    public static final String AXIS_X = "x";
    public static final String AXIS_Y = "y";

    public static final String TOPIC_LOG = "log";
    public static final String TOPIC_NODE = "node";
    public static final String TOPIC_DRIVE = "drive";
    public static final String TOPIC_SONIC_DISTANCE = "distance";

    public MQTTRobot() {
        mqtt = new MQTT();
        mqttListener = new ArrayList<MQTTListener>();
        topic = new Topic[] {new Topic(TOPIC_DRIVE, QoS.EXACTLY_ONCE)};

        try {
            mqtt.setHost(SERVER_IP, SERVER_PORT);
            connection = mqtt.blockingConnection();
            connection.connect();
            publish("Robot is connected [" + SERVER_IP + ":" + SERVER_PORT + "] ...",TOPIC_LOG);
            connection.subscribe(topic);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface MQTTListener {
        public void onDriveReceived(float distance,boolean xAxis, int robotSensorAmount);
    }

    public boolean addMQTTListener(MQTTListener listener) {
        return mqttListener.add(listener);
    }

    public boolean removeMQTTListener(MQTTListener listener) {
        return mqttListener.remove(listener);
    }

    public void startListeningThread () {
        new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        Message message = connection.receive();
                        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

                        if (message.getTopic().equals(TOPIC_DRIVE)) {
                            String [] splitedPayload = payload.toLowerCase().split("#");

                            boolean axis = splitedPayload[0].equals(AXIS_X) ? true : false;
                            int sensorAmount = Integer.parseInt(splitedPayload[2]);
                            float distance = Float.parseFloat(splitedPayload[1]);

                            for (MQTTListener listener : mqttListener)
                                listener.onDriveReceived(distance,axis,sensorAmount);
                        }
                        message.ack();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }



//    public static ArrayList<> parseJSONNodeData (String jsonData) {
//
//        try{
//            JSONObject obj = new JSONObject(jsonData);
//
//            JSONArray arr = obj.getJSONArray("nodes");
//            for (int i = 0; i < arr.length(); i++)
//            {
//                int nodeXPos = arr.getJSONObject(i).getInt("x");
//                int nodeYPos = arr.getJSONObject(i).getInt("y");
//                float nodeDeg = (float)arr.getJSONObject(i).getDouble("deg");
//
//                resultList.add(new Particle(nodeXPos,nodeYPos,nodeDeg));
//            }
//        }
//        catch (JSONException je)
//        {
//            je.printStackTrace();
//            JConsolePanel.writeToConsole("Unknown JSON type received");
//            return null;
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//        return resultList;
//    }

    public static void publish (String message, String topic) {
        try {
            connection.publish(topic, message.getBytes() ,QoS.EXACTLY_ONCE, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishLog (String msg) {
        publish(msg,TOPIC_LOG);
    }

}