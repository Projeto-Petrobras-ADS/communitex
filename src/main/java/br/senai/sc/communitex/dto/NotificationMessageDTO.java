package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.NotificationChannel;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload published to the RabbitMQ notifications exchange.
 * Field names match exactly what the notification microservice listener expects.
 *
 * @param notificationType delivery channel (EMAIL, PUSH, SMS, WEBHOOK, IN_APP)
 * @param recipient        target address — email, push token, phone, etc.
 * @param subject          short title of the notification
 * @param body             full notification text
 * @param template         optional template name the consumer should use to render the message
 * @param data             dynamic data to be injected into the template
 * @param priority         message priority — NORMAL (default) or HIGH
 * @param retryCount       number of delivery attempts already made (starts at 0)
 * @param metadata         extra key/value pairs for routing, deep-linking, etc.
 */
public record NotificationMessageDTO(
        @JsonProperty("notification_type") NotificationChannel notificationType,
        @JsonProperty("recipient")         String recipient,
        @JsonProperty("subject")           String subject,
        @JsonProperty("body")              String body,
        @JsonProperty("template")          String template,
        @JsonProperty("data")              Object data,
        @JsonProperty("priority")          String priority,
        @JsonProperty("retry_count")       Integer retryCount,
        @JsonProperty("metadata")          Object metadata
) {
    /** Convenience factory — sets opinionated defaults for priority and retryCount. */
    public static NotificationMessageDTO of(NotificationChannel notificationType,
                                            String recipient,
                                            String subject,
                                            String body,
                                            String template,
                                            Object data,
                                            Object metadata) {
        return new NotificationMessageDTO(
                notificationType,
                recipient,
                subject,
                body,
                template,
                data,
                "NORMAL",
                0,
                metadata
        );
    }
}
