package dev.mantic.hooks.discord;

/**
 * How a leaderboard embed looks. Text and URL fields may use {top_player} and {top_uuid}; empty fields are left out.
 */
public final class EmbedStyle {

    private final String webhookName;
    private final String webhookAvatar;
    private final String title;
    private final int colour;
    private final String footer;
    private final String footerIcon;
    private final String thumbnail;
    private final String image;
    private final boolean timestamp;

    public EmbedStyle(String webhookName, String webhookAvatar, String title, int colour, String footer, String footerIcon,
                      String thumbnail, String image, boolean timestamp) {
        this.webhookName = webhookName;
        this.webhookAvatar = webhookAvatar;
        this.title = title;
        this.colour = colour;
        this.footer = footer;
        this.footerIcon = footerIcon;
        this.thumbnail = thumbnail;
        this.image = image;
        this.timestamp = timestamp;
    }

    public String getWebhookName() {
        return webhookName;
    }

    public String getWebhookAvatar() {
        return webhookAvatar;
    }

    public String getTitle() {
        return title;
    }

    public int getColour() {
        return colour;
    }

    public String getFooter() {
        return footer;
    }

    public String getFooterIcon() {
        return footerIcon;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getImage() {
        return image;
    }

    public boolean isTimestamp() {
        return timestamp;
    }
}
