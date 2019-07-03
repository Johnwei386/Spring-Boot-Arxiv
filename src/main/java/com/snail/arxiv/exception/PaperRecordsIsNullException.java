package com.snail.arxiv.exception;

public class PaperRecordsIsNullException extends RuntimeException {

    public PaperRecordsIsNullException() {
        super("There are no one record existing in Database, Please insert some paper records");
    }
}
