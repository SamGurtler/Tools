package sg.cryptography;
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
                private boolean en;
                private List<File> chosen;
                public FFOptionsController() {
                	this(true);
                }
                public FFOptionsController(boolean en) {
                	this.en=en;
                	chosen=new ArrayList<File>();
                }
                @FXML 
                private void DChooser(ActionEvent event) {
                	Platform.runLater(() -> {
                		Stage fFStage=(Stage)vBox.getScene().getWindow();
                		fFStage.hide();
                		DirectoryChooser dirC=new DirectoryChooser();
                        File f=dirC.showDialog(fFStage);
                		if(f!=null)chosen.add(f);
                        fFStage.close();
    	            });
                }
                @FXML
                private void FChooser(ActionEvent event) {
                	Platform.runLater(() -> {
                		Stage fFStage=(Stage)vBox.getScene().getWindow();
                		fFStage.hide();
                		FileChooser fileC=new FileChooser();
                		List<File> f=fileC.showOpenMultipleDialog(fFStage);
                		if(f!=null)chosen.addAll(f);
                        fFStage.close();
    	            });
                }
                public List<File> getFiles(){
                	return chosen;
                }
            }