/*
 * Created on 07.03.2005
 *
 */
package edu.unika.aifb.foam.util;

/*import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;*/

/**
 * @author gl
 */
public class Mail
{
/*	private final String MAIL_HOST = "aifbmail.aifb.uni-karlsruhe.de";
	private final String MAIL_FROM = "annotator@TextToOntoServer.aifb.uni-karlsruhe.de";
	
	public void doNotify(LearnRequest req)
	{
		String text = "The following request was completed: \n";
		text += "Type:            " + (req.isLearnInstance() ? "Instance" : "Document") + "\n";
		text += "URL:             " + req.getUrl() + "\n";
		text += "Ontology URL:    " + req.getOntology() + "\n";
		text += "User:            " + req.getUser() + "\n";
		text += "Started:         " + new Date(req.getStartTime()) + "\n";
		text += "Finished:        " + new Date(req.getEndTime()) + "\n";
		text += "Processing time: " + (req.getEndTime() - req.getStartTime()) / 1000 + " seconds\n";
		if (req.isLearnInstance())
			text +="\nThe generated RDF is attached to this e-mail.";
		else
			text +="\nThe generated RDF and the annotated web page are attached to this e-mail.";
//		text += "\nResult:\n";
//		text += req.getResult();
		
		try
		{
			Properties props = new Properties();
			props.put("mail.smtp.host", Configuration.getString("mail_host", MAIL_HOST));
			
			Message m = new MimeMessage(Session.getInstance(props, null));
			
			m.addFrom(new InternetAddress [] { new InternetAddress(Configuration.getString("annotation_mail_from", MAIL_FROM)) });
			m.setRecipient(Message.RecipientType.TO, new InternetAddress(req.getUser()));
			m.setSubject("C-PANKOW Annotation: Request completed");
			m.setSentDate(new Date());
			
			Multipart multipart = new MimeMultipart();
			MimeBodyPart messageBodyPart;

			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(text);
			multipart.addBodyPart(messageBodyPart);
			
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(req.getResult());
			messageBodyPart.setFileName("annotation.rdfs");
			multipart.addBodyPart(messageBodyPart);
			
			if (!req.isLearnInstance())
			{
				messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(req.getAnnotated());
				messageBodyPart.setFileName("annotation.html");
				multipart.addBodyPart(messageBodyPart);
			}
			
			m.setContent(multipart);
			
			Transport.send(m);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	} 
	
	public void doError(LearnRequest req)
	{
		String text = "The following request generated an error: \n";
		text += "Type:            " + (req.isLearnInstance() ? "Instance" : "Document") + "\n";
		text += "URL:             " + req.getUrl() + "\n";
		text += "Ontology URL:    " + req.getOntology() + "\n";
		text += "User:            " + req.getUser() + "\n";
		text += "Started:         " + new Date(req.getStartTime()) + "\n";
		text += "Finished:        " + new Date(req.getEndTime()) + "\n";
		text += "Processing time: " + (req.getEndTime() - req.getStartTime()) / 1000 + " seconds\n";
		text += "\nThe error was:\n";
		text += req.getError();
		
		try
		{
			Properties props = new Properties();
			props.put("mail.smtp.host", Configuration.getString("mail_host", MAIL_HOST));
			
			Message m = new MimeMessage(Session.getInstance(props, null));
			
			m.addFrom(new InternetAddress [] { new InternetAddress(Configuration.getString("mail_from", MAIL_FROM)) });
			m.setRecipient(Message.RecipientType.TO, new InternetAddress(req.getUser()));
			m.setSubject("C-PANKOW Annotation: Error processing request");
			m.setSentDate(new Date());
			m.setText(text);
			
			Transport.send(m);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	} */
}