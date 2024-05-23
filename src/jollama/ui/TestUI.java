package jollama.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import jollama.ImageUtil;
import jollama.OllamaClient;

public class TestUI extends JPanel
{
	private static final long serialVersionUID = 1L;

	protected JButton btnGen;
	protected JButton btnQuit;
	protected JButton btnImage;
	
	protected JList<String> listModels;
	protected JTextArea txtPrompt;
	protected JTextArea txtResponse;
	protected JCheckBox chkImage;
	
	protected Map<String,String> nameToJSON = null;
	
	protected OllamaClient client = null;
	
	protected String base64Image = null;
	
	protected JFileChooser jfc = null;
	
	public static final String sync = "mutex";
	
	public TestUI()
	{
		super();
		initGUI();
		initClient();
	}

	protected void initGUI()
	{
		setLayout(new BorderLayout());
		JPanel tmp = new JPanel(new FlowLayout());
		tmp.add(new JLabel("Model:"));
		String sa[] = {"Model Name"};
		listModels = new JList<String>(sa);
		listModels.setVisibleRowCount(1);
		tmp.add(new JScrollPane(listModels));
		
		tmp.add(new JLabel("   "));
		
		EventHandler eh = new EventHandler();
		btnGen = new JButton("Generate");
		btnQuit = new JButton("Quit");
		btnImage = new JButton("Pick Image");
		btnGen.addActionListener(eh);
		btnQuit.addActionListener(eh);
		btnImage.addActionListener(eh);
		chkImage = new JCheckBox("Include Image");
		chkImage.addActionListener(eh);
		
		tmp.add(btnGen);
		tmp.add(btnQuit);
		tmp.add(new JLabel("   "));
		tmp.add(chkImage);
		tmp.add(btnImage);
		btnImage.setEnabled(false);
		add(tmp,BorderLayout.NORTH);
		
		txtPrompt = new JTextArea(15,60);
		txtResponse = new JTextArea(100,60);
		txtPrompt.setLineWrap(true);
		txtResponse.setLineWrap(true);
		tmp = new JPanel(new GridLayout(2,1));
		
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setBorder(BorderFactory.createTitledBorder("Prompt"));
		pnl.add(new JScrollPane(txtPrompt),BorderLayout.CENTER);
		tmp.add(pnl);
		
		pnl = new JPanel(new BorderLayout());
		pnl.setBorder(BorderFactory.createTitledBorder("Response"));
		pnl.add(new JScrollPane(txtResponse),BorderLayout.CENTER);
		tmp.add(pnl);
		
		add(tmp,BorderLayout.CENTER);
	}
	
	protected void initClient()
	{
		client = new OllamaClient();
		List<String> names = client.listModelNames();
	
		int size = names.size();
		String saNames[] = new String[size];
		for(int i=0; i<size; i++)
		{
			saNames[i]=names.get(i);
		}
		listModels.setListData(saNames);
		listModels.setVisibleRowCount(1);
	}
	
	protected void pickImage()
	{
		if(jfc == null)
		{
			jfc = new JFileChooser("Select Image");
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setApproveButtonText("Select Image");
		}
		
		int opt = jfc.showOpenDialog(this);
		if(opt == JFileChooser.APPROVE_OPTION)
		{
			File f = jfc.getSelectedFile();
			String str = ImageUtil.imageToBase64(f);
			this.base64Image = str;
		}
	}
	
	protected void doQuit()
	{
		System.exit(0);
	}
	
	protected void doGen()
	{
		synchronized(sync)
		{
			
			
			try
			{
				String model = listModels.getSelectedValue();
				String prompt = txtPrompt.getText().trim();

				
				Runnable r = new Runnable() {
					public void run() {
						btnGen.setEnabled(false);
						btnGen.repaint();
						TestUI.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						StreamCB cb = new StreamCB();
						
						String img = null;
						if(chkImage.isSelected())
						{
							img = base64Image;
						}
						client.streamGenerateResponse(model, prompt, img, cb);
						
						while(!cb.isFinished())
						{
							try {Thread.sleep(100);}catch(Exception ex) {};
						}
						btnGen.setEnabled(true);
						btnGen.repaint();
						TestUI.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				};
				
				Thread t = new Thread(r);
				t.start();
			}
			finally
			{
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				btnGen.setEnabled(true);
				btnGen.repaint();
			}
		}
	}
	
	protected class EventHandler implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent event) 
		{
			Object src = event.getSource();
			
			if(src == btnGen)
			{
				doGen();
			}
			else if(src == btnQuit)
			{
				doQuit();
			}
			else if(src == chkImage)
			{
				if(chkImage.isSelected())btnImage.setEnabled(true);
				else btnImage.setEnabled(false);
			}
			else if(src == btnImage)
			{
				pickImage();
			}
		}
		
	}
	
	protected class StreamCB implements OllamaClient.StreamTokenCallback
	{
		protected StringBuffer sb = new StringBuffer(10000);
		protected boolean finished = false;
		
		public StreamCB()
		{
			
		}
		
		@Override
		public void nextTokens(String token) 
		{
			sb.append(token);
			txtResponse.setText(sb.toString());
		}
		
		@Override
		public void streamFinished() 
		{
			finished = true;
		}
		
		public boolean isFinished()
		{
			return finished;
		}
		
	}
	
	public static void main(String[] args) 
	{
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new JFrame("Ollama Chat");
                frame.setSize(800, 600);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new TestUI());
                frame.setVisible(true);
            }
        });
	}

}
