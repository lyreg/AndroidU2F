package org.esec.mcg.androidu2f.msg;

/**
 * Created by yz on 2016/3/10.
 */
public enum ErrorCode {
    OK("OK"),
    OTHER_ERROR("Other Error"),
    BAD_REQUEST("Bad Request"),
    CONFIGURATION_UNSUPPORTED("Configuration Unsupported"),
    DEVICE_INELIGIBLE("Device Ineligible"),
    TIMEOUT("Timeout");

    private String _name;

    ErrorCode(String name) {
        _name = name;
    }

    @Override
    public String toString() {
        return _name;
    }
}
