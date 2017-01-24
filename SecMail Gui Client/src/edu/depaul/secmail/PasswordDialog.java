/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class PasswordDialog extends Dialog {

	protected Object result;
	protected Shell shlPassword;
	private Text text;
	private Label lblText;
	private boolean exit_ok = false;
	private String pass = null;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	//Jacob Burkamper
	public PasswordDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
		createContents();
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	//Jacob Burkamper
	public boolean open() {
		shlPassword.open();
		shlPassword.layout();
		Display display = getParent().getDisplay();
		while (!shlPassword.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return exit_ok;
	}

	/**
	 * Create contents of the dialog.
	 */
	//Jacob Burkamper
	private void createContents() {
		shlPassword = new Shell(getParent(), getStyle());
		shlPassword.setSize(531, 126);
		shlPassword.setText("Password");
		shlPassword.setLayout(new FormLayout());
		
		lblText = new Label(shlPassword, SWT.NONE);
		lblText.setAlignment(SWT.CENTER);
		FormData fd_lblText = new FormData();
		fd_lblText.top = new FormAttachment(0, 10);
		fd_lblText.left = new FormAttachment(0, 10);
		fd_lblText.right = new FormAttachment(100,-10);
		lblText.setLayoutData(fd_lblText);
		lblText.setText("Text");
		
		text = new Text(shlPassword, SWT.BORDER);
		FormData fd_text = new FormData();
		fd_text.right = new FormAttachment(lblText, 0, SWT.RIGHT);
		fd_text.top = new FormAttachment(lblText, 6);
		fd_text.left = new FormAttachment(0, 10);
		text.setLayoutData(fd_text);
		text.setEchoChar('*');
		
		Button btnCancel = new Button(shlPassword, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exit_ok = false;
				shlPassword.close();
			}
		});
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		fd_btnCancel.right = new FormAttachment(lblText, 0, SWT.RIGHT);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		
		Button btnOk = new Button(shlPassword, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pass = text.getText();
				exit_ok = true;
				shlPassword.close();
			}
		});
		FormData fd_btnOk = new FormData();
		fd_btnOk.bottom = new FormAttachment(btnCancel, 0, SWT.BOTTOM);
		fd_btnOk.left = new FormAttachment(lblText, 0, SWT.LEFT);
		btnOk.setLayoutData(fd_btnOk);
		btnOk.setText("Ok");

	}
	
	//Jacob Burkamper
	public void setTitle(String titleText)
	{
		setText(titleText);
	}
	
	//Jacob Burkamper
	public void setMessage(String message)
	{
		lblText.setText(message);
	}
	
	//Jacob Burkamper
	public boolean returnOK()
	{
		return exit_ok;
	}
	
	//Jacob Burkamper
	public String getText()
	{
		return pass;
	}
}
