package io.github.hliang.FqMD5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.cyanogenmod.updater.utils.MD5;

import uk.ac.babraham.FastQC.Sequence.FastQFile;
import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.SequenceFormatException;

//public class FqMD5 {
//	public static void main(String[] args) {
//		AppFrame app = new AppFrame();
//	}
//
//}

class FqMD5 extends JFrame {
	public static final String VERSION = "0.1";
	private JPanel appPanel;
	ArrayList<File> filesToProcess = new ArrayList<File>();
	private boolean countSeq = false;

	final Class[] columnClass = new Class[] { File.class, Long.class, Integer.class, String.class, Boolean.class };
	/*
	 * private Object[][] data; private Object[][] data = { { new
	 * File("/tmp/abc_R1.fq.gz"), "adkgjaldjfadf", new Long(1970804885L), new
	 * Integer(200000000), new Boolean(false) }, { new
	 * File("/tmp/123/abc_R1.fq.gz"), "15ae134setser", new Long(2986270852L), new
	 * Integer(3000000), new Boolean(true) }, { new File("abc_R1.fq.gz"),
	 * "a19aygjkaeoq3", new Long(476017200L), new Integer(400000000), new
	 * Boolean(false) }, { new File("abc_R1.fq.gz"), "al83hjas9gakj", new
	 * Long(8117923011L), new Integer(500000000), new Boolean(true) }, { new
	 * File("abc_R1.fq.gz"), "alXXXXas9gakj", new Long(8117923011L), new
	 * Integer(500000000), new Boolean(true) }, { new File("abc_R1.fq.gz"),
	 * "adllllldjfadf", new Long(1970804885L), new Integer(200000000), new
	 * Boolean(false) }, { new File("abc_R1.fq.gz"), "15ae134setser", new
	 * Long(2986270852L), new Integer(300000000), new Boolean(true) }, { new
	 * File("abc_R1.fq.gz"), "a19aygjkaeoq3", new Long(4766017200L), new
	 * Integer(400000000), new Boolean(false) }, { new File("/dir/xyz/"),
	 * "al83hjas9gakj", new Long(8117923011L), new Integer(500000000), new
	 * Boolean(true) } };
	 */

	private String[] columnNames = { "文件名", "文件大小", "序列数量", "MD5", "校验" };
	DefaultTableModel myTableModel = new DefaultTableModel(columnNames, 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
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
		System.out.println(new Date() + " FqMD5()");
		setSize(800, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setVisible(true);

		System.out.println(new Date() + " creating appPanel");
		appPanel = new JPanel();
//		appPanel.setBackground(Color.lightGray);
		appPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		appPanel.setLayout(new BorderLayout(2, 2));
		setContentPane(appPanel);

		System.out.println(new Date() + " creating controlPanel");
		JPanel controlPanel = createControlPanel();
		System.out.println(new Date() + " creating resultPanel");
		JPanel resultPanel = createResultPanel();
		System.out.println(new Date() + " creating statusBar");
		JStatusBar statusBar = createStatusBar();

		System.out.println(new Date() + " adding controlPanel");
		appPanel.add(controlPanel, BorderLayout.NORTH);
		System.out.println(new Date() + " adding resultPanel");
		appPanel.add(resultPanel, BorderLayout.CENTER);
		System.out.println(new Date() + " adding statusBar");
		appPanel.add(statusBar, BorderLayout.SOUTH);
		
	}

	private JStatusBar createStatusBar() {
//		JPanel statusBar = new JPanel();
////		statusBar.setPreferredSize(new Dimension(parent.getWidth(), 20));
//	    statusBar.setBorder(BorderFactory.createLineBorder(Color.red));
//	    final JLabel status1 = new JLabel("hahaha");
//	    final JLabel status2 = new JLabel("hoho");
//	    status1.setHorizontalAlignment(SwingConstants.LEFT);
//	    status2.setHorizontalAlignment(SwingConstants.LEFT);
//	    statusBar.add(status1);
//	    statusBar.add(status2);
//	    return statusBar;
		JStatusBar statusBar = new JStatusBar();
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		// statusBar.setBackground(Color.LIGHT_GRAY);

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
		controlPanel.setBackground(Color.lightGray);

		JButton btnAdd = new JButton("Add Files");
		JButton btnClear = new JButton("Clear");
		JButton btnCheck = new JButton("Check MD5");
		btnCheck.setEnabled(false);

		// 添加文件
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("INFO add");
				showFileOpen(appPanel);
				System.out.println("INFO add 222 datavector" + myTableModel.getDataVector());
				if (myTableModel.getDataVector() != null) {
					btnCheck.setEnabled(true);
				} else {
					btnCheck.setEnabled(false);
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
		btnCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("check");
				getFileInfo(myTableModel.getDataVector());
				
			}
		});

		controlPanel.add(btnAdd);
		controlPanel.add(btnClear);
		controlPanel.add(btnCheck);
		return controlPanel;
	}

	// result panel
	private JPanel createResultPanel() {
		System.out.println(new Date() + " createResultPanel 1111");
		JPanel resultPanel = new JPanel();
		resultPanel.setPreferredSize(new Dimension(800, 500));
		resultPanel.setBorder(BorderFactory.createLineBorder(Color.red));
		resultPanel.setBackground(Color.lightGray);

		// add a dummy row
		// Object[] newrow = { new File("/path/to/file"), new Long(8117923011L), new
		// Integer(500000000), "md5md5", new Boolean(true) };
		// myTableModel.addRow(newrow);

//		Object[][] data = this.data;
//		JTable table = new JTable(data, columnNames);
		JTable table = new JTable(myTableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.blue));
//		scrollPane.setPreferredSize(new Dimension(700, 400));
//		scrollPane.getViewport().setBackground(Color.ORANGE);
		
		System.out.println(new Date() + " createResultPanel 2222");
		table.setOpaque(false);
		table.setFillsViewportHeight(true);
		table.setBackground(new Color(255, 255, 208));
//		table.setPreferredSize(new Dimension(800, 600));
		table.setPreferredScrollableViewportSize(new Dimension(600, 300));
//		table.setPreferredScrollableViewportSize(table.getPreferredSize());
//		table.setFillsViewportHeight(true);
		// table.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		table.setGridColor(Color.lightGray);
//		table.setFont(new Font("Monospaced", Font.PLAIN, 12));

		System.out.println(new Date() + " createResultPanel 3333");
//		DefaultTableCellRenderer r = new DefaultTableCellRenderer();
//		r.setHorizontalAlignment(JLabel.RIGHT);
//		r.setValue("xxx");
//		table.setDefaultRenderer(File.class, r);
		table.setDefaultRenderer(File.class, new DefaultTableCellRenderer() {
			@Override
			protected void setValue(Object value) {
				super.setText((value == null && value instanceof File) ? "" : ((File) value).getName());
			}
		});

		
		System.out.println(new Date() + " createResultPanel 4444");
		resultPanel.add(scrollPane, BorderLayout.CENTER);

		System.out.println(new Date() + " createResultPanel 5555");
		return resultPanel;
	}

	// select files to add into the table
	public void showFileOpen(Component parent) {
		// file chooser
		JFileChooser fileChooser = new JFileChooser(
				System.getProperty("user.home") + System.getProperty("file.separator") + "prj");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		// TODO custom file filter for sequence files
		FileFilter ffilterGZ = new FileNameExtensionFilter("gzip file", "gz");
		FileFilter ffilterSH = new FileNameExtensionFilter("sh file", "sh");
		FileFilter ffilterBAM = new FileNameExtensionFilter("bam", "bam");
		fileChooser.addChoosableFileFilter(ffilterGZ);
		fileChooser.addChoosableFileFilter(ffilterSH);
		fileChooser.addChoosableFileFilter(ffilterBAM);
//		fileChooser.setFileFilter(ffilterGZ);  // set default file filter
		fileChooser.setAcceptAllFileFilterUsed(true); // false: hide the "[All files]" option
		// 接受结果
		int result = fileChooser.showOpenDialog(parent);
		// 是否选择了文件
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
//			filesToProcess = files;
//			filesToProcess.addAll(Arrays.asList(files));
			for (File file : files) {
				System.out.println(file);
//				filesToProcess.add(file);
				Object[] newrow = { file, file.length(), null, null, null };
				myTableModel.addRow(newrow);
				System.out.println(file.getName());
				System.out.println("data vector" + myTableModel.getDataVector());
			}
		}
	}

	public void getFileInfo(Vector tableDataVector) {
		System.out.println("getFileInfo starting");
		int idx_md5 = myTableModel.findColumn("MD5");
		int idx_readcount = myTableModel.findColumn("序列数量");
		for (int i = 0; i < tableDataVector.size(); i++) {
			File file = (File) ((Vector) tableDataVector.get(i)).get(0);
			System.out.println(file.getPath());
			System.out.println(file.length());
			// 计算Read数

			FastQFile seqFile;
			try {
				seqFile = new FastQFile(file);
				int seqCount = 0;
				while (seqFile.hasNext()) {
					++seqCount;
					Sequence seq;
					seq = seqFile.next();
				}
				System.out.println("total count = " + seqCount);
				myTableModel.setValueAt(seqCount, i, idx_readcount);
			} catch (SequenceFormatException e) {
				e.printStackTrace();
				myTableModel.setValueAt("ERROR", i, idx_readcount);
			} catch (IOException e) {
				e.printStackTrace();
				myTableModel.setValueAt("ERROR", i, idx_readcount);
			}

			// 计算MD5值
			String md5sum = MD5.calculateMD5(file);
			myTableModel.setValueAt(md5sum, i, idx_md5);
			System.out.println(md5sum);
		}
		System.out.println("getFileInfo end");
	}

	public static void getFileInfo(File[] files) {
		System.out.println("getFileInfo starting");
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getPath());
			System.out.println(files[i].length());
			// 计算MD5值
			String md5sum = MD5.calculateMD5(files[i]);
			System.out.println(md5sum);
		}
		System.out.println("getFileInfo end");
	}

	public static void getFileInfo(ArrayList<File> files) {
		System.out.println("getFileInfo starting");
		for (File file : files) {
			System.out.println(file.getPath());
			System.out.println(file.length());
			// 计算MD5值
			String md5sum = MD5.calculateMD5(file);
			System.out.println(md5sum);
		}
		System.out.println("getFileInfo end");
	}

}
