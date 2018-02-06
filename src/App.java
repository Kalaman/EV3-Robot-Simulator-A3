import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Exchanger;

public class App {

    static int robotX = 72;
    static int robotY = 66;
    static final int robotDeg = 60;

    static ArrayList<Line> lines;
    static Particle particle;

    public static void main (String arg []){
        MQTTRobot robot = new MQTTRobot();
        Scanner scanner = new Scanner(System.in);
        RoomMap roomMap = new RoomMap("src/room.svg");

        lines = roomMap.getRoomLines();

        robot.addMQTTListener(new MQTTRobot.MQTTListener() {
            @Override
            public void onDriveReceived(float receivedDistance, boolean x_axis, int robotSensorAmount) {
                System.out.println("Robot drives " + receivedDistance + " cm on " + (x_axis ? "X Axis" : "Y Axis"));

                if (x_axis)
                    robotX += receivedDistance;
                else
                    robotY += receivedDistance;

                particle = new Particle(robotX, robotY, robotDeg, robotSensorAmount, 0);

                Point temp_intersection, intersection = null;
                JSONObject jsonResponse = new JSONObject();

                try {
                    jsonResponse.put("x_axis",x_axis);
                    for (int i = 0; i < particle.getIntersectionPoints().length; ++i) {
                        float distance = -1, temp_distance = 0;
                        ArrayList<Line> lines = roomMap.getRoomLines();

                        for (Line line : lines) {
                            temp_intersection = particle.findIntersection(line.getX1(), line.getY1(), line.getX2(), line.getY2(), (int) particle.getIntersectionPoints()[i].getX(), (int) particle.getIntersectionPoints()[i].getY());
                            if (temp_intersection != null) {
                                temp_distance = (float) Math.sqrt(Math.pow(particle.getPositionX() - temp_intersection.getX(), 2) + Math.pow(particle.getPositionY() - temp_intersection.getY(), 2));
                                if (temp_distance < distance || distance == -1) {
                                    distance = temp_distance;
                                    intersection = temp_intersection;
                                }
                            }
                        }
                        jsonResponse.put("" + particle.getSensorDegree(i), distance);
                        System.out.println("Robot measures for " + particle.getSensorDegree(i) + "deg " + distance + " cm");
                    }
                }
                catch(Exception e){e.printStackTrace();}
                if (!jsonResponse.toString().equals(""))
                    robot.publish(jsonResponse.toString(),MQTTRobot.TOPIC_SONIC_DISTANCE);
                System.out.print("\n");
            }
        });
        robot.startListeningThread();
    }

}
