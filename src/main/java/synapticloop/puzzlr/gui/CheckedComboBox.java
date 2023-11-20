package synapticloop.puzzlr.gui;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.ComboPopup;

public class CheckedComboBox<E extends CheckableItem> extends JComboBox<E> {
	private static final long serialVersionUID = -6272739426128218788L;

	private boolean keepOpen;
	private transient ActionListener listener;
	private CheckBoxCellRenderer<E> checkBoxCellRenderer = new CheckBoxCellRenderer<>();
	public CheckedComboBox() {
		super();
	}

	public CheckedComboBox(ComboBoxModel<E> model) {
		super(model);
	}

	@Override public Dimension getPreferredSize() {
		return new Dimension(200, 20);
	}

	@Override public void updateUI() {
		setRenderer(null);
		removeActionListener(listener);
		super.updateUI();
		listener = e -> {
			if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
				updateItem(getSelectedIndex());
				keepOpen = true;
			}
		};

		CheckBoxCellRenderer<E> aRenderer = new CheckBoxCellRenderer<>();
		setRenderer(aRenderer);
		addActionListener(listener);
		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
		getActionMap().put("checkbox-select", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				Accessible a = getAccessibleContext().getAccessibleChild(0);
				if (a instanceof ComboPopup) {
					updateItem(((ComboPopup) a).getList().getSelectedIndex());
				}
			}
		});
	}

	public String[] getItems() {
		CheckBoxCellRenderer<E> renderer2 = (CheckBoxCellRenderer<E>)getRenderer();
		return(renderer2.getLabelText().split(", "));
	}

	protected void updateItem(int index) {
		if (isPopupVisible()) {
			E item = getItemAt(index);
			item.setSelected(!item.isSelected());
			setSelectedIndex(-1);
			setSelectedItem(item);
		}
	}

	@Override public void setPopupVisible(boolean v) {
		if (keepOpen) {
			keepOpen = false;
		} else {
			super.setPopupVisible(v);
		}
	}
}