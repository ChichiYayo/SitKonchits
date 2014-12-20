package server.network.Konchits;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.network.packets.Konchits.BaseWritePacketKonchits;
import server.network.packets.threading.Konchits.ThreadPoolManagerKonchits;

public class ServerKonchits {

    private ServerSocket providerSocket;
    private Thread mThread = null;

    private List<SocketProcessorKonchits> clients;

    public ServerKonchits() {
        clients = new ArrayList<>();
        ThreadPoolManagerKonchits.init();
    }

    public boolean startServer(int port) {
        try {
            providerSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(ServerKonchits.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        _start();

        return true;
    }

    private void _start() {
        System.out.println("_start");
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        Socket s = providerSocket.accept();
                        System.out.println("Client accepted address = " + s.getInetAddress());
                        SocketProcessorKonchits client = new SocketProcessorKonchits(s);
                        clients.add(client);
                        new Thread(client).start();
                    } catch (IOException ex) {
                        //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("STOP");
                try {
                    providerSocket.close();
                } catch (IOException ex) {
                   //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        mThread.start();
    }

    public void stop() {
        if (mThread != null) {
            mThread.interrupt();
        }
        try {
            providerSocket.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public void sendToAllClients(BaseWritePacketKonchits packet) {
        for (SocketProcessorKonchits sp : clients) {
            sp.write(packet);
        }
    }
}
