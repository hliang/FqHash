package io.github.hliang.FqHash;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.cyanogenmod.updater.utils.MD5;

import uk.ac.babraham.FastQC.Sequence.FastQFile;
import uk.ac.babraham.FastQC.Sequence.SequenceFormatException;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class FqHashApp extends JFrame {

	private JPanel contentPane;
	private JButton btnAdd;
	private JButton btnClear;
	private JButton btnAnalyze;
	private JButton btnVerify;
	private JCheckBox cbCountSeq;
	private JScrollPane scrollPane;
	private JTable table;
	private DefaultTableModel myTableModel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FqHashApp frame = new FqHashApp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FqHashApp() {
		initComponens();
		createEvents();
	}


	//////////////////////////////////////////////////
	// creating events
	//////////////////////////////////////////////////
	private void createEvents() {
		// show "Open File" dialog
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showFileOpen(contentPane);
				if (myTableModel.getDataVector() != null) {
					btnAnalyze.setEnabled(true);
					btnVerify.setEnabled(true);
				} else {
					btnAnalyze.setEnabled(false);
					btnVerify.setEnabled(false);
				}
			}
		});

		// clear all data in table
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myTableModel.setRowCount(0);
				btnAnalyze.setEnabled(false);
				btnVerify.setEnabled(false);
			}
		});

		// analyze the files
		btnAnalyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getFileInfo(myTableModel.getDataVector());
			}
		});

		// verify the checksums match
		btnVerify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				verifyMD5(myTableModel.getDataVector());
			}
		});
	}


	//////////////////////////////////////////////////
	// initializing components
	//////////////////////////////////////////////////
	private void initComponens() {
		setTitle("FqHash");
		setSize(1000, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(2, 2));
		setContentPane(contentPane);

		JPanel controlPanel = createControlPanel();
		JPanel resultPanel = createResultPanel();

		contentPane.add(controlPanel, BorderLayout.NORTH);
		contentPane.add(resultPanel, BorderLayout.CENTER);

	}


	// control panel
	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(BorderFactory.createLineBorder(Color.gray));

		btnAdd = new JButton("Add Files");
		btnClear = new JButton("Clear");
		btnAnalyze = new JButton("Analyze");
		btnVerify = new JButton("Verify");

		cbCountSeq = new JCheckBox("Count Sequences");
		cbCountSeq.setSelected(true);

		// set tool tips
		btnAnalyze.setToolTipText("Count sequences and calculate MD5 hash");
		btnVerify.setToolTipText("Verify MD5 Checksum");

		// disable buttons when they are created the first time
		btnAnalyze.setEnabled(false);
		btnVerify.setEnabled(false);

		// add button to control panel
		controlPanel.add(btnAdd);
		controlPanel.add(btnClear);
		controlPanel.add(cbCountSeq);
		controlPanel.add(btnAnalyze);
		controlPanel.add(btnVerify);

		return controlPanel;
	}


	// result panel
	private JPanel createResultPanel() {
		// using GridLayout(1,0) as layout is important, so that the table inside can
		// auto-resize to fill the panel
		JPanel resultPanel = new JPanel(new GridLayout(1, 0));
		resultPanel.setPreferredSize(new Dimension(800, 600));

		Class[] columnClass = new Class[] { File.class, Long.class, Integer.class, String.class, String.class,
				Boolean.class };
		String[] columnNames = { "File Name", "File Size", "Total Seq", "MD5", "Verify Checksum", "Match" };
		String[] columnToolTips = { null, null, null, "Current file MD5 checksum value",
				"Enter original md5 value to verify", "If checked, MD5 checksums match" };
		myTableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				if (columnNames[column] == "Verify Checksum") { // editable column
					return true;
				} else {
					return false;
				}
			}

			@Override
			public Class getColumnClass(int columnIndex) {
				return columnClass[columnIndex];
			}
		};

		table = new JTable(myTableModel);

		table.setFillsViewportHeight(true);
		table.setCellSelectionEnabled(true);
		table.setOpaque(false);
		table.setBackground(new Color(255, 255, 230));
		table.setGridColor(Color.lightGray);
		table.setFont(new Font("Monospaced", Font.PLAIN, 12));

		// tool tips when mouse hovers over table header
		ToolTipHeader tooltipHeader = new ToolTipHeader(table.getColumnModel());
		tooltipHeader.setToolTipStrings(columnToolTips);
		table.setTableHeader(tooltipHeader);
		// header font
		table.getTableHeader().setFont(new Font(null, Font.PLAIN, 14));

		// custom renderer
		table.setDefaultRenderer(File.class, new DefaultTableCellRenderer() {
			@Override
			protected void setValue(Object value) {
				// display file name instead of full path
				super.setText((value == null && value instanceof File) ? "" : ((File) value).getName());
			}
		});

		// conditional styling for user-provided-MD5 column
		table.setDefaultRenderer(String.class, new MD5CellHighlighterRenderer());

		// set column widths and custom render (show decimal symbol separator)
		TableColumn column = null;
		for (int i = 0; i < columnNames.length; i++) {
			column = table.getColumnModel().getColumn(i);
			if (columnNames[i] == "File Name" || columnNames[i] == "MD5" || columnNames[i] == "Verify Checksum") {
				column.setPreferredWidth(250); // third column is bigger
			} else if (columnNames[i] == "File Size" || columnNames[i] == "Total Seq") {
				table.getColumnModel().getColumn(i).setCellRenderer(new NumberTableCellRenderer());
				column.setPreferredWidth(110);
			} else {
				column.setPreferredWidth(70);
			}
		}

		scrollPane = new JScrollPane(table);

		resultPanel.add(scrollPane);

		return resultPanel;
	}


	// select files to add into the table
	protected void showFileOpen(Component parent) {
		// file chooser
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		// TODO custom file filter for all types of sequence files
		FileFilter ffilterGZ = new FileNameExtensionFilter("gzip file", "gz");
		FileFilter ffilterSH = new FileNameExtensionFilter("sh file", "sh");
		FileFilter ffilterBAM = new FileNameExtensionFilter("bam", "bam");
		fileChooser.addChoosableFileFilter(ffilterGZ);
		fileChooser.addChoosableFileFilter(ffilterSH);
		fileChooser.addChoosableFileFilter(ffilterBAM);
		fileChooser.setAcceptAllFileFilterUsed(true); // false: hide the "[All files]" option
		// 接受结果
		int result = fileChooser.showOpenDialog(parent);
		// 是否选择了文件
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			for (File file : files) {
				Object[] newrow = { file, file.length(), null, null, null, null };
				myTableModel.addRow(newrow);
			}
		}
	}


	// calculate MD5 hash and count sequences
	protected void getFileInfo(Vector tableDataVector) {
		int colMD5Calculated = myTableModel.findColumn("MD5");
		int colMD5UserProvided = myTableModel.findColumn("Total Seq");
		for (int row = 0; row < tableDataVector.size(); row++) {
			File file = (File) ((Vector) tableDataVector.get(row)).get(0);
			System.out.println(new Date() + " processing " + file);
			if (myTableModel.getValueAt(row, colMD5Calculated) == null // md5 is not calculated yet
					|| myTableModel.getValueAt(row, colMD5UserProvided) == null // sequences not counted yet
					|| myTableModel.getValueAt(row, colMD5UserProvided) == "ERROR") // there was an error counting sequences
			{
				// calculate MD5 hash
				String md5sum = MD5.calculateMD5(file);
				myTableModel.setValueAt(md5sum, row, colMD5Calculated);
				
				// count number of sequences
				if (cbCountSeq.isSelected()) {
					FastQFile seqFile;
					try {
						seqFile = new FastQFile(file);
						int seqCount = 0;
						while (seqFile.hasNext()) {
							++seqCount;
							seqFile.next();
						}
						myTableModel.setValueAt(seqCount, row, colMD5UserProvided);
					} catch (SequenceFormatException | IOException e) {
						myTableModel.setValueAt("ERROR", row, colMD5UserProvided);
					}
				}

			}
			
		}

	}


	// compare MD5 checksums, and update the column "Match"
	protected void verifyMD5(Vector tableDataVector) {
		int colMD5Calculated = myTableModel.findColumn("MD5");
		int colMD5UserProvided = myTableModel.findColumn("Verify Checksum");
		int colMD5Matched = myTableModel.findColumn("Match");
		for (int row = 0; row < tableDataVector.size(); row++) {
			if (myTableModel.getValueAt(row, colMD5Calculated) != null
					&& myTableModel.getValueAt(row, colMD5UserProvided) != null) {
				String MD5Calculated = (String) myTableModel.getValueAt(row, colMD5Calculated);
				String MD5UserProvided = (String) myTableModel.getValueAt(row, colMD5UserProvided);
				myTableModel.setValueAt(MD5Calculated.contentEquals(MD5UserProvided), row, colMD5Matched);
			}
		}

	}


	// use comma (,) to display numbers with thousands separator
	protected class NumberTableCellRenderer extends DefaultTableCellRenderer {

		public NumberTableCellRenderer() {
			setHorizontalAlignment(JLabel.RIGHT);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (value instanceof Number) {
				value = NumberFormat.getNumberInstance().format(value);
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

	}


	// renderer to set background color for user provided checksum, highlight matched/mismatched MD5 checksums
	protected class MD5CellHighlighterRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Object md5a = table.getModel().getValueAt(row, 3);
			Object md5b = table.getModel().getValueAt(row, 4);

			Component cellComp = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
			if (!isSelected) {
				if (column == 4 && md5a != null && md5b != null && md5a.equals(md5b)) {
					// cellComp.setForeground(Color.black);
					cellComp.setBackground(new Color(198, 240, 205));
				} else if (column == 4 && md5a != null && md5b != null && !md5a.equals(md5b)) {
					// cellComp.setForeground(Color.black);
					cellComp.setBackground(new Color(255, 200, 207));
				} else {
					// cellComp.setForeground(Color.black);
					cellComp.setBackground(null);
				}
			}

			return cellComp;
		}
	}


	// implementation code to set a tooltip text to each column of JTableHeader
	// adopted from https://www.tutorialspoint.com/how-to-set-a-tooltip-to-each-column-of-a-jtableheader-in-java
	protected class ToolTipHeader extends JTableHeader {
		String[] toolTips;

		public ToolTipHeader(TableColumnModel model) {
			super(model);
		}

		public String getToolTipText(MouseEvent e) {
			int col = columnAtPoint(e.getPoint());
			int modelCol = getTable().convertColumnIndexToModel(col);
			String retStr;
			try {
				retStr = toolTips[modelCol];
			} catch (NullPointerException ex) {
				retStr = "";
			} catch (ArrayIndexOutOfBoundsException ex) {
				retStr = "";
			}
			if (retStr == null || retStr.length() < 1) {
				retStr = super.getToolTipText(e);
			}
			return retStr;
		}

		public void setToolTipStrings(String[] toolTips) {
			this.toolTips = toolTips;
		}
	}

}
