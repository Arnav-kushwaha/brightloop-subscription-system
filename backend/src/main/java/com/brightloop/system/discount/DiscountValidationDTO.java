package com.brightloop.system.discount;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class DiscountValidationDTO {

    public static class Request {
        @NotBlank(message = "Coupon code is required")
        private String code;

        @NotNull(message = "Purchase amount is required")
        private BigDecimal purchaseAmount;

        private int quantity = 1;

        public Request() {
        }

        public Request(String code, BigDecimal purchaseAmount, int quantity) {
            this.code = code;
            this.purchaseAmount = purchaseAmount;
            this.quantity = quantity;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public BigDecimal getPurchaseAmount() {
            return purchaseAmount;
        }

        public void setPurchaseAmount(BigDecimal purchaseAmount) {
            this.purchaseAmount = purchaseAmount;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class Response {
        private boolean valid;
        private String message;
        private String code;
        private BigDecimal discountAmount;
        private BigDecimal finalAmount;

        public Response() {
        }

        public Response(boolean valid, String message, String code, BigDecimal discountAmount, BigDecimal finalAmount) {
            this.valid = valid;
            this.message = message;
            this.code = code;
            this.discountAmount = discountAmount;
            this.finalAmount = finalAmount;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
        }

        public BigDecimal getFinalAmount() {
            return finalAmount;
        }

        public void setFinalAmount(BigDecimal finalAmount) {
            this.finalAmount = finalAmount;
        }
    }
}
