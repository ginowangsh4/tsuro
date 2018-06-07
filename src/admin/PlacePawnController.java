package tsuro.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PlacePawnController {
    @FXML
    private ImageView boardImageView;
    @FXML
    private ChoiceBox<Side> sideDropdown;
    @FXML
    private ChoiceBox<Integer> indexDropdown;
    @FXML
    private Button submitButton;

    private enum Side { TOP, LEFT, RIGHT, BOTTOM }
    private Side startSide;
    private Integer startIndex;

    public void initialize() throws FileNotFoundException {
        boardImageView.setImage(new Image(new FileInputStream("image/board/board.png")));
        sideDropdown.getItems().addAll(Side.TOP, Side.BOTTOM, Side.LEFT, Side.RIGHT);
        for(int i = 0; i < 12; i++){
            indexDropdown.getItems().add(i);
        }

        sideDropdown.setOnMouseClicked(event -> {
            System.out.println("StartSide dropdown clicked");
        });

        indexDropdown.setOnMouseClicked(event -> {
            System.out.println("StartIndex dropdown clicked");
        });

        submitButton.setOnMouseClicked(event -> {
            System.out.println("Submit button clicked");
            startSide = sideDropdown.getValue();
            startIndex = indexDropdown.getValue();
            if (startSide != null && startIndex != null) {
                System.out.println("Selected side = " + startSide);
                System.out.println("Selected index = " + startIndex);

                App.socket.writeOutputToServer(startSide.toString() + "," + startIndex.toString());
                try {
                    System.out.println(App.socket.readInputFromServer());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                App.changeScene(submitButton, "PlayTurn.fxml");
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Tsuro Game Dialog");
                alert.setContentText("Shouldn't submit now. Please choose both side and index!");
                alert.showAndWait();
            }
        });
    }
}
