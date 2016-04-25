import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
public class ClientHandler implements Runnable {
    public int HTTP = 80;
    public int content_length;
    public byte[] clcontainer;
    public String url;
    public Socket withBroswer;
    public Socket withWeb;
    public boolean debug = false;
    public DataInputStream in;
    public DataOutputStream out;
    public DataOutputStream outoweb;
    public DataInputStream infrweb;
    public String Command = "";
    public boolean isPost = false;
    public HashMap<String, String> headerMaps;
    public byte[] inbytes = new byte[4096];
    public char intemp = '\n';
    public char retemp = '\n';
    ClientHandler(Socket s) throws IOException {
        this.withBroswer = s;
        in = new DataInputStream(s.getInputStream());
        headerMaps = new HashMap<String, String>();
        OutputStream os = s.getOutputStream();
        out = new DataOutputStream(os);
    }
    /**public String inReadLine() throws IOException {
        int ch = 0;
        String string = "";
        if (intemp - '\n' != 0) string += (char) intemp;
        while ((ch = in.read()) != -1) {
            if (ch == '\r') {
                if (debug) System.out.print("line is:" + string + "\r\n");
                intemp = (char) in.read();
                return string;
            }
            string += (char) ch;
        }
        if(string.equals("")) return null;
        return string;
    }*/
    public void reReadLine() throws IOException {
        /**int ch = 0;
        StringBuffer string = new StringBuffer();
        if (retemp - '\n' != 0) string.append((char)retemp);
        while ((ch = infrweb.read()) != -1) {
            if (ch == '\r') {
                if (debug) System.out.print("line is:" + string + "\r\n");
                retemp = (char) infrweb.read();
                return string;
            }
            string.append((char)ch);
        }
        if(string.equals("")) return null;
        return string;*/
        int ch;
        while ((ch = infrweb.read()) != -1) {
            out.write((char)ch);
        }
    }
    public void DealWithCommand(String s) {
        String[] comcon;
        comcon = s.split(" ");
        String file_name = "";
        try {
            file_name += comcon[1].substring(comcon[1].indexOf('/', 8), comcon[1].length());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("error s" + s);
        }
        isPost = comcon[0].toLowerCase().equals("post");
        Command += comcon[0] + " " + file_name + " " + "HTTP/1.0";
        if (debug) System.out.print("new line is:" + Command + "\r\n");
    }
    public void DealWithHash(String[] stringarray) {
        if (stringarray[0].equalsIgnoreCase("user-agent") || stringarray[0].equalsIgnoreCase("referer") || stringarray[0].equalsIgnoreCase("proxy-connection") || stringarray[1].equalsIgnoreCase("  Keep-Alive"))
            return;
            headerMaps.put(stringarray[0].toLowerCase() + ":", stringarray[1].toLowerCase() + "\r\n");
        return;
    }
    public void ReadHeadlers() throws IOException {
        String lines = in.readLine();
        while (!lines.equals("")) {
            String[] KEY_VALUE = lines.split(":");
            DealWithHash(KEY_VALUE);
            lines = in.readLine();
        }
    }
    public void OpenRemoteConnection() throws IOException {
        if (!headerMaps.isEmpty()) {
            String url1 = headerMaps.get("host:").toString();
            url = url1.substring(1, url1.length() - 2);
            if (debug) System.out.println("url:" + url);
            withWeb = new Socket(url, HTTP);
        }
        OutputStream temout = withWeb.getOutputStream();
        InputStream temin = withWeb.getInputStream();
        outoweb = new DataOutputStream(temout);
        infrweb = new DataInputStream(temin);
    }
    public void SentToWeb() throws IOException {
        outoweb.writeBytes(Command + "\r\n");
        if (debug) System.out.print("send command:" + Command);
        Iterator<String> iter = headerMaps.keySet().iterator();
        while (iter.hasNext()) {
            String temp = iter.next();
            outoweb.writeBytes(temp + headerMaps.get(temp));
        }
        outoweb.writeBytes("\r\n");
        outoweb.flush();
    }
    /**public void SentBackToBroswer() throws IOException {
        StringBuffer s = reReadLine();

        while (s != null) {
                System.out.println("out status:"+withBroswer.isClosed());
                out.write(s.toString().getBytes());
                if (retemp - '\n' == 0) out.writeBytes("\r\n");
                else out.writeBytes("\r");

            if (debug) System.out.println("back massage:" + s + "\r\n");
            //out.writeBytes("\r\n");
            s = reReadLine();
        }
        //out.writeBytes("\r\n");
        out.flush();
    }*/
    public void finish() throws IOException {
        infrweb.close();
        outoweb.close();
        in.close();
        out.close();
    }
    public void judge_and_do() throws IOException {
        if (headerMaps.containsKey("content-length:")) {
            String cl = headerMaps.get("content-length:");
            content_length = Integer.valueOf(cl.substring(1, cl.length() - 2));
            clcontainer = new byte[content_length];
            try {
                in.read(clcontainer, 0, content_length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            outoweb.write(clcontainer);
        }
    }
    @Override
    public void run() {
        String command = "";
        try {
            command = in.readLine();
            DealWithCommand(command);
            ReadHeadlers();
            OpenRemoteConnection();
            SentToWeb();
            if(isPost)judge_and_do();
            reReadLine();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getCause());
            System.out.println("error");
        }
    }
}
