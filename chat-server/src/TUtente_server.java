/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dca
 */
public class TUtente_server extends Thread{
    //Comunicazione
    private Socket conSocket;
    private PrintWriter tx;   //per inviare i dati ad un livello più alto
    private BufferedReader rx;
    private boolean running;    
    //private TReader_server rx; //per ricevere i dati dal client
    //Caratteristiche utente
    private String nickname;
    private Server server;
    private String nomeStanza;//Nome della stanza a cui è collegato
    
    
    public TUtente_server(Socket s,Server server) throws IOException{
      this.server = server;
      conSocket = s;
      running=true;
      nickname="";
      nomeStanza="";
      //Ottengo canale di Tx
      OutputStream os = conSocket.getOutputStream();
      tx = new PrintWriter(os,true);//true ad ogni print viene invi del buffer dei dati da inviare      
      //Ottengo canale di RX (Thread)
      InputStream is = s.getInputStream();
      rx = new BufferedReader(new InputStreamReader(is));
      start();
    }
    
     @Override
     public void run() {//In ascolto di messaggi dal client
       while(running){
         try {
           recive(rx.readLine());
          } catch (IOException ex) {
              //Logger.getLogger(TUtente_server.class.getName()).log(Level.SEVERE, null, ex);
              //Scollegato !
              server.frame.appendMessage("[ "+getIp()+" , "+getPort()+" ] "+"si è scollegato");
              server.userLogOut(this);
              running=false;
              nomeStanza="";
          }
       }
     }
    
    public void setStanza(String s){nomeStanza=s;} 
    public void setRunning(boolean b){running=b;}    
    public void send(String mes){tx.println(mes);}
    public void recive(String msg) throws IOException{server.messFromUtente(msg, this);}
    public InetAddress getIp(){return conSocket.getInetAddress();}
    public int getPort(){return conSocket.getPort();}
    public String getStanza(){return nomeStanza;}
    public String getNickname(){return nickname;}
    public void setNickname(String n){nickname=n;}

    
    
}
