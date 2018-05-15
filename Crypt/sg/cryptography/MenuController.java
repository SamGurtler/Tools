package sg.cryptography;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

public class MenuController extends VBox implements Initializable {
	@FXML
	private ToggleGroup Mode;
	@FXML
	private HBox pu, pr;
	@FXML
	private TextField puInput, prInput;
	@FXML
	private TreeView<String> showFile;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private StackPane stackPane;
	@FXML
	private ScrollPane selectionPane;
	/* Variable num is for future implementation of various sized keys */
	@SuppressWarnings("unused")
	private long num = 0L;
	private static MenuItem tmp = new MenuItem("Move"), tmp2 = new MenuItem("Move here");
	private ArrayList<TreeFileItem>/* [] */ movingPool = new ArrayList<TreeFileItem>();
	private ContextMenu fileOptioner;
	private boolean obstruction = false, en = true, zipMode = true;
	private Stage obstructingStage = null;
	//private Hashtable<String, TreeFileItem> filePaths = new Hashtable<String, TreeFileItem>();
	private InnerClass3<String,Path,TreeFileItem,Map<Path,TreeFileItem>> mess=new InnerClass3<String,Path,TreeFileItem,Map<Path,TreeFileItem>>(new Hashtable<>(),altNewLeaf);
	// Could be more stable so that if file doesn't exit, it won't cause an error.
	public MenuController() {
		// super();
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Menu.fxml"));
			fxmlLoader.setController(this);
			fxmlLoader.setRoot(this);
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	protected static class TreeFileItem extends TreeItem<String> {
		/** PATH will be the file's absolute path */
		private final Path PATH;
		private final boolean FILE, ROOT;
		/** File must exist */
		public TreeFileItem(File f) {
			this(f,true);
			if (!FILE)this.setExpanded(true);
		}
		private TreeFileItem(String text,File f) {
			super(text,getFileIcon(f));
			PATH=f.toPath();
			FILE=f.isFile();
			ROOT=false;
		}
		public TreeFileItem(File f, boolean zipMode) {
			this(zipMode ? f.getName() : f.getAbsolutePath(),f);
		}
		public TreeFileItem(String rootName) {
			super(rootName);
			ROOT = true;
			FILE = false;
			PATH = null;
		}
		public Path getPath() {
			return PATH;
		}

		public boolean isFile() {
			return FILE;
		}

		public boolean isRoot() {
			return ROOT;
		}
		public static ImageView getFileIcon(File f) {
			BufferedImage tmpBImg = ((BufferedImage) ((javax.swing.ImageIcon) FileSystemView.getFileSystemView()
					.getSystemIcon(f)).getImage());
			Image tmpImage = SwingFXUtils.toFXImage(tmpBImg,
					new WritableImage(tmpBImg.getWidth(), tmpBImg.getHeight()));
			return new ImageView(tmpImage);
		}
	}

	private TreeFileItem root = new TreeFileItem("Files and Folders");

	@FXML
	public void en(ActionEvent event) {
		en = true;
	}

	@FXML
	public void de(ActionEvent event) {
		en = false;
	}

	public void makeKeys() {
		boolean cancel = false;
		if (!puInput.getText().isEmpty() || !prInput.getText().isEmpty()) {
			// Make a window that ask are you sure you want to clear old keys
		}
		if (!cancel) {
			try {
				RSA.MakeKeys runnableClass = new RSA.MakeKeys();
				Thread KeyMaker = new Thread(runnableClass);
				KeyMaker.start();
				KeyMaker.join();
				String[] tmp = runnableClass.getKeys();
				puInput.setText(tmp[0]);
				prInput.setText(tmp[1]);
			} catch (InterruptedException ex) {
				System.err.println(ex);
			}
		}
	}

	@FXML
	public void keys(ActionEvent event) {
		makeKeys();
	}

	@FXML
	public void start(ActionEvent event) {
		if (!obstruction)
			if (zipMode && en)
				Platform.runLater(() -> {
					// System.out.println(obstruction=true);
					FileChooser fChooser = new FileChooser();
					fChooser.setTitle("Select Destination");
					fChooser.getExtensionFilters().add(new ExtensionFilter("Zip File", ".zip"));
					// Stage primary;
					showFile.setVisible(false);
					fChooser.showSaveDialog(obstructingStage = new Stage());
					obstructingStage.setAlwaysOnTop(true);
					showFile.setVisible(true);
					System.out.println(obstruction = false);
				});
			else
				obstructingStage.toFront();
	}
	
	private static class InnerClass<E,K,V extends TreeItem<E>,M extends Map<K,V>>{
		private final M map;
		public InnerClass(M map) {
			this.map=map;
		}
		public static <V>boolean hereIsEnd(V here,V end){
			return here.equals(end);
		}
		public static <E,V extends TreeItem<E>>boolean branchContains(V parent,V child) {
			return parent.getChildren().filtered(sib->child.getValue().equals(sib.getValue())).size()>0;
		}
		public boolean hasBeenAdded(K f){
			return map.containsKey(f);
		}
		public void addToHash(V item,K f) {
			map.put(f,item);
		}
		public static <E,T extends TreeItem<E>> T addToBranch(T parent,T child){
			parent.getChildren().add(child);
			return child;
		}
		public void add(V branch,V newLeaf,K file,Function<V,V> altNewLeaf) {
			add(branch,newLeaf,file,altNewLeaf,InnerClass::addToBranch,this::addToHash,this::hasBeenAdded,InnerClass::branchContains);
		}
		/*generic method intended for different types of TreeItems*/
		public static <U,K> void add(U branch,U newLeaf,K file,Function<U,U> altNewLeaf,BinaryOperator<U> addToBranch,BiConsumer<U,K> addToHash, Predicate<K> hasBeenAdded,BiPredicate<U,U> branchContains) {
			if(hasBeenAdded.negate().test(file))add(branch,newLeaf,file,altNewLeaf,addToBranch,addToHash,branchContains);
		}
		/*generic method intended for different types of TreeItems*/
		public static <U,K> void add(U branch,U newLeaf,K file,Function<U,U> altNewLeaf,BinaryOperator<U> addToBranch,BiConsumer<U,K> addToHash,BiPredicate<U,U> branchContains) {
			if(branchContains.negate().test(branch,newLeaf)) {
				U attachedLeaf=addToBranch.apply(branch,newLeaf);
				addToHash.accept(attachedLeaf, file);
			}else add(branch,altNewLeaf.apply(newLeaf),file,altNewLeaf,addToBranch,addToHash,branchContains);
		}
		@SuppressWarnings("unchecked")
		public void remove(V item,Function<V,K> key) {
			V tmp;
			if(item.getParent()!=null)removeLeaf(item,key);
			for (TreeItem<E> tFI : item.getChildren()) {
				tmp=(V)tFI;
				if (!tmp.isLeaf()&&tmp.getParent()!=null)remove(tmp,key);
				removeLeaf(tmp,key);
			}
		}
		private void removeLeaf(V leaf,Function<V,K> key) {
			map.remove(key.apply(leaf));
		}
		public void clear() {
			map.clear();
		}
	}
	//make leaf only inner class
	private static class InnerClass2<E,K,V extends TreeItem<E>,M extends Map<K,V>> extends InnerClass<E,K,V,M>{
		public void add(V branch,V newLeaf,K file,Function<V,V> altNewLeaf) {
			if(newLeaf.isLeaf())super.add(branch, newLeaf, file, altNewLeaf);
		}
		public InnerClass2(M map) {
			super(map);
		}
	}
	private static class InnerClass3<E,K,V extends TreeItem<E>,M extends Map<K,V>> extends InnerClass2<E,K,V,M>{
		private Function<V,V> altNewLeaf;
		public InnerClass3(M map,Function<V,V> altNewLeaf) {
			super(map);
			this.altNewLeaf=altNewLeaf;
		}
		public void add(V branch,V newLeaf,K file) {
			super.add(branch, newLeaf, file, altNewLeaf);
		}
	}
	
	private static Function<TreeFileItem,TreeFileItem> altNewLeaf = (item)-> {item.setValue((String)item.getValue()+"'");return item;};
	//need to account for when root is null but branch is not.
	private TreeFileItem add(TreeFileItem branch,File f,boolean toBeZipped) {
		TreeFileItem result=branch;
		if(result==null&&showFile.getRoot()==null) throw new NullPointerException("Nothing to add to.");
		else if(result==null&&showFile.getRoot()!=null)result=add((TreeFileItem)showFile.getRoot(),f,toBeZipped);
		else if(branch.isFile())add((TreeFileItem)branch.getParent(),f,toBeZipped);
		else {
			BiFunction<TreeFileItem,File,TreeFileItem> addType;
			if(toBeZipped) {
				addType= (TreeFileItem t, File file)->{
					TreeFileItem newBranch= new TreeFileItem(file,toBeZipped); 
					t.getChildren().add(newBranch);
					return newBranch;
				};
			}else addType=(TreeFileItem t, File file)->t;
			addAll(result,f,toBeZipped,addType);
			}
		return result;
	}
	private void addAll(TreeFileItem branch, File f,boolean toBeZipped,BiFunction<TreeFileItem,File,TreeFileItem> addType) {
		if(f.isDirectory()) {
			File[] files=f.listFiles();
			if(files.length>0) {
				TreeFileItem tmpBranch=addType.apply(branch, f);
				for(File subf : files)addAll(tmpBranch,subf,toBeZipped,addType);
			}
		}else if(f.isFile())shortAdd(branch,f);
	}
	private void shortAdd(TreeFileItem branch,File file) {		
		mess.add(branch,new TreeFileItem(file,zipMode),file.toPath());
	}

	private static <T> LinkedList<T> getPath(T node,T finalNode,Function<T,T> traverse){
		return getPath(node,finalNode,traverse,Function.identity(),InnerClass::hereIsEnd);
	}
	private static <T> LinkedList<T> getPath(T node,T finalNode,Function<T,T> traverse,BiPredicate<T,T> finished){
		return getPath(node,finalNode,traverse,Function.identity(),finished);
	}
	private static <U,R> LinkedList<R> getPath(U node,U finalNode,Function<U,U> traverse,Function<U,R>whatIsAdded){
		return getPath(node,finalNode,traverse,whatIsAdded,InnerClass::hereIsEnd);
	}
	private static <U,R> LinkedList<R> getPath(U node,U finalNode,Function<U,U> traverse,Function<U,R>whatIsAdded,BiPredicate<U,U> finished){
		return getPath(node,finalNode,traverse,whatIsAdded,finished,new LinkedList<R>());
	}
	private static <U,R> LinkedList<R> getPath(U node,U finalNode,Function<U,U> traverse,Function<U,R>whatIsAdded,BiPredicate<U,U> finished,LinkedList<R> list){
		list.add(whatIsAdded.apply(node));
		U nextNode=traverse.apply(node);
		if(finished.negate().test(nextNode, finalNode))return getPath(nextNode,finalNode,traverse,whatIsAdded,finished,list);
		else return list;
	}
	
	//private static Function<T extends TreeItem<T>>
	
	@FXML
	public void add(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("./fFOptions.fxml"));
		if (!obstruction) {
			obstruction = true;
			try {
				showFile.setVisible(false);
				if (zipMode && showFile.getSelectionModel().getSelectedItems().size() > 1) {
					TreeFileItem absurdism = new TreeFileItem("Select One");
					absurdism.getChildren().addAll(showFile.getSelectionModel().getSelectedItems());
					
					TreeView<String> absurdity = new TreeView<>(absurdism);
					absurdity.setShowRoot(false);
					ScrollPane absurd = new ScrollPane(absurdity);
					absurd.fitToHeightProperty().bind(selectionPane.fitToHeightProperty());
					absurd.fitToWidthProperty().bind(selectionPane.fitToWidthProperty());
					absurdity.setCellFactory(new Callback<TreeView<String>,TreeCell<String>>(){
			            public TreeCell<String> call(TreeView<String> p) {
			                return new ButtonCell(e->{
			                	System.out.println("Worked");
			                	p.toBack();
			                	stackPane.getChildren().remove(absurd);	
			                	showFile.setVisible(true);
			                	showFile.toFront();
			                	showFile.getSelectionModel().select(p.getSelectionModel().getSelectedIndex());			                	
			                }	
			                );}
			            });
					absurdity.setEditable(true);
					stackPane.getChildren().add(absurd);
					//Do stuff here. b
				} else {
					FFOptionsController CntrllrVBx = new FFOptionsController();
					loader.setController(CntrllrVBx);
					VBox vBox = (VBox) loader.load();
					(obstructingStage = new Stage()).setScene(new Scene(vBox));
					obstructingStage.setAlwaysOnTop(true);
					obstructingStage.showAndWait();
					showFile.setVisible(true);
					TreeFileItem treeItem = zipMode ? (TreeFileItem) showFile.getSelectionModel().getSelectedItem()
							: root;
					if (treeItem == null)
						treeItem = root;
					else if (treeItem.FILE)
						treeItem = (TreeFileItem) treeItem.getParent();
					for (File f : CntrllrVBx.getFiles())add(treeItem,f,zipMode);
					if (root == null)
						treeUpdate(root = treeItem);
					else
						treeUpdate();
				}
			} catch (NullPointerException | IOException ex) {
				ex.printStackTrace();
			}
			obstruction = false;
		} else
			obstructingStage.toFront();
	}
	
	private void treeUpdate(TreeFileItem root) {
		showFile.setRoot(root);
	}

	private void treeUpdate() {
		treeUpdate(root);
	}
	
	private static Path getKey(TreeFileItem item) {
		return item.getPath();
	}
	@FXML
	public void remove(ActionEvent event) {
		if (!obstruction) {
			try {
				for (TreeFileItem tmp : reverse(fixSelect(showFile.getSelectionModel().getSelectedItems()))) {
					showFile.getSelectionModel().clearSelection();
					if (tmp.equals(showFile.getRoot())) {
						mess.remove(tmp,MenuController::getKey);
						//for(TreeItem<String> t: tmp.getChildren())mess.remove((TreeFileItem) t,MenuController::getKey);
						tmp.getChildren().clear();
					} else if (tmp != null) {
						mess.remove(tmp,MenuController::getKey);
						tmp.getParent().getChildren().remove(tmp);
					}
				}
				showFile.getSelectionModel().clearSelection();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} else
			obstructingStage.toFront();
	}

	@FXML
	public void setZipMode(ActionEvent event) {
		// Make GUI that ask are you sure
		/** This will remove all current files listed */
		zipMode = !zipMode;
		movingPool = null;
		showFile.setContextMenu(zipMode ? fileOptioner : null);
		mess.clear();
		showFile.getRoot().getChildren().removeAll(showFile.getRoot().getChildren());
	}

	private <E> List<E> reverse(List<E> l) {
		E tmp;
		for (int x = l.size() - 1; x > -1; x--) {
			tmp = l.get(x);
			l.remove(tmp);
			l.add(tmp);
		}
		return l;
	}

	private List<TreeFileItem> fixSelect(ObservableList<TreeItem<String>> l) {
		return l.stream().map(f -> (TreeFileItem) f)
				.sorted((f, d) -> showFile.getTreeItemLevel(f) - showFile.getTreeItemLevel(d))
				.collect(Collectors.toList());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (root != null) {
			root.setExpanded(true);
			showFile.setRoot(root);
			showFile.setShowRoot(true);
		}
		MultipleSelectionModel<TreeItem<String>> tmp3;
		(tmp3 = showFile.getSelectionModel()).setSelectionMode(SelectionMode./*SINGLE*/MULTIPLE);
		showFile.setSelectionModel(tmp3);
		tmp.setOnAction(e -> {
			boolean canContinue = false;
			/*
			 * Need to make method for ordering of selected items that includes
			 * lexicographical order before sorting TreeItem level.
			 */
			for (TreeFileItem tri : fixSelect(showFile.getSelectionModel().getSelectedItems())) {
				try {
					if (!tri.isRoot()) {
						// tmptri.setExpanded(false);
						movingPool.add(tri);
						canContinue = true;
						TreeFileItem tmptrI = (TreeFileItem) tri.getParent();
						if (tmptrI != null)
							tmptrI.getChildren().remove(tri);
						else
							System.out.println("We have a problem at" + tri.getValue() + ".");
					} else {
						System.out.println(tri.getPath() + "->" + tri.getValue());
						System.out.println("\tIs a root:" + tri.isRoot());
						System.out.println("\tIs a file:" + tri.isFile());
					}
				} catch (NullPointerException n) {
					n.printStackTrace();
				}
			}
			if (canContinue) {
				showFile.getSelectionModel().clearSelection();
				showFile.getContextMenu().getItems().remove(tmp);
				showFile.getContextMenu().getItems().add(tmp2);

			}
		});
		tmp2.setOnAction(e -> {
			showFile.getContextMenu().getItems().remove(tmp2);
			TreeFileItem tmpTreeFileItem = (TreeFileItem) showFile.getSelectionModel().getSelectedItem();
			if (!tmpTreeFileItem.FILE)
				tmpTreeFileItem.getChildren().addAll(movingPool);
			else
				tmpTreeFileItem.getParent().getChildren().addAll(movingPool);
			movingPool.clear();
			showFile.getContextMenu().getItems().add(tmp);
		});
		showFile.setContextMenu(new ContextMenu(new MenuItem("Rename"), new MenuItem("Remove"), tmp));
		showFile.getContextMenu().getItems().get(0).setOnAction(e -> {
			showFile.edit((TreeFileItem) showFile.getSelectionModel().getSelectedItem());
		});
		showFile.getContextMenu().getItems().get(1).setOnAction(e -> this.remove(e));
		showFile.getContextMenu().setHideOnEscape(true);
		fileOptioner = showFile.getContextMenu();
		makeKeys();
	}
}