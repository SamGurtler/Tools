import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import javafx.application.Platform;
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
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.TreeSet; 
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
    private Hashtable<String,TreeFileItem> filePaths=new Hashtable<String, TreeFileItem>();
    private TreeItem root= new TreeItem<String>("Files and Folders");
    /*protected class HashMap extends java.util.Hashtable<String,TreeFileItem>{
    	public HashMap() {
    		super();	
    	}
    	/*public TreeFileItem put(File f){
    		return this.put(f,false);
    	}
    	public TreeFileItem put(File f,boolean zipMode){
    		return this.put(f.toPath(),new TreeFileItem(f,zipMode));
    	}
    }*/
    private static class TreeFileItem extends TreeItem<String>{
    	 final static javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
         
		public TreeFileItem(File f,boolean justName){
			super(justName?f.getName():f.getAbsolutePath());
			//setIconToImageView(/*fc.getFileView().getIcon(f)FileSystemView.getFileSystemView().getSystemIcon(f));
		}
		private void setIconToImageView(Icon thumbnail) {
			Runnable fetchIcon=()->{
			//File file = null;
			ImageView imageView=null;
            //file = File.createTempFile("icon", ".png"); 
			BufferedImage bufferedImage = new BufferedImage(
					 thumbnail.getIconWidth(), 
					 thumbnail.getIconHeight(), 
	                    BufferedImage.TYPE_INT_ARGB
	                );
			 thumbnail.paintIcon(null, bufferedImage.getGraphics(), 0, 0);

	                Platform.runLater(() -> {
	                    Image fxImage = SwingFXUtils.toFXImage(
	                        bufferedImage, null
	                    );
	                    super.setGraphic(new ImageView(fxImage));
	                });
	        };
	        javax.swing.SwingUtilities.invokeLater(fetchIcon);	
		}
    	public TreeFileItem(File f) {
    		new TreeFileItem(f,false);
		}
    	
    }
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
            if(this.root!=null&&root==null)root=(TreeFileItem)this.root;
            for(File f:CntrllrVBx.getFiles()){
            	if(f.isDirectory()) {
            		if(root==null)root=listFilesAndFilesSubDirectories(f,filePaths,zipMode);
            		TreeFileItem tmp;
            		System.out.println(((tmp=filePaths.get(f.getAbsolutePath())).toString()+"<-Start"));
            		root.getChildren().add(tmp);
            	}
            	/*else{
            		filePaths.put(f.getAbsolutePath(),new TreeFileItem(f,zipMode));
            		System.out.println((filePaths.get(f.getAbsolutePath()).toString()));
            		root.getChildren().add(filePaths.get(f.getAbsolutePath()));
	            	//root.getChildren().add(new TreeItem<String>(f.getName()));
            	}*/
            }
//            consoleFileSelectionView();
            if(this.root==null)treeUpdate(this.root=root);
            else treeUpdate();
        } catch (NullPointerException | IOException ex) {
            ex.printStackTrace();//Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /*private void consoleFileSelectionView() {
    	for(int count=0,fileFolderCount=0,cnctcount=-1;count<filePaths.size();count++) {
        	String tmp=filePaths.get(count);
    		if(tmp.equals(">")) {
    			fileFolderCount=-1;
    			cnctcount++;
    		}else if(tmp.equals("<")) {
    			fileFolderCount--;
    			cnctcount--;
    		}else tmp=tmp.substring(tmp.lastIndexOf('\\')+1,tmp.length()-1);
    		fileFolderCount++;
    		if(fileFolderCount>1) {
    			for(int x=cnctcount;x>0;x--)tmp="  "+tmp;
    			if(!tmp.contains(">"))tmp="  "+tmp+"\n";
    			else tmp=tmp+"\n";
    		}else if(fileFolderCount==0)for(int x=cnctcount;x>0;x--)tmp="  "+tmp;
    		else tmp=tmp+"\n";
    		System.out.print(tmp);
        }
    }*/
    public TreeFileItem listFilesAndFilesSubDirectories(File directory,Hashtable<String,TreeFileItem> filePaths){
    	return listFilesAndFilesSubDirectories(directory,filePaths,false);
    }
    public TreeFileItem listFilesAndFilesSubDirectories(File directory,Hashtable<String, TreeFileItem> filePaths,boolean zipMode){
    	//filePaths.add(">");
    	//String fileName=directory.getAbsolutePath();
    	TreeFileItem branch=new TreeFileItem(directory,zipMode);
        File[] fList=directory.listFiles();
        for(File f:fList){
        	//fileName=f.getAbsolutePath();
        	//System.out.println("You got here!");
    		if(f.isFile()) {
    			filePaths.put(f.getAbsolutePath(),new TreeFileItem(f,zipMode));
    			System.out.println((filePaths.get(f.getAbsolutePath()).toString()));
    			branch.getChildren().add(filePaths.get(f.getAbsolutePath()));
    		}
    		else if(f.isDirectory())branch.getChildren().add(listFilesAndFilesSubDirectories(f,filePaths,zipMode));
        }
        //filePaths.add("<");
        filePaths.put(directory.getAbsolutePath(),branch);
        System.out.println((filePaths.get(directory.getAbsolutePath()).toString()));
        return branch;
    }
    private void treeUpdate(TreeItem<String> root) {
    	showFile.setRoot(root);
    }
    private void treeUpdate() {
    	treeUpdate(this.root);
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
    	//Make GUI that ask are you sure
    	//This will remove all current files listed
    	zipMode=!zipMode;
    	filePaths=new Hashtable<String, TreeFileItem>();
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