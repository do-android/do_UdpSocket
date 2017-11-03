package doext.module.do_UdpSocket.implement;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import core.helper.DoIOHelper;
import core.interfaces.DoIScriptEngine;


/**
 * Created by feng_ on 2017/4/12.
 */

public class UDPClient implements Runnable {
    int localPort = 8888;
    private static DatagramSocket socket = null;
    private static DatagramPacket packetRcv;
    private byte[] msgRcv = new byte[1024]; // 接收消息
    private boolean flag = true;

    public UDPClient(int clientPort) {
        super();
        localPort = clientPort;
    }

    // 发送消息
    public void send(String _type, String _content, String hostIp, int hostPort, DoIScriptEngine _scriptEngine) throws Exception {
        InetAddress inetAddress = InetAddress.getByName(hostIp);
        byte[] _sendByte;
        if (_type.equalsIgnoreCase("HEX")) {// 发送十六进制数
            _sendByte = hexStr2Byte(_content);
        } else if (_type.equalsIgnoreCase("File")) {// 发送文件
            String path = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentPage().getCurrentApp(), _content);
            _sendByte = DoIOHelper.readAllBytes(path);
        } else {
            _sendByte = _content.getBytes();
        }
        DatagramPacket datagramPacket = new DatagramPacket(_sendByte, _sendByte.length, inetAddress, hostPort);
        socket.send(datagramPacket);
    }

    private OnReceiveListener listener;

    public interface OnReceiveListener {
        void fireReceiveEvent(String msg);
    }

    public void setReceiveListener(OnReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(localPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while (flag) {
            try {
                packetRcv = new DatagramPacket(msgRcv, msgRcv.length);
                socket.receive(packetRcv);
                byte[] data = packetRcv.getData();// 接收的数据
                String _result = bytesToHexString(data, packetRcv.getLength());
                if (listener != null) {
                    listener.fireReceiveEvent(_result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String bytesToHexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return "";
        }
        for (int i = 0; i < len; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] hexStr2Byte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    public static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public void doDispose() {
        if (socket != null) {
            if (socket.isConnected()) {
                socket.disconnect();
            }
            if (socket.isClosed() == false) {
                socket.close();
            }
            flag = false;
            socket = null;
        }
    }
}