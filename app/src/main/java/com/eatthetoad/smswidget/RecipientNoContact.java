package com.eatthetoad.smswidget;

public class RecipientNoContact extends Recipient {
    public RecipientNoContact(String name, String nameType, String number) {
        super(name, nameType, number);
    }

    public String getHeader() {
        String number = getNumber();
        if (number.equals("0")) {
            return "Error retrieving number";
        } else {
            if (number.matches(".*[a-zA-Z].*")) {
                return number;
            } else {
                return SMSDataProvider.formatNumber(number);
            }
        }
    }
}
