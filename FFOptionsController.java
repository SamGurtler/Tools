import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
public class FFOptionsController {
                @FXML 
                private VBox vBox;
                private List<File> chosen=new ArrayList<File>();
                public FFOptionsController() {
                }
                @FXML 
                private void DChooser(ActionEvent event) {
                	Platform.runLater(() -> {
                		Stage fFStage=(Stage)vBox.getScene().getWindow();
                		fFStage.hide();
                		DirectoryChooser dirC=new DirectoryChooser();
                        chosen.add(dirC.showDialog(fFStage));
                        fFStage.close();
    	            });
                }
                @FXML
                private void FChooser(ActionEvent event) {
                	Platform.runLater(() -> {
                		Stage fFStage=(Stage)vBox.getScene().getWindow();
                		fFStage.hide();
                		FileChooser fileC=new FileChooser();
                        chosen=fileC.showOpenMultipleDialog(fFStage);
                        fFStage.close();
    	            });
                }
                public List<File> getFiles(){
                	return chosen;
                }
            }