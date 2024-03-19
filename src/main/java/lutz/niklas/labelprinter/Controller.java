package lutz.niklas.labelprinter;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSnackbar;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public JFXPasswordField password;
    private double x = 0,y = 0;

    @FXML
    private AnchorPane loginForm;

    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginForm.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });

        loginForm.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() - x);
            stage.setY(mouseEvent.getScreenY() - y);
        });
    }

    private void createGuestWindow() {
        // TODO
    }

    /**
     * Creates the JavaFX scene for an admin if the password is correct.
     * @throws IOException if the FXMLLoader experiences as I/O error
     */
    @FXML
    private void createAdminWindow() throws IOException {
        String pass = "19april2001";
        if (pass.equals(password.getText())) {
            JFXSnackbar error = new JFXSnackbar(loginForm);
            error.enqueue(new JFXSnackbar.SnackbarEvent(new Label("Falsches Passwort."), Duration.seconds(2), null));
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("admin.fxml"));
        Parent root = loader.load();
        AdminWindow controller = loader.getController();
        controller.setStage(stage);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
    }

    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    void closeProgram() {
        stage.close();
    }
}