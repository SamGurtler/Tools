package sg.cryptography;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.filechooser.FileSystemView;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
	private InnerClass3<String, File, Item<File,String>, Set<File>> mess = new InnerClass3<String, File,Item<File,String>, Set<File>>(
			new HashSet(), altNewLeaf); 
	// Could be more stable so that if file doesn't exit, it won't cause an error.
	public MenuController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Menu.fxml"));
			fxmlLoader.setController(this);
			fxmlLoader.setRoot(this);
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	protected abstract static class Item<E,V> extends TreeItem<V> {
		private final ReadOnlyProperty<E> PATH;
		private final boolean FILE, ROOT;
		protected Item(V f,Node n,E e,Predicate<E> isFile) {
			super(f,n);
			PATH=new ReadOnlyObjectWrapper<E>(e);
			FILE=isFile.test(e);
			ROOT=false;
		}
		protected Item(V rootName) {
			super(rootName);
			ROOT = true;
			FILE = false;
			PATH = null;
		}
		public ReadOnlyProperty<E> getPath() {
			return PATH;
		}

		public boolean isFile() {
			return FILE;
		}

		public boolean isRoot() {
			return ROOT;
		}
	}
	private static class TreeFileItem extends Item<File,String> {
		public TreeFileItem(File f) {
			this(f,true);
		}
		public TreeFileItem(File f, boolean zipMode) {
			this(zipMode ? f.getName() : f.getAbsolutePath(),f);
			if (!super.FILE)this.setExpanded(true);
		}
		private TreeFileItem(String text,File f) {
			super(text,(Node)getFileIcon(f),f,(File ff)->ff.isFile());
		}
		public TreeFileItem(String rootName) {
			super(rootName);
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
	
	private abstract static class InnerClass<E,K,V extends TreeItem<E>,S extends Set<K>>{
		private final S set;
		protected InnerClass(S arg) {
			this.set=arg;
		}
		private static <V>boolean hereIsEnd(V here,V end){
			return here.equals(end);
		}
		private static <E,V extends TreeItem<E>>boolean branchContains(V parent,V child) {
			return parent.getChildren().stream().anyMatch(l->l.getValue().equals(child.getValue()));
		}
		private static <E,T extends TreeItem<E>> void addToBranch(T parent,T child){
			parent.getChildren().add(child);
		}
		protected boolean hasBeenAdded(K f){
			return set.contains(f);
		}
		protected void addToHash(K f) {
			set.add(f);
		}
		protected abstract V altNewLeaf(V leaf);
		public void add(V branch,V newLeaf,K file) {
			add(branch,newLeaf,file,this::altNewLeaf,InnerClass::addToBranch,this::addToHash,this::hasBeenAdded,InnerClass::branchContains);
		}
		private void add(V branch,V newLeaf,K file,Function<V,V> altNewLeaf) {
			add(branch,newLeaf,file,altNewLeaf,InnerClass::addToBranch,this::addToHash,this::hasBeenAdded,InnerClass::branchContains);
		}
		/*generic method intended for different types of TreeItems*/
		public static <U,K> void add(U branch,U newLeaf,K file,Function<U,U> altNewLeaf,BiConsumer<U,U> addToBranch,Consumer<K> addToSet, Predicate<K> hasBeenAdded,BiPredicate<U,U> branchContains) {
			if(hasBeenAdded.negate().test(file))add(branch,newLeaf,file,altNewLeaf,addToBranch,addToSet,branchContains);
		}
		/*generic method intended for different types of TreeItems*/
		public static <U,K> void add(U branch,U newLeaf,K file,Function<U,U> altNewLeaf,BiConsumer<U,U> addToBranch,Consumer<K> addToSet,BiPredicate<U,U> branchContains) {
			if(branchContains.negate().test(branch,newLeaf)) {
				addToBranch.accept(branch,newLeaf);
				addToSet.accept(file);
			}else add(branch,altNewLeaf.apply(newLeaf),file,altNewLeaf,addToBranch,addToSet,branchContains);
		}
		public void fullRemove(V internal,Function<V,K> key) {
			V parent=(V) internal.getParent();
			removeSubTFromHash(internal,key);
			if(parent==null)internal.getChildren().clear();
			else parent.getChildren().remove(internal);
		}
		private void removeSubTFromHash(V tmp2,Function<V,K> key) {
			V tmp;
			if(tmp2.getParent()!=null)removeLeaf(tmp2,key);
			for (TreeItem<E> tFI : tmp2.getChildren()) {
				tmp=(V)tFI;
				if (!tmp.isLeaf())removeSubTFromHash(tmp,key);
				else removeLeaf(tmp,key);
				}
		}
		private void removeLeaf(V leaf,Function<V,K> key) {
			set.remove(key.apply(leaf));
		}
		public void clear() {
			set.clear();
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(this.getClass().getName()+" [map=").append(set).append("]");
			return builder.toString();
		}
		
	}
	//make leaf only inner class
	private static abstract class InnerClass2<E,K,V extends TreeItem<E>,M extends Set<K>> extends InnerClass<E,K,V,M>{
		public final void add(V branch,V newLeaf,K file) {
			if(newLeaf.isLeaf())super.add(branch, newLeaf, file);
			else throw new IllegalArgumentException(this.getClass().getName()+": Invalid newLeaf has children.");
		}
		protected InnerClass2(M map) {
			super(map);
		}
	}
	protected static class InnerClass3<E,K,V extends TreeItem<E>,M extends Set<K>> extends InnerClass2<E,K,V,M>{
		private Function<V,V> altNewLeaf;
		public InnerClass3(M map,Function<V,V> altNewLeaf) {
			super(map);
			this.altNewLeaf=altNewLeaf;
		}
		@Override
		protected V altNewLeaf(V leaf) {
			return altNewLeaf.apply(leaf);
		}
		public void add(V branch,V newLeaf,K file,Function<V,V> altNewLeaf) {
			Function<V,V> tmp=this.altNewLeaf;
			this.altNewLeaf=altNewLeaf;
			add(branch,newLeaf,file);
			this.altNewLeaf=tmp;
		}
	}
	private static Function<Item<File, String>, Item<File, String>> altNewLeaf = (item)-> {item.setValue(item.getValue()+"*");return item;};
	private static <E,M extends TreeItem<E>> M mergeAdd(M parent,Supplier<M> child){
		TreeItem<E> newBranch= child.get();
		final ObjectProperty<E> tmp =newBranch.valueProperty();
		if(InnerClass.branchContains(parent,newBranch))
			newBranch=parent.getChildren().stream().filter((TreeItem<E> l)->!l.isLeaf()&&l.valueProperty().isEqualTo(tmp).getValue()).findFirst().get();
		else if(parent.valueProperty().isNotEqualTo(newBranch.valueProperty()).getValue())parent.getChildren().add(newBranch);
		//else newBranch=parent;
		return (M) newBranch;
	}
	//need to account for when root is null but branch is not.
	private TreeFileItem add(TreeFileItem branch,File f,boolean toBeZipped) {
		TreeFileItem result=branch;
		if(result!=null&&!result.isFile()){
			BiFunction<TreeFileItem,File,TreeFileItem> addType;
			if(toBeZipped) {
				addType= (TreeFileItem t, File file)->mergeAdd(t,()->new TreeFileItem(file,toBeZipped));
			}else addType=(TreeFileItem t, File file)->t;
			addAll(result,f,toBeZipped,addType);
		}else if(result!=null)add((TreeFileItem)branch.getParent(),f,toBeZipped);
		else if(showFile.getRoot()!=null) result=add((TreeFileItem)showFile.getRoot(),f,toBeZipped);
		else throw new NullPointerException("Nothing to add to."); 
		return result;
	}
	private void addAll(TreeFileItem branch, File f,boolean toBeZipped,BiFunction<TreeFileItem,File,TreeFileItem> addType) {
		if(f.isDirectory()) {
			File[] files=Arrays.stream(f.listFiles()).filter(((Predicate<File>)mess::hasBeenAdded).negate()).toArray(File[]::new);
			if(files.length>0) {
				TreeFileItem tmpBranch=addType.apply(branch, f);
				for(File subf : files)addAll(tmpBranch,subf,toBeZipped,addType);
			}
		}else if(f.isFile())shortAdd(branch,f);
	}
	private void shortAdd(TreeFileItem branch,File file) {		
		mess.add(branch,new TreeFileItem(file,zipMode),file);
	}

	private static <U> LinkedList<U> getPath(U node,U finalNode,Function<U,U> traverse){
		return getPath(node, traverse, Function.identity(), (Predicate<U>)finalNode::equals);
	}
	private static <T> LinkedList<T> getPath(T node,Function<T,T> traverse,Predicate<T> finished){
		return getPath(node,traverse,Function.identity(),finished);
	}
	private static <U,R> LinkedList<R> getPath(U node,U finalNode,Function<U,U> traverse,Function<U,R>whatIsAdded){
		return getPath(node,traverse,whatIsAdded,finalNode::equals);
	}
	private static <U,R> LinkedList<R> getPath(U node,Function<U,U> traverse,Function<U,R>whatIsAdded,Predicate<U> finished){
		return getPath(node,traverse,whatIsAdded,finished,new LinkedList<R>());
	}
	private static <U,R> LinkedList<R> getPath(U node,Function<U,U> traverse,Function<U,R>whatIsAdded,Predicate<U> finished, LinkedList<R> list){
		list.add(whatIsAdded.apply(node));
		if(finished.negate().test(node))list=getPath(traverse.apply(node),traverse, whatIsAdded, finished,list);
		return list;
	}
	
	@FXML
	public void add(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("./fFOptions.fxml"));
		if (!obstruction) {
			obstruction = true;
			try {
				showFile.setVisible(false);
				if (zipMode && showFile.getSelectionModel().getSelectedItems().size() > 1) {
					TreeItem<String> absurdism = new TreeItem<>("Select One");
					absurdism.getChildren().addAll(showFile.getSelectionModel().getSelectedItems().stream().map(i->{
						//Use mergeAdd to utilize TreeView properly instead of implementing like ListView. 
						//mergeAdd()
						return new TreeItem<String>(reverse(getPath(i,(TreeItem<String>)root,(child)->child.getParent(),(ii->ii.getValue()))).stream().reduce((a,b)->a+"/"+b).get());
						}).toArray(TreeItem[]::new));
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
					TreeFileItem treeItem = zipMode ? (TreeFileItem) showFile.getSelectionModel().getSelectedItem(): root;
					for(File f : CntrllrVBx.getFiles())add(treeItem,f,zipMode);
				}
			} catch (NullPointerException | IOException ex) {
				ex.printStackTrace();
			}
			obstruction = false;
		} else
			obstructingStage.toFront();
	}

	private static <T> T getKey(Item<T,?> item) {
		return (T) item.getPath().getValue();
	}
	@FXML
	public void remove(ActionEvent event) {
		if (!obstruction) {
			try {
				for (Item tmp : reverse(fixSelect(TreeFileItem.class,showFile.getSelectionModel().getSelectedItems()))) {
					showFile.getSelectionModel().clearSelection();
					mess.fullRemove(tmp,MenuController::getKey);
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

	private <E,T extends Collection<E>> T reverse(T l) {
		Stack<E> filo=new Stack<>();
		for(E t:l)filo.add(t);
		l.clear();
		while(filo.isEmpty()==false)l.add(filo.pop());
		return l;
	}

	private <E,T extends TreeItem<E>> List<T> fixSelect(Class<T> t,ObservableList<? super T> l) {
		return l.stream().map(t::cast)
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
			for (TreeFileItem tri : fixSelect(TreeFileItem.class,showFile.getSelectionModel().getSelectedItems())) {
				try {
					if (!tri.isRoot()) {

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
			if (!tmpTreeFileItem.isFile())
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