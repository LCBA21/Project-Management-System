package com.algoExpert.demo.UserAccount.AccountInfo.Service.Impl;

import com.algoExpert.demo.AppNotification.AppEmailBuilder;
import com.algoExpert.demo.AppUtils.ImageConvertor;
import com.algoExpert.demo.ExceptionHandler.UserNotFound;
import com.algoExpert.demo.Records.PasswordRequest;
import com.algoExpert.demo.Entity.User;
import com.algoExpert.demo.ExceptionHandler.InvalidArgument;
import com.algoExpert.demo.Repository.UserRepository;
import com.algoExpert.demo.UserAccount.AccountInfo.Entity.PasswordReset;
import com.algoExpert.demo.UserAccount.AccountInfo.Repository.PasswordResetRepository;
import com.algoExpert.demo.UserAccount.AccountInfo.Service.PasswordResetService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.algoExpert.demo.AppUtils.AppConstants.*;

@Service
@Slf4j
public class RecoverPasswordServiceImpl implements PasswordResetService {

    @Autowired
    private PasswordResetRepository resetRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AppEmailBuilder appEmailBuilder;
    static final Long EXPIRING_TIME = 1L;
    @Value("${confirm.password.url}")
    String confirmLink;

    @Autowired
    private ImageConvertor imageConvertor;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Environment environment;

    @Autowired
    private SpringTemplateEngine templateEngine;



    /**
     * Confirming the new password
     * <p>
     * this method receives a token as parameter and validated if the token is still valid, if expired delete the existing
     * token and create a new token then send email to confirm the new password
     * @param token
     * @return
     * @throws InvalidArgument
     * @Author Santos Rafaelo
     */
    @Override
    public String confirmPassword(String token) throws InvalidArgument {
        PasswordReset passwordReset = resetRepository.findByPasswordToken(token)
                .orElse(null);

        if (passwordReset == null) {
            return "No Longer Applicable. Please request a new one to continue";
        }


        if (isTokenExpired(passwordReset.getExpiresAt())){
            deleteResetPassword(passwordReset);
            return "The password reset link has expired. Please click the button below to request a new one to continue";
        }

        User user = passwordReset.getUser();
        String newPassword = passwordReset.getNewPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        deleteResetPassword(passwordReset);
        String link = "http://localhost:8080/login";
        log.info("Password reset was successful: {}", login(link));
        return "Password Reset Was Successful ";
    }

    @Override
    public void saveToken(PasswordReset passwordReset) {
        resetRepository.save(passwordReset);
    }

    /**
     * Creates a password reset request for the specified user based on the provided password request.
     * <p>
     * This method creates a new password reset request for the user identified by the username/email
     * provided in the password request. It generates a unique token, associates it with the user, and
     * sends an email containing a reset password link to the user's email address.
     * </p>
     *
     * @param passwordRequest The password request containing the user's username/email, new password,
     *                        and confirmation password.
     * @return The created PasswordReset object representing the password reset request.
     * @throws InvalidArgument If the provided username/email is not found in the user repository,
     *                         if the new password and confirmation password do not match, or if there
     *                         is an error while processing the password reset request.
     * @Author Santos Rafaelo
     */
    @Override
    public PasswordReset createPasswordReset(PasswordRequest passwordRequest) throws InvalidArgument {
        User user = userRepository.findByEmail(passwordRequest.userName())
                .orElseThrow(()-> new InvalidArgument(String.format(USERNAME_NOT_FOUND,passwordRequest.userName())));

        PasswordReset passwordByUserId = findPasswordByUserId(user.getUser_id());
        if(passwordByUserId !=null){
            deleteResetPassword(passwordByUserId);
        }

        if (!passwordRequest.password().trim().equals(passwordRequest.confirmPassword().trim())){
            throw new InvalidArgument(PASSWORD_MISMATCH);
        }
        String token = UUID.randomUUID().toString();

        PasswordReset passwordReset = PasswordReset.builder()
                .passwordToken(token)
                .newPassword(passwordRequest.confirmPassword())
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRING_TIME))
                .user(user).build();
        saveToken(passwordReset);
        //TODO change TEMP_USER_EMAIL to user.getUsername()
        String link = confirmLink + passwordReset.getPasswordToken();
        appEmailBuilder.sendEmailResetPassword(TEMP_USER_EMAIL,login(link));

        log.info("Click the link to reset the password : {}", login(link));
        return passwordReset;
    }

    @Override
    public void deleteResetPassword(PasswordReset confirmation) {
        resetRepository.delete(confirmation);
    }

    @Override
    public boolean isTokenExpired(LocalDateTime expiresAt) {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    @Override
    public PasswordReset findPasswordByUserId(int userId) {
        return resetRepository.findPasswordByUserId(userId);
    }

    @Override
    public PasswordReset findPasswordByToken(String token) throws InvalidArgument {
        return resetRepository.findByPasswordToken(token).get();

    }
    @Override
    public String sendPasswordRecoveryEmail(String email) {
        User user =  userRepository.findByEmail(email).get();
        if(user == null){
            throw new UserNotFound("requested user was not found in database");
        }
        String token =  UUID.randomUUID().toString();
        storeUserPasswordToken(token,user);
        return constructEmailToken(token,user);
    }

    public String constructEmailToken(String token, User user){
        String confirmationUrl = "http://localhost:8080/auth/forgotPassword?token=" + token;
        Context context = new Context();
        context.setVariable("confirmationUrl", confirmationUrl);

        String htmlContent = templateEngine.process("reset_password",context);
        return constructEmail("Reset Password",htmlContent, user);
    }
    public String constructEmail(String subject, String body, User user){
        MimeMessage email = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;

        //attach image to html page
        ModelAndView modelAndView = new ModelAndView("reset_password");
        String imageCon =  imageConvertor.imageToBase64("password_reset.png");
        modelAndView.addObject("password_reset_image", imageCon);

        try {
            helper = new MimeMessageHelper(email, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(body,true);
            javaMailSender.send(email);
            return "Mail send successfully";
        }catch (MessagingException e){
            e.printStackTrace();
            return "Mail could not be sent";
        }
    }

    public void storeUserPasswordToken(String token, User user){
        PasswordReset foundPasswordReset =  resetRepository.findPasswordByUserId(user.getUser_id());

        if(foundPasswordReset != null){
            foundPasswordReset.setPasswordToken(token);
            resetRepository.save(foundPasswordReset);
        }else {
            PasswordReset passwordReset =  PasswordReset.builder()
                    .passwordToken(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            resetRepository.save(passwordReset);
        }
    }




    String login(String link){
        return "<a href=\"" + link + "\">";
    }

}
