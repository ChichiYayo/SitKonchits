package server.network.Konchits;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.network.packets.Konchits.BaseReadPacketKonchits;
import server.network.packets.Konchits.BaseWritePacketKonchits;
import server.network.packets.client.Konchits.AddDataKonchits;
import server.network.packets.client.Konchits.DeleteDataKonchits;
import server.network.packets.client.Konchits.DisconnectKonchits;
import server.network.packets.client.Konchits.ExportKonchits;
import server.network.packets.client.Konchits.GetDataKonchits;
import server.network.packets.client.Konchits.ImportKonchits;
import server.network.packets.client.Konchits.LoginKonchits;
import server.network.packets.client.Konchits.SaveDataKonchits;
import server.network.packets.client.Konchits.TaskKonchits;
import server.network.packets.threading.Konchits.ThreadPoolManagerKonchits;

public class SocketProcessorKonchits implements Runnable {

    private final int MAX_PKT_SIZE = 0xFFFF;
    private final Socket s;
    private InputStream is;
    private OutputStream os;

    public SocketProcessorKonchits(Socket s) {
        this.s = s;
        try {
            is = s.getInputStream();
            os = s.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(SocketProcessorKonchits.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        while (!s.isClosed()) {
            try {
                byte[] buffer = new byte[MAX_PKT_SIZE];
                int pkgSize = is.read(buffer);
                if (pkgSize != -1) {
                    byte[] temp = new byte[pkgSize];
                    System.arraycopy(buffer, 0, temp, 0, pkgSize);
                    parse(temp);
                }
            } catch (IOException ex) {
                System.out.println("SocketProcessor -> disconnect");
                return;
            }
        }
        System.out.println("SocketProcessor -> disconnect");
    }

    private void parse(byte[] data) {
        BaseReadPacketKonchits packet = null;
        switch (data[0]) {
            case 0x01:
                packet = new DisconnectKonchits(data, this);
                break;
            case 0x02:
                packet = new LoginKonchits(data, this);
                break;
            case 0x10:
                packet = new GetDataKonchits(data, this);
                break;
            case 0x11:
                packet = new DeleteDataKonchits(data, this);
                break;
            case 0x12:
                packet = new SaveDataKonchits(data, this);
                break;
            case 0x13:
                packet = new AddDataKonchits(data, this);
                break;
            case 0x14:
                packet = new ExportKonchits(data, this);
                break;
            case 0x15:
                packet = new ImportKonchits(data, this);
                break;
            case 0x16:
                packet = new TaskKonchits(data, this);
                break;                

            default:
                break;
        }

        if (packet != null) {
            ThreadPoolManagerKonchits.execute(packet);
        } else {
            System.out.println("Error packet");
        }
    }

    public void closeConnection() {
        try {
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(SocketProcessorKonchits.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void write(BaseWritePacketKonchits packet) {
        if (os == null) {
            return;
        }

        byte[] data = packet.getContent();
        System.out.println("write size = " + data.length);
        try {
            synchronized (os) {
                try {
                    os.write(data);
                    os.flush();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        } catch (java.lang.NullPointerException e) {
            System.out.println(e);
        }
    }

    public Socket getSocket() {
        return s;
    }
}