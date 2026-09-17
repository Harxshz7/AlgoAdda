package com.algoadda.core.email;

import com.algoadda.core.bot.Bot;
import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.compliance.ComplianceCheck;
import com.algoadda.core.compliance.ReviewerType;
import com.algoadda.core.order.License;
import com.algoadda.core.order.Order;
import com.algoadda.core.order.OrderItem;
import com.algoadda.core.order.OrderStatus;
import com.algoadda.core.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EmailServiceTest {

    private SesClient sesClient;
    private EmailService emailService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sesClient = mock(SesClient.class);
        objectMapper = new ObjectMapper();
        emailService = new EmailService(sesClient, "noreply@algoadda.com", "http://localhost:3000", objectMapper);
    }

    @Test
    @DisplayName("Should send Purchase Confirmation and License Ready email for paid order")
    void testSendPurchaseConfirmationAndLicenseEmail() {
        User buyer = User.builder().id(UUID.randomUUID()).email("buyer@example.com").build();
        Order order = Order.builder().id(UUID.randomUUID()).buyer(buyer).status(OrderStatus.PAID).build();

        Bot bot = Bot.builder().id(UUID.randomUUID()).name("Alpha Scalper").build();
        BotVersion version = BotVersion.builder().id(UUID.randomUUID()).bot(bot).versionNumber("1.0.0").build();
        OrderItem item = OrderItem.builder().botVersion(version).priceAtPurchase(new BigDecimal("199.00")).build();

        emailService.sendPurchaseConfirmationAndLicenseEmail(order, List.of(item), List.of());

        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient, times(1)).sendEmail(requestCaptor.capture());

        SendEmailRequest request = requestCaptor.getValue();
        assertEquals("noreply@algoadda.com", request.source());
        assertEquals("buyer@example.com", request.destination().toAddresses().get(0));
        assertTrue(request.message().subject().data().contains("Order Confirmation"));
        assertTrue(request.message().body().text().data().contains("Alpha Scalper"));
    }

    @Test
    @DisplayName("Should send Compliance Status Update email to seller on compliance result")
    void testSendComplianceStatusUpdateEmail() {
        User seller = User.builder().id(UUID.randomUUID()).email("seller@example.com").build();
        Bot bot = Bot.builder().id(UUID.randomUUID()).name("Trend Rotator").seller(seller).build();
        BotVersion version = BotVersion.builder().id(UUID.randomUUID()).bot(bot).versionNumber("2.1.0").build();

        ComplianceCheck check = ComplianceCheck.builder()
            .botVersion(version)
            .reviewerType(ReviewerType.AUTO)
            .passed(false)
            .checklistResults("{\"noGuaranteedReturn\":{\"passed\":false,\"detail\":\"Prohibited phrase 'guaranteed' detected\"}}")
            .build();

        emailService.sendComplianceStatusUpdateEmail(check);

        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient, times(1)).sendEmail(requestCaptor.capture());

        SendEmailRequest request = requestCaptor.getValue();
        assertEquals("seller@example.com", request.destination().toAddresses().get(0));
        assertTrue(request.message().subject().data().contains("Compliance Gate FAILED"));
        assertTrue(request.message().body().text().data().contains("Prohibited phrase 'guaranteed' detected"));
    }

    @Test
    @DisplayName("Should send Order Failed email to buyer")
    void testSendOrderFailedEmail() {
        User buyer = User.builder().id(UUID.randomUUID()).email("buyer@example.com").build();
        Order order = Order.builder().id(UUID.randomUUID()).buyer(buyer).status(OrderStatus.FAILED).build();

        emailService.sendOrderFailedEmail(order);

        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient, times(1)).sendEmail(requestCaptor.capture());

        SendEmailRequest request = requestCaptor.getValue();
        assertEquals("buyer@example.com", request.destination().toAddresses().get(0));
        assertTrue(request.message().subject().data().contains("Payment Failed"));
    }

    @Test
    @DisplayName("Should send Order Refunded email to buyer")
    void testSendOrderRefundedEmail() {
        User buyer = User.builder().id(UUID.randomUUID()).email("buyer@example.com").build();
        Order order = Order.builder().id(UUID.randomUUID()).buyer(buyer).status(OrderStatus.REFUNDED).build();

        emailService.sendOrderRefundedEmail(order, new BigDecimal("499.00"));

        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient, times(1)).sendEmail(requestCaptor.capture());

        SendEmailRequest request = requestCaptor.getValue();
        assertEquals("buyer@example.com", request.destination().toAddresses().get(0));
        assertTrue(request.message().subject().data().contains("Refund Processed"));
        assertTrue(request.message().body().text().data().contains("499.00"));
    }

    @Test
    @DisplayName("Should gracefully handle SES exceptions without throwing (non-blocking requirement)")
    void testSesFailureDoesNotThrow() {
        doThrow(SesException.builder().message("SES Sandbox mode: Address not verified").build())
            .when(sesClient).sendEmail(any(SendEmailRequest.class));

        User buyer = User.builder().id(UUID.randomUUID()).email("unverified@example.com").build();
        Order order = Order.builder().id(UUID.randomUUID()).buyer(buyer).status(OrderStatus.PAID).build();

        assertDoesNotThrow(() -> emailService.sendPurchaseConfirmationAndLicenseEmail(order, List.of(), List.of()));
    }
}
