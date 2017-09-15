import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.ResourceBundle;
import javax.swing.filechooser.FileSystemView;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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
    private TreeView<String> showFile;
    @FXML
    private ProgressBar progressBar;
    /*Variable num is for future implementation of various sized keys*/
    @SuppressWarnings("unused")
	private long num=0L;
    @SuppressWarnings("unused")
	private boolean en,zipMode=true;
    private Hashtable<String, TreeFileItem> filePaths=new Hashtable<String,TreeFileItem>();
    /*	Need to make method for finding TreeFileItem's children and 
     * to check TreeFileItem's children verus saved data in HashTable.
  	 */
    //Could be more stable so that if file doesn't exit, it won't cause an error.
    public static class TreeFileItem extends TreeItem<String>{
    	/**PATH will be the file's absolute path*/
    	private final String PATH;
    	private final boolean FILE,ROOT;
    	/**File must exist*/
    	public TreeFileItem(File f) {
    		super(f.getName(),getFileIcon(f));
    		PATH=f.getAbsolutePath();
    		FILE=f.isFile();
    		ROOT=false;
    	}
    	public TreeFileItem(String rootName) {
    		super(rootName);
    		ROOT=true;
    		FILE=false;
    		PATH="This is just the root, no file repesentation.";
    	}
    	public String getPath() {
    		return PATH;
    	}
    	public boolean isFile() {
    		return FILE;
    	}
    	public boolean isRoot() {
    		return ROOT;
    	}
    	public static ImageView getFileIcon(File f) {
        	BufferedImage tmpBImg= ((BufferedImage) ((javax.swing.ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(f)).getImage());
        	Image tmpImage= SwingFXUtils.toFXImage(tmpBImg,new WritableImage(tmpBImg.getWidth(),tmpBImg.getHeight()));
        	return new ImageView(tmpImage);
        }
    }
    private TreeFileItem root=new TreeFileItem("Files and Folders");
    @FXML
    public void en(ActionEvent event){
    	en=true;
    }
    @FXML
    public void de(ActionEvent event){
    	en=false;
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
    public void start(ActionEvent event){
    }
    /**Current implementation will cause logic error if file uses local folder icon*/
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
            TreeFileItem root=(TreeFileItem) showFile.getSelectionModel().getSelectedItem();
            if(root==null)root=this.root;
            else if(root.FILE)root=(TreeFileItem) root.getParent();
            for(File f:CntrllrVBx.getFiles()){
            	if(f.isDirectory()) {
            		TreeFileItem tmp =listFilesAndFilesSubDirectories(f,filePaths,zipMode);
            		if(this.root==null&&root==null)root=tmp;
            		root.getChildren().add(tmp);
            	}
            	else if(!filePaths.containsKey(f.getAbsolutePath())) {
        			filePaths.put(f.getAbsolutePath(), new TreeFileItem(f));
        			if(root==null)System.out.println("Not implemented yet.");
        			else root.getChildren().add(filePaths.get(f.getAbsolutePath()));
            	}
            }
            if(this.root==null)treeUpdate(this.root=root);
            else treeUpdate();
        } catch (NullPointerException | IOException ex) {
            ex.printStackTrace();
        }
    }
    public TreeFileItem listFilesAndFilesSubDirectories(File directory,Hashtable<String,TreeFileItem> filePaths){
    	return listFilesAndFilesSubDirectories(directory,filePaths,false);
    }
    public TreeFileItem listFilesAndFilesSubDirectories(File directory,Hashtable<String, TreeFileItem> filePaths,boolean zipMode){
    	TreeFileItem branch=filePaths.containsKey(directory.getAbsolutePath())?null:new TreeFileItem(directory);
        File[] fList=directory.listFiles();
        for(File f:fList){
        	if(!filePaths.containsKey(f.getAbsolutePath())) {
	        	if(f.isFile()) {
	        		filePaths.put(f.getAbsolutePath(),new TreeFileItem(f));
	    			branch.getChildren().add(filePaths.get(f.getAbsolutePath()));
	    		}
	    		else if(f.isDirectory())branch.getChildren().add(listFilesAndFilesSubDirectories(f,filePaths,zipMode));
        	}
        	else if(fList==null);
        }
        if(!filePaths.containsKey(directory.getAbsolutePath()))filePaths.put(directory.getAbsolutePath(),branch);
        return branch;
    }
    private void treeUpdate(TreeFileItem root) {
    	showFile.setRoot(root);
    }
    private void treeUpdate() {
    	treeUpdate(this.root);
    }
    /*private ImageView getFileIcon(File f) {
    	BufferedImage tmpBImg= ((BufferedImage) ((javax.swing.ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(f)).getImage());
    	Image tmpImage= SwingFXUtils.toFXImage(tmpBImg,new WritableImage(tmpBImg.getWidth(),tmpBImg.getHeight()));
    	return new ImageView(tmpImage);
    }*/
    //Need to make this remove data from HashTable as well.
    @FXML
    public void remove(ActionEvent event) {
    	try{
    		TreeFileItem tmp=(TreeFileItem) showFile.getSelectionModel().getSelectedItem();
    		if(tmp.getParent()==null)tmp.getChildren().clear();
    		else if(tmp!=null)tmp.getParent().getChildren().remove(tmp);
    	}catch(NullPointerException e){
    		
    	}
    }
    @FXML
    public void setZipMode(ActionEvent event){
    	//Make GUI that ask are you sure
    	/**This will remove all current files listed*/
    	zipMode=!zipMode;
    	filePaths=new Hashtable<String,TreeFileItem>();
    	showFile.getRoot().getChildren().removeAll(showFile.getRoot().getChildren());
    }
    @Override
    public void initialize(URL location, ResourceBundle resources){
    	if(root!=null) {
    		root.setExpanded(true);
    		showFile.setRoot(root);
    	}
        makeKeys();
    }
}