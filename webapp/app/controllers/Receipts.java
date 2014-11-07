package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.*;
import be.ugent.degage.db.models.*;
import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import controllers.util.FileHelper;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import org.joda.time.DateTime;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.receipts.receipts;
import views.html.receipts.receiptspage;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.Period;
import java.util.List;

public class Receipts extends Controller {

    private static final int PAGE_SIZE = 10;

    /**
     * @return The users index-page with all users
     */
    @AllowRoles({UserRole.CAR_USER, UserRole.PROFILE_ADMIN})
    @InjectContext
    public static Result index() {
        return ok(receipts.render());
    }

    /**
     * @param page    The page in the userlists
     * @param ascInt  An integer representing ascending (1) or descending (0)
     * @param orderBy A field representing the field to order on
     * @return A partial page with a table of users of the corresponding page
     */
    // @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result showReceiptsPage(int page, int ascInt, String orderBy, String date) {
        // TODO: orderBy not as String-argument?
        FilterField receiptsField = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(date);
        return ok(receiptsList(page, receiptsField, asc, filter));
    }

    // used within inject context
    private static Html receiptsList(int page, FilterField orderBy, boolean asc, Filter filter) {
        User currentUser = DataProvider.getUserProvider().getUser();
        ReceiptDAO dao = DataAccess.getInjectedContext().getReceiptDAO();

        if (orderBy == null) {
            orderBy = FilterField.RECEIPT_DATE;
        }
        List<Receipt> listOfReceipts = dao.getReceiptsList(orderBy, asc, page, PAGE_SIZE, filter, currentUser);

        int amountOfResults = dao.getAmountOfReceipts(filter, currentUser);
        //int amountOfResults = listOfReceipts.size();
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) PAGE_SIZE);

        //if(){rendernew()}
        return receiptspage.render(listOfReceipts, page, amountOfResults, amountOfPages);
    }


}
