package edu.depaul.secmail.content;
import java.text.DateFormat;
import java.util.LinkedList;
import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.Main;
import edu.depaul.secmail.Notification;
import edu.depaul.secmail.NotificationType;
import edu.depaul.secmail.ResponseContent;

public class Inbox extends ResponseContent {

	public Inbox(MailServerConnection c) {
		super(true, c);
		LinkedList<Notification> notifications = super.mainConnection.getNewNotifications();
		if (notifications != null) {
			setContent("<b>Your messages: </b><br>");
			addContent("<div class='container-fluid'><div class='row'><div class='col-md-8 col-sm-8 col-xs-12 col-md-offset-2'>");
			for (Notification n : notifications) {
				String date = DateFormat.getDateTimeInstance().format(n.getDate());
				if (n.getType() == NotificationType.NEW_EMAIL) {
					addContent("<form class='form-horizontal' method='post' action='/readmail'>");
					addContent("<button class='btn btn-default btn-block'>");
					addContent("<div style='float:left;'>From: " + n.getFrom().compile() + "</div>");
					addContent("<div style='float:right;'>&nbsp;&nbsp; Date: " + date + "</div>");
					addContent("<div style='float:left; margin-left:20%;'>&nbsp;&nbsp; Subject: " + n.getSubject() + "</div>");
					addContent("</button>");
					addContent("<input type='hidden' name='notificationid' value='" + n.getID() + "'>");
					addContent("<input type='hidden' name='emailfrom' value='" + n.getFrom().compile() + "'>");
					addContent("<input type='hidden' name='emaildate' value='" + date + "'>");
					addContent("</form><br>");
				}
			}
			addContent("</div></div></div>");
		}
		else {
			setContent("<b>No Messages</b>");
		}
	}
}
