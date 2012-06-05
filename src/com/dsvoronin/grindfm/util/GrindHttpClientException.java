package com.dsvoronin.grindfm.util;

import java.io.IOException;

/**
 * User: dsvoronin
 * Date: 30.05.12
 * Time: 0:27
 * Кастомный эксепшн для Toast сообщений клиенту с подробным описанием ошибки в HTTP клиенте
 */
public class GrindHttpClientException extends IOException {

    public GrindHttpClientException(String detailMessage) {
        super(detailMessage);
    }

    public GrindHttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
