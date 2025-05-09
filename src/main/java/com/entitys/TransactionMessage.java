package com.entitys;
public class TransactionMessage {

    private String message;  // Dữ liệu giao dịch, có thể là JSON hoặc chuỗi tùy thuộc vào định dạng

    public TransactionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
