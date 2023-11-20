package synapticloop.puzzlr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

import synapticloop.puzzlr.gui.CheckableItem;
import synapticloop.puzzlr.gui.CheckedComboBox;

public class PuzzlrGui extends JFrame implements ActionListener, KeyListener {
	private static final long serialVersionUID = -6566272221454066796L;

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private static final String TEXT_FIELD_DATE = "date";
	private static final String TEXT_FIELD_START = "startDate";
	private static final String TEXT_FIELD_END = "endStart";

	private GridBagConstraints gridBagConstraints = new GridBagConstraints();
	private GridBagLayout gridBagLayout = new GridBagLayout();

	private Map<String, JTextField> textFieldMap = new HashMap<>();
	private Vector<JCheckBox> checkboxes = new Vector<>();

	private CheckedComboBox<CheckableItem> checkedComboBox;

	private int x = 0;
	private int y = 0;

	private JLabel titleLabel = null;

	public PuzzlrGui() throws IOException, UnsupportedLookAndFeelException {
		setLayout(gridBagLayout);
		setTitle("Puzzlr");
		setSize(800, 800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		UIManager.setLookAndFeel( new MetalLookAndFeel() );

		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);

		PuzzlrMain.parsePuzzlrJSON();

		titleLabel  = new JLabel("Puzzlr good to go...", JLabel.CENTER);

		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets.right = 12;
		gridBagConstraints.gridx = x;
		gridBagConstraints.gridy = y;
		gridBagConstraints.gridwidth = 2;
		this.add(titleLabel , gridBagConstraints);

		// reset
		gridBagConstraints.gridwidth = 1;

		newRow();
		generateLabel("Date (yyyy-MM-dd)", x, y);
		newColumn();
		generateTextBox(TEXT_FIELD_DATE, x, y);

		newRow();
		generateSeparator(x, y);

		newRow();
		generateLabel("Start (yyyy-MM-dd)", x, y);
		newColumn();
		generateTextBox(TEXT_FIELD_START, x, y);

		newRow();
		generateLabel("End (yyyy-MM-dd)", x, y);
		newColumn();
		generateTextBox(TEXT_FIELD_END, x, y);

		newRow();
		generateSeparator(x, y);
		// now to generate the checkboxes...

		Iterator<String> iterator = PuzzlrMain.SLUG_MAP.keySet().iterator();

		while (iterator.hasNext()) {
			newRow();
			String value = (String) iterator.next();
			generateCheckBox(value, PuzzlrMain.SLUG_MAP.get(value), x, y);
		}

		newRow();
		generateSeparator(x, y);

		newRow();
		generateButton("Generate", x, y);
		x = 0;


	}

	private void newRow() { x = 0; y++; }
	private void newColumn() { x++; }

	private void generateSeparator(int gridX, int gridY) {
		JSeparator jSeparator = new JSeparator(SwingConstants.HORIZONTAL);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = gridX;
		gridBagConstraints.gridy = gridY;
		gridBagConstraints.insets = new Insets(12, 2, 12, 2);
		gridBagConstraints.gridwidth = 2;
		this.add(jSeparator, gridBagConstraints);

		// reset
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);
	}

	private void generateCheckBox(String value, String label, int gridX, int gridY) {
		JCheckBox jCheckBox = new JCheckBox(label);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = gridX;
		gridBagConstraints.gridy = gridY;
		gridBagConstraints.gridwidth = 2;
		jCheckBox.setName(value);
		checkboxes.add(jCheckBox);
		this.add(jCheckBox, gridBagConstraints);
	}

	private void generateButton(String text, int gridX, int gridY) {
		JButton jButton = new JButton(text);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = gridX;
		gridBagConstraints.gridy = gridY;
		gridBagConstraints.gridwidth = 2;
		jButton.addActionListener(this);
		this.add(jButton, gridBagConstraints);
	}

	private void generateLabel(String text, int gridX, int gridY, int gridwidth) {
		JLabel jLabel = new JLabel(text, JLabel.RIGHT);

		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets.right = 12;
		gridBagConstraints.gridx = gridX;
		gridBagConstraints.gridy = gridY;
		gridBagConstraints.gridwidth = gridwidth;
		this.add(jLabel, gridBagConstraints);

		// reset
		gridBagConstraints.gridwidth = 1;
	}

	private void generateLabel(String text, int gridX, int gridY) {
		generateLabel(text, gridX, gridY, 1);
	}

	private void generateTextBox(String name, int gridX, int gridY) {
		JTextField jTextField = new JTextField(10);
		jTextField.setName(name);
		jTextField.addKeyListener(this);
		textFieldMap.put(name, jTextField);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = gridX;
		gridBagConstraints.gridy = gridY;
		jTextField.setBorder(BorderFactory.createLineBorder(Color.black));
		this.add(jTextField, gridBagConstraints);
	}

	private void generateDropdown(String text, int gridX, int gridY) {

		int size = PuzzlrMain.SLUG_MAP.size();
		CheckableItem[] m = new CheckableItem[size];
		Iterator<String> iterator = PuzzlrMain.SLUG_MAP.keySet().iterator();
		int i = 0;
		while (iterator.hasNext()) {
			String string = (String) iterator.next();
			m[i] = new CheckableItem(string + "/" + PuzzlrMain.SLUG_MAP.get(string), false);
			i++;
		}

		checkedComboBox = new CheckedComboBox<>(new DefaultComboBoxModel<>(m));

		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = gridX;
		gridBagConstraints.gridy = gridY;

		this.add(checkedComboBox, gridBagConstraints);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JTextField dateTextField = textFieldMap.get(TEXT_FIELD_DATE);
		JTextField startTextField = textFieldMap.get(TEXT_FIELD_START);
		JTextField endTextField = textFieldMap.get(TEXT_FIELD_END);

		boolean hasErrors = false;
		if(!checkCorrectDate(dateTextField)) {
			hasErrors = true;
		}
		PuzzlrMain.optionDate = dateTextField.getText();

		if(!checkCorrectDate(startTextField)) {
			hasErrors = true;
		}
		PuzzlrMain.optionRangeStart = startTextField.getText();

		if(!checkCorrectDate(endTextField)) {
			hasErrors = true;
		}
		PuzzlrMain.optionRangeEnd = endTextField.getText();

		PuzzlrMain.puzzles.clear();
		PuzzlrMain.GENERATED_FILES.clear();
		PuzzlrMain.WANTED_SLUGS.clear();

		// go through the checkboxes
		for (JCheckBox checkbox : checkboxes) {
			if(checkbox.isSelected()) {
				PuzzlrMain.WANTED_SLUGS.add(checkbox.getName());
			}
		}

		if(hasErrors) {
			titleLabel.setForeground(Color.red);
		} else {
			titleLabel.setForeground(Color.black);
			try {
				PuzzlrMain.generatePDFs();
			} catch (ParseException | IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	public boolean checkCorrectDate(JTextField jTextField) {
		String text = jTextField.getText();
		if(jTextField.getText().trim().length() != 0) {
			try {
				SIMPLE_DATE_FORMAT.parse(text);
			} catch (ParseException e) {
				jTextField.setBorder(BorderFactory.createLineBorder(Color.red));
				return(false);
			}
		}
		jTextField.setBorder(BorderFactory.createLineBorder(Color.black));
		return(true);
	}

	public static void main(String[] args) throws IOException, UnsupportedLookAndFeelException {
		PuzzlrGui puzzlrGui = new PuzzlrGui();
		puzzlrGui.pack();
		Dimension size = puzzlrGui.getSize();
		puzzlrGui.setMinimumSize(new Dimension(size.width + 40, size.height + 40));
		puzzlrGui.setVisible(true);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		Component component = e.getComponent();
		if(null != component && component instanceof JTextField) {
			if(
					((JTextField)component).getText().length() == 10 
					&&
					!(e.getKeyChar()==KeyEvent.VK_DELETE||e.getKeyChar()==KeyEvent.VK_BACK_SPACE)
					) {
				e.consume();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// DO NOTHING
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// DO NOTHING
	}
}
