package com.farm.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Email error: " + e.getMessage());
        }
    }

    // Harvest approaching alert
    public void sendHarvestAlert(String toEmail, String farmerName,
                                 String cropName, String harvestDate, String location) {
        String subject = "🌾 Harvest Alert — " + cropName + " is ready soon!";
        String body = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
                <div style="background:#1A5C2A;padding:30px;text-align:center;border-radius:12px 12px 0 0;">
                    <h1 style="color:white;margin:0;">🌾 Farm Management System</h1>
                    <p style="color:#90EE90;margin:8px 0 0;">Harvest Alert Notification</p>
                </div>
                <div style="background:#f9f9f9;padding:30px;border-radius:0 0 12px 12px;">
                    <p style="font-size:16px;">Dear <strong>{farmerName}</strong>,</p>
                    <p>Your crop is approaching its expected harvest date!</p>
                    <div style="background:#E8F5EE;border-left:4px solid #1A5C2A;
                         padding:16px;border-radius:0 8px 8px 0;margin:20px 0;">
                        <p style="margin:0;font-size:18px;font-weight:bold;
                           color:#1A5C2A;">🌱 {cropName}
                        </p>
                        <p style="margin:8px 0 0;color:#555;">
                            📅 Expected Harvest: <strong>{harvestDate}</strong><br>
                            📍 Location: <strong>{location}</strong>
                        </p>
                    </div>
                    <p style="color:#888;font-size:13px;">
                        This is an automated alert from your Farm Management System.
                        Log in to record your harvest.
                    </p>
                    <div style="text-align:center;margin-top:24px;">
                        <a href="http://localhost:8080/harvest/add"
                           style="background:#1A5C2A;color:white;padding:12px 28px;
                                  border-radius:8px;text-decoration:none;font-weight:bold;">
                            Record Harvest Now
                        </a>
                    </div>
                </div>
            </div>
            """
                .replace("{farmerName}", farmerName)
                .replace("{cropName}", cropName)
                .replace("{harvestDate}", harvestDate)
                .replace("{location}", location);

        sendEmail(toEmail, subject, body);
    }

    // Low stock alert
    public void sendLowStockAlert(String toEmail, String farmerName,
                                  String itemName, String quantity, String unit) {
        String subject = "⚠️ Low Stock Alert — " + itemName;
        String body = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
                <div style="background:#E65100;padding:30px;text-align:center;border-radius:12px 12px 0 0;">
                    <h1 style="color:white;margin:0;">⚠️ Farm Management System</h1>
                    <p style="color:#FFE0B2;margin:8px 0 0;">Low Stock Alert</p>
                </div>
                <div style="background:#f9f9f9;padding:30px;border-radius:0 0 12px 12px;">
                    <p style="font-size:16px;">Dear <strong>{farmerName}</strong>,</p>
                    <p>Your inventory is running low and needs restocking:</p>
                    <div style="background:#FFF3E0;border-left:4px solid #E65100;
                         padding:16px;border-radius:0 8px 8px 0;margin:20px 0;">
                        <p style="margin:0;font-size:18px;font-weight:bold;
                           color:#E65100;">📦 {itemName}
                        </p>
                        <p style="margin:8px 0 0;color:#555;">
                            Remaining: <strong>{quantity} {unit}</strong>
                        </p>
                    </div>
                    <p style="color:#888;font-size:13px;">
                        Please restock soon to avoid farming disruptions.
                    </p>
                </div>
            </div>
            """
                .replace("{farmerName}", farmerName)
                .replace("{itemName}", itemName)
                .replace("{quantity}", quantity)
                .replace("{unit}", unit);

        sendEmail(toEmail, subject, body);
    }

    // Welcome email on registration
    public void sendWelcomeEmail(String toEmail, String farmerName) {
        String subject = "🌱 Welcome to Farm Management System!";
        String body = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
                <div style="background:#1A5C2A;padding:30px;text-align:center;border-radius:12px 12px 0 0;">
                    <h1 style="color:white;margin:0;">🌱 Farm Management System</h1>
                    <p style="color:#90EE90;margin:8px 0 0;">Tanzania</p>
                </div>
                <div style="background:#f9f9f9;padding:30px;border-radius:0 0 12px 12px;">
                    <h2 style="color:#1A5C2A;">Welcome, {farmerName}!</h2>
                    <p>Your account has been created successfully.</p>
                    <p>You can now:</p>
                    <ul>
                        <li>✅ Manage your crops (30+ Tanzania crops)</li>
                        <li>✅ Track inventory and equipment</li>
                        <li>✅ Record harvest data</li>
                        <li>✅ Track sales and expenses</li>
                        <li>✅ View live Tanzania weather</li>
                        <li>✅ Manage multiple farm locations</li>
                    </ul>
                    <div style="text-align:center;margin-top:24px;">
                        <a href="http://localhost:8080/dashboard"
                           style="background:#1A5C2A;color:white;padding:12px 28px;
                                  border-radius:8px;text-decoration:none;font-weight:bold;">
                            Go to Dashboard
                        </a>
                    </div>
                </div>
            </div>
            """
                .replace("{farmerName}", farmerName);

        sendEmail(toEmail, subject, body);
    }
}