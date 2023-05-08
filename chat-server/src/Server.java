/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JFrame;

/**
 *
 * @author utente_2
 */
public class Server {
    protected Chat_serverGUI frame;
    private ArrayList <TUtente_server> utentiAttesa;//Utenti in attesa di entrare in una stanza
    private THandShakingSocket orecchioNuoviClient;//Thread sempre in ascolto di nuovi utenti aggiunti a utenti.
    private ArrayList<ChatRoom> stanze;

    public Server(int porta,Chat_serverGUI frame) throws IOException{
      utentiAttesa=new ArrayList<TUtente_server>();
      stanze = new ArrayList<ChatRoom>();
      stanze.add(new ChatRoom("DefaultRoom 1",null,""));
      stanze.add(new ChatRoom("DefaultRoom 2",null,""));
      stanze.add(new ChatRoom("DefaultRoom 3",null,""));
      //recuperaStanzeDaFile("stanzeSalvate");
      this.frame = frame;
      orecchioNuoviClient = new THandShakingSocket(true,porta,this);
      orecchioNuoviClient.start();//Lancia il thread per ascoltare nuove richieste di collegamento
    }
    public void recuperaStanzeDaFile(String filepath){
      BufferedReader br;
      String curline;
      try {
	br = new BufferedReader(new FileReader(filepath));
	while ((curline = br.readLine()) != null) {
          System.out.println(curline);
	}
	br.close();
      } catch (IOException e) {
        e.printStackTrace();
      }      
    }
    /*
    Viene richiamata ogni volta che un utente invia un messaggio al server
    */
    public void addNewUtenteAttesa(Socket newSocket) throws IOException{
      utentiAttesa.add(new TUtente_server(newSocket,this));
      //aux.send("Sei stato collegato!");
      //frame.appendMessage("Client[ "+newSocket.getInetAddress()+" , "+newSocket.getPort()+" ] connesso");
    }
    /*
    Viene richiamata ogni volta che un utente invia un messaggio al server
    */
    public void messFromUtente(String mes,TUtente_server u) throws IOException{//
      System.out.println("Messaggio ricevuto: "+mes);
      String[] mes_split = mes.split("-");
      String mesType = mes_split[0];
      switch(mesType){
          //case "USER_NAME"://Il client ha effettuato la scelta del nome.
            //updateUtenteName(mes_split[1],u.getIp(),u.getPort());
          //break;
          case "GET_STANZE"://Il client vuole sapere quali stanze sono presenti
            //Invio la lista di stanze sul server al client
            String nomeStanze="";//Nome1,nome2.....
            for(int i=0;i<stanze.size();i++){
              nomeStanze+=stanze.get(i).getName()+",";
            }
            frame.appendMessage("Invio stanze a [ "+u.getIp()+" , "+u.getPort()+" ]");
            u.send("STANZE-"+nomeStanze); 
          break;
          case "NUOVA_STANZA"://Richiesta di creazione di una niova stanza
            //Formato mes <nomeStanza,pwd>
              System.out.println(mes_split[1].split(",")[0]);
              if(existChatRoom(mes_split[1].split(",")[0])==-1){//Stanza non esistente
                addChatRoom(mes_split[1].split(",")[0],mes_split[1].split(",")[1],u);
                turnMessageToClientInAttesa("NUOVA_STANZA-"+mes_split[1].split(",")[0]);
                frame.appendMessage("Client [ "+u.getIp()+" , "+u.getPort()+"]: "+"Creazione stanza "+mes_split[1].split(",")[0]+ " riuscita.");
              }
              else{
                u.send("CREAZIONE_STANZA_FALLITA-");
                frame.appendMessage("Client [ "+u.getIp()+" , "+u.getPort()+"]: "+"Creazione stanza "+mes_split[1].split(",")[0]+" fallita.");
              }

          break;
          case "CANC_STANZA"://Stanza eliminata
            //Formato mes <nomeStanza,pwd>
            int pos=existChatRoom(mes_split[1].split(",")[0],mes_split[1].split(",")[1]);
            if(pos!=-1){//la stanza da cancellare esiste.
              turnMessageToClientInAttesa("CANC_STANZA-"+mes_split[1].split(",")[0]);
              stanze.get(pos).allertAllUtenti("EXIT_STANZA-");
              stanze.remove(pos);
            }else{
              u.send("CANCELLAZIONE_STANZA_FALLITA-");
            }
          break;                
          case "SCELTA_STANZA":
            //formato mes: stanzaScelta,username 
            if(!existUsernameInStanza(mes_split[1].split(",")[0],mes_split[1].split(",")[1])){//Nome valido
              u.setStanza(mes_split[1].split(",")[0]);
              u.setNickname(mes_split[1].split(",")[1]);
              logToChatRoom(u,mes_split[1].split(",")[0]);
              u.send("UTENTI_CONNESSI-"+getNameUtentiChatRoom(mes_split[1].split(",")[0],u));
              u.send("VALIDAZIONE-si");
              
              frame.appendMessage("Client [ "+u.getIp()+" , "+u.getPort()+" ] connesso alla stanza "+mes_split[1].split(",")[0]);
            }else{//Nome già inserito
              u.send("VALIDAZIONE-no");
              frame.appendMessage("Client [ "+u.getIp()+" , "+u.getPort()+" ] NON connesso alla stanza "+mes_split[1].split(",")[0]);
            }  
          break;
          case "MESS"://Un client ha inviato un messaggio formato<messaggio>
            frame.appendMessage("Client [ "+u.getIp()+" , "+u.getPort()+" ] ha inviato un messaggio nella stanza "+u.getStanza());
            turnMessageToClientInChat(u,u.getStanza(),mes_split[1]);
          break;
              
              
      }
    
    }
    public void addChatRoom(String nomeStanza,String pwd,TUtente_server u){
      stanze.add(new ChatRoom(nomeStanza,u,pwd));
    }
    public void userLogOut(TUtente_server u){
      if(u.getStanza().isEmpty()){//Utente non collegato ad una stanza
        removeUtenteInAttesa(u);  
      }
      else{//Utente collegato ad una stanza
        removeUtenteInStanza(u);
      }
    }
    /*
    Verifica se già esiste una chatroom con il nome name
    */
    public int existChatRoom(String name){
      for(int i=0;i<stanze.size();i++){
        if(stanze.get(i).getName().equals(name))
          return i;  
      }
      return -1;
    }
    public int existChatRoom(String nameStanza,String pwd){
      for(int i=0;i<stanze.size();i++){
        if(stanze.get(i).getName().equals(nameStanza) && stanze.get(i).getPwd().equals(pwd))
          return i;  
      }
      return -1;
    }    
    /*
    Ritorna una stringa che contiene il nome di tutte le stanze presenti.
    formato della stringa: nome1,nome2,....
    */
    public String getNomeStanze(){
      String nomeStanze="";//Nome1,nome2.....
      for(int i=0;i<stanze.size();i++)
        nomeStanze+=stanze.get(i).getName()+",";
      return nomeStanze;
    }
    public void turnMessageToClientInAttesa(String mes,TUtente_server u){
      for(TUtente_server aux : utentiAttesa){
        if((!aux.getIp().toString().equals(u.getIp().toString())) || (aux.getPort()!=u.getPort())){
            aux.send(mes);
        }
      }
    }
    public void turnMessageToClientInAttesa(String mes){
      for(TUtente_server aux : utentiAttesa){
            aux.send(mes);
      }
    }    
    public boolean turnMessageToClientInChat(TUtente_server u,String stanza,String mes){
      int pos=existChatRoom(stanza);
      if(pos==-1)return false;
      ChatRoom stanzaTarget=stanze.get(pos);
      stanzaTarget.sendNewMessage(u,mes);
      return true;
    }
    public boolean existUsernameInStanza(String stanza,String nome){
      int posStanza = existChatRoom(stanza);
      if(posStanza==-1)return false;
      return stanze.get(posStanza).containUserName(nome);
    }
    public String getNameUtentiChatRoom(String stanza,TUtente_server u){
      String nomiUtenti="";
      System.out.println(stanza);
      for (ChatRoom aux : stanze) {
          if (aux.getName().equals(stanza)) {
             nomiUtenti=aux.getUtentiName(u.getNickname());
          }
        }
      return nomiUtenti;
    }
    public void removeUtenteInAttesa(TUtente_server u){
      InetAddress ip = u.getIp();
      int porta = u.getPort();
      for(int i=0;i<utentiAttesa.size();i++){
        if (utentiAttesa.get(i).getIp().equals(ip) && utentiAttesa.get(i).getPort()==porta) {
          utentiAttesa.remove(i);
          break;  
        }
      }
    }
    public boolean removeUtenteInStanza(TUtente_server u){
      int posStanza = existChatRoom(u.getStanza());
      if(posStanza==-1)return false;
      stanze.get(posStanza).removeUser(u.getNickname());
      return true;
    }
 
    public boolean logToChatRoom(TUtente_server u,String stanza){
        for (ChatRoom aux : stanze) {
          if (aux.getName().equals(stanza)) {
            aux.addUtente(u);
            removeUtenteInAttesa(u);
            return true;
          }
        }
        return false;
    } 
    
    public void updateUtenteName(String name,InetAddress ip,int porta){
      
        for (TUtente_server aux : utentiAttesa) {
            if (aux.getPort()==porta && aux.getIp().equals(ip)) {
                aux.setNickname(name);
                frame.appendMessage("Client[ "+ip+" , "+porta+" ] si è collegato come: "+name);
                break;
            }
        }
        
    }
}
