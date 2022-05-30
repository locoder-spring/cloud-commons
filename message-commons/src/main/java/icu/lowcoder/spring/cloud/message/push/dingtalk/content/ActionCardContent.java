package icu.lowcoder.spring.cloud.message.push.dingtalk.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

@Getter
@Setter
public class ActionCardContent implements Content {
    private String msgtype = "actionCard";
    private ActionCard actionCard;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ActionCard {
        private String title;
        private String text;
        private String singleTitle;
        private String singleURL;
        private String btnOrientation;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private ActionCard card;

        public Builder() {
            card = new ActionCard();
        }

        public Builder text(String text) {
            this.card.setText(text);
            return this;
        }

        public Builder title(String title) {
            this.card.setTitle(title);
            return this;
        }
        public Builder singleTitle(String singleTitle) {
            this.card.setSingleTitle(singleTitle);
            return this;
        }
        public Builder singleURL(String singleURL) {
            this.card.setSingleURL(singleURL);
            return this;
        }
        public Builder btnVertical() {
            this.card.setBtnOrientation("0");
            return this;
        }
        public Builder btnHorizontal() {
            this.card.setBtnOrientation("1");
            return this;
        }

        public ActionCardContent build() {
            ActionCardContent content = new ActionCardContent();
            content.setActionCard(card);
            return content;
        }
    }
}
