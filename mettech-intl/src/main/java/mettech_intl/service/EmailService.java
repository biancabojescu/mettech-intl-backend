package mettech_intl.service;

import mettech_intl.exception.EmailFailureException;
import mettech_intl.model.Users;
import mettech_intl.model.VerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Value("${email.from}")
    private String fromAddress;
    @Value("${app.frontend.url}")
    private String url;
    private JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    private SimpleMailMessage makeMailMessage(){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromAddress);
        return simpleMailMessage;
    }

    public void sendVerificationEmail(VerificationToken verificationToken) throws EmailFailureException {
        SimpleMailMessage message = makeMailMessage();
        message.setTo(verificationToken.getUser().getEmail());
        message.setSubject("mettech intl - verify your mail");
        message.setText("Vă rog, accesați link-ul următor pentru a vă activa contul.\n"
                + url + "/auth/verify?token=" + verificationToken.getToken());
        try{
            javaMailSender.send(message);
        } catch(MailException ex){
            throw new EmailFailureException();
        }
    }

    public void sendPasswordResetEmail(Users user, String token) throws EmailFailureException{
        SimpleMailMessage message = makeMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("mettech intl. - password reset");
        message.setText("Ați solicitat recuperarea parolei. Gasiți mai jos link-ul ce vă va permite resetarea accesteia.\n" +
                url + "/auth/reset?token="+token);
        try{
            javaMailSender.send(message);
        } catch (MailException ex){
            throw new EmailFailureException();
        }
    }
}
