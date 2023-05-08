/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;

/**
 *
 * @author dca
 */
public class ChatRoom {
    private ArrayList <TUtente_server> utenti;//Utenti connessi alla stanza
    private String nome;//Nome della stanza
    private TUtente_server creatore;
    private String pwd;
    public ChatRoom(String nome,TUtente_server nomeStanza,String pwd){
      utenti = new ArrayList<TUtente_server>();
      this.nome = nome;
      this.creatore = creatore;
      this.pwd=pwd;
    }
    public void sendNewMessage(TUtente_server u,String mes){
        //System.out.println("Giro il mes");
        for (TUtente_server aux : utenti) {
           if((aux.getIp().toString().equals(u.getIp().toString())) && aux.getPort()==u.getPort()){//il messaggio deve essere in questo formato MESS-<IP,porta,nome,messaggio>

           }
           else{
               //System.out.println("invio: MESS-"+aux.getNickname()+","+mes);
               aux.send("MESS-"+u.getNickname()+","+mes);           
           }
        }        
    }
    public String getName(){return nome;}
    public String getPwd(){return pwd;}
    public int getNumUntentiConnessi(){return utenti.size();}
    public void addUtente(TUtente_server u){
      allertAllUtenti("NUOVO_UTENTE-"+u.getNickname());    
      utenti.add(u);
    }
    public boolean containUserName(String uName){
      for(TUtente_server aux:utenti){
        //System.out.println(aux.getNickname()+" == "+uName);
        if(aux.getNickname().equals(uName))return true;        
      }
      return false;
    }
    public boolean removeUser(String name){
      for(int i=0;i<utenti.size();i++){
          if(utenti.get(i).getNickname().equals(name)){
            utenti.remove(i);
            System.out.println("Utente "+name+" rimosso.");
            allertAllUtenti("LOGOUT_UTENTE-"+name);
            return true;
          }
      }
      return false;
    }
    public String getUtentiName(String nonPrendere){//formato <nome1,nome2,  . . .>
      String nomi="";
      for(TUtente_server aux:utenti){
        System.out.println(aux.getNickname()+"=="+nonPrendere);
          if(!(aux.getNickname().equals(nonPrendere)))
            nomi+=aux.getNickname()+",";
      }
      return nomi;
    }
    public void allertAllUtenti(String mes){
        for(TUtente_server aux : utenti){
            aux.send(mes);
        }
    }
}
