package com.algoadda.core.email;

import com.algoadda.core.compliance.ComplianceCheck;
import com.algoadda.core.order.License;
import com.algoadda.core.order.Order;
import com.algoadda.core.order.OrderItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final SesClient sesClient;
    private final String fromEmail;
    private final String appUrl;
    private final ObjectMapper objectMapper;

    public EmailService(
        SesClient sesClient,
        @Value("${algoadda.aws.ses.from-email:noreply@algoadda.com}") String fromEmail,
        @Value("${algoadda.app-url:http://localhost:3000}") String appUrl,
        ObjectMapper objectMapper
    ) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
        this.appUrl = appUrl;
        this.objectMapper = objectMapper;
    }

    /**
     * 1. Purchase Confirmation + License Ready Email (Combined)
     * Sent to buyer when an Order transitions to PAID status.
     */
    @Async
    public void sendPurchaseConfirmationAndLicenseEmail(Order order, List<OrderItem> items, List<License> licenses) {
        if (order == null || order.getBuyer() == null || order.getBuyer().getEmail() == null) {
            log.warn("Cannot send purchase confirmation email: missing order or buyer email");
            return;
        }

        String recipientEmail = order.getBuyer().getEmail();
        String subject = "AlgoAdda — Order Confirmation & License Ready (Order #" + order.getId() + ")";

        StringBuilder itemsListText = new StringBuilder();
        StringBuilder itemsListHtml = new StringBuilder();

        if (items != null && !items.isEmpty()) {
            for (OrderItem item : items) {
                String botName = item.getListing() != null && item.getListing().getBotVersion() != null
                    ? item.getListing().getBotVersion().getBot().getName()
                    : "Trading Algorithm";
                String ver = item.getBotVersion() != null ? item.getBotVersion().getVersionNumber() : "1.0.0";
                BigDecimal price = item.getPriceAtPurchase() != null ? item.getPriceAtPurchase() : BigDecimal.ZERO;

                itemsListText.append("- ").append(botName).append(" (v").append(ver).append(") - ₹").append(price).append("\n");
                itemsListHtml.append("<li><strong>").append(botName).append("</strong> (v").append(ver).append(") — ₹").append(price).append("</li>");
            }
        } else if (order.getListing() != null && order.getListing().getBotVersion() != null) {
            String botName = order.getListing().getBotVersion().getBot().getName();
            String ver = order.getListing().getBotVersion().getVersionNumber();
            BigDecimal price = order.getListing().getPrice();

            itemsListText.append("- ").append(botName).append(" (v").append(ver).append(") - ₹").append(price).append("\n");
            itemsListHtml.append("<li><strong>").append(botName).append("</strong> (v").append(ver).append(") — ₹").append(price).append("</li>");
        }

        String dashboardUrl = appUrl + "/buyer/dashboard";

        String textBody = "Hello,\n\n"
            + "Thank you for your purchase on AlgoAdda!\n\n"
            + "Your payment was processed successfully. Here are your order details:\n"
            + "Order ID: " + order.getId() + "\n"
            + "Items Purchased:\n" + itemsListText
            + "\nYour white-box strategy license(s) are active and ready for download.\n"
            + "Access your purchased algorithms on your Buyer Dashboard: " + dashboardUrl + "\n\n"
            + "Happy Trading,\nAlgoAdda Team";

        String htmlBody = "<html><body>"
            + "<h2>Thank you for your purchase on AlgoAdda!</h2>"
            + "<p>Your payment was processed successfully. Your order details are below:</p>"
            + "<p><strong>Order ID:</strong> " + order.getId() + "</p>"
            + "<h3>Purchased Strategy Algorithms:</h3>"
            + "<ul>" + itemsListHtml + "</ul>"
            + "<p>Your white-box strategy license(s) have been generated and activated.</p>"
            + "<p><a href=\"" + dashboardUrl + "\" style=\"display: inline-block; padding: 10px 20px; background-color: #5D7052; color: white; text-decoration: none; border-radius: 6px;\">Go to Buyer Dashboard</a></p>"
            + "<br/><p>Best regards,<br/>AlgoAdda Team</p>"
            + "</body></html>";

        sendEmail(recipientEmail, subject, textBody, htmlBody);
    }

    /**
     * 2. Compliance Status Changes Email
     * Sent to seller when a ComplianceCheck result changes to passed or failed.
     */
    @Async
    public void sendComplianceStatusUpdateEmail(ComplianceCheck check) {
        if (check == null || check.getBotVersion() == null || check.getBotVersion().getBot() == null
            || check.getBotVersion().getBot().getSeller() == null || check.getBotVersion().getBot().getSeller().getEmail() == null) {
            log.warn("Cannot send compliance status email: missing seller details");
            return;
        }

        String recipientEmail = check.getBotVersion().getBot().getSeller().getEmail();
        String botName = check.getBotVersion().getBot().getName();
        String versionNum = check.getBotVersion().getVersionNumber();
        boolean passed = check.isPassed();

        String statusStr = passed ? "PASSED" : "FAILED";
        String subject = "AlgoAdda — Compliance Gate " + statusStr + ": " + botName + " (v" + versionNum + ")";

        List<String> failedCheckNotes = extractFailedCheckDetails(check.getChecklistResults());

        StringBuilder failedText = new StringBuilder();
        StringBuilder failedHtml = new StringBuilder();
        if (!passed && !failedCheckNotes.isEmpty()) {
            failedText.append("\nSpecific Failed Requirements:\n");
            failedHtml.append("<h3>Failed Requirement Details:</h3><ul>");
            for (String note : failedCheckNotes) {
                failedText.append("- ").append(note).append("\n");
                failedHtml.append("<li>").append(note).append("</li>");
            }
            failedHtml.append("</ul>");
        }

        String textBody = "Hello Seller,\n\n"
            + "The compliance review for your bot version '" + botName + "' (v" + versionNum + ") has been processed.\n\n"
            + "Reviewer: " + check.getReviewerType() + "\n"
            + "Status: " + statusStr + "\n"
            + failedText
            + (passed
                ? "\nYour strategy version is verified compliant and eligible for publication on AlgoAdda."
                : "\nPlease update your strategy details to address the failed compliance rules before re-submitting.") + "\n\n"
            + "Regards,\nAlgoAdda Compliance Team";

        String htmlBody = "<html><body>"
            + "<h2>Compliance Gate Status: <span style=\"color: " + (passed ? "#5D7052" : "#A85448") + ";\">" + statusStr + "</span></h2>"
            + "<p>The compliance review for strategy <strong>" + botName + "</strong> (v" + versionNum + ") is complete.</p>"
            + "<p><strong>Reviewer Type:</strong> " + check.getReviewerType() + "</p>"
            + failedHtml
            + "<p>" + (passed
                ? "Your strategy version is verified compliant and ready for publication."
                : "Please review and fix the issues above to publish your strategy.") + "</p>"
            + "<br/><p>Regards,<br/>AlgoAdda Compliance Team</p>"
            + "</body></html>";

        sendEmail(recipientEmail, subject, textBody, htmlBody);
    }

    /**
     * 3. Order Status Update: FAILED
     * Sent to buyer on payment failure.
     */
    @Async
    public void sendOrderFailedEmail(Order order) {
        if (order == null || order.getBuyer() == null || order.getBuyer().getEmail() == null) {
            log.warn("Cannot send order failed email: missing buyer email");
            return;
        }

        String recipientEmail = order.getBuyer().getEmail();
        String subject = "AlgoAdda — Payment Failed (Order #" + order.getId() + ")";
        String marketplaceUrl = appUrl + "/marketplace";

        String textBody = "Hello,\n\n"
            + "Your payment for AlgoAdda Order #" + order.getId() + " could not be completed.\n\n"
            + "No funds were charged. You can retry your order by visiting the marketplace: " + marketplaceUrl + "\n\n"
            + "If you need assistance, please contact our support team.\n\n"
            + "Regards,\nAlgoAdda Team";

        String htmlBody = "<html><body>"
            + "<h2 style=\"color: #A85448;\">Payment Failed for Order #" + order.getId() + "</h2>"
            + "<p>We were unable to complete your payment for Order #" + order.getId() + ".</p>"
            + "<p>No charges were applied to your payment method. You can retry purchasing at any time.</p>"
            + "<p><a href=\"" + marketplaceUrl + "\" style=\"display: inline-block; padding: 10px 20px; background-color: #C18C5D; color: white; text-decoration: none; border-radius: 6px;\">Return to Marketplace</a></p>"
            + "<br/><p>Regards,<br/>AlgoAdda Team</p>"
            + "</body></html>";

        sendEmail(recipientEmail, subject, textBody, htmlBody);
    }

    /**
     * 4. Order Status Update: REFUNDED
     * Sent to buyer when an order is refunded.
     */
    @Async
    public void sendOrderRefundedEmail(Order order, BigDecimal refundAmount) {
        if (order == null || order.getBuyer() == null || order.getBuyer().getEmail() == null) {
            log.warn("Cannot send refund email: missing buyer email");
            return;
        }

        String recipientEmail = order.getBuyer().getEmail();
        String subject = "AlgoAdda — Refund Processed (Order #" + order.getId() + ")";

        String textBody = "Hello,\n\n"
            + "Your refund request for AlgoAdda Order #" + order.getId() + " has been processed.\n\n"
            + "Refund Amount: ₹" + (refundAmount != null ? refundAmount : "0.00") + "\n"
            + "Associated license(s) for this order have been revoked.\n\n"
            + "The refunded amount will reflect in your original payment account according to your bank's processing timeline.\n\n"
            + "Regards,\nAlgoAdda Support Team";

        String htmlBody = "<html><body>"
            + "<h2>Refund Processed for Order #" + order.getId() + "</h2>"
            + "<p>Your refund of <strong>₹" + (refundAmount != null ? refundAmount : "0.00") + "</strong> for Order #" + order.getId() + " has been issued successfully.</p>"
            + "<p>Please note that the access licenses associated with this purchase have been revoked.</p>"
            + "<br/><p>Regards,<br/>AlgoAdda Support Team</p>"
            + "</body></html>";

        sendEmail(recipientEmail, subject, textBody, htmlBody);
    }

    private void sendEmail(String toEmail, String subject, String textContent, String htmlContent) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                        .text(Content.builder().data(textContent).build())
                        .html(Content.builder().data(htmlContent).build())
                        .build())
                    .build())
                .build();

            sesClient.sendEmail(request);
            log.info("Successfully dispatched SES transactional email to {} with subject '{}'", toEmail, subject);
        } catch (Exception e) {
            // Log error clearly without throwing - non-blocking error handling requirement
            log.error("Failed to send SES email to {} [subject='{}']: {}", toEmail, subject, e.getMessage());
        }
    }

    private List<String> extractFailedCheckDetails(String jsonResults) {
        List<String> failedNotes = new ArrayList<>();
        if (jsonResults == null || jsonResults.isBlank()) {
            return failedNotes;
        }

        try {
            JsonNode root = objectMapper.readTree(jsonResults);
            root.fields().forEachRemaining(entry -> {
                JsonNode checkItem = entry.getValue();
                if (checkItem.isObject() && checkItem.has("passed") && !checkItem.get("passed").asBoolean()) {
                    String detail = checkItem.has("detail") ? checkItem.get("detail").asText() : entry.getKey() + " check failed";
                    failedNotes.add(detail);
                }
            });
        } catch (Exception e) {
            log.debug("Could not parse failed check details from JSON: {}", e.getMessage());
        }

        return failedNotes;
    }
}
