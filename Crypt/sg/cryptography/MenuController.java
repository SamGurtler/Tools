package sg.cryptography;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.filechooser.FileSystemView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
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
    private static MenuItem tmp=new MenuItem("Move"),tmp2=new MenuItem("Move here");
    private ArrayList<TreeFileItem>/*[]*/ movingPool=new ArrayList<TreeFileItem>();
    private ContextMenu fileOptioner;
    private boolean obstruction=false,en=true,zipMode=true;
    private Stage obstructingStage= null;
    private Hashtable<String, TreeFileItem> filePaths=new Hashtable<String,TreeFileItem>();
    /*	Need to make method for finding TreeFileItem's children and 
     * to check TreeFileItem's children verus saved data in HashTable.
  	 */
    //Could be more stable so that if file doesn't exit, it won't cause an error.
    protected static class TreeFileItem extends TreeItem<String>{
    	/**PATH will be the file's absolute path*/
    	private final String PATH;
    	private final boolean FILE,ROOT;
    	/**File must exist*/
    	public TreeFileItem(File f) {
    		super(f.getName(),getFileIcon(f));
    		PATH=f.getAbsolutePath();
    		FILE=f.isFile();
    		if(!FILE)this.setExpanded(true);
    		ROOT=false;
    	}
    	public TreeFileItem(File f,boolean zipMode) {
    		super(zipMode?f.getName():f.getAbsolutePath(),getFileIcon(f));
    		PATH=zipMode?f.getAbsolutePath():this.getValue();
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
    	if(!obstruction) {
    		if(zipMode&&en) {
	    		Platform.runLater(() -> {
	    			System.out.println(obstruction=true);
	    		FileChooser fChooser = new FileChooser();
	        	fChooser.setTitle("Select Destination");
	        	fChooser.getExtensionFilters().add(new ExtensionFilter("Zip File",".zip"));
	        	Stage primary;
	        	showFile.setVisible(false);
	        	//(primary=(Stage) pr.getScene().getWindow()).hide();
	        	fChooser.showSaveDialog(obstructingStage=new Stage());
	        	obstructingStage.setAlwaysOnTop(true);
	        	//primary.show();
	        	showFile.setVisible(true);
	        	System.out.println(obstruction=false);
	    		});
    		}
    	}else obstructingStage.toFront();
    }
    @FXML
    public void add(ActionEvent event) {
    	FXMLLoader loader=new FXMLLoader(getClass().getResource("./fFOptions.fxml"));
        if(!obstruction) {	
        	System.out.println(obstruction=true);
        	try {
	            showFile.setVisible(false);
	        	FFOptionsController CntrllrVBx=new FFOptionsController();
	            loader.setController(CntrllrVBx);
	            VBox vBox=(VBox)loader.load();
	            (obstructingStage=new Stage()).setScene(new Scene(vBox));
	            obstructingStage.setAlwaysOnTop(true);
	            obstructingStage.showAndWait();
	            TreeFileItem root=zipMode?(TreeFileItem)showFile.getSelectionModel().getSelectedItem():this.root;
	            showFile.setVisible (true);
	            if(root==null)root=this.root;
	            else if(root.FILE)root=(TreeFileItem) root.getParent();
	            for(File f:CntrllrVBx.getFiles()){
	            	if(!zipMode&&f.isDirectory()) {
	            		getAllFiles(root,f,filePaths);
	            	}
	            	else if(f.isDirectory()) {
	            		TreeFileItem tmp =listFilesAndFilesSubDirectories(f,filePaths);
	            		if(this.root==null&&root==null)root=tmp;
	            		//for(TreeItem<String> x: (tmp.getPar ent()).getChildren())if()
	            		if(!root.getChildren().contains(tmp))root.getChildren().add(tmp);
	            		else System.out.println("The rename feature isn't implemented yet.");
	            	}
	            	else if(!filePaths.containsKey(f.getAbsolutePath())) {
	        			filePaths.put(f.getAbsolutePath(), new TreeFileItem(f,zipMode));
	        			if(root==null)System.out.println("Not implemented yet.");
	        			else root.getChildren().add(filePaths.get(f.getAbsolutePath()));
	            	}
	            }
	            if(this.root==null)treeUpdate(this.root=root);
	            else treeUpdate();
	        } catch (NullPointerException | IOException ex) {
	            //ex.printStackTrace();
	        }
        	System.out.println(obstruction=false);
        }else obstructingStage.toFront();
    }
    private void getAllFiles(TreeFileItem root, File directory, Hashtable<String, TreeFileItem> filePaths) {
    	if(root.ROOT) {
    		File[] fList=directory.listFiles();
            for(File f:fList){
            	if(!filePaths.containsKey(f.getAbsolutePath())) {
    	        	if(f.isFile()) {
    	        		filePaths.put(f.getAbsolutePath(),new TreeFileItem(f,zipMode));
    	    			root.getChildren().add(filePaths.get(f.getAbsolutePath()));
    	    		}
    	    		else if(f.isDirectory())getAllFiles(root,f,filePaths);
            	}
            	else if(fList==null);
            }

    	}else throw new IllegalArgumentException();
    }
    protected TreeFileItem listFilesAndFilesSubDirectories(File directory,Hashtable<String, TreeFileItem> filePaths){
    	TreeFileItem branch=filePaths.containsKey(directory.getAbsolutePath())?null:new TreeFileItem(directory);
        File[] fList=directory.listFiles();
        for(File f:fList){
        	if(!filePaths.containsKey(f.getAbsolutePath())) {
	        	if(f.isFile()) {
	        		filePaths.put(f.getAbsolutePath(),new TreeFileItem(f));
	    			branch.getChildren().add(filePaths.get(f.getAbsolutePath()));
	    		}
	    		else if(f.isDirectory())branch.getChildren().add(listFilesAndFilesSubDirectories(f,filePaths));
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
    @FXML
    public void remove(ActionEvent event) {
    	if(!obstruction) {
	    	try{
	    		TreeFileItem tmp=(TreeFileItem) showFile.getSelectionModel().getSelectedItem();
	    		//System.out.println(getRedirectedPath(tmp));
	    		if(tmp.getParent()==null) {
	    			removeFromHashTable(tmp);
	    			tmp.getChildren().clear();
	    		}
	    		else if(tmp!=null) {
	    			removeFromHashTable(tmp.getParent().getChildren().get(tmp.getParent().getChildren().indexOf(tmp)));
	    			tmp.getParent().getChildren().remove(tmp);
	    		}
	    	}catch(NullPointerException e){
	    		e.printStackTrace();
	    	}
    	}else obstructingStage.toFront();
    }
    private void removeFromHashTable(TreeItem<String> item) {
    	TreeFileItem tmp=(TreeFileItem)item;
		filePaths.remove(tmp.getPath());
    	for(TreeItem<String>  tFI:item.getChildren()) {
    		tmp=(TreeFileItem) tFI;
    		if(!tmp.FILE)removeFromHashTable(tmp);
    		filePaths.remove(tmp.getPath());
    	}
    }
    private String getRedirectedPath(TreeFileItem tFI) {
    	TreeFileItem tmp=(TreeFileItem)tFI.getParent();
    	if(tmp==null||tmp.isRoot())return tFI.getPath();
    	else return getRedirectedPath(tmp)+"\\"+tFI.getValue();
    }
    @FXML
    public void setZipMode(ActionEvent event){
    	//Make GUI that ask are you sure
    	/**This will remove all current files listed*/
    	zipMode=!zipMode;
    	movingPool=null;
    	showFile.setContextMenu(zipMode?fileOptioner:null);
    	filePaths=new Hashtable<String,TreeFileItem>();
    	showFile.getRoot().getChildren().removeAll(showFile.getRoot().getChildren());
    }
    @Override
    public void initialize(URL location, ResourceBundle resources){
    	if(root!=null) {
    		root.setExpanded(true);
    		showFile.setRoot(root);
    	}
    	MultipleSelectionModel tmp3;
		(tmp3 =showFile.getSelectionModel()).setSelectionMode(SelectionMode.MULTIPLE);
		showFile.setSelectionModel(tmp3);
    	tmp.setOnAction(e->{
    			boolean canContinue=false;
    			/* Need to make method for ordering of selected items that includes
    			 * lexicographical order before sorting TreeItem level.
    			 * */
    			for(TreeFileItem tri:showFile.getSelectionModel().getSelectedItems().stream(
    					).map(f->(TreeFileItem)f
    							).sorted((f,d)->showFile.getTreeItemLevel(f)-showFile.getTreeItemLevel(d)
    							).collect(Collectors.toList())
    				)
    			{
    				try{
    					if(!tri.isRoot()) {
	    					//tmptri.setExpanded(false);
	    					movingPool.add(tri);
	    					canContinue=true;
	    					TreeFileItem tmptrI=(TreeFileItem) tri.getParent();
	        				if(tmptrI!=null)tmptrI.getChildren().remove(tri);
	        				else System.out.println("We have a problem at"+tri.getValue()+".");
	    				}else {
	    					System.out.println(tri.getPath()+"->"+tri.getValue());
	    					System.out.println("\tIs a root:"+tri.isRoot());
	    					System.out.println("\tIs a file:"+tri.isFile());
	    				}
    				}catch(NullPointerException n) {
    					n.printStackTrace();
    				}
    			}
    			if(canContinue) {
    				showFile.getSelectionModel().clearSelection();
    				showFile.getContextMenu().getItems().remove(tmp);
    				showFile.getContextMenu().getItems().add(tmp2);
    				
    			}
    	});
    	tmp2.setOnAction(e->{
    		showFile.getContextMenu().getItems().remove(tmp2);
			TreeFileItem tmpTreeFileItem=(TreeFileItem)showFile.getSelectionModel().getSelectedItem();
			if(!tmpTreeFileItem.FILE)tmpTreeFileItem.getChildren().addAll(movingPool);
			else tmpTreeFileItem.getParent().getChildren().addAll(movingPool);
			movingPool.clear();
			showFile.getContextMenu().getItems().add(tmp);
		});
    	showFile.setContextMenu(new ContextMenu(new MenuItem("Rename"),new MenuItem("Remove"),tmp));
    	showFile.getContextMenu().getItems().get(0).setOnAction(e->{
    		showFile.edit((TreeFileItem)showFile.getSelectionModel().getSelectedItem());
    	});
    	showFile.getContextMenu().getItems().get(1).setOnAction(e->this.remove(e));
    	showFile.getContextMenu().setHideOnEscape(true);
    	fileOptioner=showFile.getContextMenu();
        makeKeys();
    }
}