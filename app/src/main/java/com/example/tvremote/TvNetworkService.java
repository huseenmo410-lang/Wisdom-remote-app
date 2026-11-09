package com.example.tvremote;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.Base64;

public class TvNetworkService {
    private static final String TAG = "TvNetworkService";
    
    static {
        System.loadLibrary("tvremote"); // Load C++ compiled shared library
    }

    // Native C++ Declaration
    public native boolean sendLegacyCommandNative(String tvIp, int port, byte[] payload);

    private WebSocketClient webSocketClient;

    // Modern WebSocket Protocol for Tizen / Smart Cloud TVs (Port 8001 / 8002)
    public void sendWebSocketCommand(String ipAddress, String keyCmd) {
        try {
            String appNameBase64 = Base64.getEncoder().encodeToString("SamsungRemoteApp".getBytes());
            String wsUrl = "ws://" + ipAddress + ":8001/api/v2/channels/samsung.remote.control?name=" + appNameBase64;
            
            if (webSocketClient != null && webSocketClient.isOpen()) {
                sendKeyPayload(keyCmd);
                return;
            }

            webSocketClient = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.i(TAG, "Connected to TV WebSocket");
                    sendKeyPayload(keyCmd);
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Message from TV: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.w(TAG, "WebSocket Closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket Error", ex);
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize WebSocket", e);
        }
    }

    private void sendKeyPayload(String keyCmd) {
        String jsonPayload = "{"
                + "\"method\":\"ms.remote.control\","
                + "\"params\":{"
                + "\"Cmd\":\"Click\","
                + "\"DataOfCmd\":\"" + keyCmd + "\","
                + "\"Option\":\"false\","
                + "\"TypeOfRemote\":\"SendRemoteKey\""
                + "}}";
        webSocketClient.send(jsonPayload);
    }
          }

