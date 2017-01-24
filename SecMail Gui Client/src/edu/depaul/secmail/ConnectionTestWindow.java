/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */

package edu.depaul.secmail;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class ConnectionTestWindow extends Shell {
	private Text txtServer;
	private Text txtResult;

	/**
	 * Launch the application.
	 * @param args
	 */
	//Jacob Burkamper
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			ConnectionTestWindow shell = new ConnectionTestWindow(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * @param display
	 */
	//Jacob Burkamper
	public ConnectionTestWindow(Display display) {
		super(display, SWT.SHELL_TRIM);
		setLayout(new GridLayout(3, false));
		
		Label lblLblserver = new Label(this, SWT.NONE);
		lblLblserver.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLblserver.setText("Server:");
		
		txtServer = new Text(this, SWT.BORDER);
		txtServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		
		Label lblResult = new Label(this, SWT.NONE);
		lblResult.setText("Result:");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		txtResult = new Text(this, SWT.BORDER);
		txtResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		Button btnConnect = new Button(this, SWT.NONE);
		btnConnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				txtResult.setText("");
				txtResult.setText(testConnection());
			}
		});
		btnConnect.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnConnect.setText("Connect");
		new Label(this, SWT.NONE);
		
		Button btnClose = new Button(this, SWT.NONE);
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ConnectionTestWindow.this.close();
			}
		});
		btnClose.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnClose.setText("Close");
		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	//Jacob Burkamper
	protected void createContents() {
		setText("Test SecMail Connection");
		setSize(316, 300);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	//Jacob Burkamper
	//Attempt to connect to a server and send a CONNECTION_TEST command
	private String testConnection()
	{
		String[] server = txtServer.getText().split(":");
		String returnString;
		if (server.length != 2)
		{
			return "Invalid Server format. Please use format <server>:<port>\n";
		}
		
		try {
			Socket s = new Socket(server[0], Integer.valueOf(server[1]));
			
			DHEncryptionIO io = new DHEncryptionIO(s, false);
			
			//create the appropriate packet
			PacketHeader testPacketHeader = new PacketHeader();
			testPacketHeader.setCommand(Command.CONNECT_TEST);
			
			//send the packet
			io.writeObject(testPacketHeader);
			
			//get the response
			PacketHeader responsePacket = (PacketHeader)io.readObject();
			
			if (responsePacket.getCommand() != Command.CONNECT_SUCCESS)
				returnString = "Response Packet contained non-success command";
			else
				returnString = "Successfully connected to remote server.\nSuccessfully transmitted test packet.\n"
						+ "Recieved valid Success packet\n"
						+ "Connection test successful!\n"
						+ "Connection closing...";
			
			io.writeObject(new PacketHeader(Command.CLOSE));
			
			io.close();
			s.close();
		} catch (Exception e)
		{
			returnString = "Exception thrown while trying to connect.\n" + e;
		}
		return returnString;
	}
}
