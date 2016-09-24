import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class PhotoAlbum{
	
	private static ArrayList<Photo> pic = new ArrayList<Photo>();
	private static File file;
	private static JList<String> listPics;
	private static JTextField txtFile, txtTitleView;
	private static JLabel lblPhotoView, lblPrev;
	private static JButton btnRemove, btnView, btnNext, btnPrevious;
	private static JTextArea txtAnnView;
	private static int index = -1;
	private static JPanel viewTab;
	
	//------------utility functions---------------//
	
	//----to resize an image
    private static BufferedImage resizeImage(BufferedImage originalImage, int type, int width, int height){
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
			
		return resizedImage;
    }

    //----to update the photo list after a photo has been added or removed
	public static void updateList(){
		String[] str = new String[pic.size()];
		
		for(int i=0; i< pic.size(); i++){
			str[i] = pic.get(i).getTitle();
		}
		
		listPics.setListData(str);
		if(pic.size() >0)
			listPics.setSelectedIndex(0);
		
		btnRemove.setEnabled(pic.size()>0);
		btnView.setEnabled(pic.size()>0);
	}
	
	//----to refresh the displayed photo when View, Previous or Next button is clicked
	public static void refreshPhoto(){

		try{	
			BufferedImage image = ImageIO.read(pic.get(index).getImage());
			int w = image.getWidth(), h= image.getHeight();
			if(w> 857) w= 857;
			if(h> 492) h = 492;
			lblPhotoView.setIcon(new ImageIcon(resizeImage(image, image.getType(), w, h )));

		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "Oops! Some error occured in fetching photo.");
		}
			
		txtTitleView.setText(pic.get(index).getTitle());
		txtAnnView.setText(pic.get(index).getAnn());
		
		btnNext.setEnabled(index < pic.size()-1);
		btnPrevious.setEnabled(index > 0);
	}
	
	//----to save records to file at close
	public static void saveToFile(){
		try{
		
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("photos"));
			os.writeObject(pic);
			os.close();
		}catch(Exception e){}
	}

	//----to load records from file at start
	public static void loadFromFile(){

		try{
			ObjectInputStream is = new ObjectInputStream(new FileInputStream("photos"));
			pic = (ArrayList<Photo>) is.readObject();
			is.close();
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "No saved records were found.");
		}
		
		//now check loaded records for consistency
		int i=0, remCount = 0;
		while(i<pic.size()){
			try{
				ImageIO.read(pic.get(i).getImage());
			}catch(Exception e){
				remCount++;
				pic.remove(i);
				continue;
			}
			i++;
		}
		
		if(remCount>0){
			JOptionPane.showMessageDialog(null, remCount + " record(s) removed because of missing files.");
		}

	}
	
	//---------------end of utility functions----------------//
	//---------------static classes--------------------------//

	
	public static class Photo implements Serializable{
		private File img;
		private String title, ann;

		public Photo(File f, String t, String a) {
			try{
				img = f;
				title = t;
				ann = a;
			}catch(Exception e){}
		}
		
		public String getTitle(){
			return title;
		}
		public String getAnn(){
			return ann;
		}
		public File getImage(){
			return img;
		}
	}

	public static class PhotoAlbumPanel extends JPanel{
		public PhotoAlbumPanel(JFrame window){
			setSize(900,700);
			setLayout(null);
			
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setEnabled(false);
			tabbedPane.setBounds(0, 0, 898, 665);
			add(tabbedPane);
			
			JPanel mainTab = new JPanel();
			tabbedPane.addTab("Main", null, mainTab, null);
			mainTab.setLayout(null);
			
			viewTab = new JPanel();
			tabbedPane.addTab("View", null, viewTab, null);
			viewTab.setLayout(null);
			
			listPics = new JList<String>();
			listPics.setBounds(78, 92, 544, 199);
			mainTab.add(listPics);
			
			btnView = new JButton("View");
			btnView.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					index = listPics.getSelectedIndex();
					tabbedPane.setSelectedIndex(1);
					refreshPhoto();
					
				}
			});
			btnView.setBounds(246, 566, 156, 26);
			mainTab.add(btnView);
			
			btnRemove = new JButton("Remove");
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int ch = listPics.getSelectedIndex();
					pic.remove(ch);
					updateList();
					
				}
			});
			btnRemove.setBounds(78, 566, 156, 26);
			mainTab.add(btnRemove);
			
			JLabel lblPhotosList = new JLabel("Your photos:");
			lblPhotosList.setBounds(78, 64, 211, 16);
			mainTab.add(lblPhotosList);
			
			JButton btnAddNew = new JButton("Add new...");
			btnAddNew.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tabbedPane.setSelectedIndex(2);
				}
			});
			btnAddNew.setBounds(672, 566, 156, 26);
			mainTab.add(btnAddNew);
			
			JPanel newTab = new JPanel();
			tabbedPane.addTab("New", null, newTab, null);
			newTab.setLayout(null);
			
			JLabel lblSelectFile = new JLabel("Source file*");
			lblSelectFile.setBounds(35, 56, 106, 16);
			newTab.add(lblSelectFile);
			
			JTextField txtTitle = new JTextField();
			txtTitle.setBounds(137, 321, 380, 20);
			newTab.add(txtTitle);
			txtTitle.setColumns(10);
			
			
			JButton btnBrowse = new JButton("Browse...");
			btnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					FileFilter filter = new FileNameExtensionFilter("Image file", new String[] {"png", "gif", "jpg", "jpeg"});
					fc.setFileFilter(filter);
					fc.setAcceptAllFileFilterUsed(false);
					fc.addChoosableFileFilter(filter);
					int retval = fc.showOpenDialog(window);
					if(retval == JFileChooser.APPROVE_OPTION){
						file = fc.getSelectedFile();
						txtFile.setText(file.getName());
						txtTitle.setText(file.getName());
						
						try{
							
							BufferedImage image = ImageIO.read(file);
							int w = image.getWidth(), h= image.getHeight();
							if(w> 289) w= 289;
							if(h> 136) h = 136;
							
							lblPrev.setIcon(new ImageIcon(resizeImage(image, image.getType(), w, h )));
						}catch(Exception ex){
							JOptionPane.showMessageDialog(null, "Oops! Some error occured in fetching photo.");
						}
						
					}
				}
			});
			btnBrowse.setBounds(592, 51, 143, 26);
			newTab.add(btnBrowse);
			
			JLabel lblImageTitle = new JLabel("Image title*");
			lblImageTitle.setBounds(35, 323, 104, 16);
			newTab.add(lblImageTitle);
			
			JLabel lblAnnotation = new JLabel("Annotation:");
			lblAnnotation.setBounds(35, 369, 110, 16);
			newTab.add(lblAnnotation);
			
			
			JTextArea txtAnn = new JTextArea();
			txtAnn.setLineWrap(true);
			txtAnn.setWrapStyleWord(true);
			txtAnn.setBounds(137, 369, 380, 126);
			newTab.add(txtAnn);
			
			JButton btnAdd = new JButton("Add");
			btnAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					if(file == null){
						JOptionPane.showMessageDialog(window, "You need to select a photo."); return;
					}
					if(txtTitle.getText().equals("") || txtTitle.getText().length()>20){
						JOptionPane.showMessageDialog(window, "You need to give a title between 1 and 20 chars only."); return;	
					}
					if(txtAnn.getText().length()>100){
						JOptionPane.showMessageDialog(window, "Annotation must be within 100 chars only."); return;
					}
					if(pic.size() == 10){
						JOptionPane.showMessageDialog(window, "You cannot add more than 10 photos in the album."); return;
					}
					pic.add(new Photo(file, txtTitle.getText(), txtAnn.getText()));
					updateList();
					txtTitle.setText("");
					txtAnn.setText("");
					txtFile.setText("");
					file = null;
					lblPrev.setIcon(null);
					tabbedPane.setSelectedIndex(0);
					
				}
			});
			btnAdd.setBounds(585, 560, 150, 26);
			newTab.add(btnAdd);
			
			txtFile = new JTextField();
			txtFile.setEditable(false);
			txtFile.setBounds(137, 54, 380, 20);
			newTab.add(txtFile);
			txtFile.setColumns(10);
			
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					txtTitle.setText("");
					txtAnn.setText("");
					txtFile.setText("");
					file = null;
					lblPrev.setIcon(null);
					tabbedPane.setSelectedIndex(0);
				}
			});
			btnCancel.setBounds(393, 560, 150, 26);
			newTab.add(btnCancel);
			
			JLabel lblPreview = new JLabel("Preview:");
			lblPreview.setBounds(35, 98, 66, 16);
			newTab.add(lblPreview);
			
			lblPrev = new JLabel("");
			lblPrev.setBounds(147, 86, 336, 222);
			newTab.add(lblPrev);
			
			JLabel lblDenotesRequired = new JLabel("* denotes required fields");
			lblDenotesRequired.setBounds(35, 527, 170, 16);
			newTab.add(lblDenotesRequired);
			
			
			JLabel lblTitleView = new JLabel("Title:");
			lblTitleView.setBounds(122, 539, 66, 16);
			viewTab.add(lblTitleView);
			
			JLabel lblAnnotationView = new JLabel("Annotation:");
			lblAnnotationView.setBounds(122, 567, 135, 16);
			viewTab.add(lblAnnotationView);
			
			btnNext = new JButton("Next");
			btnNext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					index++;
					refreshPhoto();
				}
			});
			btnNext.setBounds(789, 595, 92, 26);
			viewTab.add(btnNext);
			
			btnPrevious = new JButton("Previous");
			btnPrevious.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					index--;
					refreshPhoto();
				}
			});
			btnPrevious.setBounds(12, 595, 92, 26);
			viewTab.add(btnPrevious);
			
			txtTitleView = new JTextField();
			txtTitleView.setEditable(false);
			txtTitleView.setBounds(219, 537, 531, 20);
			viewTab.add(txtTitleView);
			txtTitleView.setColumns(10);
			
			txtAnnView = new JTextArea();
			txtAnnView.setEditable(false);
			txtAnnView.setLineWrap(true);
			txtAnnView.setWrapStyleWord(true);
			txtAnnView.setBounds(219, 559, 531, 62);
			viewTab.add(txtAnnView);
			
			JButton btnBack = new JButton("Back");
			btnBack.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tabbedPane.setSelectedIndex(0);
				}
			});
			btnBack.setBounds(12, 534, 92, 26);
			viewTab.add(btnBack);
			
			lblPhotoView = new JLabel("");
			lblPhotoView.setBounds(12, 12, 869, 504);
			viewTab.add(lblPhotoView);

			updateList();
		}
	}
	
	
	//-------------end of static classes----------------------//
	
	public static void main(String[] args){
		
		loadFromFile();
		JFrame window = new JFrame("Digital Photo Album");
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try{ 
					saveToFile();
				}catch(Exception ex){}
			}
		});
		window.setLocation(new Point(200, 10));
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(new Dimension(900, 700));
		window.setContentPane(new PhotoAlbumPanel(window));
		window.setVisible(true);
		
	}
}