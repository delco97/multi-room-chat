/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dca
 */
public class TReader_client extends Thread{
  private BufferedReader rx;
  private boolean running;
  private Chat_clientGUI utente;

  public TReader_client(Socket s,Chat_clientGUI utente) throws IOException{
    InputStream is = s.getInputStream();
    rx = new BufferedReader(new InputStreamReader(is));
    running=true;
    this.utente=utente;
  }
  
  @Override
  public void run() {
      while(running){
          try {
            utente.messFromServer(rx.readLine());  
          } catch (Exception ex) {
              //Logger.getLogger(TReader_client.class.getName()).log(Level.SEVERE, null, ex);
              //SERVER GIU!
              utente.serverDown();
              running=false;
          }
      }
  }
    
  public void setRunning(boolean b){running=b;}
}
