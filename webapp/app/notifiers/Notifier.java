package notifiers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import controllers.routes;
import data.EurocentAmount;
import db.DataAccess;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.mvc.Http;
import providers.DataProvider;


/**
 * Created by Stefaan Vermassen on 16/02/14.
 */

public class Notifier extends Mailer {

    public static final String NOREPLY = "Dégage <noreply@degage.be>";

    // to be used with injected context
    public static void sendVerificationMail(User user, String verificationUrl) {
        setSubject("Verifieer jouw Dégage-account");
        addRecipient(user.getEmail());
        addFrom(NOREPLY);
        TemplateDAO dao = DataAccess.getInjectedContext().getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.VERIFICATION);
        String mail = replaceUserTags(user, template.getBody());
        String vUrl = "http://" + Http.Context.current().request().host() + routes.Login.register_verification(user.getId(), verificationUrl).toString();
        mail = mail.replace("%verification_url%", vUrl);

        if (!play.api.Play.isDev(play.api.Play.current())) {
            send(mail);
        }
    }

    /**
     * Creates a notification for the user and automatically invalidates the cache
     *
     * @param dao
     * @param user
     * @param subject
     * @param mail
     */
    private static void createNotification(NotificationDAO dao, User user, String subject, String mail) {
        dao.createNotification(user, subject, mail);
        DataProvider.getCommunicationProvider().invalidateNotifications(user.getId());
        DataProvider.getCommunicationProvider().invalidateNotificationNumber(user.getId());
    }

    // to be used with injected context
    public static void sendWelcomeMail(User user) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.WELCOME);
        String mail = replaceUserTags(user, template.getBody());
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }

    // to be used with injected context
    public static void sendMembershipStatusChanged(User user, boolean approved, String comment) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template;
        if (approved) {
            template = dao.getTemplate(MailType.MEMBERSHIP_APPROVED);
        } else {
            template = dao.getTemplate(MailType.MEMBERSHIP_REFUSED);
        }
        String mail = replaceUserTags(user, template.getBody());
        mail = mail.replace("%comment%", comment);
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }

    // to be used with injected context
    public static void sendCarCostStatusChanged(User user, CarCost carCost, boolean approved) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template;
        if (approved) {
            template = dao.getTemplate(MailType.CARCOST_APPROVED);
        } else {
            template = dao.getTemplate(MailType.CARCOST_REFUSED);
        }
        String mail = replaceUserTags(user, template.getBody());
        mail = replaceCarCostTags(carCost, mail);
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }


    // to be used with injected context
    public static void sendRefuelStatusChanged(User user, Refuel refuel, boolean approved) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template;
        if (approved) {
            template = dao.getTemplate(MailType.REFUEL_APPROVED);
        } else {
            template = dao.getTemplate(MailType.REFUEL_REFUSED);
        }
        String mail = replaceUserTags(user, template.getBody());
        mail = replaceRefuelTags(refuel, mail);
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }

    // to be used with injected context
    public static void sendCarCostRequest(CarCost carCost) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.CARCOST_REQUEST);
        UserRoleDAO userRoleDAO = context.getUserRoleDAO();
        NotificationDAO notificationDAO = context.getNotificationDAO();
        Iterable<User> carAdminList = userRoleDAO.getUsersByRole(UserRole.CAR_ADMIN);
        String mail = replaceCarCostTags(carCost, template.getBody());
        for (User u : carAdminList) {
            createNotification(notificationDAO, u, template.getSubject(), replaceUserTags(u, mail));
        }

    }

    // to be used with injected context
    public static void sendRefuelRequest(User user, Refuel refuel) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.REFUEL_REQUEST);
        String mail = replaceUserTags(user, template.getBody());
        mail = replaceRefuelTags(refuel, mail);
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }

    public static void sendInfoSessionEnrolledMail(DataAccessContext context, User user, InfoSession infoSession) {
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.INFOSESSION_ENROLLED);
        String mail = replaceUserTags(user, template.getBody());
        mail = replaceInfoSessionTags(infoSession, mail);
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }

    // to be used with injected context
    public static void sendReservationApproveRequestMail(User owner, Reservation carReservation, User driver) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.RESERVATION_APPROVE_REQUEST);
        String mail = replaceUserTags(owner, template.getBody());
        mail = replaceCarReservationTags(carReservation, mail, driver);
        mail = mail.replace("%reservation_url%", routes.Drives.details(carReservation.getId()).url());
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, owner, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(owner.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }

    // to be used with injected context
    public static void sendReservationDetailsProvidedMail(User user, Reservation carReservation) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.DETAILS_PROVIDED);
        String mail = replaceUserTags(user, template.getBody());
        mail = replaceCarReservationTags(carReservation, mail, carReservation.getUser());
        mail = mail.replace("%reservation_url%", routes.Drives.details(carReservation.getId()).url());
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }


    // to be used with injected context
    public static void sendReservationApprovedByOwnerMail(User user, String remarks, Reservation carReservation) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        CarDAO cdao = context.getCarDAO();
        Car car = cdao.getCar(carReservation.getCar().getId());
        EmailTemplate template = dao.getTemplate(MailType.RESERVATION_APPROVED_BY_OWNER);
        String mail = replaceUserTags(user, template.getBody());
        mail = replaceCarReservationTags(carReservation, mail, carReservation.getUser());
        mail = mail.replace("%reservation_car_address%", car.getLocation().toString());
        mail = mail.replace("%reservation_remarks%", ("".equals(remarks) ? "[Geen opmerkingen]" : remarks));
        mail = mail.replace("%reservation_url%", routes.Drives.details(carReservation.getId()).toString());
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }

    // to be used with injected context
    public static void sendReservationRefusedByOwnerMail(User user, String reason, Reservation carReservation) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.RESERVATION_REFUSED_BY_OWNER);
        String mail = replaceUserTags(user, template.getBody());
        mail = replaceCarReservationTags(carReservation, mail, carReservation.getUser());
        mail = mail.replace("%reservation_reason%", reason);
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }

    // to be used with injected context
    public static void sendContractManagerAssignedMail(User user, Approval approval) {
        DataAccessContext context = DataAccess.getInjectedContext();
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.CONTRACTMANAGER_ASSIGNED);
        String mail = replaceUserTags(user, template.getBody());
        mail = mail.replace("%admin_name%", approval.getAdmin().getFullName());
        NotificationDAO notificationDAO = context.getNotificationDAO();
        createNotification(notificationDAO, user, template.getSubject(), mail);
        if (template.getSendMail()) {
            setSubject(template.getSubject());
            addRecipient(user.getEmail());
            addFrom(NOREPLY);
            send(mail);
        }
    }

    // to be used with injected context
    public static void sendPasswordResetMail(User user, String verificationUrl) {
        setSubject("Wachtwoord opnieuw instellen");
        addRecipient(user.getEmail());
        addFrom(NOREPLY);
        TemplateDAO dao = DataAccess.getInjectedContext().getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.PASSWORD_RESET);
        String mail = replaceUserTags(user, template.getBody());
        String vUrl = "http://" + Http.Context.current().request().host() + routes.Login.resetPassword(user.getId(), verificationUrl).toString();
        mail = mail.replace("%password_reset_url%", vUrl);

        if (!play.api.Play.isDev(play.api.Play.current())) {
            send(mail);
        }
    }

    public static void sendReminderMail(DataAccessContext context, User user) {
        setSubject("Ongelezen berichten");
        addRecipient(user.getEmail());
        addFrom(NOREPLY);
        TemplateDAO dao = context.getTemplateDAO();
        EmailTemplate template = dao.getTemplate(MailType.REMINDER_MAIL);
        String mail = replaceUserTags(user, template.getBody());
        send(mail);
        SchedulerDAO sdao = context.getSchedulerDAO();
        sdao.setReminded(user);
    }

    private static String replaceUserTags(User user, String template) {
        template = template.replace("%user_firstname%", user.getFirstName());
        template = template.replace("%user_lastname%", user.getLastName());
        //TODO: replace address only when provided
        return template;
    }

    private static String replaceInfoSessionTags(InfoSession infoSession, String template) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("E, d MMM yyyy HH:mm");
        template = template.replace("%infosession_date%", fmt.print(infoSession.getTime()));
        template = template.replace("%infosession_address%", infoSession.getAddress().toString());
        return template;
    }

    private static String replaceCarReservationTags(Reservation carReservation, String template, User driver) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("E, d MMM yyyy HH:mm");
        template = template.replace("%reservation_from%", fmt.print(carReservation.getFrom()));
        template = template.replace("%reservation_to%", fmt.print(carReservation.getTo()));
        template = template.replace("%comment%", carReservation.getMessage() == null ? "[Geen commentaar]" : carReservation.getMessage());
        template = template.replace("%reservation_user_firstname%", driver.getFirstName());
        template = template.replace("%reservation_user_lastname%", driver.getLastName());
        return template;
    }

    private static String replaceCarCostTags(CarCost carCost, String template) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("E, d MMM yyyy HH:mm");
        template = template.replace("%car_cost_time%", fmt.print(carCost.getTime()));
        template = template.replace("%car_name%", carCost.getCar().getName());
        template = template.replace("%amount%", carCost.getAmount().toPlainString() + " euro");
        template = template.replace("%car_cost_description%", carCost.getDescription());
        return template;
    }

    private static String replaceRefuelTags(Refuel refuel, String template) {
        template = template.replace("%car_name%", refuel.getCarRide().getReservation().getCar().getName());
        template = template.replace("%amount%", EurocentAmount.toString(refuel.getEurocents()) + " euro");
        return template;
    }

}
