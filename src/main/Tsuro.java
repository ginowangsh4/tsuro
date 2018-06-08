package tsuro;

import javafx.application.Application;
import tsuro.admin.App;

public class Tsuro {
    public static void main(String[] args) {
        // start a local host for networked tournament
        Server server = Server.getInstance();
        try {
            server.startGame(1,7,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
