import java.util.ArrayList;
import java.util.Scanner;

public class App {

    static int robotX = 0;
    static final int robotY = 65;
    static ArrayList<Line> lines;

    public static void main (String arg []){
        MQTTRobot robot = new MQTTRobot();
        Scanner scanner = new Scanner(System.in);
        RoomMap roomMap = new RoomMap("src/houses.svg");

        lines = roomMap.getRoomLines();

        robot.addMQTTListener(new MQTTRobot.MQTTListener() {
            @Override
            public void onDriveReceived(float distanceInCM) {
                System.out.println("Robot drives " + distanceInCM + " cm");

                robotX += (int)distanceInCM;

                int ultrasonicDistance = 0;

                for (int i=0;i<lines.size();++i)
                {
                    Line currentLine = lines.get(i);
                    if(robotX >= currentLine.x1 && robotX < currentLine.x2)
                    {
                        ultrasonicDistance = robotY - currentLine.y2;
                        break;
                    }
                }

                System.out.println("Robot measures " + ultrasonicDistance + " cm");
                robot.publish(ultrasonicDistance+"",MQTTRobot.TOPIC_SONIC_DISTANCE);
            }
        });
        robot.startListeningThread();


    }

}
