import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MenuController implements Initializable{
    @FXML
    private ToggleGroup Mode;
    @FXML
    private HBox pu;
    @FXML
    private TextField puInput;
    @FXML
    private HBox pr;
    @FXML
    private TextField prInput;
    @FXML 
    private TreeView showFile;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private CheckBox checkZipmode;
    private long num=0L;
    private boolean en=true;
    @FXML
    public void en(ActionEvent event){
    }
    @FXML
    public void de(ActionEvent event){
    }
    public void makeKeys(){
        boolean cancel = false;
        if(!puInput.getText().isEmpty()||!prInput.getText().isEmpty()){
            //Make a window that ask are you sure you want to clear old keys
        }
        if(!cancel){
            try{
                RSA.MakeKeys runnableClass=new RSA.MakeKeys();
                Thread KeyMaker=new Thread(runnableClass);
                KeyMaker.start();
                KeyMaker.join();
                String[] tmp=runnableClass.getKeys();
                puInput.setText(tmp[0]);
                prInput.setText(tmp[1]);
            }catch(InterruptedException ex){
                System.err.println(ex);
            }
        }
    }
    @FXML
    public void keys(ActionEvent event){
            makeKeys();
    }
    @FXML
    public void send(InputMethodEvent event){
    }
    @FXML
    public void start(ActionEvent event){
    }
    @FXML
    public void add(ActionEvent event) {
        FXMLLoader loader=new FXMLLoader(getClass().getResource("./fFOptions.fxml"));
        try {
            FFOptionsController CntrllrVBx=new FFOptionsController();
            VBox vBox=(VBox)loader.load();
            Stage fFStage= (Stage)pu.getScene().getWindow();
            Scene tmp=fFStage.getScene();
            vBox.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST,(WindowEvent event1)-> {
            fFStage.setScene(tmp);
            System.out.println("HUH!");
            //Supposed to add file paths into the Treeview object called showFile
            //TreeItem<String> root=new TreeItem<>();
            //for(File f : CntrllrVBx.getFiles())root.getChildren().add(new TreeItem<>(f.toString()));
            });
            fFStage.setScene(new Scene(vBox));
        } catch (IOException ex) {
            //Logger.getLogger(MenuController.class.getName()).log(Level.INFO, null, ex);
        }
    }
    @FXML
    public void remove(ActionEvent event){
    }
    @FXML
    public void setZipMode(ActionEvent event){
    }
    @Override
    public void initialize(URL location, ResourceBundle resources){
        showFile=new TreeView<String>(new TreeItem<String>("Files and Folders"));
        makeKeys();
    }
}