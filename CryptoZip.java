import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
public class CryptoZip extends Application {
    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader=new FXMLLoader(getClass().getResource("./Menu.fxml"));
        Parent root;
        Scene scene;
		try {
			MenuController controller=loader.getController();
			root = (VBox)loader.load();
			scene = new Scene((VBox)root,1250,917);
		} catch (IOException e) {
			e.printStackTrace();
			scene = new Scene((root=(Parent)(new Pane())),1250,917);
		}
        primaryStage.setTitle("Cryptographic Compresser");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
