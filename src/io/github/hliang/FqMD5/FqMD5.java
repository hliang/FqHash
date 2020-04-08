package io.github.hliang.FqMD5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.cyanogenmod.updater.utils.MD5;

import uk.ac.babraham.FastQC.Sequence.FastQFile;
import uk.ac.babraham.FastQC.Sequence.SequenceFormatException;


class FqMD5 extends JFrame {
	public static final String VERSION = "0.1";
	private JPanel appPanel;

	final Class[] columnClass = new Class[] { File.class, Long.class, Integer.class, String.class, String.class, Boolean.class };

	private String[] columnNames = { "文件名", "文件大小", "序列数量", "MD5", "校验", "匹配" };
	DefaultTableModel myTableModel = new DefaultTableModel(columnNames, 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			if (columnNames[column] == "校验") {
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

	public static void main(String[] args) {
		System.out.println(new Date() + " main");
		FqMD5 appFrame = new FqMD5();
		System.out.println(new Date() + " appFrame creation done");
		appFrame.setVisible(true);
		appFrame.setResizable(true);
	}

	public FqMD5() {
		setSize(960, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		appPanel = new JPanel();
		appPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		appPanel.setLayout(new BorderLayout(2, 2));
		setContentPane(appPanel);

		JPanel controlPanel = createControlPanel();
		JPanel resultPanel = createResultPanel();
		JStatusBar statusBar = createStatusBar();

		appPanel.add(controlPanel, BorderLayout.NORTH);
		appPanel.add(resultPanel, BorderLayout.CENTER);
		appPanel.add(statusBar, BorderLayout.SOUTH);

	}

	private JStatusBar createStatusBar() {
		JStatusBar statusBar = new JStatusBar();
		statusBar.setBorder(BorderFactory.createEtchedBorder());

		JLabel leftLabel = new JLabel("Ready. Select files to start.");
		statusBar.setLeftComponent(leftLabel);

		final JLabel dateLabel = new JLabel("Idle");
		dateLabel.setHorizontalAlignment(JLabel.CENTER);
		statusBar.addRightComponent(dateLabel);

		final JLabel timeLabel = new JLabel("FqMD5 v" + VERSION);
		timeLabel.setHorizontalAlignment(JLabel.CENTER);
		statusBar.addRightComponent(timeLabel);

		return statusBar;
	}

	// control panel
	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(BorderFactory.createLineBorder(Color.gray));

		JButton btnAdd = new JButton("Add Files");
		JButton btnClear = new JButton("Clear");
		JButton btnAnalyze = new JButton("Analyze");
		btnAnalyze.setEnabled(false);
		JButton btnVerify = new JButton("Verify");
		btnVerify.setEnabled(false);


		// 添加文件
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("INFO add");
				showFileOpen(appPanel);
				System.out.println("INFO add 222 datavector" + myTableModel.getDataVector());
				if (myTableModel.getDataVector() != null) {
					btnAnalyze.setEnabled(true);
					btnVerify.setEnabled(true);
				} else {
					btnAnalyze.setEnabled(false);
					btnVerify.setEnabled(false);
				}
				System.out.println("INFO add 333");
			}
		});

		// 清除
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("INFO clear");
				myTableModel.setRowCount(0);
			}
		});

		// 处理文件
		btnAnalyze.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("check");
				getFileInfo(myTableModel.getDataVector());

			}
		});

		// 检查MD5是否匹配
		btnVerify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				verifyMD5(myTableModel.getDataVector());
			}
		});

		controlPanel.add(btnAdd);
		controlPanel.add(btnClear);
		controlPanel.add(btnAnalyze);
		controlPanel.add(btnVerify);
		return controlPanel;
	}

	// result panel
	private JPanel createResultPanel() {
		System.out.println(new Date() + " createResultPanel 1111");
		// using GridLayout(1,0) as layout is important, so that the table inside can
		// auto-resize to fill the panel
		JPanel resultPanel = new JPanel(new GridLayout(1, 0));
		resultPanel.setPreferredSize(new Dimension(800, 600));
		resultPanel.setBackground(Color.lightGray);

		JTable table = new JTable(myTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(500, 100));
		table.setFillsViewportHeight(false);

		JScrollPane scrollPane = new JScrollPane(table);

		table.setOpaque(false);
		table.setBackground(new Color(255, 255, 230));
		table.setGridColor(Color.lightGray);
		table.setFont(new Font("Monospaced", Font.PLAIN, 12));

		// custom renderer
		table.setDefaultRenderer(File.class, new DefaultTableCellRenderer() {
			@Override
			protected void setValue(Object value) {
				super.setText((value == null && value instanceof File) ? "" : ((File) value).getName());
			}
		});
		// conditional styling for user-provided-MD5 column
		table.setDefaultRenderer(String.class, new MD5CellHighlighterRenderer());

		// set column widths and custom render (show decimal symbol separator)
		TableColumn column = null;
		for (int i = 0; i < columnNames.length; i++) {
			column = table.getColumnModel().getColumn(i);
			if (columnNames[i] == "文件名" || columnNames[i] == "MD5" || columnNames[i] == "校验") {
				column.setPreferredWidth(250); // third column is bigger
			} else if (columnNames[i] == "文件大小" || columnNames[i] == "序列数量") {
				table.getColumnModel().getColumn(i).setCellRenderer(new NumberTableCellRenderer());
			} else {
				column.setPreferredWidth(100);
			}
		}

		resultPanel.add(scrollPane);

		return resultPanel;
	}

	// select files to add into the table
	public void showFileOpen(Component parent) {
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
				System.out.println(file);
				Object[] newrow = { file, file.length(), null, null, null, null};
				myTableModel.addRow(newrow);
				System.out.println(file.getName());
				System.out.println("data vector" + myTableModel.getDataVector());
			}
		}
	}

	public void getFileInfo(Vector tableDataVector) {
		int idx_md5 = myTableModel.findColumn("MD5");
		int idx_readcount = myTableModel.findColumn("序列数量");
		for (int i = 0; i < tableDataVector.size(); i++) {
			File file = (File) ((Vector) tableDataVector.get(i)).get(0);
			if (myTableModel.getValueAt(i, idx_md5) == null || myTableModel.getValueAt(i, idx_readcount) == null
					|| myTableModel.getValueAt(i, idx_readcount) == "ERROR") {
				// 计算Read数
				FastQFile seqFile;
				try {
					seqFile = new FastQFile(file);
					int seqCount = 0;
					while (seqFile.hasNext()) {
						++seqCount;
						seqFile.next();
					}
					myTableModel.setValueAt(seqCount, i, idx_readcount);
				} catch (SequenceFormatException | IOException e) {
					myTableModel.setValueAt("ERROR", i, idx_readcount);
				}

				// 计算MD5值
				String md5sum = MD5.calculateMD5(file);
				myTableModel.setValueAt(md5sum, i, idx_md5);
			}
		}
	}


	protected void verifyMD5(Vector tableDataVector) {
		System.out.println("verifyMD5 starting");
		int idxMD5Calculated = myTableModel.findColumn("MD5");
		int idxMD5UserProvided = myTableModel.findColumn("校验");
		int idxMD5Matched = myTableModel.findColumn("匹配");
		for (int i = 0; i < tableDataVector.size(); i++) {
			System.out.println(myTableModel.getValueAt(i, idxMD5Calculated));
			System.out.println(myTableModel.getValueAt(i, idxMD5UserProvided));
			if (myTableModel.getValueAt(i, idxMD5Calculated) != null && myTableModel.getValueAt(i, idxMD5UserProvided) != null) {
				String MD5Calculated = (String) myTableModel.getValueAt(i, idxMD5Calculated);
				String MD5UserProvided = (String) myTableModel.getValueAt(i, idxMD5UserProvided);
				myTableModel.setValueAt(MD5Calculated.contentEquals(MD5UserProvided), i, idxMD5Matched);
			}
		}
		System.out.println("verifyMD5 done");

	}

	public class NumberTableCellRenderer extends DefaultTableCellRenderer {

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

	public class MD5CellHighlighterRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object obj,
				boolean isSelected, boolean hasFocus, int row, int column) {

			Object md5a = table.getModel().getValueAt(row, 3);
			Object md5b = table.getModel().getValueAt(row, 4);

			Component cellComp = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
			if (!isSelected) {
			if (column == 4 && md5a != null && md5b != null && md5a.equals(md5b)) {
				// cellComp.setForeground(Color.black);
				cellComp.setBackground(new Color(191, 238, 144));
			} else if (column == 4 && md5a != null && md5b != null && ! md5a.equals(md5b)) {
				// cellComp.setForeground(Color.black);
				cellComp.setBackground(Color.pink);
			} else {
				// cellComp.setForeground(Color.black);
				cellComp.setBackground(null);
			}
			}

			return cellComp;
		}
	}


}
