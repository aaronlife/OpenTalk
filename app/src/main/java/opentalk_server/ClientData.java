package opentalk_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientData
{
    private String uid;
    private String name;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private Security.AESKeySet aesKeySet;

    public ClientData(String id, String name, Socket socket, BufferedReader
                      reader, PrintWriter writer, Security.AESKeySet aesKeySet)
    {
        this.uid = id;
        this.name = name;
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;

        this.aesKeySet = aesKeySet;
    }

    public String getId()
    {
        return uid;
    }

    public void setId(String uuid)
    {
        uid = uuid;
    }

    public Security.AESKeySet getAesKeySet()
    {
        return aesKeySet;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public BufferedReader getReader()
    {
        return reader;
    }

    public synchronized void send(String cmd)
    {
        writer.println(cmd);
        writer.flush();

        Globals.log("=>", getId(), cmd);
    }

    public void close()
    {
        try
        {
            reader.close();
            writer.close();
            socket.close();
        }
        catch(IOException e)
        {}
    }
}