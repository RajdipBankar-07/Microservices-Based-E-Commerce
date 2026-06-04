package com.rajdip.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Core email-sending service.
 *
 * All sends are @Async so they never block the main request thread.
 * If app.email.enabled=false (default for local dev), emails are
 * logged but NOT sent — no SMTP credentials needed.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.email.sender-name:ShopEasy E-Commerce}")
    private String senderName;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── Core send method ───────────────────────────────────────────────────────

    /**
     * Sends an HTML email asynchronously.
     * Safe to call from any service — logs instead of sending when disabled.
     */
    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        if (!emailEnabled) {
            log.info("[EMAIL DISABLED] To: {} | Subject: {}", toEmail, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);
            log.info("[EMAIL SENT] To: {} | Subject: {}", toEmail, subject);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[EMAIL FAILED] To: {} | Subject: {} | Error: {}", toEmail, subject, e.getMessage());
        }
    }

    // ── Convenience wrappers (called by EmailTemplateService) ─────────────────

    public void sendWelcomeEmail(String toEmail, String userName) {
        String subject = "Welcome to " + senderName + "! 🎉";
        String body    = buildWelcomeHtml(userName, senderName);
        sendHtmlEmail(toEmail, subject, body);
    }

    public void sendOrderPlacedEmail(String toEmail, String userName,
                                     Long orderId, String productName,
                                     int quantity, double totalPrice) {
        String subject = "Order Confirmed #" + orderId + " 🛍️";
        String body    = buildOrderPlacedHtml(userName, orderId, productName, quantity, totalPrice, senderName);
        sendHtmlEmail(toEmail, subject, body);
    }

    public void sendOrderCancelledEmail(String toEmail, String userName,
                                        Long orderId, String productName) {
        String subject = "Order Cancelled #" + orderId;
        String body    = buildOrderCancelledHtml(userName, orderId, productName, senderName);
        sendHtmlEmail(toEmail, subject, body);
    }

    public void sendOrderRefundedEmail(String toEmail, String userName,
                                       Long orderId, String productName, double amount) {
        String subject = "Refund Processed #" + orderId + " 💰";
        String body    = buildOrderRefundedHtml(userName, orderId, productName, amount, senderName);
        sendHtmlEmail(toEmail, subject, body);
    }

    public void sendPaymentSuccessEmail(String toEmail, String userName,
                                        Long orderId, String txnId,
                                        String method, double amount) {
        String subject = "Payment Successful – ₹" + String.format("%.2f", amount) + " ✅";
        String body    = buildPaymentSuccessHtml(userName, orderId, txnId, method, amount, senderName);
        sendHtmlEmail(toEmail, subject, body);
    }

    public void sendPaymentFailedEmail(String toEmail, String userName,
                                       Long orderId, String txnId, String method) {
        String subject = "Payment Failed for Order #" + orderId + " ❌";
        String body    = buildPaymentFailedHtml(userName, orderId, txnId, method, senderName);
        sendHtmlEmail(toEmail, subject, body);
    }

    public void sendPaymentRefundedEmail(String toEmail, String userName,
                                         Long orderId, String txnId, double amount) {
        String subject = "Refund of ₹" + String.format("%.2f", amount) + " Initiated 💳";
        String body    = buildPaymentRefundedHtml(userName, orderId, txnId, amount, senderName);
        sendHtmlEmail(toEmail, subject, body);
    }

    // ── HTML Template builders ─────────────────────────────────────────────────

    private static String wrap(String title, String content, String brand) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8">
            <style>
              body { font-family: 'Segoe UI', Arial, sans-serif; background:#f4f6f8; margin:0; padding:0; }
              .container { max-width:600px; margin:32px auto; background:#ffffff;
                           border-radius:12px; overflow:hidden;
                           box-shadow:0 4px 20px rgba(0,0,0,0.08); }
              .header { background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);
                        padding:32px 24px; text-align:center; }
              .header h1 { color:#fff; margin:0; font-size:24px; letter-spacing:1px; }
              .header p  { color:rgba(255,255,255,0.85); margin:6px 0 0; font-size:14px; }
              .body { padding:32px 24px; color:#333; line-height:1.7; }
              .info-box { background:#f8f9ff; border-left:4px solid #667eea;
                          border-radius:6px; padding:16px 20px; margin:20px 0; }
              .info-box p { margin:4px 0; font-size:15px; }
              .info-box strong { color:#667eea; }
              .badge { display:inline-block; padding:6px 16px; border-radius:20px;
                       font-size:13px; font-weight:700; letter-spacing:0.5px; }
              .badge-success  { background:#d4edda; color:#155724; }
              .badge-danger   { background:#f8d7da; color:#721c24; }
              .badge-warning  { background:#fff3cd; color:#856404; }
              .badge-info     { background:#d1ecf1; color:#0c5460; }
              .footer { background:#f8f9ff; text-align:center; padding:16px;
                        font-size:12px; color:#999; border-top:1px solid #eee; }
              .btn { display:inline-block; background:linear-gradient(135deg,#667eea,#764ba2);
                     color:#fff; padding:12px 28px; border-radius:25px;
                     text-decoration:none; font-weight:700; margin:16px 0; }
            </style></head>
            <body>
            <div class="container">
              <div class="header">
                <h1>%s</h1>
                <p>%s</p>
              </div>
              <div class="body">%s</div>
              <div class="footer">© 2025 %s &nbsp;|&nbsp; All rights reserved.</div>
            </div>
            </body></html>
            """.formatted(brand, title, content, brand);
    }

    private static String buildWelcomeHtml(String name, String brand) {
        String content = """
            <p>Hi <strong>%s</strong> 👋,</p>
            <p>Welcome to <strong>%s</strong>! We're thrilled to have you on board.</p>
            <p>You can now browse products, place orders, track payments, and leave reviews.</p>
            <div class="info-box">
              <p>🛍️ Explore our latest products</p>
              <p>💳 Multiple payment methods supported</p>
              <p>⭐ Rate and review products you purchase</p>
            </div>
            <p>Happy shopping! 🎉</p>
            """.formatted(name, brand);
        return wrap("Welcome aboard!", content, brand);
    }

    private static String buildOrderPlacedHtml(String name, Long orderId, String product,
                                               int qty, double total, String brand) {
        String content = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Your order has been placed successfully! 🎊</p>
            <div class="info-box">
              <p><strong>Order ID:</strong> #%d</p>
              <p><strong>Product:</strong> %s</p>
              <p><strong>Quantity:</strong> %d</p>
              <p><strong>Total:</strong> ₹%.2f</p>
              <p><strong>Status:</strong> <span class="badge badge-success">PLACED</span></p>
            </div>
            <p>Please complete your payment to confirm the order.</p>
            """.formatted(name, orderId, product, qty, total);
        return wrap("Order Confirmed! 🛍️", content, brand);
    }

    private static String buildOrderCancelledHtml(String name, Long orderId,
                                                  String product, String brand) {
        String content = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Your order has been <strong>cancelled</strong>.</p>
            <div class="info-box">
              <p><strong>Order ID:</strong> #%d</p>
              <p><strong>Product:</strong> %s</p>
              <p><strong>Status:</strong> <span class="badge badge-danger">CANCELLED</span></p>
            </div>
            <p>Stock has been restored. Feel free to place a new order anytime.</p>
            """.formatted(name, orderId, product);
        return wrap("Order Cancelled", content, brand);
    }

    private static String buildOrderRefundedHtml(String name, Long orderId,
                                                 String product, double amount, String brand) {
        String content = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Your refund has been processed successfully! 🎉</p>
            <div class="info-box">
              <p><strong>Order ID:</strong> #%d</p>
              <p><strong>Product:</strong> %s</p>
              <p><strong>Refund Amount:</strong> ₹%.2f</p>
              <p><strong>Status:</strong> <span class="badge badge-warning">REFUNDED</span></p>
            </div>
            <p>The refund will reflect in your account within 5–7 business days.</p>
            """.formatted(name, orderId, product, amount);
        return wrap("Refund Processed 💰", content, brand);
    }

    private static String buildPaymentSuccessHtml(String name, Long orderId, String txnId,
                                                  String method, double amount, String brand) {
        String content = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Your payment was successful! ✅</p>
            <div class="info-box">
              <p><strong>Order ID:</strong> #%d</p>
              <p><strong>Transaction ID:</strong> %s</p>
              <p><strong>Payment Method:</strong> %s</p>
              <p><strong>Amount Paid:</strong> ₹%.2f</p>
              <p><strong>Status:</strong> <span class="badge badge-success">SUCCESS</span></p>
            </div>
            <p>Thank you for your purchase! 🛒</p>
            """.formatted(name, orderId, txnId, method, amount);
        return wrap("Payment Successful ✅", content, brand);
    }

    private static String buildPaymentFailedHtml(String name, Long orderId,
                                                 String txnId, String method, String brand) {
        String content = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Unfortunately, your payment could <strong>not</strong> be processed.</p>
            <div class="info-box">
              <p><strong>Order ID:</strong> #%d</p>
              <p><strong>Transaction ID:</strong> %s</p>
              <p><strong>Payment Method:</strong> %s</p>
              <p><strong>Status:</strong> <span class="badge badge-danger">FAILED</span></p>
            </div>
            <p>Please try again with a different payment method or contact support.</p>
            """.formatted(name, orderId, txnId, method);
        return wrap("Payment Failed ❌", content, brand);
    }

    private static String buildPaymentRefundedHtml(String name, Long orderId,
                                                   String txnId, double amount, String brand) {
        String content = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Your payment refund has been initiated.</p>
            <div class="info-box">
              <p><strong>Order ID:</strong> #%d</p>
              <p><strong>Transaction ID:</strong> %s</p>
              <p><strong>Refund Amount:</strong> ₹%.2f</p>
              <p><strong>Status:</strong> <span class="badge badge-warning">REFUNDED</span></p>
            </div>
            <p>Refund will reflect in your account within 5–7 business days.</p>
            """.formatted(name, orderId, txnId, amount);
        return wrap("Payment Refund Initiated 💳", content, brand);
    }
}
