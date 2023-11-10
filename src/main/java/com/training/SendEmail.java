package com.training;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmail {
    public static void main(String[] args) {
        final String smtpServer = "smtp.office365.com";
        final String smtpPort = "587"; // Use 587 for TLS
        final String smtpUsername = "nagendra_nallamilli@suchiit.com";
        final String smtpPassword = "n@ge1994";
        final String senderEmail = "nagendra_nallamilli@suchiit.com";
        final String recipientEmail = "nagendra_nallamilli@suchiit.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("SMTP Test");
            message.setText("This is a test email sent via Office 365 SMTP using Java.");

            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            System.err.println("Email could not be sent. Error: " + e.getMessage());
        }
    }
}
