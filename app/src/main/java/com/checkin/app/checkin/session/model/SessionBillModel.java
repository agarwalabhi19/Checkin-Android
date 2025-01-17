package com.checkin.app.checkin.session.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionBillModel {
    @JsonProperty("subtotal")
    private Double subtotal;

    @JsonProperty("tax")
    private Double tax;

    @JsonProperty("tip")
    private Double tip;

    @JsonProperty("discount")
    private Double discount;

    @JsonProperty("offers")
    private Double offers;

    @JsonProperty("total")
    private Double total;

    @JsonProperty("promo")
    private String promo;

    private Double discountPercentage;

    public SessionBillModel() {
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public Double getTax() {
        return tax;
    }

    public Double getTip() {
        return tip;
    }

    public Double getDiscount() {
        return discount;
    }

    @JsonProperty("discount")
    public void setDiscount(Double discount) {
        this.discount = (discount > 0) ? discount : null;
    }

    public Double getOffers() {
        return offers;
    }

    @JsonProperty("offers")
    public void setOffers(@Nullable Double offers) {
        this.offers = (offers != null && offers > 0) ? offers : null;
    }

    public String formatSubTotal() {
        return String.valueOf(subtotal);
    }

    public String formatTax() {
        return String.valueOf(tax);
    }

    public String formatTip() {
        return String.valueOf(tip);
    }

    public String formatDiscount() {
        return String.valueOf(discount);
    }

    public String formatTotal() {
        return String.valueOf(total);
    }

    public Double getTotal() {
        return total;
    }

    public String getPromo() {
        return "Promo - (" + promo + ")";
    }

    public void setPromo(String promo) {
        this.promo = promo;
    }

    public void calculateDiscount(Double value, boolean isINR) {
        if (!isINR) {
            discountPercentage = value;
            discount = (subtotal * value) / 100;
            total -= subtotal - discount;
        } else {
            discount = value;
            total = subtotal - value;
        }
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public void giveTip(double tip) {
        this.tip = tip;
        calculateTotal();
    }

    public double getTotalSaving() {
        double savings = 0;
        if (discount != null)
            savings += discount;
        if (offers != null)
            savings += offers;
        return savings;
    }

    private void calculateTotal() {
        this.total = 0d;
        if (this.subtotal != null)
            this.total += this.subtotal;
        if (this.tip != null)
            this.total += this.tip;
        if (this.tax != null)
            this.total += this.tax;
        if (this.discount != null)
            this.total -= this.discount;
        if (this.offers != null)
            this.total -= this.offers;
        this.total = (double) Math.round(this.total);
    }
}
