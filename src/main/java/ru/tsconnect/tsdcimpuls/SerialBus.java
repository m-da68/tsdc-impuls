package ru.tsconnect.tsdcimpuls;

import com.install4j.api.windows.RegistryRoot;
import jssc.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.install4j.api.windows.WinRegistry;

public class SerialBus {
    private String vid;
    private String pid;
    private String device_id;
    private String com;
    private String registryKey;
    private static SerialPort serialPort;
    private static final RegistryRoot hkey = RegistryRoot.HKEY_LOCAL_MACHINE;

    public SerialBus(String vid, String pid, String device_id) {
        this.vid = vid;
        this.pid = pid;
        this.device_id = device_id;
        this.registryKey = String.format("SYSTEM\\CurrentControlSet\\Enum\\USB\\VID_%s&PID_%s\\%s", this.vid, this.pid, this.device_id);
    }
    public SerialBus(String vid, String pid) {
        this.vid = vid;
        this.pid = pid;
        this.registryKey = String.format("SYSTEM\\CurrentControlSet\\Enum\\USB\\VID_%s&PID_%s", this.vid, this.pid);
    }

    public SerialBus(String com) {
        this.com = com;
    }

    public boolean isSet() {
        if (this.com == null) {
            try {
                if (this.device_id == null) {
                    return WinRegistry.getSubKeyNames(hkey, this.registryKey).length != 0;
                } else {
                    return WinRegistry.getValueNames(hkey, this.registryKey).length != 0;
                }
            } catch (Exception e) {
                return false;
            }
        } else return false;
    }

    public String[] getDevices() {
        if (this.device_id == null) {
            return WinRegistry.getSubKeyNames(hkey, this.registryKey);
        }
        return new String[0];
    }

    public void setDevice(String device_id) {
        this.device_id = device_id;
    }

    public void rebuildRegistryKey() {
        this.registryKey = String.format("SYSTEM\\CurrentControlSet\\Enum\\USB\\VID_%s&PID_%s\\%s", this.vid, this.pid, this.device_id);
    }

    public void set(String vid, String pid, String device_id) {
        this.vid = vid;
        this.pid = pid;
        this.device_id = device_id;
        this.registryKey = String.format("SYSTEM\\CurrentControlSet\\Enum\\USB\\VID_%s&PID_%s\\%s", vid, pid, device_id);
    }

    public void set(String vid, String pid) {
        this.vid = vid;
        this.pid = pid;
        this.device_id = null;
        this.registryKey = String.format("SYSTEM\\CurrentControlSet\\Enum\\USB\\VID_%s&PID_%s", vid, pid);
    }

    public void set(String com) {
        this.com = com;
        this.vid = null;
        this.pid = null;
        this.device_id = null;
        this.registryKey = null;
    }

    public Object getFriendlyName() {
        if (this.registryKey == null || this.registryKey.isEmpty()) {
            System.out.println("'registryKey' null or empty");
        }
        try {
            return WinRegistry.getValue(hkey, this.registryKey, "FriendlyName");
        } catch (Exception ex) { // catch-all:
            // readString() throws IllegalArg, IllegalAccess, InvocationTarget
            System.err.println(ex.getMessage());
            return null;
        }
    }

    public void setFriendlyName(String value) {
        System.out.println(WinRegistry.setValue(hkey, this.registryKey, "FriendlyName", value));
    }

    public String[] getPortList() {
        return SerialPortList.getPortNames();
    }

    public String getCom() {
        return "COM" + getComNumber();
    }

    public String getCom(String friendlyName) {
        return "COM" + getComNumber(friendlyName);
    }

    public int getComNumber() {
        String friendlyName = (String) getFriendlyName();
        return getComNumber(friendlyName);
    }

    public int getComNumber(String friendlyName) {
        System.out.println(friendlyName);
        if (friendlyName != null && friendlyName.contains("COM")) {
            String substr = friendlyName.substring(friendlyName.indexOf("COM"));
            Matcher matchInt = Pattern.compile("\\d+").matcher(substr);
            if (matchInt.find()) {
                return Integer.parseInt(matchInt.group());
            }
        } else System.out.println(1);
        return -1;
    }

    public boolean open(String port) {
        serialPort = new SerialPort(port);
        try {
            serialPort.openPort();

            return true;
        } catch (SerialPortException e) {
            return false;
//            throw new RuntimeException(e);
        }
    }

    public void close() throws SerialPortException {
        serialPort.closePort();
    }

    public void write(String data) throws SerialPortException {
        serialPort.writeString(data+"\r");
    }

    public void sendCMD(String data) throws SerialPortException {
        write(String.format("CMD:%s;", data));
    }

    public void sendSND(String data) throws SerialPortException {
        write(String.format("SND:%s;", data));
    }

    public void addEventListener(SerialPortEventListener listener, int mask) throws SerialPortException {
        serialPort.addEventListener(listener, mask);
    }

    public void removeEventListener() throws SerialPortException {
        serialPort.removeEventListener();
    }

    public void settings(int baudrate, int databits, int stopbits, int parity) throws SerialPortException {
        serialPort.setParams(baudrate, databits, stopbits, parity);
    }

    public boolean isOpened() {
        return serialPort.isOpened();
    }

    public void setHardwareMode(boolean value) throws SerialPortException {
        if (value) serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
        else serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
    }

    public String readString(int event) throws SerialPortException {
        return serialPort.readString(event);
    }
}