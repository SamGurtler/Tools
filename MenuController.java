import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
    private long num=0L;
    private boolean en,zipMode=true;
    private List<String> filePaths=new ArrayList<String>();
    private TreeItem<String> root=new TreeItem<String>("Files and Folders");
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
            loader.setController(CntrllrVBx);
            VBox vBox=(VBox)loader.load();
            Stage optionStage=new Stage();
            optionStage.setScene(new Scene(vBox));
            optionStage.showAndWait();
            for(File f:CntrllrVBx.getFiles()){
            	if(f.isDirectory())root.getChildren().add(listFilesAndFilesSubDirectories(f,filePaths,zipMode));
            	else{
            		filePaths.add(zipMode?f.getName():f.getAbsolutePath());
            		root.getChildren().add(new TreeItem<String>(zipMode?f.getName():f.getAbsolutePath()));
            	}
            }
            if(CntrllrVBx.getFiles()!=null)filePaths.add("$End$");
            treeUpdate();
        } catch (IOException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.INFO, null, ex);
        }
    }
    public TreeItem<String> listFilesAndFilesSubDirectories(File directory,List<String> filePaths){
    	return listFilesAndFilesSubDirectories(directory,filePaths,false);
    }
    public TreeItem<String> listFilesAndFilesSubDirectories(File directory,List<String> filePaths,boolean zipMode){
    	filePaths.add(zipMode?directory.getName():directory.getAbsolutePath());
    	TreeItem<String> branch=new TreeItem<String>(zipMode?directory.getName():directory.getAbsolutePath());
        File[] fList=directory.listFiles();
        for(File f:fList){
            if(f.isFile()){
            	filePaths.add(f.getName());
            	branch.getChildren().add(new TreeItem<String>(zipMode?f.getName():f.getAbsolutePath()));
            }else if(f.isDirectory()){
                branch.getChildren().add(listFilesAndFilesSubDirectories(f,filePaths,zipMode));
            }
        }
        filePaths.add("$End$");
        return branch;
    }
    private void treeUpdate() {
    	showFile.setRoot(root);
    }
    @FXML
    public void remove(ActionEvent event) {
    	try{
    		TreeItem tmp=(TreeItem) showFile.getSelectionModel().getSelectedItem();
    		if(tmp!=null)tmp.getParent().getChildren().remove(tmp);
    	}catch(NullPointerException e){
    		
    	}
    }
    @FXML
    public void setZipMode(ActionEvent event){
    	zipMode=!zipMode;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources){
        root.setExpanded(true);
    	showFile.setRoot(root);
        makeKeys();
    }
}