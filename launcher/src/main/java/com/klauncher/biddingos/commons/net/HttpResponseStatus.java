package com.klauncher.biddingos.commons.net;

public class HttpResponseStatus {

    public static Code getStatus(int nCode) {
        switch (nCode) {
            case 100:
                return Code.CONTINUE;
            case 101:
                return Code.SWITCHING_PROTOCOLS;
            case 102:
                return Code.PROCESSING;
            case 200:
                return Code.OK;
            case 201:
                return Code.CREATED;
            case 202:
                return Code.ACCEPTED;
            case 203:
                return Code.NON_AUTHORITATIVE_INFORMATION;
            case 204:
                return Code.NO_CONTENT;
            case 205:
                return Code.RESET_CONTENT;
            case 206:
                return Code.PARTIAL_CONTENT;
            case 207:
                return Code.MULTI_STATUS;
            case 300:
                return Code.MULTIPLE_CHOICES;
            case 301:
                return Code.MOVED_PERMANENTLY;
            case 302:
                return Code.FOUND;
            case 303:
                return Code.SEE_OTHER;
            case 304:
                return Code.NOT_MODIFIED;
            case 305:
                return Code.USE_PROXY;
            case 307:
                return Code.TEMPORARY_REDIRECT;
            case 400:
                return Code.BAD_REQUEST;
            case 401:
                return Code.UNAUTHORIZED;
            case 402:
                return Code.PAYMENT_REQUIRED;
            case 403:
                return Code.FORBIDDEN;
            case 404:
                return Code.NOT_FOUND;
            case 405:
                return Code.METHOD_NOT_ALLOWED;
            case 406:
                return Code.NOT_ACCEPTABLE;
            case 407:
                return Code.PROXY_AUTHENTICATION_REQUIRED;
            case 408:
                return Code.REQUEST_TIMEOUT;
            case 409:
                return Code.CONFLICT;
            case 410:
                return Code.GONE;
            case 411:
                return Code.LENGTH_REQUIRED;
            case 412:
                return Code.PRECONDITION_FAILED;
            case 413:
                return Code.REQUEST_ENTITY_TOO_LARGE;
            case 414:
                return Code.REQUEST_URI_TOO_LONG;
            case 415:
                return Code.UNSUPPORTED_MEDIA_TYPE;
            case 416:
                return Code.REQUESTED_RANGE_NOT_SATISFIABLE;
            case 417:
                return Code.EXPECTATION_FAILED;
            case 421:
                return Code.MISDIRECTED_REQUEST;
            case 422:
                return Code.UNPROCESSABLE_ENTITY;
            case 423:
                return Code.LOCKED;
            case 424:
                return Code.FAILED_DEPENDENCY;
            case 425:
                return Code.UNORDERED_COLLECTION;
            case 426:
                return Code.UPGRADE_REQUIRED;
            case 428:
                return Code.PRECONDITION_REQUIRED;
            case 429:
                return Code.TOO_MANY_REQUESTS;
            case 431:
                return Code.REQUEST_HEADER_FIELDS_TOO_LARGE;
            case 500:
                return Code.INTERNAL_SERVER_ERROR;
            case 501:
                return Code.NOT_IMPLEMENTED;
            case 502:
                return Code.BAD_GATEWAY;
            case 503:
                return Code.SERVICE_UNAVAILABLE;
            case 504:
                return Code.GATEWAY_TIMEOUT;
            case 505:
                return Code.HTTP_VERSION_NOT_SUPPORTED;
            case 506:
                return Code.VARIANT_ALSO_NEGOTIATES;
            case 507:
                return Code.INSUFFICIENT_STORAGE;
            case 510:
                return Code.NOT_EXTENDED;
            case 511:
                return Code.NETWORK_AUTHENTICATION_REQUIRED;
        }
        return Code.NONE;
    }

    public static enum Code {
        CONTINUE,
        SWITCHING_PROTOCOLS,
        PROCESSING,
        OK,
        CREATED,
        ACCEPTED,
        NON_AUTHORITATIVE_INFORMATION,
        NO_CONTENT,
        RESET_CONTENT,
        PARTIAL_CONTENT,
        MULTI_STATUS,
        MULTIPLE_CHOICES,
        MOVED_PERMANENTLY,
        FOUND,
        SEE_OTHER,
        NOT_MODIFIED,
        USE_PROXY,
        TEMPORARY_REDIRECT,
        BAD_REQUEST,
        UNAUTHORIZED,
        PAYMENT_REQUIRED,
        FORBIDDEN,
        NOT_FOUND,
        METHOD_NOT_ALLOWED,
        NOT_ACCEPTABLE,
        PROXY_AUTHENTICATION_REQUIRED,
        REQUEST_TIMEOUT,
        CONFLICT,
        GONE,
        LENGTH_REQUIRED,
        PRECONDITION_FAILED,
        REQUEST_ENTITY_TOO_LARGE,
        REQUEST_URI_TOO_LONG,
        UNSUPPORTED_MEDIA_TYPE,
        REQUESTED_RANGE_NOT_SATISFIABLE,
        EXPECTATION_FAILED,
        MISDIRECTED_REQUEST,
        UNPROCESSABLE_ENTITY,
        LOCKED,
        FAILED_DEPENDENCY,
        UNORDERED_COLLECTION,
        UPGRADE_REQUIRED,
        PRECONDITION_REQUIRED,
        TOO_MANY_REQUESTS,
        REQUEST_HEADER_FIELDS_TOO_LARGE,
        INTERNAL_SERVER_ERROR,
        NOT_IMPLEMENTED,
        BAD_GATEWAY,
        SERVICE_UNAVAILABLE,
        GATEWAY_TIMEOUT,
        HTTP_VERSION_NOT_SUPPORTED,
        VARIANT_ALSO_NEGOTIATES,
        INSUFFICIENT_STORAGE,
        NOT_EXTENDED,
        NETWORK_AUTHENTICATION_REQUIRED,
        NONE
        ;
    }
}
