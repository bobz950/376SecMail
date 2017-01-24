/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import swing2swt.layout.BoxLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

public class MainWindow {

	protected Shell shlSecmail;
	private DHEncryptionIO serverIO = null;
	private static String mailDir = "./mail/";
	private static final int SHELL_TRIM = SWT.CLOSE | SWT.TITLE | SWT.MIN;

	/**
	 * Launch the application.
	 * @param args
	 */
	//Jacob Burkamper
	public static void main(String[] args) {
		try {
			File mailDirFile = new File(mailDir);
			if (!mailDirFile.exists() && !mailDirFile.mkdir())
			{
				System.err.println("Error, unable to open mail directory: "+mailDirFile.getAbsolutePath());
				System.exit(10);
			}
			MainWindow window = new MainWindow();
			window.open();	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	//Jacob Burkamper
	public void open() {
		Display display = Display.getDefault();
		createContents();
				
		shlSecmail.open();
		shlSecmail.layout();
		
		//get login info
		LoginDialog login = new LoginDialog(shlSecmail, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		LoginDialog.Status result = login.open();
		
		if (result != LoginDialog.Status.LOGIN_SUCCESS) // we exited or the login failed
			shlSecmail.close();
		
		serverIO = login.getServerConnection();
		
		while (!shlSecmail.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	//Jacob Burkamper
	protected void createContents() {
		shlSecmail = new Shell(SHELL_TRIM & (~SWT.RESIZE));
		shlSecmail.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				if (serverIO != null)
					try {
						serverIO.writeObject(new PacketHeader(Command.CLOSE));
						serverIO.close();
					} catch (IOException ex)
					{
						System.out.println("IOException while trying to close application");
						System.out.println(ex);
					}
			}
		});
		shlSecmail.setSize(466, 378);
		shlSecmail.setText("SecMail");
		shlSecmail.setLayout(new FormLayout());
		
		Composite composite = new Composite(shlSecmail, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		FormData fd_composite = new FormData();
		fd_composite.right = new FormAttachment(0, 298);
		fd_composite.top = new FormAttachment(0, 10);
		fd_composite.left = new FormAttachment(0, 155);
		fd_composite.bottom = new FormAttachment(0, 299);
		composite.setLayoutData(fd_composite);
		
		Button btnNewEmail = new Button(composite, SWT.NONE);
		btnNewEmail.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				MailWriter newMail = new MailWriter(Display.getCurrent(), MainWindow.this.serverIO);
				newMail.open();
			}
		});
		GridData gd_btnNewEmail = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnNewEmail.widthHint = 133;
		btnNewEmail.setLayoutData(gd_btnNewEmail);
		btnNewEmail.setText("New Email");
		
		Button btnFetchMail = new Button(composite, SWT.NONE);
		btnFetchMail.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				RecvMailWindow recvMail = new RecvMailWindow(Display.getCurrent(), serverIO);
				recvMail.open();
			}
		});
		GridData gd_btnFetchMail = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnFetchMail.widthHint = 131;
		btnFetchMail.setLayoutData(gd_btnFetchMail);
		btnFetchMail.setText("Fetch Mail");
		
		Button btnTestConnection = new Button(composite, SWT.NONE);
		btnTestConnection.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ConnectionTestWindow test = new ConnectionTestWindow(Display.getCurrent());
				test.open();
			}
		});
		GridData gd_btnTestConnection = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnTestConnection.widthHint = 134;
		btnTestConnection.setLayoutData(gd_btnTestConnection);
		btnTestConnection.setText("Test Connection");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		Button btnClose = new Button(composite, SWT.NONE);
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				shlSecmail.close();
			}
		});
		GridData gd_btnClose = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnClose.widthHint = 134;
		btnClose.setLayoutData(gd_btnClose);
		btnClose.setText("Close");

	}
	//Jacob Burkamper
	public static String getMailDir()
	{
		return mailDir;
	}
}
