import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FFOptionsController {
                @FXML 
                private VBox vBox;
                private List<File> chosen;
                public FFOptionsController(){
                }
                @FXML 
                private void DChooser(ActionEvent event) {
                    Stage fFStage=(Stage)vBox.getScene().getWindow();
                    //fFStage.hide();
                    DirectoryChooser dirC=new DirectoryChooser();
                    chosen.add(dirC.showDialog(fFStage));
                    //fFStage.close();
                }
                @FXML
                private void FChooser(ActionEvent event) {
                    Stage fFStage=(Stage)vBox.getScene().getWindow();
                    //fFStage.hide();
                    FileChooser fileC=new FileChooser();
                    chosen=fileC.showOpenMultipleDialog(fFStage);
                    //fFStage.close();
                }
                public List<File> getFiles(){
                    return chosen;
                }
            }