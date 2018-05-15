package sg.cryptography;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

public class ButtonCell extends TreeCell<String> {
	private Button button;
	private static TreeItem<String> selectedTreeItem;

	public ButtonCell(EventHandler<ActionEvent> e) {
		button = new Button("Select");
		button.setOnAction(e);

	}

	@Override
	public void startEdit() {
		super.startEdit();
		setText(getString());
		setGraphic(button);
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getString());
		setGraphic(getTreeItem().getGraphic());
	}

	@Override
	public void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				setGraphic(button);
				setText(getString());
			} else {
				setText(getString());
				setGraphic(getTreeItem().getGraphic());
			}
		}
	}

	private String getString() {
		return getItem() == null ? "" : getItem().toString();
	}

	public static TreeItem<String> getSelectedTreeItem() {
		return selectedTreeItem;
	}
}
