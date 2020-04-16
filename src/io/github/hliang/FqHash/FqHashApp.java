package io.github.hliang.FqHash;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.cyanogenmod.updater.utils.MD5;

import uk.ac.babraham.FastQC.FileFilters.SequenceFileFilter;
import uk.ac.babraham.FastQC.Sequence.FastQFile;
import uk.ac.babraham.FastQC.Sequence.SequenceFormatException;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class FqHashApp extends JFrame {

	public static final String VERSION = "0.1.1";

	private JPanel contentPane;
	private JButton btnAdd;
	private JButton btnDelete;
	private JButton btnAnalyze;
	private JButton btnVerify;
	private JCheckBox cbCountSeq;
	private JScrollPane scrollPane;
	private JLabel currTaskLabel;
	private JTable table;
	private DefaultTableModel myTableModel;
	private FqHashWorker fqhashWorker;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					try {
						UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					} catch (Exception e) {
						System.out.println("Look and Feel not set");;
					}
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
				showFileOpen();
			}
		});

		// clear selected rows in table
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteSelectedRows(table);
			}
		});

		// analyze the files
		btnAnalyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ("Analyze".equalsIgnoreCase(btnAnalyze.getText())) {
//					getFileInfo(myTableModel.getDataVector());
					fqhashWorker = new FqHashWorker();
					fqhashWorker.addPropertyChangeListener(new WorkerStateListener());
					fqhashWorker.execute();
					
				} else if ("Stop".equalsIgnoreCase(btnAnalyze.getText())) {
					// cancel background task
					fqhashWorker.cancel(true);
//					fqhashWorker = null;
					currTaskLabel.setText("Stopped");
				}
			}
		});

		// verify the checksums match
		btnVerify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				verifyMD5(myTableModel.getDataVector());
			}
		});
		
		// clear selection
		contentPane.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		        if(!table.contains(e.getPoint())) { // contains(Point point) method is inherited from java.awt.Component
		            table.clearSelection();
		        }
		    }
		});
		
		// change button status (enabled/disabled) based on table Data Changes
		table.getModel().addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				if (((DefaultTableModel) e.getSource()).getRowCount() > 0 ) {
					if (fqhashWorker == null || fqhashWorker.getState() == SwingWorker.StateValue.DONE) {
						btnAdd.setEnabled(true);
						btnDelete.setEnabled(true);
						btnAnalyze.setEnabled(true);
						btnVerify.setEnabled(true);
					} else {
						btnAdd.setEnabled(false);
						btnDelete.setEnabled(false);
						btnAnalyze.setEnabled(true);
						btnVerify.setEnabled(true);
					}
				} else {
					btnAdd.setEnabled(true);
					btnDelete.setEnabled(false);
					btnAnalyze.setEnabled(false);
					btnVerify.setEnabled(false);
				}
			}
			
		});
		
	}
	
	
	private class WorkerStateListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getNewValue() == SwingWorker.StateValue.STARTED) {
            	btnAdd.setEnabled(false);
				btnDelete.setEnabled(false);
				btnAnalyze.setText("Stop");
				btnAnalyze.setIcon(new ImageIcon(this.getClass().getResource("/io/github/hliang/FqHash/Resources/stop-32.png")));
				cbCountSeq.setEnabled(false);
            } else if (e.getNewValue() == SwingWorker.StateValue.DONE) {
            	cbCountSeq.setEnabled(true);
				btnAnalyze.setText("Analyze");
				btnAnalyze.setIcon(new ImageIcon(this.getClass().getResource("/io/github/hliang/FqHash/Resources/play-32.png")));
				btnDelete.setEnabled(true);
				btnAdd.setEnabled(true);
				currTaskLabel.setText("Done");
				btnVerify.setEnabled(true);
            }
        }
    }


	//////////////////////////////////////////////////
	// initializing components
	//////////////////////////////////////////////////
	private void initComponens() {
		setTitle("FqHash");
		setSize(1000, 600);
		setMinimumSize(new Dimension(600, 300));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// set the icon displayed when the window is minimized
		setIconImage(new ImageIcon(this.getClass().getResource("/io/github/hliang/FqHash/Resources/sandcastle-256.png")).getImage());

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(2, 2));
		setContentPane(contentPane);

		JPanel controlPanel = createControlPanel();
		JPanel resultPanel = createResultPanel();
		JStatusBar statusBar = createStatusBar();

		contentPane.add(controlPanel, BorderLayout.NORTH);
		contentPane.add(resultPanel, BorderLayout.CENTER);
		contentPane.add(statusBar, BorderLayout.SOUTH);

	}


	// control panel
	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel(new GridLayout(0,7));
		controlPanel.setBorder(BorderFactory.createLineBorder(Color.gray));

		btnAdd = new JButton("Add File/Folder");
		btnDelete = new JButton("Delete");
		btnAnalyze = new JButton("Analyze");
		btnVerify = new JButton("Verify");

		cbCountSeq = new JCheckBox("Count Sequences");
		cbCountSeq.setSelected(false);
		
		// set icon
		btnAdd.setIcon(new ImageIcon(this.getClass().getResource("/io/github/hliang/FqHash/Resources/folder-32.png")));
		btnDelete.setIcon(new ImageIcon(this.getClass().getResource("/io/github/hliang/FqHash/Resources/row_delete-32.png")));
		btnAnalyze.setIcon(new ImageIcon(this.getClass().getResource("/io/github/hliang/FqHash/Resources/play-32.png")));
		btnVerify.setIcon(new ImageIcon(this.getClass().getResource("/io/github/hliang/FqHash/Resources/verify-32.png")));
		
		// set tool tips
		btnAdd.setToolTipText("Select files or folders (all sequence files inside will be added)");
		btnDelete.setToolTipText("Delete selected rows");
		btnAnalyze.setToolTipText("Count sequences and calculate MD5 hash");
		btnVerify.setToolTipText("Verify MD5 Checksum");

		// disable buttons when they are created the first time
		btnAnalyze.setEnabled(false);
		btnVerify.setEnabled(false);
		btnDelete.setEnabled(false);

		// add button to control panel
		controlPanel.add(new JLabel()); // just a place holder
		controlPanel.add(btnAdd);
		controlPanel.add(btnDelete);
		controlPanel.add(cbCountSeq);
		controlPanel.add(btnAnalyze);
		controlPanel.add(btnVerify);
		controlPanel.add(new JLabel()); // just a place holder

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
				"Enter original MD5 value to verify", "If checked, MD5 checksums match" };
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
			public Class<?> getColumnClass(int columnIndex) {
				return columnClass[columnIndex];
			}
		};

		table = new JTable(myTableModel);

		table.setFillsViewportHeight(true);
		table.setCellSelectionEnabled(true);
		table.setOpaque(false);
		// table.setBackground(new Color(255, 255, 230));
		table.setGridColor(Color.lightGray);
		// set font
		String OS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		if (OS.contains("win")) {
			table.setFont(new Font("Consolas", Font.PLAIN, 12));
		} else if (OS.contains("mac")) {
			table.setFont(new Font("Monospaced", Font.PLAIN, 12));
		} else if (OS.contains("nux")) {
			table.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 12));
		}

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

		table.setIntercellSpacing(new Dimension (10, 1));
		scrollPane = new JScrollPane(table);

		resultPanel.add(scrollPane);

		return resultPanel;
	}


	// status bar
	private JStatusBar createStatusBar() {
		JStatusBar statusBar = new JStatusBar();
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		
		currTaskLabel = new JLabel("Select files to start");
		statusBar.setLeftComponent(currTaskLabel);
		
		final JLabel versionLabel = new JLabel("v" + VERSION);
		versionLabel.setHorizontalAlignment(JLabel.CENTER);
		statusBar.addRightComponent(versionLabel);
		
		return statusBar;
	}


	// select files to add into the table
	protected void showFileOpen() {
		// file chooser
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setMultiSelectionEnabled(true);
		// custom file filter for all types of sequence files
		SequenceFileFilter sff = new SequenceFileFilter();
		fileChooser.setFileFilter(sff);
		// pops up an "Open File" file chooser dialog
		int result = fileChooser.showOpenDialog(this);
		// if any file is chosen
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			for (File file : files) {
				if (file.isDirectory()) {  // traverse files in folder
					String[] seqFileSuffixes = {".txt", ".fastq", ".fq",
							".txt.gz", ".fastq.gz", ".fq.gz",
							".txt.bz", ".fastq.bz", ".fq.bz",
							".txt.bz2", ".fastq.bz2", ".fq.bz2",
							".bam", ".sam", ".compact-reads", ".goby"};
					List<File> filesInDir = findFiles(file, seqFileSuffixes);
					filesInDir.forEach(aFile -> {
						Object[] newrow = { aFile, aFile.length(), null, null, null, null };
						myTableModel.addRow(newrow);
					});
				} else {  // regular file, just add it to table
					Object[] newrow = { file, file.length(), null, null, null, null };
					myTableModel.addRow(newrow);
				}
			}
		} else if (result == JFileChooser.CANCEL_OPTION) {
			return;
		}
	}
	

	/**
	 * Traverse a directory and search for all files matching file name extensions/suffixes.
	 * @param dir the {@code File} folder to search
	 * @param suffixes the accepted file name extensions/suffixes. If suffixes is null or empty, any file format is accepted
	 * @return a List of File
	 */
	public static List<File> findFiles(File dir, String[] suffixes) {
		ArrayList<File> result = new ArrayList<>();
		File[] files = dir.listFiles(f -> f.isDirectory() || suffixes == null || suffixes.length == 0
				|| Arrays.stream(suffixes).filter(suf -> f.getName().toLowerCase().endsWith(suf)).count() > 0);
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					result.addAll(findFiles(file, suffixes));
				} else {
					result.add(file);
				}
			}
		}
		return result;
	}


	// calculate MD5 hash and count sequences
	protected void getFileInfo(Vector<?> tableDataVector) {
		int colMD5Calculated = myTableModel.findColumn("MD5");
		int colSeqCount = myTableModel.findColumn("Total Seq");
		for (int row = 0; row < tableDataVector.size(); row++) {
			File file = (File) ((Vector<?>) tableDataVector.get(row)).get(0);
			System.out.println(new Date() + " start processing " + file);
			
			// calculate MD5 hash
			if (myTableModel.getValueAt(row, colMD5Calculated) == null) {  // md5 is not calculated yet
				new MD5CalWorker(file, myTableModel, row, colMD5Calculated).execute();
				
			}
			
			// count number of sequences
			if (cbCountSeq.isSelected()
					&& (myTableModel.getValueAt(row, colSeqCount) == null || myTableModel.getValueAt(row, colSeqCount) == "ERROR"))
			{
				if (cbCountSeq.isSelected()) {
					new SeqCountWorker(file, myTableModel, row, colSeqCount).execute();
				}
					
			}
			
		}

	}
	
	
	// custom class CellData used for for SwingWorker.publish()
	private class CellData {
		File file;
		int row;
		int col;
		Object val;

		public CellData(File file, int row, int col, Object val) {
			this.file = file;
			this.row = row;
			this.col = col;
			this.val = val;
		}
	}
	
	
	// single SwingWorker to do the analysis for all all input files -- so only one thread is used
	private class FqHashWorker extends SwingWorker<Void, CellData> {
		int colFile = myTableModel.findColumn("File Name");
		int colMD5Calculated = myTableModel.findColumn("MD5");
		int colSeqCount = myTableModel.findColumn("Total Seq");

		@Override
		protected Void doInBackground() throws Exception {
			String md5sum = null;
			Integer seqCount = null;
				for (int row = 0; row < myTableModel.getRowCount(); row++) {
					if (isCancelled()) {
						return null;
					}
					
					File file = (File) myTableModel.getValueAt(row, colFile);
					
					CellData newCellData = new CellData(file, -1, -1, null);
					publish(newCellData);  // update status bar to show file being processed
					
					// calculate MD5 hash
					if (myTableModel.getValueAt(row, colMD5Calculated) == null) {  // md5 is not calculated yet
						//Thread.sleep(new Random().nextInt(5000));  // for test
						md5sum = MD5.calculateMD5(file);  // TODO: modify MD5.calculateMD5(), so the calculation can be canceled/interrupted
						newCellData = new CellData(file, row, colMD5Calculated, md5sum);
						
						if (isCancelled()) {
							return null;
						}
						publish(newCellData);
						Thread.yield();
					}

					// count sequences
					if (cbCountSeq.isSelected()
							&& (myTableModel.getValueAt(row, colSeqCount) == null || myTableModel.getValueAt(row, colSeqCount) == "ERROR"))
					{
						try {
							//Thread.sleep(new Random().nextInt(5000));  // for test
							FastQFile seqFile = new FastQFile(file);
							seqCount = 0;
							while (!isCancelled() && seqFile.hasNext()) {
								++seqCount;
								seqFile.next();
							}
						} catch (SequenceFormatException | IOException e) {
							seqCount = -1;
						}
						newCellData = new CellData(file, row, colSeqCount, seqCount);
						
						if (isCancelled()) {
							return null;
						}
						publish(newCellData);
						Thread.yield();
					}

				}
				
			return null;
		}
		
		@Override
		protected void process(List<CellData> chunks) {
			for (CellData newCellData : chunks) {
				if (newCellData.val == null) {  // update status bar to show file being processed
					currTaskLabel.setText("Processing ... " + newCellData.file);
					continue;
				}
				// update MD5
				if (newCellData.col == colMD5Calculated) {
					myTableModel.setValueAt(newCellData.val, newCellData.row, newCellData.col);
				}
				// update number of sequences
				if (newCellData.col == colSeqCount) {
					if ( ((int) newCellData.val) < 0) {
						myTableModel.setValueAt("ERROR", newCellData.row, newCellData.col);
					} else {
						myTableModel.setValueAt(newCellData.val, newCellData.row, newCellData.col);
					}
				}

	         }
		}

		@Override
		protected void done() {
			cbCountSeq.setEnabled(true);
			btnAnalyze.setText("Analyze");
			btnAnalyze.setIcon(new ImageIcon(this.getClass().getResource("/io/github/hliang/FqHash/Resources/play-32.png")));
			btnDelete.setEnabled(true);
			btnDelete.setText("Delele Rows");
			btnAdd.setEnabled(true);
			// don't set currTaskLabel in done(), because done() can be triggered by normal completion or cancellation
			// currTaskLabel.setText("Done");
		}
	}

	// SwingWorker for calculating MD5 hash
	private class MD5CalWorker extends SwingWorker<Void, Void> {
		private File file;
		private int row;
		private int col;
		
		public MD5CalWorker(File file, DefaultTableModel tableModel, int row, int col) {
			this.file = file;
			this.row = row;
			this.col = col;
			
		}

		@Override
		protected Void doInBackground() throws Exception {
			// calculate MD5 hash
			String md5sum = MD5.calculateMD5(file);
			myTableModel.setValueAt(md5sum, row, col);
			return null;
		}
		
	}
	
	// SwingWorker for counting sequences
	private class SeqCountWorker extends SwingWorker<Void, Void> {
		private File file;
		private int row;
		private int col;
		
		public SeqCountWorker(File file, DefaultTableModel tableModel, int row, int col) {
			this.file = file;
			this.row = row;
			this.col = col;
			
		}

		@Override
		protected Void doInBackground() throws Exception {
			FastQFile seqFile;
			try {
				seqFile = new FastQFile(file);
				int seqCount = 0;
				while (seqFile.hasNext()) {
					++seqCount;
					seqFile.next();
				}
				myTableModel.setValueAt(seqCount, row, col);
			} catch (SequenceFormatException | IOException e) {
				myTableModel.setValueAt("ERROR", row, col);
			}
			return null;
		}
		
	}


	// compare MD5 checksums, and update the column "Match"
	protected void verifyMD5(Vector<?> tableDataVector) {
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


	/**
	 * get all selected rows and DELETE one by one.
	 *
	 * @param jtable the table to DELETE selected rows
	 */
	public static void deleteSelectedRows(JTable jtable) {
	    int[] selectedrows = jtable.getSelectedRows();  // all selected rows
	    for (int row : selectedrows) {
	        row = jtable.getSelectedRow();  // after deleting a row, the index of the next selected row might change
	        if (jtable.getRowSorter() != null) {
	            row = jtable.getRowSorter().convertRowIndexToModel(row);
	        }
	        ((DefaultTableModel) jtable.getModel()).removeRow(row);
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
