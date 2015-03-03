package net.sblit.configuration.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
/**
 * 
 * @author Nikola
 *
 */
@SuppressWarnings("serial")
@Deprecated
public class DataDirectoryConfiguration extends JFrame{
	
	private JFileChooser fileChooser;
	private JFrame frame = this;
	private JTextField directory;
	public File confDirectory;
	
	public DataDirectoryConfiguration(File configurationDirectory) {
		this.confDirectory = configurationDirectory;
		setTitle("SBLIT - Choose directory");
		setSize(500, 100);
		
		JLabel text = new JLabel("Choose the directory to synchronize: ");
		directory = new JTextField();
		directory.setEditable(false);
		JButton dialog = new JButton("Choose Directory...");
		JButton confirm = new JButton("OK");
		JPanel contentPane = new JPanel();
		BorderLayout layout = new BorderLayout();
		
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		confirm.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String syncDirectory = directory.getText();
				if(syncDirectory != null && !syncDirectory.equals("")){
					try {
						BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(confDirectory.getAbsolutePath() + "\\syncdirectory.txt"))));
						br.write(syncDirectory);
						br.close();
						frame.setVisible(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		});
		
		contentPane.setLayout(layout);
		contentPane.add(text, BorderLayout.WEST);
		contentPane.add(directory, BorderLayout.CENTER);
		contentPane.add(dialog, BorderLayout.EAST);
		contentPane.add(confirm, BorderLayout.SOUTH);
		
		dialog.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int dialog = fileChooser.showDialog(frame, "Choose");
				if(dialog == JFileChooser.APPROVE_OPTION){
					directory.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		setContentPane(contentPane);
		setVisible(true);
	}
//	
//	private File getDirectory(){
//		
//		return null;	
//	}
	
}

