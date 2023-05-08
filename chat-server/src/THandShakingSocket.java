/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dca
 */
public class THandShakingSocket extends Thread{
  private ServerSocket listener;
  private boolean running=true;
  private Server server;
  
  public THandShakingSocket(boolean start,int porta,Server server) throws IOException{
    listener = new ServerSocket(porta); 
    running=start;
    this.server=server;
  }
  public THandShakingSocket(){
    running=false;
  }  

  @Override
  public void run() {
      while(running){
          try {
              Socket newConnection = listener.accept();
              server.addNewUtenteAttesa(newConnection);
          } catch (IOException ex) {
              Logger.getLogger(THandShakingSocket.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
  }
  public void setRunning(boolean b){running=b;}

  
  
  
}
